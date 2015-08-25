package com.lewa.player.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.model.Album;
import com.lewa.player.model.Artist;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.PlaylistSong;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.util.Constants;
import com.lewa.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wuzixiu on 12/29/13.
 */
public class DbHandler extends SQLiteOpenHelper {
    private static final String TAG = "DbHandler";

    private static final String DATABASE_NAME = "com_lewa_musicplayer.db";
    private static final int DATABASE_VERSION = 17;

    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        initData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        createTables(db);
        initData(db);
    }

    private void dropTables(SQLiteDatabase db) {
        String DEL_ARTIST_TABLE = "DROP TABLE " + " IF EXISTS " + Artist.TABLE_NAME;
        db.execSQL(DEL_ARTIST_TABLE);
        String DEL_ALBUM_TABLE = "DROP TABLE " + " IF EXISTS " + Album.TABLE_NAME;
        db.execSQL(DEL_ALBUM_TABLE);
        String DEL_PLAYLIST_TABLE = "DROP TABLE " + " IF EXISTS " + Playlist.TABLE_NAME;
        db.execSQL(DEL_PLAYLIST_TABLE);
        String DEL_SONG_TABLE = "DROP TABLE " + " IF EXISTS " + Song.TABLE_NAME;
        db.execSQL(DEL_SONG_TABLE);
        String DEL_PLAYLIST_SONG_TABLE = "DROP TABLE " + " IF EXISTS " + PlaylistSong.TABLE_NAME;
        db.execSQL(DEL_PLAYLIST_SONG_TABLE);
        String DEL_PLAY_HISTORY_TABLE = "DROP TABLE " + " IF EXISTS " + SongCollection.TABLE_NAME;
        db.execSQL(DEL_PLAY_HISTORY_TABLE);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL(Playlist.CREATE_TABLE_SCRIPT);
        db.execSQL(Song.CREATE_TABLE_SCRIPT);
        db.execSQL(SongCollection.CREATE_TABLE_SCRIPT);

        String CREATE_PLAYLIST_SONG_TABLE = "CREATE TABLE IF NOT EXISTS " + PlaylistSong.TABLE_NAME + "("
                + PlaylistSong.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + PlaylistSong.PLAYLIST_ID + " INTEGER,"
                + PlaylistSong.SONG_ID + " INTEGER," + PlaylistSong.SONG_TYPE + " TEXT,"
                + PlaylistSong.CREATE_TIME + " INTEGER," + PlaylistSong.LAST_UPDATE_TIME + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_PLAYLIST_SONG_TABLE);
        
        String CREATE_SEARCH_HISTORY_TABLE="CREATE TABLE IF NOT EXISTS " + Constants.SEARCH_HISTORY_TABLE + "("
        +Constants.SEARCH_HISTORY_ID + " INTEGER PRIMARY KEY," + Constants.SEARCH_HISTORY_TEXT +" TEXT)";
        db.execSQL(CREATE_SEARCH_HISTORY_TABLE);
    }

    public void recreateTables() {
        SQLiteDatabase db = getReadableDatabase();
        dropTables(db);
        createTables(db);
        initData(db);
    }

    private void initData(SQLiteDatabase db) {
        Log.i(TAG, "Initialize database.");
        List<Playlist> playlists = new ArrayList<Playlist>();
        Playlist allPl = new Playlist(Playlist.ALL_ID, "全部本地歌曲", Playlist.TYPE.ALL, 1, 1, null);
        Playlist favoritePl = new Playlist(Playlist.FAVORITE_ID, "我的收藏", Playlist.TYPE.FAVORITE, 1, 2, null);
        Playlist recentPlayPl = new Playlist(Playlist.RECENT_PLAY_ID, "最近播放", Playlist.TYPE.RECENT_PLAY, 1, 3, null);
        Playlist downloadPl = new Playlist(Playlist.DOWNLOAD_ID, "我的下载", Playlist.TYPE.DOWNLOAD, 1, 4, null);
        playlists.add(allPl);
        playlists.add(favoritePl);
        playlists.add(recentPlayPl);
        playlists.add(downloadPl);

        //playlists.add(new Playlist(10L, "歌单", Playlist.TYPE.ONLINE_CATEGORY, 1, 1, "http://c.hiphotos.baidu.com/ting/pic/item/8c1001e93901213f1decac7655e736d12e2e95b9.jpg"));
        //playlists.add(new Playlist(11L, "排行榜", Playlist.TYPE.TOP_LIST_CATEGORY, 1, 2, "http://b.hiphotos.baidu.com/ting/pic/item/ac4bd11373f08202427372954afbfbedaa641bb2.jpg"));
        //playlists.add(new Playlist(12L, "全明星", Playlist.TYPE.ALL_STAR_CATEGORY, 1, 3, "http://b.hiphotos.baidu.com/ting/pic/item/d1a20cf431adcbefedf99cacadaf2edda2cc9f4f.jpg"));
        playlists.add(new Playlist(10L, "歌单", Playlist.TYPE.ONLINE_CATEGORY, 1, 1, null));
        playlists.add(new Playlist(11L, "排行榜", Playlist.TYPE.TOP_LIST_CATEGORY, 1, 2, null));
        playlists.add(new Playlist(12L, "全明星", Playlist.TYPE.ALL_STAR_CATEGORY, 1, 3, null));

        Playlist newCountdown = new Playlist(13l, Lewa.string(R.string.new_songs_list), Playlist.TYPE.TOP_LIST_NEW, 1, 1, null);
        Playlist hotCountdown = new Playlist(14l, Lewa.string(R.string.hot_songs_list), Playlist.TYPE.TOP_LIST_HOT, 2, 1, null);
        playlists.add(newCountdown);
        playlists.add(hotCountdown);

        SongCollection favoriteSc = new SongCollection(SongCollection.FAVORITE_ID, favoritePl.getName(), SongCollection.Type.PLAYLIST, favoritePl, 4, 0);
        SongCollection downloadSc = new SongCollection(SongCollection.DOWNLOAD_ID, downloadPl.getName(), SongCollection.Type.PLAYLIST, downloadPl, 5, 0);
        SongCollection allSc = new SongCollection(SongCollection.ALL_ID, allPl.getName(), SongCollection.Type.PLAYLIST, allPl, 6, 0);

        try {
            for (Playlist playlist : playlists) {
                savePlaylist(playlist, db);
            }

            insertOrReplacePlayHistroy(favoriteSc, db);
            insertOrReplacePlayHistroy(downloadSc, db);
            insertOrReplacePlayHistroy(allSc, db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void savePlaylist(Playlist playlist, SQLiteDatabase db) {
        try {
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

            db.insert(Playlist.TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertOrReplacePlayHistroy(SongCollection sc, SQLiteDatabase db) {
//        if (sc.getType() == SongCollection.Type.SINGLE) return;
        Log.i(TAG, "Insert play history: " + sc.toString());

        try {
            ContentValues values = buildPlayHistoryValues(sc);
            if (values != null) {
                db.insertWithOnConflict(SongCollection.TABLE_NAME, SongCollection.ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ContentValues buildPlayHistoryValues(SongCollection sc) {
        if (sc.getType() == null || sc.getOwner() == null) {
            return null;
        }

        ContentValues values = new ContentValues();
        values.put(SongCollection.ID, sc.getId());
        values.put(SongCollection.COLLECTION_TYPE, sc.getType().name());

        Log.v(TAG, "Display order: " + sc.getDisplayOrder() + " -> " + (sc.getDisplayOrder() < 4 ? 3 : sc.getDisplayOrder()));
        values.put(SongCollection.DISPLAY_ORDER, sc.getDisplayOrder() < 4 ? 3 : sc.getDisplayOrder());
        values.put(SongCollection.NAME, sc.getName());

        if (sc.getType() == SongCollection.Type.PLAYLIST) {
            Playlist playlist = (Playlist) sc.getOwner();

            if (StringUtils.isBlank(sc.getCoverUrl())) {
                values.put(SongCollection.COVER_URL, playlist.getCoverUrl());
            } else {
                values.put(SongCollection.COVER_URL, sc.getCoverUrl());
            }

            if (playlist.getType() != null) {
                Playlist.TYPE plt = playlist.getType();
                values.put(SongCollection.REF_TYPE, plt.name());

                switch (plt) {
                    case LOCAL:
                    case RECENT_PLAY:
                    case FAVORITE:
                    case DOWNLOAD:
                    case ALL:
                        if (playlist.getId() != null) {
                            values.put(SongCollection.REF_ID, playlist.getId());
                        }
                        break;
                    case ARTIST:
                        if (playlist.getArtist() != null) {
                            values.put(SongCollection.REF_ID, playlist.getArtist().getId());
                        }
                        break;
                    default:
                        if (playlist.getBdCode() != null) {
                            values.put(SongCollection.REF_ID, playlist.getBdCode());
                        }
                        break;
                }
            } else {
                return null;
            }
        } else if (sc.getType() == SongCollection.Type.ALBUM) {
            Album album = (Album) sc.getOwner();

            if (StringUtils.isBlank(sc.getCoverUrl())) {
                values.put(SongCollection.COVER_URL, album.getArt());
            } else {
                values.put(SongCollection.COVER_URL, sc.getCoverUrl());
            }

            if (album.getType() != null) {
                values.put(SongCollection.REF_TYPE, album.getType().name());
            }
            values.put(SongCollection.REF_ID, album.getId());
        } else if (sc.getType() == SongCollection.Type.SINGLE) {
            Song song = (Song) sc.getOwner();

            if (song != null) {
                if (StringUtils.isBlank(sc.getCoverUrl())) {
                    values.put(SongCollection.COVER_URL, song.getCoverUrl());
                } else {
                    values.put(SongCollection.COVER_URL, sc.getCoverUrl());
                }
                values.put(SongCollection.REF_TYPE, song.getType().name());
            }
        }

        Log.i("Play history", "Analysed name: " + values.getAsString(SongCollection.NAME));

        //TODO: * -1 for online album
        values.put(SongCollection.IS_EMPTY, sc.getIsEmpty());
        values.put(SongCollection.LAST_SONG_ID, sc.getLastSong() == null ? 0 : sc.getLastSong().getId());
        values.put(SongCollection.LAST_SONG_NAME, sc.getLastSong() == null ? "" : sc.getLastSong().getName());

        return values;
    }
}
