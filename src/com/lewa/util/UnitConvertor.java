package com.lewa.util;

import android.content.Context;

/**
 * Created by Administrator on 13-11-28.
 */
public class UnitConvertor {

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5f);
    }


}
