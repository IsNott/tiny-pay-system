package org.nott.vo;

import lombok.Data;

/**
 * @author Nott
 * @date 2024-5-23
 */

@Data
public class OrderQueryVo {

    private Long orderNo;

    private Integer orderType;

    private String paymentCode;

    private Integer payStatus;

    private Long refundOrderNo;
}
