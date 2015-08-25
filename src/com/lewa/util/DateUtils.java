package com.lewa.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 13-12-7.
 */
public class DateUtils {

    private static SimpleDateFormat y4M2d2Format = new SimpleDateFormat("yyyy.MM.dd");
    private static SimpleDateFormat m2s2Format = new SimpleDateFormat("m:ss");

    public static String y4M2d2S(Date date) {
        return y4M2d2Format.format(date);
    }

    //TODO: check the result if the first character is striped.
    public static String m2s2(Date date) {
        return m2s2Format.format(date);
    }
}
