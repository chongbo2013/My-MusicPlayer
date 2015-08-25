package com.lewa.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;

/**
 * Created by wuzixiu on 12/15/13.
 */
public class GradientImageView extends RecyclingImageView {
    private Paint mMaskPaint;

    public GradientImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMaskPaint = new Paint();
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null,
                Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                        | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                        | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                        | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

        super.onDraw(canvas);
        drawGradual(canvas);

        canvas.restoreToCount(sc);
    }

    private void drawGradual(Canvas canvas) {
        LinearGradient shader = new LinearGradient(0, 0, 0, canvas.getHeight(),
                Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);
        mMaskPaint.setShader(shader);
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, 0, canvas.getWidth()-10, canvas.getHeight()-10, mMaskPaint);
    }

}
