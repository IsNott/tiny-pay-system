package org.nott.constant;

public class AlipayBusinessConstant {

    public interface Common {
        final String UTF8 = "UTF-8";
        final String RSA2 = "RSA2";

        final String JSON = "json";
    }

    public interface Trade {
        final String SUCCESS = "TRADE_SUCCESS";

        final String CLOSED = "TRADE_CLOSED";
    }

    public interface TradeFiled {
        final String OUT_TRADE_NO = "out_trade_no";
        final String TOTAL_AMOUNT = "total_amount";
        final String SUBJECT = "subject";
        final String PRODUCT_CODE = "product_code";
    }
}
