package org.nott.controller;

import jakarta.annotation.Resource;
import org.nott.common.R;
import org.nott.common.ThreadPoolContext;
import org.nott.dto.PayDTO;
import org.nott.dto.PayOrderDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayPaymentType;
import org.nott.exception.PayException;
import org.nott.payment.alipay.AlipayService;
import org.nott.result.h5.H5PayResult;
import org.nott.service.impl.OrderService;
import org.nott.service.impl.PaymentService;
import org.nott.vo.PayOrderInfoVo;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("pay")
public class PayController {

    @Resource
    private OrderService orderService;

    @Resource
    private ThreadPoolContext threadPoolContext;

    @Resource
    private PaymentService paymentService;

    @Resource
    private AlipayService alipayService;


    @RequestMapping(path = "createOrder", method = RequestMethod.POST)
    public R<?> createPayOrder(@RequestBody PayOrderDTO payOrderDTO) {
        PayOrderInfoVo orderInfoVo = orderService.initializeOrder(payOrderDTO);
        return R.okData(orderInfoVo);
    }

    @RequestMapping(path = "gateway", method = RequestMethod.POST)
    public R<?> gateway(@RequestBody PayDTO payDTO) {
        List<PayPaymentType> payments = paymentService.findPaymentByCode(payDTO.getPaymentCode());
        // TODO ..转换具体支付实现

        PayOrderInfo orderInfo = orderService.getByOrderNo(payDTO.getOrderNo());
        H5PayResult h5PayResult = alipayService.doH5Pay(orderInfo);

        return R.okData(h5PayResult);
    }

    @RequestMapping(path = "refund", method = RequestMethod.POST)
    public R<?> refund() {

        return R.okData(null);
    }

    @RequestMapping(path = "notify")
    public R<?> notifyByOutSystem() {
        // 处理回调信息

        // 异步处理（更新内部交易记录）
        ThreadPoolTaskExecutor executor = threadPoolContext.threadPoolTaskExecutor();
        executor.execute(() -> {
            //...
        });
        return R.ok();
    }
}
