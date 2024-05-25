package org.nott.service;

import org.nott.entity.PayOrderInfo;
import org.nott.result.PayResult;

public interface QrPayService extends TransactionService{

    PayResult doQrPay(PayOrderInfo payOrderInfo);
}
