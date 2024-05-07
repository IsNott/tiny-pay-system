package org.nott.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Nott
 * @date 2024-5-7
 */

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    private String appId;

    private String privateKey;

    private String publicKey;

    private String signType;

    private String notifyUrl;

    private String h5ReturnUrl;
}
