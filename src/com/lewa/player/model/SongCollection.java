package com.lewa.player.model;

import android.database.Cursor;
import android.util.Log;

import com.lewa.Lewa;
import com.lewa.player.db.DBService;
import com.lewa.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by wuzixiu on 12/24/13.
 */
public class SongCollection {
    private static final String TAG = "SongCollection";//.class.getName();
    public static final String ID = "id";
    public static final String TABLE_NAME = "play_history";
    public static final String NAME = "name";
    public static final String COLLECTION_TYPE = "collection_type";
    public static final String REF_TYPE = "ref_type";
    public static final String REF_ID = "ref_id";
    public static final String COVER_URL = "cover_url";
    public static final String DISPLAY_ORDER = "display_order";
    public static final String IS_EMPTY = "is_empty";
    public static final String LAST_SONG_ID = "last_song_id";
    public static final String LAST_SONG_NAME = "last_song_name";

    private String id;
    private String name;
    private String coverUrl;
    private Type type;
    private Object owner;
    private int displayOrder;
    private int isEmpty;
    private Song lastSong;
    private Cursor cursor;
    private List<Song> songs;
    private boolean isCursor = false;
    private SongCursorIndex mSongCursorIndex = null;
    private boolean isInitialized = false;

    public String albumArtistName = null; //only use for album type

    public SongCollection() {
    }

    public SongCollection(Type type, Object owner) {
        this.type = type;
        this.owner = owner;

        if (type == Type.PLAYLIST && owner != null) {
            Log.i(TAG, "Playlist name: " + ((Playlist) owner).getName());
        }
    }

    public SongCollection(Type type, Object owner, String albumArtistName) {
        this.type = type;
        this.owner = owner;
        this.albumArtistName = albumArtistName;

        if (type == Type.PLAYLIST && owner != null) {
            Log.i(TAG, "Playlist name: " + ((Playlist) owner).getName());
        }
    }

