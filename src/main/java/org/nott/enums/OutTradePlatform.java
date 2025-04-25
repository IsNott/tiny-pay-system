package org.nott.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OutTradePlatform {

    ALIPAY("支付宝"),
    WECHAT("微信"),
    UNIONPAY("银联");

    private String name;
}
