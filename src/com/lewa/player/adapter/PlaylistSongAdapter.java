package com.lewa.player.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.listener.EditPlaylistListener;
import com.lewa.player.model.Song;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlaylistSongAdapter extends BaseAdapter {

    private static final boolean DEBUG = false;
    private static final String TAG = "PlaylistSongAdapter";
    private List<Song> mData = new ArrayList<Song>();
    private View.OnClickListener mListener;
    private EditPlaylistListener mEditPlaylistListener;

    public PlaylistSongAdapter(View.OnClickListener listener, EditPlaylistListener editPlaylistListener) {
        this.mListener = listener;
        this.mEditPlaylistListener = editPlaylistListener;
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
            convertView = Lewa.inflater().inflate(R.layout.item_song_pick, null);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Song song = (Song) mData.get(position);
        viewHolder.songNameTv.setText(song.getName());

        if (song.getArtist().getName() != null) {
            viewHolder.artistNameTv.setText(song.getArtist().getName());
        }
        if(mEditPlaylistListener.getSelectedSong().contains(song)) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
        }
        viewHolder.checkBox.setVisibility(View.VISIBLE);
        viewHolder.checkBox.setTag(R.id.tag_entity, song);
        viewHolder.checkBox.setOnClickListener(mListener);
        convertView.setTag(R.id.tag_entity, song);
        convertView.setTag(viewHolder);
        return convertView;
    }

    public void setData(Collection<Song> songs) {
        mData.clear();
        if (songs != null) {
            mData.addAll(songs);
        }

        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView songNameTv;
        TextView artistNameTv;
        CheckBox checkBox;

        public ViewHolder(View view) {
            songNameTv = (TextView) view.findViewById(R.id.tv_song_name);
            artistNameTv = (TextView) view.findViewById(R.id.tv_artist_name);
            checkBox = (CheckBox) view.findViewById(R.id.cb_select);
        }
    }
}
