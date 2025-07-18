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
     * 商品名称
     */
    private String subjectName;

    /**
     * 金额，元为单位
     */
    private String amount;

    /**
     * 退款订单号
     */
    private Long refundOrderNo;

    /**
     * 订单内容
     */
    private String orderParam;

    /**
     * 支付方式code
     */
    private String paymentCode;

    /**
     * 支付业务方式
     */
    private String paymentType;

    /**
     * 订单号
     */
    private Long orderNo;

    /**
     * 额外信息
     */
    private String extra;

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
    private String inTransactionNo;


}
