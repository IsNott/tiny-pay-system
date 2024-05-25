package org.nott.service;

import org.nott.entity.PayOrderInfo;
import org.nott.result.PayResult;

public interface JsApiPayService {

    PayResult doJsApiPay(PayOrderInfo payOrderInfo);
}
