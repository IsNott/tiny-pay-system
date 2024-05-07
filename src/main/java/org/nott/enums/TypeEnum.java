package org.nott.enums;

/**
 * @author Nott
 * @date 2024-5-7
 */

public enum TypeEnum {

    PAY(1,"pay order"),
    REFUND(2,"refund order");

    private Integer type;

    private String desc;

    TypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }
}
