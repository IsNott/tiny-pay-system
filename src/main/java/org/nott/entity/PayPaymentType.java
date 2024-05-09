package org.nott.entity;


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
public class PayPaymentType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 支付方式名称
     */
    private String paymentName;

    /**
     * 支付方式代码
     */
    private String paymentCode;

    /**
     * 支付类型
     */
    private String paymentType;

    /**
     * 第三方支付地址
     */
    private String paymentUrl;

    /**
     * 预留
     */
    private String extra;


}
