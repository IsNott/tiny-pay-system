package org.nott.service;

import org.nott.entity.PayOrderInfo;
import org.nott.result.H5PayResult;

/**
 * H5支付相关接口
 */
public interface H5PayService extends TransactionService {

    H5PayResult doH5Pay(PayOrderInfo payOrderInfo);
}
