package com.lewa.player.model;

import android.database.Cursor;

/**
 * Created by Administrator on 13-12-2.
 */
public class Album extends BaseModel {
    public static final String TABLE_NAME = "album";
    public static final String NAME = "name";
    public static final String ARTIST_ID = "artist_id";
    public static final String SONG_NUM = "song_num";

    private String name;

    private Artist artist;

    private TYPE type;

    //picture url
    private String art;

    private Integer songNum;

    public Album() {
    }

    public Album(Long id) {
        this.id = id;
    }

    public Album(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Album(String name, Integer songNum, String art) {
        this.name = name;
        this.songNum = songNum;
        this.art = art;
    }

    public static enum TYPE {
        ONLINE, LOCAL
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSongNum() {
        return songNum;
    }

    public void setSongNum(Integer songNum) {
        this.songNum = songNum;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public static Album fromCursor(Cursor albumCursor, AlbumCursorIndex albumCursorIndex) {
        Album album = new Album();
        album.setId(albumCursor.getLong(albumCursorIndex.getIdIdx()));
        album.setName(albumCursor.getString(albumCursorIndex.getNameIdx()));
        album.setSongNum(albumCursor.getInt(albumCursorIndex.getSongNumIdx()));
        Artist artist = new Artist();
        artist.setName(albumCursor.getString(albumCursorIndex.getArtistIdx()));
        album.setArtist(artist);
        album.setArt(albumCursor.getString(albumCursorIndex.getAlbumArtIdx()));

        return album;
    }

}
