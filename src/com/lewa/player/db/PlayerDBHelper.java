package com.lewa.player.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lewa.player.MusicUtils;


public class PlayerDBHelper extends SQLiteOpenHelper {
    private final static String TAG = "PlayerDBHelper";//.class.getName();

    private static final int DATABASE_VERSION = 14;
    private static final int MAX_FAVOURITE_COUNT = 30;

    final Context mContext;
    final boolean mInternal;
    int playtimes = 0;
//    private Cursor cursor;

    public PlayerDBHelper(Context context, String name, boolean internal) {
        super(context, name, null, DATABASE_VERSION);
        mContext = context;
        mInternal = internal;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        createTables(db);
    }

    public String getFavouriteArtist() {
        String artistName = null;
        SQLiteDatabase ldb = this.getReadableDatabase();
        Cursor c = ldb.query("playlist_audio_map", new String[]{"artist"}, "play_times!=-1", null, null, null, "play_times desc");
        if (c != null) {
            c.moveToFirst();
            if (c.getCount() > 0) {
                artistName = c.getString(0);
            } else {
                artistName = null;
            }
        }
        c.close();
        c = null;
        return artistName;
    }

    public int read_times(int id) {
        int ret = 0;
        try {
            SQLiteDatabase ldb = this.getReadableDatabase();
            String sname = MusicUtils.getSongName(mContext, id)[0].toString();
            Cursor c = ldb.query("playlist_audio_map", new String[]{"play_times"}, "name LIKE" + "'" + sname + "'", null, null, null, null);
            //ldb.execSQL("select play_times from playlist_audio_map where song_id =" + String.valueOf(id));
            //ldb.insert("playlist_audio_map", "song_id", values);
            if (c != null) {
                c.moveToFirst();
                if (c.getCount() > 0) {
                    ret = c.getInt(0);
                } else {
                    ret = -1;
                }
            }
            c.close();
            c = null;
            //ldb.close();
        } catch (SQLException e) {

        }
        return ret;
    }

    public String[] getDBFavouriteList() {
        String[] ret = null;
        try {
            SQLiteDatabase ldb = this.getReadableDatabase();
            Cursor c = ldb.query("playlist_audio_map", new String[]{"name"}, "play_times!=-1", null, null, null, "play_times desc");
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                int count = c.getCount() >= MAX_FAVOURITE_COUNT ? MAX_FAVOURITE_COUNT : c.getCount();
                ret = new String[count];
                for (int i = 0; i < count; i++) {
                    ret[i] = c.getString(0);
                    c.moveToNext();
                }
            }
            c.close();
            c = null;
            // ldb.close();
        } catch (SQLException e) {

        }

