package org.nott.result.alipay;

import lombok.Data;
import org.nott.result.PayResult;

@Data
public class AlipayQrResult extends PayResult {

    private String qrUrl;
}
