package org.nott.id;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Nott
 * @date 2024-5-7
 */
public class TransactionNoFactory {

    private static final String SYS_VERSION = "1";

    private static final String DATA_VERSION = "0";

    private static final String SYS_IDENTIFICATION_CODE = "001";

    public static String next(){
        Date date = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
        String format = formater.format(date);
        String id = IdWorker.getIdStr();
        String idstr = format + SYS_VERSION + DATA_VERSION + SYS_IDENTIFICATION_CODE + id;
        return idstr;
    }



}
