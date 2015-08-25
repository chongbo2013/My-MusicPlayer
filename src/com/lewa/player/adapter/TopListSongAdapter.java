package com.lewa.player.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;

import java.util.ArrayList;
import java.util.List;

public class TopListSongAdapter extends BaseAdapter {

    private static final boolean DEBUG = false;
    private static final String TAG = "TopListSongAdapter";
    private List<Song> mData = new ArrayList<Song>();
    private SongCollection mSongCollection;

    public TopListSongAdapter() {
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
            convertView = Lewa.inflater().inflate(R.layout.item_top_list_song, null);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Song song = (Song) mData.get(position);
        song.setCollection(mSongCollection);
        viewHolder.topListTv.setText((position + 1) + "");
        if (song.getType() == Song.TYPE.LOCAL) {
            viewHolder.downloadBtn.setVisibility(View.GONE);
        }
        viewHolder.songNameTv.setText(song.getName());
        String artist = song.getArtist() == null ? "-" : song.getArtist().getName();
        viewHolder.artistNameTv.setText(artist);
        convertView.setTag(R.id.tag_entity, song);
        convertView.setTag(viewHolder);

        return convertView;
    }

    public void setData(List<Song> songs, SongCollection songCollection) {
        mData = songs;
        this.mSongCollection = songCollection;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageButton downloadBtn;
        TextView songNameTv;
        TextView topListTv;
        TextView artistNameTv;

        public ViewHolder(View view) {
            downloadBtn = (ImageButton) view.findViewById(R.id.bt_download);
            songNameTv = (TextView) view.findViewById(R.id.tv_song_name);
            topListTv = (TextView) view.findViewById(R.id.tv_top_list);
            artistNameTv = (TextView) view.findViewById(R.id.tv_artist_name);
        }
    }
}
