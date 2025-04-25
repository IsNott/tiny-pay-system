package org.nott.dto;

import lombok.Data;
import org.nott.enums.BusinessEnum;
import org.nott.enums.OutTradePlatform;

/**
 * @author Nott
 * @date 2025-4-25
 */

@Data
public class TradeNotifyDTO {

    private Long payTransactionInfoId;

    private String inTradeNo;

    private String outTradeNo;

    private String tradeStatus;

    private OutTradePlatform outTradePlatform;

    private BusinessEnum businessEnum;
}
