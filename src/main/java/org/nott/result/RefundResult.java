package org.nott.result;

import lombok.Data;

/**
 * @author Nott
 * @date 2024-5-8
 */
@Data
public abstract class RefundResult implements Result {

    private boolean requestSuccess;

    private Long orgOrderNo;

    private Long refundOrderNo;
}
