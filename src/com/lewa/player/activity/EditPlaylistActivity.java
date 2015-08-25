package com.lewa.player.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.lewa.player.R;
import com.lewa.player.db.DBService;
import com.lewa.player.fragment.EditPlaylistFragment;
import com.lewa.player.fragment.PickSongFragment;
import com.lewa.player.listener.EditPlaylistListener;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.PlaylistSong;
import com.lewa.player.model.Song;
import android.view.View;
import lewa.support.v7.app.ActionBar;


import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class EditPlaylistActivity extends BaseFragmentActivity implements EditPlaylistListener {
    private static final String TAG = "EditPlaylistActivity";
    public final static String EDIT_SONG_INTENT_KEY = "songData";
    public final static String ARG_PLAYLIST_ID = "playlistId";
    private String TAG_PICK_FRAGMENT = "PICK";
    private String TAG_EDIT_FRAGMENT = "EDIT";
    private Long playlistId;
    private Playlist playlist;
    private List<PlaylistSong> savedSongs = new ArrayList<PlaylistSong>();
    private List<Song> pickedSongs = new ArrayList<Song>();
    private List<Song> tmpPickedSongs = new ArrayList<Song>();
    private List<Song> newPickedSongs = new ArrayList<Song>();
    private List<Song> removedSongs = new ArrayList<Song>();
    private List<Song> tmpRemovedSongs = new ArrayList<Song>();
    private String mPlaylistImageDir = Environment.getExternalStorageDirectory() + "/LEWA/music/avatar/";
    private String mPlaylistTmpFile = "temp.jpg";

    //PickSongFragment mPickSongFragment = null;
    int mTotalNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hasBackground = true;
        setContentView(R.layout.activity_edit_playlist);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //getWindow().getDecorView().setSystemUiVisibility(0x10000000 | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras == null) {
                playlistId = null;
            } else {
                playlistId = extras.getLong(ARG_PLAYLIST_ID);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.l_first_page_container, EditPlaylistFragment.newInstance(playlistId), TAG_EDIT_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            Intent intent = getIntent();
            if (intent.hasExtra(EDIT_SONG_INTENT_KEY)) {
                ArrayList<String> jsonlist = (ArrayList<String>) intent.getStringArrayListExtra(EDIT_SONG_INTENT_KEY);
                pickedSongs.clear();
                for (String json : jsonlist) {
                    pickedSongs.add(Song.fromJson(json));
                }
            } else {
                playlist = DBService.findPlaylist(playlistId);
                if (playlist != null) {
                    pickedSongs = DBService.findSongsOfPlaylist(this, playlistId);
                }
            }
            mTotalNum = pickedSongs.size();
            removedSongs.addAll(pickedSongs);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to query playlist: \n" + e.getMessage());
        }
    }

    @Override
    public void showPickSongFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.anim_slide_in, R.anim.anim_slide_out,
                                        R.anim.anim_slide_in, R.anim.anim_slide_out);

        PickSongFragment pickSongFragment = (PickSongFragment) getSupportFragmentManager().findFragmentByTag(TAG_PICK_FRAGMENT);
        if (pickSongFragment == null) {
            pickSongFragment = PickSongFragment.newInstance();
            ft.add(R.id.l_secondary_page_container, pickSongFragment, TAG_PICK_FRAGMENT);
            ft.addToBackStack(null);
            ft.commit();
        } 

        
    }

    @Override
    public void hidePickSongFragment() {
        PickSongFragment pickSongFragment = (PickSongFragment) getSupportFragmentManager().findFragmentByTag(TAG_PICK_FRAGMENT);

        if (pickSongFragment != null) {            
            getSupportFragmentManager().popBackStack();
        }

        EditPlaylistFragment editPlaylistFragment = (EditPlaylistFragment) getSupportFragmentManager().findFragmentByTag(TAG_EDIT_FRAGMENT);

        if (editPlaylistFragment != null) {
            editPlaylistFragment.refreshList();
        }
    }

    @Override
    public void doEdit(String name) {
        File tmpAvatarFile = new File(mPlaylistImageDir + mPlaylistTmpFile);
        if (playlist == null) {
            playlist = new Playlist();
        }
        String uuid = UUID.randomUUID().toString();
        String absolutePath = null;
        if (tmpAvatarFile.exists()) {
            File avatarFile = new File(mPlaylistImageDir + uuid + ".jpg");
            tmpAvatarFile.renameTo(avatarFile);
            absolutePath = mPlaylistImageDir + uuid + ".jpg";
        }
        playlist.setName(name);
        playlist.setType(Playlist.TYPE.LOCAL);
        playlist.setPriority(2);
        playlist.setSongs(new ArrayList());
        if (playlist.getId() == null || playlist.getId() == 0) {
            playlist.setCoverUrl(absolutePath);
        }
        playlist.setSongNum(0);
        pickedSongs.addAll(tmpPickedSongs);

        if (pickedSongs != null && pickedSongs.size() > 0) {
            playlist.setSongNum(pickedSongs.size());

            for (int i = 0; i < pickedSongs.size(); i++) {
                PlaylistSong playlistSong = new PlaylistSong();
                playlistSong.setSong(pickedSongs.get(i));
                playlistSong.setPlaylist(playlist);
                playlist.getSongs().add(playlistSong);
            }
        }

        new Thread() {
            public void run() {
                try {
                    DBService.savePlaylistWithSong(playlist);
                    synchronized (EditPlaylistActivity.this) {
                        EditPlaylistActivity.this.finish();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            };
        }.start();
        // try {
        // DBService.savePlaylistWithSong(playlist);
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
        //this.finish();
    }

    @Override
    public void cancelEdit() {
        this.finish();
    }

    @Override
    public boolean isSongPicked(Song song) {
        return pickedSongs.contains(song) || tmpPickedSongs.contains(song);
    }

    @Override
    public void toggleSong(Song song) {
        if(null != song) {
	        if (!pickedSongs.contains(song) && !tmpPickedSongs.contains(song)) {
	            tmpPickedSongs.add(song);
	        } else {
	            if (pickedSongs.contains(song)) {
	                pickedSongs.remove(song);
	            }

	            if (tmpPickedSongs.contains(song)) {
	                tmpPickedSongs.remove(song);
	            }
	        }
        }

        PickSongFragment pickSongFragment = (PickSongFragment) getSupportFragmentManager().findFragmentByTag(TAG_PICK_FRAGMENT);
        if (pickSongFragment != null) {
            pickSongFragment.toggleSelectState(tmpPickedSongs.size());
        }
    }

    public void unSelectAll() {
        tmpPickedSongs.clear();
    }

    public void selectAll(Collection<Song> songs) {
        if (songs != null) {
            tmpPickedSongs.clear();
            tmpPickedSongs.addAll(songs);
        }
    }

    public List<Song> getSelectedSong() {
        return tmpRemovedSongs;
    }

    public void remove(Song song) {
        if (!tmpRemovedSongs.contains(song)) {
            tmpRemovedSongs.add(song);
        } else {
            tmpRemovedSongs.remove(song);
        }

        refreshRemoveButtonInEdit();
    }


    public void doRemove() {
        removedSongs.removeAll(tmpRemovedSongs);
        newPickedSongs.removeAll(tmpRemovedSongs);
        pickedSongs.removeAll(tmpRemovedSongs);
        tmpRemovedSongs.clear();

        EditPlaylistFragment editPlaylistFragment = (EditPlaylistFragment) getSupportFragmentManager().findFragmentByTag(TAG_EDIT_FRAGMENT);
        if (editPlaylistFragment != null) {
            editPlaylistFragment.refreshRemoveButton();
        }
    }

    @Override
    public boolean hasRemoves() {
        return tmpRemovedSongs.size() > 0;
    }

    @Override
    public void doPick() {

        if (tmpPickedSongs != null) {
            for (Song song : tmpPickedSongs) {
                newPickedSongs.add(song);
                pickedSongs.add(song);
            }
        }
        tmpPickedSongs.clear();
    }

    @Override
    public void cancelPick() {
        tmpPickedSongs.clear();
    }

    @Override
    public int getNewPickedNum() {
        return newPickedSongs.size();
    }

    @Override
    public int getRemovedNum() {
        return mTotalNum - removedSongs.size();
    }

    private void refreshRemoveButtonInEdit() {
        EditPlaylistFragment editPlaylistFragment = (EditPlaylistFragment) getSupportFragmentManager().findFragmentByTag(TAG_EDIT_FRAGMENT);
        if (editPlaylistFragment != null) {
            editPlaylistFragment.refreshRemoveButton();
        }
    }

    @Override
    public Playlist getPlaylist() {
        return playlist;
    }

    @Override
    public Collection<Song> getPickedSongs() {
        return pickedSongs;
    }

    @Override
    public void onBackPressed() {
        
        PickSongFragment pickSongFragment = (PickSongFragment) getSupportFragmentManager().findFragmentByTag(TAG_PICK_FRAGMENT);
        if (pickSongFragment != null && pickSongFragment.isVisible()) {
            hidePickSongFragment();
        } else {
            finish();
        }
    }

}
