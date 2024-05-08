package org.nott.controller;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.nott.common.CommonUtils;
import org.nott.config.AlipayConfig;
import org.nott.exception.PayException;
import org.nott.payment.alipay.AlipayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Nott
 * @date 2024-5-8
 */

@RestController
@RequestMapping("/notify/")
public class NotifyController {

    @Resource
    private AlipayService alipayService;

    @Resource
    private AlipayConfig alipayConfig;

    private final Logger logger = LoggerFactory.getLogger(NotifyController.class);

    @RequestMapping(path = "alipay")
    public void notifyByAliPay(HttpServletRequest request) {
        // 验证支付宝的回调信息
        Map<String,String> params = new HashMap<>();
        try {
            Map requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用。
                //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                params.put(name, valueStr);
            }

            boolean flag = AlipaySignature.rsaCheckV1(params, alipayConfig.getPublicKey(), "utf-8","RSA2");
            if(!flag){
                throw new PayException("Alipay notify sign verified failed.");
            }
        } catch (AlipayApiException e) {
            logger.error("Alipay notify error :{}", e.getMessage(), e);
            throw new PayException(e.getMessage());
        }
        logger.info("Received Alipay notify content:{}", JSON.toJSONString(params));
        alipayService.handleNotifyMsg(params);

        CommonUtils.writeHttpResp();
    }
}
