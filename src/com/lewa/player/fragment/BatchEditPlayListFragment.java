package com.lewa.player.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.activity.EditPlaylistActivity;
import com.lewa.player.adapter.DialogAdapter;
import com.lewa.player.adapter.SongPickAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.listener.EditPlaylistListener;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.listener.PlayStatusBackgroundListener;
import com.lewa.player.model.Album;
import com.lewa.player.model.Artist;
import com.lewa.player.model.DialogItem;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.PlaylistSong;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.lewa.player.activity.LibraryActivity;

/**
 * Created by wuzixiu on 12/14/13.
 */
public class BatchEditPlayListFragment extends BaseFragment implements View.OnFocusChangeListener, EditPlaylistListener, View.OnClickListener {
    private static final String TAG = "EditPlaylistFragment";//.class.getName();
    public static final String ARG_SONG_ID = "songId";
    public final static String EDIT_SONG_INTENT_KEY = "songData";

    ImageButton mBackBtn;
    ImageButton mSelectAllBtn;
    ImageButton mAddToBtn;
    ImageButton mFavoriteBtn;
    ImageButton mRemoveBtn;
    ImageView mCoverIv;
    TextView mTitleTv;
    LinearLayout mSaveBtn;
    ListView mSongLv;

    private LibraryListener mLibraryListener;
    private String mCheckedSongNumber;
    private SongPickAdapter mSongPickAdapter;
    private boolean mIsSelectAll = false;
    private List<Song> mTmpPickedSongs = new ArrayList<Song>();
    private List<Song> mSongs;
    private SongCollection mCollection;
    private Toast mToast;


