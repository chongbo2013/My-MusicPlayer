package com.lewa.player.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.db.DBService;
import com.lewa.player.model.Playlist;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibraryMineAdapter extends ScanBaseAdapter {

    private static final boolean DEBUG = false;
    private static final String TAG = "LibraryMineAdapter";
    private List<Playlist> mData = new ArrayList<Playlist>();
    private View.OnClickListener mClickListener;

    public LibraryMineAdapter(View.OnClickListener clickListener) {
        this.mClickListener = clickListener;
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
    public View getView(int position, View convertView, ViewGroup convertViewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = Lewa.inflater().inflate(R.layout.item_mine, null);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Playlist playlist = (Playlist) mData.get(position);
        if (playlist.getType() == Playlist.TYPE.LOCAL) {
            viewHolder.editBtn.setVisibility(View.VISIBLE);
        } else {
            viewHolder.editBtn.setVisibility(View.GONE);
        }

        /*if (position == 0) {
            try {
                Playlist allPlaylist = DBService.findPlaylist(Playlist.ALL_ID);
                playlist.setId(Playlist.ALL_ID);
                playlist.setCoverUrl(allPlaylist.getCoverUrl());
                if(playlist.getSongNum()>0){
                	LewaUtils.logE(TAG, "songNum: "+playlist.getSongNum());
                	MusicUtils.mHasSongs=true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }*/

        if (playlist.getId() != null && playlist.getId() > 0 && playlist.getCoverUrl() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(playlist.getCoverUrl());

            if (bitmap != null) {
                viewHolder.picView.setImageBitmap(bitmap);
            } else {
                viewHolder.picView.setImageResource(R.drawable.cover);
            }
        } else {
            viewHolder.picView.setImageResource(R.drawable.cover);
        }
        //pr954268 modify by wjhu begin
        String name = playlist.getName();
        if ("我的收藏".equals(name)) {
        	name = Lewa.string(R.string.mine_my_favorite);
        } else if ("最近播放".equals(name)) {
        	name = Lewa.string(R.string.mine_recent_play);
        } else if ("我的下载".equals(name)) {
        	name = Lewa.string(R.string.mine_my_download);
		}
        viewHolder.titleTv.setText(name);
        //pr954268 modify by wjhu end
        int songNum = playlist.getSongNum();
        viewHolder.hint_iv.setVisibility(View.GONE);
        if(playlist.getType() == Playlist.TYPE.RECENT_PLAY) {
            if(playlist.getSongNum() > 30) {
                songNum = 30;
            }
        }else if(playlist.getType() == Playlist.TYPE.DOWNLOAD){
        	if(Constants.show_down_tip)
        		viewHolder.hint_iv.setVisibility(View.VISIBLE);
        }
        viewHolder.songNumTv.setText(songNum + Lewa.string(R.string.song_number_postfix));

        viewHolder.editBtn.setOnClickListener(mClickListener);
        viewHolder.editBtn.setTag(R.id.tag_entity, playlist);
        viewHolder.playBt.setOnClickListener(mClickListener);
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

    public void setData(List<Playlist> playlists) {
        if (playlists == null) {
            return;
        }

        mData = playlists;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView picView;
        ImageButton editBtn;
        TextView titleTv;
        TextView songNumTv;
        View playBt;
        ImageView hint_iv;

        public ViewHolder(View view) {
            picView = (ImageView) view.findViewById(R.id.iv);
            editBtn = (ImageButton) view.findViewById(R.id.bt_edit_playlist);
            titleTv = (TextView) view.findViewById(R.id.tv_title);
            songNumTv = (TextView) view.findViewById(R.id.tv_number);
            playBt = view.findViewById(R.id.bt_play);
            hint_iv = (ImageView) view.findViewById(R.id.hint_iv);
        }
    }

}
