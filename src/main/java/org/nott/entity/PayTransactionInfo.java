package org.nott.entity;


import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author nott
 * @since 2024-05-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PayTransactionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 外部回调信息
     */
    private String outNotifyMsg;

    /**
     * 外部交易号
     */
    private String outTransactionNo;

    /**
     * 外部交易参数
     */
    private String outTransactionParam;

    /**
     * 内部交易号
     */
    private Long transactionNo;

    /**
     * 交易状态 订单状态 0-init 1-paying 2-pay success 3-failed 4-refund
     */
    private Integer transactionStatus;

    /**
     * 交易类型 1-支付 2-退款
     */
    private Integer transactionType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
