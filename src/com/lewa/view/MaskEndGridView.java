package com.lewa.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.GridView;

import com.lewa.player.R;
import com.lewa.player.helper.ImageHelper;
import com.lewa.player.listener.BaseLibraryListener;


/**
 * Created by wuzixiu on 12/15/13.
 */
public class MaskEndGridView  extends   GridView{ 
    private static final String TAG = "MaskEndGridView"; //.class.getName();
    Bitmap startBitmap = null;
    Bitmap endBitmap = null;
    Paint paint = null;
    private Drawable mHeaderMaskDrawable;
    private Drawable mFooterMaskDrawable;
    private int mHeaderMaskHeight;
    private int mFooterMaskHeight;
    private BaseLibraryListener mTrackListener;
    private int mMotionY;


    public MaskEndGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(
                PorterDuff.Mode.DST_IN));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaskEndGridView);
        mFooterMaskDrawable = a.getDrawable(R.styleable.MaskEndGridView_gv_footer_mask);
        mHeaderMaskDrawable = a.getDrawable(R.styleable.MaskEndGridView_gv_header_mask);
        mHeaderMaskHeight = a.getDimensionPixelSize(R.styleable.MaskEndGridView_gv_header_mask_height, -1);
        mFooterMaskHeight = a.getDimensionPixelSize(R.styleable.MaskEndGridView_gv_footer_mask_height, -1);
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.i(TAG, "On layout.");
    }

    protected void dispatchDraw(Canvas canvas) {
        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null,
                Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                        | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                        | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                        | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

        Log.d(TAG, "Dispatch draw.");
        super.dispatchDraw(canvas);
        drawMask(canvas);

        canvas.restoreToCount(sc);
    }

    private void drawMask(Canvas canvas) {
        if (startBitmap == null && mHeaderMaskDrawable != null) {
            startBitmap = ImageHelper.drawableToBitmap(mHeaderMaskDrawable, canvas.getWidth(), mHeaderMaskHeight);
        }

        if (mHeaderMaskDrawable != null) {
            canvas.drawBitmap(startBitmap, 0, 0, paint);
        }

        if (endBitmap == null && mFooterMaskDrawable != null) {
            endBitmap = ImageHelper.drawableToBitmap(mFooterMaskDrawable, canvas.getWidth(), mFooterMaskHeight);
        }

        if (mFooterMaskDrawable != null) {
            canvas.drawBitmap(endBitmap, 0, canvas.getHeight() - mFooterMaskHeight, paint);
        }
    }

    public void setOnTrackListener(BaseLibraryListener listener) {
        this.mTrackListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 记录点击时 y 的坐标
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 第一次点击是 ACTION_DOWN 事件，把值保存起来
                mMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                // 当你滑动屏幕时是 ACTION_MOVE 事件，在这里做逻辑处理
                // （y - mMotionY） 的正负就代表了 向上和向下
                if(mTrackListener != null) {
                    if ((y - mMotionY) > 5) {
                        mTrackListener.showMiniPlayerView();
                    } else if ((y - mMotionY) < -5) {
                        mTrackListener.hideMiniPlayerView();
                    }
                }
                mMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                if(mTrackListener != null) {
                    mTrackListener.showMiniPlayerView();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

}
