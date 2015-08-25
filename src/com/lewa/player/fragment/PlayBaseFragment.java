package com.lewa.player.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.lewa.Lewa;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.activity.EditPlaylistActivity;
import com.lewa.player.adapter.DialogAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.listener.CallbackFavoriteListener;
import com.lewa.player.model.Album;
import com.lewa.player.model.Artist;
import com.lewa.player.model.DialogItem;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.PlaylistSong;
import com.lewa.player.model.Song;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Toast;
import java.lang.reflect.Field;



/**
 * Created by Administrator on 14-1-4.
 */
public class PlayBaseFragment extends BaseFragment {

    private static final String TAG = "PlayBaseFragment"; //.class.getName();
    protected static boolean isNeedFresh = false;
    public final static String EDIT_SONG_INTENT_KEY = "songData";
    ImageButton mArtistBtn; //artist or download all icon 
    ImageButton mAddToBtn;
    ImageButton mSetAsBellBtn;
    ImageButton mEditBtn;
    ImageButton mDownloadBtn;
    ImageButton mShareBtn;

    AlertDialog.Builder mAddBuilder;
    AlertDialog.Builder mEditBuilder;
    boolean mShowDialog = false;
    boolean mMoreViewStatus = true;
    View mMoreLo;
    Handler mHandler;

    protected void initSettingBtn(View rootView, View.OnClickListener listener) {
        mAddToBtn = (ImageButton) rootView.findViewById(R.id.bt_add_to_list);
        mSetAsBellBtn = (ImageButton) rootView.findViewById(R.id.bt_as_bell);
        mEditBtn = (ImageButton) rootView.findViewById(R.id.bt_edit);
        mDownloadBtn = (ImageButton) rootView.findViewById(R.id.bt_download);
        mShareBtn = (ImageButton) rootView.findViewById(R.id.bt_share);

        mAddToBtn.setOnClickListener(listener);
        mSetAsBellBtn.setOnClickListener(listener);
        mEditBtn.setOnClickListener(listener);
        mDownloadBtn.setOnClickListener(listener);
        mShareBtn.setOnClickListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler = new Handler();
    }

	 //add by sjxu start for bug 44329
     public void setUserVisibleHint (boolean isVisibleToUser) {
        if(isNeedFresh && isVisibleToUser) {
            isNeedFresh = false;
            refreshSongInfo();
        }

        if(!isVisibleToUser ||null == mDownloadBtn ) return;
        Song playingSong = Lewa.getPlayingSong();
        if(null == playingSong || playingSong.getType() == Song.TYPE.LOCAL) return;
        
        Song downloadingSong = Lewa.getDownloadingSong().get(playingSong.getId());
        if(null == downloadingSong) {                    
            mDownloadBtn.setClickable(true);
            mDownloadBtn.setAlpha(1.0f);
        } else {    //song is downloading
            mDownloadBtn.setAlpha(0.5f);
            mDownloadBtn.setClickable(false);    
        }
    }
	//add by sjxu end for bug 44329

    protected void refreshSongInfo() {

    }
	
    protected void addTo(final Song song, final CallbackFavoriteListener callbackListener) {
        mHandler.removeCallbacks(mDelayHideTask);
        if (song == null) {
            startCloseTask(mMoreLo);
            return;
        }

        final List<DialogItem> items = new ArrayList<DialogItem>();
        DialogItem item1 = new DialogItem();
        item1.setName(getResources().getString(R.string.text_create_new_play_list));
        item1.setType(DialogItem.TYPE_CREATE_PLAY_LIST);
        items.add(item1);
        try {
            List<Playlist> playlists = DBService.findPlaylistsForAddTo();
            for (Playlist playlist : playlists) {
                DialogItem item = new DialogItem();
                item.setId(playlist.getId());
                item.setType(DialogItem.TYPE_CUSTOMER_PLAY_LIST);
                item.setName(playlist.getName());
                items.add(item);
                item.setEntity(playlist);
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
        mAddBuilder = new AlertDialog.Builder(this.getActivity());
        mAddBuilder.setTitle(getResources().getString(R.string.text_title_add_to))
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        startCloseTask(mMoreLo);
                    }
                })
                //.setAdapter(adapter, new DialogInterface.OnClickListener() {
                .setItems(itemsArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        if (position == 0) {
                            dialogInterface.dismiss();
                            Intent intent = new Intent(getActivity(), EditPlaylistActivity.class);
                            ArrayList<String> jsonlist = new ArrayList<String>();
                            jsonlist.add(song.toJsonString());
                            intent.putStringArrayListExtra(EDIT_SONG_INTENT_KEY, jsonlist);
                            startActivity(intent);
                        } else {
                            DialogItem item = items.get(position);
                            if (item.getType() == DialogItem.TYPE_CUSTOMER_PLAY_LIST) {
                                Playlist playlist = (Playlist) item.getEntity();
                                if (playlist.getType() == Playlist.TYPE.FAVORITE && callbackListener != null) {
                                    callbackListener.execute();
                                }
                                PlaylistSong playlistSong = new PlaylistSong();
                                playlistSong.setPlaylist(playlist);
                                playlistSong.setCreateTime(new Date());
                                playlistSong.setSong(song);
                                playlist.setSongNum(playlist.getSongNum() + 1);
                                try {
                                    DBService.updatePlaylistWithoutSongs(playlist);
                                    DBService.savePlaylistSong(playlistSong, playlist.getId(), true);
                                    Toast.makeText(getActivity(), Lewa.string(R.string.song_add_to, song.getName(), item.getName()), Toast.LENGTH_SHORT).show();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        startCloseTask(mMoreLo);
                    }
                }).create().show();
        mShowDialog = true;
    }

