package org.nott.result;

import lombok.Data;

/**
 * @author Nott
 * @date 2024-5-8
 */
@Data
public class RefundResult {

    private boolean requestSuccess;

    private Integer orderStatus;
}
