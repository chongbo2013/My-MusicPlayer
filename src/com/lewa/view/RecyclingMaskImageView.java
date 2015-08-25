package com.lewa.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;

import com.lewa.player.R;

/**
 * Created by wuzixiu on 12/10/13.
 */
@RemoteView
public class RecyclingMaskImageView extends RecyclingImageView {
    private Paint mMaskPaint;
    private Drawable mMaskDrawable;
    private int mDegree = 0;

    public RecyclingMaskImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclingMaskImageView);
        mDegree = a.getInteger(R.styleable.RecyclingMaskImageView_rotation, mDegree);
        mMaskDrawable = a.getDrawable(R.styleable.RecyclingMaskImageView_maskDrawables);

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
        Bitmap bm = ((BitmapDrawable) mMaskDrawable).getBitmap();
        Rect srcRect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
        Rect rect = new Rect(0, 0, this.getWidth(), this.getHeight());
        canvas.drawBitmap(bm, srcRect, rect, mMaskPaint);
    }

}
