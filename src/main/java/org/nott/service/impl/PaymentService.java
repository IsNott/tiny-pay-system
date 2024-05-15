package org.nott.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.nott.dto.PayOrderDTO;
import org.nott.entity.PayPaymentType;
import org.nott.exception.PayException;
import org.nott.mapper.PayPaymentTypeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PaymentType 服务类
 * @author Nott
 * @date 2024-5-7
 */
@Service
public class PaymentService {

    @Resource
    private PayPaymentTypeMapper payPaymentTypeMapper;

    public List<PayPaymentType> findPaymentByCode(String code) {
        LambdaQueryWrapper<PayPaymentType> wrapper = new LambdaQueryWrapper<PayPaymentType>()
                .eq(StringUtils.isNotEmpty(code), PayPaymentType::getPaymentCode, code);
        List<PayPaymentType> payments = payPaymentTypeMapper.selectList(wrapper);

        if(payments == null || payments.isEmpty()){
            throw new PayException(String.format("代码%s没有可用的支付方式",code));
        }
        return payments;
    }

    public PayPaymentType findPaymentByOrderDTO(PayOrderDTO payOrderDTO) {
        String code = payOrderDTO.getPaymentCode();
        String type = payOrderDTO.getPaymentType();
        LambdaQueryWrapper<PayPaymentType> wrapper = new LambdaQueryWrapper<PayPaymentType>()
                .eq(PayPaymentType::getPaymentCode, code)
                .eq(PayPaymentType::getPaymentType, type);
        List<PayPaymentType> payments = payPaymentTypeMapper.selectList(wrapper);

        if(payments == null || payments.isEmpty()){
            throw new PayException(String.format("代码%s没有可用的支付方式",code));
        }
        return payments.get(0);
    }
}
