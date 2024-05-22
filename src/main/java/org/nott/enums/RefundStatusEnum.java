package org.nott.enums;

/**
 * @author Nott
 * @date 2024-5-22
 */
public enum RefundStatusEnum {

    INIT(0,"交易创建"),
    REFUNDING(1,"请求中"),
    REFUNDED(2,"退款成功"),
    REFUND_FAIL(3,"退款失败");


    private Integer code;

    private String desc;

    RefundStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
