package com.lewa.player.db;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.music.download.DownloadStatus;
import com.lewa.Lewa;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.Album;
import com.lewa.player.model.AlbumCursorIndex;
import com.lewa.player.model.Artist;
import com.lewa.player.model.ArtistCursorIndex;
import com.lewa.player.model.OnlineSongCursorIndex;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.PlaylistCursorIndex;
import com.lewa.player.model.PlaylistSong;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.player.model.SongCollectionIndex;
import com.lewa.player.model.SongCursorIndex;
import com.lewa.player.online.DownLoadAsync;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;
import com.lewa.util.StringUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBService {

    private final static String TAG = "DBService"; //.class.getName();

    private static DBService instance = null;
    private static DbHandler dbHandler;
    private static PlayerDBHelper playerDBHelper;

    public static DBService getInstance() {
        if (instance == null) {
            instance = new DBService();
        }

        return instance;
    }

    private DBService() {
    }

    public void recreateTables() {
    }

    public static void init() {
        dbHandler = new DbHandler(Lewa.context());
        /**
         * !!this is important to force call onCreate() of dbHandler
         */
//            dbHandler.getReadableDatabase();
//        dbHandler.recreateTables();

        playerDBHelper = new PlayerDBHelper(Lewa.context(), "com_lewa_musicplayer.db", true);
//        playerDBHelper.recreateTables();
    }

    public void releaseHelper() {
        dbHandler = null;
    }

    private static Cursor getAlbumCursor(AsyncQueryHandler async, Long artistId, String filter) {
        String[] cols = new String[]{
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
        };

        Uri uri = null;
        if (artistId == null || artistId == 0) {
            uri = MediaStore.Audio.Albums.getContentUri("external");
        } else {
            uri = MediaStore.Audio.Artists.Albums.getContentUri("external", artistId);
        }

        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        }
        if (async != null) {
            async.startQuery(0, null,
                    uri,
                    cols, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
            return null;
        } else {
            return MusicUtils.query(Lewa.context(), uri,
                    cols, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        }
    }

    private static Cursor getArtistCursor(AsyncQueryHandler async, String filter) {
        String[] cols = new String[]{
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
        };

        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        }
        if (async != null) {
            async.startQuery(0, null,
                    uri,
                    cols, null, null, MediaStore.Audio.Albums._ID);

            return null;
        } else {
            return MusicUtils.query(Lewa.context(), uri,
                    cols, null, null, MediaStore.Audio.Albums._ID);
        }
    }

    private static Cursor getAudioCursor(List<Long> inIds) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        StringBuffer selectArgs = new StringBuffer();
        if (inIds != null && inIds.size() > 0) {
            selectArgs.append(MediaStore.Audio.Media._ID + " in(");
        }
        for (Long id : inIds) {
            selectArgs.append(id + ",");
        }
        if (selectArgs.length() > 0) {
            selectArgs.delete(selectArgs.length() - 1, selectArgs.length());
            selectArgs.append(")");
        }
        String selection = selectArgs.length() > 0 ? selectArgs.toString() : null;

        String durationSelection = MediaStore.Audio.Media.DURATION + " > " + 60 * 1000;
        if (MusicUtils.getIntPref(Lewa.context(), "isFilterSongs", 0) == 1) {
            if (selection != null) {
                selection += " and " + durationSelection;
            } else {
                selection = durationSelection;
            }
        }


        return MusicUtils.query(Lewa.context(), uri,
                null, selection, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    public static Cursor loadArtists() {
        return getArtistCursor(null, null);
    }

    public static int loadSongCountOfArtist(long artistId) {
        Cursor cursor = loadSongsOfArtist(Lewa.context(), artistId);

        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return count;
    }

    public static List<Artist> findArtists(Integer country) throws SQLException {
//        QueryBuilder<Artist, Long> queryBuilder = dbh.getArtistDao().queryBuilder()
//                .orderBy("initial", false);
//
//        if (country != null) {
//            Where<Artist, Long> where = queryBuilder.where().lt("id", 0);
//            return where.query();
//        } else {
//            return queryBuilder.query();
//        }

        List<Artist> artists = new ArrayList<Artist>();
        String selectQuery = "SELECT  * FROM " + Artist.TABLE_NAME;

        SQLiteDatabase db = dbHandler.getWritableDatabase();

        if (db == null) return null;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Artist artist = new Artist();
                artist.setId(cursor.getLong(0));
                artist.setName(cursor.getString(1));
                artist.setPicPath(cursor.getString(2));
                artist.setInitial(cursor.getString(3));
                artists.add(artist);
                cursor.moveToNext();
            }
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return artists;
    }

    public static Artist findArtistByName(String name) {
        Cursor cursor = null;
        try {
            cursor = MusicUtils.query(Lewa.context(), MediaStore.Audio.Artists.getContentUri("external"), null, MediaStore.Audio.Artists.ARTIST + " LIKE '%" + name + "%'", null, null);
//            cursor = getArtistCursor(null, MediaStore.Audio.Artists.ARTIST + " LIKE '%" + name + "%'");

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                ArtistCursorIndex cursorIndex = new ArtistCursorIndex(cursor);

                return Artist.fromCursor(cursor, cursorIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    public static Cursor loadAlbumsOfArtist(Long artistId) throws SQLException {
        Cursor cur = getAlbumCursor(null, artistId, null);

        return cur;
    }

    public static Cursor loadAlbums() throws SQLException {
        return loadAlbumsOfArtist(null);
    }

    public static Album loadAlbumById(Long albumId) throws SQLException {
        String[] cols = new String[]{
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
        };

        Cursor cursor = null;
        Uri uri = MediaStore.Audio.Albums.getContentUri("external");

        cursor = MusicUtils.query(Lewa.context(), uri,
                cols, MediaStore.Audio.Albums._ID + "=?", new String[]{String.valueOf(albumId)}, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

        AlbumCursorIndex albumCursorIndex = new AlbumCursorIndex(cursor);
        Album album = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            album = Album.fromCursor(cursor, albumCursorIndex);
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return album;
    }

    public static int countSongs() throws SQLException {
        String selection = buildSongSelection(null);
        SQLiteDatabase db = dbHandler.getReadableDatabase();
//        Cursor cursor = db.rawQuery(countQuery, null);


        Cursor cursor = MusicUtils.query(Lewa.context(), MediaStore.Audio.Media.getContentUri("external"), null, selection, null, null);
        int count = 0;
        if (cursor != null && !cursor.isClosed()) {
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }

    public static void matchDownload() {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) return;

        //String sql = "select * from " + Song.TABLE_NAME + " where " + Song.DOWNLOAD_STATUS + "! = " + DownloadStatus.STATUS_SUCCESS;
		String sql = "select * from " + Song.TABLE_NAME + " where " + Song.DOWNLOAD_STATUS + " == " + DownloadStatus.STATUS_SUCCESS;	

        Cursor cursor = null;
        Cursor mediaStoreCursor = null;
        try {
            cursor = db.rawQuery(sql, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                OnlineSongCursorIndex cursorIndex = new OnlineSongCursorIndex(cursor);

                while (!cursor.isAfterLast()) {
                    Song onlineSong = Song.fromCursor(cursor, cursorIndex);
                    Song localSong = findSongByPathFromMediaStore(onlineSong.getPath());

                    Log.i(TAG, "Online id: " + onlineSong.getId() + ", local id:" + (localSong == null ? "" : localSong.getId()));

                    if (localSong != null) {
                        onlineSong.setLocalId(localSong.getId());
                        onlineSong.setDownloadStatus(DownloadStatus.STATUS_SUCCESS);
                        saveSong(onlineSong);

                        //save to download playlist
                        PlaylistSong playlistSong = new PlaylistSong();
                        playlistSong.setSongType(Song.TYPE.LOCAL);
                        playlistSong.setSong(new Song(localSong.getId()));
                        playlistSong.setPlaylist(new Playlist(Playlist.DOWNLOAD_ID));
                        playlistSong.setCreateTime(new Date());
                        savePlaylistSong(playlistSong, Playlist.DOWNLOAD_ID, false);
                    }

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            if (mediaStoreCursor != null && !mediaStoreCursor.isClosed()) {
                mediaStoreCursor.close();
            }
        }
    }

    public static Song findSongByPath(String path) {
        if (StringUtils.isBlank(path)) return null;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) return null;

        String sql = "select * from " + Song.TABLE_NAME + " where " + Song.PATH + "='" + path+"'";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                OnlineSongCursorIndex cursorIndex = new OnlineSongCursorIndex(cursor);
                return Song.fromCursor(cursor, cursorIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return null;
    }

    public static void downloadDone(String path, String localId) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null) return;

        try {
            ContentValues values = new ContentValues();
            values.put(Song.LOCAL_ID, localId);
            values.put(Song.DOWNLOAD_STATUS, DownloadStatus.STATUS_SUCCESS);
            db.update(Song.TABLE_NAME, values, Song.PATH + " = ?", new String[]{path});

            /*PlaylistSong playlistSong = new PlaylistSong();
            playlistSong.setSongType(Song.TYPE.LOCAL);
            Song song=new Song(Long.valueOf(localId),Song.TYPE.LOCAL);
            playlistSong.setSong(song);
            playlistSong.setPlaylist(new Playlist(Playlist.DOWNLOAD_ID));
            playlistSong.setCreateTime(new Date());
            savePlaylistSong(playlistSong, Playlist.DOWNLOAD_ID, true);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	 public static void saveDownloadingSong(long song_id){
    	 SQLiteDatabase db = dbHandler.getWritableDatabase();
         if (db == null) return;
         try {
			PlaylistSong playlistSong=new PlaylistSong();
			 playlistSong.setSongType(Song.TYPE.ONLINE);
			 playlistSong.setSong(new Song(song_id, Song.TYPE.ONLINE));
			 playlistSong.setPlaylist(new Playlist(Playlist.DOWNLOAD_ID));
			 playlistSong.setCreateTime(new Date());
			 savePlaylistSong(playlistSong, Playlist.DOWNLOAD_ID,true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static void matchSongs(List<Song> onlineSongs) {
        if (onlineSongs == null || onlineSongs.size() == 0) return;

        StringBuffer selectArgs = new StringBuffer(Song.ID + " in(");
        for (int i = 0; i < onlineSongs.size(); i++) {
            Song song = onlineSongs.get(i);
            if(song != null) {
                selectArgs.append(song.getId());
                if (i != onlineSongs.size() - 1) {
                    selectArgs.append(",");
                }
            }
            
        }
        selectArgs.append(")");

        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) return;

        String sql = "select " + Song.ID + "," + Song.LOCAL_ID + " from " + Song.TABLE_NAME + " where " + selectArgs;

        Cursor cursor = null;
        Cursor mediaStoreCursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            List<Long> localIds = new ArrayList<Long>();
            Map<Long, Long> idMap = new HashMap<Long, Long>();

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    long localId = cursor.getLong(1);

                    if (localId > 0) {
                        localIds.add(cursor.getLong(1));
                        idMap.put(localId, cursor.getLong(0));
                    }

                    cursor.moveToNext();
                }
            }

            mediaStoreCursor = getAudioCursor(localIds);
            Map<Long, Song> localSongsMap = new HashMap<Long, Song>();
            Set<Song> localSongs = new HashSet<Song>();

            if (mediaStoreCursor != null && mediaStoreCursor.getCount() > 0) {
                mediaStoreCursor.moveToFirst();
                SongCursorIndex songCursorIndex = new SongCursorIndex(mediaStoreCursor);

                while (!mediaStoreCursor.isAfterLast()) {
                    Song song = Song.fromMediaStore(mediaStoreCursor, songCursorIndex);
                    localSongsMap.put(song.getId(), song);
                    localSongs.add(song);

                    mediaStoreCursor.moveToNext();
                }
            }

            if (localSongsMap.size() > 0) {
                for (Song song : localSongs) {
                    long onlineId = idMap.get(song.getId());
                    int onlineSongIndex = onlineSongs.indexOf(new Song(onlineId, Song.TYPE.ONLINE));
                    onlineSongs.set(onlineSongIndex, song);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            if (mediaStoreCursor != null && !mediaStoreCursor.isClosed()) {
                mediaStoreCursor.close();
            }
        }
    }

    public static List<Song> loadAllSongs() throws SQLException {
        String[] paths = MusicUtils.getFolderPath(Lewa.context());
        List<Long> ids = new ArrayList<Long>();
        for (String path : paths) {
            ids.addAll(MusicUtils.getFolderAudioId(Lewa.context(), path, 0));
        }
        Cursor cursor = getAudioCursor(ids);
        List<Song> songs = bumpSongsFromCursor(cursor);

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return songs;
    }

    public static List<Song> findSongsByIds(List<Long> ids) throws SQLException {
        Cursor cursor = null;

        try {
            cursor = getAudioCursor(ids);
            return bumpSongsFromCursor(cursor);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Song>();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private static List<Song> bumpSongsFromCursor(Cursor cursor) {
        List<Song> songs = new ArrayList<Song>();

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            SongCursorIndex songCursorIndex = new SongCursorIndex(cursor);
            while (!cursor.isAfterLast()) {
                songs.add(Song.fromMediaStore(cursor, songCursorIndex));
                cursor.moveToNext();
            }
        }

        return songs;
    }

    public static List<Song> findSongsByName(String name) throws SQLException {
        Cursor cursor = MusicUtils.query(Lewa.context(), MediaStore.Audio.Media.getContentUri("external"), null, MediaStore.Audio.Media.TITLE + " LIKE '%" + name + "%' OR " + MediaStore.Audio.Media.ARTIST + " LIKE '%" + name + "%'", null, null);
        List<Song> songs = bumpSongsFromCursor(cursor);

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return songs;
    }

    public static List<Song> findSongsOfPlaylist(Context context, Long playlistId, Playlist.TYPE type) throws SQLException {
        String selectQuery = "SELECT  a.* FROM " + PlaylistSong.TABLE_NAME + " a WHERE a." + PlaylistSong.PLAYLIST_ID + "=" + playlistId + " order by " + PlaylistSong.LAST_UPDATE_TIME + " desc";
        return findSongsOfPlaylist(context, selectQuery);
    }

    public static List<Song> findSongsOfPlaylist(Context context, Long playlistId) throws SQLException {
        String  selectQuery = "SELECT  a.* FROM " + PlaylistSong.TABLE_NAME + " a WHERE a." + PlaylistSong.PLAYLIST_ID + "=" + playlistId;
        return findSongsOfPlaylist(context, selectQuery);
    }

    public static List<Song> findSongsOfPlaylist(Context context, String selectQuery) throws SQLException {
        List<Song> songs = new ArrayList<Song>();
        List<PlaylistSong> playlistSongs = new ArrayList<PlaylistSong>();
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) return null;

        Cursor cursor = null;
        Cursor localSongCursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    playlistSongs.add(PlaylistSong.fromCursor(cursor));
                    cursor.moveToNext();
                }
            } else {
                return new ArrayList<Song>();
            }

            List<Long> localSongIds = new ArrayList<Long>();
            List<Long> onlineSongIds = new ArrayList<Long>();
            Map<String, Song> songsMap = new HashMap<String, Song>();
            Map<Long, Long> idMap = new HashMap<Long, Long>();

            for (PlaylistSong playlistSong : playlistSongs) {
                switch (playlistSong.getSongType()) {
                    case LOCAL:
                        localSongIds.add(playlistSong.getSong().getId());
                        break;
                    default:
                        onlineSongIds.add(playlistSong.getSong().getId());
                        break;
                }
            }

            List<Song> onlineSongs = findOnlineSongs(onlineSongIds);

            for (Song song : onlineSongs) {
                songsMap.put(Song.TYPE.ONLINE.name() + song.getId(), song);
                if (song.getLocalId() > 0) {
                    localSongIds.add(song.getLocalId());
                    idMap.put(song.getId(), song.getLocalId());
                }
            }

            localSongCursor = getAudioCursor(localSongIds);

            if (localSongCursor != null && localSongCursor.getCount() > 0) {
                localSongCursor.moveToFirst();
                SongCursorIndex songCursorIndex = new SongCursorIndex(localSongCursor);

                while (!localSongCursor.isAfterLast()) {
                    Song song = Song.fromMediaStore(localSongCursor, songCursorIndex);
                    songsMap.put(Song.TYPE.LOCAL.name() + song.getId(), song);

                    localSongCursor.moveToNext();
                }
            }

            for (PlaylistSong pls : playlistSongs) {
                Song song = null;
                if (pls.getSongType() == Song.TYPE.LOCAL) {
                    song = songsMap.get(Song.TYPE.LOCAL.name() + pls.getSong().getId());
                } else {
                    Long localId = idMap.get(pls.getSong().getId());
                    song = songsMap.get(Song.TYPE.LOCAL.name() + localId);

                    if (song == null) {
                        song = songsMap.get(Song.TYPE.ONLINE.name() + pls.getSong().getId());
                    }
                }
                if (song != null&&!songs.contains(song)) {
                    songs.add(song);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (localSongCursor != null && !localSongCursor.isClosed()) {
                localSongCursor.close();
            }
        }

        return songs;
    }

    public static List<Song> findOnlineSongs(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<Song>();
        }

        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) return null;

        StringBuffer selectArgs = new StringBuffer(Song.ID + " in(");
        for (int i = 0; i < ids.size(); i++) {
            selectArgs.append(ids.get(i));
            if (i != ids.size() - 1) {
                selectArgs.append(",");
            }
        }
        selectArgs.append(")");

        Cursor cursor = null;
        List<Song> songs = new ArrayList<Song>();

        try {
            cursor = db.query(Song.TABLE_NAME, null, selectArgs.toString(),
                    null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                OnlineSongCursorIndex cursorIndex = new OnlineSongCursorIndex(cursor);

                while (!cursor.isAfterLast()) {
                    songs.add(Song.fromCursor(cursor, cursorIndex));
                    cursor.moveToNext();
                }
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return songs;
    }

    public static Song findSongById(Long id) throws SQLException {
        if (id > 0) {
            return findSongById(id, Song.TYPE.LOCAL);
        } else if (id == 0) {
            return null;
        } else {
            return findSongById(-1 * id, Song.TYPE.ONLINE);
        }
    }

    /**
     * TODO check song type to decide read from mediastore or sqllite.
     *
     * @param id
     * @return
     * @throws SQLException
     */
    public static Song findSongById(Long id, Song.TYPE type) throws SQLException {
        if (id == null) {
            return null;
        }

        Song song = null;

        if (type == Song.TYPE.LOCAL) {
            song = findSongFromMediaStore(id);
        } else {
            SQLiteDatabase db = dbHandler.getReadableDatabase();
            if (db == null) return null;

            Cursor cursor = db.query(Song.TABLE_NAME, null, Song.ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                OnlineSongCursorIndex cursorIndex = new OnlineSongCursorIndex(cursor);
                song = Song.fromCursor(cursor, cursorIndex);
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return song;
    }


    public static void mappingOnlineAndLocalSong(long onlineSongId, long localSongId) {
        if (onlineSongId <= 0 || localSongId <= 0) {
            return;
        }

        try {
            Song savedSong = findSongById(onlineSongId, Song.TYPE.ONLINE);

            if (savedSong != null) {
                savedSong.setLocalId(localSongId);
                savedSong.setDownloadStatus(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveSong(Song song) throws SQLException {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null) return;

        Log.i(TAG, "save song:" + song.toString());

        ContentValues values = new ContentValues();
        values.put(Song.ID, song.getId());
        values.put(Song.NAME, song.getName());
        values.put(Song.F_TYPE, song.getType().name());
        if (song.getArtist() != null) {
            values.put(Song.ARTIST_NAME, song.getArtist().getName());
        }
        if (song.getAlbum() != null && song.getAlbum().getName() != null)
            values.put(Song.ALBUM_NAME, song.getAlbum().getName());

        if (!StringUtils.isBlank(song.getPath())) {
            values.put(Song.PATH, song.getPath());
        }
        values.put(Song.BITRATE, song.getBitrate());
        values.put(Song.IS_LOSSLESS, String.valueOf(song.isLossless()));

        values.put(Song.LOCAL_ID, song.getLocalId());
        values.put(Song.DOWNLOAD_STATUS, song.getDownloadStatus());

        if (song.getId() != null) {
            Song savedSong = findSongById(song.getId(), song.getType());

            if (savedSong != null) {
                db.update(Song.TABLE_NAME, values, Song.ID + " = ?", new String[]{String.valueOf(song.getId())});
            } else {
                db.insert(Song.TABLE_NAME, null, values);
            }
        } else {
            //TODO generate id and save
        }

    }

    public static void updateSong(Song song) throws SQLException {
        if (song.getId() == null) return;
        ContentResolver resolver = Lewa.context().getContentResolver();
        Uri trackuri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
        try {
            ContentValues values = new ContentValues(3);
            values.put(MediaStore.Audio.Media.TITLE, song.getName());
            values.put(MediaStore.Audio.Media.ARTIST, song.getArtist().getName());
            values.put(MediaStore.Audio.Media.ALBUM, song.getAlbum().getName());
            resolver.update(trackuri, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeSongFile(Song song) throws SQLException {
        if (song.getType() == Song.TYPE.ONLINE) {
            SQLiteDatabase db = dbHandler.getWritableDatabase();
            if (db == null) return;

            db.delete(Song.TABLE_NAME, Song.ID + " = ?", new String[]{String.valueOf(song.getId())});
        } else {
            File songFile = new File(song.getPath());
            if (songFile.exists()) {
                songFile.delete();
            }
        }

    }
    
    public static void removeDownloadSong(long localId){
    	SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null) return;
        Cursor c = db.query(Song.TABLE_NAME, new String[]{"id"}, "local_id = ?", new String[]{String.valueOf(localId)}, null, null, null);
        if(c!=null && c.moveToFirst()){
        	db.delete(PlaylistSong.TABLE_NAME, "playlist_id = ? and song_id = ?", new String[]{Playlist.DOWNLOAD_ID+"",c.getLong(0)+""});
        }
        closeCursor(c);
        updatePlaylistSongNumber(Playlist.DOWNLOAD_ID);
    }

    public static void removeSongFromMediaStore(Song song) throws SQLException {
        if (song.getId() == null) return;
        ContentResolver resolver = Lewa.context().getContentResolver();
        try {
            resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + "=?", new String[]{song.getId() + ""});
            removeDownloadSong(song.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*if (song.getAlbum() != null && song.getAlbum().getId() != null) {
            Album album = loadAlbumById(song.getAlbum().getId());
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Albums.NUMBER_OF_SONGS, album.getSongNum() - 1);
            ContentResolver albumResolver = Lewa.context().getContentResolver();
            albumResolver.update(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Albums._ID + "=?", new String[]{String.valueOf(album.getId())});
        }*/
    }

    public static void deleteLocalSong(Song song) throws SQLException {
        if (song.getId() == null) return;
        ContentResolver resolver = Lewa.context().getContentResolver();
        try {
            resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + "=?", new String[]{song.getId() + ""});

            if (!StringUtils.isBlank(song.getPath())) {
                File songFile = new File(song.getPath());
                if (songFile.exists()) {
                    songFile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Song findSongFromMediaStore(Long songId) {
        if (songId == null || songId == 0) {
            return null;
        } else {
            Cursor cursor = null;
            try {
                cursor = Lewa.context().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media._ID + "=?", new String[]{songId + ""}, null);
                SongCursorIndex songCursorIndex = new SongCursorIndex(cursor);

                Song song = null;
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    song = Song.fromMediaStore(cursor, songCursorIndex);
                }

                if (song != null) {
                    Log.i(TAG, "Get song from media store, id: " + songId + ", path: " + song.getPath());
                } else {
                    Log.i(TAG, "Not found song with id: " + songId);
                }

                return song;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
    }

    public static Song findSongByPathFromMediaStore(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        } else {
            Cursor cursor = null;
            try {
                cursor = Lewa.context().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + "=?", new String[]{path}, null);
                SongCursorIndex songCursorIndex = new SongCursorIndex(cursor);

                Song song = null;
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    song = Song.fromMediaStore(cursor, songCursorIndex);
                }

                if (song != null) {
                    Log.i(TAG, "Get song from media store, id: " + song.getId() + ", path: " + song.getPath());
                } else {
                    Log.i(TAG, "Not found song with id: " + song.getId());
                }

                return song;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
    }

    public static int loadSongCountOfAlbum(Long albumId) {
        Cursor cursor = loadSongsOfAlbum(Lewa.context(), albumId);
        int count = cursor.getCount();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return count;
    }

    public static int loadSongCountOfAlbum(Long albumId, String artistName) {
        Cursor cursor = loadSongsOfAlbum(Lewa.context(), albumId);
        SongCursorIndex mSongCursorIndex = new SongCursorIndex(cursor);
        int count = cursor.getCount();
        int tmp = 0;
        if (cursor != null && !cursor.isClosed()) {
            if(count > 0) {
                cursor.moveToFirst();
                 while (!cursor.isAfterLast()) {
                    Song song = Song.fromMediaStore(cursor, mSongCursorIndex);
                    if(artistName.equals(song.getArtist().getName())) {
                        tmp++;
                    }
                    cursor.moveToNext();
                 }
            }
            cursor.close();
            
        }
        //Log.i(TAG, "tmp = " + tmp);
        //Log.i(TAG, "count = " + count);
        //return count;
        return tmp;
    }

    public static Cursor loadSongsOfAlbum(Context context, Long albumId) {
        if (albumId == null || albumId == 0) {
            return MusicUtils.query(context, MediaStore.Audio.Media.getContentUri("external"), null, null, null, null);
        } else {
            String selection = buildSongSelection(MediaStore.Audio.Media.ALBUM_ID + "=?");

            return MusicUtils.query(context, MediaStore.Audio.Media.getContentUri("external"), null, selection, new String[]{albumId + ""}, null);
        }
    }

    public static Cursor loadSongsOfArtist(Context context, Long artistId) {
        if (artistId == null || artistId == 0) {
            return MusicUtils.query(context, MediaStore.Audio.Media.getContentUri("external"), null, null, null, null);
        } else {
            String selection = buildSongSelection(MediaStore.Audio.Media.ARTIST_ID + "=?");

            return MusicUtils.query(context, MediaStore.Audio.Media.getContentUri("external"), null, selection, new String[]{artistId + ""}, null);
        }
    }

    private static String buildSongSelection(String extraSelection) {
        String[] paths = MusicUtils.getFolderPath(Lewa.context());
        List<Long> ids = new ArrayList<Long>();
        for (String path : paths) {
            ids.addAll(MusicUtils.getFolderAudioId(Lewa.context(), path, 0));
        }
        StringBuffer selectArgs = new StringBuffer();
        if (ids != null && ids.size() > 0) {
            selectArgs.append(MediaStore.Audio.Media._ID + " in(");
        }
        for (Long id : ids) {
            selectArgs.append(id + ",");
        }
        if (selectArgs.length() > 0) {
            selectArgs.delete(selectArgs.length() - 1, selectArgs.length());
            selectArgs.append(")");
        }
        String selection;

        if (!StringUtils.isBlank(extraSelection)) {
            selection = selectArgs.length() > 0 ? extraSelection + " and " + selectArgs.toString() : extraSelection;
        } else {
            selection = selectArgs.length() > 0 ? selectArgs.toString() : "";
        }

        String durationSelection = MediaStore.Audio.Media.DURATION + " > " + 60 * 1000;
        if (MusicUtils.getIntPref(Lewa.context(), "isFilterSongs", 0) == 1) {

            if (StringUtils.isBlank(selection)) {
                selection = durationSelection;
            } else {
                selection += " and " + durationSelection;
            }
        }

        return selection;
    }

    public static List<Playlist> findPlaylistsForBrowse() throws SQLException {
        String selectQuery = "SELECT  * FROM " + Playlist.TABLE_NAME + " where " + Playlist.F_TYPE + " in ('"
                + Playlist.TYPE.ONLINE_CATEGORY.name() + "','" + Playlist.TYPE.TOP_LIST_CATEGORY.name() + "','" + Playlist.TYPE.ALL_STAR_CATEGORY.name()
                + "') order by " + Playlist.PRIORITY + "," + Playlist.DISPLAY_ORDER + "," + Playlist.CREATE_TIME + " desc";

        return findPlaylistsInternal(selectQuery, null);
    }

    public static List<Playlist> findPlaylistsForAddTo() throws SQLException {
        String selectQuery = "SELECT  * FROM " + Playlist.TABLE_NAME + " where " + Playlist.F_TYPE + " in ('"
                + Playlist.TYPE.FAVORITE.name() + "','" + Playlist.TYPE.LOCAL.name()
                + "') order by " + Playlist.PRIORITY + "," + Playlist.DISPLAY_ORDER + "," + Playlist.CREATE_TIME + " desc";

        return findPlaylistsInternal(selectQuery, null);
    }

    public static List<Playlist> findPlaylistsForMine() throws SQLException {
        String selectQuery = "SELECT  * FROM " + Playlist.TABLE_NAME + " where " + Playlist.F_TYPE + " in ('"
                + Playlist.TYPE.FAVORITE.name() + "','" + Playlist.TYPE.RECENT_PLAY.name() + "','" + Playlist.TYPE.DOWNLOAD.name() + "','" + Playlist.TYPE.LOCAL.name()
                + "') order by " + Playlist.PRIORITY + "," + Playlist.DISPLAY_ORDER + "," + Playlist.CREATE_TIME + " desc";

        return findPlaylistsInternal(selectQuery, null);
    }
    //pr939694 add by wjhu begin
    //to get all userdefined playlists from DB
	public static List<Playlist> findPlaylistsForUserDefined()
			throws SQLException {
		String selectQuery = "SELECT  * FROM " + Playlist.TABLE_NAME
				+ " where " + Playlist.F_TYPE + " in ('"
				+ Playlist.TYPE.LOCAL.name() + "') order by "
				+ Playlist.PRIORITY + "," + Playlist.DISPLAY_ORDER + ","
				+ Playlist.CREATE_TIME + " desc";

		return findPlaylistsInternal(selectQuery, null);
	}
	//pr939694 add by wjhu end

    public static Playlist findPlaylistForDownload() throws SQLException {
        String selectQuery = "select * from "  + Playlist.TABLE_NAME + " where " + Playlist.F_TYPE + "=?" ;//+ Playlist.TYPE.DOWNLOAD.name();
		List<Playlist> lists = findPlaylistsInternal(selectQuery, new String[] {Playlist.TYPE.DOWNLOAD.name()});
		if(null != lists && lists.size() > 0) {
			return lists.get(0);
		}
        return null;
    }

    private static List<Playlist> findPlaylistsInternal(String rawQuery, String[] selectionArgs) {
        List<Playlist> playlists = new ArrayList<Playlist>();
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) return null;

        Cursor cursor = db.rawQuery(rawQuery, selectionArgs);

        if (cursor != null && cursor.getCount() > 0) {
            PlaylistCursorIndex cursorIndex = new PlaylistCursorIndex(cursor);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                playlists.add(Playlist.fromCursor(cursor, cursorIndex));
                cursor.moveToNext();
            }
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

		for(Playlist playList : playlists) {
			List<Song> songs = null;
			try {
				songs = findSongsOfPlaylist(Lewa.context(), playList.getId(), playList.getType());
				int songsSize = songs.size();
				if(songsSize != playList.getSongNum()) {
					ContentValues values = new ContentValues();
					values.put(Playlist.SONG_NUM, songsSize);
            		db.update(Playlist.TABLE_NAME, values, Playlist.ID + " = ?", new String[]{playList.getId().toString()});
					playList.setSongNum(songsSize);
				}
            	
			} catch (SQLException e) {
                e.printStackTrace();
            }
		}

        return playlists;
    }

    public static Playlist findSinglePlaylist(Playlist.TYPE type) {
        Playlist playlist = null;
        SQLiteDatabase db = dbHandler.getReadableDatabase();

        if (db == null) {
            return null;
        }
        Cursor cursor = db.query(Playlist.TABLE_NAME, null, Playlist.F_TYPE + "=?",
                new String[]{String.valueOf(type.name())}, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            playlist = new Playlist();
            playlist.setId(cursor.getLong(0));
            playlist.setName(cursor.getString(1));
            playlist.setCoverUrl(cursor.getString(2));
            if (cursor.getString(3) != null) {
                playlist.setType(Playlist.TYPE.valueOf(cursor.getString(3)));
            }
            playlist.setSongNum(cursor.getInt(6));
            //TODO set other fields
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return playlist;
    }

    public static List<Playlist> findPlaylist(Playlist.TYPE type) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) {
            return null;
        }
        List<Playlist> playlists = new ArrayList<Playlist>();
        Cursor cursor = db.query(Playlist.TABLE_NAME, null, Playlist.F_TYPE + "=?",
                new String[]{String.valueOf(type.name())}, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            PlaylistCursorIndex cursorIndex = new PlaylistCursorIndex(cursor);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                playlists.add(Playlist.fromCursor(cursor, cursorIndex));
                cursor.moveToNext();
            }
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return playlists;
    }

    public static void removeCoverForPlaylist(Playlist playlist) {
    	LewaUtils.logE(TAG, "removeCoverForPlaylist");
        if (playlist.getId() == null) {
            return;
        }
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) return;

        ContentValues values = new ContentValues();
        values.put(Playlist.COVER_URL, playlist.getCoverUrl());
        db.update(Playlist.TABLE_NAME, values, Playlist.ID + " = ?", new String[]{String.valueOf(playlist.getId())});
    }

    public static Playlist findPlaylist(Long id) throws SQLException {
        if (id == null) {
            return null;
        }

        Playlist playlist = null;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) return null;

        Cursor cursor = db.query(Playlist.TABLE_NAME, new String[]{Playlist.ID,
                Playlist.NAME, Playlist.F_TYPE, Playlist.COVER_URL, Playlist.CREATE_TIME,Playlist.SONG_NUM}, Album.ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            playlist = new Playlist();
            playlist.setId(cursor.getLong(0));
            playlist.setName(cursor.getString(1));

            if (cursor.getString(2) != null) {
                playlist.setType(Playlist.TYPE.valueOf(cursor.getString(2)));
            }
            playlist.setCoverUrl(cursor.getString(3));
            playlist.setSongNum(cursor.getInt(5));
            cursor.close();
            //TODO set other fields
        }
        return playlist;
    }

    public static Playlist findPlaylistWithSongs(Long playlistId, boolean fetchSongDetail) throws SQLException {
        if (playlistId == null) {
            return null;
        }

        Playlist playlist = findPlaylist(playlistId);

        if (playlist == null) {
            return null;
        }

        playlist.setSongs(new ArrayList<PlaylistSong>());
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) return null;
        String query = "select " + PlaylistSong.ID + ", " + PlaylistSong.PLAYLIST_ID + ", " + PlaylistSong.SONG_ID + ", "
                + PlaylistSong.SONG_TYPE + " from " + PlaylistSong.TABLE_NAME + " where " + PlaylistSong.PLAYLIST_ID
                + " = ? order by " + PlaylistSong.SONG_TYPE;
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(playlistId)});

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                PlaylistSong pls = PlaylistSong.fromCursor(cursor);

                if (fetchSongDetail) {
                    pls.setSong(findSongById(pls.getSong().getId(), pls.getSong().getType()));
                }

                playlist.getSongs().add(pls);
                cursor.moveToNext();
            }
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return playlist;
    }

    public static void removePlaylist(Playlist playlist) throws SQLException {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null) return;
        db.delete(Playlist.TABLE_NAME, Album.ID + " = ?", new String[]{String.valueOf(playlist.getId())});
        db.delete(PlaylistSong.TABLE_NAME, PlaylistSong.PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlist.getId())});
    }

    public static void removePlaylistSong(Long playlistId, Song song) throws SQLException {
        Log.i(TAG, "Delete playlist song: pid" + playlistId + ", song > " + song.getType().name() + "-" + song.getId());
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null || song == null) return;
        db.delete(PlaylistSong.TABLE_NAME, PlaylistSong.SONG_ID + " = ? and " + PlaylistSong.PLAYLIST_ID + "=?", new String[]{String.valueOf(song.getId()), String.valueOf(playlistId)});

        if (song.getType() == Song.TYPE.LOCAL) {
            Long onlineId = findSongByLocalId(String.valueOf(song.getId()));
            db.delete(PlaylistSong.TABLE_NAME, PlaylistSong.SONG_ID + " = ? and " + PlaylistSong.PLAYLIST_ID + "=?", new String[]{String.valueOf(onlineId), String.valueOf(playlistId)});
        }
    }

    private static Long findSongByLocalId(String localId) {
        Cursor cursor = null;

        try {
            SQLiteDatabase db = dbHandler.getReadableDatabase();
            if (db == null) return null;
            cursor = db.query(Song.TABLE_NAME, new String[]{Song.ID}, Song.LOCAL_ID + "=?", new String[]{localId}, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                Long onlineId = cursor.getLong(0);
                return onlineId;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static void updatePlaylistSongNumber(Long playlistId) {
    	LewaUtils.logE(TAG, "updatePlaylistSongNumber");
        if (playlistId == null) return;
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null) return;

        String rawQuery = "select 1 from " + PlaylistSong.TABLE_NAME + " where " + PlaylistSong.PLAYLIST_ID + " = " + playlistId.toString();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(rawQuery, null);
            int count = 0;

            if (cursor != null) {
                count = cursor.getCount();
            }
            ContentValues values = new ContentValues();
            values.put(Playlist.SONG_NUM, count);
            db.update(Playlist.TABLE_NAME, values, Playlist.ID + " = ?", new String[]{playlistId.toString()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static boolean isFavorited(Song song) throws SQLException {
        if (song != null) {
            if (song.getId() == null || song.getType() == null) {
                Log.e(TAG, "Bad song info when check if is favorited, id: " + song.getId() + ", type: " + song.getType());
                return false;
            }

            SQLiteDatabase db = dbHandler.getReadableDatabase();
            if (db == null) return false;

            String rawQuery = "select a." + Playlist.ID + " from " + Playlist.TABLE_NAME + " a, " + PlaylistSong.TABLE_NAME + " b WHERE a." + Playlist.ID + "=b." + PlaylistSong.PLAYLIST_ID
                    + " and a." + Playlist.F_TYPE + "=? and b." + PlaylistSong.SONG_ID + " =? and b." + PlaylistSong.SONG_TYPE + " = ?";
            Cursor cursor = db.rawQuery(rawQuery, new String[]{Playlist.TYPE.FAVORITE.name(), String.valueOf(song.getId()), song.getType().name()});
            int count = 0;

            if (cursor != null) {
                count = cursor.getCount();
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            return count > 0;
        }
        return false;
    }

    public static void updatePlaylistWithoutSongs(Playlist playlist, boolean isUpdateCoverUrl) throws SQLException {
    	LewaUtils.logE(TAG, "updatePlaylistWithoutSongs has boolean ");
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null) return;

        if (playlist.getCreateTime() == null) {
            playlist.setCreateTime(new Date());
        }

        ContentValues values = new ContentValues();
        values.put(Playlist.NAME, playlist.getName());
        if (isUpdateCoverUrl) {
            values.put(Playlist.COVER_URL, playlist.getCoverUrl());
        }
        values.put(Playlist.SONG_NUM, playlist.getSongNum());
        if (playlist.getType() != null) {
            values.put(Playlist.F_TYPE, playlist.getType().name());
        }
        Long playlistId = playlist.getId();

        if (playlistId != null) {
            db.update(Playlist.TABLE_NAME, values, Playlist.ID + " = ?", new String[]{String.valueOf(playlist.getId())});
        }
    }

    public static void updatePlaylistWithoutSongs(Playlist playlist) throws SQLException {
    	LewaUtils.logE(TAG, "updatePlaylistWithoutSongs");
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null) return;

        if (playlist.getCreateTime() == null) {
            playlist.setCreateTime(new Date());
        }

        ContentValues values = new ContentValues();
        values.put(Playlist.NAME, playlist.getName());
        values.put(Playlist.COVER_URL, playlist.getCoverUrl());
        values.put(Playlist.SONG_NUM, playlist.getSongNum());
        if (playlist.getType() != null) {
            values.put(Playlist.F_TYPE, playlist.getType().name());
        }
        Long playlistId = playlist.getId();

        if (playlistId != null) {
            db.update(Playlist.TABLE_NAME, values, Playlist.ID + " = ?", new String[]{String.valueOf(playlist.getId())});
            updatePlaylistSongNumber(playlistId);
        }
    }

    public static boolean savePlaylistSong(PlaylistSong playlistSong, Long playlistId, boolean updateSongNum) throws SQLException {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null) return false;

        if (playlistSong.getSong() == null || playlistSong.getSong().getId() == null || playlistSong.getSong().getType() == null) {
        	LewaUtils.logE(TAG, "savePlaylistSong song or id or type is null and return");
            return true;
        }

        String selectQuery = "SELECT " + PlaylistSong.ID + " FROM " + PlaylistSong.TABLE_NAME + " a WHERE a." + PlaylistSong.PLAYLIST_ID + "=" + playlistId + " and a." + PlaylistSong.SONG_ID + "=" + playlistSong.getSong().getId() + " and a." + PlaylistSong.SONG_TYPE + "='" + playlistSong.getSong().getType().name() + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor == null) {
        	LewaUtils.logE(TAG, "savePlaylistSong cursor is null and return");
        	return false;
        }

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Long id = cursor.getLong(0);
            cursor.close();

            ContentValues plsValues = new ContentValues();
            plsValues.put(PlaylistSong.LAST_UPDATE_TIME, new Date().getTime());
            db.update(PlaylistSong.TABLE_NAME, plsValues, PlaylistSong.ID + " = ?", new String[]{id.toString()});
            return false;
        } else {
            ContentValues plsValues = new ContentValues();
            plsValues.put(PlaylistSong.PLAYLIST_ID, playlistId);
            plsValues.put(PlaylistSong.SONG_ID, playlistSong.getSong().getId());
            plsValues.put(PlaylistSong.SONG_TYPE, playlistSong.getSong().getType().name());
            plsValues.put(PlaylistSong.CREATE_TIME, new Date().getTime());
            plsValues.put(PlaylistSong.LAST_UPDATE_TIME, new Date().getTime());
            db.insert(PlaylistSong.TABLE_NAME, null, plsValues);
            if (playlistSong.getSong().getType() == Song.TYPE.ONLINE) {
                //insert to song table
                saveSong(playlistSong.getSong());
            }
            cursor.close();

            if (updateSongNum) {
                updatePlaylistSongNumber(playlistId);
            }
            return true;
        }


    }

    public static void savePlaylistWithSong(Playlist playlist) throws SQLException {
    	LewaUtils.logE(TAG, "savePlaylistWithSong");
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        if (db == null) return;

        if (playlist.getCreateTime() == null) {
            playlist.setCreateTime(new Date());
        }

        ContentValues values = new ContentValues();
        values.put(Playlist.NAME, playlist.getName());
        values.put(Playlist.COVER_URL, playlist.getCoverUrl());
        values.put(Playlist.SONG_NUM, playlist.getSongNum());
        values.put(Playlist.COVER_URL, playlist.getCoverUrl());

        if (playlist.getType() != null) {
            values.put(Playlist.F_TYPE, playlist.getType().name());
        }

        Long playlistId = playlist.getId();
        Playlist savedPlaylist = null;

        if (playlistId != null) {
            savedPlaylist = findPlaylistWithSongs(playlist.getId(), false);
            if (savedPlaylist != null) {
                db.update(Playlist.TABLE_NAME, values, Playlist.ID + " = ?", new String[]{String.valueOf(playlist.getId())});
            } else {
                playlistId = db.insert(Playlist.TABLE_NAME, null, values);
            }
        } else {
            playlistId = db.insert(Playlist.TABLE_NAME, null, values);
        }
        playlist.setId(playlistId);

        if (playlist.getSongs() == null) {
            playlist.setSongs(new ArrayList<PlaylistSong>());
        }

        for (PlaylistSong pls : playlist.getSongs()) {
            if (pls.getId() == null) {

                pls.setPlaylist(playlist);

                if (pls.getSongType() == null) {
                    pls.setSongType(Song.TYPE.LOCAL);
                }

                ContentValues plsValues = new ContentValues();
                plsValues.put(PlaylistSong.PLAYLIST_ID, playlistId);
                plsValues.put(PlaylistSong.SONG_ID, pls.getSong().getId());
                plsValues.put(PlaylistSong.SONG_TYPE, pls.getSong().getType().name());
                db.insert(PlaylistSong.TABLE_NAME, null, plsValues);

                if (pls.getSong().getType() == Song.TYPE.ONLINE && findSongById(Long.valueOf(pls.getSong().getId()), pls.getSong().getType()) == null) {
                    ContentValues songValues = new ContentValues();
                    Song song=pls.getSong();
                    songValues.put(Song.ID, song.getId());
                    songValues.put(Song.NAME, song.getName());
                    songValues.put(Song.F_TYPE, song.getType().name());
                    songValues.put(Song.ARTIST_NAME,song.getArtist().getName());
                    songValues.put(Song.BITRATE, song.getBitrate());
                    songValues.put(Song.IS_LOSSLESS, String.valueOf(song.isLossless()));
                    if (pls.getSong().getAlbum() != null && pls.getSong().getAlbum().getName() != null)
                        values.put(Song.ALBUM_NAME, song.getAlbum().getName());

                    if (pls.getSong().getId() != null) {
                        db.insert(Song.TABLE_NAME, null, songValues);
                    }
                }
            }
        }

        if (savedPlaylist != null && savedPlaylist.getSongs() != null && savedPlaylist.getSongs().size() > 0) {
            savedPlaylist.getSongs().removeAll(playlist.getSongs());

            for (PlaylistSong pls : savedPlaylist.getSongs()) {
                db.delete(PlaylistSong.TABLE_NAME, " id = ?", new String[]{String.valueOf(pls.getId())});
            }
        }

        updatePlaylistSongNumber(playlistId);
    }

//    public static PlaylistSong findByPlaylistAndSong(Long playlistId, Long songId) throws SQLException {
//        if (playlistId == null || songId == null) {
//            return null;
//        }
//
//        QueryBuilder<PlaylistSong, Long> statementBuilder = dbh.getPlaylistSongDao().queryBuilder();
//        statementBuilder.where().eq(PlaylistSong.PLAYLIST_ID, new Playlist(playlistId)).and().eq(PlaylistSong.SONG_ID, new Song(songId));
//
//        return dbh.getPlaylistSongDao().queryForFirst(statementBuilder.prepare());
//    }

    public static String getAlbumName(Context context, String songName) {
//        final String[] ccols = new String[] { MediaStore.Audio.Media.ALBUM };
//        StringBuilder where = new StringBuilder();
//        //remove by shenqi for some mp3 store as  ringtones
//        if(songName.contains("'")){
//            songName=songName.replace("'", "\"");
//        }
//        where.append(MediaStore.Audio.Media.TITLE + "='" + songName+"'");
//        //+ " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
////        where.append(getWhereBuilder(context, "_id", 0));
//        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                ccols, where.toString(), null, null);//"sort_key");
//
//        if (cursor != null&&cursor.moveToFirst()) {
//            String albumName=cursor.getString(0);
//            cursor.close();
//            return albumName;
//        }
//        if(cursor!=null){
//            cursor.close();
//            cursor=null;
//        }
        return null;
    }

    public static String[] getSongListForArtist(Context context, String artist) {
//        final String[] ccols = new String[] { MediaStore.Audio.Media.TITLE };
//        StringBuilder where = new StringBuilder();
//        //remove by shenqi for some mp3 store as  ringtones
//        if(artist.contains("'"))
//            artist=artist.replace("'", "\"");
//        where.append(MediaStore.Audio.Media.ARTIST + "='" + artist+"'");
//        //+ " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
////        where.append(getWhereBuilder(context, "_id", 0));
//        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                ccols, where.toString(), null, null);//"sort_key");
//
//        if (cursor != null) {
//            String[] list=new String[cursor.getCount()];
//            int i=0;
//            while(cursor.moveToNext()){
//                list[i]=cursor.getString(cursor.getColumnIndexOrThrow("title"));
//                i++;
//            }
//            cursor.close();
//            cursor=null;
//            return list;
//        }
        return null;
    }

    public static Cursor getAllSongsCursor() {
        String[] paths = MusicUtils.getFolderPath(Lewa.context());
        List<Long> ids = new ArrayList<Long>();
        for (String path : paths) {
            ids.addAll(MusicUtils.getFolderAudioId(Lewa.context(), path, 0));
        }
        Cursor cur = getAudioCursor(ids);

        return cur;
    }

    public static long[] getAllSongs() {
        StringBuilder where = new StringBuilder();
        //remove by shenqi for some mp3 store as  ringtones
        //where.append(MediaStore.Audio.Media.IS_MUSIC + "=1");
        where.append("1=1");
//        where.append(getWhereBuilder(context, "_id", 0));
        Cursor c = MusicUtils.query(Lewa.context(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID}, where.toString(),
                null, null);
        try {
            if (c == null || c.getCount() == 0) {
                return null;
            }
            int len = c.getCount();
            long[] list = new long[len];
            for (int i = 0; i < len; i++) {
                c.moveToNext();
                list[i] = c.getLong(0);
            }

            return list;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static List<SongCollection> getPlayHistories() {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if (db == null) {
            return null;
        }
        List<SongCollection> histories = new ArrayList<SongCollection>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + SongCollection.TABLE_NAME + " order by " + SongCollection.DISPLAY_ORDER + " desc limit 6", null);
            SongCollectionIndex songCollectionIndex = new SongCollectionIndex(cursor);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    histories.add(SongCollection.fromCursor(cursor, songCollectionIndex));
                    cursor.moveToNext();
                }
            }

            for (SongCollection sc : histories) {
                Log.i(TAG, "Play history: " + sc.toString());
            }

            Collections.reverse(histories);
            List<SongCollection> cleanedHistories = new ArrayList<SongCollection>();

            Log.i(TAG, "history count: " + histories.size());
            for (SongCollection history : histories) {
                if (history.getType() == SongCollection.Type.ALBUM) {
                    Album album = (Album) history.getOwner();

                    if (album != null) {
                        if (album.getType() == Album.TYPE.ONLINE) {
                            cleanedHistories.add(history);
                        } else if (album.getId() != null) {
                            int songNum = loadSongCountOfAlbum(album.getId());

                            if (songNum > 0) {
                                cleanedHistories.add(history);
                            }
                        }
                    }
                } else if (history.getType() == SongCollection.Type.SINGLE) {
                    if(history.getLastSong() != null) {
                        cleanedHistories.add(history);
                    }
                }else if (history.getType() == SongCollection.Type.PLAYLIST) {
                    Playlist playlist = (Playlist) history.getOwner();

                    if (playlist != null && playlist.isLocal()) {
                        int songNum = 0;
                        List<Song> songs;
                        if (playlist.getType() == Playlist.TYPE.ALL) {
                            songNum = countSongs();
//                            songs = loadAllSongs();
                        } else if (playlist.getType() == Playlist.TYPE.ARTIST) {
                            songNum = loadSongCountOfArtist(playlist.getArtist().getId());
//                            songs = loadAllSongs();
                        } else {
                            playlist = findPlaylist(playlist.getId());
                            songNum = playlist.getSongNum();
//                            songs = findSongsOfPlaylist(Lewa.context(), playlist.getId());
                        }

                        if (songNum > 0) {
                            cleanedHistories.add(history);
                        }
                    } else {
                        cleanedHistories.add(history);
                    }
                }
            }

            for (SongCollection sc : cleanedHistories) {
                Log.i(TAG, "Not empty history: " + sc.toString());
            }

            return cleanedHistories;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return null;
    }

    public static void logPlayHistory(SongCollection sc, Song song) {
        if (song == null) return;

        if (sc == null) {
            sc = new SongCollection(SongCollection.Type.PLAYLIST, new Playlist(Playlist.TYPE.ALL));
            sc.setName(Lewa.string(R.string.text_all_local_songs));
        }

        Log.i(TAG, "Log play history: " + sc.toString());

        switch (sc.getType()) {
            /*case SINGLE:
                Album album = song.getAlbum();

                if (album != null && song.getType() == Song.TYPE.ONLINE) {
                    album.setType(Album.TYPE.ONLINE);
                }

                sc.setOwnerWithoutReset(song.getAlbum());
                break;*/
            case SINGLE:
            case ALBUM:
                break;
            case PLAYLIST:
                Playlist playlist = (Playlist) sc.getOwner();
                if (playlist == null) {
                    return;
                }

                if (playlist.getType() == Playlist.TYPE.ALL) {
                    sc.setDisplayOrder(6);
                    sc.setName(Lewa.string(R.string.text_all_local_songs));
                } else if (playlist.getType() == Playlist.TYPE.DOWNLOAD) {
                    sc.setDisplayOrder(5);
                } else if (playlist.getType() == Playlist.TYPE.FAVORITE) {
                    sc.setDisplayOrder(4);
                } else {
                    sc.setName(playlist.getName());
                }
                break;
        }

        if (sc.getId() == null) return;

        sc.setLastSong(song);

        SQLiteDatabase db = dbHandler.getWritableDatabase();
        String sql = "update " + SongCollection.TABLE_NAME + " set " + SongCollection.DISPLAY_ORDER + " = " + SongCollection.DISPLAY_ORDER + " - 1 where "
                + SongCollection.ID + " not in ('" + SongCollection.ALL_ID + "','" + SongCollection.DOWNLOAD_ID + "', '" + SongCollection.FAVORITE_ID + "', '"
                + sc.getId() + "') and " + SongCollection.DISPLAY_ORDER + " < 4;";
        Log.v(TAG, "Update Sql: " + sql);
        db.execSQL(sql);

        dbHandler.insertOrReplacePlayHistroy(sc, db);
        //TODO: remove older histories here.
    }

    public static void logRecentPlay(Song playingSong) {
        if (playingSong == null) {
            return;
        }

        Cursor cursor = null;
        try {
            //insert into recently playlist
//            playingSong.setType(Song.TYPE.LOCAL);
            Playlist recentPlaylist = DBService.findSinglePlaylist(Playlist.TYPE.RECENT_PLAY);

            if (recentPlaylist != null) {
                SQLiteDatabase db = dbHandler.getWritableDatabase();
                if (db == null) {
                    return;
                }

                cursor = db.query(PlaylistSong.TABLE_NAME, new String[]{PlaylistSong.ID}, PlaylistSong.PLAYLIST_ID + " = ?", new String[]{String.valueOf(Playlist.RECENT_PLAY_ID)}, null, null, PlaylistSong.ID + " desc");

                if (cursor.getCount() >= 30) {
                    cursor.moveToPosition(30);
                    long[] ids = new long[cursor.getCount() - 29];
                    int i = 0;
                    StringBuffer sb = new StringBuffer();

                    while (!cursor.isAfterLast()) {
                        if (i > 0) {
                            sb.append(", ");
                        }

                        sb.append(cursor.getLong(0));
                        i++;
                        cursor.moveToNext();
//                        ids[i++] = cursor.getLong(0);
                    }

                    Log.v(TAG, "Recent play song count: " + cursor.getCount() + ", to be deleted ids: " + sb.toString());
                    cursor.close();

                    db.delete(PlaylistSong.TABLE_NAME, PlaylistSong.ID + " in (" + sb.toString() + ")", null);
                }

                PlaylistSong playlistSong = new PlaylistSong();
                playlistSong.setSong(playingSong);
                playlistSong.setPlaylist(recentPlaylist);
                playlistSong.setSongType(playingSong.getType());
                savePlaylistSong(playlistSong, recentPlaylist.getId(), true);
            }

            /*SongCollection playingCollection = Lewa.getPlayingCollection();

            if (playingCollection != null && playingSong != null && playingSong.getArtist() != null && !StringUtils.isBlank(playingSong.getArtist().getName())) {
                if (playingCollection.getType() == SongCollection.Type.PLAYLIST) {
                    Playlist playlist = (Playlist) playingCollection.getOwner();

                    if (playlist.getType() == Playlist.TYPE.ALL) {
                        playlist.setId(Playlist.ALL_ID);
                    }

                    if (playlist.isLocal()) {
                        playlist = findPlaylist(playlist.getId());

                        playlist.setCoverUrl(LewaUtils.getArtistPicPath(playingSong.getArtist().getName()));
                        updatePlaylistWithoutSongs(playlist, false);
                    }
                }
            }*/
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static List<String> getSearchHistroy(){
    	List<String> histroies=new ArrayList<String>();
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = null;
        try {
			if(db!=null){
				cursor=db.query(Constants.SEARCH_HISTORY_TABLE, null, null, null, null, null, Constants.SEARCH_HISTORY_ID+" asc");
				if(cursor!=null&&cursor.moveToFirst()){
					for(int i= 0;i<cursor.getCount();i++){
						histroies.add(cursor.getString(1));
						cursor.moveToNext();
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			closeCursor(cursor);
		}
        return histroies;
    }

    public static void updateSearchHistroy(List<String> histroies){
    	if(histroies==null||histroies.size()<=0)
    		return;
    	SQLiteDatabase db = dbHandler.getReadableDatabase();
    	if(db!=null){
    		db.beginTransaction();
    		db.delete(Constants.SEARCH_HISTORY_TABLE, null, null);
    		for(int i=0;i<histroies.size();i++){
    			ContentValues values = new ContentValues();
    			values.put(Constants.SEARCH_HISTORY_ID, i);
    			values.put(Constants.SEARCH_HISTORY_TEXT, histroies.get(i));
    			db.insert(Constants.SEARCH_HISTORY_TABLE, null, values);
    		}
    		db.setTransactionSuccessful();
    		db.endTransaction();
    	}
    }

    public static void clearSearchHistroy(){
    	SQLiteDatabase db = dbHandler.getReadableDatabase();
    	if(db!=null){
    		db.delete(Constants.SEARCH_HISTORY_TABLE, null, null);
    	}
    }


    private static void closeCursor(Cursor cursor){
    	if(cursor!=null&&!cursor.isClosed())
    		cursor.close();
    }
}
