package com.lewa.player.model;

import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by wuzixiu on 1/10/14.
 */
public class AlbumCursorIndex {

    int idIdx = -1;
    int nameIdx = -1;
    int artistIdx = -1;
    int albumArtIdx = -1;
    int songNumIdx = -1;


    public AlbumCursorIndex(Cursor cursor) {
        idIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
        nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
        artistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
        albumArtIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
        songNumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
    }

    public int getIdIdx() {
        return idIdx;
    }

    public void setIdIdx(int idIdx) {
        this.idIdx = idIdx;
    }

    public int getNameIdx() {
        return nameIdx;
    }

    public void setNameIdx(int nameIdx) {
        this.nameIdx = nameIdx;
    }

    public int getArtistIdx() {
        return artistIdx;
    }

    public void setArtistIdx(int artistIdx) {
        this.artistIdx = artistIdx;
    }

    public int getAlbumArtIdx() {
        return albumArtIdx;
    }

    public void setAlbumArtIdx(int albumArtIdx) {
        this.albumArtIdx = albumArtIdx;
    }

    public int getSongNumIdx() {
        return songNumIdx;
    }

    public void setSongNumIdx(int songNumIdx) {
        this.songNumIdx = songNumIdx;
    }
}
