package com.lewa.view.lyric;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.lewa.Lewa;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LyricViewVertical extends ScrollView {
    private Paint NotCurrentPaint;
    private Paint CurrentPaint;
    private Paint MovingCurrentPaint;
    private Paint MovingLinePaint;
    private float moveScroll = 0;
    private long movingCurrentTime = 0;
    private boolean onmove = false;
    private int hrindex = 0;

    private int notCurrentPaintColor = Color.WHITE;
    private int CurrentPaintColor = Color.WHITE;
    private int MovingCurrentPaintColor = Lewa.resources().getColor(R.color.blue_text);
    private int MovingLinePaintColor = Color.WHITE;
    private Typeface Texttypeface = Typeface.SERIF;
    private Typeface CurrentTexttypeface = Typeface.DEFAULT_BOLD;
    private float width;
    private static Lyric mLyric;
    private int brackgroundcolor = Color.TRANSPARENT;
    private float lrcTextSize = 0; // ��ʴ�С
    private float CurrentTextSize = 0;
    private float SecondSize = 22;
    private float ThridSize = 16;
    private float ForthSize = 12;
    private int speedScroll = 20;
    private int indexBeforeMove = 0;
    private IMediaPlaybackService mService;
    private long sct;
    private int sctmove;
    private Context mContext;
    // private Align = Paint.Align.CENTER

    public float mTouchHistoryY;
    private float firstclick;
    private float secondclick;
    private int count = 0;

    private int height;
    private long currentDunringTime;
    // private float middleY;
    private int TextHeight = 20; // 每一行的间隔
    private boolean lrcInitDone = false;
    public int index = 0;
    private int lastIndex = 0;
    private int sentenceNumber = 0;
    private List<Sentence> Sentencelist;

    private long currentTime;

    private long sentenctTime;

    //    public static boolean isHorizon = false;
    private long downtime;

    GestureDetector gestureDetector;
    ResumeScrollTask resumeScrollTask;
    final private Timer resumeTimer = new Timer("resumeTimer");

    public void setService(IMediaPlaybackService mService) {
        this.mService = mService;
    }

    public Paint getNotCurrentPaint() {
        return NotCurrentPaint;
    }

    public void setNotCurrentPaint(Paint notCurrentPaint) {
        NotCurrentPaint = notCurrentPaint;
    }

    public boolean isLrcInitDone() {
        return lrcInitDone;
    }

    public Typeface getCurrentTexttypeface() {
        return CurrentTexttypeface;
    }

    public void setCurrentTexttypeface(Typeface currrentTexttypeface) {
        CurrentTexttypeface = currrentTexttypeface;
    }

    public void setLrcInitDone(boolean lrcInitDone) {
        this.lrcInitDone = lrcInitDone;
    }

    public float getLrcTextSize() {
        return lrcTextSize;
    }

    public void setLrcTextSize(float lrcTextSize) {
        this.lrcTextSize = lrcTextSize;
    }

    public float getCurrentTextSize() {
        return CurrentTextSize;
    }

    public void setCurrentTextSize(float currentTextSize) {
        CurrentTextSize = currentTextSize;
    }

    public static Lyric getmLyric() {
        return mLyric;
    }

    public void setmLyric(Lyric mLyric) {
        LyricViewVertical.mLyric = mLyric;
    }

    public Paint getCurrentPaint() {
        return CurrentPaint;
    }

    /*
     * public void setCurrentPaint(Paint currentPaint) { CurrentPaint =
     * currentPaint; }
     */

    public List<Sentence> getSentencelist() {
        return Sentencelist;
    }

    public void setSentencelist(List<Sentence> sentencelist) {
        Sentencelist = sentencelist;
        if (Sentencelist != null)
            sentenceNumber = Sentencelist.size();
    }

    public int getNotCurrentPaintColor() {
        return notCurrentPaintColor;
    }

    public void setNotCurrentPaintColor(int notCurrentPaintColor) {
        this.notCurrentPaintColor = notCurrentPaintColor;
    }

    public int getCurrentPaintColor() {
        return CurrentPaintColor;
    }

    public void setCurrentPaintColor(int currrentPaintColor) {
        CurrentPaintColor = currrentPaintColor;
    }

    public Typeface getTexttypeface() {
        return Texttypeface;
    }

    public void setTexttypeface(Typeface texttypeface) {
        Texttypeface = texttypeface;
    }

    public int getBrackgroundcolor() {
        return brackgroundcolor;
    }

    public void setBrackgroundcolor(int brackgroundcolor) {
        this.brackgroundcolor = brackgroundcolor;
    }

    public int getTextHeight() {
        return TextHeight;
    }

    public void setTextHeight(int textHeight) {
        TextHeight = textHeight;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    private void setPram() {
        // TODO Auto-generated method stub
        lrcTextSize = getResources().getDimensionPixelOffset(
                R.dimen.lrc_text_size);
        CurrentTextSize = getResources().getDimensionPixelOffset(
                R.dimen.lrc_current_text_size);
        TextHeight = getResources().getDimensionPixelOffset(
                R.dimen.lrc_text_height);
        speedScroll = getResources().getDimensionPixelOffset(
                R.dimen.lrc_speed_scroll);
    }

    public LyricViewVertical(Context context, AttributeSet attr) {
        super(context, attr);
        gestureDetector = new GestureDetector(context, new GestureListener());
        setPram();
        init();
    }

    public LyricViewVertical(Context context, AttributeSet attr, int i) {
        super(context, attr, i);
        gestureDetector = new GestureDetector(context, new GestureListener());
        setPram();
        init();
    }

    private void init() {
        setFocusable(true);
        // PlayListItem pli = new PlayListItem("Because Of You",
        // "/sdcard/MP3/Because Of You.mp3", 0L, true);
        // mLyric = new Lyric(new File("/sdcard/MP3/Because Of You.lrc"), pli);

        NotCurrentPaint = new Paint();
        NotCurrentPaint.setAntiAlias(true);

        NotCurrentPaint.setTextAlign(Paint.Align.CENTER);

        CurrentPaint = new Paint();
        CurrentPaint.setAntiAlias(true);
        // CurrentPaint.setColor(CurrentPaintColor);

        CurrentPaint.setTextAlign(Paint.Align.CENTER);
        // list = mLyric.list;
        MovingCurrentPaint = new Paint();
        MovingCurrentPaint.setAntiAlias(true);
        MovingCurrentPaint.setTextAlign(Paint.Align.CENTER);

        this.MovingLinePaint = new Paint();

        MovingLinePaint.setAntiAlias(true);
        MovingLinePaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);

        /*int totalHeight = this.getHeight();
        final int iAction = event.getAction();
        final float iCurrentx = event.getX();
        final float iCurrenty = event.getY();


        switch (iAction) {
            case MotionEvent.ACTION_DOWN:
                downtime = System.currentTimeMillis();
                previousX = iCurrentx;
                previousY = iCurrenty;
                indexBeforeMove = index;
                // Log.i("MotionEvent","ACTION_DOWN");
                moveScroll = 0;
                sct = currentTime - sentenctTime;
                if (currentDunringTime != 0)
                    sctmove = (int) (20 * sct / currentDunringTime);
                movingCurrentTime = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                long movetime = System.currentTimeMillis();
                if (movetime - downtime < 300)
                    break;
                onmove = true;
                float moved = iCurrenty - previousY;
                moveScroll = moved;

                break;
            case MotionEvent.ACTION_UP:
                moveScroll = 0;
                try {
                    // if(movingCurrentTime == 0){
                    // mService.seek(currentTime);
                    // }else{
                    if (this.Sentencelist != null && hrindex >= 0 && this.Sentencelist.get(hrindex) != null)
                        mService.seek(this.Sentencelist.get(hrindex).getFromTime());
                    // }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                movingCurrentTime = 0;
                onmove = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                onmove = false;
                break;

            default:
                break;

        }
        return true;*/
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            downtime = System.currentTimeMillis();
            indexBeforeMove = index;
            // Log.i("MotionEvent","ACTION_DOWN");
//            moveScroll = 0;
//            sct = currentTime - sentenctTime;
//            if (currentDunringTime != 0)
//                sctmove = (int) (20 * sct / currentDunringTime);
//            movingCurrentTime = 0;

            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            onmove = true;
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();

            moveScroll -= distanceY;

            if (resumeScrollTask != null) {
                resumeScrollTask.cancel();
            }

            resumeScrollTask = new ResumeScrollTask();
            resumeTimer.schedule(resumeScrollTask, 2000);

//            LyricViewVertical.this.smoothScrollBy((int) dx, (int) dy);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Log.e("Update", "onDraw");
        canvas.drawColor(brackgroundcolor);
        NotCurrentPaint.setColor(notCurrentPaintColor);
        CurrentPaint.setColor(CurrentPaintColor);
        MovingCurrentPaint.setColor(MovingCurrentPaintColor);
        MovingLinePaint.setColor(MovingLinePaintColor);

        NotCurrentPaint.setTextSize(lrcTextSize);
        // NotCurrentPaint.setColor(notCurrentPaintColor);
        NotCurrentPaint.setTypeface(Texttypeface);

        CurrentPaint.setTextSize(lrcTextSize);
        CurrentPaint.setTypeface(Texttypeface);

        MovingCurrentPaint.setTextSize(lrcTextSize);
        MovingCurrentPaint.setTypeface(Texttypeface);

        MovingLinePaint.setTextSize(lrcTextSize);
        MovingLinePaint.setTypeface(CurrentTexttypeface);

        float plus = currentDunringTime == 0 ? speedScroll
                : speedScroll
                + (((float) currentTime - (float) sentenctTime) / (float) currentDunringTime)
                * (float) speedScroll;

        float tempY = height / 2;
//        if (onmove) {
//            canvas.drawLine(0, tempY - speedScroll, getWidth(), tempY
//                    - speedScroll, MovingLinePaint);
//            // drawText(currentTime+"", canvas, MovingCurrentPaint, tempY,
//            // false);
//            if (mContext != null) {
//                canvas.drawText(
//                        MusicUtils.makeTimeString(mContext,
//                                movingCurrentTime / 1000) + "", mContext.getResources().getInteger(R.integer.lyric_time_X), tempY,
//                        MovingCurrentPaint
//                );
//            }
//        }

        float moveFromSentenceStart;
        int moveHeightSentence;
        hrindex = index;
        float movingScroll = moveScroll;
        if (onmove) {
            moveFromSentenceStart = moveScroll - plus;
            moveHeightSentence = -(int) Math.ceil(moveFromSentenceStart
                    / speedScroll);

            if (moveHeightSentence < 0) {
                int mhs = moveHeightSentence;
                int count = 0;
                while (mhs < 0) {
                    count++;
                    if (index - count < 0) {
                        count = index;
                        break;
                    }
                    List<String> str = autoSplit(
                            Sentencelist.get(index - count).getContent(),
                            NotCurrentPaint, getWidth() - 60);

                    mhs += str.size();
                }

                hrindex = index - count;

                float leftTimeScroll = speedScroll
                        - (-moveHeightSentence * speedScroll - moveFromSentenceStart);

                String hrContent = Sentencelist.get(hrindex).getContent();
                int strSize = autoSplit(hrContent, NotCurrentPaint,
                        getWidth() - 60).size();

                float leftTime = leftTimeScroll
                        * Sentencelist.get(hrindex).getDuring()
                        / (strSize * speedScroll);

                // Log.i("leftTime",""+leftTime);
                // float leftTime = leftTimeScroll *
                movingCurrentTime = (long) (Sentencelist.get(hrindex)
                        .getToTime() - leftTime);

            } else {
                int mhs = 0;
                int count = 0;
                while (mhs <= moveHeightSentence) {
                    if (index + count >= Sentencelist.size()) {
                        count = Sentencelist.size() - index;
                        break;
                    }
                    if ((index + count) < 0)
                        return;
                    List<String> str = autoSplit(
                            Sentencelist.get(index + count).getContent(),
                            NotCurrentPaint, getWidth() - 60);
                    mhs += str.size();
                    count++;
                }

                count--;
                hrindex = index + count;

                float leftTimeScroll = -moveFromSentenceStart - speedScroll
                        * moveHeightSentence;
                String hrContent = Sentencelist.get(hrindex).getContent();
                int strSize = autoSplit(hrContent, NotCurrentPaint,
                        getWidth() - 60).size();
                float leftTime = leftTimeScroll
                        * Sentencelist.get(hrindex).getDuring()
                        / (strSize * speedScroll);
                movingCurrentTime = (long) (Sentencelist.get(hrindex)
                        .getFromTime() + leftTime);
            }

        }

        if (onmove) {
            // Log.i("moveScroll",moveScroll+"");
            canvas.translate(0, -plus + moveScroll);
        } else {
            canvas.translate(0, -plus);
        }
        try {
            String content = "";
            if (index > 0) {
                content = Sentencelist.get(index).getContent();
            } else {
                content = "   ";
            }
            ArrayList<String> texts;
            // ArrayList<String> texts = autoSplit(content, CurrentPaint,
            // getWidth() - 60);
            if (onmove) {
                if (index == hrindex) {
                    texts = autoSplit(content, MovingCurrentPaint,
                            getWidth() - 60);
                    tempY = drawText(texts, canvas, MovingCurrentPaint,
                            tempY, false);
                } else {
                    texts = autoSplit(content, NotCurrentPaint,
                            getWidth() - 60);
                    tempY = drawText(texts, canvas, NotCurrentPaint, tempY,
                            false);
                }

                // canvas.drawl
            } else {
                texts = autoSplit(content, CurrentPaint, getWidth() - 60);
                tempY = drawText(texts, canvas, CurrentPaint, tempY, false);

            }
            // canvas.translate(0, plus);

            float baseY = tempY;
            tempY = height / 2;
            for (int i = index - 1; i >= 0; i--) {
                // Sentence sen = list.get(i);
                tempY = (float) (tempY - TextHeight);
                if (!onmove && tempY < 0) {
                    break;
                }

                if (index > 0) {
                    content = Sentencelist.get(i).getContent();
                } else {
                    content = "   ";
                }

                if (onmove && i == hrindex) {
                    texts = autoSplit(content, MovingCurrentPaint,
                            getWidth() - 60);
                    tempY = drawText(texts, canvas, MovingCurrentPaint,
                            tempY, true);
                } else {
                    texts = autoSplit(content, NotCurrentPaint,
                            getWidth() - 60);
                    tempY = drawText(texts, canvas, NotCurrentPaint, tempY,
                            true);// 已演唱的歌词
                }

            }

            tempY = baseY;
            if (Sentencelist == null) {
                return;
            }

            for (int j = index + 1; j < this.Sentencelist.size(); j++) {
                tempY = (float) (tempY + TextHeight);
                if (!onmove && tempY > height) {
                    break;
                }
                    /*
                     * if(j == index + 1) {
                     * NotCurrentPaint.setTextSize(SecondSize); }else if(j ==
                     * index + 2){ NotCurrentPaint.setTextSize(ThridSize); }else
                     * if(j >= index + 3){
                     * NotCurrentPaint.setTextSize(ForthSize); }
                     */
                if (index > 0) {
                    content = Sentencelist.get(j).getContent();
                } else {
                    content = "   ";
                }

                if (onmove && j == hrindex) {
                    texts = autoSplit(content, MovingCurrentPaint,
                            getWidth() - 60);
                    tempY = drawText(texts, canvas, MovingCurrentPaint,
                            tempY, false);
                } else {
                    texts = autoSplit(content, NotCurrentPaint,
                            getWidth() - 60);
                    tempY = drawText(texts, canvas, NotCurrentPaint, tempY,
                            false);// 未演唱的歌词
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
//    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        width = w; // remember the center of the screen
        height = h;
        // middleY = h * 0.5f;
    }

    public float drawText(ArrayList<String> texts, Canvas canvas, Paint paint,
                          float y, boolean ifUp) {
        int textsLen = texts.size();
        int s = 0;
        if (ifUp) {
            y -= TextHeight * (textsLen - 1);
        }
        while (s < textsLen) {
            canvas.drawText(texts.get(s), width / 2, y, paint);
//            canvas.drawText(texts.get(s), 0, y, paint);
            if (s < textsLen - 1) {
                y += TextHeight;
            }
            s++;
        }
        if (ifUp) {
            y -= TextHeight * (textsLen - 1);
        }
        return y;
    }

    private ArrayList<String> autoSplit(String content, Paint p, float width) {
        int length = content.length();
        float textWidth = p.measureText(content);
        ArrayList<String> lineTexts = new ArrayList<String>();
        if (textWidth <= width) {
            lineTexts.add(content);
            return lineTexts;
        }

        int start = 0, end = 1, i = 0;
        // int lines = (int) Math.ceil(textWidth / width);

        while (start < length) {
            if (p.measureText(content, start, end) > width) {
                lineTexts.add((String) content.subSequence(start, end));
                start = end;
            }
            if (end == length) {
                lineTexts.add((String) content.subSequence(start, end));
                break;
            }
            end += 1;
        }
        int len = lineTexts.size();
        for (int j = 0; j < len; j++) {
            if ("".equals(lineTexts.get(j))) {
                lineTexts.remove(lineTexts.get(j));
            }
        }
        return lineTexts;
    }

    private class ResumeScrollTask extends TimerTask {

        /*
         * (non-Javadoc)
         *
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            onmove = false;
        }
    }

    //

    /**
     * @param time
     * @return null
     */
    public void updateIndex(long time) {
        if (mLyric == null)
            return;
        if (onmove) {
            return;
        }
        this.currentTime = time;
        index = mLyric.getNowSentenceIndex(time);
        if (index != -1 && Sentencelist != null && index < Sentencelist.size()) {

            Sentence sen = Sentencelist.get(index);
            sentenctTime = sen.getFromTime();
            currentDunringTime = sen.getDuring();
        }
    }

    public void release() {
        setSentencelist(null);
        mLyric = null;
        if (NotCurrentPaint != null)
            NotCurrentPaint = null;
        if (CurrentPaint != null)
            CurrentPaint = null;
        if (MovingCurrentPaint != null)
            MovingCurrentPaint = null;
        if (MovingLinePaint != null)
            MovingLinePaint = null;
    }

}