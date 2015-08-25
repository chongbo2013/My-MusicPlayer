package com.lewa.player.listener;

import com.lewa.player.enums.StarCatalog;
import com.lewa.player.fragment.SearchFragment.SearchType;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;

import java.util.List;

/**
 * Created by Administrator on 13-12-6.
 */
public interface LibraryListener extends AlbumListener {

    public void showOnlinePlaylistFragment();

    public void hideBatchCheckSongFragment();

    

    public void hideTrackFragment();

    public void showTopListFragment();

    public void hideTopListFragment();

    public void showArtistFragment(StarCatalog catalog);

    public void hideArtistFragment();

    public void showAllStarFragment();

    public void hideAllStarFragment();

    public void showAlbumFragment(Long artistId, String artistName);

    public void hideAlbumFragment();

    public List<Song> getSongs();

    

    public void removeLocalSongsFromCollection(List<Song> songs);

    public void removeSongsFromPlaylist(List<Song> songs, boolean removeFile);

    public void showSearchFragment(SearchType type);

    public void hideSearchFragment();

    public void refreshSongView();

    public Song getLongClickSong();

    public boolean getKeyboardStatus();

    public boolean setKeyboardStatus (boolean keyboardStatus);

}
