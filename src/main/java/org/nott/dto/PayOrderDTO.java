package org.nott.dto;

import lombok.Data;
import lombok.Setter;

/**
 * @author Nott
 * @date 2024-5-7
 */
@Data
public class PayOrderDTO {

    private String subjectName;

    private String amount;

    private String extra;
}
