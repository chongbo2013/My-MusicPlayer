package com.lewa.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.lewa.player.listener.PlayStatusListener;
import android.content.Context;
import com.lewa.player.R;
import java.lang.Runnable;
import com.lewa.Lewa;
import android.content.res.Resources;



public class BlurRunnable implements Runnable {

    Context context = null;
    PlayStatusListener mPlayStatusListener = null;

    public BlurRunnable(Context context, PlayStatusListener playStatusListener) {
        this.context = context;
        this.mPlayStatusListener = playStatusListener;
    }

    public void run() {
        blurDefaultBg(context.getResources(), mPlayStatusListener);
    }

    public static void  blurDefaultBg(Resources res, PlayStatusListener mPlayStatusListener) {
        if(null == res) {
            return;
        }
        Bitmap cover = BitmapFactory.decodeResource(res, R.drawable.cover);
        if(null != cover ) {
            Lewa.startBlur(cover, null, mPlayStatusListener);
        }
    }
}

