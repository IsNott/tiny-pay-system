package org.nott.payment.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import jakarta.annotation.Resource;
import org.nott.annotations.Payment;
import org.nott.annotations.PaymentType;
import org.nott.config.AlipayConfig;
import org.nott.dto.RefundOrderDTO;
import org.nott.dto.TradeNotifyDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayPaymentType;
import org.nott.entity.PayTransactionInfo;
import org.nott.enums.OrderTypeEnum;
import org.nott.enums.RefundStatusEnum;
import org.nott.enums.StatusEnum;
import org.nott.exception.PayException;
import org.nott.result.RefundResult;
import org.nott.result.alipay.AlipayH5Result;
import org.nott.result.PayResult;
import org.nott.result.alipay.AlipayRefundResult;
import org.nott.service.AbstractPaymentService;
import org.nott.service.H5PayService;
import org.nott.service.impl.OrderService;
import org.nott.service.impl.PaymentService;
import org.nott.service.impl.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Nott
 * @date 2024-5-7
 */

@Payment(code = "alipay")
@Service
public class AlipayService extends AbstractPaymentService implements H5PayService {

    private static final Logger logger = LoggerFactory.getLogger(AlipayService.class);

    private AlipayClient alipayClient;

    @Resource
    private TransactionService transactionService;

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private PaymentService paymentService;

    @Resource
    private OrderService orderService;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public AlipayClient getAlipayClient(String paymentUrl) {
        if(this.alipayClient == null){
            this.alipayClient = new DefaultAlipayClient(
                    paymentUrl,
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    "UTF-8",
                    alipayConfig.getPublicKey(),
                    alipayConfig.getSignType());
        }
        return this.alipayClient;
    }

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
            orderService.updatePayStatusAndOutTradeInfo(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAY_FAIL.getCode());
            throw new PayException(e.getMessage());
        }
        payOrderInfo.setPaymentCode(payPaymentType.getPaymentCode());

        AlipayH5Result result = new AlipayH5Result();
        result.setPageData(pageRedirectionData);
        result.setOrderNo(orderNo);

        orderService.updatePayStatusAndOutTradeInfo(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAYING.getCode());
        return result;

    }


    private AlipayTradeWapPayResponse createAlipayTradeWapPay(PayPaymentType payPaymentType, PayOrderInfo payOrderInfo, PayTransactionInfo payTransactionInfo) throws AlipayApiException {
        AlipayTradeWapPayResponse response;
        AlipayClient alipayClient = getAlipayClient(payPaymentType.getPaymentUrl());

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
        response = alipayClient.pageExecute(request, "GET");

        if (!response.isSuccess()) {
            throw new PayException("Alipay H5 pay request executed failed, " + response.toString());
        }

        payTransactionInfo.setOutTransactionParam(JSON.toJSONString(request));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleNotifyMsg(Map<String, String> notifyParam) {
        // 回调处理
        TradeNotifyDTO tradeNotifyDTO = JSON.parseObject(JSON.toJSONString(notifyParam), TradeNotifyDTO.class);
        boolean isTradeSuccess = "TRADE_SUCCESS".equals(tradeNotifyDTO.getTrade_status());
        if (!isTradeSuccess) {
            return;
        }
        PayTransactionInfo payTransactionInfo = transactionService.checkIfExist(tradeNotifyDTO.getOut_trade_no());
        final Long payTransactionInfoId = payTransactionInfo.getId();

        transactionService.updateTradeStateByACS(payTransactionInfoId, tradeNotifyDTO);
        // 异步处理业务逻辑
        threadPoolTaskExecutor.execute(() -> {
            // 更新信息
            transactionService.updateTradeBusinessInfo(payTransactionInfoId, tradeNotifyDTO);
            // 交互第三方...
        });
    }

    @Override
    public RefundResult doRefund(RefundOrderDTO refundOrderDTO) {
        String payOrderNo = refundOrderDTO.getPayOrderNo();
        // 找回原来支付的信息
        PayOrderInfo payOrderInfo = orderService.getByOrderNo(payOrderNo, OrderTypeEnum.PAY.getCode(), StatusEnum.PAY_SUCCESS.getCode());
        Long payOrderInfoId = payOrderInfo.getId();
        List<PayTransactionInfo> transactionInfos = transactionService.getTransactionByOrder(payOrderInfoId);
        if (transactionInfos.isEmpty()) {
            throw new PayException(String.format("订单：[%s],没有找到对应的交易记录，请检查", payOrderNo));
        }
        String inTransactionNo = payOrderInfo.getInTransactionNo();
        // 找回退款订单记录
        String refundOrderNo = refundOrderDTO.getRefundOrderNo();
        PayOrderInfo refundOrder = orderService.getByOrderNo(refundOrderNo, OrderTypeEnum.REFUND.getCode(), StatusEnum.INIT.getCode());
        // 原订单的支付方式
        PayPaymentType payPaymentType = paymentService.findPaymentByCode(payOrderInfo.getPaymentCode()).get(0);
        // 组装退款信息和执行请求
        AlipayTradeFastpayRefundQueryResponse response = this.assemableRefundRequestAndExecute(inTransactionNo, payPaymentType,refundOrder);
        // 更新退款记录
        refundOrder.setPayStatus(RefundStatusEnum.REFUNDING.getCode());
        // 返回结果
        AlipayRefundResult result = new AlipayRefundResult();
        result.setRefundOrderNo(payOrderInfo.getRefundOrderNo());
        result.setOrgOrderNo(payOrderInfo.getOrderNo());
        result.setRequestSuccess(true);

        // 是否同步执行退款成功，都走异步处理结果接口
        threadPoolTaskExecutor.execute(()->{
            JSONObject respJsonObj = JSONObject.parseObject(JSON.toJSONString(response));
            this.handleNotifyMsg(respJsonObj.toJavaObject(Map.class));
        });
        return result;
    }

    private AlipayTradeFastpayRefundQueryResponse assemableRefundRequestAndExecute(String inTransactionNo, PayPaymentType payPaymentType,PayOrderInfo refundOrderInfo) {
        // 构建退款请求
        AlipayClient alipayClient = getAlipayClient(payPaymentType.getPaymentUrl());
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(inTransactionNo);
        // 设置查询选项
        List<String> queryOptions = new ArrayList<String>();
        queryOptions.add("gmt_refund_pay");
        model.setQueryOptions(queryOptions);
        request.setBizModel(model);
        AlipayTradeFastpayRefundQueryResponse response = null;

        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            logger.error("Execute alipay refund request error:{}", response.toString());
            throw new PayException("Execute alipay refund request error.");
        }

        if(!response.isSuccess()){
            throw new PayException("Execute alipay refund request failed.");
        }

        refundOrderInfo.setOrderParam(JSON.toJSONString(request));
        return response;
    }
}

