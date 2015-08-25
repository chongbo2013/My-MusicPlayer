package com.lewa.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.util.LruCache;
import lewa.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.ExitApplication;
import com.lewa.player.db.PlayerDBHelper;
import com.lewa.player.online.DownLoadAllPicsAsync;
import com.lewa.player.online.LocalAsync;
//import com.ting.mp3.android.onlinedata.LyricManager;
//import com.ting.mp3.android.onlinedata.OnlineAlbumDataManager;
//import com.ting.mp3.android.onlinedata.OnlineCustomDataManager;
//import com.ting.mp3.android.onlinedata.OnlineManagerEngine;
//import com.ting.mp3.android.onlinedata.OnlineRadioDataManager;
//import com.ting.mp3.android.onlinedata.OnlineSearchDataManager;
//import com.ting.mp3.android.onlinedata.OnlineSingerDataManager;
//import com.ting.mp3.android.onlinedata.OnlineTopListDataManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.lewa.player.helper.MusicFilesObserver;


//import android.text.TextUtils;
//import android.view.Window;


public class MusicUtils {

    private static final String TAG = "MusicUtils";
    private static final String ACTION_ADD_PLAYLIST = "com.lewa.player.ui.ADD_PLAYLIST";
    private static final String MUSIC_PREFERENCES = "Music_setting";
    public static final String FILTER_SONGS = "com.lewa.player.filterSongs";
    private static final String COVER_PATH = Environment.getExternalStorageDirectory() + "/LEWA/music/playlist/cover/";
    public static boolean mHasSongs = true;
    public static final String ACTION_DELETEITEM = "DELETEITEM";
    public static final String ACTION_FILTER = "FILTER_SONGS";
    private static final int ID_MUSIC_NO_SONGS_CONTENT = 100;
    public static boolean artist_info_loaded = false;
    public static boolean album_info_loaded = false;
    public static boolean isScan = false;
    public static boolean isLiving = false;
    public static boolean isProtected = false;
    public static int curposition = 1;
    public static String UPDATE_ALLSONGSIMG = "com.lewa.player.ui.update_allsongs";
    public static String UPDATE_NOWPLAYINGALBUM = "com.lewa.player.ui.UPDATE_TOPALBUM";
    public static String UPDATE_TOKEN = "com.lewa.player.update_token";
    public static String CERTIFY_TOKEN = "com.lewa.player.certify_token";
    public static String ONLINE_PLAY_SCAN_FINISHED = "com.lewa.player.online_play_scan_finished";
    public static boolean isFirst = false;
    public static boolean isLricFile = false;
    public static boolean isSdMounted = true;
    public static final String CMDAPPWIDGETUPDATE = "appwidgetupdate";
    static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final static int catheSize = maxMemory / 8;
    public static BitmapDrawable bitmapDrawableCache;
    public static Map<String, LocalAsync> taskMap = new HashMap<String, LocalAsync>();
    public static boolean scan_finished = false;
    public static LruCache<String, Bitmap> mMemoryCathe = new LruCache<String, Bitmap>(catheSize) {

        @Override
        protected int sizeOf(String key, Bitmap value) {
            // TODO Auto-generated method stub
            return value.getByteCount() / 1024;
        }
    };

    public static void addBitmapToMemoryCathe(String key, Bitmap bitmap) {
        if (key != null && getBitmapFromMemoryCathe(key) == null && bitmap != null) {
            mMemoryCathe.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemoryCathe(String key) {
        if (key != null) {
            return mMemoryCathe.get(key);
        } else {
            return null;
        }
    }

    public interface Defs {
        public final static int OPEN_URL = 0;
        public final static int ADD_TO_PLAYLIST = 1;
        public final static int USE_AS_RINGTONE = 2;
        public final static int PLAYLIST_SELECTED = 3;
        public final static int NEW_PLAYLIST = 4;
        public final static int PLAY_SELECTION = 5;
        public final static int GOTO_START = 6;
        public final static int GOTO_PLAYBACK = 7;
        public final static int PARTY_SHUFFLE = 8;
        public final static int SHUFFLE_ALL = 9;
        public final static int DELETE_ITEM = 10;
        public final static int SCAN_DONE = 11;
        public final static int QUEUE = 12;
        public final static int EFFECTS_PANEL = 13;
        public final static int SHARE_LIST = 14;
        public final static int SETTINGS = 15;
        public final static int SEARCH = 16;
        public final static int FOLDER = 17;
        public final static int EDIT_PLAYLIST = 18;
        public final static int SAVE_TO_PLAYLIST = 19;
        public final static int MODIFY_ID3 = 20;
        public final static int MORE_MENU = 21;
        public final static int SLEEP = 22;
        public final static int EQ_SETTING = 23;
        public final static int EXIT = 24;
        public final static int GET_PIC = 25;
        public final static int REFRESH = 26;
        public final static int CHILD_MENU_BASE = 27;  // this should be the last item
        public final static int DOWNLOAD = 28;
//		int getCount();
    }

    public static String makeAlbumsLabel(Context context, int numalbums, int numsongs, boolean isUnknown) {
        // There are two formats for the albums/songs information:
        // "N Song(s)"  - used for unknown artist/album
        // "N Album(s)" - used for known albums

        StringBuilder songs_albums = new StringBuilder();

        Resources r = context.getResources();
        if (isUnknown) {
            if (numsongs == 1) {
                // songs_albums.append(context.getString(R.string.onesong));
            } else {
                //String f = r.getQuantityText(R.plurals.Nsongs, numsongs).toString();
                //sFormatBuilder.setLength(0);
                //sFormatter.format(f, Integer.valueOf(numsongs));
                //songs_albums.append(sFormatBuilder);
            }
        } else {
            //String f = r.getQuantityText(R.plurals.Nalbums, numalbums).toString();
            // sFormatBuilder.setLength(0);
            // sFormatter.format(f, Integer.valueOf(numalbums));
            // songs_albums.append(sFormatBuilder);
            //.append(context.getString(R.string.albumsongseparator));
        }
        return songs_albums.toString();
    }

    /**
     * This is now only used for the query screen
     */
    public static String makeAlbumsSongsLabel(Context context, int numalbums, int numsongs, boolean isUnknown) {
        // There are several formats for the albums/songs information:
        // "1 Song"   - used if there is only 1 song
        // "N Songs" - used for the "unknown artist" item
        // "1 Album"/"N Songs"
        // "N Album"/"M Songs"
        // Depending on locale, these may need to be further subdivided

        StringBuilder songs_albums = new StringBuilder();

        if (numsongs == 1) {
            //songs_albums.append(context.getString(R.string.onesong));
        } else {
            Resources r = context.getResources();
            if (!isUnknown) {
                //String f = r.getQuantityText(R.plurals.Nalbums, numalbums).toString();
                //sFormatBuilder.setLength(0);
                //sFormatter.format(f, Integer.valueOf(numalbums));
                //songs_albums.append(sFormatBuilder);
                //songs_albums.append(context.getString(R.string.albumsongseparator));
            }
            //String f = r.getQuantityText(R.plurals.Nsongs, numsongs).toString();
            //sFormatBuilder.setLength(0);
            //sFormatter.format(f, Integer.valueOf(numsongs));
            //songs_albums.append(sFormatBuilder);
        }
        return songs_albums.toString();
    }

    public static IMediaPlaybackService sService = null;
    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

    public static class ServiceToken {
        ContextWrapper mWrappedContext;

        ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static ServiceToken bindToService(Activity context) {
        return bindToService(context, null);
    }

    public static ServiceToken bindToService(Activity context, ServiceConnection callback) {
        Activity realActivity = context.getParent();
        if (realActivity == null) {
            realActivity = context;
        }
        ContextWrapper cw = new ContextWrapper(realActivity);
        cw.startService(new Intent(cw, MediaPlaybackService.class));
        ServiceBinder sb = new ServiceBinder(callback);
        if (cw.bindService((new Intent()).setClass(cw, MediaPlaybackService.class), sb, 0)) {
            sConnectionMap.put(cw, sb);
            return new ServiceToken(cw);
        }
//        Log.e("Music", "Failed to bind to service");
        return null;
    }

    public static void unbindFromService(ServiceToken token) {
        try {
            if (sService != null && !sService.isPlaying())
                sService.removeStatusBar();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mLastSdStatus = null;
        if (token == null) {
//            Log.e("MusicUtils", "Trying to unbind with null token");
            return;
        }
        ContextWrapper cw = token.mWrappedContext;
        ServiceBinder sb = sConnectionMap.remove(cw);
        if (sb == null) {
//            Log.e("MusicUtils", "Trying to unbind for unknown Context");
            return;
        }
        if (cw != null) {
            cw.unbindService(sb);
        }
        if (sConnectionMap.isEmpty()) {
            // presumably there is nobody interested in the service at this point,
            // so don't hang on to the ServiceConnection
            sService = null;
        }
    }

    private static class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;

        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }

        public void onServiceConnected(ComponentName className, android.os.IBinder service) {
            sService = IMediaPlaybackService.Stub.asInterface(service);
            initAlbumArtCache();
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            sService = null;
        }
    }

    private static PlayerDBHelper lewaDBhelp = null;
    private static SQLiteDatabase db = null;

    private synchronized static void createLewaDbHelp(Context mContext) {
        if (lewaDBhelp == null) {
            lewaDBhelp = new PlayerDBHelper(mContext.getApplicationContext(), "com_lewa_player.db", true);
        }
    }

    private static void getLdb(Context context) {
        if (db == null || !db.isOpen()) {
            createLewaDbHelp(context);
            db = lewaDBhelp.getWritableDatabase();
        }
    }

    public static void clearResource() {
        if (db != null)
            db.close();
        if (lewaDBhelp != null)
            lewaDBhelp.close();
    }

    public static void songPlayTimesPlus(Context mContext) {
        createLewaDbHelp(mContext);
        //lewaDBhelp = new PlayerDBHelper(mContext,"com_lewa_player.db", true);
        try {
            if (sService != null) {
                long[] playlist = sService.getQueue();
                int position = sService.getQueuePosition();
                if (playlist == null) return;
                if (position >= playlist.length) return;
                if (sService.getRepeatMode() == MediaPlaybackService.REPEAT_CURRENT) {
                    position = position == 0 ? playlist.length - 1 : position;
                } else {
                    position = position == 0 ? playlist.length - 1 : position - 1;
                }
                if (position >= 0) {
                    int song_id = (int) playlist[position];
                    lewaDBhelp.times_plus(song_id);
                    lewaDBhelp.close();
                    //will change uri for favorite list
                    mContext.getContentResolver().notifyChange(Uri.parse("content://media"), null);
                }
            }

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static long[] getFavouriteTracks(Context mContext) {
        createLewaDbHelp(mContext);
        String[] mDBFavouriteList = lewaDBhelp.getDBFavouriteList();
        //lewaDBhelp = new PlayerDBHelper(mContext,"com_lewa_player.db", true);

        if (mDBFavouriteList == null) {
            return null;
        }

        List<Long> trackList = new ArrayList<Long>();
        long trackId;
        for (int i = 0; i < mDBFavouriteList.length; i++) {
            trackId = getTrackIdFromTrack(mContext, mDBFavouriteList[i]);
            if (trackId > 0) {
                trackList.add(Long.valueOf(trackId));
            }
        }
        long[] aTrackList = new long[trackList.size()];
        for (int i = 0; i < aTrackList.length; i++) {
            aTrackList[i] = trackList.get(i).longValue();
        }
        return aTrackList;
    }

    public static long getTrackIdFromTrack(Context context, String name) {
        long ret = 0;
        String[] mCursorCols = new String[]{
                MediaStore.Audio.Media._ID};
        Cursor mCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mCursorCols, "TITLE LIKE " + "'" + name + "'", null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0) {
                ret = mCursor.getLong(0);

            }

            mCursor.close();
            mCursor = null;
        }
        return ret;

    }

    public static void deleteFavoriteTracks(Context context, String name) {
        createLewaDbHelp(context);
        //lewaDBhelp = new PlayerDBHelper(context,"com_lewa_player.db", true);
        lewaDBhelp.deleteDBFavoriteList(name);
    }

    public static long getCurrentAlbumId() {
        if (sService != null) {
            try {
                return sService.getAlbumId();
            } catch (RemoteException ex) {
            }
        }
        return -1;
    }

    public static long getCurrentArtistId() {
        if (MusicUtils.sService != null) {
            try {
                return sService.getArtistId();
            } catch (RemoteException ex) {
            }
        }
        return -1;
    }

    public static String getCurrentTrackName() {
        if (MusicUtils.sService != null) {
            try {
                return sService.getTrackName();
            } catch (RemoteException ex) {
            }
        }
        return "";
    }

    public static long getCurrentAudioId() {
        if (MusicUtils.sService != null) {
            try {
                return sService.getAudioId();
            } catch (RemoteException ex) {
            }
        }
        return -1;
    }

    public static int getCurrentShuffleMode() {
        int mode = MediaPlaybackService.SHUFFLE_NONE;
        if (sService != null) {
            try {
                mode = sService.getShuffleMode();
            } catch (RemoteException ex) {
            }
        }
        return mode;
    }

    public static void togglePartyShuffle() {
        if (sService != null) {
            int shuffle = getCurrentShuffleMode();
            try {
                if (shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
                    sService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                } else {
                    sService.setShuffleMode(MediaPlaybackService.SHUFFLE_AUTO);
                }
            } catch (RemoteException ex) {
            }
        }
    }

    public static Bitmap getDefaultArtImg(Context mContext) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // options.inSampleSize = 2;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        // options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap ret;
        ret = BitmapFactory.decodeStream(mContext.getApplicationContext()
                .getResources().openRawResource(R.drawable.playlist_default_0),
                null, options);
        return ret;

    }

    public static Bitmap getDefaultBg(Context mContext, int resId) {
        if (!isBitmapDrawableRecyled(bitmapDrawableCache))
            return null;
        int bgid = getIntPref(mContext, "playerbg", 0);
        switch (bgid) {
            case 1:
                resId = R.drawable.default_bg_1;
                break;
            case 2:
                resId = R.drawable.default_bg_2;
                break;
            case 3:
                resId = R.drawable.default_bg_3;
                break;
            default:
                resId = R.drawable.playlist_default;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        //options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap ret = BitmapFactory.decodeStream(mContext.
                getApplicationContext().getResources().openRawResource(resId),
                null, options);

        return ret;

    }

    public static Bitmap getDefaultBg(Context mContext, int resId, int w, int h) {
        int bgid = getIntPref(mContext, "playerbg", 0);
        switch (bgid) {
            case 1:
                resId = R.drawable.default_bg_1;
                break;
            case 2:
                resId = R.drawable.default_bg_2;
                break;
            case 3:
                resId = R.drawable.default_bg_3;
                break;
            default:
                resId = R.drawable.playlist_default;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), resId, options);
        final int minSideLength = Math.min(w, h);
        options.inSampleSize = computeSampleSize(options, minSideLength,
                w * h);
        //options.inSampleSize = 2;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        options.inJustDecodeBounds = false;
        //options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap ret = BitmapFactory.decodeStream(mContext.
                getApplicationContext().getResources().openRawResource(resId),
                null, options);

