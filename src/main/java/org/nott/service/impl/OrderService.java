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
import org.nott.enums.StatusEnum;
import org.nott.enums.OrderTypeEnum;
import org.nott.exception.PayException;
import org.nott.mapper.PayOrderInfoMapper;
import org.nott.mapper.PayTransactionInfoMapper;
import org.nott.vo.PayOrderInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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

    //TODO 生成退款单
    public PayOrderInfo initializeRefundOrder(String orderNo) {
        PayOrderInfo payOrder = this.getByOrderNo(orderNo, OrderTypeEnum.PAY.getCode(), StatusEnum.PAY_SUCCESS.getCode());
        Long refundOrderNo = payOrder.getRefundOrderNo();
        if (refundOrderNo != null) {
            PayOrderInfo refundOrder = this.getByOrderNo(orderNo, OrderTypeEnum.REFUND.getCode(), StatusEnum.INIT.getCode());
            if (refundOrder == null) {
                throw new PayException(String.format("退款单：%s，已有退款中记录，请稍后重试", refundOrderNo));
            }
            return refundOrder;
        }
        PayOrderInfo refundOrder = new PayOrderInfo();
        BeanUtils.copyProperties(payOrder, refundOrder, "id", "updateTime", "createTime", "orderType", "payStatus", "inTransactionNo");
        refundOrder.setOrderType(OrderTypeEnum.REFUND.getCode());
        refundOrder.setPayStatus(StatusEnum.INIT.getCode());
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

}
