package com.lewa.util;

import android.graphics.Bitmap;

/**
 * Created by wuzixiu on 1/21/14.
 */
public class ImageUtilsSo {
    static {
        try {
            System.loadLibrary("lewa_imageutils");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    private static native void native_blur(Bitmap in, Bitmap out, int radius);

    public static void fastBlur(Bitmap in, Bitmap out, int radius) {
        native_blur(in, out, radius);
    }
}
