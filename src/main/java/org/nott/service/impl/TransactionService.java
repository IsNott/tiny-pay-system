package org.nott.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.nott.dto.TradeNotifyDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayTransactionInfo;
import org.nott.enums.*;
import org.nott.exception.PayException;
import org.nott.id.TransactionNoFactory;
import org.nott.mapper.PayOrderInfoMapper;
import org.nott.mapper.PayTransactionInfoMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * PayTransactionInfo 服务类
 * @author Nott
 * @date 2024-5-7
 */

@Service
public class TransactionService {

    @Resource
    private PayTransactionInfoMapper payTransactionInfoMapper;
    @Resource
    private PayOrderInfoMapper payOrderInfoMapper;

    @Resource
    private OrderService orderService;

    public List<PayTransactionInfo> getTransactionByOrder(Long orderId){
        LambdaQueryWrapper<PayTransactionInfo> wrapper = new LambdaQueryWrapper<PayTransactionInfo>()
                .eq(PayTransactionInfo::getInOrderId, orderId);
        List<PayTransactionInfo> transactionInfos = payTransactionInfoMapper.selectList(wrapper);
        return transactionInfos;
    }

    public PayTransactionInfo getSingleTransactionByOrder(Long orderId){
        List<PayTransactionInfo> transactionByOrder = this.getTransactionByOrder(orderId);
        if(transactionByOrder == null || transactionByOrder.isEmpty()){
            throw new PayException(String.format("根据OrderId:[%s]找不到交易数据", orderId));
        }
        return transactionByOrder.get(0);
    }

    public PayTransactionInfo createTransactionByOrder(PayOrderInfo payOrderInfo) {
        boolean isRefund = OrderTypeEnum.REFUND.getCode().equals(payOrderInfo.getOrderType());
        // 关联退款与支付交易记录
        String orgTransactionNo;

        PayTransactionInfo payTransactionInfo = new PayTransactionInfo();
        payTransactionInfo.setInOrderId(payOrderInfo.getId());
        payTransactionInfo.setTransactionType(payOrderInfo.getOrderType());
        payTransactionInfo.setTransactionNo(TransactionNoFactory.next());
        payTransactionInfo.setTransactionStatus(StatusEnum.INIT.getCode());

        if(isRefund){
            Long refundOrderNo = payOrderInfo.getOrderNo();
            PayOrderInfo payOrder = orderService.findPayOrderByRefundOrderNo(refundOrderNo);
            orgTransactionNo = payOrder.getInTransactionNo();
            payTransactionInfo.setOrgTransactionNo(orgTransactionNo);
        }

        payTransactionInfoMapper.insert(payTransactionInfo);

        payOrderInfo.setInTransactionNo(payTransactionInfo.getTransactionNo());
        payOrderInfoMapper.updateById(payOrderInfo);
        return payTransactionInfo;
    }

    public int updateTradeStateByACS(String outTradeNo, String inTransactionNo) {
        LambdaUpdateWrapper<PayTransactionInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(PayTransactionInfo::getNotifyTime,new Date());
        wrapper.set(PayTransactionInfo::getOutTransactionNo,outTradeNo);
        wrapper.set(PayTransactionInfo::getTransactionStatus,StatusEnum.PAY_SUCCESS.getCode());
        wrapper.eq(PayTransactionInfo::getTransactionNo,inTransactionNo);
        wrapper.isNull(PayTransactionInfo::getOutTransactionNo);
        int update = payTransactionInfoMapper.update(wrapper);
        if(update == 0){
            throw new PayException(String.format("交易%s已被其他线程更新，放弃当前通知", inTransactionNo));
        }
        return update;
    }

    public void updateRefundStateByACS(String outTradeNo, TradeNotifyDTO notifyDTO) {
        String payTradeNo = notifyDTO.getOutTradeNo();
        LambdaUpdateWrapper<PayTransactionInfo> wrapper = new LambdaUpdateWrapper<PayTransactionInfo>()
                .eq(PayTransactionInfo::getTransactionNo,payTradeNo)
                .eq(PayTransactionInfo::getTransactionStatus,StatusEnum.REFUNDING.getCode());
        PayTransactionInfo payTransactionInfo = payTransactionInfoMapper.selectOne(wrapper);
        if(payTransactionInfo == null){
            return;
        }
        LambdaQueryWrapper<PayOrderInfo> queryWrapper = new LambdaQueryWrapper<PayOrderInfo>()
                .eq(PayOrderInfo::getInTransactionNo, payTransactionInfo.getTransactionNo())
                .eq(PayOrderInfo::getPayStatus,StatusEnum.REFUNDING.getCode());
        PayOrderInfo payOrderInfo = payOrderInfoMapper.selectOne(queryWrapper);
        if(payOrderInfo == null){
            return;
        }
        Long refundOrderNo = payOrderInfo.getRefundOrderNo();
        PayOrderInfo refundOrder = orderService.getByOrderNo(String.valueOf(refundOrderNo), OrderTypeEnum.REFUND.getCode(), RefundStatusEnum.REFUNDING.getCode());
        if(refundOrder == null){
            return;
        }
        String transactionNo = refundOrder.getInTransactionNo();
        this.updateTradeStateByACS(outTradeNo,transactionNo);
    }

