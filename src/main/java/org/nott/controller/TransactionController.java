package org.nott.controller;

import jakarta.annotation.Resource;
import org.nott.common.R;
import org.nott.dto.PayOrderDTO;
import org.nott.dto.CreateOrderDTO;
import org.nott.dto.RefundOrderDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayPaymentType;
import org.nott.enums.OrderTypeEnum;
import org.nott.enums.StatusEnum;
import org.nott.payment.alipay.AlipayService;
import org.nott.result.Result;
import org.nott.service.impl.OrderService;
import org.nott.service.impl.PaymentService;
import org.nott.support.PayServiceContext;
import org.nott.vo.PayOrderInfoVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("transaction")
public class TransactionController {

    @Resource
    private OrderService orderService;

    @Resource
    private PaymentService paymentService;

    @Resource
    private AlipayService alipayService;

    @Value("payment.package.name")
    private String paymentPackage;


    @RequestMapping(path = "createPay", method = RequestMethod.POST)
    public R<?> createPayOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        PayOrderInfoVo orderInfoVo = orderService.initializeOrder(createOrderDTO);
        return R.okData(orderInfoVo);
    }

    @RequestMapping(path = "gateway", method = RequestMethod.POST)
    public R<?> gateway(@RequestBody PayOrderDTO payOrderDTO) {
        PayPaymentType paymentType = paymentService.findPaymentByOrderDTO(payOrderDTO);
        // 转换具体支付实现 paymentName + type注解定位
        PayOrderInfo orderInfo = orderService.getByOrderNo(payOrderDTO.getOrderNo(), OrderTypeEnum.PAY.getCode(), StatusEnum.INIT.getCode());
        Result result = PayServiceContext.invokePaymentTypeMethod(paymentType.getPaymentCode(), paymentType.getPaymentType(), orderInfo);
        return R.okData(result);
    }

    @RequestMapping(path = "refund", method = RequestMethod.POST)
    public R<?> refund(@RequestBody RefundOrderDTO refundOrderDTO) {
        PayOrderInfo refundOrder = orderService.initializeRefundOrder(refundOrderDTO.getPayOrderNo());
        PayOrderInfo orgPayOrder = orderService.findPayOrderByRefundOrderNo(refundOrder.getOrderNo());
        refundOrderDTO.setRefundOrderNo(String.valueOf(refundOrder.getOrderNo()));
        // 转换具体退款实现
        Result result = PayServiceContext.invokeRefundMethod(orgPayOrder.getPaymentCode(), refundOrderDTO);
        return R.okData(result);
    }

}
