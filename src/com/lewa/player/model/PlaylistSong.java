package com.lewa.player.model;

import android.database.Cursor;

import java.util.Date;

/**
 * Created by wuzixiu on 12/11/13.
 */
public class PlaylistSong extends BaseModel {
    public static final String TABLE_NAME = "playlist_song";
    public static final String PLAYLIST_ID = "playlist_id";
    public static final String SONG_ID = "song_id";
    public static final String SONG_TYPE = "song_type";
    public static final String CREATE_TIME = "create_time";
    public static final String LAST_UPDATE_TIME = "last_update_time";

    private Playlist playlist;

    private Song song;

    private Song.TYPE songType;

    private Date createTime;

    public static String getPlaylistId() {
        return PLAYLIST_ID;
    }

    public static String getSongId() {
        return SONG_ID;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Song.TYPE getSongType() {
        return songType;
    }

    public void setSongType(Song.TYPE songType) {
        this.songType = songType;
    }

    public static PlaylistSong fromCursor(Cursor cursor) {
        PlaylistSong pls = new PlaylistSong();

        pls.setId(cursor.getLong(0));
        pls.setPlaylist(new Playlist(cursor.getLong(1)));
        pls.setSong(new Song(cursor.getLong(2)));

        if (cursor.getString(3) != null) {
            pls.setSongType(Song.TYPE.valueOf(cursor.getString(3)));
        }

        return pls;
    }
}
