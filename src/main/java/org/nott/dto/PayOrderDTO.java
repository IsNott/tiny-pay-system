package org.nott.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Nott
 * @date 2024-5-7
 */
@Getter
@Setter
public class PayOrderDTO {

    private String paymentCode;

    private String paymentType;

    private String subjectName;

    private String amount;

    private String extra;

}
