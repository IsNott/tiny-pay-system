package org.nott.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author nott
 * @since 2025-04-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PayMerchantInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 商户号
     */
    private String merchantNo;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 外部商户号
     */
    private String outMerchantId;

    /**
     * 外部商户信息
     */
    private String outMerchantInfo;

    /**
     * 外部商户证书存放地址
     */
    private String outMerchantCertificatePath;

    /**
     * 是否需要证书
     */
    private Integer needCertificate;

    /**
     * 内部私钥
     */
    private String inPrivateKey;

    /**
     * 内部公钥
     */
    private String inPublicKey;

    /**
     * 外部应用id
     */
    private Long outAppId;

    /**
     * 外部私钥
     */
    private String outPrivateKey;

    /**
     * 外部公钥
     */
    private String outPublicKey;

    /**
     * 回调通知地址（后台）
     */
    private String notifyUrl;

    /**
     * h5页面（支付结束返回）
     */
    private String h5ReturnUrl;

    /**
     * 应用与支付平台签名类型（预留）
     */
    private String inSignType;

    /**
     * 签名类型
     */
    private String outSignType;

    /**
     * 预留字段
     */
    private String extra;


}
