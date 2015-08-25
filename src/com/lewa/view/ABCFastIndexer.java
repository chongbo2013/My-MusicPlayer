package com.lewa.view;

import com.lewa.player.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class ABCFastIndexer extends FrameLayout {

    private static final String TAG = "ABCFastIndexer";
    OnTouchingLetterChangedListener mListener;
    // 26 char
    public static String[] letters = { /*"\u2605", */"#", "A", "B", "C", "D", "E",
            "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z" };
    private int choose = 0;
    private Paint paint = new Paint();
    private int mChooseColor;
    private int mBarTextSize;
    private int mBarWidth;
    private TextView mPopTextView;
    private int mPopTextSize;
    private int mPopTextColor;
    private Drawable mBarBackground;
    private Drawable mPopBackground;
    private boolean showBkg = false;
    private ArrayList<String> mLetters = new ArrayList<String>();

    private ArrayList<Integer> mY = new ArrayList<Integer>();
    private int mX = 0;
    
    private Handler mHandler = new Handler();

    private int mPopLeftPadding;
    private int mAbcfastBoomPadding;
    private int mPopTextRingtPadding;
    private int mPopTextBoomPadding;
    private int mAbcfastTopPadding = 0;

    private Runnable mHideFastIndexerPop = new Runnable() {
        @Override
        public void run() {
            mPopTextView.setVisibility(View.GONE);
        }
    };

    public ABCFastIndexer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ABCFastIndexer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ABCFastIndexer(Context context) {
        super(context);
        init(context);
    }

    public void setLetters(ArrayList<String> letters) {
        mLetters.clear();
        mLetters.addAll(letters);
    }

    public void setLetters(String[] letters) {
        mLetters.clear();
        for (String letter : letters) {
            mLetters.add(letter);
        }
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        mChooseColor = resources.getColor(android.R.color.white);
        mBarTextSize = resources.getDimensionPixelSize(
                R.dimen.alphabet_fast_index_text_size);
        mBarWidth = resources.getDimensionPixelSize(R.dimen.abc_fast_index_bar_width);
        mPopTextSize =  resources.getDimensionPixelSize(R.dimen.choose_text_size);
        mPopTextColor = resources.getColor(android.R.color.white);
        mBarBackground = resources.getDrawable(R.drawable.abc_fast_index_bar_bg);
        mPopBackground = resources.getDrawable(R.drawable.abcfastindexer_pop_bg);

        mPopLeftPadding = resources.getDimensionPixelSize(R.dimen.abc_fast_left_padding);
        mAbcfastBoomPadding = resources.getDimensionPixelSize(R.dimen.abc_fast_boom_padding);
        mPopTextRingtPadding = resources.getDimensionPixelSize(R.dimen.abc_fast_index_right_padding);
        mPopTextBoomPadding = resources.getDimensionPixelSize(R.dimen.abc_fast_index_boom_padding);
        
        mLetters.clear();
        for (String letter : letters) {
            mLetters.add(letter);
        }

        getPopTextView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        
        int height = getHeight() - mAbcfastBoomPadding;
        int width = getWidth();
        
        if (showBkg) {
            if (mBarBackground != null) {
                mBarBackground.setBounds(width - mBarWidth, mAbcfastTopPadding , width, height + mAbcfastTopPadding);
                mBarBackground.draw(canvas);
            }
        } else {
            canvas.drawColor(Color.parseColor("#00000000"));
        }
        int size = mLetters.size();
        int singleHeight = height / size;
        int remainder = height - singleHeight * size;
        int paddingTop = remainder / 2 + mAbcfastTopPadding;
        mY.clear();
        mX = 0;
        for (int i = 0; i < size; i++) {
            paint.setColor(Color.argb(127, 255, 255, 255));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            paint.setTextSize(mBarTextSize);
            FontMetrics fontMetrics = paint.getFontMetrics();

            if (i == choose) {
                paint.setColor(mChooseColor);
                paint.setFakeBoldText(true);
            }
            String letterStr = mLetters.get(i);
            float xPos = mBarWidth / 2 - paint.measureText(letterStr) / 2 + (width - mBarWidth);
            float yPos = singleHeight * i + singleHeight - fontMetrics.bottom + paddingTop;
            mY.add((int)yPos);
            //mX.add((int)xPos);
            mX = (int)xPos;
            canvas.drawText(letterStr, xPos, yPos, paint);
            paint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float xf = event.getRawX();
        float yf = event.getRawY();
        Rect frame = new Rect();
        getGlobalVisibleRect(frame);
        frame.left = frame.right - mBarWidth;

        
        final int action = event.getAction();
        final float y = event.getY() - mAbcfastTopPadding;
        final int c = (int) (y / (getHeight() - mAbcfastBoomPadding)* mLetters.size());
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (!frame.contains((int) xf, (int) yf)) {
                return false;
            }
            showBkg = true;

        case MotionEvent.ACTION_MOVE:
            if (!frame.contains((int) xf, (int) yf)) {
                return false;
            }
            drawThumb(c, true);
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            showBkg = false;

            invalidate();
            break;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void drawThumb(int position, boolean isShowPop) {
        if (choose != position && mListener != null) {
            if (position >= 0 && position < mLetters.size()) {
                choose = position;
                //Lewa fjli #OS6 type.start
               LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

               //layoutParams.setMargins((int)(mX.get(3)) - (mPopBackground.getMinimumWidth() - mPopLeftPadding), (int)(mY.get(choose)) - (mPopBackground.getMinimumHeight()/2), 0 , 0);
               if(mY.size() <= choose) {
                    return;
               }
               layoutParams.setMargins(mX - (mPopBackground.getMinimumWidth() - mPopLeftPadding), (int)(mY.get(choose)) - (mPopBackground.getMinimumHeight()/2), 0 , 0);
                mPopTextView.setLayoutParams(layoutParams);
                //end
                mPopTextView.setText(mLetters.get(choose));
                if (isShowPop && mPopTextView.getVisibility() != View.VISIBLE) {
                    mPopTextView.setVisibility(View.VISIBLE);
                }
                mHandler.removeCallbacks(mHideFastIndexerPop);
                mHandler.postDelayed(mHideFastIndexerPop, 1000);
                if (mListener != null) {
                    mListener.onTouchingLetterChanged(mLetters.get(position));
                }
                invalidate();
            }
        }
    }

    public void drawThumb(String letter) {
        if (letter != null && choose < mLetters.size() && !letter.equals(mLetters.get(choose))) {
            int i = mLetters.indexOf(letter);
            if (i == -1)
                i = 0;
            if (choose != i) {
                choose = i;
                invalidate();
            }
        }
    }
    
    public void drawThumb(char letter) {
        drawThumb(String.valueOf(letter));
    }
    
    public void drawThumb(int position) {
        drawThumb(position, false);
    }
    
    private TextView getPopTextView() {
        if (mPopTextView == null) {
            mPopTextView = new TextView(getContext());
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            layoutParams.gravity = Gravity.LEFT;
            mPopTextView.setLayoutParams(layoutParams);
            mPopTextView.setTextColor(mPopTextColor);
            mPopTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mPopTextSize);
            mPopTextView.setBackground(mPopBackground);
            mPopTextView.setGravity(Gravity.CENTER);
            mPopTextView.setPadding(0, 0, mPopTextRingtPadding, mPopTextBoomPadding);
            mPopTextView.setVisibility(View.GONE);
            addView(mPopTextView);
        }
        return mPopTextView;
    }

    /**
     * public method
     * 
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.mListener = onTouchingLetterChangedListener;
    }

    public void setPopBackgroundResource(int resId) {
        if (resId > 0) {
            mPopBackground = mContext.getResources().getDrawable(resId);
        } else {
            mPopBackground = null;
        }
        if (mPopTextView != null) {
            mPopTextView.setBackground(mPopBackground);
        }
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
    
    public void setmAbcfastTopPadding(int mAbcfastTopPadding) {
        this.mAbcfastTopPadding = mAbcfastTopPadding;
    }

    public void setmAbcfastBoomPadding(int mAbcfastBoomPadding) {
        this.mAbcfastBoomPadding = mAbcfastBoomPadding;
    }
    
    //Set the font color
    public void setTextColor(int color){
        if (mPopTextView != null) {
           mPopTextView.setTextColor(color);
        }
    }

    public void setPaintColor(int color){
        if(paint != null){
            paint.setColor(color);
        }
    }
    public void setPaintAlpha(int alpha){
        if(paint != null){
            paint.setAlpha(alpha);
        }
    }
}
