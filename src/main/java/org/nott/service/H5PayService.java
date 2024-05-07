package org.nott.service;

import org.nott.entity.PayOrderInfo;
import org.nott.result.h5.H5PayResult;

/**
 * H5支付相关接口
 */
public interface H5PayService extends PayService{

    H5PayResult doH5Pay(PayOrderInfo payOrderInfo);
}
