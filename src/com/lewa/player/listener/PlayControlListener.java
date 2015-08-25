package com.lewa.player.listener;

import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;

/**
 * Created by Administrator on 13-12-15.
 */
public interface PlayControlListener {

    void showPlaylist();

    void showPlay();

    void play();

    void playSong(Song song);

    void pause();

    void previous();

    void next();

    void playPlaylist(Playlist playlist);

    void resetBg(int position);

}
