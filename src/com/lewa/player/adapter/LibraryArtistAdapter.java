package com.lewa.player.adapter;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.db.DBService;
import com.lewa.player.model.Artist;
import com.lewa.player.model.ArtistCursorIndex;
import com.lewa.player.model.Playlist;
import com.lewa.player.online.DownLoadAsync;
import com.lewa.util.LewaUtils;
import com.lewa.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibraryArtistAdapter extends ScanBaseAdapter implements SectionIndexer {

    private static final boolean DEBUG = false;
    private static final String TAG = "LibraryArtistAdapter";
    private List<Artist> mData = new ArrayList<Artist>();
    private Cursor mArtistCurcor;
    private Artist mHeaderArtist;
    private View.OnClickListener mListener;
    private boolean mIsCursor = false;
    private ArtistCursorIndex mCursorIndex;

    public LibraryArtistAdapter(View.OnClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getCount() {
        Log.i(TAG, "Get artist item count.");
        if (mIsCursor) {
            return mArtistCurcor.getCount() + 1;
        }
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

    public List<Artist> getList() {
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
        Artist artist = null;
        if (mIsCursor) {
            if (position == 0) {
                artist = mHeaderArtist;
                try {
                    Playlist allPlaylist = DBService.findPlaylist(Playlist.ALL_ID);
                    artist.setPicPath(allPlaylist.getCoverUrl());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                mArtistCurcor.moveToPosition(position - 1);
                artist = Artist.fromCursor(mArtistCurcor, mCursorIndex);
            }
        } else {
            artist = (Artist) mData.get(position);
        }
//        if (artist.getId() > 0) {
//            //TODO display image here
//            if (artist.getPicPath() != null && !"".equals(artist.getPicPath())) {
//                MyVolley.getImageLoader().get(artist.getPicPath(), ImageLoader.getImageListener(viewHolder.avatarIv, R.drawable.cover, R.drawable.cover));
//            } else {
//                viewHolder.avatarIv.setImageResource(R.drawable.cover);
//            }
//        } else {
//            viewHolder.avatarIv.setImageResource(R.drawable.bg_cdcover);
//        }
        String path = artist.getPicPath();

        if (StringUtils.isBlank(path)) {
            path = LewaUtils.getArtistPicPath(artist.getName());
        }
        Bitmap avatar = Lewa.getLocalImage(path);

        if (avatar == null) {
            viewHolder.avatarIv.setImageResource(R.drawable.cover);
        } else {
            viewHolder.avatarIv.setImageBitmap(avatar);
        }

        String artistName = artist.getName();
		if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
			viewHolder.artistNameTv.setText(parent.getResources().getString(
					R.string.unknown_artist_name));
		} else {
			viewHolder.artistNameTv.setText(artistName);
		}
        int ablumNum = artist.getAlbumNum()== null ? 0 : artist.getAlbumNum();
       
            
        viewHolder.ablumNumTv.setText(Lewa.string(R.string.text_album_num, ablumNum));
        
        int songNum = artist.getSongNum() == null ? 0 : artist.getSongNum();
        viewHolder.songNumTv.setVisibility(View.VISIBLE);
        viewHolder.songNumTv.setText(Lewa.string(R.string.text_song_num, songNum));
        viewHolder.playBt.setTag(R.id.tag_entity, artist);
        viewHolder.playBt.setOnClickListener(mListener);
        convertView.setTag(R.id.tag_entity, artist);
        convertView.setTag(viewHolder);
        /*if (isOpeningSubLayout) {
            if (position == openingPosition) {
                viewHolder.playBt.setVisibility(View.VISIBLE);
            } else {
                viewHolder.playBt.setVisibility(View.GONE);
            }
        } else {
            if (position == 0 && artist.getId() == 0) {
                viewHolder.playBt.setVisibility(View.VISIBLE);
            } else {
                viewHolder.playBt.setVisibility(View.GONE);
            }
        }*/
        viewHolder.playBt.setVisibility(View.VISIBLE);
        return convertView;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return mData.get(position).getInitial().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = mData.get(i).getInitial();
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

    public int getStarredCount() {
        return 0;
    }

    public boolean getisNeedHideStarred() {
        return false;
    }

    public void setData(Cursor artistCursor, Artist artist) {
        mIsCursor = true;
        mArtistCurcor = artistCursor;
        if (artistCursor != null) {
            mCursorIndex = new ArtistCursorIndex(artistCursor);
        }

        mHeaderArtist = artist;
        notifyDataSetChanged();
    }

    public void setData(List<Artist> artists) {
        mIsCursor = false;
        mData = artists;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView avatarIv;
        View playBt;
        TextView artistNameTv;
        TextView songNumTv;
        TextView ablumNumTv;

        public ViewHolder(View view) {
            avatarIv = (ImageView) view.findViewById(R.id.iv);
            playBt = view.findViewById(R.id.bt_play);
            artistNameTv = (TextView) view.findViewById(R.id.tv_title);
            ablumNumTv = (TextView) view.findViewById(R.id.tv_number);
            songNumTv = (TextView) view.findViewById(R.id.tv_sub_number);
        }
    }

}
