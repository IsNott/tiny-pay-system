package org.nott.entity;


import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class PayOrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 订单内容
     */
    private String orderParam;

    /**
     * 支付方式code
     */
    private String paymentCode;

    /**
     * 订单号
     */
    private Long orderNo;

    /**
     * 订单类型 1-支付 2-退款
     */
    private Integer orderType;

    /**
     * 订单状态 0-init 1-paying 2-pay success 3-failed 4-refund
     */
    private Integer payStatus;

    /**
     * 内部交易号
     */
    private Long inTransactionNo;


}
