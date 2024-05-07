package org.nott.result.h5;

import lombok.Data;

/**
 * 支付宝H5支付结果类
 * @author Nott
 * @date 2024-5-7
 */
@Data
public class AlipayH5Result extends H5PayResult{

    private String pageData;
}
