package com.lewa.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.lewa.player.R;
import lewa.util.LewaUiUtil;
import android.util.Log;

public class VerticalSeekBar extends SeekBar {
    private Drawable mThumb;

    public interface OnSeekBarChangeListener {
        void onProgressChanged(VerticalSeekBar VerticalSeekBar, int progress, boolean fromUser);
        void onStartTrackingTouch(VerticalSeekBar VerticalSeekBar);
        void onStopTrackingTouch(VerticalSeekBar VerticalSeekBar);
    }

    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    public VerticalSeekBar(Context context) {
        this(context, null);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.seekBarStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

	public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }

    void onStartTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    public void doAfterSetProgress() {
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    void onStopTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    void onProgressRefresh(float scale, boolean fromUser) {
        Drawable thumb = mThumb;
        if (thumb != null) {
            setThumbPos(getHeight(), thumb, scale, Integer.MIN_VALUE);
            invalidate();
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser);
        }
    }

	private void setThumbPos(int w, Drawable thumb, float scale, int gap) {
		int available = w - getPaddingLeft() - getPaddingRight();
		int thumbWidth = thumb.getIntrinsicWidth();
		int thumbHeight = thumb.getIntrinsicHeight();
		available -= thumbWidth;
		// The extra space for the thumb to move on the track
		available += getThumbOffset() * 2;
		int thumbPos = (int) (scale * available);
		int topBound, bottomBound;
		if (gap == Integer.MIN_VALUE) {
			Rect oldBounds = thumb.getBounds();
			topBound = oldBounds.top;
			bottomBound = oldBounds.bottom;
		} else {
			topBound = gap;
			bottomBound = gap + thumbHeight;
		}
		thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound);
	}

	@Override
	protected void onDraw(Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);
		super.onDraw(c);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

    @Override
	public void setThumb(Drawable thumb) {
        mThumb = thumb;
		super.setThumb(thumb);
	}

	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(h, w, oldw, oldh);
    }


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setPressed(true);
				onStartTrackingTouch();
				trackTouchEvent(event);
				onSizeChanged(getWidth(), getHeight(), 0, 0);
				break; 
			case MotionEvent.ACTION_MOVE:
			  	trackTouchEvent(event);
				attemptClaimDrag();
				onSizeChanged(getWidth(), getHeight(), 0, 0);
				break;

			case MotionEvent.ACTION_UP:
				trackTouchEvent(event);
				onStopTrackingTouch();
				setPressed(false);
				onSizeChanged(getWidth(), getHeight(), 0, 0);
				break;

			case MotionEvent.ACTION_CANCEL:				
				onStopTrackingTouch();
				setPressed(false);
				onSizeChanged(getWidth(), getHeight(), 0, 0);
				break;
		}
		return true;
	}

    private void trackTouchEvent(MotionEvent event) {
        final int Height = getHeight();
        final int available = Height - getPaddingBottom() - getPaddingTop();
        int Y = (int)event.getY();
        float scale;
        float progress = 0;
        if (Y > Height - getPaddingBottom()) {
            scale = 0.0f;
        } else if (Y  < getPaddingTop()) {
            scale = 1.0f;
        } else {
            scale = (float)(Height - getPaddingBottom()-Y) / (float)available;
        }


        final int max = getMax();
        progress = scale * max;

        setProgress((int) progress);
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
        	getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
    	if(event.getAction()==KeyEvent.ACTION_DOWN) {
    		KeyEvent newEvent = null;
    		switch(event.getKeyCode()) {
    			case KeyEvent.KEYCODE_DPAD_UP:
    				newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT);
    				break;
    			case KeyEvent.KEYCODE_DPAD_DOWN:
    				newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_LEFT);
    				break;
    			case KeyEvent.KEYCODE_DPAD_LEFT:
    				newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN);
    				break;
    			case KeyEvent.KEYCODE_DPAD_RIGHT:
    				newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP);
    				break;
    			default:
    				newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,event.getKeyCode());
					break;
    		}
    		return newEvent.dispatch(this);
    	}
    	return false;
    }
}
