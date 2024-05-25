package org.nott.service;

import org.nott.entity.PayOrderInfo;
import org.nott.result.PayResult;

public interface AppPayService {

    PayResult doAppPay(PayOrderInfo payOrderInfo);
}
