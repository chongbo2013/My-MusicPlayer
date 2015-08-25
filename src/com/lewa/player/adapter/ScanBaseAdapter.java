package com.lewa.player.adapter;

import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Created by Administrator on 13-12-25.
 */
public abstract class ScanBaseAdapter extends BaseAdapter {

    protected boolean isOpeningSubLayout = false;
    protected int openingPosition = -1;

    public void openSubWindow(int openingPosition) {
        this.openingPosition = openingPosition;
        this.isOpeningSubLayout = true;
        notifyDataSetChanged();
    }

    public void reset() {
        this.openingPosition = openingPosition;
        this.isOpeningSubLayout = false;
    }

    public void closeSubWindow() {
        this.isOpeningSubLayout = false;
        notifyDataSetChanged();
    }

}