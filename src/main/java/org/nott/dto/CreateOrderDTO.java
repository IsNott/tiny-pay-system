package org.nott.dto;

import lombok.Data;

/**
 * @author Nott
 * @date 2024-5-7
 */
@Data
public class CreateOrderDTO {

    private String subjectName;

    private String amount;

    private String buyerId;

    private String extra;
}
