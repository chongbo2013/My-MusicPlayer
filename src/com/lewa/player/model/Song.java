package com.lewa.player.model;

import android.database.Cursor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.music.download.DownloadStatus;

import java.util.Date;

/**
 * Created by Administrator on 13-11-25.
 */
public class Song extends BaseModel {
    public static final String TAG = "Song";
    public static final String TABLE_NAME = "song";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LOCAL_ID = "local_id";
    public static final String DOWNLOAD_STATUS = "download_status";
    public static final String PATH = "path";
    public static final String F_TYPE = "type";
    public static final String ARTIST_ID = "artist_id";
    public static final String ARTIST_NAME = "artist";
    public static final String ALBUM_ID = "album_id";
    public static final String ALBUM_NAME = "album";
    public static final String BITRATE = "bitrate";
    public static final String IS_LOSSLESS = "isLossless";

    public static final int DOWNLOADED_NOT_MATCHED = 1;
    public static final int DOWNLOADED_AND_MATCHED = 2;

    private String name;
    private TYPE type;
    private String path;
    private String url;
    private String onlineId;
    private long localId;
    private int downloadStatus;
    private Integer size;
    private Artist artist;
    private Album album;
    private Long duration;
    private Date year;
    private String mimeType;
    private String lrcPath;
    private String bitrate;
    private boolean isLossless;
    private MusicDownloadStatus downStatus;
    //this will not be saved in database, but assigned from artist or album
    private String coverUrl;

    private String bigCoverUrl;

    private SongCollection collection;

    public Song() {
    }

    public String getBigCoverUrl() {
        return bigCoverUrl;
    }

    public void setBigCoverUrl(String bigCoverUrl) {
        this.bigCoverUrl = bigCoverUrl;
    }

    public Song(Long id) {
        this.id = id;
    }

    public Song(Long id, TYPE type) {
        this.id = id;
        this.type = type;
    }

    public Song(Long id, String name, Long artistId, Long albumId, Long duration) {
        this.id = id;
        this.name = name;
        this.artist = new Artist();
        artist.setId(artistId);
        this.album = new Album();
        album.setId(albumId);
        this.duration = duration;
    }

    public static enum TYPE {
        LOCAL, ONLINE, TMP
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOnlineId() {
        return onlineId;
    }

