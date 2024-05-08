package org.nott.result.alipay;

import lombok.Data;
import org.nott.result.H5PayResult;

/**
 * 支付宝H5支付结果类
 * @author Nott
 * @date 2024-5-7
 */
@Data
public class AlipayH5Result extends H5PayResult {

    private String pageData;
}
