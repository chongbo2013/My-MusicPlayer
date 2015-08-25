package com.lewa.player.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class TopListAdapter extends ScanBaseAdapter {

    private static final boolean DEBUG = false;
    private static final String TAG = "TopListAdapter";
    private List<Playlist> mData = new ArrayList<Playlist>();
    private View.OnClickListener listener;

    public TopListAdapter(View.OnClickListener listener) {
        this.listener = listener;
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

    public List<Playlist> getList() {
        return mData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = Lewa.inflater().inflate(R.layout.item_top_list, null);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Playlist topList = (Playlist) mData.get(position);
        //TODO display image here
//        ImageLoader.getInstance().displayImage(topList.getCoverUri(), viewHolder.coverIv, Lewa.middleDIOS());
        viewHolder.topListNameTv.setText(topList.getName());
//        viewHolder.updateTimeTv.setText("更新日期：" + DateUtils.y4M2d2S(topList.getUpdateTime()));
        viewHolder.playBt.setOnClickListener(listener);
        viewHolder.playBt.setTag(R.id.tag_entity, topList);
        convertView.setTag(R.id.tag_entity, topList);
        convertView.setTag(viewHolder);
        if (isOpeningSubLayout) {
            if (position == openingPosition) {
                viewHolder.playBt.setVisibility(View.VISIBLE);
            } else {
                viewHolder.playBt.setVisibility(View.GONE);
            }
        } else {
            viewHolder.playBt.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public void setData(List<Playlist> topLists) {
        mData = topLists;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView coverIv;
        View playBt;
        TextView topListNameTv;
        TextView updateTimeTv;

        public ViewHolder(View view) {
            coverIv = (ImageView) view.findViewById(R.id.iv);
            playBt = view.findViewById(R.id.bt_play);
            topListNameTv = (TextView) view.findViewById(R.id.tv_top_list_name);
            updateTimeTv = (TextView) view.findViewById(R.id.tv_update_time);
        }
    }

}
