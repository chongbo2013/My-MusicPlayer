package com.lewa.player.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.listener.EditPlaylistListener;
import com.lewa.player.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongPickAdapter extends BaseAdapter implements View.OnClickListener,OnItemClickListener {
    private static final String TAG = "SongPickAdapter";
    private List<Song> mData = new ArrayList<Song>();
    private EditPlaylistListener mEditPlaylistListener;

    public SongPickAdapter(EditPlaylistListener editPlaylistListener) {
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
        ViewHolder holder;
        if (convertView == null) {
            convertView = Lewa.inflater().inflate(R.layout.item_song_pick, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Song song = (Song) mData.get(position);
        holder.songNameTv.setText(song.getName());

        if (song.getArtist() != null) {
            holder.artistNameTv.setText(song.getArtist().getName());
        }

        holder.songCb.setVisibility(View.VISIBLE);
        if (mEditPlaylistListener.isSongPicked(song)) {
            holder.songCb.setChecked(true);
        } else {
            holder.songCb.setChecked(false);
        }

        holder.songCb.setTag(R.id.tag_entity, song);
        holder.songCb.setOnClickListener(this);
        convertView.setTag(R.id.tag_entity, song);
        convertView.setTag(holder);
        return convertView;
    }

    public void setData(List<Song> songs) {
        mData = songs;
        notifyDataSetChanged();

    }

    public List<Song> getData() {
        return mData;
    }

    @Override
    public void onClick(View v) {
        Song song = (Song) v.getTag(R.id.tag_entity);
        mEditPlaylistListener.toggleSong(song);
    }

    static class ViewHolder {
        TextView songNameTv;
        TextView artistNameTv;
        CheckBox songCb;

        public ViewHolder(View view) {
            songNameTv = (TextView) view.findViewById(R.id.tv_song_name);
            artistNameTv = (TextView) view.findViewById(R.id.tv_artist_name);
            songCb = (CheckBox) view.findViewById(R.id.cb_select);
        }
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		CheckBox songCb = (CheckBox) view.findViewById(R.id.cb_select);
		if(null != songCb) {
			songCb.setChecked(!songCb.isChecked());
		}
		Song song = (Song) view.getTag(R.id.tag_entity);
	    mEditPlaylistListener.toggleSong(song);
	}

}
