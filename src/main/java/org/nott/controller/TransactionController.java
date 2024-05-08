package org.nott.controller;

import jakarta.annotation.Resource;
import org.nott.common.R;
import org.nott.dto.PayDTO;
import org.nott.dto.PayOrderDTO;
import org.nott.dto.RefundOrderDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayPaymentType;
import org.nott.enums.OrderTypeEnum;
import org.nott.enums.StatusEnum;
import org.nott.payment.alipay.AlipayService;
import org.nott.result.PayResult;
import org.nott.service.impl.OrderService;
import org.nott.service.impl.PaymentService;
import org.nott.vo.PayOrderInfoVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("transaction")
public class TransactionController {

    @Resource
    private OrderService orderService;

    @Resource
    private PaymentService paymentService;

    @Resource
    private AlipayService alipayService;


    @RequestMapping(path = "createPay", method = RequestMethod.POST)
    public R<?> createPayOrder(@RequestBody PayOrderDTO payOrderDTO) {
        PayOrderInfoVo orderInfoVo = orderService.initializeOrder(payOrderDTO);
        return R.okData(orderInfoVo);
    }

    @RequestMapping(path = "gateway", method = RequestMethod.POST)
    public R<?> gateway(@RequestBody PayDTO payDTO) {
        List<PayPaymentType> payments = paymentService.findPaymentByCode(payDTO.getPaymentCode());
        // TODO ..转换具体支付实现

        PayOrderInfo orderInfo = orderService.getByOrderNo(payDTO.getOrderNo(), OrderTypeEnum.PAY.getCode(), StatusEnum.INIT.getCode());
        PayResult h5PayResult = alipayService.doH5Pay(orderInfo);

        return R.okData(h5PayResult);
    }

    @RequestMapping(path = "refund", method = RequestMethod.POST)
    public R<?> refund(@RequestBody RefundOrderDTO refundOrderDTO) {
        PayOrderInfo refundOrder = orderService.initializeRefundOrder(refundOrderDTO.getPayOrderNo());
        refundOrderDTO.setRefundOrderNo(String.valueOf(refundOrder.getOrderNo()));
        // TODO ..转换具体退款实现
        alipayService.doRefund(refundOrderDTO);
        return R.okData(null);
    }

}
