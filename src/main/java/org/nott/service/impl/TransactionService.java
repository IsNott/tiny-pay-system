package org.nott.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.nott.dto.TradeNotifyDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayTransactionInfo;
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

    public List<PayTransactionInfo> getTransactionByOrder(Long orderId){
        LambdaQueryWrapper<PayTransactionInfo> wrapper = new LambdaQueryWrapper<PayTransactionInfo>()
                .eq(PayTransactionInfo::getInOrderId, orderId);
        List<PayTransactionInfo> transactionInfos = payTransactionInfoMapper.selectList(wrapper);
        return transactionInfos;
    }

    public PayTransactionInfo createTransactionByOrder(PayOrderInfo payOrderInfo) {
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

    public int updateTradeStateByACS(Long id, TradeNotifyDTO tradeNotifyDTO) {
        LambdaUpdateWrapper<PayTransactionInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(PayTransactionInfo::getNotifyTime,new Date());
        wrapper.set(PayTransactionInfo::getOutTransactionNo,tradeNotifyDTO.getTrade_no());
        wrapper.eq(PayTransactionInfo::getId,id);
        wrapper.isNull(PayTransactionInfo::getOutTransactionNo);
        int update = payTransactionInfoMapper.update(wrapper);
        if(update == 0){
            throw new PayException(String.format("交易%s已被其他线程更新，放弃当前通知", tradeNotifyDTO.getOut_trade_no()));
        }
        return update;
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
        }
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
