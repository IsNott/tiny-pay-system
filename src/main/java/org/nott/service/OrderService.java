package org.nott.service;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import org.nott.entity.PayOrderInfo;
import org.nott.enums.StatusEnum;
import org.nott.enums.TypeEnum;
import org.nott.mapper.PayOrderInfoMapper;
import org.nott.vo.PayOrderInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author Nott
 * @date 2024-5-7
 */

@Service
public class OrderService {

    @Resource
    private PayOrderInfoMapper payOrderInfoMapper;

    public PayOrderInfoVo initializeOrder(){
        PayOrderInfo payOrderInfo = new PayOrderInfo();
        payOrderInfo.setOrderNo(IdWorker.getId());
        payOrderInfo.setOrderType(TypeEnum.PAY.getType());
        payOrderInfo.setPayStatus(StatusEnum.INIT.getCode());
        payOrderInfoMapper.insert(payOrderInfo);
        PayOrderInfoVo vo = new PayOrderInfoVo();
        BeanUtils.copyProperties(payOrderInfo,vo);
        return vo;
    }
}