    protected void editSong(final Song song) {
        mHandler.removeCallbacks(mDelayHideTask);
        if (song == null) {
            startCloseTask(mMoreLo);
            return;
        }
        showOrHideMoreView(mMoreLo);
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
        mEditBuilder = new AlertDialog.Builder(this.getActivity());
        mEditBuilder.setTitle(R.string.edit_song_text)
                .setView(editView)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        startCloseTask(mMoreLo);
                    }
                })
                .setPositiveButton(getResources().getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String songName = songNameEt.getText().toString();
                        String artistName = artistNameEt.getText().toString();
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            if(null == songName || songName.trim().length() == 0) {
                                Toast.makeText(getActivity(), R.string.song_name_null_hint, Toast.LENGTH_SHORT).show();
                                field.set(dialog, false);
                                return;
                            }

                            if(null == artistName || artistName.trim().length() == 0) {
                                Toast.makeText(getActivity(), R.string.artist_name_null_hint, Toast.LENGTH_SHORT).show();
                                field.set(dialog, false);
                                return;
                            }
                            field.set(dialog,  true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        song.setName(songName);
                        if (song.getArtist() == null) {
                            Artist artist = new Artist();
                            artist.setName(artistName);
                            song.setArtist(artist);
                        } else {
                            song.getArtist().setName(artistName);
                        }
                        if (song.getAlbum() == null) {
                            Album album = new Album();
                            album.setName(albumNameEt.getText().toString());
                            song.setAlbum(album);
                        } else {
                            song.getAlbum().setName(albumNameEt.getText().toString());
                        }
                        try {
                            DBService.updateSong(song);
                        } catch (SQLException e) {
                            Log.e(TAG, "Update song failed.");
                        }
                        startCloseTask(mMoreLo);
						//add by sjxu start for bug 44329
                        isNeedFresh = true;
                        
                        refreshSongInfo();
						//add by sjxu end for bug 44329
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);                            
                            field.set(dialog,  true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        startCloseTask(mMoreLo);
                        dialog.cancel();
                    }
                }).create().show();
    }

    protected void setAsBell(Song song) {
        mHandler.removeCallbacks(mDelayHideTask);
        if (song == null || song.getType() != Song.TYPE.LOCAL) {
            startCloseTask(mMoreLo);
            return;
        }
        MusicUtils.setRingtone(getActivity(), song.getId());
        //startCloseTask(mMoreLo);
        showOrHideMoreView(mMoreLo);
    }

    protected void shareSong(Song song) {
        //TODO
        //mHandler.removeCallbacks(mDelayHideTask);

        //startCloseTask(mMoreLo);
        showOrHideMoreView(mMoreLo);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        Artist artist = song.getArtist();
        String artistName = null;
        if(null != artist) {
            artistName = artist.getName();
        }
        intent.putExtra(Intent.EXTRA_TEXT,
                        String.format(getResources().getString(R.string.share_content), 
                                      artistName + "  << " + song.getName() + ">>", "http://music.baidu.com/song/" + song.getId()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_title)));
    }

    protected void downloadSong(Song song) {
        mHandler.removeCallbacks(mDelayHideTask);
        if (song != null && song.getId() != null) {
            Lewa.downloadSong(song);

            Song playingSong = Lewa.getPlayingSong();
            if (playingSong != null) {
                if(song.getId() == playingSong.getId()) {
                     mDownloadBtn.setAlpha(0.5f);
        	            mDownloadBtn.setClickable(false); 
                }
            }
        }
        startCloseTask(mMoreLo);
    }
	
	//Play need refresh playing song state (isLocal , ID ...) when downloaded  a song  
    protected void refreshPlaySongState(PlayBaseFragment fragment, long localId) {
        
            Song playingSong = Lewa.getPlayingSong();
            if(null != playingSong) {
                try {
                    Song downloadedSong = DBService.findSongById(localId);
                    if(playingSong.getName().equals(downloadedSong.getName()) ){
                        playingSong.setId(localId);
                        playingSong.setType(Song.TYPE.LOCAL);
                        refreshMoreBarState(playingSong);
                        mDownloadBtn.setClickable(true);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            
           
    }

    //Play need refresh more bar state (download  setAsBell ...) when downloaded  a song  . sub class implements refreshMoreBarState(...)
    protected void refreshMoreBarState(Song playingSong) {
        if (playingSong != null) {
            if (playingSong.getType() == Song.TYPE.LOCAL) {
                mEditBtn.setVisibility(View.VISIBLE);
                mSetAsBellBtn.setVisibility(View.VISIBLE);
                mDownloadBtn.setVisibility(View.GONE);
                mShareBtn.setVisibility(View.GONE);
            } else {
                mEditBtn.setVisibility(View.GONE);
                mSetAsBellBtn.setVisibility(View.GONE);
                mDownloadBtn.setVisibility(View.VISIBLE);               
                mShareBtn.setVisibility(View.VISIBLE);
                //mDownloadBtn.setClickable(true);
                Song downloadingSong = Lewa.getDownloadingSong().get(playingSong.getId());
                if(null == downloadingSong) {                    
                    mDownloadBtn.setClickable(true);
                    mDownloadBtn.setAlpha(1.0f);
                } else {    //song is downloading
                    mDownloadBtn.setAlpha(0.5f);
                    mDownloadBtn.setClickable(false);    
                }
            }
        }

    }

    protected void showOrHideMoreView(View view) {
        //mHandler.removeCallbacks(mDelayHideTask);
        //mMoreLo = view;
        removeCloseTask(view);

        if (mMoreLo.isShown()) {
             mMoreLo.startAnimation(AnimationUtils.loadAnimation(this.getActivity(), lewa.R.anim.panel_exit ));             
            mMoreLo.setVisibility(View.GONE);
            //pr947217 modify by wjhu
            //should not do this because it is still visible
			//mAddToBtn.setEnabled(false);
            //mArtistBtn.setEnabled(false);
			mDownloadBtn.setEnabled(false);
			mShareBtn.setEnabled(false);
        } else {
             mMoreLo.startAnimation(AnimationUtils.loadAnimation(this.getActivity(), lewa.R.anim.panel_enter));
            mMoreLo.setVisibility(View.VISIBLE);
            mAddToBtn.setEnabled(true);
            mArtistBtn.setEnabled(true);
			mDownloadBtn.setEnabled(true);
			mShareBtn.setEnabled(true);
            startCloseTask(mMoreLo);
        }
    }
    
    protected void startCloseTask(final View view) {
        mMoreLo = view;
        mMoreViewStatus = true;
        mHandler.removeCallbacks(mDelayHideTask);
        mHandler.postDelayed(mDelayHideTask, 5000);
    }

    protected void removeCloseTask(final View view) {
        mMoreLo = view;
        mHandler.removeCallbacks(mDelayHideTask);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    TimerTask mDelayHideTask = new TimerTask() {
        public void run() {
            if(null != mMoreLo && mMoreLo.isShown()) {
                mMoreLo.startAnimation(AnimationUtils.loadAnimation(PlayBaseFragment.this.getActivity(), lewa.R.anim.panel_exit));
                mMoreLo.setVisibility(View.GONE);
                mAddToBtn.setEnabled(true);
                mArtistBtn.setEnabled(true);
            }
        }
    };

    @Override
    void refreshPlayStatus(PlayStatus playStatus) {
        super.refreshPlayStatus(playStatus);

    }

}