        return ret;

    }

    public void deleteDBFavoriteList(String name) {

        try {
            SQLiteDatabase ldb = this.getWritableDatabase();
            ldb.delete("playlist_audio_map", "name in " + "(" + name + ")", null);
            //ldb.close();
        } catch (SQLException e) {
        }
    }

    public void times_plus(int id) {
        int songid = read_times(id);
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            if (songid < 0) {
                songid = 0;
                values.put("song_id", id);
                values.put("name", MusicUtils.getSongName(mContext, id)[0].toString());
                values.put("artist", MusicUtils.getArtistName(mContext, MusicUtils.getArtistId(mContext, id)));
                values.put("play_times", songid);
                db.insert("playlist_audio_map", "song_id", values);
            } else {
                songid = songid + 1;
                values.put("song_id", id);
                values.put("play_times", songid);
                db.update("playlist_audio_map", values, "song_id=" + id, null);
            }
            values.clear();
            //db.close();
        } catch (SQLException e) {

        }
    }

    public void updateDBFolder(String[] path) {
        try {
            SQLiteDatabase ldb = this.getWritableDatabase();

            ldb.delete("folder", null, null);

            ContentValues values = new ContentValues();
            int size = path.length;
            for (int i = 0; i < size; i++) {
                values.put("folder_path", path[i]);
                ldb.insert("folder", "folder_path", values);
            }
            values.clear();
            //ldb.close();
        } catch (SQLException e) {

        }
    }

    public void insertDBFolder(String[] path) {
        try {
            SQLiteDatabase ldb = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            int size = path.length;
            for (int i = 0; i < size; i++) {
                Cursor cursor = ldb.query("folder", null, "folder_path=?", new String[]{path[i]}, null, null, null);
                if (cursor.getCount() <= 0) {
                    values.put("folder_path", path[i]);
                    ldb.insert("folder", "folder_path", values);
                }
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
            values.clear();
            //ldb.close();
        } catch (SQLException e) {

        }
    }

    public void deleteDBFolder(String path) {
        SQLiteDatabase ldb = this.getWritableDatabase();
        ldb.delete("folder", "folder_path=?", new String[]{path});
    }

    public String[] getDBFolder() {
        String[] paths = new String[0];
        try {
            SQLiteDatabase ldb = this.getReadableDatabase();
            Cursor c = ldb.query("folder", new String[]{"folder_path"}, null, null, null, null, null);
            if (c != null && !c.isClosed() && ldb.isOpen() && c.getCount() > 0) {
                c.moveToFirst();
                paths = new String[c.getCount()];
                for (int i = 0; i < c.getCount(); i++) {
                    paths[i] = c.getString(0);
                    c.moveToNext();
                }
            }
            c.close();
            c = null;
            //ldb.close();
        } catch (SQLException e) {

        }

        return paths;
    }

    //add by wangliqiang
    public void addArtistDetail(String[] info) {
        SQLiteDatabase ldb = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("artistname", info[0]);
        values.put("albumstotal", info[1]);
        values.put("area", info[2]);
        values.put("bloodtype", info[3]);
        values.put("company", info[4]);
        values.put("country", info[5]);
        values.put("height", info[6]);
        values.put("intro", info[7]);
        values.put("birthday", info[8]);
        values.put("star", info[9]);
        values.put("songstotal", info[10]);
        values.put("weight", info[11]);
        if (values != null && info[0] != null)
            ldb.insert("artist_detail", null, values);
        values.clear();
        ldb.close();
    }
    //add by wangliqiang
//    public Cursor getArtistDetail(String artistName){
//        SQLiteDatabase ldb=this.getReadableDatabase();
//        if(artistName!=null){
//        cursor = ldb.query("artist_detail", null, "artistname='"+artistName+"'", null, null, null, null);
//        }
//        return cursor;
//    }

    public void addAlbumDetail(String[] info) {
        SQLiteDatabase ldb = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("albumid", info[0]);
        values.put("albumtitle", info[1]);
        values.put("songstotal", info[2]);
        values.put("publishtime", info[3]);
        values.put("songstitle", info[4]);
        values.put("author", info[5]);
        values.put("info", info[6]);
        if (values != null)
            ldb.insert("album_detail", null, values);
        values.clear();
        ldb.close();
    }

    public void addSongAlbum(String songid, String albumid) {
        SQLiteDatabase ldb = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("songid", songid);
        values.put("albumid", albumid);
        if (values != null)
            ldb.insert("song_album", null, values);
        values.clear();
        ldb.close();

    }

//    public Cursor getAlbumDetail(long songid){
//        SQLiteDatabase ldb=this.getReadableDatabase();
//        Cursor cursor=ldb.rawQuery("select albumid from song_album where songid=?", new String[]{String.valueOf(songid)});
//        if(!cursor.moveToFirst())
//            return null;
//        Cursor c=ldb.rawQuery("select * from album_detail where albumid=?", new String[]{cursor.getString(cursor.getColumnIndex("albumid"))});
//        cursor.close();
//        return c;
//    }

//    public Cursor getAlbumDetail(String albumname){
//        SQLiteDatabase ldb=this.getReadableDatabase();
//        Cursor c=ldb.rawQuery("select * from album_detail where albumtitle=?", new String[]{albumname});
//        return c;
//    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        if (oldVersion == 3) {
            db.execSQL("DROP TABLE IF EXISTS playlist_audio_map");
            db.execSQL("create table if not exists playlist_audio_map("
                    + "song_id integer primary key,"
                    + "name varchar,"
                    + "playlist,"
                    + "date_added,"
                    + "isonline,"
                    + "artist,"
                    + "play_times integer)");
        }
        if (oldVersion == 4) {
            db.execSQL("create table if not exists artist_detail (" +
                    "artistname varchar primary key," +
                    "albumstotal," +
                    "area," +
                    "bloodtype," +
                    "company," +
                    "country," +
                    "height," +
                    "intro," +
                    "birthday," +
                    "star," +
                    "songstotal," +
                    "weight varchar" +
                    ")");
            db.execSQL("create table if not exists album_detail (" +
                    "albumid varchar primary key," +
                    "albumtitle," +
                    "songstotal," +
                    "publishtime," +
                    "songstitle," +
                    "author," +
                    "info varchar" +
                    ")");

            db.execSQL("create table if not exists song_album (" +
                    "songid varchar primary key," +
                    "albumid varchar" +
                    ")"
            );

            db.execSQL("create table if not exists down_music (" +
                    "songid unique," +
                    "position," +
                    "tag varchar" +
                    ")");
        }

        if (oldVersion == 5) {
            db.execSQL("DROP TABLE IF EXISTS artist_detail");
            db.execSQL("DROP TABLE IF EXISTS album_detail");
            db.execSQL("DROP TABLE IF EXISTS song_album");
        }
        if (oldVersion == 6) {
            db.execSQL("create table if not exists down_music (" +
                    "songid unique," +
                    "position," +
                    "tag varchar" +
                    ")");
        }

        if (oldVersion == 7) {
            db.execSQL("DROP TABLE IF EXISTS down_music");
            db.execSQL("create table if not exists down_music (" +
                    "songid unique," +
                    "position," +
                    "downId," +
                    "tag varchar" +
                    ")");
        }

        if (oldVersion < 9)
            onCreate(db);

    }

    public void recreateTables() {
        Log.i(TAG, "Recreate tables of old player.");
        SQLiteDatabase db = getReadableDatabase();
        dropTables(db);
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("create table if not exists playlist_audio_map("
                + "song_id integer primary key,"
                + "name varchar,"
                + "playlist,"
                + "date_added,"
                + "isonline,"
                + "artist,"
                + "play_times integer)");

        db.execSQL("create table if not exists select_folder("
                + "id integer primary key,"
                + "path varchar,"
                + "play_times integer)");

        db.execSQL("create table if not exists folder("
                + "_id integer primary key autoincrement,"
                + "folder_path varchar)");
        db.execSQL("create table if not exists down_music (" +
                "songid unique," +
                "position," +
                "downId," +
                "tag varchar" +
                ")");
        db.execSQL("create table if not exists playlist_online("
                + "song_id long,"
                + "artist,"
                + "title varchar,"
                + "playlist_id,"
                + "duration long,"
                + "play_order integer," +
                "_data varchar," +
                "_id long)");
//    	 db.execSQL("create table if not exists artist_detail (" +
//                 "artistname varchar primary key," +
//                 "albumstotal," +
//                 "area," +
//                 "bloodtype," +
//                 "company," +
//                 "country," +
//                 "height," +
//                 "intro," +
//                 "birthday," +
//                 "star," +
//                 "songstotal," +
//                 "weight varchar" +
//                 ")");
//         db.execSQL("create table if not exists album_detail ("+
//                    "albumid varchar primary key,"+
//                    "albumtitle,"+
//                    "songstotal,"+
//                    "publishtime,"+
//                    "songstitle,"+
//                    "author,"+
//                    "info varchar"+
//                    ")");
//
//          db.execSQL("create table if not exists song_album ("+
//                      "songid varchar primary key," +
//                      "albumid varchar"+
//                      ")"
//         );
        try {
            //String filePath = Environment.getExternalStorageDirectory()+"/LEWA/music/";
            //Runtime.getRuntime().exec("rm -rf " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS playlist_audio_map");
        db.execSQL("DROP TABLE IF EXISTS select_folder");
        db.execSQL("DROP TABLE IF EXISTS folder");
        db.execSQL("DROP TABLE IF EXISTS down_music");
        db.execSQL("DROP TABLE IF EXISTS playlist_online");
    }
}
