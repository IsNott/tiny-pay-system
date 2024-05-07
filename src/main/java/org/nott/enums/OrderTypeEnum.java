package org.nott.enums;

/**
 * 订单类型枚举
 * @author Nott
 * @date 2024-5-7
 */

public enum OrderTypeEnum {

    PAY(1,"pay order"),
    REFUND(2,"refund order");

    private Integer type;

    private String desc;

    OrderTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }
}
