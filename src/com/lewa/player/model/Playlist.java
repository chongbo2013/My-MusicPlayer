package com.lewa.player.model;

import android.database.Cursor;

import com.baidu.music.model.Channel;
import com.baidu.music.model.Topic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by wuzixiu on 11/19/13.
 */
public class Playlist extends BaseModel {
    public static final String TABLE_NAME = "playlist";
    public static final String NAME = "name";
    public static final String F_TYPE = "type";
    public static final String COVER_URL = "cover_url";
    public static final String PRIORITY = "priority";
    public static final String DISPLAY_ORDER = "display_order";
    public static final String SONG_NUM = "song_num";
    public static final String CREATE_TIME = "create_time";

    private String name;

    private TYPE type;

    private String coverUrl;

    private int songNum;

    private int priority;

    private int displayOrder;

    private int isBgCustomized;

    private Date createTime;

    private Date updateTime;

    private Date lastPlayTime;

    private String bdCode;

    private Artist artist;

    private Album album;

    private Collection<PlaylistSong> songs;

    public Playlist() {

    }

    public Playlist(Long id) {
        this.id = id;
    }

    public Playlist(TYPE type) {
        this.type = type;
    }

    public Playlist(String name, String coverUrl) {
        this.name = name;
        this.coverUrl = coverUrl;
    }

    public Playlist(Artist artist) {
        this.type = TYPE.ARTIST;
        this.artist = artist;

        if (artist != null) {
            this.name = artist.getName();
        }
    }

    public Playlist(Long id, String name, String coverUrl) {
        this.id = id;
        this.name = name;
        this.coverUrl = coverUrl;
    }

    public Playlist(Long id, String name, TYPE type, int priority, int displayOrder, String coverUrl) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.priority = priority;
        this.displayOrder = displayOrder;
        this.coverUrl = coverUrl;
    }

    public static enum TYPE {
        ONLINE, LOCAL, TOP_LIST, ALL_STAR, FM, FAVORITE, RECENT_PLAY, DOWNLOAD,
        ONLINE_CATEGORY, TOP_LIST_CATEGORY, TOP_LIST_NEW, TOP_LIST_HOT, ALL_STAR_CATEGORY, ALL, ALBUM, ARTIST
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getSongNum() {
        return songNum;
    }

    public void setSongNum(int songNum) {
        this.songNum = songNum;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(Date lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public Collection<PlaylistSong> getSongs() {
        return songs;
    }

    public void setSongs(Collection<PlaylistSong> songs) {
        this.songs = songs;
    }

    public String getBdCode() {
        return bdCode;
    }

    public void setBdCode(String bdCode) {
        this.bdCode = bdCode;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public List<Song> getSingleSongs() {
        List<Song> songs = new ArrayList<Song>();
        if (getSongs() != null) {
            for (PlaylistSong playlistSong : getSongs()) {
                songs.add(playlistSong.getSong());
            }
        }
        return songs;
    }

    public static String CREATE_TABLE_SCRIPT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + NAME + " TEXT,"
            + COVER_URL + " TEXT," + F_TYPE + " TEXT," + PRIORITY + " INTEGER,"
            + DISPLAY_ORDER + " INTEGER," + SONG_NUM + " INTEGER," + CREATE_TIME + " INTEGER)";

    public static Playlist fromCursor(Cursor cursor, PlaylistCursorIndex cursorIndex) {
        Playlist playlist = new Playlist();
        playlist.setId(cursor.getLong(cursorIndex.idIdx));
        playlist.setName(cursor.getString(cursorIndex.nameIdx));
        playlist.setCoverUrl(cursor.getString(cursorIndex.coverUrlIdx));

        if (cursor.getString(cursorIndex.typeIdx) != null) {
            playlist.setType(Playlist.TYPE.valueOf(cursor.getString(cursorIndex.typeIdx)));
        }

        playlist.setSongNum(cursor.getInt(cursorIndex.songNumberIdx));

        return playlist;
    }

    public static Playlist fromBdChannel(Channel channelData) {
        Playlist playlist = new Playlist();
        playlist.setBdCode(channelData.mChannelId);
        playlist.setId(Long.valueOf(channelData.mChannelId));
        playlist.setName(channelData.mName);
        playlist.setType(TYPE.FM);
        playlist.setCoverUrl(channelData.mThumb);

        return playlist;
    }

    public static Playlist fromBdHotAlbum(Topic data) {
        Playlist playList = new Playlist();
        playList.setBdCode(data.mCode);
        playList.setType(Playlist.TYPE.ONLINE);
        playList.setCoverUrl(data.mPicture);
        playList.setName(data.mName);

        return playList;
    }

    public static Playlist fromBdHotArtist(com.baidu.music.model.Artist data) {
        Playlist playList = new Playlist();
        playList.setBdCode(data.mArtistId);
        Artist artist = new Artist();
        artist.setId(Long.valueOf(data.mArtistId));
        artist.setName(data.mName);
        artist.setSongNum(Integer.valueOf(data.mMusicCount));
        artist.setPicPath(data.mAvatarBig != null ? data.mAvatarBig : (data.mAvatarMiddle != null ? data.mAvatarMiddle : (data.mAvatarMini != null ? data.mAvatarMini : null)));
        artist.setPicPathBig(data.mAvatarBig);
        artist.setPicPathMini(data.mAvatarMini);
        playList.setArtist(artist);

        playList.setType(TYPE.ALL_STAR);

        playList.setCoverUrl(artist.getPicPath());
        playList.setName(data.mName);

        return playList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Playlist) {
            if (obj == null) {
                return false;
            } else {
                Playlist another = (Playlist) obj;

                if (type == null || another.getType() == null || type != another.getType()) {
                    return false;
                }

                switch (type) {
                    case ALL:
                    case RECENT_PLAY:
                    case FAVORITE:
                    case DOWNLOAD:
                        return true;

                    case ALL_STAR:
                    case TOP_LIST_HOT:
                    case TOP_LIST_NEW:
                    case ONLINE:
                    case FM:

                    case ONLINE_CATEGORY:
                    case TOP_LIST_CATEGORY:
                    case ALL_STAR_CATEGORY:
                        return bdCode != null && another.getBdCode() != null && bdCode.trim().endsWith(another.getBdCode().trim());
                    case ARTIST:
                        return artist != null && another.getArtist() != null && artist.equals(another.getArtist());
                    default:
                        return id != null && another.getId() != null && id.longValue() == another.getId();
                }
            }
        } else {
            return false;
        }
    }

    public boolean isLocal() {
        return type == TYPE.ALL || type == TYPE.DOWNLOAD || type == TYPE.FAVORITE || type == TYPE.RECENT_PLAY || type == TYPE.LOCAL || type == TYPE.ARTIST;
    }

    public static final long ALL_ID = 1;
    public static final long FAVORITE_ID = 2;
    public static final long RECENT_PLAY_ID = 3;
    public static final long DOWNLOAD_ID = 4;
}
