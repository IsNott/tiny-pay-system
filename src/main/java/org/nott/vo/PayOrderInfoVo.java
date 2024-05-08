package org.nott.vo;

import lombok.Data;

/**
 * @author Nott
 * @date 2024-5-7
 */

@Data
public class PayOrderInfoVo {

    /**
     * 订单号
     */
    private Long orderNo;

//    /**
//     * 订单类型 1-支付 2-退款
//     */
//    private Integer orderType;
//
//    /**
//     * 订单状态 0-init 1-paying 2-pay success 3-failed 4-refund
//     */
//    private Integer payStatus;
}
