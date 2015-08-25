package com.lewa.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.helper.ImageHelper;
import com.lewa.player.listener.BaseLibraryListener;

/**
 * Created by wuzixiu on 12/15/13.
 */
public class MaskEndListView extends ListView {
    Bitmap startBitmap = null;
    Bitmap endBitmap = null;
    Paint paint = null;
    private Drawable mHeaderMaskDrawable;
    private Drawable mFooterMaskDrawable;
    private int mHeaderMaskHeight;
    private int mFooterMaskHeight;
    private View mBlankFooter;
    private BaseLibraryListener mTrackListener;
    private int mMotionY;

    public MaskEndListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(
                PorterDuff.Mode.DST_IN));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaskEndListView);
        mFooterMaskDrawable = a.getDrawable(R.styleable.MaskEndListView_lv_footer_mask);
        mHeaderMaskDrawable = a.getDrawable(R.styleable.MaskEndListView_lv_header_mask);
        mHeaderMaskHeight = a.getDimensionPixelSize(R.styleable.MaskEndListView_lv_header_mask_height, -1);
        mFooterMaskHeight = a.getDimensionPixelSize(R.styleable.MaskEndListView_lv_footer_mask_height, -1);

        a.recycle();

        mBlankFooter = Lewa.inflater().inflate(R.layout.v_transpant_footer, null);
        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mFooterMaskHeight);
        mBlankFooter.setLayoutParams(layoutParams);
        mBlankFooter.setFocusable(false);
        mBlankFooter.setFocusableInTouchMode(false);
        mBlankFooter.setClickable(false);
        mBlankFooter.setLongClickable(false);
        mBlankFooter.setOnClickListener(null);
        super.addFooterView(mBlankFooter);
    }

    protected void dispatchDraw(Canvas canvas) {
        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null,
                Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                        | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                        | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                        | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

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
                View mDownView = null;
                Rect rect = new Rect();
                int childCount = getChildCount();
                int[] listViewCoords = new int[2];
                getLocationOnScreen(listViewCoords);
                int x1 = (int) event.getRawX() - listViewCoords[0];
                int y1 = (int) event.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x1, y1)) {
                        mDownView = child; // This is your down view
                        break;
                    }
                }

                if (mDownView == null && mTrackListener != null) {
                    mTrackListener.hideSongList();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // 当你滑动屏幕时是 ACTION_MOVE 事件，在这里做逻辑处理
                // （y - mMotionY） 的正负就代表了 向上和向下
                if (mTrackListener != null) {
                    if ((y - mMotionY) > 5) {
                        mTrackListener.showMiniPlayerView();
                    } else if ((y - mMotionY) < -5) {
                        mTrackListener.hideMiniPlayerView();
                    }

                    if (Math.abs(y - mMotionY) > 10) {
                        mTrackListener.hideSongList();
                    }
                }
                mMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackListener != null) {
                    mTrackListener.showMiniPlayerView();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public int getFooterMaskHeight() {
        return mFooterMaskHeight;
    }
}
