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
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;

import android.content.Context;
import com.lewa.player.MusicUtils;
import android.widget.FrameLayout;
import com.lewa.player.model.PlayStatus;
import android.graphics.drawable.AnimationDrawable;

public class LibraryLocalAdapter extends ScanBaseAdapter implements
		SectionIndexer {

	private static final boolean DEBUG = false;
	private static final String TAG = "LibraryArtistAdapter";
	private List<Song> mData = new ArrayList<Song>();
	private Cursor mArtistCurcor;
	private Artist mHeaderArtist;
	private View.OnClickListener mListener;
	private boolean mIsCursor = false;
	private ArtistCursorIndex mCursorIndex;
	private Context context = null;
	SongCollection songCollection = null;

	public LibraryLocalAdapter(View.OnClickListener listener) {
		this.mListener = listener;
	}

	public LibraryLocalAdapter(Context context) {
		this.context = context;
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

	public List<Song> getList() {
		return mData;
	}

	public List<Song> getData() {
		return mData;
	}

	public SongCollection getCollection() {
		return songCollection;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = Lewa.inflater().inflate(R.layout.fragment_local_item,
					null);
			viewHolder = new ViewHolder(convertView);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Song song = null;
		if (mIsCursor) {

		} else {
			song = (Song) mData.get(position);
		}

		long playingSongId = -1;
		PlayStatus ps = Lewa.getPlayStatus();

		if (ps != null) {
			Log.i(TAG, "ps != null");
			Song playingSong = ps.getPlayingSong();

			if (playingSong != null) {
				Log.i(TAG, "playingSong != null");
				Long id = playingSong.getId();
				Log.i(TAG, "id  " + id);
				if (id != null) {
					Log.i(TAG, "id != null");
					playingSongId = playingSong.getId().longValue();
				}
			}
		}

		viewHolder.songNameTv.setText(song.getName());
		long songDuration = song.getDuration() == null ? 0
				: song.getDuration() / 1000;
		String durTime = MusicUtils.makeTimeString(context, songDuration);
		viewHolder.songDurationTv.setText(durTime);
		String artistName = song.getArtist().getName();
		if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
			viewHolder.songInfoTv.setText(context.getResources().getString(
					R.string.unknown_artist_name));
		} else {
			viewHolder.songInfoTv.setText(artistName);
		}
		Log.i(TAG, "song id = " + song.getId());
		Log.i(TAG, "playingSongId = " + playingSongId);
		if (song.getId() != null && song.getId() == playingSongId) {
			viewHolder.nowPlayingView.setVisibility(View.VISIBLE);

			if (null != ps) {
				AnimationDrawable anim = (AnimationDrawable) viewHolder.nowPlayingView
						.getBackground();

				if (ps.isPlaying()) {
					anim.start();
				} else {
					anim.stop();
				}
			}
		} else {
			viewHolder.nowPlayingView.setVisibility(View.INVISIBLE);

		}

		convertView.setTag(R.id.tag_entity, song);
		convertView.setTag(viewHolder);

		return convertView;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return mData.get(position).getInitial().charAt(0);
	}

	public int getStarredCount() {
		return 0;
	}

	public boolean getisNeedHideStarred() {
		return false;
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

	public void setData(Cursor songCursor, Song artist) {

		notifyDataSetChanged();
	}

	public void setData(SongCollection songCollection) {
		mIsCursor = false;
		this.songCollection = songCollection;
		mData = songCollection.getSongs();

		notifyDataSetChanged();
	}

	static class ViewHolder {
		TextView songNameTv;
		TextView songDurationTv;
		TextView songInfoTv;
		ImageView nowPlayingView;

		public ViewHolder(View view) {
			nowPlayingView = (ImageView) view
					.findViewById(R.id.iv_now_playing_icon);
			songNameTv = (TextView) view.findViewById(R.id.tv_title);
			songDurationTv = (TextView) view.findViewById(R.id.tv_duration);
			songInfoTv = (TextView) view.findViewById(R.id.tv_info);
		}
	}

}
