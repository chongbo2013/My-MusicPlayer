package com.lewa.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import lewa.util.ImageUtils;

public class Blur {

    public static Bitmap createBlurBitmap(Bitmap bitmap, int width, int height, int radius) {
        try {
            if (bitmap == null) {
                return null;
            }
            int left = (bitmap.getWidth() - width) / 2;
            int right = (bitmap.getWidth() + width) / 2;
            int top = (bitmap.getHeight() - height) / 2;
            int bottom = (bitmap.getHeight() + height) / 2;
            Rect src = new Rect(left, top, right, bottom);
            Bitmap bmp = Bitmap.createBitmap(src.width() / 3, src.height() / 3, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Rect dst = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
            canvas.drawBitmap(bitmap, src, dst, null);
            Bitmap blurred = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
                    Bitmap.Config.ARGB_8888);
            blurred.eraseColor(0xff000000);
            ImageUtils.fastBlur(bmp, blurred, radius);
            bmp.recycle();
            return blurred;
        } catch (OutOfMemoryError e) {
        } catch (Exception e) {
        }
        return null;
    }

    public static Bitmap createBlurBitmap(Bitmap bitmap, int radius) {
        if (radius == 0) return bitmap;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        return createBlurBitmap(bitmap, w, h, radius);
    }

}