    public static BatchEditPlayListFragment newInstance(Long songId) {
        BatchEditPlayListFragment pickSongFragment = new BatchEditPlayListFragment();
        Bundle args = new Bundle();
        if (songId != null) {
            args.putLong(ARG_SONG_ID, songId);
        }
        pickSongFragment.setArguments(args);
        return pickSongFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_batch_pick_song, container, false);
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mBackBtn.setOnClickListener(this);
        mSelectAllBtn = (ImageButton) rootView.findViewById(R.id.bt_select_all);
        mSelectAllBtn.setOnClickListener(this);
        mAddToBtn = (ImageButton) rootView.findViewById(R.id.bt_add_to);
        mRemoveBtn = (ImageButton) rootView.findViewById(R.id.bt_remove);
        mFavoriteBtn = (ImageButton) rootView.findViewById(R.id.bt_favorite);
        mAddToBtn.setOnClickListener(this);
        mTitleTv = (TextView) rootView.findViewById(R.id.tv_title);
        mSaveBtn = (LinearLayout) rootView.findViewById(R.id.bt_add_song);
        mSongLv = (ListView) rootView.findViewById(R.id.lv_song);
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);

        mSelectAllBtn.setOnClickListener(this);
        mFavoriteBtn.setOnClickListener(this);
        mRemoveBtn.setOnClickListener(this);
        mAddToBtn.setOnClickListener(this);
        mSongPickAdapter = new SongPickAdapter(this);
        mSongLv.setAdapter(mSongPickAdapter);
        mSongLv.setOnItemClickListener(mSongPickAdapter);

        mCheckedSongNumber = String.format(getResources().getString(R.string.check_song_number), 0);
        mTitleTv.setText(mCheckedSongNumber);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mLibraryListener = (LibraryListener) activity;
        } catch (ClassCastException cce) {
            Log.e(TAG, "Activity should implement EditPlaylistListener.");
        }
    }

    @Override
    public void onResume() {
        mPlayStatusListener = new PlayStatusBackgroundListener(BatchEditPlayListFragment.class.getName(), mCoverIv);
        super.onResume();
        Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);

        mCollection = mLibraryListener.getSongCollection();
        int position = 0;
        if (getArguments().getLong(ARG_SONG_ID) > 0) {
            mSongs = new ArrayList<Song>();
            Song song = DBService.findSongFromMediaStore(getArguments().getLong(ARG_SONG_ID));
            if (song != null) {
                mSongs.add(song);
            }
            Bundle arg = new Bundle();
            arg.putLong(ARG_SONG_ID, 0);
            setArguments(arg);
            mTmpPickedSongs = mSongs;
        } else {
            Song longClickSong = mLibraryListener.getLongClickSong();
            if (null != longClickSong && !getPickedSongs().contains(longClickSong)) {
                getPickedSongs().add(longClickSong);
            }
			try {
            	((LibraryActivity)getActivity()).setLongClickSong(null);   
			} catch (Exception e) {
				e.printStackTrace();
			}    
            
            if (mCollection != null) {
                mSongs = mCollection.getSongs();
                if(null != mSongs) {
                	position = mSongs.indexOf(longClickSong);
                }
                switch (mCollection.getType()) {
                    case PLAYLIST:
                        Playlist playlist = (Playlist) mCollection.getOwner();
                        if (playlist != null) {
                            switch (playlist.getType()) {
                                case ONLINE:
                                case ALL_STAR:
                                case TOP_LIST_NEW:
                                case TOP_LIST_HOT:
                                    //ONLINE
                                    setOnlineList();
                                    break;
                            }
                        }
                        break;
                    case SINGLE:
                    	setOnlineList();
                    	break;
                }
            }
        }
        mSongPickAdapter.setData(mSongs);
        mSongLv.setSelection(position);
        setDynamicView();
    }

    private void setOnlineList() {
        //ONLINE
        mRemoveBtn.setVisibility(View.GONE);
        //mFavoriteBtn.setBackgroundResource(R.drawable.se_bg_action_menu_item_right);
    }

    //pr962459 add by wjhu begin
    //to avoid show toast for too long
    private void showToast(String text) {
		if (mToast == null) {
			mToast = Toast.makeText(getActivity(), text,
					Toast.LENGTH_SHORT);
		} else {
			mToast.setText(text);
		}
		mToast.show();
	}
    //pr962459 add by wjhu end
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View view) {
        AlertDialog.Builder builder;
        switch (view.getId()) {
            case R.id.bt_add_to:
                if (mTmpPickedSongs.size() == 0) {
                    Toast.makeText(getActivity(), R.string.no_selected_song, Toast.LENGTH_SHORT).show();
                    break;
                }
                openAddToDialog();
                break;
            case R.id.bt_select_all:
                toggleAll();
                break;
            case R.id.bt_download:
                //TODO
                break;
            case R.id.bt_share:
                //TODO
                break;
            case R.id.bt_as_bell:
                Collection<Song> songs = getPickedSongs();
                if (songs != null && songs.size() == 1) {
                    Song song = songs.iterator().next();
                    File sdfile = new File(song.getPath());
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
                    values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                    values.put(MediaStore.Audio.Media.IS_ALARM, false);
                    values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                    Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
                    Uri newUri = getActivity().getContentResolver().insert(uri, values);
                    RingtoneManager.setActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_RINGTONE, newUri);
                    Toast.makeText(getActivity(), "设置来电铃声成功！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_edit:
                if (getPickedSongs() != null && getPickedSongs().size() == 1) {
                    final Song song = getPickedSongs().iterator().next();
                    View editView = Lewa.inflater().inflate(R.layout.v_edit_song, null);
                    final EditText songNameEt = (EditText) editView.findViewById(R.id.et_song_name);
                    final EditText artistNameEt = (EditText) editView.findViewById(R.id.et_artist_name);
                    final EditText albumNameEt = (EditText) editView.findViewById(R.id.et_album_name);
                    songNameEt.setText(song.getName());
                    if (song.getArtist() != null) {
                        artistNameEt.setText(song.getArtist().getName());
                    }
                    if (song.getAlbum() != null) {
                        albumNameEt.setText(song.getAlbum().getName());
                    }
                    AlertDialog.Builder mEditBuilder = new AlertDialog.Builder(this.getActivity());
                    mEditBuilder.setTitle(R.string.edit_song_text)
                            .setView(editView)
                            .setPositiveButton(getResources().getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        song.setName(songNameEt.getText().toString());
                                        if (song.getArtist() == null) {
                                            Artist artist = new Artist();
                                            artist.setName(artistNameEt.getText().toString());
                                            song.setArtist(artist);
                                        } else {
                                            song.getArtist().setName(artistNameEt.getText().toString());
                                        }
                                        if (song.getAlbum() == null) {
                                            Album album = new Album();
                                            album.setName(albumNameEt.getText().toString());
                                            song.setAlbum(album);
                                        } else {
                                            song.getAlbum().setName(albumNameEt.getText().toString());
                                        }
                                        DBService.updateSong(song);
                                    } catch (SQLException e) {
                                        Log.e(TAG, "Update song failed.");
                                    }
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
                break;
            case R.id.cb_select:
                Song song = (Song) view.getTag(R.id.tag_entity);
                toggleSong(song);
                break;
            case R.id.bt_favorite:
                if (mTmpPickedSongs.size() == 0) {
                    Toast.makeText(getActivity(), R.string.no_selected_song, Toast.LENGTH_SHORT).show();
                    break;
                }
                try {
                    Playlist favoriteList = DBService.findSinglePlaylist(Playlist.TYPE.FAVORITE);
                    for (Song item : mTmpPickedSongs) {
                        PlaylistSong playlistSong = new PlaylistSong();
                        playlistSong.setPlaylist(favoriteList);
                        playlistSong.setCreateTime(new Date());
                        playlistSong.setSong(item);
                        //save playlist song
                        DBService.savePlaylistSong(playlistSong, favoriteList.getId(), false);
                    }
                    DBService.updatePlaylistWithoutSongs(favoriteList);
                    String message = getActivity().getString(R.string.favorite_successfully_toast_text);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bt_back:
                mLibraryListener.refreshSongView();
                mLibraryListener.hideBatchCheckSongFragment();
                break;
            case R.id.bt_remove:
                if (mTmpPickedSongs.size() == 0) {
                    Toast.makeText(getActivity(), R.string.no_selected_song, Toast.LENGTH_SHORT).show();
                    break;
                }
                switch (mCollection.getType()) {
                    case SINGLE:
                    case ALBUM:
                        openRemoveSongDialog();
                        break;
                    case PLAYLIST:
                        Playlist playlist = (Playlist) mCollection.getOwner();
                        if (playlist.getType() == Playlist.TYPE.ALL || playlist.getType() == Playlist.TYPE.ARTIST) {
                            openRemoveSongDialog();
                        } else {
                            openRemoveSongWithCheckboxDialog();
                        }
                        break;
                }


                break;
        }
    }

    private void openRemoveSongDialog() {
        String promptStr = getResources().getString(R.string.remove_song_file_prompt_text);
        View removeView = Lewa.inflater().inflate(R.layout.v_remove_prompt_without_checkbox, null);
        TextView removeSongTv = (TextView) removeView.findViewById(R.id.tv_remove_song);
        removeSongTv.setText(promptStr);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(R.string.remove_song_file_text)
                .setView(removeView)
                .setPositiveButton(getResources().getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mLibraryListener.removeLocalSongsFromCollection(mTmpPickedSongs);
                        mSongs.removeAll(getPickedSongs());
                        mTmpPickedSongs.clear();
                        setDynamicView();
                        mSongPickAdapter.setData(mSongs);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void openRemoveSongWithCheckboxDialog() {
        String promptStr = Lewa.string(R.string.remove_song_prompt_text);
        View removeView = Lewa.inflater().inflate(R.layout.v_remove_prompt, null);
        TextView removeSongTv = (TextView) removeView.findViewById(R.id.tv_remove_song);
        final CheckBox checkBox = (CheckBox) removeView.findViewById(R.id.cb_select);
        removeSongTv.setText(promptStr);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(R.string.remove_song_text)
                .setView(removeView)
                .setPositiveButton(getResources().getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        List<Song> songs = new ArrayList<Song>();
                        songs.addAll(getPickedSongs());
                        mLibraryListener.removeSongsFromPlaylist(songs, checkBox.isChecked());

                        if (mCollection.getType() == SongCollection.Type.PLAYLIST) {
                            Playlist playlist = (Playlist) mCollection.getOwner();
                            DBService.updatePlaylistSongNumber(playlist.getId());
                        }

                        mSongs.removeAll(getPickedSongs());
                        mSongPickAdapter.setData(mSongs);
                        mTmpPickedSongs.clear();
                        setDynamicView();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    @Override
    public void onFocusChange(View view, boolean b) {

    }

    @Override
    public void showPickSongFragment() {

    }

    @Override
    public void hidePickSongFragment() {

    }

    @Override
    public void cancelEdit() {

    }

    @Override
    public void doEdit(String name) {

    }

    @Override
    public boolean isSongPicked(Song song) {
        return mTmpPickedSongs.contains(song);
    }

    @Override
    public void toggleSong(Song song) {
        if (!mTmpPickedSongs.contains(song)) {
            mTmpPickedSongs.add(song);
        } else {
            mTmpPickedSongs.remove(song);
        }

        setDynamicView();
    }

    private void setDynamicView() {
        mCheckedSongNumber = getResources().getString(R.string.check_song_number);
        mCheckedSongNumber = String.format(mCheckedSongNumber, mTmpPickedSongs.size());
        mTitleTv.setText(mCheckedSongNumber);

        List<Song> allSongs = mSongPickAdapter.getData();
        if(null != allSongs) {
            if(allSongs.size() == mTmpPickedSongs.size()) {
                mSelectAllBtn.setTag(false);
                mSelectAllBtn.setImageResource(R.drawable.se_btn_unselect_all);
            } else {
                mSelectAllBtn.setTag(true);
                mSelectAllBtn.setImageResource(R.drawable.se_btn_select_all);
            }
        }
        if(mTmpPickedSongs.size() <= 0) {
            mRemoveBtn.setEnabled(false);
        } else {
            mRemoveBtn.setEnabled(true);
        }

        if(null == mSongs || mSongs.size() <= 0) {
                mLibraryListener.refreshSongView();
                mLibraryListener.hideBatchCheckSongFragment();
        }
    }

    public void toggleAll() {
        Boolean isSelectAll = (Boolean) mSelectAllBtn.getTag();
        if (isSelectAll == null) {
            isSelectAll = true;
        }

        if (isSelectAll) {
            List<Song> allSongs = mSongPickAdapter.getData();
            mTmpPickedSongs = new ArrayList<Song>();
            mTmpPickedSongs.addAll(allSongs);
            mSelectAllBtn.setTag(false);
            mSelectAllBtn.setImageResource(R.drawable.se_btn_unselect_all);
        } else {
            mTmpPickedSongs = new ArrayList<Song>();
            mSelectAllBtn.setTag(true);
            mSelectAllBtn.setImageResource(R.drawable.se_btn_select_all);
        }
        mSongPickAdapter.notifyDataSetChanged();
        setDynamicView();
    }

    @Override
    public void unSelectAll() {

    }

    @Override
    public int getNewPickedNum() {
        return 0;
    }

    @Override
    public int getRemovedNum() {
        return 0;
    }

    @Override
    public void selectAll(Collection<Song> songs) {

    }

    @Override
    public void doPick() {

    }

    @Override
    public void cancelPick() {

    }

    @Override
    public Playlist getPlaylist() {
        return null;
    }

    @Override
    public Collection<Song> getPickedSongs() {
        return mTmpPickedSongs;
    }

    @Override
    public List<Song> getSelectedSong() {
        return null;
    }

    @Override
    public void remove(Song song) {

    }

    @Override
    public boolean hasRemoves() {
        return false;
    }

    @Override
    public void doRemove() {

    }

    public void openAddToDialog() {       
        final List<DialogItem> items = new ArrayList<DialogItem>();
        DialogItem item1 = new DialogItem();
        item1.setName(getResources().getString(R.string.text_create_new_play_list));
        item1.setType(DialogItem.TYPE_CREATE_PLAY_LIST);
        items.add(item1);
        //itemsArray.add(getResources().getString(R.string.text_create_new_play_list));
        try {
            List<Playlist> playlists = DBService.findPlaylistsForAddTo();
            for (Playlist playlist : playlists) {
                DialogItem item = new DialogItem();
                item.setId(playlist.getId());
                item.setType(DialogItem.TYPE_CUSTOMER_PLAY_LIST);
                item.setName(playlist.getName());
                items.add(item);
                item.setEntity(playlist);
                //itemsArray.add(playlist.getName());
            }
        } catch (Exception e) {

        }
        int itemSize = items.size();
        String[] itemsArray = new String[itemSize];

        for(int i = 0; i < itemSize; i++) {
            DialogItem item = items.get(i);
            itemsArray[i] =  item.getName();
        }
        
        //final DialogAdapter adapter = new DialogAdapter();
        //adapter.setData(items);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(getResources().getString(R.string.text_title_add_to))
                .setCancelable(true)
                .setItems(itemsArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        dialogInterface.dismiss();
                        if (position == 0) {
                            Intent intent = new Intent(getActivity(), EditPlaylistActivity.class);
                            ArrayList<String> jsonlist = new ArrayList<String>();
                            for (Song songData : getPickedSongs()) {
                                jsonlist.add(songData.toJsonString());
                            }
                            intent.putStringArrayListExtra(EDIT_SONG_INTENT_KEY, jsonlist);
                            startActivity(intent);
                        } else {
                            DialogItem item = items.get(position);
                            if (item.getType() == DialogItem.TYPE_CUSTOMER_PLAY_LIST) {
                                Playlist playlist = (Playlist) item.getEntity();
                                try {
                                    for (Song song : mTmpPickedSongs) {
                                        PlaylistSong playlistSong = new PlaylistSong();
                                        playlistSong.setPlaylist(playlist);
                                        playlistSong.setCreateTime(new Date());
                                        playlistSong.setSong(song);
                                        //save playlist song
                                        DBService.savePlaylistSong(playlistSong, playlist.getId(), false);
                                        //pr938219 modify by wjhu
                                        //to show a more detail message
										showToast(Lewa
												.string(R.string.song_num_add_to_playlist,
														mTmpPickedSongs.size())
												+ item.getName());
                                    }
                                    DBService.updatePlaylistSongNumber(playlist.getId());
//                                    DBService.updatePlaylistWithoutSongs(playlist);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).create().show();
    }

}
