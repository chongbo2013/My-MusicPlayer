package com.lewa.view.lyric;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.view.MaskEndListView;


public class LyricView extends MaskEndListView {
    private final String TAG = LyricView.class.getName();
    private View endView;

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        endView = Lewa.inflater().inflate(R.layout.v_transpant_footer, null);

        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        endView.setLayoutParams(layoutParams);
        this.addHeaderView(endView);
        this.addFooterView(endView);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int middle = canvas.getHeight() / 2;
        endView.getLayoutParams().height = middle;

        super.dispatchDraw(canvas);
    }

}