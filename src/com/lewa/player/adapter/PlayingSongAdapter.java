package com.lewa.player.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.db.DBService;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Song;
import com.lewa.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import android.graphics.drawable.AnimationDrawable;


public class PlayingSongAdapter extends BaseAdapter {

    private static final boolean DEBUG = false;
    private static final String TAG = "PlayingSongAdapter";
    private List<Song> mData = new ArrayList<Song>();
    private View.OnClickListener listener;
    //private View.OnClickListener mClickListener;

    public PlayingSongAdapter(View.OnClickListener listener) {
        this.listener = listener;
        //mClickListener = new DownloadClickListener();
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

    public List<Song> getList() {
        return mData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = Lewa.inflater().inflate(R.layout.item_playing_song, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        long playingSongId = -1;
        String playingSongName = null;
        PlayStatus ps = Lewa.getPlayStatus();

        if (ps != null) {
            Song playingSong = ps.getPlayingSong();

            if (playingSong != null) {
                Long id = playingSong.getId();
                if (id != null) {
                    playingSongId = playingSong.getId().longValue();
                    playingSongName = playingSong.getName();
                }
            }
        }

        Song song = (Song) mData.get(position);

        if (song != null) {
            if (song.getType() == Song.TYPE.LOCAL) {
                holder.downloadBtn.setVisibility(View.GONE);
            } else {    //online play song
                Song downloadingSong = Lewa.getDownloadingSong().get(song.getId());
                if(null == downloadingSong) {                    
                    holder.downloadBtn.setEnabled(true);
                    holder.downloadBtn.setAlpha(1.0f);
                    //holder.downloadBtn.setOnClickListener(mClickListener);
                } else {    //song is downloading
                    holder.downloadBtn.setAlpha(0.5f);
                    holder.downloadBtn.setEnabled(false);    
                    //holder.downloadBtn.setOnClickListener(null);

                }
                holder.downloadBtn.setVisibility(View.VISIBLE);
                holder.downloadBtn.setTag(R.id.tag_entity, song);
                //holder.downloadBtn.setOnClickListener(mClickListener);
                holder.downloadBtn.setOnClickListener(listener);
            }
            holder.songName.setText(StringUtils.defaultIfBlank(song.getName(), ""));
            if (song.getArtist() != null) {
                holder.artistName.setText(StringUtils.defaultIfBlank(song.getArtist().getName(), ""));
            } else {
                holder.artistName.setText(String.valueOf("--"));
            }

            //pr963151 modify by wjhu
            //if use "||" two songs with same names will show the nowplaying icon
            if (song.getId() != null && (song.getId() == playingSongId && song.getName().equals(playingSongName))) {
                holder.nowPlayingView.setVisibility(View.VISIBLE);

                if(null != ps) {
                    AnimationDrawable anim = (AnimationDrawable) holder.nowPlayingView.getBackground();  

                    if(null != anim) {
                        if(ps.isPlaying()) {
                            anim.start();  
                        } else {
                            anim.stop();  
                        } 
                    }
                }
            } else {
                holder.nowPlayingView.setVisibility(View.GONE);
                //holder.songName.setTextAppearance(convertView.getContext(), R.style.TextView_Middle_White);
            }
        }
        convertView.setTag(R.id.tag_entity, song);
        convertView.setTag(holder);
        return convertView;
    }

    public void setData(List<Song> songs) {
        mData = songs;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView nowPlayingView;
        ImageButton downloadBtn;
        TextView songName;
        TextView artistName;

        public ViewHolder(View view) {
            nowPlayingView = (ImageView) view.findViewById(R.id.iv_now_playing_icon);
            downloadBtn = (ImageButton) view.findViewById(R.id.bt_download);
            songName = (TextView) view.findViewById(R.id.tv_song_name);
            artistName = (TextView) view.findViewById(R.id.tv_artist_name);
        }
    }

    public void songDownloaded() {
        if (mData != null) {
            DBService.matchSongs(mData);
            notifyDataSetChanged();
        }
    }

    private class DownloadClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Song song = (Song) v.getTag(R.id.tag_entity);

            if (v.getId() == R.id.bt_download && song != null) {
                Lewa.downloadSong(song);
                v.setEnabled(false);
                v.setAlpha(0.5f);
            }
        }
    }
}
