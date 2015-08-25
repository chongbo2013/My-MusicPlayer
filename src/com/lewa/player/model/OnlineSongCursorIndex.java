package com.lewa.player.model;

import android.database.Cursor;

/**
 * Created by wuzixiu on 1/10/14.
 */
public class OnlineSongCursorIndex {
    public int idIdx;
    public int typeIdx;
    public int nameIdx;
    public int artistIdIdx;
    public int artistIdx;
    public int albumIdIdx;
    public int albumIdx;
    public int localIdIdx;
    public int durationIdx;
    public int dataIdx;
    public int sizeIdx;
    public int downloadedIdx;
    public int pathIndex;
    public int bitrateIndex;
    public int isLosslessIndex;

    public OnlineSongCursorIndex(Cursor cursor) {
        idIdx = cursor.getColumnIndexOrThrow(Song.ID);
        typeIdx = cursor.getColumnIndexOrThrow(Song.F_TYPE);
        nameIdx = cursor.getColumnIndexOrThrow(Song.NAME);
        artistIdIdx = cursor.getColumnIndexOrThrow(Song.ARTIST_ID);
        artistIdx = cursor.getColumnIndexOrThrow(Song.ARTIST_NAME);
        albumIdIdx = cursor.getColumnIndexOrThrow(Song.ALBUM_ID);
        albumIdx = cursor.getColumnIndexOrThrow(Song.ALBUM_NAME);
        localIdIdx = cursor.getColumnIndexOrThrow(Song.LOCAL_ID);
//        durationIdx = cursor.getColumnIndexOrThrow(Song.DURATION);
//        dataIdx = cursor.getColumnIndexOrThrow(Song.DATA);
//        sizeIdx = cursor.getColumnIndexOrThrow(Song.SIZE);
        downloadedIdx = cursor.getColumnIndexOrThrow(Song.DOWNLOAD_STATUS);
        pathIndex = cursor.getColumnIndexOrThrow(Song.PATH);
        bitrateIndex=cursor.getColumnIndexOrThrow(Song.BITRATE);
        isLosslessIndex=cursor.getColumnIndexOrThrow(Song.IS_LOSSLESS);
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

    public int getLocalIdIdx() {
        return localIdIdx;
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

    public int getDownloadedIdx() {
        return downloadedIdx;
    }
}
