package com.lewa.player.model;

import android.database.Cursor;

/**
 * Created by wuzixiu on 1/10/14.
 */
public class PlaylistCursorIndex {
    public int idIdx;
    public int typeIdx;
    public int nameIdx;
    public int coverUrlIdx;
    public int priorityIdx;
    public int displayOrderIdx;
    public int songNumberIdx;
    public int createTimeIdx;

    public PlaylistCursorIndex(Cursor cursor) {
        idIdx = cursor.getColumnIndexOrThrow(Playlist.ID);
        typeIdx = cursor.getColumnIndexOrThrow(Playlist.F_TYPE);
        nameIdx = cursor.getColumnIndexOrThrow(Playlist.NAME);
        coverUrlIdx = cursor.getColumnIndexOrThrow(Playlist.COVER_URL);
        priorityIdx = cursor.getColumnIndexOrThrow(Playlist.PRIORITY);
        displayOrderIdx = cursor.getColumnIndexOrThrow(Playlist.DISPLAY_ORDER);
        songNumberIdx = cursor.getColumnIndexOrThrow(Playlist.SONG_NUM);
        createTimeIdx = cursor.getColumnIndexOrThrow(Playlist.CREATE_TIME);
    }
}
