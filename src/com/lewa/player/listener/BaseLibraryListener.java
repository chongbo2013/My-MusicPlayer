package com.lewa.player.listener;

import android.view.View;

import com.lewa.player.model.SongCollection;

/**
 * Created by wuzixiu on 1/4/14.
 */
public interface BaseLibraryListener {

    public void showSongList(View view, SongCollection songCollection, CallbackListener listener);

    public boolean hideSongList();

    public void showSongInfoListFragment(String title, SongCollection songCollection);
    public void hideSongInfoListFragment();

    public void onBack();

    public void showMiniPlayerView();

    public void hideMiniPlayerView();
}
