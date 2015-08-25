package com.lewa.player.adapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.lewa.Lewa;
import com.lewa.kit.MyVolley;
import com.lewa.player.R;
import com.lewa.player.model.Artist;
import com.lewa.player.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class AllStarAdapter extends ScanBaseAdapter implements SectionIndexer {

    private static final boolean DEBUG = false;
    private static final String TAG = "LibraryArtistAdapter";
    private List<Playlist> mData = new ArrayList<Playlist>();
    private View.OnClickListener mListener;

    public AllStarAdapter(View.OnClickListener listener, List<Playlist> playlists) {
        this.mListener = listener;
        this.mData = playlists;
    }

    @Override
    public int getCount() {
        Log.i(TAG, "Get artist item count.");
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
            convertView = Lewa.inflater().inflate(R.layout.library_common_item, null);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Playlist playlist = (Playlist) mData.get(position);
        Artist artist = playlist.getArtist();

        if (artist.getId() > 0) {
            if (artist.getPicPath() != null && !"".equals(artist.getPicPath())) {
                MyVolley.getImageLoader().get(artist.getPicPath(), ImageLoader.getImageListener(viewHolder.avatarIv, R.drawable.cover, R.drawable.cover));
            } else {
                viewHolder.avatarIv.setImageResource(R.drawable.cover);
            }
        } else {
            viewHolder.avatarIv.setImageResource(R.drawable.bg_cdcover);
        }
        viewHolder.artistNameTv.setText(artist.getName());
        int songNum = artist.getSongNum() == null ? 0 : artist.getSongNum();
        viewHolder.songNumTv.setText(Lewa.string(R.string.text_song_num, songNum));
        viewHolder.playBt.setTag(R.id.tag_entity, playlist);
        viewHolder.playBt.setOnClickListener(mListener);
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

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return mData.get(position).getArtist().getInitial().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = mData.get(i).getArtist().getInitial();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    static class ViewHolder {
        ImageView avatarIv;
        View playBt;
        TextView artistNameTv;
        TextView songNumTv;

        public ViewHolder(View view) {
            avatarIv = (ImageView) view.findViewById(R.id.iv);
            playBt = view.findViewById(R.id.bt_play);
            artistNameTv = (TextView) view.findViewById(R.id.tv_title);
            songNumTv = (TextView) view.findViewById(R.id.tv_number);
        }
    }
}
