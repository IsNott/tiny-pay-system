package org.nott.dto;

import lombok.Data;

/**
 * @author Nott
 * @date 2024-5-8
 */

@Data
public class RefundOrderDTO implements Param {

    private String payOrderNo;

    private String refundOrderNo;

}
