package com.lewa.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.lewa.player.R;

public class AlphabetFastIndexer extends View {
    OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26 char
    public static String[] b = { "\u2605", "#", "A", "B", "C",
            "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
            "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
    private int choose = 0;
    private Paint paint  = new Paint();
    private int mChooseColor;
    private int mTextsize;
    private int mPaddingBottom;
    private boolean mHasFav = true;
    
    private ArrayList<String> mLetters = new ArrayList<String>();
    
    public AlphabetFastIndexer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        setHasFavorite(false);
    }

    public AlphabetFastIndexer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setHasFavorite(false);
    }
    
    public AlphabetFastIndexer(Context context) {
        super(context);
        init(context);
        setHasFavorite(false);
    }
    
    private void init(Context context) {
        mChooseColor = context.getResources().getColor(android.R.color.holo_blue_light);
        mTextsize = context.getResources().getDimensionPixelSize(R.dimen.alphabet_fast_index_text_size);
        mPaddingBottom =54;
    }
    
    public void setHasFavorite(boolean hasFav) {
        if (mHasFav != hasFav) {
            mHasFav = hasFav;
            mLetters.clear();
            for(String letter : b) {
                if (!hasFav && letter.equals("\u2605")) continue;
                
                mLetters.add(letter);
            }
            if (choose != 0) choose = hasFav ? choose + 1 : choose - 1;
            if (choose < 0) choose = 0;
            else if (choose >= mLetters.size()) choose = mLetters.size()-1;
            
            requestLayout();
        }
    }
    
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, mHasFav ? heightMeasureSpec - mPaddingBottom : heightMeasureSpec);
    }

    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showBkg) {
            canvas.drawColor(Color.parseColor("#4C000000"));
        } else {
            canvas.drawColor(Color.parseColor("#26000000"));
        }
        
        int height = getHeight();
        int width = getWidth();
        int size = mLetters.size();
        int singleHeight = height / size;
        for (int i = 0; i < size; i++) {
            paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            paint.setTextSize(mTextsize);
            if (i == choose) {
                paint.setColor(mChooseColor);
                paint.setFakeBoldText(true);
            }
            String letterStr = mLetters.get(i);
            float xPos = width / 2 - paint.measureText(letterStr) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(letterStr, xPos, yPos, paint);
            paint.reset();
        }

    }

    private boolean showBkg = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int c = (int) (y / getHeight() * mLetters.size());

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            showBkg = true;
//            if (oldChoose != c && listener != null) {
//                if (c >= 0 && c < b.length) {
//                    if (listener != null) {
//                        listener.onTouchingLetterChanged(b[c]);
//                    }
//                    choose = c;
//                    invalidate();
//                }
//            }
//
//            break;
        case MotionEvent.ACTION_MOVE:
            drawThumb(c);
            break;
        case MotionEvent.ACTION_UP:
            showBkg = false;
//            choose = -1;
            invalidate();
            break;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
    
    public void drawThumb(int position) {
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        if (choose != position && listener != null) {
            if (position >= 0 && position < mLetters.size()) {
                if (listener != null) {
                    listener.onTouchingLetterChanged(mLetters.get(position));
                }
                choose = position;
                invalidate();
            }
        }
    }
    
    public void drawThumb(String letter) {
        if (letter != null && !letter.equals(mLetters.get(choose))) {
            int i = mLetters.indexOf(letter);
            if (i == -1) i = 0;
            if (choose != i) {
                choose = i;
                invalidate();
            }
        }
    }
    /**
     * public method
     * 
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * Interface
     * 
     * @author coder
     * 
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }
}
