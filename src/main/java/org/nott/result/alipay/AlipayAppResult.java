package org.nott.result.alipay;

import lombok.Data;
import org.nott.result.PayResult;

@Data
public class AlipayAppResult extends PayResult {

    private String signStr;
}
