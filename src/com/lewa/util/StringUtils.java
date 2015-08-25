package com.lewa.util;

import android.text.TextUtils;

public class StringUtils {
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
        return StringUtils.isBlank(str) ? defaultStr : str;
    }
}