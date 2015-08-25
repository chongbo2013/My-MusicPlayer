package com.lewa.player.model;

import android.database.Cursor;

/**
 * Created by wuzixiu on 1/10/14.
 */
public class SongCollectionIndex {
    public int idIdx;
    public int nameIdx;
    public int typeIdx;
    public int refTypeIdx;
    public int refIdIdx;
    public int coverUrlIdx;
    public int displayOrderIdx;
    public int isEmptyIdx;
    public int lastSongIdIdx;
    public int lastSongNameIdx;
    public int mimeIdx;

    public SongCollectionIndex(Cursor cursor) {
        idIdx = cursor.getColumnIndexOrThrow(SongCollection.ID);
        nameIdx = cursor.getColumnIndexOrThrow(SongCollection.NAME);
        typeIdx = cursor.getColumnIndexOrThrow(SongCollection.COLLECTION_TYPE);
        refTypeIdx = cursor.getColumnIndexOrThrow(SongCollection.REF_TYPE);
        refIdIdx = cursor.getColumnIndexOrThrow(SongCollection.REF_ID);
        coverUrlIdx = cursor.getColumnIndexOrThrow(SongCollection.COVER_URL);
        displayOrderIdx = cursor.getColumnIndexOrThrow(SongCollection.DISPLAY_ORDER);
        isEmptyIdx = cursor.getColumnIndexOrThrow(SongCollection.IS_EMPTY);
        lastSongIdIdx = cursor.getColumnIndexOrThrow(SongCollection.LAST_SONG_ID);
        lastSongNameIdx = cursor.getColumnIndexOrThrow(SongCollection.LAST_SONG_NAME);
    }

    public int getIdIdx() {
        return idIdx;
    }

    public int getNameIdx() {
        return nameIdx;
    }

    public int getTypeIdx() {
        return typeIdx;
    }

    public int getRefTypeIdx() {
        return refTypeIdx;
    }

    public int getRefIdIdx() {
        return refIdIdx;
    }

    public int getCoverUrlIdx() {
        return coverUrlIdx;
    }

    public int getDisplayOrderIdx() {
        return displayOrderIdx;
    }

    public int getIsEmptyIdx() {
        return isEmptyIdx;
    }

    public int getLastSongIdIdx() {
        return lastSongIdIdx;
    }

    public int getLastSongNameIdx() {
        return lastSongNameIdx;
    }

    public int getMimeIdx() {
        return mimeIdx;
    }
}
