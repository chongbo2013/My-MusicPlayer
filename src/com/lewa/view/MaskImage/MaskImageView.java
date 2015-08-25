package com.lewa.view.MaskImage;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;

import com.lewa.Lewa;
import com.lewa.player.R;

/**
 * Created by wuzixiu on 12/10/13.
 */
@RemoteView
public class MaskImageView extends ImageView {
    private static final String TAG = MaskImageView.class.getName();
    private Paint mPaint = new Paint();
    private Path mPath;
    private int mSize = 0;
    private int mMaskColor = -1;

    public MaskImageView(Context context) {
        super(context);
        mPath = PathHelper.doPath(Lewa.string(R.string.svg_180));
        Log.i(TAG, "Create MaskImageView.");
    }

    public MaskImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPath = PathHelper.doPath(Lewa.string(R.string.svg_180));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaskImageView);
        mSize = a.getDimensionPixelSize(R.styleable.MaskImageView_layout_size, 180);
        mMaskColor = a.getColor(R.styleable.MaskImageView_mask_color, -1);
        Log.i(TAG, "Create MaskImageView, layout pixes size: " + mSize);
    }

    @Override
    public void onDraw(Canvas canvas) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable();

        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null) {
                //Draw background here, as we don't call super.onDraw() any more
                if (getBackground() != null) {
                    getBackground().draw(canvas);
                }

                MaskBitmapDrawable mcd = new MaskBitmapDrawable(Lewa.resources(), bitmap);
                mcd.setPath(mPath);
//                Log.i(TAG, "Canvas size: h" + canvas.getHeight() + "\tw" + canvas.getWidth() + "\tBitmap size: h" + bitmap.getHeight() + "\tw" + bitmap.getWidth());
                mcd.setMaskColor(mMaskColor);
                mcd.setViewSize(mSize);
                mcd.draw(canvas);
            } else {
                super.onDraw(canvas);
            }
        } else {
            super.onDraw(canvas);
        }

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.v(TAG, "On Measure: h" + getMeasuredHeight() + " w" + getMeasuredWidth());
    }

    public void setPath(Path path) {
        mPath = path;
    }

    public void setSize(int size) {
        Log.v(TAG, "set size: " + size);
        setLayoutSize(size);
        requestLayout();
        invalidate();
    }

    public void setLayoutSize(int size) {
        Log.v(TAG, "set layout size: " + size);
        mSize = size;
        getLayoutParams().height = size;
        getLayoutParams().width = size;
    }

    public int getSize() {
        return mSize;
    }

    public void setMaskColor(int color) {
        mMaskColor = color;
    }

    public int getMaskColor() {
        return mMaskColor;
    }
}