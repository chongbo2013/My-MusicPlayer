package com.lewa.player.listener;
import com.lewa.player.model.SongCollection;

/**
 * Created by Administrator on 13-12-6.
 */
public interface AlbumListener extends BaseLibraryListener {

    public void showAlbumFragment(Long artistId, String artistName);

    public void hideAlbumFragment();

    public void showBatchCheckSongFragment();

    public void showBatchCheckSongFragment(Long songId);

    public void setSongCollection(SongCollection songCollection);

    public SongCollection getSongCollection();

}
