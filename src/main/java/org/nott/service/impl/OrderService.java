package org.nott.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.nott.dto.PayOrderDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayTransactionInfo;
import org.nott.enums.StatusEnum;
import org.nott.enums.OrderTypeEnum;
import org.nott.exception.PayException;
import org.nott.mapper.PayOrderInfoMapper;
import org.nott.vo.PayOrderInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PayOrderInfo 相关操作服务类
 * @author Nott
 * @date 2024-5-7
 */

@Service
public class OrderService extends ServiceImpl<PayOrderInfoMapper,PayOrderInfo> {

    @Resource
    private PayOrderInfoMapper payOrderInfoMapper;

    @Resource
    private TransactionService transactionService;

    public PayOrderInfoVo initializeOrder(PayOrderDTO payOrderDTO){
        String orderParam = JSON.toJSONString(payOrderDTO);

        PayOrderInfo payOrderInfo = new PayOrderInfo();
        BeanUtils.copyProperties(payOrderDTO,payOrderInfo);
        payOrderInfo.setOrderNo(IdWorker.getId());
        payOrderInfo.setOrderType(OrderTypeEnum.PAY.getType());
        payOrderInfo.setPayStatus(StatusEnum.INIT.getCode());
        payOrderInfo.setOrderParam(orderParam);
        payOrderInfoMapper.insert(payOrderInfo);
        PayTransactionInfo payTransactionInfo = transactionService.createTransactionByOrder(payOrderInfo);

        PayOrderInfoVo vo = new PayOrderInfoVo();
        BeanUtils.copyProperties(payOrderInfo,vo);
        return vo;
    }

    public PayOrderInfo getByOrderNo(String orderNo){
        LambdaQueryWrapper<PayOrderInfo> wrapper = new LambdaQueryWrapper<PayOrderInfo>().eq(PayOrderInfo::getOrderNo, orderNo);
        List<PayOrderInfo> payOrderInfos = payOrderInfoMapper.selectList(wrapper);
        if(payOrderInfos.isEmpty()){
            throw new PayException(String.format("根据订单号%s没有找到记录", orderNo));
        }
        return payOrderInfos.get(0);
    }
}