    public SongCollection(String id, String name, Type type, Object owner, int displayOrder, int isEmpty) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.displayOrder = displayOrder;
        this.isEmpty = isEmpty;
    }

    public static String CREATE_TABLE_SCRIPT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + ID + " TEXT PRIMARY KEY," + NAME + " TEXT," + COVER_URL + " TEXT,"
            + COLLECTION_TYPE + " TEXT," + REF_TYPE + " TEXT," + REF_ID + " TEXT,"
            + DISPLAY_ORDER + " INTEGER," + IS_EMPTY + " INTEGER," + LAST_SONG_NAME + " TEXT," + LAST_SONG_ID + " INTEGER)";

    public enum Type {
        PLAYLIST, ALBUM, SINGLE
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Object getOwner() {
        return owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;

        reset();
    }

    public void setOwnerWithoutReset(Object owner) {
        this.owner = owner;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        if (name != null) {
            return name;
        } else {
            if (type == Type.ALBUM) {
                Album album = (Album) owner;
                if (album != null) {
                    return album.getName();
                }
            } else if (type == Type.PLAYLIST) {
                Playlist playlist = (Playlist) owner;
                if (playlist != null) {
                    return playlist.getName();
                } else {
                    Log.i(TAG, "Playlist is Null.");
                }
            } else if (type == Type.SINGLE) {
                if (owner != null) {
                    Song song = (Song) owner;

                    if (song.getAlbum() != null) {
                        return song.getAlbum().getName();
                    }
                }
            }
        }
        return "";
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getCoverUrl() {
        if (!StringUtils.isBlank(coverUrl)) {
            return coverUrl;
        } else {
            if (type == Type.ALBUM) {
                Album album = (Album) owner;
                if (album != null) {
                    return album.getArt();
                }
            } else if (type == Type.PLAYLIST) {
                Playlist playlist = (Playlist) owner;
                if (playlist != null) {
                    return playlist.getCoverUrl();
                }
            }
        }
        return "";
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public int getIsEmpty() {
        return isEmpty;
    }

    public void setIsEmpty(int isEmpty) {
        this.isEmpty = isEmpty;
    }

    public Song getLastSong() {
        return lastSong;
    }

    public void setLastSong(Song lastSong) {
        this.lastSong = lastSong;
    }

    public void initMaybe() {
        if (isInitialized) return;

        reset();
    }

    public void reset() {
        isCursor = false;

        id = getId();

        if (type == Type.PLAYLIST) {
            Playlist playlist = (Playlist) owner;

            syncNameAndCoverMaybe(playlist.getName(), playlist.getCoverUrl());
//            name = playlist.getName();
//            coverUrl = playlist.getCoverUrl();

            switch (playlist.getType()) {
                case ALL:
                    clear();
                    try {
                        songs = DBService.loadAllSongs();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    isCursor = false;
                    break;
                case ARTIST:
                    cursor = DBService.loadSongsOfArtist(Lewa.context(), playlist.getArtist().getId());
                    isCursor = true;
                    mSongCursorIndex = new SongCursorIndex(cursor);
                    break;
                case LOCAL:
                case RECENT_PLAY:
                case FAVORITE:
                case DOWNLOAD:
                    try {
                        songs = DBService.findSongsOfPlaylist(Lewa.context(), playlist.getId(), playlist.getType());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    isCursor = false;
                    break;
                default:
                    isCursor = false;
                    Collection<PlaylistSong> playlistSongs = playlist.getSongs();
                    name = playlist.getName();
                    if (songs == null) {
                        songs = new ArrayList<Song>();
                    } else {
                        songs.clear();
                    }

                    if (playlistSongs != null) {
                        for (PlaylistSong pls : playlistSongs) {
                            songs.add(pls.getSong());
                        }
                    }
                    break;
            }
        } else if (type == Type.ALBUM) {
            Album album = (Album) owner;
            clear();
            cursor = DBService.loadSongsOfAlbum(Lewa.context(), album.getId());
            isCursor = true;
            mSongCursorIndex = new SongCursorIndex(cursor);
            syncNameAndCoverMaybe(album.getName(), album.getArt());
//            name = album.getName();
//            coverUrl = album.getArt();
        } else if (type == Type.SINGLE) {
            isCursor = false;
            if (songs == null) {
                songs = new ArrayList<Song>();
            } else {
                songs.clear();
            }

            if (owner != null) {
                Song song = (Song) owner;
                try {
                    song = DBService.findSongById(song.getId(), song.getType());
                    songs.add(song);
                } catch (SQLException e) {
                    e.printStackTrace();
                    songs.add((Song) owner);
                }
            }
        }

        isInitialized = true;
        Log.i(TAG, "Init done, type = " + type.name() + ", is cursor = " + isCursor + ", cursor is " + (cursor == null ? "null and " : "not null and "));

        if (cursor != null) {
            Log.i(TAG, "Cursor is " + (cursor.isClosed() ? "closed" : "not closed"));
        }
    }

    public void syncNameAndCoverMaybe(String name, String cover) {
        if (!StringUtils.isBlank(name)) {
            this.name = name;
        }

        if (!StringUtils.isBlank(cover)) {
            this.coverUrl = cover;
        }
    }

    public int getCount() {

        initMaybe();
        if (type == Type.ALBUM && isCursor &&  null != albumArtistName) {
            List<Song> songList = getSongs();
            return songList == null ? 0 : songList.size();
        }
        
        if (isCursor) {
            return cursor.getCount();
        } else {
            return songs == null ? 0 : songs.size();
        }
    }

    public Song getSong(int position) {

        initMaybe();

        if (isCursor) {
            cursor.moveToPosition(position);

            return Song.fromMediaStore(cursor, mSongCursorIndex);
        } else {
            return songs.get(position);
        }
    }

    public List<Song> getSongs() {

        initMaybe();

        if (isCursor) {
            List<Song> songs = new ArrayList<Song>();

            if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    Song song = Song.fromMediaStore(cursor, mSongCursorIndex);
                    if (type == Type.ALBUM && null != albumArtistName) {
                        Album album = (Album) owner;
                       
                        String songArtistName = song.getArtist().getName();
                        
                        
                        if(albumArtistName.equals(songArtistName)) {
                            
                            songs.add(song);
                        }
                        Log.i(TAG, "---------------END----------------");
                    } else {
                        songs.add(song);
                    }
                    
                    cursor.moveToNext();
                }
            }

            return songs;
        } else {
            return songs;
        }
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        isInitialized = true;
        clear();
    }


    public void clear() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            isCursor = false;
        }
    }

    public void exceptData(Collection<Song> removeSongs) {
        if (isCursor) {
            songs = new ArrayList<Song>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Song song = Song.fromMediaStore(cursor, mSongCursorIndex);
                if (!removeSongs.contains(song)) {
                    songs.add(song);
                }
                cursor.moveToNext();
            }
            isCursor = false;
        }
    }

    public String getId() {
        if (!StringUtils.isBlank(id)) return id;

        StringBuffer sb = new StringBuffer();

        switch (type) {
            case SINGLE:
                sb.append("single_");
                if (owner == null) {
                    sb = null;
                } else {
                    Song song = (Song) owner;
                    sb.append(song.getType().name());
                    sb.append(song.getId());
                }
                break;
            case ALBUM:
                sb.append("album");

                if (owner == null) {
                    sb.append("0");
                } else {
                    Album album = (Album) owner;
                    sb.append(album.getId());
                }
                break;
            case PLAYLIST:
                if (owner == null) {
                    break;
                } else {
                    Playlist playlist = (Playlist) owner;

                    switch (playlist.getType()) {
                        case FAVORITE:
                            sb.append(FAVORITE_ID);
                            break;
                        case DOWNLOAD:
                            sb.append(DOWNLOAD_ID);
                            break;
                        case ALL:
                            sb.append(ALL_ID);
                            break;
                        case ARTIST:
                            sb.append("playlist_a" + playlist.getArtist().getId());
                            break;
                        case LOCAL:
                            sb.append("playlist_l" + playlist.getId());
                            break;
                        default:
                            sb.append("playlist_");

                            if (playlist.getType() == null) {
                                sb.append("o");
                            } else {
                                sb.append(playlist.getType().name());
                            }

                            sb.append("_" + playlist.getBdCode());
                            break;
                    }
                }
                break;
            default:
                sb = null;
                break;
        }

        if (sb == null) {
            id = null;
        } else {
            id = sb.toString();
        }

        return id;
    }

    public boolean equals(Object obj) {
        if (obj instanceof SongCollection) {
            SongCollection another = (SongCollection) obj;

            return getId() != null && another.getId() != null && getId().equals(another.getId());
        }

        return false;
    }

    public static SongCollection fromCursor(Cursor cursor, SongCollectionIndex songCollectionIndex) {
        if (cursor == null || cursor.isClosed() || songCollectionIndex == null) {
            return null;
        }

        SongCollection songCollection = new SongCollection();
        songCollection.setId(cursor.getString(songCollectionIndex.getIdIdx()));
        songCollection.setName(cursor.getString(songCollectionIndex.getNameIdx()));
        if (cursor.getString(songCollectionIndex.getTypeIdx()) != null) {
            songCollection.setType(Type.valueOf(cursor.getString(songCollectionIndex.getTypeIdx())));
        }
        songCollection.setCoverUrl(cursor.getString(songCollectionIndex.getCoverUrlIdx()));
        songCollection.setIsEmpty(cursor.getInt(songCollectionIndex.getIsEmptyIdx()));
        Song lastSong = new Song((long) cursor.getInt(songCollectionIndex.getLastSongIdIdx()));
        lastSong.setName(cursor.getString(songCollectionIndex.getLastSongNameIdx()));
        songCollection.setLastSong(lastSong);
        songCollection.setDisplayOrder(cursor.getInt(songCollectionIndex.getDisplayOrderIdx()));

        if (songCollection.getType() != null) {
            String refType = cursor.getString(songCollectionIndex.getRefTypeIdx());
            String refId = cursor.getString(songCollectionIndex.getRefIdIdx());
            if (songCollection.getType() == Type.ALBUM) {
                Album album;

                if (!StringUtils.isBlank(refId)) {
                    album = new Album(Long.valueOf(refId));
                } else {
                    album = new Album();
                }

                songCollection.setOwnerWithoutReset(album);

                if (!StringUtils.isBlank(refType)) {
                    try {
                        album.setType(Album.TYPE.valueOf(refType));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (songCollection.getType() == Type.SINGLE) {
                Song.TYPE st = Song.TYPE.valueOf(refType);
                songCollection.getLastSong().setType(st);
                songCollection.setOwnerWithoutReset(songCollection.getLastSong());
            } else if (songCollection.getType() == Type.PLAYLIST) {
                Playlist playlist = new Playlist();
                playlist.setName(songCollection.getName());

                if (!StringUtils.isBlank(refType)) {
                    Playlist.TYPE plt = Playlist.TYPE.valueOf(refType);
                    playlist.setType(plt);

                    if (plt != null && !StringUtils.isBlank(refId)) {
                        switch (plt) {
                            case LOCAL:
                            case RECENT_PLAY:
                            case FAVORITE:
                            case DOWNLOAD:
                            case ALL:
                                playlist.setId(Long.valueOf(refId));
                                break;
                            case ARTIST:
                                playlist.setArtist(new Artist(Long.valueOf(refId)));
                                break;
                            default:
                                playlist.setBdCode(refId);
                                break;
                        }
                    }
                }

                songCollection.setOwnerWithoutReset(playlist);
            }
        }

        return songCollection;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("id: ");
        sb.append(id);
        sb.append(", name: ");
        sb.append(name);
        sb.append(", type: ");
        sb.append(type == null ? "null" : type.name());
        sb.append(", lastSongId: ");
        sb.append(lastSong == null ? "" : lastSong.getId());
        sb.append(", lastSongName: ");
        sb.append(lastSong == null ? "" : lastSong.getName());
        sb.append(", order: ");
        sb.append(displayOrder);
        return sb.toString();
    }

    public static final String FAVORITE_ID = "4";
    public static final String DOWNLOAD_ID = "5";
    public static final String ALL_ID = "6";
}