    public PayTransactionInfo checkIfExist(String outTradeNo) {
        LambdaQueryWrapper<PayTransactionInfo> wrapper = new LambdaQueryWrapper<PayTransactionInfo>().eq(PayTransactionInfo::getTransactionNo, outTradeNo);
        PayTransactionInfo payTransactionInfo = payTransactionInfoMapper.selectOne(wrapper);
        if (Objects.isNull(payTransactionInfo)) {
            throw new PayException(String.format("没有找到流水号为%s的交易记录", outTradeNo));
        }
        return payTransactionInfo;
    }

    public void updateTradeBusinessInfo(TradeNotifyDTO notifyDTO) {
        OutTradePlatform outTradePlatform = notifyDTO.getOutTradePlatform();
        switch (outTradePlatform){
            case ALIPAY -> {
                if (notifyDTO.getBusinessEnum() == null) {
                    throw new PayException("支付宝通知消息中没有业务类型");
                }
                if (notifyDTO.getBusinessEnum() == BusinessEnum.PAY && "TRADE_SUCCESS".equals(notifyDTO.getTradeStatus())) {
                    this.updateTradeStateByACS(notifyDTO.getOutTradeNo(), notifyDTO.getInTradeNo());
                } else if (notifyDTO.getBusinessEnum() == BusinessEnum.REFUND && "TRADE_CLOSED".equals(notifyDTO.getTradeStatus())) {
                    this.handleRefundMessage(notifyDTO.getPayTransactionInfoId(), notifyDTO);
                }
            }
            default -> throw new PayException("不支持的支付平台");
        }
    }

    private void handleRefundMessage(Long payTransactionInfoId, TradeNotifyDTO notifyDTO) {
        // 更新退款记录
        PayTransactionInfo orgPayTransactionInfo = payTransactionInfoMapper.selectById(payTransactionInfoId);
        String transactionNo = orgPayTransactionInfo.getTransactionNo();
        PayTransactionInfo refundTransaction = getRefundTransactionByOrgNo(transactionNo);
        PayOrderInfo orgPayOrder = orderService.getByTransactionNo(transactionNo, OrderTypeEnum.PAY.getCode());
        PayOrderInfo refundOrder = orderService.getByTransactionNo(refundTransaction.getTransactionNo(), OrderTypeEnum.REFUND.getCode());
        // 更新原始订单及交易记录
        orgPayOrder.setPayStatus(StatusEnum.REFUND.getCode());
        orgPayTransactionInfo.setTransactionStatus(StatusEnum.REFUND.getCode());

        // 更新退款订单及交易记录
        refundTransaction.setTransactionStatus(RefundStatusEnum.REFUNDED.getCode());
        refundOrder.setPayStatus(RefundStatusEnum.REFUNDING.getCode());

        orderService.updateById(refundOrder);
        orderService.updateById(orgPayOrder);

        payTransactionInfoMapper.updateById(refundTransaction);
        payTransactionInfoMapper.updateById(orgPayTransactionInfo);
    }

    public PayTransactionInfo getRefundTransactionByOrgNo(String orgTransactionNo){
        LambdaUpdateWrapper<PayTransactionInfo> wrapper = new LambdaUpdateWrapper<PayTransactionInfo>()
                .eq(PayTransactionInfo::getOrgTransactionNo, orgTransactionNo);
        PayTransactionInfo payTransactionInfo = payTransactionInfoMapper.selectOne(wrapper);
        if(payTransactionInfo == null){
            throw new PayException("根据原始交易号%s，找不到对应退款交易记录".formatted(orgTransactionNo));
        }
        return payTransactionInfo;
    }

    private void handlePayMessage(Long payTransactionInfoId, TradeNotifyDTO notifyDTO) {
        // 更新交易记录
        PayTransactionInfo payTransactionInfo = payTransactionInfoMapper.selectById(payTransactionInfoId);
        payTransactionInfo.setOutNotifyMsg(JSON.toJSONString(notifyDTO));
        payTransactionInfo.setTransactionStatus(StatusEnum.PAY_SUCCESS.getCode());
        // 更新订单
        PayOrderInfo orderInfo = payOrderInfoMapper.selectById(payTransactionInfo.getInOrderId());
        orderInfo.setPayStatus(StatusEnum.PAY_SUCCESS.getCode());
        payTransactionInfoMapper.updateById(payTransactionInfo);
        payOrderInfoMapper.updateById(orderInfo);
    }


    public PayTransactionInfo checkAndReturnPayTransaction(PayOrderInfo payOrderInfo) {
        Long orderNo = payOrderInfo.getOrderNo();
        PayTransactionInfo payTransactionInfo = null;

        List<PayTransactionInfo> transactionInfos = this.getTransactionByOrder(payOrderInfo.getId());
        if (transactionInfos.isEmpty()) {
            throw new PayException(String.format("订单：[%s],没有找到对应的交易记录，请检查", orderNo));
        }

        // 订单与外部交易记录关系为一对一，如果有支付中的状态，可能是别的线程已经操作过或者已经失败
        payTransactionInfo = transactionInfos.get(0);
        Integer transactionStatus = payTransactionInfo.getTransactionStatus();
        if (!StatusEnum.INIT.getCode().equals(transactionStatus)) {
            throw new PayException(String.format("订单：[%s],已有支付中/失败状态，请检查后重试", orderNo));
        }
        return payTransactionInfo;
    }
}
