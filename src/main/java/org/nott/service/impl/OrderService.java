package org.nott.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.nott.dto.CreateOrderDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayTransactionInfo;
import org.nott.enums.RefundStatusEnum;
import org.nott.enums.StatusEnum;
import org.nott.enums.OrderTypeEnum;
import org.nott.exception.PayException;
import org.nott.mapper.PayOrderInfoMapper;
import org.nott.mapper.PayTransactionInfoMapper;
import org.nott.vo.OrderQueryVo;
import org.nott.vo.PayOrderInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PayOrderInfo 相关操作服务类
 *
 * @author Nott
 * @date 2024-5-7
 */

@Service
public class OrderService extends ServiceImpl<PayOrderInfoMapper, PayOrderInfo> {

    @Resource
    private PayOrderInfoMapper payOrderInfoMapper;

    @Resource
    private TransactionService transactionService;

    @Resource
    private PayTransactionInfoMapper payTransactionInfoMapper;

    public PayOrderInfoVo initializeOrder(CreateOrderDTO createOrderDTO) {
        String orderParam = JSON.toJSONString(createOrderDTO);

        PayOrderInfo payOrderInfo = new PayOrderInfo();
        BeanUtils.copyProperties(createOrderDTO, payOrderInfo);
        payOrderInfo.setOrderNo(IdWorker.getId());
        payOrderInfo.setOrderType(OrderTypeEnum.PAY.getCode());
        payOrderInfo.setPayStatus(StatusEnum.INIT.getCode());
        payOrderInfo.setOrderParam(orderParam);
        payOrderInfoMapper.insert(payOrderInfo);
        transactionService.createTransactionByOrder(payOrderInfo);

        PayOrderInfoVo vo = new PayOrderInfoVo();
        BeanUtils.copyProperties(payOrderInfo, vo);
        return vo;
    }

    public PayOrderInfo getByOrderNo(String orderNo, Integer type, Integer status) {
        LambdaQueryWrapper<PayOrderInfo> wrapper = new LambdaQueryWrapper<PayOrderInfo>().eq(PayOrderInfo::getOrderNo, orderNo)
                .eq(status != null, PayOrderInfo::getPayStatus, status)
                .eq(PayOrderInfo::getOrderType, type);
        List<PayOrderInfo> payOrderInfos = payOrderInfoMapper.selectList(wrapper);
        if (payOrderInfos.isEmpty()) {
            throw new PayException(String.format("订单：[%s]没有相关记录", orderNo));
        }
        return payOrderInfos.get(0);
    }

