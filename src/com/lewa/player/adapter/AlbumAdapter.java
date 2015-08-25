package com.lewa.player.adapter;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.db.DBService;
import com.lewa.player.model.Album;
import com.lewa.player.model.AlbumCursorIndex;
import com.lewa.player.model.Playlist;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends ScanBaseAdapter {

    private static final boolean DEBUG = false;
    private static final String TAG = "AlbumAdapter";
    private List<Album> mData = new ArrayList<Album>();
    private Cursor mAlbumCurcor;
    private Album mHeaderAlbum;
    private boolean mIsCursor = false;
    private View.OnClickListener mClickListener;
    private AlbumCursorIndex mAlbumCursorIndex = null;

    public AlbumAdapter(View.OnClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    @Override
    public int getCount() {
        if (mIsCursor) {
            int count = mHeaderAlbum != null ? mAlbumCurcor.getCount() + 1 : mAlbumCurcor.getCount();
            return count;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = Lewa.inflater().inflate(R.layout.item_album, null);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Album album = null;
        if (mIsCursor) {
            if (position == 0 && mHeaderAlbum != null) {
                album = mHeaderAlbum;
                try {
                    Playlist allPlaylist = DBService.findPlaylist(Playlist.ALL_ID);
                    album.setArt(allPlaylist.getCoverUrl());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                int pos = mHeaderAlbum != null ? position - 1 : position;
                mAlbumCurcor.moveToPosition(pos);

                album = Album.fromCursor(mAlbumCurcor, mAlbumCursorIndex);
            }
        } else {
            album = (Album) mData.get(position);
        }
        //TODO display image here
//        ImageLoader.getInstance().displayImage(album.getArt(), viewHolder.coverIv, Lewa.middleDIOS());
        Bitmap bm = Lewa.getLocalImage(album.getArt());

        if (bm != null) {
            viewHolder.coverIv.setImageBitmap(bm);
        } else {
            viewHolder.coverIv.setImageResource(R.drawable.cover);
        }
        String albumName = album.getName();
		if (MediaStore.UNKNOWN_STRING.equals(albumName)) {
			viewHolder.titleTv.setText(parent.getResources().getString(
					R.string.unknown_album_name));
		} else {
			viewHolder.titleTv.setText(albumName);
		}
        int songNum = album.getSongNum() == null ? 0 : album.getSongNum();
        if (album.getId() > 0) {
            //songNum = DBService.loadSongCountOfAlbum(album.getId()); //del by sjxu for bug 46839, num is provide by ArtistAlbumFragment onResume() 
        }
        viewHolder.songNumTv.setText(Lewa.string(R.string.text_song_num, songNum));

        viewHolder.playBt.setOnClickListener(mClickListener);
        viewHolder.playBt.setTag(R.id.tag_entity, album);
        convertView.setTag(R.id.tag_entity, album);
        convertView.setTag(viewHolder);

        viewHolder.playBt.setTag(R.id.tag_entity, album);
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

    public void setData(Cursor albumCursor, Album album) {
        mIsCursor = true;
        mAlbumCurcor = albumCursor;
        mAlbumCursorIndex = new AlbumCursorIndex(mAlbumCurcor);

        mHeaderAlbum = album;
        notifyDataSetChanged();
    }

    public void setData(List<Album> albums) {
        mIsCursor = false;
        mData = albums;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView coverIv;
        View playBt;
        TextView titleTv;
        TextView songNumTv;

        public ViewHolder(View view) {
            coverIv = (ImageView) view.findViewById(R.id.iv);
            playBt = view.findViewById(R.id.bt_play);
            titleTv = (TextView) view.findViewById(R.id.tv_album_name);
            songNumTv = (TextView) view.findViewById(R.id.tv_number);
        }
    }

}
