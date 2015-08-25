package com.lewa.player.listener;

import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;

import java.util.Collection;
import java.util.List;

/**
 * Created by wuzixiu on 12/13/13.
 */
public interface EditPlaylistListener {

    public void showPickSongFragment();

    public void hidePickSongFragment();

    public void cancelEdit();

    public void doEdit(String name);

    public boolean isSongPicked(Song song);

    public void toggleSong(Song song);

    public void selectAll(Collection<Song> songs);

    public  void unSelectAll();

    public void doPick();

    public void cancelPick();

    public Playlist getPlaylist();

    public Collection<Song> getPickedSongs();

    public boolean hasRemoves();

    public void doRemove();

    public void remove(Song song);

    public List<Song> getSelectedSong();

    public int getNewPickedNum();

    public int getRemovedNum();

}
