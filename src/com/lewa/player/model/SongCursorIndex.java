package com.lewa.player.model;

import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by wuzixiu on 1/10/14.
 */
public class SongCursorIndex {
    public int idIdx;
    public int nameIdx;
    public int artistIdIdx;
    public int artistIdx;
    public int albumIdIdx;
    public int albumIdx;
    public int yearIdx;
    public int durationIdx;
    public int dataIdx;
    public int sizeIdx;
    public int mimeIdx;

    public SongCursorIndex(Cursor cursor) {
        idIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        artistIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
        artistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        albumIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
        albumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
        yearIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);
        durationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
        dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
        mimeIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE);
    }

    public int getIdIdx() {
        return idIdx;
    }

    public int getNameIdx() {
        return nameIdx;
    }

    public int getArtistIdIdx() {
        return artistIdIdx;
    }

    public int getArtistIdx() {
        return artistIdx;
    }

    public int getAlbumIdIdx() {
        return albumIdIdx;
    }

    public int getAlbumIdx() {
        return albumIdx;
    }

    public int getYearIdx() {
        return yearIdx;
    }

    public int getDurationIdx() {
        return durationIdx;
    }

    public int getDataIdx() {
        return dataIdx;
    }

    public int getSizeIdx() {
        return sizeIdx;
    }

    public int getMimeIdx() {
        return mimeIdx;
    }
}
