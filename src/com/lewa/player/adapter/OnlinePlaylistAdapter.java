package com.lewa.player.adapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.kit.MyVolley;
import com.lewa.player.R;
import com.lewa.player.listener.SimpleImageListener;
import com.lewa.player.model.Playlist;

import java.util.List;

public class OnlinePlaylistAdapter extends ScanBaseAdapter {

    private static final boolean DEBUG = false;
    private static final String TAG = "OnlinePlaylistAdapter";
    private List<Playlist> mData = null;
    private View.OnClickListener mOnClickListener;

    public OnlinePlaylistAdapter(View.OnClickListener clickListener, List<Playlist> data) {
        this.mOnClickListener = clickListener;
        this.mData = data;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mData != null) {
            count = mData.size();
        }
        Log.d(TAG, "Online playlist count: " + count);
        return count;
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
            convertView = Lewa.inflater().inflate(R.layout.item_online_playlist, null);
            viewHolder = new ViewHolder(convertView);
            viewHolder.coverIv = (ImageView) convertView.findViewById(R.id.iv);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Playlist playlist = (Playlist) mData.get(position);
        Log.v(TAG, "Image url: " + playlist.getCoverUrl());
        Log.v(TAG, "Baidu code: " + playlist.getBdCode());
        //new NetImageListener(viewHolder.coverIv, R.drawable.cover, R.drawable.cover, R.string.svg_200, Lewa.resources().getDimensionPixelSize(R.dimen.song_cover_size))
        MyVolley.getImageLoader().get(playlist.getCoverUrl(), new SimpleImageListener(viewHolder.coverIv, R.drawable.cover, R.drawable.cover));
        viewHolder.playlistTv.setText(playlist.getName());
        viewHolder.playBt.setOnClickListener(mOnClickListener);
        viewHolder.playBt.setTag(R.id.tag_entity, playlist);
        convertView.setTag(R.id.tag_entity, playlist);
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

    public void setData(List<Playlist> list) {
        Log.i(TAG, "Set data for online playlist adapter, size: " + (list == null ? 0 : list.size()));
        mData = list;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView coverIv;
        View playBt;
        TextView playlistTv;

        public ViewHolder(View view) {
            coverIv = (ImageView) view.findViewById(R.id.iv);
            playBt = view.findViewById(R.id.bt_play);
            playlistTv = (TextView) view.findViewById(R.id.tv_playlist_name);
        }
    }

}