    public void setOnlineId(String onlineId) {
        this.onlineId = onlineId;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
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

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Date getYear() {
        return year;
    }

    public void setYear(Date year) {
        this.year = year;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public SongCollection getCollection() {
        return collection;
    }

    public void setCollection(SongCollection collection) {
        this.collection = collection;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getLrcPath() {
        return lrcPath;
    }

    public void setLrcPath(String lrcPath) {
        this.lrcPath = lrcPath;
    }
    
    public String getBitrate() {
  		return bitrate;
  	}

  	public void setBitrate(String bitrate) {
  		this.bitrate = bitrate;
  	}

  	public boolean isLossless() {
  		return isLossless;
  	}

  	public void setLossless(boolean isLossless) {
  		this.isLossless = isLossless;
  	}
  	

    public MusicDownloadStatus getDownStatus() {
		return downStatus;
	}

	public void setDownStatus(MusicDownloadStatus downStatus) {
		this.downStatus = downStatus;
	}

	@Override
    public boolean equals(Object obj) {
        if (obj instanceof Song) {
            Song song = (Song) obj;

            if (type == null || song.getType() == null || type != song.getType()) return false;

            if (type == TYPE.LOCAL) {
                if (id == null) {
                    return false;
                } else {
                    return id.equals(song.getId());
                }
            } else {
                if (id == null) {
                    return false;
                } else {
                    return id.equals(song.getId());
                }
                //use id insteadly
//                if (onlineId == null) {
//                    return song.getOnlineId() == null;
//                } else {
//                    return onlineId.equals(song.getOnlineId());
//                }
            }
        } else {
            return false;
        }
    }

    public String toJsonString() {
        JSONObject jo = new JSONObject();
        try {
            if (id != null) {
                jo.put(Song.ID, id);
            } else if (onlineId != null) {
                jo.put(Song.ID, onlineId);
            }
            if (album != null) {
                jo.put(Song.ALBUM_ID, album.getId());
                jo.put(Song.ALBUM_NAME, album.getName());
            }
            jo.put(Song.PATH, path);
            jo.put(Song.NAME, name);
            if(bitrate!=null)
            	jo.put(Song.BITRATE, bitrate);
            jo.put(Song.IS_LOSSLESS, isLossless);
            jo.put(Song.F_TYPE, type.name());
            if (artist != null) {
                jo.put(Song.ARTIST_NAME, artist.getName());
                jo.put(Song.ARTIST_ID, artist.getId());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jo.toString();
    }

    public static Song fromJsonObject(JSONObject jo) {
        Song song = new Song();
        try {
            if (jo.has(Song.ID)) {
                song.setId(jo.getLong(Song.ID));
            }
            if (jo.has(Song.PATH)) {
                song.setPath(jo.getString(Song.PATH));
            }
            if (jo.has(Song.NAME)) {
                song.setName((jo.getString(Song.NAME)));
            }
            Artist artist = new Artist();
            if (jo.has(Song.ARTIST_NAME)) {
                artist.setName(jo.getString(Song.ARTIST_NAME));
            }
            if (jo.has(Song.ARTIST_ID)) {
                artist.setId(jo.getLong(Song.ARTIST_ID));
            }
            song.setArtist(artist);
            Album album = new Album();
            if (jo.has(Song.ALBUM_NAME)) {
                album.setName(jo.getString(Song.ALBUM_NAME));
            }
            if (jo.has(Song.ALBUM_ID)) {
                album.setId(jo.getLong(Song.ALBUM_ID));
            }
            song.setAlbum(album);

            Log.d("Song", "Type: " + jo.getString(Song.F_TYPE));
            if (jo.has(Song.F_TYPE)) {
                song.setType(TYPE.valueOf(jo.getString(Song.F_TYPE)));
            }
            if(jo.has(Song.BITRATE))
            	song.setBitrate(jo.getString(Song.BITRATE));
            if(jo.has(Song.IS_LOSSLESS))
            	song.setLossless(jo.getBoolean(Song.IS_LOSSLESS));
            Log.d("Song", "Type: " + song.getType());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return song;
    }

    public static Song fromJson(String json) {
        try {
            JSONObject jo = new JSONObject(json);
            return fromJsonObject(jo);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String CREATE_TABLE_SCRIPT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + ID + " INTEGER PRIMARY KEY," + NAME + " TEXT," + LOCAL_ID + " INTEGER," + DOWNLOAD_STATUS + " INTEGER,"
            + PATH + " TEXT," + F_TYPE + " TEXT," + ARTIST_ID + " INTEGER,"
            + ARTIST_NAME + " TEXT," + ALBUM_ID + " INTEGER," + ALBUM_NAME + " TEXT,"+BITRATE+" TEXT,"+IS_LOSSLESS+" TEXT)";

    public static Song fromMediaStore(Cursor cur, SongCursorIndex sci) {
        Song song = new Song();
        song.setId(cur.getLong(sci.getIdIdx()));
        song.setName(cur.getString(sci.getNameIdx()));
        Artist artist = new Artist(cur.getLong(sci.getArtistIdIdx()));
        artist.setName(cur.getString(sci.getArtistIdx()));
        song.setArtist(artist);
        Album album = new Album(cur.getLong(sci.getAlbumIdIdx()));
        album.setName(cur.getString(sci.getAlbumIdx()));
        song.setAlbum(album);

        Long time = cur.getLong(sci.getYearIdx());

        if (time != null) {
            song.setYear(new Date(time));
        }
        song.setDuration(cur.getLong(sci.getDurationIdx()));
        song.setPath(cur.getString(sci.getDataIdx()));
        song.setSize(cur.getInt(sci.getSizeIdx()));
        song.setMimeType(cur.getString(sci.getMimeIdx()));
        song.setType(Song.TYPE.LOCAL);
        return song;
    }

    public static Song fromCursor(Cursor cursor, OnlineSongCursorIndex cursorIndex) {
        Song song = new Song();
        song.setId(cursor.getLong(cursorIndex.idIdx));
        song.setName(cursor.getString(cursorIndex.nameIdx));

        if (cursor.getString(cursorIndex.typeIdx) != null) {
            song.setType(Song.TYPE.valueOf(cursor.getString(cursorIndex.typeIdx)));
        }
        Artist artist = new Artist();
        artist.setId(cursor.getLong(cursorIndex.artistIdIdx));
        artist.setName(cursor.getString(cursorIndex.artistIdx));
        song.setArtist(artist);

        Album album = new Album();
        album.setId(cursor.getLong(cursorIndex.albumIdIdx));
        album.setName(cursor.getString(cursorIndex.albumIdx));
        song.setLocalId(cursor.getLong(cursorIndex.localIdIdx));
        song.setDownloadStatus(cursor.getInt(cursorIndex.downloadedIdx));
        MusicDownloadStatus downStatus=new MusicDownloadStatus();
        downStatus.setStatus(cursor.getInt(cursorIndex.downloadedIdx));
        song.setDownStatus(downStatus);
        song.setPath(cursor.getString(cursorIndex.pathIndex));
        song.setBitrate(cursor.getString(cursorIndex.bitrateIndex));
        song.setLossless(Boolean.valueOf(cursor.getString(cursorIndex.bitrateIndex)));
        return song;
    }

    public String toString() {
        return "id:" + id + ", name:" + name + ", cover:" + coverUrl + ", bigCover:" + bigCoverUrl
                + ", path:" + path + ", downloadStatus:" + downloadStatus + ", localId:" + localId;
    }

}
