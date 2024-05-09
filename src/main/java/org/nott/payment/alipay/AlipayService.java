package org.nott.payment.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import jakarta.annotation.Resource;
import org.nott.annotations.Payment;
import org.nott.annotations.PaymentType;
import org.nott.common.ThreadPoolContext;
import org.nott.config.AlipayConfig;
import org.nott.dto.RefundOrderDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayPaymentType;
import org.nott.entity.PayTransactionInfo;
import org.nott.enums.StatusEnum;
import org.nott.exception.PayException;
import org.nott.result.RefundResult;
import org.nott.result.alipay.AlipayH5Result;
import org.nott.result.PayResult;
import org.nott.service.H5PayService;
import org.nott.service.impl.OrderService;
import org.nott.service.impl.PaymentService;
import org.nott.service.impl.TransactionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Nott
 * @date 2024-5-7
 */

@Payment(code = "alipay")
@Service
public class AlipayService implements H5PayService {

    @Resource
    private TransactionService transactionService;

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private PaymentService paymentService;

    @Resource
    private ThreadPoolContext threadPoolContext;

    @Resource
    private OrderService orderService;

    @PaymentType("h5")
    @Override
    public PayResult doH5Pay(PayOrderInfo payOrderInfo) {
        Long orderNo = payOrderInfo.getOrderNo();
        PayTransactionInfo payTransactionInfo = null;

        List<PayTransactionInfo> transactionInfos = transactionService.getTransactionByOrder(payOrderInfo.getId());
        if (transactionInfos.isEmpty()) {
            throw new PayException(String.format("订单：[%s],没有找到对应的交易记录，请检查", orderNo));
        }

        // 订单与外部交易记录关系为一对一，如果有支付中的状态，可能是别的线程已经操作过或者已经失败
        payTransactionInfo = transactionInfos.get(0);
        Integer transactionStatus = payTransactionInfo.getTransactionStatus();
        if (!StatusEnum.INIT.getCode().equals(transactionStatus)) {
            throw new PayException(String.format("订单：[%s],已有支付中/失败状态，请检查后重试", orderNo));
        }

        // 支付方式
        List<PayPaymentType> payments = paymentService.findPaymentByCode(payOrderInfo.getPaymentCode());
        if (payments == null || payments.isEmpty()) {
            throw new PayException(String.format("支付能力代码：[%s] 没有可用的支付方式", payOrderInfo.getPaymentCode()));
        }
        PayPaymentType payPaymentType = payments.get(0);


        // 创建支付订单
        AlipayTradeWapPayResponse response = null;
        String pageRedirectionData = null;
        try {
            response = createAlipayTradeWapPay(payPaymentType, payOrderInfo, payTransactionInfo);
            pageRedirectionData = response.getBody();
        } catch (AlipayApiException e) {
            orderService.updatePayStatus(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAY_FAIL.getCode());
            throw new PayException(e.getMessage());
        }

        AlipayH5Result result = new AlipayH5Result();
        result.setPageData(pageRedirectionData);
        result.setOrderNo(orderNo);

        orderService.updatePayStatus(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAY_SUCCESS.getCode());
        return result;

    }


    private AlipayTradeWapPayResponse createAlipayTradeWapPay(PayPaymentType payPaymentType, PayOrderInfo payOrderInfo, PayTransactionInfo payTransactionInfo) throws AlipayApiException {
        AlipayTradeWapPayResponse response;
        AlipayClient alipayClient = new DefaultAlipayClient(
                payPaymentType.getPaymentUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getPublicKey(),
                alipayConfig.getSignType());

        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        //异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        //同步跳转地址，仅支持http/https
        request.setReturnUrl(alipayConfig.getH5ReturnUrl());
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", payTransactionInfo.getTransactionNo());
        //支付金额，最小值0.01元
        bizContent.put("total_amount", payOrderInfo.getAmount());
        //订单标题，不可使用特殊符号
        bizContent.put("subject", payOrderInfo.getSubjectName());

        //手机网站支付默认传值QUICK_WAP_WAY
        bizContent.put("product_code", "QUICK_WAP_WAY");

        request.setBizContent(bizContent.toString());
        response = alipayClient.pageExecute(request, "POST");

        if (!response.isSuccess()) {
            throw new PayException("Alipay H5 not success, " + response.toString());
        }
        return response;
    }

    public void handleNotifyMsg(Map<String, String> notifyParam) {
        //TODO 回调处理

    }

    @Override
    public RefundResult doRefund(RefundOrderDTO refundOrderDTO) {
        return null;
    }
}

