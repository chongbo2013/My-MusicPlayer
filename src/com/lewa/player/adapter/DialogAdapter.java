package com.lewa.player.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.model.DialogItem;

import java.util.ArrayList;
import java.util.List;

public class DialogAdapter extends BaseAdapter {

    private static final boolean DEBUG = false;
    private static final String TAG = "DialogAdapter";
    private List<DialogItem> mData = new ArrayList<DialogItem>();

    public DialogAdapter() {
    }

    @Override
    public int getCount() {
        return this.mData != null ? mData.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = Lewa.inflater().inflate(R.layout.item_dialog, null);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DialogItem item = (DialogItem) mData.get(position);
        viewHolder.titleTv.setText(item.getName());

        convertView.setTag(R.id.view_hold, item);
        convertView.setTag(viewHolder);
        return convertView;
    }

    public void setData(List<DialogItem> albums) {
        if (mData == null) {
            mData = new ArrayList<DialogItem>();
        } else {
            mData.clear();
        }

        mData.addAll(albums);
    }

    static class ViewHolder {
        TextView titleTv;

        public ViewHolder(View view) {
            titleTv = (TextView) view.findViewById(R.id.tv_title);
        }
    }

}