    public PayOrderInfo getByTransactionNo(String transactionNo, Integer type) {
        LambdaUpdateWrapper<PayOrderInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayOrderInfo::getInTransactionNo, transactionNo)
                .eq(PayOrderInfo::getOrderType, type);
        List<PayOrderInfo> payOrderInfos = payOrderInfoMapper.selectList(wrapper);
        if (payOrderInfos == null || payOrderInfos.isEmpty()) {
            throw new PayException(String.format("根据交易号%s，没有找到类型为%s的订单记录", transactionNo, type));
        }
        return payOrderInfos.get(0);
    }

    // 生成内部退款单
    public PayOrderInfo initializeRefundOrder(String orderNo) {
        PayOrderInfo payOrder = this.getByOrderNo(orderNo, OrderTypeEnum.PAY.getCode(), StatusEnum.PAY_SUCCESS.getCode());
        Long refundOrderNo = payOrder.getRefundOrderNo();
        boolean alreadyHasRefund = payOrder.getRefundOrderNo() != null;
        if (alreadyHasRefund) {
            PayOrderInfo refundOrder = this.getByOrderNo(String.valueOf(payOrder.getRefundOrderNo()), OrderTypeEnum.REFUND.getCode(), StatusEnum.INIT.getCode());
            if (refundOrder == null) {
                throw new PayException(String.format("退款单：%s，已有退款中记录，请稍后重试", refundOrderNo));
            }
            return refundOrder;
        }
        refundOrderNo = IdWorker.getId();
        PayOrderInfo refundOrder = new PayOrderInfo();
        BeanUtils.copyProperties(payOrder, refundOrder,
                "id", "updateTime", "orderParam", "createTime",
                "orderType", "payStatus", "inTransactionNo");
        refundOrder.setOrderType(OrderTypeEnum.REFUND.getCode());
        refundOrder.setOrderNo(refundOrderNo);
        refundOrder.setPayStatus(StatusEnum.INIT.getCode());
        payOrder.setRefundOrderNo(refundOrderNo);

        LambdaUpdateWrapper<PayOrderInfo> updateWrapper = new LambdaUpdateWrapper<PayOrderInfo>().eq(PayOrderInfo::getId, payOrder.getId())
                .eq(PayOrderInfo::getPayStatus, StatusEnum.PAY_SUCCESS.getCode())
                .set(PayOrderInfo::getRefundOrderNo, refundOrderNo);
        payOrderInfoMapper.update(updateWrapper);
        payOrderInfoMapper.insert(refundOrder);
        transactionService.createTransactionByOrder(refundOrder);

        return refundOrder;
    }

    public void updatePayStatusAndOutTradeInfo(PayOrderInfo payOrderInfo, PayTransactionInfo payTransactionInfo, Integer preStatus, Integer updateStatus) {
        LambdaUpdateWrapper<PayOrderInfo> wrapper = new LambdaUpdateWrapper<PayOrderInfo>()
                .eq(PayOrderInfo::getPayStatus, preStatus)
                .eq(PayOrderInfo::getId, payOrderInfo.getId())
                .set(StringUtils.isNotEmpty(payOrderInfo.getPaymentCode()),
                        PayOrderInfo::getPaymentCode, payOrderInfo.getPaymentCode())
                .set(PayOrderInfo::getPayStatus, updateStatus);
        payOrderInfoMapper.update(wrapper);

        LambdaUpdateWrapper<PayTransactionInfo> updateWrapper = new LambdaUpdateWrapper<PayTransactionInfo>()
                .eq(PayTransactionInfo::getTransactionStatus, preStatus)
                .eq(PayTransactionInfo::getId, payTransactionInfo.getId())
                .set(StringUtils.isNotEmpty(payTransactionInfo.getOutTransactionParam()),
                        PayTransactionInfo::getOutTransactionParam, payTransactionInfo.getOutTransactionParam())
                .set(PayTransactionInfo::getTransactionStatus, updateStatus);
        payTransactionInfoMapper.update(updateWrapper);
    }

    public PayOrderInfo findPayOrderByRefundOrderNo(Long refundOrderNo) {
        LambdaQueryWrapper<PayOrderInfo> queryWrapper = new LambdaQueryWrapper<PayOrderInfo>().eq(PayOrderInfo::getRefundOrderNo, refundOrderNo)
                .eq(PayOrderInfo::getPayStatus, StatusEnum.PAY_SUCCESS.getCode());
        PayOrderInfo orgPayOrder = payOrderInfoMapper.selectOne(queryWrapper);
        if (orgPayOrder == null) {
            throw new PayException(String.format("退款订单号：%s，没有可退款的原始支付记录", refundOrderNo));
        }
        return orgPayOrder;
    }


    public OrderQueryVo queryOrderStatus(String orderNo) {
        LambdaQueryWrapper<PayOrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrderInfo::getOrderNo, orderNo);
        PayOrderInfo orderInfo = payOrderInfoMapper.selectOne(wrapper);
        OrderQueryVo vo = new OrderQueryVo();
        BeanUtils.copyProperties(orderInfo, vo);
        //TODO 处于pending的状态需要查询一下外部交易系统
        if (StatusEnum.PAYING.getCode().equals(vo.getPayStatus()) && OrderTypeEnum.PAY.getCode().equals(orderInfo.getOrderType())) {

        }
        if ((StatusEnum.REFUNDING.getCode().equals(vo.getPayStatus()) && OrderTypeEnum.PAY.getCode().equals(orderInfo.getOrderType())) ||
                (RefundStatusEnum.REFUNDING.getCode().equals(vo.getPayStatus()) && OrderTypeEnum.REFUND.getCode().equals(orderInfo.getOrderType()))) {

        }

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRefundStatus(String payOrderNo, String refundOrderNo) {
        LambdaQueryWrapper<PayOrderInfo> wrapper = new LambdaQueryWrapper<PayOrderInfo>().eq(PayOrderInfo::getOrderNo, payOrderNo)
                .eq(PayOrderInfo::getPayStatus, StatusEnum.REFUNDING.getCode());
        PayOrderInfo orderInfo = payOrderInfoMapper.selectOne(wrapper);
        // 查找支付订单状态是否已经被更新为退款中
        if (orderInfo != null) {
            return;
        }

        PayOrderInfo orgPayOrder = getByOrderNo(payOrderNo, OrderTypeEnum.PAY.getCode(), StatusEnum.PAY_SUCCESS.getCode());
        PayOrderInfo refundOrder = getByOrderNo(refundOrderNo, OrderTypeEnum.REFUND.getCode(), StatusEnum.INIT.getCode());

        PayTransactionInfo payTransactionInfo = transactionService.getSingleTransactionByOrder(orgPayOrder.getId());
        PayTransactionInfo refundTransactionInfo = transactionService.getSingleTransactionByOrder(refundOrder.getId());

        orgPayOrder.setPayStatus(StatusEnum.REFUNDING.getCode());
        refundOrder.setPayStatus(RefundStatusEnum.REFUNDING.getCode());
        payTransactionInfo.setTransactionStatus(StatusEnum.REFUNDING.getCode());
        refundTransactionInfo.setTransactionStatus(RefundStatusEnum.REFUNDING.getCode());
        payTransactionInfoMapper.updateById(payTransactionInfo);
        payTransactionInfoMapper.updateById(refundTransactionInfo);

        payOrderInfoMapper.updateById(orgPayOrder);
        payOrderInfoMapper.updateById(refundOrder);

    }
}
