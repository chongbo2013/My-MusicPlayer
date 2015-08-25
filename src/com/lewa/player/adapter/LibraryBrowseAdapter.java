package com.lewa.player.adapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.kit.MyVolley;
import com.lewa.player.R;
import com.lewa.player.listener.SimpleImageListener;
import com.lewa.player.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class LibraryBrowseAdapter extends BaseAdapter {

    private static final boolean DEBUG = false;
    private static final String TAG = "LibraryBrowseAdapter";
    private List<Playlist> mData = new ArrayList<Playlist>();
    private View.OnClickListener listener;

    public LibraryBrowseAdapter(View.OnClickListener listener, List<Playlist> data) {
        this.listener = listener;
        this.mData = data;
    }

    @Override
    public int getCount() {
        int count = 0;

        if (mData != null) {
            count = mData.size();
        } else {
            Log.i(TAG, "list is null");
        }
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
        final Playlist playlist = mData.get(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            if (playlist.getType() != Playlist.TYPE.FM) {
                viewHolder = new ViewHolder();
                convertView = Lewa.inflater().inflate(R.layout.item_browse_with_border, null);
            } else {
                viewHolder = new ViewHolder();
                convertView = Lewa.inflater().inflate(R.layout.item_browse, null);
            }

            viewHolder.iv = (ImageView) convertView.findViewById(R.id.iv);
            viewHolder.titleTv = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.button = convertView.findViewById(R.id.button);
        } else {
            Playlist convertPlaylist = (Playlist) convertView.getTag(R.id.tag_entity);

            if ((convertPlaylist.getType() == Playlist.TYPE.FM && playlist.getType() == Playlist.TYPE.FM) || (
                    convertPlaylist.getType() != Playlist.TYPE.FM && playlist.getType() != Playlist.TYPE.FM
            )) {
                viewHolder = (ViewHolder) convertView.getTag();
            } else {
                if (playlist.getType() != Playlist.TYPE.FM) {
                    viewHolder = new ViewHolder();
                    convertView = Lewa.inflater().inflate(R.layout.item_browse_with_border, null);
                } else {
                    viewHolder = new ViewHolder();
                    convertView = Lewa.inflater().inflate(R.layout.item_browse, null);
                }

                viewHolder.iv = (ImageView) convertView.findViewById(R.id.iv);
                viewHolder.titleTv = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.button = convertView.findViewById(R.id.button);
            }
        }

        viewHolder.titleTv.setText(playlist.getName());
        Log.v(TAG, "Image url: " + playlist.getCoverUrl());

        if (playlist.getCoverUrl() != null
			&& playlist.getType() != Playlist.TYPE.ONLINE_CATEGORY 
			&& playlist.getType() != Playlist.TYPE.TOP_LIST_CATEGORY
			&& playlist.getType() != Playlist.TYPE.ALL_STAR_CATEGORY) {
            MyVolley.getImageLoader().get(playlist.getCoverUrl(), new SimpleImageListener(viewHolder.iv, R.drawable.cover, R.drawable.cover));
        }

		if(playlist.getType() == Playlist.TYPE.ONLINE_CATEGORY) {
			viewHolder.iv.setImageResource(R.drawable.online_category);
		} else if(playlist.getType() == Playlist.TYPE.TOP_LIST_CATEGORY) {
			viewHolder.iv.setImageResource(R.drawable.online_top_list);
		} else if(playlist.getType() == Playlist.TYPE.ALL_STAR_CATEGORY) {
			viewHolder.iv.setImageResource(R.drawable.online_all_star);
		}

        viewHolder.button.setTag(R.id.tag_entity, playlist);
        viewHolder.button.setOnClickListener(listener);
        //pr942250 add by wjhu begin
        //add onclick listen for imageview
        if (playlist.getType() == Playlist.TYPE.FM) {
        	viewHolder.iv.setTag(R.id.tag_entity, playlist);
        	viewHolder.iv.setOnClickListener(listener);
        }
        //pr942250 add by wjhu end
        convertView.setTag(R.id.tag_entity, playlist);
        convertView.setTag(viewHolder);

        return convertView;
    }

    public void setData(List<Playlist> playlists) {
        Log.i(TAG, "Set playlists: " + playlists.size());
        mData.addAll(playlists);
        notifyDataSetChanged();
    }

    public void getData() {
        Log.i(TAG, "Get playlists: " + mData.size());
    }

    private static class ViewHolder {
        public View button;
        public ImageView iv;
        public TextView titleTv;
    }
}
