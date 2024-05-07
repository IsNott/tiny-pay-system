package org.nott.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Nott
 * @date 2024-5-7
 */
@Data
@Component
@ConfigurationProperties("weixinpay")
public class WxPayConfig {

    private String mchId;

    private String appId;

    private String apiV3Key;

    private String notifyUrl;

    private String privateKeyPath;

    private String merchantSerialNumber;
}
