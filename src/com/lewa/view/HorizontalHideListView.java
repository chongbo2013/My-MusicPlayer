package com.lewa.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.lewa.player.listener.BaseLibraryListener;

/**
 * Created by wuzixiu on 12/15/13.
 */
public class HorizontalHideListView extends ListView {
    private BaseLibraryListener mTrackListener;
    private int mMotionX;
    private int mMotionY;

    public HorizontalHideListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnTrackListener(BaseLibraryListener listener) {
        this.mTrackListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 记录点击时 y 的坐标
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 第一次点击是 ACTION_DOWN 事件，把值保存起来
                mMotionX = x;
                mMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                // 当你滑动屏幕时是 ACTION_MOVE 事件，在这里做逻辑处理
                // （x - mMotionX） 的正负就代表了 向左右
                if (mTrackListener != null) {
                    if ((x - mMotionX) > 20 && Math.abs(y - mMotionY) < 20) {
                        mTrackListener.hideSongList();
                    }
                }
                mMotionX = x;
                mMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

}
