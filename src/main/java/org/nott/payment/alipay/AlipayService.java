package org.nott.payment.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import org.nott.annotations.Payment;
import org.nott.annotations.PaymentType;
import org.nott.annotations.Refund;
import org.nott.config.AlipayConfig;
import org.nott.constant.AlipayBusinessConstant;
import org.nott.dto.RefundOrderDTO;
import org.nott.dto.AliTradeNotifyDTO;
import org.nott.entity.PayOrderInfo;
import org.nott.entity.PayPaymentType;
import org.nott.entity.PayTransactionInfo;
import org.nott.enums.BusinessEnum;
import org.nott.enums.OrderTypeEnum;
import org.nott.enums.OutTradePlatform;
import org.nott.enums.StatusEnum;
import org.nott.exception.PayException;
import org.nott.result.RefundResult;
import org.nott.result.alipay.*;
import org.nott.result.PayResult;
import org.nott.service.*;
import org.nott.service.impl.OrderService;
import org.nott.service.impl.PaymentService;
import org.nott.service.impl.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Nott
 * @date 2024-5-7
 */

@Payment(code = "alipay")
@Service
public class AlipayService extends AbstractPaymentService implements H5PayService, QrPayService, JsApiPayService, AppPayService {

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
        if (this.alipayClient == null) {
            this.alipayClient = new DefaultAlipayClient(
                    paymentUrl,
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    AlipayBusinessConstant.Common.JSON,
                    AlipayBusinessConstant.Common.UTF8,
                    alipayConfig.getPublicKey(),
                    alipayConfig.getSignType());
        }
        return this.alipayClient;
    }

    @PaymentType("h5")
    @Override
    public PayResult doH5Pay(PayOrderInfo payOrderInfo) {
        // 根据订单找到生成的外部交易记录
        PayTransactionInfo payTransactionInfo = transactionService.checkAndReturnPayTransaction(payOrderInfo);

        // 支付方式
        List<PayPaymentType> payments = paymentService.findPaymentByCode(payOrderInfo.getPaymentCode());
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
        result.setOrderNo(payOrderInfo.getOrderNo());

        orderService.updatePayStatusAndOutTradeInfo(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAYING.getCode());
        return result;

    }

    @PaymentType("qr")
    @Override
    public PayResult doQrPay(PayOrderInfo payOrderInfo) {
        // 根据订单找到生成的外部交易记录
        PayTransactionInfo payTransactionInfo = transactionService.checkAndReturnPayTransaction(payOrderInfo);

        // 支付方式
        List<PayPaymentType> payments = paymentService.findPaymentByCode(payOrderInfo.getPaymentCode());
        PayPaymentType payPaymentType = payments.get(0);

        AlipayTradePrecreateResponse response;
        String qrUrl;

        try {
            response = createAlipayQrRequest(payPaymentType,payOrderInfo,payTransactionInfo);
            qrUrl = response.getQrCode();
        } catch (Exception e) {
            // 失败时定义为失败记录
            orderService.updatePayStatusAndOutTradeInfo(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAY_FAIL.getCode());
            throw new PayException(e.getMessage());
        }

        AlipayQrResult alipayQrResult = new AlipayQrResult();
        alipayQrResult.setOrderNo(payOrderInfo.getOrderNo());
        alipayQrResult.setQrUrl(qrUrl);

        orderService.updatePayStatusAndOutTradeInfo(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAYING.getCode());

        return alipayQrResult;
    }

    @PaymentType("jsapi")
    @Override
    public PayResult doJsApiPay(PayOrderInfo payOrderInfo) {
        // 根据订单找到生成的外部交易记录
        PayTransactionInfo payTransactionInfo = transactionService.checkAndReturnPayTransaction(payOrderInfo);

        // 支付方式
        PayPaymentType payPaymentType = paymentService.findPaymentByCode(payOrderInfo.getPaymentCode()).get(0);

        try {
            createAlipayJsApiTradeRequest(payPaymentType,payOrderInfo,payTransactionInfo);
        } catch (Exception e) {
            // 失败时定义为失败记录
            orderService.updatePayStatusAndOutTradeInfo(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAY_FAIL.getCode());
            throw new PayException(e.getMessage());
        }

        AlipayJsApiResult result = new AlipayJsApiResult();
        result.setOrderNo(payOrderInfo.getOrderNo());

        orderService.updatePayStatusAndOutTradeInfo(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAYING.getCode());
        return result;
    }

    @PaymentType("app")
    @Override
    public PayResult doAppPay(PayOrderInfo payOrderInfo) {
        // 根据订单找到生成的外部交易记录
        PayTransactionInfo payTransactionInfo = transactionService.checkAndReturnPayTransaction(payOrderInfo);

        // 支付方式
        PayPaymentType payPaymentType = paymentService.findPaymentByCode(payOrderInfo.getPaymentCode()).get(0);

        AlipayTradeAppPayResponse response;

        try {
            response = createAlipayAppTradeRequest(payPaymentType,payOrderInfo,payTransactionInfo);
        } catch (Exception e) {
            // 失败时定义为失败记录
            orderService.updatePayStatusAndOutTradeInfo(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAY_FAIL.getCode());
            throw new PayException(e.getMessage());
        }

        AlipayAppResult result = new AlipayAppResult();
        result.setSignStr(response.getOrderStr());
        result.setOrderNo(payOrderInfo.getOrderNo());

        orderService.updatePayStatusAndOutTradeInfo(payOrderInfo, payTransactionInfo, StatusEnum.INIT.getCode(), StatusEnum.PAYING.getCode());
        return result;
    }

    private AlipayTradeAppPayResponse createAlipayAppTradeRequest(PayPaymentType payPaymentType, PayOrderInfo payOrderInfo, PayTransactionInfo payTransactionInfo) throws AlipayApiException {
        AlipayClient alipayClient = getAlipayClient(payPaymentType.getPaymentUrl());
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setNotifyUrl("");
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payTransactionInfo.getTransactionNo());
        bizContent.put("total_amount", payOrderInfo.getAmount());
        bizContent.put("subject", payOrderInfo.getSubjectName());
        bizContent.put("product_code", "QUICK_MSECURITY_PAY");
        request.setBizContent(bizContent.toString());
        AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
        if(!response.isSuccess()){
            throw new PayException("Alipay APP trade request executed failed");
        }
        return response;
    }

    private AlipayTradeCreateResponse createAlipayJsApiTradeRequest(PayPaymentType payPaymentType, PayOrderInfo payOrderInfo, PayTransactionInfo payTransactionInfo) throws AlipayApiException {
        AlipayClient alipayClient = getAlipayClient(payPaymentType.getPaymentUrl());
        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        AlipayTradeCreateModel model = new AlipayTradeCreateModel();
        model.setSubject(payOrderInfo.getSubjectName());
        model.setTotalAmount(payOrderInfo.getAmount());
        model.setOutTradeNo(payTransactionInfo.getTransactionNo());
        model.setProductCode("JSAPI_PAY");
        model.setOpAppId(alipayConfig.getAppId());
        model.setOpBuyerOpenId(payOrderInfo.getBuyerId());
        request.setBizModel(model);
        AlipayTradeCreateResponse response = alipayClient.execute(request);
        if(!response.isSuccess()){
            throw new PayException("Alipay JSAPI trade request executed failed");
        }
        return response;
    }

    private AlipayTradePrecreateResponse createAlipayQrRequest(PayPaymentType payPaymentType, PayOrderInfo payOrderInfo, PayTransactionInfo payTransactionInfo) throws AlipayApiException {
        AlipayTradePrecreateResponse response;
        AlipayClient alipayClient = getAlipayClient(payPaymentType.getPaymentUrl());
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(payTransactionInfo.getTransactionNo());
        model.setTotalAmount(payOrderInfo.getAmount());
        model.setSubject(payOrderInfo.getSubjectName());
        request.setBizModel(model);
        response = alipayClient.execute(request);
        if(!response.isSuccess()){
            throw new PayException("Alipay QR trade request executed failed");
        }
        //todo 多商品是否需要展示详情
        // 设置订单包含的商品列表信息
//        List<GoodsDetail> goodsDetail = new ArrayList<GoodsDetail>();
//        GoodsDetail goodsDetail0 = new GoodsDetail();
//        goodsDetail0.setGoodsName("ipad");
//        goodsDetail0.setQuantity(1L);
//        goodsDetail0.setPrice("2000");
//        goodsDetail0.setGoodsId("apple-01");
//        goodsDetail0.setGoodsCategory("34543238");
//        goodsDetail0.setCategoriesTree("124868003|126232002|126252004");
//        goodsDetail0.setShowUrl("http://www.alipay.com/xxx.jpg");
//        goodsDetail.add(goodsDetail0);
//        model.setGoodsDetail(goodsDetail);
        return response;
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
        bizContent.put(AlipayBusinessConstant.TradeFiled.OUT_TRADE_NO, payTransactionInfo.getTransactionNo());
        //支付金额，最小值0.01元
        bizContent.put(AlipayBusinessConstant.TradeFiled.TOTAL_AMOUNT, payOrderInfo.getAmount());
        //订单标题，不可使用特殊符号
        bizContent.put(AlipayBusinessConstant.TradeFiled.SUBJECT, payOrderInfo.getSubjectName());

        //手机网站支付默认传值QUICK_WAP_WAY
        bizContent.put(AlipayBusinessConstant.TradeFiled.PRODUCT_CODE, "QUICK_WAP_WAY");

        request.setBizContent(bizContent.toString());
        response = alipayClient.pageExecute(request, "GET");

        if (!response.isSuccess()) {
            throw new PayException("Alipay H5 pay request executed failed, " + response.toString());
        }

        payTransactionInfo.setOutTransactionParam(JSON.toJSONString(request));
        return response;
    }

    /**
     * 处理回调信息
     * 目前测试发现的QR支付退款没有回调通知
     * @param notifyParam 经过签名校验的支付宝回调参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleNotifyMsg(Map<String, String> notifyParam) {
        // 回调处理
        AliTradeNotifyDTO aliPayTradeNotifyDTO = JSON.parseObject(JSON.toJSONString(notifyParam), AliTradeNotifyDTO.class);
        String tradeStatus = aliPayTradeNotifyDTO.getTrade_status();
        String outTradeNo = aliPayTradeNotifyDTO.getOut_trade_no();
        String zfbTradeNo = aliPayTradeNotifyDTO.getTrade_no();
        boolean isTradeFinish = AlipayBusinessConstant.Trade.SUCCESS.equals(tradeStatus) ||
                AlipayBusinessConstant.Trade.CLOSED.equals(tradeStatus);
        if (!isTradeFinish) {
            return;
        }

        PayTransactionInfo payTransactionInfo = transactionService.checkIfExist(outTradeNo);

        switch (tradeStatus){
            default -> throw new PayException("UnKnown notify Msg");
            case AlipayBusinessConstant.Trade.SUCCESS -> {
                transactionService.updateTradeStateByACS(zfbTradeNo, outTradeNo);
                logger.info("成功接收到内部交易号为[{}]的支付宝支付业务通知",outTradeNo);
                logger.info("业务：[支付]");
                logger.info("正文：\n{}", JSON.toJSONString(notifyParam));
            }
            case AlipayBusinessConstant.Trade.CLOSED -> {
                transactionService.updateRefundStateByACS(zfbTradeNo, aliPayTradeNotifyDTO);
                logger.info("成功接收到内部交易号为[{}]的支付宝退款业务通知",outTradeNo);
                logger.info("业务：[退款]");
                logger.info("正文：\n{}", JSON.toJSONString(notifyParam));
            }
        }

        aliPayTradeNotifyDTO.setOutTradeNo(outTradeNo);
        aliPayTradeNotifyDTO.setInTradeNo(zfbTradeNo);
        aliPayTradeNotifyDTO.setOutTradePlatform(OutTradePlatform.ALIPAY);
        aliPayTradeNotifyDTO.setPayTransactionInfoId(payTransactionInfo.getId());
        if(tradeStatus.equals(AlipayBusinessConstant.Trade.SUCCESS)){
            aliPayTradeNotifyDTO.setBusinessEnum(BusinessEnum.PAY);
        }
        if(tradeStatus.equals(AlipayBusinessConstant.Trade.CLOSED)){
            aliPayTradeNotifyDTO.setBusinessEnum(BusinessEnum.PAY);
        }

        // 异步处理业务逻辑
        threadPoolTaskExecutor.execute(() -> {
            // 更新信息
            transactionService.updateTradeBusinessInfo(aliPayTradeNotifyDTO);
            // 交互第三方...
        });
    }

    @Refund
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
        AlipayTradeRefundResponse  response = this.assemableRefundRequestAndExecute(inTransactionNo, payPaymentType, refundOrder);
        logger.info("Alipay refund response:{}", JSON.toJSONString(response));

        // 更新记录
        orderService.updateRefundStatus(payOrderNo, refundOrderNo);
        // 返回结果
        AlipayRefundResult result = new AlipayRefundResult();
        result.setRefundOrderNo(payOrderInfo.getRefundOrderNo());
        result.setOrgOrderNo(payOrderInfo.getOrderNo());
        result.setRequestSuccess(true);

//        // 是否同步执行退款成功，都走异步处理结果接口
//        threadPoolTaskExecutor.execute(() -> {
//            JSONObject respJsonObj = JSONObject.parseObject(JSON.toJSONString(response));
//            this.handleNotifyMsg(respJsonObj.toJavaObject(Map.class));
//        });
        return result;
    }

    private AlipayTradeRefundResponse assemableRefundRequestAndExecute(String inTransactionNo, PayPaymentType payPaymentType, PayOrderInfo refundOrderInfo) {
        // 构建退款请求
        AlipayClient alipayClient = getAlipayClient(payPaymentType.getPaymentUrl());
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest ();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutRequestNo(IdWorker.get32UUID());
        model.setOutTradeNo(inTransactionNo);
        model.setRefundReason("正常退款");
        model.setRefundAmount(refundOrderInfo.getAmount());
        // 设置查询选项
        List<String> queryOptions = Arrays.asList("gmt_refund_pay","refund_detail_item_list");
        model.setQueryOptions(queryOptions);
        request.setBizModel(model);
        AlipayTradeRefundResponse  response = null;

        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            logger.error("Execute alipay refund request error:{}", response.toString());
            throw new PayException("Execute alipay refund request error.");
        }

        if (!response.isSuccess()) {
            throw new PayException("Execute alipay refund request failed.");
        }

        refundOrderInfo.setOrderParam(JSON.toJSONString(request));
        return response;
    }

}

