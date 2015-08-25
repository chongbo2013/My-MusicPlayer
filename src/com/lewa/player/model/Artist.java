package com.lewa.player.model;

import android.database.Cursor;

/**
 * Created by Administrator on 13-11-28.
 */
public class Artist extends BaseModel {
    public static final String TABLE_NAME = "artist";
    public static final String NAME = "name";
    public static final String PIC_PATH = "pic_path";
    public static final String INITIAL = "initial";
    public static final String SONG_NUM = "song_num";
    public static final String ALBUM_NUM = "album_num";

    private String name;

    //picture url
    private String picPath;

    private String picPathMini;

    private String picPathBig;

    //private String initial;

    private Integer songNum;

    private Integer albumNum;

    private boolean isOnline;

    public Artist() {
    }

    public Artist(Long id) {
        this.id = id;
    }

    public Artist(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Artist(Long id, String name, String picPath, String initial) {
        this(id, name);
        this.picPath = picPath;
        this.initial = initial;
    }

    public String getPicPathMiddle() {
        return picPathMini;
    }

    public void setPicPathMini(String picPathMini) {
        this.picPathMini = picPathMini;
    }

    public String getPicPathBig() {
        return picPathBig;
    }

    public void setPicPathBig(String picPathBig) {
        this.picPathBig = picPathBig;
    }

    public Integer getSongNum() {
        return songNum;
    }

    public void setSongNum(Integer songNum) {
        this.songNum = songNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    /*public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }*/

    public Integer getAlbumNum() {
        return albumNum;
    }

    public void setAlbumNum(Integer albumNum) {
        this.albumNum = albumNum;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public static Artist fromCursor(Cursor artistCursor, ArtistCursorIndex cursorIndex) {
        Artist artist = new Artist();
        artist.setId(artistCursor.getLong(cursorIndex.idIdx));
        artist.setName(artistCursor.getString(cursorIndex.nameIdx));
        artist.setSongNum(artistCursor.getInt(cursorIndex.songNumIdx));
        artist.setAlbumNum(artistCursor.getInt(cursorIndex.albumNumIdx));

        return artist;
    }

    public boolean equals(Object o) {
        if (o == null || id == null || !(o instanceof Artist)) return false;

        Artist another = (Artist) o;
        if (another.getId() == null) return false;

        return id == another.getId();
    }
}
