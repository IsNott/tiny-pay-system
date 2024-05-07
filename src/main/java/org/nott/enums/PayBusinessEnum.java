package org.nott.enums;

/**
 * 支付业务枚举
 * @author Nott
 * @date 2024-5-7
 */

public enum PayBusinessEnum {

    H5("h5",""),
    APP("h5",""),
    MINI_PROGRAM("h5",""),
    QR_CODE("h5","");

    private String businessType;

    private String desc;

    PayBusinessEnum(String businessType, String desc) {
        this.businessType = businessType;
        this.desc = desc;
    }

    public String getBusinessType() {
        return businessType;
    }

    public String getDesc() {
        return desc;
    }
}
