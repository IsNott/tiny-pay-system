package org.nott.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.nott.dto.TradeNotifyDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayTransactionInfo;
import org.nott.enums.OrderTypeEnum;
import org.nott.enums.RefundStatusEnum;
import org.nott.enums.StatusEnum;
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
        //todo 关联退款与支付交易记录

        PayTransactionInfo payTransactionInfo = new PayTransactionInfo();
        payTransactionInfo.setInOrderId(payOrderInfo.getId());
        payTransactionInfo.setTransactionType(payOrderInfo.getOrderType());
        payTransactionInfo.setTransactionNo(TransactionNoFactory.next());
        payTransactionInfo.setTransactionStatus(StatusEnum.INIT.getCode());

        payTransactionInfoMapper.insert(payTransactionInfo);

        payOrderInfo.setInTransactionNo(payTransactionInfo.getTransactionNo());
        payOrderInfoMapper.updateById(payOrderInfo);
        return payTransactionInfo;
    }

    public int updateTradeStateByACS(String outTradeNo, String inTransactionNo) {
        LambdaUpdateWrapper<PayTransactionInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(PayTransactionInfo::getNotifyTime,new Date());
        wrapper.set(PayTransactionInfo::getOutTransactionNo,outTradeNo);
        wrapper.eq(PayTransactionInfo::getTransactionNo,inTransactionNo);
        wrapper.isNull(PayTransactionInfo::getOutTransactionNo);
        int update = payTransactionInfoMapper.update(wrapper);
        if(update == 0){
            throw new PayException(String.format("交易%s已被其他线程更新，放弃当前通知", inTransactionNo));
        }
        return update;
    }

    public void updateRefundStateByACS(String outTradeNo, TradeNotifyDTO tradeNotifyDTO) {
        String payTradeNo = tradeNotifyDTO.getOut_trade_no();
        LambdaUpdateWrapper<PayTransactionInfo> wrapper = new LambdaUpdateWrapper<PayTransactionInfo>()
                .eq(PayTransactionInfo::getTransactionNo,payTradeNo)
                .eq(PayTransactionInfo::getTransactionStatus,StatusEnum.REFUNDING);
        PayTransactionInfo payTransactionInfo = payTransactionInfoMapper.selectOne(wrapper);
        if(payTransactionInfo == null){
            return;
        }
        LambdaQueryWrapper<PayOrderInfo> queryWrapper = new LambdaQueryWrapper<PayOrderInfo>()
                .eq(PayOrderInfo::getInTransactionNo, payTransactionInfo.getTransactionNo())
                .eq(PayOrderInfo::getPayStatus,StatusEnum.REFUNDING);
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

    public void updateTradeBusinessInfo(Long payTransactionInfoId, TradeNotifyDTO tradeNotifyDTO) {
        String tradeStatus = tradeNotifyDTO.getTrade_status();
        switch (tradeStatus){
            default -> throw new PayException("Unknown Trade State from Alipay notify message");
            case "TRADE_SUCCESS" -> {
                this.handlePayMessage(payTransactionInfoId,tradeNotifyDTO);
            }
            case "TRADE_CLOSED" -> {
                this.handleRefundMessage(payTransactionInfoId, tradeNotifyDTO);
            }
        }
    }

    private void handleRefundMessage(Long payTransactionInfoId, TradeNotifyDTO tradeNotifyDTO) {
        // todo 更新退款记录
    }

    private void handlePayMessage(Long payTransactionInfoId, TradeNotifyDTO tradeNotifyDTO) {
        // 更新交易记录
        PayTransactionInfo payTransactionInfo = payTransactionInfoMapper.selectById(payTransactionInfoId);
        payTransactionInfo.setOutNotifyMsg(JSON.toJSONString(tradeNotifyDTO));
        payTransactionInfo.setTransactionStatus(StatusEnum.PAY_SUCCESS.getCode());
        // 更新订单
        PayOrderInfo orderInfo = payOrderInfoMapper.selectById(payTransactionInfo.getInOrderId());
        orderInfo.setPayStatus(StatusEnum.PAY_SUCCESS.getCode());
        payTransactionInfoMapper.updateById(payTransactionInfo);
        payOrderInfoMapper.updateById(orderInfo);
    }


}
