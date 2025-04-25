package org.nott.dto;

import lombok.Data;

/**
 * @author Nott
 * @date 2024-5-22
 */

@Data
public class AliTradeNotifyDTO extends TradeNotifyDTO {

    private String gmt_create;

    private String charset;

    private String seller_email;

    private String subject;

    private String buyer_id;

    private String invoice_amount;

    private String notify_id;

    private String fund_bill_list;

    private String notify_type;

    private String trade_status;

    private String receipt_amount;

    private String buyer_pay_amount;

    private String app_id;

    private String seller_id;

    private String gmt_payment;

    private String notify_time;

    private String version;

    private String out_trade_no;

    private String total_amount;

    private String trade_no;

    private String auth_app_id;

    private String buyer_logon_id;

    private String point_amount;
}
