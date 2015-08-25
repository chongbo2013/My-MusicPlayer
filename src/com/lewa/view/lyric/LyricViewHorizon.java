package com.lewa.view.lyric;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.R;

import java.util.ArrayList;
import java.util.List;

public class LyricViewHorizon extends TextView {
    private Paint NotCurrentPaint; // �ǵ�ǰ��ʻ���
    private Paint CurrentPaint; // ��ǰ��ʻ���
    private Paint MovingCurrentPaint;
    private Paint MovingLinePaint;
    private float previousX = 0;
    private float previousY = 0;
    private float moveScroll = 0;
    private long movingCurrentTime = 0;
    private boolean onmove = false;
    private int hrindex;

    private int notCurrentPaintColor = Color.GRAY;// �ǵ�ǰ��ʻ��� ��ɫ
    private int CurrentPaintColor = Color.RED; // ��ǰ��ʻ��� ��ɫ
    private int MovingCurrentPaintColor = Color.WHITE;
    private int MovingLinePaintColor = Color.WHITE;
    private Typeface Texttypeface = Typeface.SERIF;
    private Typeface CurrentTexttypeface = Typeface.DEFAULT_BOLD;
    private float width;
    private static Lyric mLyric;
    private int brackgroundcolor = Color.TRANSPARENT; // ������ɫ
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
    // private Align = Paint.Align.CENTER��

    public float mTouchHistoryY;

    private int height;
    private long currentDunringTime; // ��ǰ�и�ʳ����ʱ�䣬�ø�ʱ����sleep
    // private float middleY;// y���м�
    private int TextHeight = 20; // 每一行的间隔
    private boolean lrcInitDone = false;// �Ƿ��ʼ�������
    public int index = 0;
    private int lastIndex = 0;
    private int sentenceNumber = 0;
    private List<Sentence> Sentencelist; // ����б�

    private long currentTime;

    private long sentenctTime;
    private long toTime;
    

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


    public void setmLyric(Lyric mLyric) {
        LyricViewHorizon.mLyric = mLyric;
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
        if(Sentencelist!=null)
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

    public LyricViewHorizon(Context context, AttributeSet attr) {
        super(context, attr);
        setPram();
        init();
    }

    public LyricViewHorizon(Context context, AttributeSet attr, int i) {
        super(context, attr, i);
        setPram();
        init();
    }

    private void init() {
        setFocusable(true);
        // PlayListItem pli = new PlayListItem("Because Of You",
        // "/sdcard/MP3/Because Of You.mp3", 0L, true);
        // mLyric = new Lyric(new File("/sdcard/MP3/Because Of You.lrc"), pli);

        // �Ǹ�������
        NotCurrentPaint = new Paint();
        NotCurrentPaint.setAntiAlias(true);

        NotCurrentPaint.setTextAlign(Paint.Align.CENTER);

        // �������� ��ǰ���
        CurrentPaint = new Paint();
        CurrentPaint.setAntiAlias(true);
        // CurrentPaint.setColor(CurrentPaintColor);

        CurrentPaint.setTextAlign(Paint.Align.LEFT);
        // list = mLyric.list;
        MovingCurrentPaint = new Paint();
        MovingCurrentPaint.setAntiAlias(true);
        MovingCurrentPaint.setTextAlign(Paint.Align.CENTER);

        this.MovingLinePaint = new Paint();

        MovingLinePaint.setAntiAlias(true);
        MovingLinePaint.setTextAlign(Paint.Align.LEFT);
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Log.e("Update", "onDraw");
        canvas.drawColor(brackgroundcolor);
        CurrentPaint.setColor(CurrentPaintColor);
        CurrentPaint.setTextSize(lrcTextSize);
        CurrentPaint.setTypeface(Texttypeface);
        FontMetrics fm = CurrentPaint.getFontMetrics();
        float baseline = fm.descent - fm.ascent; 
        float x = 0;
        float y =  baseline; 
            String content = "";
            if (index > 0&&index<Sentencelist.size()) {
                content = Sentencelist.get(index).getContent();
            } else {
                content = "   ";
            }
            ArrayList<String> texts = autoSplit(content, CurrentPaint,
                    getWidth() - 50);
//            canvas.drawText(texts.get(0), 0, y,
//                    CurrentPaint);
            if(texts.size()==1){
                canvas.drawText(texts.get(0), 0, y,
                        CurrentPaint);
            }else{

                if(currentDunringTime*2<(toTime+sentenctTime)){
                    canvas.drawText(texts.get(0), 0, y,
                        CurrentPaint);
                    }else{
                        canvas.drawText(texts.get(1), 0, y,
                                CurrentPaint);
                    }
               
//                y+=baseline+fm.leading;
            
            }
        } 

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
            canvas.drawText(texts.get(s), 0, y, paint);
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

    /**
     * �Զ��ָ��ı�
     * 
     * @param content
     *            ��Ҫ�ָ���ı�
     * @param p
     *            ���ʣ����������������ı��Ŀ��
     * @param width
     *            ָ���Ŀ��
     * @return һ���ַ�ArrayList������ÿ�е��ı�
     */
    private ArrayList<String> autoSplit(String content, Paint p, float width) {
        int length = content.length();
        float textWidth = p.measureText(content);
        ArrayList<String> lineTexts = new ArrayList<String>();
        if (textWidth <= width) {
            lineTexts.add(content);
            return lineTexts;
        }

        int start = 0, end = 1, i = 0;
        // int lines = (int) Math.ceil(textWidth / width); //��������

        while (start < length) {
            if (p.measureText(content, start, end) > width) { // �ı���ȳ����ؼ����ʱ
                lineTexts.add((String) content.subSequence(start, end));
                start = end;
            }
            if (end == length) { // ����һ�е��ı�
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

    //
    /**
     * @param time
     *            ��ǰ��ʵ�ʱ����
     * 
     * @return null
     */
    public void updateIndex(long time) {
        if(mLyric==null)
            return;
        if (onmove) {
            return;
        }
        currentTime = time;
        // ������
        index = mLyric.getNowSentenceIndex(time);
        if (index != -1&&Sentencelist!=null&&index<Sentencelist.size()) {

            Sentence sen = Sentencelist.get(index);
            sentenctTime = sen.getFromTime();
            try {
                currentDunringTime = mService.position();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            toTime=sen.getToTime();
//            System.out.println("~~~~~~~~~~~~~setenctTime"+sentenctTime);
//            System.out.println("~~~~~~~~~~~~~currentDunringTime"+currentDunringTime);
//            System.out.println("~~~~~~~~~~~~~toTime"+toTime);
        }
    }
    
    public void release(){
        setSentencelist(null);
        mLyric=null;
        if(NotCurrentPaint!=null)
            NotCurrentPaint=null;
        if(CurrentPaint!=null)
            CurrentPaint=null;
        if(MovingCurrentPaint!=null)
            MovingCurrentPaint=null;
        if(MovingLinePaint!=null)
            MovingLinePaint=null;
    }

}