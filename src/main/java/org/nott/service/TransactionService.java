package org.nott.service;

import org.nott.dto.RefundOrderDTO;
import org.nott.result.RefundResult;

/**
 * 标识交易基本接口
 */
public interface TransactionService {

    RefundResult doRefund(RefundOrderDTO refundOrderDTO);

}
