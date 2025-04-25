package org.nott.dto;

import lombok.Data;
import org.nott.enums.BusinessEnum;

/**
 * @author Nott
 * @date 2025-4-25
 */

@Data
public class GatewayBusinessDTO {

    private BusinessEnum business;

    private Param param;
}
