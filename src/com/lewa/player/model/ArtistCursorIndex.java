package com.lewa.player.model;

import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by wuzixiu on 1/10/14.
 */
public class ArtistCursorIndex {

    public int idIdx = -1;
    public int nameIdx = -1;
    public int songNumIdx = -1;
    public int albumNumIdx = -1;


    public ArtistCursorIndex(Cursor cursor) {
        idIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
        nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
        albumNumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
        songNumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
    }
}
