package org.nott.service;

import org.nott.entity.PayOrderInfo;
import org.nott.result.PayResult;

/**
 * H5支付相关接口
 */
public interface H5PayService extends TransactionService {

    PayResult doH5Pay(PayOrderInfo payOrderInfo);
}