        return ret;

    }

    public static Bitmap getResources(Context mContext, int resId, int w, int h) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), resId, options);
        final int minSideLength = Math.min(w, h);
        options.inSampleSize = computeSampleSize(options, minSideLength,
                w * h);
        //options.inSampleSize = 2;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        options.inJustDecodeBounds = false;
        //options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap ret = BitmapFactory.decodeStream(mContext.
                getApplicationContext().getResources().openRawResource(resId),
                null, options);

        return ret;

    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap setDefaultBackground(Context context, View view, int bgId) {
        Bitmap bm = MusicUtils.getDefaultBg(context, bgId, view.getWidth(), view.getHeight());
        if (bm != null) {
            MusicUtils.setBackground(view, bm);
            return bm;
        }
        view.setBackgroundColor(0xff000000);
        return bm;
    }

    public static void setPartyShuffleMenuIcon(Menu menu) {
        MenuItem item = menu.findItem(Defs.PARTY_SHUFFLE);
        if (item != null) {
            int shuffle = MusicUtils.getCurrentShuffleMode();
            if (shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
                //item.setIcon(R.drawable.ic_menu_party_shuffle);
                //item.setTitle(R.string.party_shuffle_off);
            } else {
                //item.setIcon(R.drawable.ic_menu_party_shuffle);
                //item.setTitle(R.string.party_shuffle);
            }
        }
    }

    /*
     * Returns true if a file is currently opened for playback (regardless
     * of whether it's playing or paused).
     */
    public static boolean isMusicLoaded() {
        if (MusicUtils.sService != null) {
            try {
                return sService.getPath() != null;
            } catch (RemoteException ex) {
            }
        }
        return false;
    }

    private final static long[] sEmptyList = new long[0];

    public static long[] getSongListForCursor(Cursor cursor) {
        if (cursor == null) {
            return sEmptyList;
        }
        int len = cursor.getCount();
        long[] list = new long[len];
        cursor.moveToFirst();
        int colidx = -1;
        try {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        } catch (IllegalArgumentException ex) {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        }
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getLong(colidx);
            cursor.moveToNext();
        }
        return list;
    }

    public static long[] getCustomSonglist(Cursor cursor) {
        if (cursor == null) {
            return sEmptyList;
        }
        int len = cursor.getCount();
        long[] list = new long[len];
        cursor.moveToFirst();
        int colidx = -1;
        try {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        } catch (IllegalArgumentException ex) {
            try {
                colidx = cursor.getColumnIndexOrThrow("song_id");
            } catch (IllegalArgumentException e) {
                colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            }
        }
        int data_column = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        for (int i = 0; i < len; i++) {
            String data = cursor.getString(data_column);
            if (data.equals("online")) {
                list[i] = -cursor.getLong(colidx);
            } else {
                list[i] = cursor.getLong(colidx);
            }
            cursor.moveToNext();
        }

        return list;
    }

    public static String[] getSongNameListForCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        int len = cursor.getCount();
        String[] list = new String[len];
        cursor.moveToFirst();
        int colidx = -1;
        try {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.TITLE);
        } catch (IllegalArgumentException ex) {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        }
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getString(colidx);
            cursor.moveToNext();
        }
        return list;
    }

    public static long[] getSongListForArtist(Context context, long id) {
        final String[] ccols = new String[]{MediaStore.Audio.Media._ID};
        StringBuilder where = new StringBuilder();
        //remove by shenqi for some mp3 store as  ringtones
        where.append(MediaStore.Audio.Media.ARTIST_ID + "=" + id);
        //+ " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
        where.append(getWhereBuilder(context, "_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, null);//"sort_key");

        if (cursor != null) {
            long[] list = getSongListForCursor(cursor);
            if (!cursor.isClosed()) {
                cursor.close();
            }
            cursor = null;
            return list;
        }
        return sEmptyList;
    }

    public static long getSongId(Context context, String songName) {
        if (songName == null) {
            return -1;
        }

        final String[] ccols = new String[]{MediaStore.Audio.Media._ID};
        StringBuilder where = new StringBuilder();
        //remove by shenqi for some mp3 store as  ringtones
        if (songName.contains("'")) {
            songName = songName.replace("'", "\"");
        }
        where.append(MediaStore.Audio.Media.TITLE + "='" + songName + "'");
        //+ " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
//        where.append(getWhereBuilder(context, "_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, null);//"sort_key");

        if (cursor != null && cursor.moveToFirst()) {
            long songid = cursor.getLong(0);
            cursor.close();
            return songid;
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return 0;
    }

    public static String getAlbumName(Context context, String songName) {
        final String[] ccols = new String[]{MediaStore.Audio.Media.ALBUM};
        StringBuilder where = new StringBuilder();
        //remove by shenqi for some mp3 store as  ringtones
        if (songName.contains("'")) {
            songName = songName.replace("'", "\"");
        }
        where.append(MediaStore.Audio.Media.TITLE + "='" + songName + "'");
        //+ " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
//        where.append(getWhereBuilder(context, "_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, null);//"sort_key");

        if (cursor != null && cursor.moveToFirst()) {
            String albumName = cursor.getString(0);
            cursor.close();
            return albumName;
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return null;
    }

    public static String[] getSongListForArtist(Context context, String artist) {
        final String[] ccols = new String[]{MediaStore.Audio.Media.TITLE};
        StringBuilder where = new StringBuilder();
        //remove by shenqi for some mp3 store as  ringtones
        if (artist.contains("'"))
            artist = artist.replace("'", "\"");
        where.append(MediaStore.Audio.Media.ARTIST + "='" + artist + "'");
        //+ " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
//        where.append(getWhereBuilder(context, "_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, null);//"sort_key");

        if (cursor != null) {
            String[] list = new String[cursor.getCount()];
            int i = 0;
            while (cursor.moveToNext()) {
                list[i] = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                i++;
            }
            cursor.close();
            cursor = null;
            return list;
        }
        return null;
    }

    public static int getAlbumCountForArtist(Context context, long id) {
        final String[] ccols = new String[]{
                MediaStore.Audio.Media._ID};
        StringBuilder where = new StringBuilder();
        where.append("_id IN (select album_id from audio where artist_id=" + id + ")");
        where.append(getWhereBuilder(context, "album_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, null);

        if (cursor != null) {
            int count = cursor.getCount();
            cursor.close();
            return count;
        }
        return 0;
    }

    public static long[] getSongListForAlbum(Context context, long id, long aid) {
        final String[] ccols = new String[]{MediaStore.Audio.Media._ID};
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.ALBUM_ID + " = " + id + " AND " +
                MediaStore.Audio.Media.IS_MUSIC + "=1");

        //modify by zhaolei,120323,for album count
        if (aid > 0) {
            where.append(" AND " + MediaStore.Audio.Media.ARTIST_ID + " = " + aid);
        }
        //end

        where.append(getWhereBuilder(context, "_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, null);//"sort_key");

        if (cursor != null) {
            long[] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }

    public static long[] getSongListForGenre(Context context, long id) {
        final String[] ccols = new String[]{};
        StringBuilder where = new StringBuilder();
        where.append("_id in (SELECT audio_id FROM audio_genres_map WHERE genre_id = " + id);
        where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1 AND 1=1");
        where.append(MusicUtils.getWhereBuilder(context, "_id", 1));
        where.append(")");
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where.toString(), null, null);// "Upper(sort_key)");

        if (cursor != null) {
            long[] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }

    public static long[] getSongListForOtherGenre(Context context) {
        final String[] ccols = new String[]{MediaStore.Audio.Media._ID};
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media._ID + " not in (select audio_id from audio_genres_map ))");
        where.append(" AND (" + MediaStore.Audio.Media.IS_MUSIC + "=1 AND 1=1");
        where.append(MusicUtils.getWhereBuilder(context, "_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, null);//"Upper(sort_key)");
        if (cursor != null) {
            long[] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }

    public static long[] getSongListForPlaylist(Context context, long plid) {
        final String[] ccols = new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID};
        Cursor cursor = query(context, MediaStore.Audio.Playlists.Members.getContentUri("external", plid),
                ccols, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            long[] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }

    public static void playPlaylist(Context context, long plid) {
        long[] list = getSongListForPlaylist(context, plid);
        if (list != null) {
            playAll(context, list, -1, false);
        }
    }

    public static long[] getAllSongs(Context context) {
        StringBuilder where = new StringBuilder();
        //remove by shenqi for some mp3 store as  ringtones
        //where.append(MediaStore.Audio.Media.IS_MUSIC + "=1");
        where.append("1=1");
        where.append(getWhereBuilder(context, "_id", 0));
        Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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

    public static long[] getAllSongsInDB(Context context) {

        //remove by shenqi for some mp3 store as  ringtones
        //StringBuilder where = new StringBuilder();
        //where.append(MediaStore.Audio.Media.IS_MUSIC + "=1");
        Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID}, null,//where.toString(),
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

    public static boolean ifHasSongs(Context context) {
        Cursor c = null;
        try {
            c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID}, null,
                    null, null);
            if (c != null && c.getCount() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;

        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
    }

    /**
     * Fills out the given submenu with items for "new playlist" and
     * any existing playlists. When the user selects an item, the
     * application will receive PLAYLIST_SELECTED with the Uri of
     * the selected playlist, NEW_PLAYLIST if a new playlist
     * should be created, and QUEUE if the "current playlist" was
     * selected.
     *
     * @param context The context to use for creating the menu items
     * @param sub     The submenu to add the items to.
     */
    public static void makePlaylistMenu(Context context, SubMenu sub, boolean isPlayingView) {
        makePlaylistMenu(context, sub, -1, isPlayingView);
    }

    public static void makePlaylistMenu(Context context, SubMenu sub, int playlistId, boolean isPlayingView) {
        String[] cols = new String[]{
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Playlists.NAME + " != ''" +
                    " AND  name != '" + context.getString(R.string.record) + "'";
            Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    cols, whereclause, null,
                    MediaStore.Audio.Playlists.NAME);
            sub.clear();
            sub.add(1, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
            if (!isPlayingView) {
                sub.add(1, Defs.QUEUE, 0, R.string.queue);
            }
            //sub.add(1, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
            if (cur != null && cur.getCount() > 0) {
                //sub.addSeparator(1, 0);
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    if (playlistId == -1 || playlistId != cur.getLong(0)) {
                        Intent intent = new Intent();
                        intent.putExtra("playlist", cur.getLong(0));
                        //                    if (cur.getInt(0) == mLastPlaylistSelected) {
                        //                        sub.add(0, MusicBaseActivity.PLAYLIST_SELECTED, cur.getString(1)).setIntent(intent);
                        //                    } else {
                        sub.add(1, Defs.PLAYLIST_SELECTED, 0, cur.getString(1)).setIntent(intent);
                        //                    }
                    }
                    cur.moveToNext();
                }
            }
            if (cur != null) {
                cur.close();
            }
        }
    }

    public static void makePlaylistMenu(Context context, AlertDialog.Builder builder, boolean isPlayingView, long[] ids) {
        makePlaylistMenu(context, builder, -1, isPlayingView, ids);
    }

    public static void makePlaylistMenu(final Context context, AlertDialog.Builder builder, int playlistId, boolean isPlayingView, final long[] ids) {
        String[] cols = new String[]{
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };
        ArrayList<CharSequence> arrayList = new ArrayList<CharSequence>();
        final ArrayList<Long> playLists = new ArrayList<Long>();
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Playlists.NAME + " != ''" +
                    " AND  name != '" + context.getString(R.string.record) + "'";
            Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    cols, whereclause, null,
                    MediaStore.Audio.Playlists.NAME);
//            sub.clear();
//            sub.add(1, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
            arrayList.add(context.getString(R.string.new_playlist));
            if (!isPlayingView) {
                arrayList.add(context.getString(R.string.queue));
//                sub.add(1, Defs.QUEUE, 0, R.string.queue);
            }
            //sub.add(1, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
            if (cur != null && cur.getCount() > 0) {
                //sub.addSeparator(1, 0);
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    if (playlistId == -1 || playlistId != cur.getLong(0)) {
                        playLists.add(cur.getLong(0));
//                        Intent intent = new Intent();
//                        intent.putExtra("playlist", cur.getLong(0));
                        //                    if (cur.getInt(0) == mLastPlaylistSelected) {
                        //                        sub.add(0, MusicBaseActivity.PLAYLIST_SELECTED, cur.getString(1)).setIntent(intent);
                        //                    } else {
//                            sub.add(1, Defs.PLAYLIST_SELECTED, 0, cur.getString(1)).setIntent(intent);
                        arrayList.add(cur.getString(1));
                        //                    }
                    }
                    cur.moveToNext();
                }
            }
            if (cur != null) {
                cur.close();
            }
        }
        CharSequence[] items = new CharSequence[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            items[i] = arrayList.get(i);
        }
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                switch (which) {
                    case 0:
                        MusicUtils.addToNewPlaylist(context, ids, Defs.NEW_PLAYLIST);
                        break;
                    case 1:
                        MusicUtils.addToCurrentPlaylist(context, ids);
                        break;
                    default:
                        long playlist = playLists.get(which - 2);
                        MusicUtils.addToPlaylist(context, ids, playlist);
                        break;
                }
            }
        });
        builder.create().show();
    }

    public static void clearPlaylist(Context context, int plid) {

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", plid);
        context.getContentResolver().delete(uri, null, null);
        return;
    }

    public static void deleteTracks(Context context, long[] list) {
        long id = 0;
        long currentId = 0;
        try {
            if (MusicUtils.sService != null && MusicUtils.sService.getQueuePosition() >= 0 && MusicUtils.sService.getQueuePosition() < MusicUtils.sService.getQueue().length)
                currentId = MusicUtils.sService.getQueue()[MusicUtils.sService.getQueuePosition()];
        } catch (RemoteException e1) {
        }
        String[] cols = new String[]{MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID};
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            where.append(list[i]);
            if (i < list.length - 1) {
                where.append(",");
            }
        }
        where.append(")");
        // Make sure database exist when deleting and make sure we have latest news
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;

        }
        Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols,
                where.toString(), null, null);

        if (c != null) {

            // step 1: remove selected tracks from the current playlist, as well
            // as from the album art cache
            try {
                c.moveToFirst();
                while (!c.isAfterLast()) {

                    // remove from current playlist
                    id = c.getLong(0);
                    sService.removeTrack(id);
                    // remove from album art cache
                    long artIndex = c.getLong(2);
                    synchronized (sArtCache) {
                        sArtCache.remove(artIndex);
                    }
                    c.moveToNext();
                }
            } catch (RemoteException ex) {
            }

            // step 2: remove selected tracks from the database
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                if (c != null) {
                    c.close();
                    c = null;
                }
                return;
            }
            try {
                String songNameList = "'" + MusicUtils.getSongName(context, list[0])[0].toString() + "'";
                int length = list.length;

                for (int i = 0; i < length - 1; i++) {
                    songNameList += ", ";
                    songNameList += "'" + MusicUtils.getSongName(context, list[i])[0].toString() + "'";
                }
                deleteFavoriteTracks(context, songNameList);
                context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where.toString(), null);
            } catch (Exception e) {
                // Just in case
                if (c != null) {
                    c.close();
                    c = null;
                }
                return;

            }

            // step 3: remove files from card
            c.moveToFirst();
            while (!c.isAfterLast() &&
                    Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String name = c.getString(1);
                File f = new File(name);
                try {  // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
//                        Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

        String message = context.getResources().getQuantityString(
                R.plurals.NNNtracksdeleted, list.length, Integer.valueOf(list.length));
        /*Added by ruiwei, for Modifying Toast style, 20150211, start*/
        Context activity = ExitApplication.getTopActivity();
        if(null == activity) {
        	activity = context;
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
        // We deleted a number of tracks, which could affect any number of things
        // in the media content domain, so update everything.
        MediaPlaybackService.normal = true;
        if (MediaPlaybackService.isOnlinePlay && id == currentId && !MusicUtils.isProtected) {
            MediaPlaybackService.isOnlinePlay = false;
            Intent intent2 = new Intent(MediaPlaybackService.NEXT_ACTION);
            intent2.putExtra("isToLocal", true);
            intent2.putExtra("isDeleteItem", true);
            context.sendBroadcast(intent2);
        }
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        Intent intent = new Intent();
        intent.putExtra("deleteItemId", list);
        intent.setAction(ACTION_DELETEITEM);
        context.sendBroadcast(intent);
    }

    public static void addToCurrentPlaylist(Context context, long[] list) {
        if (sService == null || list == null) {
            return;
        }
        try {
            if (list.length == 0) {
                return;
            }
            sService.enqueue(list, MediaPlaybackService.LAST);
            String message = context.getResources().getQuantityString(
                    R.plurals.NNNtrackstoplaylist, list.length, Integer.valueOf(list.length));
            /*Added by ruiwei, for Modifying Toast style, 20150211, start*/
            Context activity = ExitApplication.getTopActivity();
            if(null == activity) {
            	activity = context;
            }
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
        } catch (RemoteException ex) {
        }
    }

    private static ContentValues[] sContentValuesCache = null;

    /**
     * @param ids    The source array containing all the ids to be added to the playlist
     * @param offset Where in the 'ids' array we start reading
     * @param len    How many items to copy during this pass
     * @param base   The play order offset to use for this pass
     */
    private static void makeInsertItems(long[] ids, int offset, int len, int base) {
        // adjust 'len' if would extend beyond the end of the source array
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }
        // allocate the ContentValues array, or reallocate if it is the wrong size
        if (sContentValuesCache == null || sContentValuesCache.length != len) {
            sContentValuesCache = new ContentValues[len];
        }
        // fill in the ContentValues array with the right values for this pass
        for (int i = 0; i < len; i++) {
            if (sContentValuesCache[i] == null) {
                sContentValuesCache[i] = new ContentValues();
            }

            sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }

    public static void addToPlaylist(Context context, long[] ids, long playlistid) {
        if (ids == null) {
            // this shouldn't happen (the menuitems shouldn't be visible
            // unless the selected item represents something playable
//            Log.e("MusicBase", "ListSelection null");
        } else {
            int size = ids.length;
            ContentResolver resolver = context.getContentResolver();
            // need to determine the number of items currently in the playlist,
            // so the play_order field can be maintained.
            String[] cols = new String[]{
                    "count(*)"
            };
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
            Cursor cur = resolver.query(uri, cols, null, null, null);
            cur.moveToFirst();
            int base = cur.getInt(0);
            cur.close();
            int numinserted = 0;
            for (int i = 0; i < size; i += 1000) {
                makeInsertItems(ids, i, 1000, base);
                numinserted += resolver.bulkInsert(uri, sContentValuesCache);
            }
            String message = context.getResources().getQuantityString(
                    R.plurals.NNNtrackstoplaylist, numinserted, numinserted);
            /*Added by ruiwei, for Modifying Toast style, 20150211, start*/
            Context activity = ExitApplication.getTopActivity();
            if(null == activity) {
            	activity = context;
            }
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
            //mLastPlaylistSelected = playlistid;
        }
    }

    public static Cursor query(Context context, Uri uri, String[] projection,
                               String selection, String[] selectionArgs, String sortOrder, int limit) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            if (limit > 0) {
                uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception ex) {
            return null;
        }

    }

    public static Cursor query(Context context, Uri uri, String[] projection,
                               String selection, String[] selectionArgs, String sortOrder) {
        return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
    }

    public static boolean isMediaScannerScanning(Context context) {
        boolean result = false;
        Cursor cursor = query(context, MediaStore.getMediaScannerUri(),
                new String[]{MediaStore.MEDIA_SCANNER_VOLUME}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                result = "external".equals(cursor.getString(0));
            }
            cursor.close();
        }

        return result;
    }

    public static long getArtistId(Context mContext, long song_id) {
        if (song_id < 0) return -1;
        long artistid = -1;
        String[] projection = new String[]{MediaStore.Audio.Media.ARTIST_ID};
        Cursor mCursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, "_id=" + song_id, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            artistid = mCursor.getLong(0);
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        return artistid;

    }

    public static String getArtistIdFromAlbum(Context mContext, long albumid) {
        if (albumid < 0) return null;
        String artistid = "";
        String[] projection = new String[]{"artist_id"};
        Cursor mCursor = mContext.getContentResolver().query(
                Uri.parse("content://media/external/audio/albums/" + String.valueOf(albumid)),
                projection, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            artistid = mCursor.getString(0);
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return artistid;

    }

    public static String getArtistName(Context mContext, long artist_id) {
        // TODO Auto-generated method stub
        if (artist_id < 0) return null;
        String artist = null;
        String[] projection = new String[]{"artist"};
        try {
            Cursor mCursor = mContext.getContentResolver().query(
                    Uri.parse("content://media/external/audio/artists/" + String.valueOf(artist_id)),
                    projection, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0 && mCursor.getColumnCount() > 0) {
                mCursor.moveToNext();
                artist = mCursor.getString(0);
            }
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        } catch (SQLiteDiskIOException e) {
            // TODO Auto-generated catch block
            return null;
        }

        return artist;
    }

    public static String getArtistNameOnlyFirst() {
        // TODO Auto-generated method stub
        String artist = null;
        if (sService == null)
            return null;
        try {
            artist = sService.getArtistName();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (artist == null)
            return null;
        if (artist.contains(","))
            artist = artist.substring(0, artist.indexOf(","));
        if (artist.contains("-"))
            artist = artist.substring(0, artist.indexOf("-"));
        if (artist.contains("，"))
            artist = artist.substring(0, artist.indexOf("，"));
        if (artist.contains("&"))
            artist = artist.substring(0, artist.indexOf("&"));
        if (artist.contains("+"))
            artist = artist.substring(0, artist.indexOf("+"));
        return artist;
    }

    public static String getAlbumName(Context mContext, long album_id) {
        // TODO Auto-generated method stub
        if (album_id < 0) return null;
        String album = null;
        String[] projection = new String[]{"album"};
        Cursor mCursor = mContext.getContentResolver().query(
                Uri.parse("content://media/external/audio/albums/" + String.valueOf(album_id)),
                projection, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0 && mCursor.getColumnCount() > 0) {
            mCursor.moveToNext();
            album = mCursor.getString(0);
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        return album;
    }


    public static String[] getSongName(Context mContext, long trackId) {

        String[] mCursorCols = new String[]{
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION};
        Cursor mCursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mCursorCols, "_id=" + trackId, null, null);
        long artist_id = 0;
        String songname = "";
        String artistname = "";
        String duration = "";
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0) {
                artist_id = mCursor.getInt(0);
                artistname = getArtistName(mContext, artist_id);
                songname = mCursor.getString(1);
                int secs = mCursor.getInt(2) / 1000;
                if (secs != 0) {
                    duration = MusicUtils.makeTimeString(mContext, secs);
                }
            }

            mCursor.close();
            mCursor = null;
        }
        String[] ret = {songname, artistname, duration};
        return ret;
    }

    public static String getSongPath(Context mContext, long trackId) {

        String songPath = null;
        if (trackId < 0) {
            return null;
        }
        String[] mCursorCols = new String[]{
                MediaStore.Audio.Media.DATA};
        try {
            Cursor mCursor = mContext.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mCursorCols, "_id=" + trackId, null, null);

            if (mCursor != null) {
                mCursor.moveToFirst();
                if (mCursor.getCount() > 0) {
                    songPath = mCursor.getString(0);
                }

                mCursor.close();
            }
        } catch (Exception ex) {
//			Log.e(TAG, "exception !! " + ex);
        }

        return songPath;
    }


    public static void setSpinnerState(Activity a) {
        if (isMediaScannerScanning(a)) {
            // start the progress spinner
            a.setProgressBarIndeterminateVisibility(true);
        } else {
            // stop the progress spinner
            a.setProgressBarIndeterminateVisibility(false);
        }
    }

    private static String mLastSdStatus = null;

    public static void displayDatabaseError(Activity a, boolean isMounted) {
        if (a.isFinishing()) {
            // When switching tabs really fast, we can end up with a null
            // cursor (not sure why), which will bring us here.
            // Don't bother showing an error message in that case.
            return;
        }

        String status = Environment.getExternalStorageState();
        int title, message;
        if ((null != mLastSdStatus) && (mLastSdStatus.equals(status))) {
//            Log.d(TAG, "displayDatabaseError: SD status is not change");
            return;
        }
//        Log.d(TAG, "displayDatabaseError: SD status=" + status);
        mLastSdStatus = status;

        //if (android.os.Environment.isExternalStorageRemovable()) {
//            title = R.string.sdcard_error_title;
//            message = R.string.sdcard_error_message;
        //} else {
        //    title = R.string.sdcard_error_title_nosdcard;
        //    message = R.string.sdcard_error_message_nosdcard;
        //}

        if (status.equals(Environment.MEDIA_SHARED) ||
                status.equals(Environment.MEDIA_UNMOUNTED)) {
            //if (android.os.Environment.isExternalStorageRemovable()) {
//                title = R.string.sdcard_busy_title;
//                message = R.string.sdcard_busy_message;
            //} else {
            //    title = R.string.sdcard_busy_title_nosdcard;
            //    message = R.string.sdcard_busy_message_nosdcard;
            //}
        } else if (status.equals(Environment.MEDIA_REMOVED)) {
            //if (android.os.Environment.isExternalStorageRemovable()) {
//                title = R.string.sdcard_missing_title;
            message = R.string.sdcard_missing_message;
            //} else {
            //    title = R.string.sdcard_missing_title_nosdcard;
            //    message = R.string.sdcard_missing_message_nosdcard;
            //}
        } else if (status.equals(Environment.MEDIA_MOUNTED) && isMounted) {
            // The card is mounted, but we didn't get a valid cursor.
            // This probably means the mediascanner hasn't started scanning the
            // card yet (there is a small window of time during boot where this
            // will happen).
            a.setTitle("");
            Intent intent = new Intent();
            intent.setClass(a, ScanningProgress.class);
            a.startActivityForResult(intent, Defs.SCAN_DONE);
        }

//        a.setTitle(title);
        View v = a.findViewById(R.id.sd_message);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
        v = a.findViewById(R.id.sd_icon);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
        v = a.findViewById(android.R.id.list);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
//        v = a.findViewById(R.id.buttonbar);
//        if (v != null) {
//            v.setVisibility(View.GONE);
//        }
//        v = a.findViewById(R.id.nowplaying);
//        if (v != null) {
//            v.setVisibility(View.GONE);
//        }
        TextView tv = (TextView) a.findViewById(R.id.sd_message);
//        tv.setText(message);
    }

    public static void hideDatabaseError(Activity a) {
        /*View v = a.findViewById(R.id.sd_message);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
        v = a.findViewById(R.id.sd_icon);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
        v = a.findViewById(android.R.id.list);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }*/
    }

    static protected Uri getContentURIForPath(String path) {
        return Uri.fromFile(new File(path));
    }


    /*  Try to use String.format() as little as possible, because it creates a
     *  new Formatter every time you call it, which is very inefficient.
     *  Reusing an existing Formatter more than tripled the speed of
     *  makeTimeString().
     *  This Formatter/StringBuilder are also used by makeAlbumSongsLabel()
     */
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    public static String makeTimeString(Context context, long secs) {
        String durationformat = context.getString(
                secs < 3600 ? R.string.durationformatshort : R.string.durationformatlong);

        /* Provide multiple arguments so the format can be changed easily
         * by modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format(Locale.getDefault(), durationformat, timeArgs).toString();
    }

    public static void shuffleAll(Context context, Cursor cursor) {
        playAll(context, cursor, -1, true);
    }

    public static void playAll(Context context, Cursor cursor) {
        playAll(context, cursor, 0, false);
    }

    public static void playAll(Context context, Cursor cursor, int position) {
        playAll(context, cursor, position, false);
    }

    public static void playAll(Context context, long[] list, int position) {
        playAll(context, list, position, false);
    }

    private static void playAll(Context context, Cursor cursor, int position, boolean force_shuffle) {

        long[] list = getSongListForCursor(cursor);
        playAll(context, list, position, force_shuffle);
    }

    private static void playAll(Context context, long[] list, int position, boolean force_shuffle) {
        if (list == null || list.length == 0 || sService == null) {
//            Log.d("MusicUtils", "attempt to play empty song list");
            // Don't try to play empty playlists. Nothing good will come of it.
            // String message = context.getString(R.string.emptyplaylist, list.length);
            // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (force_shuffle) {
                sService.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
            }
            long curid = sService.getAudioId();
            int curpos = sService.getQueuePosition();
            if (position != -1 && curpos == position && curid == list[position]) {
                // The selected file is the file that's currently playing;
                // figure out if we need to restart with a new playlist,
                // or just launch the playback activity.
                long[] playlist = sService.getQueue();
                if (Arrays.equals(list, playlist)) {
                    // we don't need to set a new list, but we should resume playback if needed
                    sService.play();
                    return; // the 'finally' block will still run
                }
            }
            if (position < 0) {
                position = 0;
            }
            sService.open(list, force_shuffle ? -1 : position);
            // sService.play();
        } catch (RemoteException ex) {
        } finally {
            Intent intent = new Intent("com.lewa.player.PLAY_VIEWER")
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    public static String[] getTrackNameNext(Context mContext) {
        String nextTrackName[] = new String[4];
        try {
            long[] playlist = sService.getQueue();
            int len = playlist.length;
            int curpos = sService.getQueuePosition();
            int repeatMode = sService.getRepeatMode();
            int nexNum = len - curpos - 1;
            int p;
            if (repeatMode != MediaPlaybackService.REPEAT_ALL) {
                p = nexNum > 4 ? 4 : nexNum;
            } else {
                p = 4;
            }
            if (nexNum >= 0) {
                for (int i = (curpos + 1) % len; i < (curpos + 1) % len + p; i++) {
                    String[] projection = new String[]{MediaStore.Audio.Media.TITLE};
                    Cursor mCursor = query(mContext,
                            Uri.parse("content://media/external/audio/media/" + String.valueOf(playlist[i % len])),
                            projection, null, null, null);
                    if (mCursor != null) {
                        mCursor.moveToFirst();
                    }
                    //String next = mCursor.getString(0);
                    nextTrackName[i - (curpos + 1) % len] = mCursor.getString(0);
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                }
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }

        return nextTrackName;

    }

    public static void clearQueue() {
        try {
            sService.removeTracks(0, Integer.MAX_VALUE);
        } catch (RemoteException ex) {
        }
    }

    // A really simple BitmapDrawable-like class, that doesn't do
    // scaling, dithering or filtering.
    private static class FastBitmapDrawable extends Drawable {
        private Bitmap mBitmap;

        public FastBitmapDrawable(Bitmap b) {
            mBitmap = b;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }
    }

    private static int sArtId = -2;
    private static Bitmap mCachedBit = null;
    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final HashMap<Long, Drawable> sArtCache = new HashMap<Long, Drawable>();
    private static int sArtCacheId = -1;

    static {
        // for the cache,
        // 565 is faster to decode and display
        // and we don't want to dither here because the image will be scaled down later
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptionsCache.inDither = false;

        sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        sBitmapOptions.inDither = false;
    }

    public static void initAlbumArtCache() {
        try {
            int id = sService.getMediaMountedCount();
            if (id != sArtCacheId) {
                clearAlbumArtCache();
                sArtCacheId = id;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void clearAlbumArtCache() {
        synchronized (sArtCache) {
            sArtCache.clear();
        }
    }

    public static Drawable getCachedArtwork(Context context, long artIndex, String albumName, BitmapDrawable defaultArtwork) {
        Drawable d = null;
        synchronized (sArtCache) {
            d = sArtCache.get(artIndex);
        }
        if (d == null) {
            d = defaultArtwork;
            final Bitmap icon = defaultArtwork.getBitmap();
            int w = icon.getWidth();
            int h = icon.getHeight();
            Bitmap b = MusicUtils.getArtworkQuick(context, artIndex, w, h);
            if (b == null) {
                b = MusicUtils.getLocalBitmap(context, albumName, Environment.getExternalStorageDirectory() + DownLoadAllPicsAsync.ALBUM_PATH);
                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same bitmap
                        if (tmp != b) b.recycle();
                        b = tmp;
                    }
                }
            }
            if (b != null) {
                d = new FastBitmapDrawable(b);
                synchronized (sArtCache) {
                    // the cache may have changed since we checked
                    Drawable value = sArtCache.get(artIndex);
                    if (value == null) {
                        sArtCache.put(artIndex, d);
                    } else {
                        d = value;
                    }
                }
            }
        }
        return d;
    }

    public static Drawable getCachedArtwork(Context context, long artIndex, String albumName, BitmapDrawable defaultArtwork, int w, int h) {
        Drawable d = null;
        synchronized (sArtCache) {
            d = sArtCache.get(artIndex);
        }
        if (d == null) {
            d = defaultArtwork;
//            final Bitmap icon = defaultArtwork.getBitmap();
//            int w = icon.getWidth();
//            int h = icon.getHeight();
            Bitmap b = MusicUtils.getArtworkQuick(context, artIndex, w, h);
            if (b == null) {
                b = MusicUtils.getLocalBitmap(context, albumName, Environment.getExternalStorageDirectory() + DownLoadAllPicsAsync.ALBUM_PATH);
                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same bitmap
                        if (tmp != b) b.recycle();
                        b = tmp;
                    }
                }
            }
            if (b != null) {
                d = new FastBitmapDrawable(b);
                synchronized (sArtCache) {
                    // the cache may have changed since we checked
                    Drawable value = sArtCache.get(artIndex);
                    if (value == null) {
                        sArtCache.put(artIndex, d);
                    } else {
                        d = value;
                    }
                }
            }
        }
        return d;
    }


    public static Drawable getDrawable(Context context, String name, BitmapDrawable defaultArtwork, String path) {
        Drawable d = null;
        if (d == null) {
//            d = defaultArtwork;
            final Bitmap icon = defaultArtwork.getBitmap();
            int w = icon.getWidth();
            int h = icon.getHeight();
            Bitmap b = MusicUtils.getLocalBitmap(context, name, path);
            if (b != null) {
                // finally rescale to exactly the size we need
                if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                    Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                    // Bitmap.createScaledBitmap() can return the same bitmap
                    if (tmp != b) b.recycle();
                    b = tmp;
                }
            }
            if (b != null) {
                d = new FastBitmapDrawable(b);
            }
        }
        return d;
    }

    public static Bitmap getLocalBitmap(Context context, String name, String path) {
        if (name == null)
            return null;
        Bitmap b = null;
        InputStream isImg = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdCardDir = path;

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                //modified by wangliqiang
                isImg = new FileInputStream(sdCardDir + name + ".jpg");
                b = BitmapFactory.decodeStream(isImg, null, options);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            } finally {
                try {
                    if (isImg != null)
                        isImg.close();
                } catch (IOException e) {
                }
                isImg = null;
            }
        }
        return b;
    }

    public static Bitmap getLocalBitmap(Context context, String path, int w, int h) {
        Bitmap b = null;
        InputStream isImg = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //String sdCardDir = path;  //no use
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
//                final int minSideLength = Math.min(w, h);
//                options.inSampleSize = computeSampleSize(options, minSideLength,
//                        w * h);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            if (imageHeight > h || imageWidth > w) {

                // Calculate ratios of height and width to requested height and width
                final int heightRatio = Math.round((float) imageHeight / (float) h);
                final int widthRatio = Math.round((float) imageWidth / (float) w);

                // Choose the smallest ratio as inSampleSize value, this will guarantee
                // a final image with both dimensions larger than or equal to the
                // requested height and width.
                options.inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            try {
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inJustDecodeBounds = false;
                //modified by wangliqiang
                isImg = new FileInputStream(path);
                b = BitmapFactory.decodeStream(isImg, null, options);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    if (isImg != null)
                        isImg.close();
                } catch (IOException e) {
                }
                isImg = null;
            }

        }
        return b;
    }

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    private static Bitmap getArtworkQuick(Context context, long album_id, int w, int h) {
        // NOTE: There is in fact a 1 pixel border on the right side in the ImageView
        // used to display this drawable. Take it into account now, so we don't have to
        // scale later.
        w -= 1;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth > w && nextHeight > h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same bitmap
                        if (tmp != b) b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     * This method always returns the default album art icon when no album art is found.
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id) {
        return getArtwork(context, song_id, album_id, true);
    }

    /**
     * Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id,
                                    boolean allowdefault) {

        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context);
            }
            return null;
        }

        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }

        return null;
    }

    // get album art for specified file
    private static final String sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();

    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        byte[] art = null;
        String path = null;

        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }

        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (IllegalStateException ex) {
        } catch (FileNotFoundException ex) {
        }
        if (bm != null) {
            mCachedBit = bm;
        }
        return bm;
    }

    private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return null;
        //BitmapFactory.decodeStream(
        //     context.getResources().openRawResource(R.drawable.albumart_mp_unknown), null, opts);//null
    }

    public static int getIntPref(Context context, String name, int def) {
        SharedPreferences prefs =
                context.getSharedPreferences(MUSIC_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getInt(name, def);
    }

    public static void setIntPref(Context context, String name, int value) {
        SharedPreferences prefs =
                context.getSharedPreferences(MUSIC_PREFERENCES, Context.MODE_PRIVATE);
        Editor ed = prefs.edit();
        ed.putInt(name, value);
        ed.commit();
        //SharedPreferencesCompat.apply(ed);
    }

    public static void setRingtone(Context context, long id) { //modify func by sjxu for bug 65705
        ContentResolver resolver = context.getContentResolver();
        // Set the flag in the database to mark this as a ringtone
        Uri ringUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.Media.IS_ALARM, "1");
            resolver.update(ringUri, values, null, null);
        } catch (Exception ex) { // most likely the card just got unmounted           
            //Log.e(TAG, "couldn't set ringtone flag for id " + id);
            ex.printStackTrace();
            return;
        }

        String[] cols = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE
        };

        String where = MediaStore.Audio.Media._ID + "=" + id;
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cols, where, null, null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, ringUri.toString());
//                Settings.System.putString(resolver, Settings.System.RINGTONE_2, ringUri.toString());
                //try {
                    Method[] methods = Settings.System.class.getMethods();
                    Method method = Settings.System.class.getDeclaredMethod("putString", ContentResolver.class, String.class, String.class);
                    method.invoke(null, resolver, "ringtone_2", ringUri.toString());
               // } catch (Exception e) {
                    //e.printStackTrace();
                //}
                String message = context.getString(R.string.ringtone_set, cursor.getString(2));
                /*Added by ruiwei, for Modifying Toast style, 20150211, start*/
                Context activity = ExitApplication.getTopActivity();
                if(null == activity) {
                	activity = context;
                }
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
            }
        } catch(Exception e) {
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    static int sActiveTabIndex = -1;


    static void updateNowPlaying(Activity a) {
       /* View nowPlayingView = a.findViewById(R.id.nowplaying);
        if (nowPlayingView == null) {
            return;
        }
        try {
            boolean withtabs = false;
            Intent intent = a.getIntent();
            if (intent != null) {
                withtabs = intent.getBooleanExtra("withtabs", false);
            }
            if (true && MusicUtils.sService != null && MusicUtils.sService.getAudioId() != -1) {
                TextView title = (TextView) nowPlayingView.findViewById(R.id.title);
                TextView artist = (TextView) nowPlayingView.findViewById(R.id.artist);
                title.setText(MusicUtils.sService.getTrackName());
                String artistName = MusicUtils.sService.getArtistName();
                if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
                    artistName = a.getString(R.string.unknown_artist_name);
                }
                artist.setText(artistName);
                //mNowPlayingView.setOnFocusChangeListener(mFocuser);
                //mNowPlayingView.setOnClickListener(this);
                nowPlayingView.setVisibility(View.VISIBLE);
                nowPlayingView.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        Context c = v.getContext();
                        c.startActivity(new Intent(c, MediaPlaybackActivity.class));
                    }});
                return;
            }
        } catch (RemoteException ex) {
        }
        nowPlayingView.setVisibility(View.GONE);*/
    }

    public static BitmapDrawable setBackground(View v, Bitmap bm) {
        if (v == null)
            return null;
        int vwidth = v.getWidth();
        int vheight = v.getHeight();
        boolean isCacheRecyle = isBitmapDrawableRecyled(bitmapDrawableCache);
        if (bm == null && isCacheRecyle || vwidth <= 0 || vheight <= 0) {
            v.setBackgroundResource(0);
            return null;
        }
        if (!isCacheRecyle) {
//            MusicUtils.recyleBitmap(bm);
            v.setBackgroundDrawable(bitmapDrawableCache);
            return bitmapDrawableCache;
        }
        int bwidth = bm.getWidth();
        int bheight = bm.getHeight();
        float scalex = (float) vwidth / bwidth;
        float scaley = (float) vheight / bheight;
        float scale = Math.max(scalex, scaley) * 1.005f;

        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bg = null;
        try {
            bg = Bitmap.createBitmap(vwidth, vheight, config);
        } catch (OutOfMemoryError e) {
            System.gc();
            System.runFinalization();
            v.setBackgroundResource(0);
            return null;
        }
        Canvas c = new Canvas(bg);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        ColorMatrix greymatrix = new ColorMatrix();
        //greymatrix.setSaturation(0);
        ColorMatrix darkmatrix = new ColorMatrix();
        darkmatrix.setScale(0.5f, 0.5f, 0.5f, 1.0f);
        greymatrix.postConcat(darkmatrix);
        ColorFilter filter = new ColorMatrixColorFilter(greymatrix);
        paint.setColorFilter(filter);
        Matrix matrix = new Matrix();
        matrix.setTranslate(-bwidth / 2, -bheight / 2); // move bitmap center to origin
//        matrix.postRotate(10);
        matrix.postScale(scale, scale);
        matrix.postTranslate(vwidth / 2, vheight / 2);  // Move bitmap center to view center
        c.drawBitmap(bm, matrix, paint);
//        if(!bm.isRecycled())
//            bm.recycle();
        bitmapDrawableCache = new BitmapDrawable(bg);
        sArtCache.put((long) -100, bitmapDrawableCache);
//        addBitmapToMemoryCathe("bitmapDrawableCache", bitmapDrawableCache.getBitmap());
        v.setBackgroundDrawable(bitmapDrawableCache);

        return bitmapDrawableCache;
    }

    public static BitmapDrawable setBackground(WeakReference<View> vieWeakReference, Bitmap bm) {
        View v = vieWeakReference.get();
        if (v == null)
            return null;
        int vwidth = v.getWidth();
        int vheight = v.getHeight();
        boolean isCacheRecyle = isBitmapDrawableRecyled(bitmapDrawableCache);
        if (bm == null && isCacheRecyle || vwidth <= 0 || vheight <= 0) {
            v.setBackgroundResource(0);
            return null;
        }
        if (!isCacheRecyle) {
            Log.i("wangliqiang", "cache");
//            MusicUtils.recyleBitmap(bm);
            v.setBackgroundDrawable(bitmapDrawableCache);
            return bitmapDrawableCache;
        }
        int bwidth = bm.getWidth();
        int bheight = bm.getHeight();
        float scalex = (float) vwidth / bwidth;
        float scaley = (float) vheight / bheight;
        float scale = Math.max(scalex, scaley) * 1.005f;

        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bg = null;
        try {
            bg = Bitmap.createBitmap(vwidth, vheight, config);
        } catch (OutOfMemoryError e) {
            System.gc();
            System.runFinalization();
            v.setBackgroundResource(0);
            return null;
        }
        Canvas c = new Canvas(bg);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        ColorMatrix greymatrix = new ColorMatrix();
        //greymatrix.setSaturation(0);
        ColorMatrix darkmatrix = new ColorMatrix();
        darkmatrix.setScale(0.5f, 0.5f, 0.5f, 1.0f);
        greymatrix.postConcat(darkmatrix);
        ColorFilter filter = new ColorMatrixColorFilter(greymatrix);
        paint.setColorFilter(filter);
        Matrix matrix = new Matrix();
        matrix.setTranslate(-bwidth / 2, -bheight / 2); // move bitmap center to origin
//        matrix.postRotate(10);
        matrix.postScale(scale, scale);
        matrix.postTranslate(vwidth / 2, vheight / 2);  // Move bitmap center to view center
        c.drawBitmap(bm, matrix, paint);
//        if(!bm.isRecycled())
//            bm.recycle();
        bitmapDrawableCache = new BitmapDrawable(bg);
        sArtCache.put((long) -100, bitmapDrawableCache);
        if (v != null)
            v.setBackgroundDrawable(bitmapDrawableCache);

        return bitmapDrawableCache;
    }


    public static Bitmap getBackgroundBitmap(View v, Bitmap bm) {


        int vwidth = v.getWidth();
        int vheight = v.getHeight();
        if (bm == null || vwidth <= 0 || vheight <= 0) {
            return null;
        }
        int bwidth = bm.getWidth();
        int bheight = bm.getHeight();
        float scalex = (float) vwidth / bwidth;
        float scaley = (float) vheight / bheight;
        float scale = Math.max(scalex, scaley) * 1.005f;

        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bg = null;
        try {
            bg = Bitmap.createBitmap(vwidth, vheight, config);
        } catch (OutOfMemoryError e) {
            System.gc();
            System.runFinalization();
            return null;
        }
        Canvas c = new Canvas(bg);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        ColorMatrix greymatrix = new ColorMatrix();
        //greymatrix.setSaturation(0);
        ColorMatrix darkmatrix = new ColorMatrix();
        darkmatrix.setScale(0.5f, 0.5f, 0.5f, 1.0f);
        greymatrix.postConcat(darkmatrix);
        ColorFilter filter = new ColorMatrixColorFilter(greymatrix);
        paint.setColorFilter(filter);
        Matrix matrix = new Matrix();
        matrix.setTranslate(-bwidth / 2, -bheight / 2); // move bitmap center to origin
//        matrix.postRotate(10);
        matrix.postScale(scale, scale);
        matrix.postTranslate(vwidth / 2, vheight / 2);  // Move bitmap center to view center
        c.drawBitmap(bm, matrix, paint);
        if (!bm.isRecycled())
            bm.recycle();
//        v.setBackgroundDrawable(new BitmapDrawable(bg));
        return bg;
    }

    public static Bitmap getBackgroundBitmap(int vwidth, int vheight, Bitmap bm) {

        if (bm == null) {
            return null;
        }

        int bwidth = bm.getWidth();
        int bheight = bm.getHeight();
        float scalex = (float) vwidth / bwidth;
        float scaley = (float) vheight / bheight;
        float scale = Math.max(scalex, scaley) * 1.005f;

        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bg = null;
        try {
            bg = Bitmap.createBitmap(vwidth, vheight, config);
        } catch (OutOfMemoryError e) {
            System.gc();
            System.runFinalization();
            return null;
        }
        Canvas c = new Canvas(bg);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        ColorMatrix greymatrix = new ColorMatrix();
        //greymatrix.setSaturation(0);
        ColorMatrix darkmatrix = new ColorMatrix();
        darkmatrix.setScale(0.5f, 0.5f, 0.5f, 1.0f);
        greymatrix.postConcat(darkmatrix);
        ColorFilter filter = new ColorMatrixColorFilter(greymatrix);
        paint.setColorFilter(filter);
        Matrix matrix = new Matrix();
        matrix.setTranslate(-bwidth / 2, -bheight / 2); // move bitmap center to origin
//        matrix.postRotate(10);
        matrix.postScale(scale, scale);
        matrix.postTranslate(vwidth / 2, vheight / 2);  // Move bitmap center to view center
        c.drawBitmap(bm, matrix, paint);
//        if(!bm.isRecycled())
//            bm.recycle();
//        v.setBackgroundDrawable(new BitmapDrawable(bg));
        return bg;
    }

    public static void darkBackground() {
        Paint paint = new Paint();
        ColorMatrix greymatrix = new ColorMatrix();
        greymatrix.setSaturation(0);
        ColorMatrix darkmatrix = new ColorMatrix();
        darkmatrix.setScale(.3f, .3f, .3f, 1.0f);
        greymatrix.postConcat(darkmatrix);
        ColorFilter filter = new ColorMatrixColorFilter(greymatrix);
        paint.setColorFilter(filter);
    }

    static int getCardId(Context context) {
        ContentResolver res = context.getContentResolver();
        Cursor c = res.query(Uri.parse("content://media/external/fs_id"), null, null, null, null);
        int id = -1;
        if (c != null) {
            c.moveToFirst();
            id = c.getInt(0);
            c.close();
        }
        return id;
    }

    static class LogEntry {
        Object item;
        long time;

        LogEntry(Object o) {
            item = o;
            time = System.currentTimeMillis();
        }

        void dump(PrintWriter out) {
            sTime.set(time);
            out.print(sTime.toString() + " : ");
            if (item instanceof Exception) {
                ((Exception) item).printStackTrace(out);
            } else {
                out.println(item);
            }
        }
    }

    private static LogEntry[] sMusicLog = new LogEntry[100];
    private static int sLogPtr = 0;
    private static Time sTime = new Time();


    static void debugLog(Object o) {

        sMusicLog[sLogPtr] = new LogEntry(o);
        sLogPtr++;
        if (sLogPtr >= sMusicLog.length) {
            sLogPtr = 0;
        }
    }

    static void debugDump(PrintWriter out) {
        for (int i = 0; i < sMusicLog.length; i++) {
            int idx = (sLogPtr + i);
            if (idx >= sMusicLog.length) {
                idx -= sMusicLog.length;
            }
            LogEntry entry = sMusicLog[idx];
            if (entry != null) {
                entry.dump(out);
            }
        }
    }

    public static void deleteItems(final Context context, String desc, final long[] itemList) {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setTitle(context.getResources().getString(R.string.title_dialog_xml));
        alertDialog.setMessage(desc);

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getResources()
                .getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTracks(context, itemList);
                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources()
                .getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub

                    }


                });
        alertDialog.show();
    }

    public static void addToNewPlaylist(Context context, long[] id, int resultType) {
        if (id == null) {
            return;
        }
        ArrayList<Integer> arrSongsId = new ArrayList<Integer>();
        int size = id.length;
        for (int i = 0; i < size; i++) {
            arrSongsId.add(new Integer((int) id[i]));
        }
        Intent intent = new Intent(MusicUtils.ACTION_ADD_PLAYLIST);
        intent.putIntegerArrayListExtra("song_id", arrSongsId);
        if (MediaPlaybackService.isOnlinePlay && sService != null) {
            try {
                intent.putExtra("artist", sService.getArtistName());
                intent.putExtra("title", sService.getTrackName());
                intent.putExtra("duration", sService.duration());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if (resultType >= 0)
            context.startActivity(intent);
        else
            ((Activity) context).startActivityForResult(intent, resultType);
    }

    public static int[] storeSortKey(Cursor cursor, int sortKeyIdx) {
        if (cursor == null || cursor.getCount() == 0)
            return null;
        int totalCount = cursor.getCount();
        cursor.moveToFirst();
        int[] sortKeyArray = new int[cursor.getCount()];
        sortKeyArray[0] = cursor.getPosition();
        if (totalCount == 1) {
            return sortKeyArray;
        }
        String lastSortKey = cursor.getString(sortKeyIdx).substring(0, 1);
        int lastNum = 0;
        cursor.moveToNext();
        for (int i = 1; i < cursor.getCount(); i++) {
            String currentSortKey = cursor.getString(sortKeyIdx).substring(0, 1);
            if ((currentSortKey.toUpperCase()).equals(lastSortKey.toUpperCase())) {
                sortKeyArray[i] = lastNum;
            } else {
                sortKeyArray[i] = cursor.getPosition();
                lastNum = cursor.getPosition();
            }
            lastSortKey = currentSortKey;
            cursor.moveToNext();
        }
        return sortKeyArray;
    }

    public static String[] getFolderPath(Context context) {
        createLewaDbHelp(context);
        //lewaDBhelp = new PlayerDBHelper(context,"com_lewa_player.db", true);
        String[] lewaFolderPath;
        synchronized (lewaDBhelp) {
            lewaFolderPath = lewaDBhelp.getDBFolder();
        }

        return lewaFolderPath;
    }

    public static void updateFolderPath(Context context, String[] path) {
        createLewaDbHelp(context);
        //lewaDBhelp = new PlayerDBHelper(context,"com_lewa_player.db", true);
        lewaDBhelp.updateDBFolder(path);
        MusicFilesObserver.getInstance(context).restartWatching();
    }

    public static void insertFolderPath(Context context, String[] path) {
        createLewaDbHelp(context);
        lewaDBhelp.insertDBFolder(path);
        MusicFilesObserver.getInstance(context).restartWatching();
    }

    public static void deleteFolderPath(Context context, String path) {
        createLewaDbHelp(context);
        //lewaDBhelp = new PlayerDBHelper(context,"com_lewa_player.db", true);
        lewaDBhelp.deleteDBFolder(path);
        MusicFilesObserver.getInstance(context).restartWatching();
    }

    public static ArrayList<String> getPathList(Context context) {
        Cursor dataCursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                //remove by shenqi for some mp3 store as  ringtones
                new String[]{MediaStore.Audio.Media.DATA}, //MediaStore.Audio.Media.IS_MUSIC + "=1"
                " _data not like '%LEWA/Voice_Recorder%'"
                        + " and _data not like '%LEWA/PIM%'",
                null, MediaStore.Audio.Media.DATA);
        ArrayList<String> list = new ArrayList<String>();
        if (dataCursor == null || dataCursor.getCount() == 0) {
            if (dataCursor != null) {
                dataCursor.close();
            }
            return list;
        }
        String data;
        String subData;
        if (dataCursor != null) {
            try {
                dataCursor.moveToFirst();
                for (int i = 0; i < dataCursor.getCount(); i++) {
                    data = dataCursor.getString(dataCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    subData = data.substring(0, data.lastIndexOf("/"));
                    dataCursor.moveToNext();
                    if (list.contains(subData))
                        continue;
                    else {
                        list.add(subData);
                    }
                }
            } catch (Exception e) {
//                Log.e(TAG, "e = " + e);
            } finally {
                dataCursor.close();
            }
        }
        return list;
    }

    public static ArrayList<String> getDownLoadList(Context context) {
        Cursor dataCursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                //remove by shenqi for some mp3 store as  ringtones
                new String[]{MediaStore.Audio.Media.DISPLAY_NAME}, //MediaStore.Audio.Media.IS_MUSIC + "=1"
                " _data  like '%LEWA/music/mp3%'",
                null, MediaStore.Audio.Media.DISPLAY_NAME);
        ArrayList<String> list = new ArrayList<String>();
        if (dataCursor == null || dataCursor.getCount() == 0) {
            if (dataCursor != null) {
                dataCursor.close();
            }
            return list;
        }
        String data;
        if (dataCursor != null) {
            try {
                dataCursor.moveToFirst();
                for (int i = 0; i < dataCursor.getCount(); i++) {
                    data = dataCursor.getString(dataCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                    dataCursor.moveToNext();
                    if (list.contains(data))
                        continue;
                    else {
                        list.add(data);
                    }
                }
            } catch (Exception e) {
//                Log.e(TAG, "e = " + e);
            } finally {
                dataCursor.close();
            }
        }
        return list;
    }

    public static ArrayList<String> getAlbumDownLoadList(Context context) {
        Cursor dataCursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                //remove by shenqi for some mp3 store as  ringtones
                null, //MediaStore.Audio.Media.IS_MUSIC + "=1"
                " _data  like '%LEWA/music/mp3%'",
                null, MediaStore.Audio.Media.DISPLAY_NAME);
        ArrayList<String> list = new ArrayList<String>();
        if (dataCursor == null || dataCursor.getCount() == 0) {
            if (dataCursor != null) {
                dataCursor.close();
            }
            return list;
        }
        String data;
        String album;
        String title;
        if (dataCursor != null) {
            try {
                dataCursor.moveToFirst();
                for (int i = 0; i < dataCursor.getCount(); i++) {
                    album = dataCursor.getString(dataCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    title = dataCursor.getString(dataCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    data = album + "-" + title;
                    dataCursor.moveToNext();
                    if (list.contains(data))
                        continue;
                    else {
                        list.add(data);
                    }
                }
            } catch (Exception e) {
//                Log.e(TAG, "e = " + e);
            } finally {
                dataCursor.close();
            }
        }
        return list;
    }

    public static ArrayList<Long> getFolderAudioId(Context context, String folderPath, int flag) {
        ArrayList<Long> audioIdList = new ArrayList<Long>();
        String[] cols = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID
        };
        Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols,
                MediaStore.Audio.Media.DATA + " LIKE " + "'" + folderPath + "/" + "%"
                        + "'",//+" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1",
                null, null);
        if (c != null && c.getCount() > 0) {
            int count = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < count; i++) {
                if (c.getString(1).lastIndexOf("/") == folderPath.length()
                        && c.getString(1).startsWith(folderPath)) {
                    switch (flag) {
                        case 0:     //all tracks
                        case 3:     //genre
                            audioIdList.add(Long.valueOf(c.getLong(0)));
                            break;
                        case 1:     //album
                            audioIdList.add(Long.valueOf(c.getLong(2)));
                            break;
                        case 2:     //artist
                            audioIdList.add(Long.valueOf(c.getLong(2)));
                            break;
                    }
                }
                c.moveToNext();
            }
        }
        if (c != null) {
            c.close();
        }
        return audioIdList;
    }

    public static String getWhereBuilder(Context context, String idName, int isGenre) {
        StringBuilder where = new StringBuilder();
        where.append("");
        String[] folderPath = getFolderPath(context);
        int len = 0;
        if (folderPath != null) {
            len = folderPath.length;
        }
        int isfilterSongs = getIntPref(context, "isFilterSongs", 0);
        if (len == 0) {
            if (isGenre == 0) {
                where.append(" and _id not in (select " + idName + " from audio where" +
                        " _data like '%LEWA/Voice_Recorder%'" +
                        " or _data like '%LEWA/PIM%'");
            } else if (isGenre == 1) {
                where.append(" and audio_id not in (select " + idName + " from audio where" +
                        " _data like '%LEWA/Voice_Recorder%'" +
                        " or _data like '%LEWA/PIM%'");
            }
            if (isfilterSongs == 1) {
                where.append(" or duration<60000 )");
            } else {
                where.append(")");
            }
            return where.toString();
        } else {
            if (isGenre == 0) {
                where.append(" and _id in ( select " + idName + " from audio where ((");
            } else if (isGenre == 1) {
                where.append(" and audio_id in ( select " + idName + " from audio where ((");
            }
        }
        for (int i = 0; i < len; i++) {
            if (folderPath[i].contains("'")) {
                folderPath[i] = folderPath[i].replace("'", "\''");
            }
            where.append(" ( _data like '" + folderPath[i] + "/%' ) AND ");
            where.append(" (_data not like '" + folderPath[i] + "/%/%')");
            if (i < len - 1) {
                where.append(") OR (");
            }
        }

        where.append(" and _id not in (select " + idName + " from audio where" +
                " _data like '%LEWA/Voice_Recorder%'" +
                " or _data like '%LEWA/PIM% ')");
        if (isfilterSongs == 1 && len > 0) {
            where.append(")) and duration>60000)");
            return where.toString();
        }
        if (len > 0)
            where.append(")))");
        return where.toString();
    }

    public static void updateTrackInfo(Context context, String valuetag[], long songid) {
        ContentResolver resolver = context.getContentResolver();
        Uri trackuri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songid);
        try {
            ContentValues values = new ContentValues(3);
            values.put(MediaStore.Audio.Media.TITLE, valuetag[0]);
            values.put(MediaStore.Audio.Media.ARTIST, valuetag[1]);
            values.put(MediaStore.Audio.Media.ALBUM, valuetag[2]);
            resolver.update(trackuri, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getPlaylistCover(int playlistId) {
        InputStream isImg = null;
        Bitmap bitmap = null;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                isImg = new FileInputStream(COVER_PATH + playlistId);
                bitmap = BitmapFactory.decodeStream(isImg, null, options);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (isImg != null)
                try {
                    isImg.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return bitmap;
    }

    public static long[] getSongListForFolder(Context context, String path) {
        final String[] ccols = new String[]{};
        StringBuilder where = new StringBuilder();
        where.append("_data like '" + path + "/%'");
        where.append("AND _data not like '" + path + "/%/%'");
        //maybe two sdcard
        where.append("AND _data not like '%LEWA/Voice_Recorder%'");
        where.append("AND _data not like '%LEWA/PIM%'");
        //remove by shenqi for some mp3 store as  ringtones
        //where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1 AND 1=1");

        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, null);// "Upper(sort_key)");

        if (cursor != null) {
            long[] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }

    public static void checkHasSongs(Activity activity) {
        if (MusicUtils.getAllSongs(activity) == null) {
            mHasSongs = false;
        } else {
            mHasSongs = true;
        }
        //  setupNoSongsView(activity);
    }

    private static void setupNoSongsView(Activity activity) {
        View view = activity.findViewById(ID_MUSIC_NO_SONGS_CONTENT);

        if (mHasSongs == false) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            } else {
                ViewGroup lv = (ViewGroup) activity.findViewById(android.R.id.list);
                view = activity.getLayoutInflater().inflate(R.layout.music_no_songs, lv, false);
                view.setId(ID_MUSIC_NO_SONGS_CONTENT);
//                view.setPadding(0, mNoSongsPaddingTop, 0, 0);
                activity.addContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
            TextView txtView = (TextView) view.findViewById(R.id.text_no_songs);
            String status = Environment.getExternalStorageState();
            if (!(status.equals(Environment.MEDIA_MOUNTED))) {
                txtView.setText(R.string.nosd);
            } else {
                txtView.setText(R.string.no_songs);
            }
        } else {
            if (view != null)
                view.setVisibility(View.GONE);
        }
    }

    public static void resetSdStatus() {
        mLastSdStatus = null;
    }

    /*public static void scan(Context context, String volume) {
        Bundle args = new Bundle();
        args.putString("volume", volume);
        context.startService(
                new Intent(context, MediaScannerService.class).putExtras(args));
    }*/
    public static String getFavouriteArtist(Context context) {
        createLewaDbHelp(context);
        return lewaDBhelp.getFavouriteArtist();

    }

//    private static OnlineRadioDataManager onlineRadioDataManager;
//    private static OnlineManagerEngine engine = null;
//    private static OnlineSingerDataManager onlineSingerDataManager = null;
//    private static OnlineCustomDataManager onlineCustomDataManager = null;
//    private static OnlineAlbumDataManager onlineAlbumDataManager = null;
//    private static OnlineTopListDataManager onlineTopListDataManager;
//    private static OnlineSearchDataManager onlineSearchDataManager;
//    private static LyricManager lyricManager;

//    public static OnlineManagerEngine getEngine(Context context) {
//        if (engine == null)
//            engine = OnlineManagerEngine.getInstance(context);
//        return engine;
//
//    }

//    public static void releaseEngine() {
//        if (engine != null) {
//            engine.releaseEngine();
//            engine = null;
//        }
//    }

    public static void addDownloadSong(String position, String songId, String downId, String tag, Context context) {
        createLewaDbHelp(context);
        SQLiteDatabase ldb = lewaDBhelp.getWritableDatabase();
        Cursor cursor = ldb.query("down_music", new String[]{"songid"}, "songid=?", new String[]{songId}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return;
        }
        ContentValues values = new ContentValues();
        values.put("songid", songId);
        values.put("position", position);
        values.put("tag", tag);
        values.put("downId", downId);
        ldb.insert("down_music", null, values);
        values.clear();
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        ldb.close();
        MusicUtils.clearResource();
    }

    public static List<String> queryDownloadSong(String tag, Context context) {
        createLewaDbHelp(context);
        SQLiteDatabase ldb = lewaDBhelp.getWritableDatabase();
        Cursor cursor = ldb.query("down_music", new String[]{"position", "songid", "downId"}, "tag=?", new String[]{tag}, null, null, null);
        List<String> lists = new ArrayList<String>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String position = cursor.getString(cursor.getColumnIndex("position"));
                String songid = cursor.getString(cursor.getColumnIndex("songid"));
                String downId = cursor.getString(cursor.getColumnIndex("downId"));
                lists.add(position + "-" + songid + "-" + downId);
            }
        }
        cursor.close();
        ldb.close();
        MusicUtils.clearResource();
        return lists;
    }

    public static void delDownloadSong(String songid, Context context) {
        createLewaDbHelp(context);
        SQLiteDatabase ldb = lewaDBhelp.getWritableDatabase();
        ldb.delete("down_music", "songid=?", new String[]{songid});
        ldb.close();
        MusicUtils.clearResource();
    }

    public static void clearDownloadSong(Context context) {
        createLewaDbHelp(context);
        SQLiteDatabase ldb = lewaDBhelp.getWritableDatabase();
        try {
            ldb.delete("down_music", null, null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
        ldb.close();
        MusicUtils.clearResource();
    }

    public static int getDownloadSongNum(Context context) {
        createLewaDbHelp(context);
        int num = 0;
        SQLiteDatabase ldb = lewaDBhelp.getWritableDatabase();
        Cursor cursor = ldb.query("down_music", null, null, null, null, null, null);
        if (cursor != null) {
            num = cursor.getCount();
        }
        cursor.close();
        ldb.close();
        MusicUtils.clearResource();
        return num;
    }


//    public static OnlineRadioDataManager getOnlineRadioDataManager(Context context) {
//        getEngine(context);
//        if (onlineRadioDataManager == null)
//            onlineRadioDataManager = engine.getOnlineRadioDataManager(context);
//        return onlineRadioDataManager;
//    }
//
//    public static void releaseOnlineRadioDataManager() {
//        if (onlineRadioDataManager != null) {
//            onlineRadioDataManager = null;
//        }
//    }
//
//    public static OnlineSingerDataManager getOnlineSingerDataManager(Context context) {
//        getEngine(context);
//        if (onlineSingerDataManager == null)
//            onlineSingerDataManager = engine.getOnlineSingerDataManager(context);
//        return onlineSingerDataManager;
//    }
//
//    public static void releaseOnlineSingerDataManager() {
//        if (onlineSingerDataManager != null) {
//            onlineSingerDataManager.releaseCacheData();
//            onlineSingerDataManager = null;
//        }
//    }
//
//    public static OnlineCustomDataManager getOnlineCustomDataManager(Context context) {
//        getEngine(context);
//        if (onlineCustomDataManager == null)
//            onlineCustomDataManager = engine.getOnlineCustomDataManager(context);
//        return onlineCustomDataManager;
//    }
//
//    public static void releaseOnlineCustomDataManager() {
//        if (onlineCustomDataManager != null) {
//            onlineCustomDataManager.releaseCacheData();
//            onlineCustomDataManager = null;
//        }
//    }
//
//    public static OnlineAlbumDataManager getOnlineAlbumDataManager(Context context) {
//        getEngine(context);
//        ;
//        if (onlineAlbumDataManager == null)
//            onlineAlbumDataManager = engine.getOnlineAlbumDataManager(context);
//        return onlineAlbumDataManager;
//    }

//    public static void releaseOnlineAlbumDataManager() {
//        if (onlineAlbumDataManager != null) {
//            onlineAlbumDataManager.releaseCacheData();
//            onlineAlbumDataManager = null;
//        }
//    }

//    public static OnlineTopListDataManager getOnlineTopListDataManager(Context context) {
//        getEngine(context);
//        if (onlineTopListDataManager == null)
//            onlineTopListDataManager = engine.getOnlineTopListDataManager(context);
//        return onlineTopListDataManager;
//    }
//
//    public static void releaseOnlineTopListDataManager() {
//        if (onlineTopListDataManager != null) {
//            onlineTopListDataManager.releaseCacheData();
//            onlineTopListDataManager = null;
//        }
//    }
//
//    public static OnlineSearchDataManager getOnlineSearchDataManager(Context context) {
//        getEngine(context);
//        if (onlineSearchDataManager == null)
//            onlineSearchDataManager = engine.getOnlineSearchDataManager(context);
//        return onlineSearchDataManager;
//    }
//
//    public static void releaseOnlineSearchDataManager() {
//        if (onlineSearchDataManager != null) {
//            onlineSearchDataManager = null;
//        }
//    }

//    public static LyricManager getLyricManager(Context context) {
//        getEngine(context);
//        if (lyricManager == null)
//            lyricManager = engine.getLyricManager(context);
//        return lyricManager;
//    }

//    public static void releaseLyricManager() {
//        if (lyricManager != null) {
//            lyricManager = null;
//        }
//    }

//    public static void releaseOnlineSource() {
//        releaseOnlineSingerDataManager();
//        releaseOnlineCustomDataManager();
//        releaseOnlineAlbumDataManager();
//        releaseOnlineTopListDataManager();
//        releaseOnlineSearchDataManager();
//        releaseLyricManager();
//    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String buildArtistName(String artistName) {
        if (artistName != null) {
            if (artistName.contains(","))
                artistName = artistName.substring(0, artistName.indexOf(","));
            /*if (artistName.contains("-"))
                artistName = artistName.substring(0, artistName.indexOf("-"));*/
            if (artistName.contains("，"))
                artistName = artistName.substring(0, artistName.indexOf("，"));
            if (artistName.contains("&"))
                artistName = artistName.substring(0, artistName.indexOf("&"));
            if (artistName.contains("+"))
                artistName = artistName.substring(0, artistName.indexOf("+"));

            artistName = artistName.trim().replaceAll(" ", "-");
        }
        return artistName;
    }

    public static void recyleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public static boolean isBitmapDrawableRecyled(BitmapDrawable bitmapDrawable) {
        if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null && !bitmapDrawable.getBitmap().isRecycled()) {
            return false;
        } else {
            return true;
        }
    }

    public static void insertOnlinePlayLists(Context context, ContentValues values) {
        createLewaDbHelp(context);
        getLdb(context);
        if (db != null && values != null) {
            db.insert("playlist_online", null, values);
        }
    }

    public static Cursor getOnlinePlayLists(Context context, long playlist_id) {
        createLewaDbHelp(context);
        getLdb(context);
        String[] cols = new String[]{"song_id", "artist", "title", "duration", "play_order", "_data", "_id"};
        if (db != null) {
            return db.query("playlist_online", cols, "playlist_id=" + playlist_id, null, null, null, null);
        }
        return null;
    }

    public static int getPlaylistCountLocal(Context context, long playlistid) {
        ContentResolver resolver = context.getContentResolver();
        String[] cols = new String[]{
                "count(*)"
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        if (cur != null) {
            cur.moveToFirst();
            int count = cur.getInt(0);
            cur.close();
            return count;
        }
        return 0;
    }

    public static int getPlaylistCountOnline(Context context, long playlistid) {
        createLewaDbHelp(context);
        getLdb(context);
        ContentResolver resolver = context.getContentResolver();
        String[] cols = new String[]{
                "count(*)"
        };
        if (db != null) {
            Cursor cur = db.query("playlist_online", cols, "playlist_id=" + playlistid, null, null, null, null);
            if (cur != null) {
                cur.moveToFirst();
                int count = cur.getInt(0);
                cur.close();
                return count;
            }
        }
        return 0;
    }

    public static Cursor getPlaylistOnlineCursor(Context context, long song_id) {
        createLewaDbHelp(context);
        getLdb(context);
        if (db != null) {
            Cursor cur = db.query("playlist_online", null, "song_id=" + song_id, null, null, null, null);
            return cur;
        }
        return null;
    }

    public static Cursor getPlaylistOnlineCursor(Context context, String title, String artist) {
        createLewaDbHelp(context);
        getLdb(context);
        if (db != null) {
            Cursor cur = db.query("playlist_online", null, "title='" + title + "' and artist ='" + artist + "'", null, null, null, null);
            return cur;
        }
        return null;
    }

    public static void delPlaylistOnlineRaw(Context context, String title, String artist) {
        createLewaDbHelp(context);
        getLdb(context);
        if (db != null) {
            db.delete("playlist_online", "title='" + title + "' and artist ='" + artist + "'", null);
        }
    }

    public static void delPlaylistOnlineRaw(Context context, long song_id, long playlist_id) {
        createLewaDbHelp(context);
        getLdb(context);
        if (db != null) {
            db.delete("playlist_online", "song_id=" + song_id + " and playlist_id=" + playlist_id, null);
        }
    }

    public static void delPlaylistOnline(Context context, long playlist_id) {
        createLewaDbHelp(context);
        getLdb(context);
        if (db != null) {
            db.delete("playlist_online", "playlist_id=" + playlist_id, null);
        }
    }

    public static void insertPlaylistOnline(Context context, long song_id, String artist, String title, long duration, long playlist_id) {
        ContentValues values = new ContentValues();
        values.put("song_id", song_id);
        values.put("artist", artist);
        values.put("title", title);
        values.put("duration", duration);
        values.put("playlist_id", playlist_id);
        int play_order = MusicUtils.getPlaylistCountLocal(context, playlist_id) + MusicUtils.getPlaylistCountOnline(context, playlist_id);
        values.put("play_order", play_order);
        values.put("_data", "online");
        values.put("_id", 0);
        insertOnlinePlayLists(context, values);
    }

    public static synchronized void updatePlaylistOnlineOrder(Context context, long playlist_id, long song_id, int from, int to) {
        createLewaDbHelp(context);
        getLdb(context);
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put("play_order", to);
            db.update("playlist_online", values, "playlist_id=" + playlist_id + " and play_order=" + from + " and song_id=" + song_id, null);
        }
    }

    public static synchronized void updatePlaylistLocalOrder(Context context, long playlist_id, long song_id, int from, int to) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);
        ContentValues values = new ContentValues();
        values.put("play_order", to);
        context.getContentResolver().update(uri, values, "play_order=" + from + " and audio_id=" + song_id, null);
    }

    public static List<String> getAllPlaylistNames(Context context) {
        ArrayList<String> list = new ArrayList<String>();
        String[] cols = new String[]{
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = context.getContentResolver();
        Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                cols, null, null,
                MediaStore.Audio.Playlists.NAME);
        if (cur != null) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                list.add(cur.getString(0));
                cur.moveToNext();
            }
            cur.close();
        }
        return list;
    }

    public static void updateActionModeTitle(ActionMode mode, Context context, int selectedNum) {
        if (mode != null) {
            String format = context.getResources().getString(R.string.title_actionbar_selected_items);
            mode.setTitle(String.format(format, selectedNum));
        }
    }
}
