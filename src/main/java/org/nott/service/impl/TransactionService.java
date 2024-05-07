package org.nott.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayTransactionInfo;
import org.nott.enums.StatusEnum;
import org.nott.id.TransactionNoFactory;
import org.nott.mapper.PayOrderInfoMapper;
import org.nott.mapper.PayTransactionInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

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
        PayTransactionInfo po = new PayTransactionInfo();
        po.setInOrderId(payOrderInfo.getId());
        po.setTransactionType(payOrderInfo.getOrderType());
        po.setTransactionNo(TransactionNoFactory.next());
        po.setTransactionStatus(StatusEnum.INIT.getCode());

        payTransactionInfoMapper.insert(po);

        payOrderInfo.setInTransactionNo(po.getTransactionNo());
        payOrderInfoMapper.updateById(payOrderInfo);
        return po;
    }
}
