package com.lewa.view.MaskImage;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Shader.TileMode;

public class MaskBitmapUtils {

    public static void drawBitmap(Bitmap bitmap, Canvas canvas, Path path, Paint paint,
                                  float viewSize) {
        if (bitmap != null && canvas != null && path != null && paint != null) {
            BitmapShader bs = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            //float viewSize = 240f;
            float c = 1;
            if (w != viewSize || h != viewSize) {
                c = viewSize / Math.min(w, h);
            }

            Matrix localM = new Matrix();
            float dx = 0;
            float dy = 0;
            float newx = w * c;
            float newy = h * c;
            if (newx > viewSize) {
                dx = (newx - viewSize) / 2;
            }
            if (newy > viewSize) {
                dy = (newy - viewSize) / 2;
            }
            localM.setScale(c, c);
            localM.postTranslate(-dx, -dy);
            bs.setLocalMatrix(localM);

            paint.setAntiAlias(true);
            paint.setStyle(Style.FILL);
            paint.setShader(bs);
            canvas.drawPath(path, paint);
        }

    }
}
