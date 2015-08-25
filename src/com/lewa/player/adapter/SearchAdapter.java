package com.lewa.player.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.model.Song;
import com.lewa.player.model.Song.TYPE;
import com.lewa.util.LewaUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String TAG = "SongPickAdapter";
    private String filter;
    private List<Song> mData = new ArrayList<Song>();
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Song song = (Song) v.getTag(R.id.tag_entity);
            if (song != null && song.getId() != null) {
                Lewa.downloadSong(song);
            }
        }
    };
    public SearchAdapter() {
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
            convertView = Lewa.inflater().inflate(R.layout.item_search, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Song song = (Song) mData.get(position);
        String song_name=song.getName();
        if(!TextUtils.isEmpty(song_name))
        	holder.songNameTv.setText(LewaUtils.highlight(Lewa.context(),song_name, filter));

        if (song.getArtist() != null) {
        	String artist_name=song.getArtist().getName();
        	if(!TextUtils.isEmpty(artist_name)){
        		String album_name = null;
        		if(song.getAlbum()!=null)
        			album_name=song.getAlbum().getName();
        		if(!TextUtils.isEmpty(album_name))
        			artist_name=album_name.concat("-").concat(artist_name);
        		holder.artistNameTv.setText(LewaUtils.highlight(Lewa.context(),artist_name, filter));
        	}
        }
        
        if(song.getType()==Song.TYPE.LOCAL){
        	holder.downloadBtn.setVisibility(View.GONE);
        }else{
        	 holder.downloadBtn.setVisibility(View.VISIBLE);
             holder.downloadBtn.setTag(R.id.tag_entity, song);
             holder.downloadBtn.setOnClickListener(mOnClickListener);
        }

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
    }

    static class ViewHolder {
        TextView songNameTv;
        TextView artistNameTv;
        ImageButton downloadBtn;
        public ViewHolder(View view) {
            songNameTv = (TextView) view.findViewById(R.id.tv_song_name);
            artistNameTv = (TextView) view.findViewById(R.id.tv_artist_name);
            downloadBtn = (ImageButton) view.findViewById(R.id.bt_download);
        }
    }

	public void setFilter(String filter) {
		this.filter = filter;
	}
    
    
}
