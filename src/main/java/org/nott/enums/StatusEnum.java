package org.nott.enums;

/**
 * 订单状态枚举
 * @author Nott
 * @date 2024-5-7
 */

public enum StatusEnum {

    INIT(0,"交易创建"),
    PAYING(1,"支付中"),
    PAY_SUCCESS(2,"支付成功"),
    PAY_FAIL(3,"支付失败"),
    REFUNDING(4,"交易退款中"),
    REFUND(5,"交易退款"),
    PAY_CLOSED(6,"订单关闭"),
    ;

    private Integer code;

    private String desc;

    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
