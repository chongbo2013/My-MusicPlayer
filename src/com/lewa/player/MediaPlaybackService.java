/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.player;

import com.lewa.player.widget.Widget4x1;
import com.lewa.player.widget.Widget4x3;

import lewa.lockscreen.service.LockScreenService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.lewa.Lewa;
import com.lewa.ExitApplication;
import com.lewa.player.activity.MusicEQActivity;
import com.lewa.player.db.DBService;
import com.lewa.player.helper.AsyncMusicPlayer;
import com.lewa.player.model.Song;
import com.lewa.player.model.Song.TYPE;
import com.lewa.player.online.DownLoadAsync;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.media.MediaScannerConnection;
import android.net.Uri;

import com.lewa.view.lyric.Sentence;
import com.lewa.view.lyric.Lyric;
import com.lewa.view.lyric.PlayListItem;
import com.baidu.music.onlinedata.LyricManager.LyricDownloadListener;
import com.lewa.player.online.OnlineLoader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.PatternMatcher;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.telephony.PhoneStateListener;
import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;
import android.os.RemoteException;
import com.lewa.player.widget.WidgetUtils;


//import android.R.integer;
//import android.content.ContentProvider;
//import android.os.RemoteException;
//import android.util.Log;
//import android.widget.ListView;
//import com.lewa.player.ui.NowPlayingController;

/**
 * Provides "background" audio playback capabilities, allowing the user to
 * switch between activities without stopping playback.
 */
public class MediaPlaybackService extends Service {
    private final static String TAG = "MediaPlaybackService";//.class.getName();

    /**
     * used to specify whether enqueue() should start playing the new list of
     * files right away, next or once all the currently queued files have been
     * played
     */
    public static final int NOW = 1;
    public static final int NEXT = 2;
    public static final int LAST = 3;
    public static final int PLAYBACKSERVICE_STATUS = 1;

    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;

    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;

    public static final String PLAYSTATE_CHANGED = "com.lewa.player.playstatechanged";
    public static final String META_CHANGED = "com.lewa.player.metachanged";
    public static final String SONG_DOWNLOADED = "com.lewa.player.songdownloaded";
    public static final String QUEUE_CHANGED = "com.lewa.player.queuechanged";

    public static final String SERVICECMD = "com.lewa.player.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
	public static final String TOGGLE_LYR_ACTION = "com.lewa.player.widget.toggle_lyr"; 	//add by xsj 
    public static final String TOGGLEPAUSE_ACTION = "com.lewa.player.musicservicecommand.togglepause";
    public static final String PAUSE_ACTION = "com.lewa.player.musicservicecommand.pause";
    public static final String PREVIOUS_ACTION = "com.lewa.player.musicservicecommand.previous";
    public static final String NEXT_ACTION = "com.lewa.player.musicservicecommand.next";
    public static final String HEADPLUG = "android.intent.action.HEADSET_PLUG";
    public static final String SHAKE = "com.lewa.player.musicservicecommand.shake";
    public static final String SLEEP = "com.lewa.player.musicservicecommand.sleep";
    public static final String UPDATEID3INFO = "com.lewa.player.updateid3info";
    public static final String PLAYTIMESADD = "com.lewa.player.PLAYTIMESADD";
    public static final String STATUSPAUSE_ACTION = "com.lewa.player.musicservicecommand.statuspause";
    public static final String ADDNEWSONG_ACTION = "com.lewa.player.musicservicecommand.addnewsong";
    public static final String ONLINE_PLAY="com.lewa.player.onlineplay";
    public static final String SCANMUSIC = "com.lewa.player.scanmusic";
    public static String DOWN_FINISHED="com.lewa.player.downFinished";
    public static String UPDATE_STATUS="com.lewa.player.updatestatus";
    public static final String EXTRA_IS_PLAYING = "playing";
    public static boolean getFromOnline=true;
    public static boolean normal=true;
    public static String STATUS;
    private static boolean isLockScreenNeed=false;
    public static boolean isWidgetAdded=false;	//4x4
    public static boolean isWidget4X1Added=false;//4	//4x1
    public static boolean isWidget4X3Added=false;//4	//4x3
    public static boolean isShowLyr = false;
    private String DOWN_PATH=Environment.getExternalStorageDirectory()+"/LEWA/music/mp3";

    public static final int TRACK_ENDED = 1;
    public static final int RELEASE_WAKELOCK = 2;
    private static final int SERVER_DIED = 3;
    private static final int FADEIN = 4;
    private static final int FOCUSCHANGE = 5;
    private static final int FADEOUTOPEN = 6;
    private static final int FADEOUTPAUSE = 7;
    private static final int FADEOUTCLOSE = 8;
    public static final int PLAY_ONLINE_SONG = 9;
    public static final int ERROR_NETWORK = 10;
    public static final int ERROR_HINT = 11;
    private static final int VOLUME_CLEARED_FADEIN = 0;
    private static final int MAX_HISTORY_SIZE = 100;
    private static final int FORGROUND_NOTIFI_ID=23;

    public static String showTitleName=null;
    public static String showArtistName=null;
    public static String widgetartistname=null; //4x1
    public static String widgeTrackName=null; //4x1 use for refreshing artist bitmap
	public static String widgetartist3name=null;//4x3
	public static String widgetrack3name=null;//4x3 use for refreshing lrc 
	public static String onDownloadingArtistName=null;//4  //windget 4x3 need when download lrc
	public static AsyncMusicPlayer mPlayer;
    private String mFileToPlay;
    private int mShuffleMode = SHUFFLE_NONE;
    private int mRepeatMode = REPEAT_NONE;
    private int mMediaMountedCount = 0;
    private long[] mAutoShuffleList = null;
    private long[] mPlayList = null;
    private int mPlayListLen = 0;
    private Vector<Integer> mHistory = new Vector<Integer>(MAX_HISTORY_SIZE);
    private Cursor mCursor;
    private int mPlayPos = -1;
    private int mPrePlayPos = -1;
    private static final String LOGTAG = "MediaPlaybackService";
    private final Shuffler mRand = new Shuffler();
    private int mOpenFailedCounter = 0;
    private int[] appWidgetIds;
    String[] mCursorCols = new String[] {
            "audio._id AS _id", // index must match IDCOLIDX below
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.IS_PODCAST, // index must match PODCASTCOLIDX
            // below
            MediaStore.Audio.Media.BOOKMARK // index must match BOOKMARKCOLIDX
            // below
    };
    private final static int IDCOLIDX = 0;
    private final static int PODCASTCOLIDX = 8;
    private final static int BOOKMARKCOLIDX = 9;
    private BroadcastReceiver mUnmountReceiver = null;
    private WakeLock mWakeLock;
    private int mServiceStartId = -1;
    private boolean mServiceInUse = false;
    public static boolean mIsSupposedToBePlaying = false;
    private boolean mQuietMode = false;
    private AudioManager mAudioManager;
    private boolean mQueueIsSaveable = true;
    // used to track what type of audio focus loss caused the playback to pause
    private boolean mPausedByTransientLossOfFocus = false;


    // add by zhaolei,120828, for pause in statusbar
    private boolean mIsStatusPause = false;
    private boolean isLockScreen=false;

    private SharedPreferences mPreferences;
    // We use this to distinguish between different cards when saving/restoring
    // playlists.
    // This will have to change if we want to support multiple simultaneous
    // cards.
    private int mCardId;

    private MediaAppWidgetProvider mAppWidgetProvider;
	private Widget4x1 mWidget4x1 = null;
	private Widget4x3 mWidget4x3 = null;
    // interval after which we stop the service when idle
    private static final int IDLE_DELAY = 60000;

    ShakeListener mShaker;
    private static final int REFRESH = 1;
    private static final int WAIT_PLAY_NEXT = 2; //when player play online songs and wifi is not activity
    protected static final int SEND_TIMES = 22;

    private Equalizer mEqualizer;
    private NotificationManager notificationManager;

    short[] levels = new short[5];

    private boolean isStatusCanCancel;
    // wangliqiang
    private boolean isNotification=false;
    public static boolean isOnlinePlay=false;
    public static boolean isFromOnlinePlay=false;
    private String trackName_online;
    public static String artistName_online;
    private String albumName_online;
    private Toast mToast;

    private List<String> downListName=new ArrayList<String>();
    private boolean isRingCome;
    //MTK solution:these two flag for this case of Headset: Headsetphone update to Headset(10->01)
    private int mic_with_flag = 0;
    private int mic_no_flag = 0;

    private Handler mMediaplayerHandler = new Handler() {
        float mCurrentVolume = 1.0f;

        @Override
        public void handleMessage(Message msg) {
            // MusicUtils.debugLog("mMediaplayerHandler.handleMessage " +
            // msg.what);
            switch (msg.what) {
                case VOLUME_CLEARED_FADEIN:
                    mCurrentVolume = 0f;
                    if (mCurrentVolume < 1.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEIN, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case FADEIN:
                    mCurrentVolume += 0.01f;
                    if (mCurrentVolume < 1.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEIN, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    if (mIsSupposedToBePlaying) {
                        next(true);
                    } else {
                        // the server died when we were idle, so just
                        // reopen the same song (it will start again
                        // from the beginning though when the user
                        // restarts)
                        openCurrent();
                    }
                    break;
                case TRACK_ENDED:
                    Integer state = (Integer)msg.obj;
                    if (mRepeatMode == REPEAT_CURRENT && AsyncMusicPlayer.FINISH_COMPLETE == state) {
                    // pr968711 modified by wjhu begin
                    // if(!isOnlinePlay) {
                    // seek(0);
                    // } else {
                    // mPlayer.setDataSource(mPlayer.getMusicPath());
                    // }
                    mPlayer.setDataSource(mPlayer.getMusicPath());
                    play();
                    // pr968711 modified by wjhu end
                    } else {
                        next(false);
                    }
                    if(!isOnlinePlay)
                        PlayTimesAdd();
                    Intent intent = new Intent();
                    intent.setAction(PLAYTIMESADD);
                    sendBroadcast(intent);
                    break;
                case ERROR_NETWORK:
                    //Toast.makeText(MediaPlaybackService.this, Lewa.string(R.string.no_network_text), Toast.LENGTH_SHORT).show();
                    pause();
                    break;   
                case PLAY_ONLINE_SONG:
                    play();
                    break;
                case ERROR_HINT:
                    String hint = (String)msg.obj;
                    if(null != hint) { 
                    	/*Added by ruiwei, for Modifying Toast style, 20150211, start*/
                    	ExitApplication exit = (ExitApplication) getApplication();
                        Context activity = exit.getTopActivity();
                        if(null == activity) {
                        	activity = MediaPlaybackService.this;
                        }
                        //pr962339 modify by wjhu
                        //to make the toast show once
                        if (!Lewa.isShowedToast) {
                        	showToast(hint);
                        	Lewa.isShowedToast = true;
                        }
                        /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
                    }
                    break;
                case RELEASE_WAKELOCK:
                    mWakeLock.release();
                    break;

                case FOCUSCHANGE:
                    // This code is here so we can better synchronize it with the code
                    // that
                    // handles fade-in
                    // AudioFocus is a new feature: focus updates are made verbose on
                    // purpose
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
//            Log.v(LOGTAG, "AudioFocus: received AUDIOFOCUS_LOSS");
                            if (isPlaying()) {
                                mPausedByTransientLossOfFocus = false;
                            }
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//            Log.v(LOGTAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
                            if (isPlaying()) {
                                mPausedByTransientLossOfFocus = true;
                            }
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
//            Log.v(LOGTAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                            if (!isPlaying() && mPausedByTransientLossOfFocus) {
                                mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                mPlayer.setVolume(mCurrentVolume);
                                play(); // also queues a fade-in
                            }
                            break;
                        default:
//            Log.e(LOGTAG, "Unknown audio focus change code");
                    }
                    break;

                case FADEOUTOPEN:
                    mCurrentVolume -= 0.02f;
                    if (mCurrentVolume > 0.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEOUTOPEN, 10);
                    } else {
                        mCurrentVolume = 0.0f;
                        nextAfterFadeOut();
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;

                case FADEOUTPAUSE:

                    mCurrentVolume -= 0.02f;
                    if (mCurrentVolume > 0.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEOUTPAUSE, 10);
                    } else {
                        mCurrentVolume = 0.0f;
                        pauseAfterFadeOut();

                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;

                case FADEOUTCLOSE:

                    mCurrentVolume -= 0.02f;
                    if (mCurrentVolume > 0.0f) {
                        if(mPlayer!=null)
                            mPlayer.setVolume(mCurrentVolume);
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEOUTCLOSE, 10);
                        /** exit application **/
                        MusicUtils.clearDownloadSong(Lewa.context());


                    } else {
                        mCurrentVolume = 0.0f;
                        if(mPlayer!=null)
                            mPlayer.setVolume(mCurrentVolume);
                        if(notificationManager!=null)
                            notificationManager.cancel(PLAYBACKSERVICE_STATUS);

                        /** exit application **/
                        MusicUtils.clearDownloadSong(Lewa.context());
                        //pr945871 add by wjhu
                        //to stop playback and reflash the widget
                        stop();

                        Intent exitIntent = new Intent(Intent.ACTION_MAIN);
                        exitIntent.addCategory(Intent.CATEGORY_HOME);
                        exitIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        exitIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(exitIntent);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        stopSelf(mServiceStartId);
                    }

                    break;
                case SEND_TIMES:
                    if(isLockScreenNeed){
                Message lockScreenmsg =
                        Message.obtain(null, LockScreenService.MSG_SET_MUSIC_INFO,
                                (int) (duration() / 1000), (int) (position() / 1000));
                try {
                    ((ExitApplication) getApplication()).mService.send(lockScreenmsg);
                    mMediaplayerHandler.sendEmptyMessageDelayed(SEND_TIMES, 1000);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                   }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent==null)
                return;
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            // MusicUtils.debugLog("mIntentReceiver.onReceive " + action + " / "
            // + cmd);
            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                isStatusCanCancel=false;
                boolean isDeleteItem=intent.getBooleanExtra("isDeleteItem", false);
                if(isDeleteItem){
                    mPlayPos--;
                    if(MusicUtils.getAllSongs(context)!=null){
                        MusicUtils.mHasSongs=true;
                    }else{
                        MusicUtils.mHasSongs=false;
                    }
                }
                if(MusicUtils.mHasSongs){
                    next(true);
                }else{
                    stopStreamPlayer();
                    notifyChange(META_CHANGED);
                }
            } else if (CMDPREVIOUS.equals(cmd)
                    || PREVIOUS_ACTION.equals(action)) {
                if(MusicUtils.mHasSongs)
                    prev();
            } else if (CMDTOGGLEPAUSE.equals(cmd)
                    || TOGGLEPAUSE_ACTION.equals(action)) {
                if (isPlaying()) {
                    isStatusCanCancel=true;
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    if(MusicUtils.mHasSongs)
                        play();
                    isStatusCanCancel=false;
                }
            } else if (STATUSPAUSE_ACTION.equals(action)) {
                isNotification=true;
                if (isPlaying()) {
                    isStatusCanCancel=true;
                    mIsStatusPause = true;
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    isStatusCanCancel=false;
                    play();
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                isStatusCanCancel=true;
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMDSTOP.equals(cmd)) {
                isStatusCanCancel=true;
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
            } else if (MusicUtils.CMDAPPWIDGETUPDATE.equals(cmd)) {
                appWidgetIds = intent
                        .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if(isWidgetAdded){
                    if(mAppWidgetProvider==null)
                        mAppWidgetProvider=MediaAppWidgetProvider.getInstance();
                    mAppWidgetProvider.performUpdate(MediaPlaybackService.this,
                            appWidgetIds);
                }

                updateWidget4X1UI(appWidgetIds);
                updateWidget4X3UI(appWidgetIds);
            } else if (SHAKE.equals(action)) {
                doShake();
            } else if (SLEEP.equals(action)) {
                SleepModeManager.setSleepTime(context, 0);
                if(mMediaplayerHandler!=null)
                    mMediaplayerHandler.sendEmptyMessage(FADEOUTCLOSE);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                if (mShaker != null) {
                    mShaker.pause();
                    mShaker = null;
                }
                doShake();
            } else if (HEADPLUG.equals(action)) {
                int state=intent.getIntExtra("state", -1);
                int mic = intent.getIntExtra("microphone", -1);
                if(state==-1||mic==-1)
                    return;
                if(state==1){
                    if(mic==1){
                        mic_with_flag=1;
                    }else{
                        mic_no_flag=1;
                    }
                }else if (state == 0) {
                    //This path for Headsetphone update to Headset(10->01)
                    if((0==mic) && (1==mic_with_flag) && (1==mic_no_flag)) {
                        mic_with_flag = 0;
                        mic_no_flag = 0;
                    }else{
                        pause();
                        /*if (notificationManager != null) {    //del this code for bug 57603
                            notificationManager.cancel(PLAYBACKSERVICE_STATUS); 
                        }*/
                        mic_with_flag = 0;
                        mic_no_flag = 0;
                    }
                }
            } else if (MediaPlaybackService.UPDATEID3INFO.equals(action)) {

                mCursor = getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mCursorCols, "_id=" + String.valueOf(mPlayList[mPlayPos]), null, null);
                if(mCursor!=null)
                    mCursor.moveToFirst();
                if(isPlaying()) {
                    updateNotification();
                }
                notifyChange(META_CHANGED);

                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
                // add by wangliqiang
                else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
                        || Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                    if (notificationManager != null) {
                        notificationManager.cancel(PLAYBACKSERVICE_STATUS);
                    }
                }
                mCursor = getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mCursorCols,
                        "_id=" + String.valueOf(mPlayList[mPlayPos]), null,
                        null);
                mCursor.moveToFirst();
                if (isPlaying()) {
                    updateNotification();
                }
                notifyChange(META_CHANGED);
            }else if(Intent.ACTION_MEDIA_MOUNTED.equals(action)){
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if(MusicUtils.getAllSongsInDB(getApplicationContext()).length>0){
                            MusicUtils.mHasSongs=true;
                        }else{
                            MusicUtils.mHasSongs=false;
                        }
                    }
                }, 2000);

                MusicUtils.isSdMounted=true;
                if(isWidgetAdded){
                    if(mAppWidgetProvider==null)
                        mAppWidgetProvider=MediaAppWidgetProvider.getInstance();
                    mAppWidgetProvider.performUpdate(MediaPlaybackService.this,
                            appWidgetIds);
                }

		         updateWidget4X1UI(appWidgetIds);
		         updateWidget4X3UI(appWidgetIds);
//                appWidgetIds = intent
//                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            }else if(action.equals(
                    Intent.ACTION_MEDIA_UNMOUNTED)
                    || action.equals(
                    Intent.ACTION_MEDIA_SHARED)
                    || action.equals(
                    Intent.ACTION_MEDIA_REMOVED)){
                MusicUtils.isSdMounted=false;
                MusicUtils.mHasSongs=false;
            }else if(action.equals("lockscreen.action.SONG_METADATA_REQUEST")){
                notifyPlayChanged();
            }else if(action.equals("lockscreen.action.SEND_MUSICINFO_STATUS")){
                Intent screenIntent=new Intent("com.lewa.player.playStatus");
                screenIntent.putExtra(EXTRA_IS_PLAYING, mIsSupposedToBePlaying);
                sendBroadcast(screenIntent);
            } else if(action.equals("com.lewa.player.requestlyric")) {

            	LrcList mLrcList = getLrc();
				if(null != mLrcList) {
					Intent screenIntent = new Intent("com.lewa.player.responselyric");					
					screenIntent.putCharSequenceArrayListExtra("lyric", mLrcList.lrc);					
					screenIntent.putExtra("lyric_time", mLrcList.mTimeArray);
					long pos = position();
					//Log.i(TAG, "position = " + pos);
					screenIntent.putExtra(EXTRA_IS_PLAYING, isPlaying());
					screenIntent.putExtra("duration", duration());
					screenIntent.putExtra("position", pos);
					screenIntent.putExtra("time_stamp", System.currentTimeMillis());
					sendBroadcast(screenIntent);
				}
            } else if(action.equals(OnlineLoader.UPDATELRC)) {
            	//Log.i(TAG, "------------OnlineLoader.UPDATELRC---------------- ");
            	int stat = intent.getIntExtra("downStat", -1);
            	if (stat == LyricDownloadListener.STATUS_SUCCESS) {
            		LrcList mLrcList = getLrc();
    				if(null != mLrcList) {
    					Intent screenIntent = new Intent("com.lewa.player.responselyric");					
    					screenIntent.putCharSequenceArrayListExtra("lyric", mLrcList.lrc);					
    					screenIntent.putExtra("lyric_time", mLrcList.mTimeArray);
    					long pos = position();
    					//Log.i(TAG, "position = " + pos);
    					screenIntent.putExtra(EXTRA_IS_PLAYING, isPlaying());
    					screenIntent.putExtra("duration", duration());
    					screenIntent.putExtra("position", pos);
    					screenIntent.putExtra("time_stamp", System.currentTimeMillis());
    					sendBroadcast(screenIntent);
    				}
            	}
            } else if(action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)){
                reloadQueue();
                notifyChange(META_CHANGED);
                Log.i(TAG, "Scan finished, check and match download songs.");
                DBService.matchDownload();
            }
            else if(action.equals(Intent.ACTION_REBOOT)){
                saveQueue(true);
            }else if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
                SystemClock.sleep(5000);
                reloadQueue();
                notifyChange(META_CHANGED);
            }else if(ONLINE_PLAY.equals(action)){
                isFromOnlinePlay=true;
                normal=false;
                trackName_online=intent.getStringExtra("songName");
                artistName_online=intent.getStringExtra("songAuthor");
                albumName_online=intent.getStringExtra("songAlbum");
                boolean isExists=intent.getBooleanExtra("isExists", false);
                if(!isExists&&isOnlinePlay){
                    mPlayer.setDataSource(intent.getStringExtra("songId"));
                }else{
                    isOnlinePlay=false;
                    trackName_online=intent.getStringExtra("songName");
                    albumName_online=intent.getStringExtra("songAlbum");
                    long songid=MusicUtils.getSongId(getApplicationContext(), trackName_online);
                    enqueue(new long[]{songid},NOW);
                    notifyChange(QUEUE_CHANGED);
                    normal=true;
                }
                notifyChange(META_CHANGED);
                mIsSupposedToBePlaying=true;
            }else if(SCANMUSIC.equals(action)){
                Intent scanFileIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                String filepath=intent.getStringExtra("fullpath");
                long id = intent.getLongExtra("music_id", 0);
                if(!downListName.contains(intent.getStringExtra("trackTitle")))
                   downListName.add(intent.getStringExtra("trackTitle"));
                if(filepath!=null){
	                File file=new File(filepath);
	                if(file.exists()) {
	                    requestScanFile(context, filepath, id);
	                }
	             }
                normal=false;
            } else if(UPDATE_STATUS.equals(action)) {
                boolean isSuccess = intent.getBooleanExtra("downFinish", false);
                boolean isFailure = intent.getBooleanExtra("failure", false);

                if (isSuccess || isFailure) {
                    Long onlineId = intent.getLongExtra("songId",0);
                    Log.i(TAG, "Download result> success = " + isSuccess + ", failure = " + isFailure + ", song id = " + onlineId);

                    if (isSuccess) {
                        String path = intent.getStringExtra("data");

                        if (onlineId != null) {
                            try {
                                Song onlineSong = DBService.findSongById(onlineId, Song.TYPE.ONLINE);
                                onlineSong.setDownloadStatus(Song.DOWNLOADED_NOT_MATCHED);
                                onlineSong.setPath(path);
                                DBService.saveSong(onlineSong);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else if("com.lewa.player.updateDownloadImage".equals(action)){
                if(isPlaying())
                    updateNotification();
            }else if("lockscreen.action.SEND_MUSICINFO_REQUEST".equals(action)){
                isLockScreenNeed=true;
                mMediaplayerHandler.sendEmptyMessage(SEND_TIMES);
            }else if("lockscreen.action.SEND_MUSICINFO_REJECT".equals(action)){
                isLockScreenNeed=false;
                mMediaplayerHandler.removeMessages(SEND_TIMES);
            }else if("android.intent.action.killProcess".equals(action)){
                if(notificationManager!=null)
                    notificationManager.cancel(PLAYBACKSERVICE_STATUS);
            } else if("com.android.systemui.RECREATE_STATUSBAR_COMPLATE".equals(action)) {
                updateNotification();
            }


        }
    };

    public static class LrcList {
		public ArrayList<CharSequence> lrc = null;
		public int[] mTimeArray = null;

		public LrcList() {

		}
		
		public LrcList(int size) {
			lrc = new ArrayList<CharSequence>();
			mTimeArray = new int[size];
		}
	}
	
	public LrcList getLrc() {
		LrcList mLrcList = null;
		PlayListItem currentLrc = new PlayListItem(getTrackName(), null, 0L, true);
		String sdCardDir = Environment.getExternalStorageDirectory()
                        + Constants.SRC_PATH;
		File lrcFile = new File(sdCardDir + getTrackName() + "-" + getArtistName()
                        + ".lrc"); 
		
		
		if((null == lrcFile || !lrcFile.exists())) {
			OnlineLoader.getSongLrc(getTrackName(), getArtistName()); //download LRC from network			
		} else if(null != lrcFile && lrcFile.exists()){
			Lyric mLyric = new Lyric(lrcFile, currentLrc, duration());
			List<Sentence> sentences = mLyric.list;
			int lrcSize = sentences.size();
			if(null != sentences && 0 != lrcSize) {
				int[] mTimeArray = new int[lrcSize];
				ArrayList<CharSequence> lrcs = new ArrayList<CharSequence>();
				int i = 0;
				for(; i < lrcSize; i++) {
					Sentence sentence = sentences.get(i);
					mTimeArray[i] = (int)sentence.getFromTime();
					lrcs.add(sentence.getContent());
				}

				if(mTimeArray.length == lrcs.size()) {
					mLrcList = new LrcList();
					mLrcList.lrc = lrcs;
					mLrcList.mTimeArray = mTimeArray;
				}
			}
		} 
		
		

		return mLrcList;
	}

    //sjxu add
    BroadcastReceiver themeStateReceiver = new BroadcastReceiver() {

        private boolean isAlarmPause = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "action = " + action);
            updateNotification();
        }
    };

    //jczou
    BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {

        private boolean isAlarmPause = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println(action);
            if(isPlaying() && action.equals("com.android.deskclock.START_ALARM")){
                pause();
                isAlarmPause = true;
            }
            else if(isAlarmPause && (action.equals("com.android.deskclock.ALARM_DONE") || action.equals("alarm_killed"))){
                play();
                isAlarmPause = false;
            }
        }
    };

    private void doShake() {
        Context context = getApplicationContext();
        int isShake = MusicUtils.getIntPref(context, "shake", 0);
        if (isShake == 1) {
            if (mShaker == null) {
                mShaker = new ShakeListener(this);
            }
        } else {
            if (mShaker != null) {
                mShaker.pause();
                mShaker = null;
            }
        }
    }

    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0)
                    .sendToTarget();
        }
    };

    public MediaPlaybackService() {

    }

    protected void PlayTimesAdd() {
        // TODO Auto-generated method stub
        MusicUtils.songPlayTimesPlus(this);

    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(
                getPackageName(), MediaButtonIntentReceiver.class.getName()));

        mPreferences = getSharedPreferences("Music", MODE_WORLD_READABLE
                | MODE_WORLD_WRITEABLE);
        isWidgetAdded=mPreferences.getBoolean("isWidgetAdded", false);
		isWidget4X1Added = mPreferences.getBoolean("is4X1WidgetAdded", false);
		isWidget4X3Added = mPreferences.getBoolean("is4X3WidgetAdded", false);

        // modify by zhaolei, 120713, for 4.0
        mCardId = MusicUtils.getCardId(this); // from musicUtils
        // mCardId =
        // FileUtils.getFatVolumeId(Environment.getExternalStorageDirectory().getPath());

        registerExternalStorageListener();

        // Needs to be done in this thread, since otherwise
        // ApplicationContext.getPowerManager() crashes.
        // AsyncMusicPlayer localmediaplayer = new AsyncMusicPlayer(this);
        mPlayer = new AsyncMusicPlayer(this);
        mPlayer.setHandler(mMediaplayerHandler);

        reloadQueue();
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                long[] list=MusicUtils.getAllSongs(getApplicationContext());
                if(list==null){
                    MusicUtils.mHasSongs=false;
                }else{
                    MusicUtils.mHasSongs=true;
                }
            }

        });

        IntentFilter audioNoisyFilter = new IntentFilter();
        audioNoisyFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(new MediaButtonIntentReceiver(), audioNoisyFilter);
        
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(SERVICECMD);
        commandFilter.addAction(TOGGLEPAUSE_ACTION);
        commandFilter.addAction(PAUSE_ACTION);
        commandFilter.addAction(NEXT_ACTION);
        commandFilter.addAction(PREVIOUS_ACTION);
        commandFilter.addAction(SHAKE);
        commandFilter.addAction(SLEEP);
        commandFilter.addAction(HEADPLUG);
        commandFilter.addAction(Intent.ACTION_SCREEN_OFF);
        commandFilter.addAction(UPDATEID3INFO);
        commandFilter.addAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        commandFilter.addAction(STATUSPAUSE_ACTION);
        commandFilter.addAction("com.lewa.player.updateDownloadImage");
        commandFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        commandFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        commandFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        commandFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        commandFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        commandFilter.addAction(Intent.ACTION_REBOOT);
        commandFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        commandFilter.addAction(ONLINE_PLAY);
        commandFilter.addAction(SCANMUSIC);
        commandFilter.addAction(UPDATE_STATUS);
        commandFilter.addAction("lockscreen.action.SEND_MUSICINFO_REQUEST");
        commandFilter.addAction("lockscreen.action.SEND_MUSICINFO_REJECT");
        commandFilter.addAction("lockscreen.action.SONG_METADATA_REQUEST");
        commandFilter.addAction("lockscreen.action.SEND_MUSICINFO_STATUS");
//add by sjxu start for lockscreen
		commandFilter.addAction("com.lewa.player.requestlyric");
		commandFilter.addAction(OnlineLoader.UPDATELRC);
//add by sjxu end for lockscreen
        commandFilter.addAction("android.intent.action.killProcess");

        // add by fan.yang #65355
        commandFilter.addAction("com.android.systemui.RECREATE_STATUSBAR_COMPLATE");
        registerReceiver(mIntentReceiver, commandFilter);

        IntentFilter alarmFilter = new IntentFilter();
        alarmFilter.addAction("alarm_killed");
        alarmFilter.addAction("com.android.deskclock.ALARM_DONE");
        alarmFilter.addAction("com.android.deskclock.START_ALARM");
        registerReceiver(mAlarmReceiver, alarmFilter);

//add by sjxu start for theme change is order notification change
        IntentFilter themeFilter = new IntentFilter();
        themeFilter.addAction("com.lewa.intent.action.THEME_CHANGED");
        themeFilter.addDataScheme("content");
        themeFilter.addDataAuthority("com.lewa.themechooser.themes", null);
        themeFilter.addDataPath("/theme", PatternMatcher.PATTERN_PREFIX);
        registerReceiver(themeStateReceiver, themeFilter);
//add by sjxu end for theme change is order notification change

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
                .getClass().getName());
        mWakeLock.setReferenceCounted(false);

        // If the service was idle, but got killed before it stopped itself, the
        // system will relaunch it. Make sure it gets stopped again in that
        // case.
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);

//        setRepeatMode(MediaPlaybackService.REPEAT_ALL);
        int repeatMode = mPreferences.getInt("repeatmode", MediaPlaybackService.REPEAT_ALL);
        int shuffleMode = mPreferences.getInt("shufflemode", MediaPlaybackService.SHUFFLE_NONE);
        setRepeatMode(repeatMode);
        setShuffleMode(shuffleMode);
        Lewa.setRepeatAndShuffleMode(repeatMode, shuffleMode);
        doShake();

        try {
            mEqualizer = new Equalizer(0, getAudioSessionId());
            mEqualizer.setEnabled(true);
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
        registerReceiver(preferenceUpdateReceiver, new IntentFilter(
                MusicEQActivity.ACTION_UPDATE_EQ));

        SharedPreferences music_settings = this.getSharedPreferences(
                "Music_setting", 0);
        short l1 = (short) music_settings.getInt("LowerEQ", 0);
        short l2 = (short) music_settings.getInt("LowEQ", 0);
        short l3 = (short) music_settings.getInt("MiddleEQ", 0);
        short l4 = (short) music_settings.getInt("HighEQ", 0);
        short l5 = (short) music_settings.getInt("HigherEQ", 0);

        levels[0] = l1;
        levels[1] = l2;
        levels[2] = l3;
        levels[3] = l4;
        levels[4] = l5;

        updateDsp();
//        notifyChange(META_CHANGED);
        TelephonyManager tManager=(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        tManager.listen(new PhoneStateListener(){	//this is for listen phone state 

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                // TODO Auto-generated method stub
              switch (state) {
                  case TelephonyManager.CALL_STATE_RINGING:
                      if(isPlaying()){
                          isStatusCanCancel=true;
                          mIsStatusPause = true;
                          isNotification=true;
                          pause();
                          mPausedByTransientLossOfFocus = false;
                          isRingCome=true;
                      }
                      break;
                  case TelephonyManager.CALL_STATE_IDLE:
                      if(isRingCome){
                          isStatusCanCancel=false;
                          isNotification=true;
                          play();
                          isRingCome=false;
                      }
                      break;
                default:
                    break;
            }
            }

        }, PhoneStateListener.LISTEN_CALL_STATE);
        statusIconWidth = MusicUtils.dip2px(this, 64);
		// pr940244 add by wjhu
		isWidgetAdded = true;
		isWidget4X1Added = true;
		isWidget4X3Added = true;
        //pr943935 modify by wjhu
        //not to notify user the music service is running
        //startForeground(FORGROUND_NOTIFI_ID, new Notification());
    }

    @Override
    public void onDestroy() {
    	isWidget4X1Added = false;
		isWidget4X3Added = false;
        // Check that we're not being destroyed while something is still
        // playing.
        if (isPlaying()) {
//            Log.e(LOGTAG, "Service being destroyed while still playing.");
        }
        // release all MediaPlayer resources, including the native player and
        // wakelocks

        saveQueue(true);
        Intent i = new Intent(
                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(i);
        if(mPlayer!=null){
            mPlayer.release();
        }
        if(mAudioManager!=null)
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
        // make sure there aren't any other messages coming
        if(mDelayedStopHandler!=null)
            mDelayedStopHandler.removeCallbacksAndMessages(null);
        if(mMediaplayerHandler!=null)
            mMediaplayerHandler.removeCallbacksAndMessages(null);

        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if(mIntentReceiver!=null)
            unregisterReceiver(mIntentReceiver);
        if(preferenceUpdateReceiver!=null)
            unregisterReceiver(preferenceUpdateReceiver);
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }
        if(mAlarmReceiver!=null)
        {   unregisterReceiver(mAlarmReceiver);
            mAlarmReceiver=null;
        }
        if(mWakeLock!=null)
            mWakeLock.release();
        if (mShaker != null) {
            mShaker.pause();
            mShaker = null;
        }
        super.onDestroy();
    }

    private final char hexdigits[] = new char[] { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private void saveQueue(boolean full) {
        if (!mQueueIsSaveable) {
            return;
        }

        Editor ed = mPreferences.edit();
        // long start = System.currentTimeMillis();
        if (full) {
            StringBuilder q = new StringBuilder();

            // The current playlist is saved as a list of "reverse hexadecimal"
            // numbers, which we can generate faster than normal decimal or
            // hexadecimal numbers, which in turn allows us to save the playlist
            // more often without worrying too much about performance.
            // (saving the full state takes about 40 ms under no-load conditions
            // on the phone)
            int len = mPlayListLen;
            for (int i = 0; i < len; i++) {
                long n = mPlayList[i];
                if (n < 0) {
                    continue;
                } else if (n == 0) {
                    q.append("0;");
                } else {
                    while (n != 0) {
                        int digit = (int) (n & 0xf);
                        n >>>= 4;
                        q.append(hexdigits[digit]);
                    }
                    q.append(";");
                }
            }
            // Log.i("@@@@ service", "created queue string in " +
            // (System.currentTimeMillis() - start) + " ms");
            ed.putString("queue", q.toString());
            ed.putInt("cardid", mCardId);
            if (mShuffleMode != SHUFFLE_NONE) {
                // In shuffle mode we need to save the history too
                len = mHistory.size();
                q.setLength(0);
                for (int i = 0; i < len; i++) {
                    int n = mHistory.get(i);
                    if (n == 0) {
                        q.append("0;");
                    } else {
                        while (n != 0) {
                            int digit = (n & 0xf);
                            n >>>= 4;
                            q.append(hexdigits[digit]);
                        }
                        q.append(";");
                    }
                }
                ed.putString("history", q.toString());
            }
        }
        ed.putInt("curpos", mPlayPos);
        if (mPlayer.isInitialized()) {
            ed.putLong("seekpos", mPlayer.position());
        }
        if(mShuffleMode==SHUFFLE_NONE&&mRepeatMode==REPEAT_NONE)
            return;
        ed.putInt("repeatmode", mRepeatMode).commit();
        ed.putInt("shufflemode", mShuffleMode).commit();
        // SharedPreferencesCompat.apply(ed);2

        // Log.i("@@@@ service", "saved state in " + (System.currentTimeMillis()
        // - start) + " ms");
    }

    private void reloadQueue() {
        String q = null;
        
        boolean newstyle = false;
        int id = mCardId;
        if (mPreferences.contains("cardid")) {
            newstyle = true;
            id = mPreferences.getInt("cardid", ~mCardId);
        }
        if (id == mCardId) {
            // Only restore the saved playlist if the card is still
            // the same one as when the playlist was saved
            q = mPreferences.getString("queue", "");
        }
        int qlen = q != null ? q.length() : 0;
        if (qlen > 1) {
            // Log.i("@@@@ service", "loaded queue: " + q);
            int plen = 0;
            int n = 0;
            int shift = 0;
            for (int i = 0; i < qlen; i++) {
                char c = q.charAt(i);
                if (c == ';') {
                    ensurePlayListCapacity(plen + 1);
                    mPlayList[plen] = n;
                    plen++;
                    n = 0;
                    shift = 0;
                } else {
                    if (c >= '0' && c <= '9') {
                        n += ((c - '0') << shift);
                    } else if (c >= 'a' && c <= 'f') {
                        n += ((10 + c - 'a') << shift);
                    } else {
                        // bogus playlist data
                        plen = 0;
                        break;
                    }
                    shift += 4;
                }
            }
            mPlayListLen = plen;

            int pos = mPreferences.getInt("curpos", 0);
            if (pos < 0 || pos >= mPlayListLen) {
                // The saved playlist is bogus, discard it
                mPlayListLen = 0;
                return;
            }
            mPlayPos = pos;

            // When reloadQueue is called in response to a card-insertion,
            // we might not be able to query the media provider right away.
            // To deal with this, try querying for the current file, and if
            // that fails, wait a while and try again. If that too fails,
            // assume there is a problem and don't restore the state.
            Cursor crsr = null;
            Cursor crsrd = null;
            try {
                crsr = MusicUtils.query(this,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[] { "_id" }, "_id=" + mPlayList[mPlayPos],
                        null, null);
                if (crsr == null || crsr.getCount() == 0) {
                    // wait a bit and try again
                    SystemClock.sleep(3000);
                    crsrd = getContentResolver().query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            mCursorCols, "_id=" + mPlayList[mPlayPos], null,
                            null);
                }

            } finally {
                if (crsr != null) {
                    crsr.close();
                }
                if (crsrd != null) {
                    crsrd.close();
                }
            }

            // Make sure we don't auto-skip to the next song, since that
            // also starts playback. What could happen in that case is:
            // - music is paused
            // - go to UMS and delete some files, including the currently
            // playing one
            // - come back from UMS
            // (time passes)
            // - music app is killed for some reason (out of memory)
            // - music service is restarted, service restores state, doesn't
            // find
            // the "current" file, goes to the next and: playback starts on its
            // own, potentially at some random inconvenient time.
            mOpenFailedCounter = 20;
            mQuietMode = true;
            openCurrent();
            mQuietMode = false;
            if (!mPlayer.isInitialized()) {
                // couldn't restore the saved state
                mPlayListLen = 0;
                return;
            }

            long seekpos = mPreferences.getLong("seekpos", 0);
            seek(seekpos >= 0 && seekpos < duration() ? seekpos : 0);

            int repmode = mPreferences.getInt("repeatmode", REPEAT_ALL);
            if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
                repmode = REPEAT_NONE;
            }
            mRepeatMode = repmode;

            int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
            if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
                shufmode = SHUFFLE_NONE;
            }
            if (shufmode != SHUFFLE_NONE) {
                // in shuffle mode we need to restore the history too
                q = mPreferences.getString("history", "");
                qlen = q != null ? q.length() : 0;
                if (qlen > 1) {
                    plen = 0;
                    n = 0;
                    shift = 0;
                    mHistory.clear();
                    for (int i = 0; i < qlen; i++) {
                        char c = q.charAt(i);
                        if (c == ';') {
                            if (n >= mPlayListLen) {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }
                            mHistory.add(n);
                            n = 0;
                            shift = 0;
                        } else {
                            if (c >= '0' && c <= '9') {
                                n += ((c - '0') << shift);
                            } else if (c >= 'a' && c <= 'f') {
                                n += ((10 + c - 'a') << shift);
                            } else {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }
                            shift += 4;
                        }
                    }
                }
            }
            if (shufmode == SHUFFLE_AUTO) {
                if (!makeAutoShuffleList()) {
                    shufmode = SHUFFLE_NONE;
                }
            }
            mShuffleMode = shufmode;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        if (intent != null) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            // MusicUtils.debugLog("onStartCommand " + action + " / " + cmd);

            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                isNotification=true;
                if(MusicUtils.mHasSongs){
                    isStatusCanCancel=false;
                    next(true);
//                    updateNotification();
                } else {
					Song playingSong = Lewa.getPlayingSong();
                    if(playingSong != null && playingSong.getType() == TYPE.ONLINE || MusicUtils.mHasSongs){	//online play songs
                        isStatusCanCancel = false;
                        next(true);
                    }
				}

            } else if (CMDPREVIOUS.equals(cmd)
                    || PREVIOUS_ACTION.equals(action)) {
                // if (position() < 2000) {
                // prev();
                // } else {
                // seek(0);
                // play();
                // }
                if(MusicUtils.mHasSongs) {
                    prev();
                } else {
					Song playingSong = Lewa.getPlayingSong();
                    if(playingSong != null && playingSong.getType() == TYPE.ONLINE || MusicUtils.mHasSongs){        //online play songs                
                        prev();
                    }
				}

				
            } else if (CMDTOGGLEPAUSE.equals(cmd)
                    || TOGGLEPAUSE_ACTION.equals(action)) {
                isNotification=true;
                if (isPlaying()) {
                    pause();
                    isStatusCanCancel = true;
                    mPausedByTransientLossOfFocus = false;
                } else {
                    if(MusicUtils.getAllSongs(getApplicationContext())==null){
                        MusicUtils.mHasSongs=false;
                    }else{
                        MusicUtils.mHasSongs=true;
                    }
                    Song playingSong = Lewa.getPlayingSong();
                    if(playingSong!=null && playingSong.getType() == TYPE.ONLINE || MusicUtils.mHasSongs){
                        isStatusCanCancel = false;
                        play();
                    }

                    // add by wangliqiang
                    if (Environment.getExternalStorageState().equals(
                            Environment.MEDIA_UNMOUNTED)
                            || Environment.getExternalStorageState().equals(
                            Environment.MEDIA_SHARED)
                            || Environment.getExternalStorageState().equals(
                            Environment.MEDIA_REMOVED)) {
                        MusicUtils.isSdMounted = false;
                    }
                    if(isWidgetAdded){
                        if(mAppWidgetProvider==null)
                            mAppWidgetProvider=MediaAppWidgetProvider.getInstance();
                        mAppWidgetProvider.performUpdate(
                                MediaPlaybackService.this, appWidgetIds);
                    }

		            updateWidget4X1UI(appWidgetIds);        
                    updateWidget4X3UI(appWidgetIds);
                }
            } else if (STATUSPAUSE_ACTION.equals(action)) {

                if (isPlaying()) {
                    mIsStatusPause = true;
                    isStatusCanCancel = true;
                    isNotification=true;
                    pause();
                    mPausedByTransientLossOfFocus = false;
//                    updateNotification();
                } else {
                    isStatusCanCancel = false;
                    isNotification=true;
                    play();
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                pause();
                isStatusCanCancel=true;
                mPausedByTransientLossOfFocus = false;
                //updateNotification();	//del by sjxu for bug 61909
            } else if (CMDSTOP.equals(cmd)) {
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
                updateNotification();

            }

			if(TOGGLE_LYR_ACTION.equals(action)) {
				//Log.i(TAG, "--------------------------------------------");
				isShowLyr = !isShowLyr;
				updateWidget4X3UI(appWidgetIds);
				Widget4x1.isUpdate = true;
			}
        }
        // make sure the service will shut down on its own if it was
        // just started but not bound to and nothing is playing
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);

        return START_NOT_STICKY; // START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mServiceInUse = false;
        // Take a snapshot of the current playlist
        saveQueue(true);

        if (isPlaying() || mPausedByTransientLossOfFocus) {
            // something is currently playing, or will be playing once
            // an in-progress action requesting audio focus ends, so don't stop
            // the service now.
            return true;
        }

        // If there is a playlist but playback is paused, then wait a while
        // before stopping the service, so that pause/resume isn't slow.
        // Also delay stopping the service if we're transitioning between
        // tracks.
        if (mPlayListLen > 0 || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
            Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
            return true;
        }

        // No active playlist, OK to stop the service right now
        stopSelf(mServiceStartId);
        return true;
    }

    private Handler mDelayedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Check again to make sure nothing is playing right now
            if (isPlaying() || mPausedByTransientLossOfFocus || mServiceInUse
                    || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
                return;
            }
            // save the queue again, because it might have changed
            // since the user exited the music app (because of
            // party-shuffle or because the play-position changed)
            saveQueue(true);
            stopSelf(mServiceStartId);
        }
    };

    /**
     * Called when we receive a ACTION_MEDIA_EJECT notification.
     *
     * @param storagePath
     *            path to mount point for the removed media
     */
    public void closeExternalStorageFiles(String storagePath) {
        // stop playback and clean up if the SD card is going to be unmounted.
        stop(true);
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
    }

    /**
     * Registers an intent to listen for ACTION_MEDIA_EJECT notifications. The
     * intent will call closeExternalStorageFiles() if the external media is
     * going to be ejected, so applications can clean up any files they have
     * open.
     */
    public void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)
                            || action.equals(Intent.ACTION_MEDIA_SHARED)) {
                        saveQueue(true);
                        mQueueIsSaveable = false;
                        closeExternalStorageFiles(intent.getData().getPath());
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        mMediaMountedCount++;
                        mCardId = MusicUtils
                                .getCardId(MediaPlaybackService.this);
                        // modify by zhaolei, 120713, for 4.0
                        // mCardId =
                        // FileUtils.getFatVolumeId(Environment.getExternalStorageDirectory().getPath());

                        reloadQueue();
                        mQueueIsSaveable = true;
                        notifyChange(QUEUE_CHANGED);
                        notifyChange(META_CHANGED);
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addAction(Intent.ACTION_MEDIA_SHARED);
            iFilter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, iFilter);
        }
    }

    //pr962339 add by wjhu begin
    private void showToast(String text) {
		if (mToast == null) {
			mToast = Toast.makeText(MediaPlaybackService.this, text,
					Toast.LENGTH_SHORT);
		} else {
			mToast.setText(text);
		}
		mToast.show();
	}
    //pr962339 add by wjhu end
    
    /**
     * Notify the change-receivers that something has changed. The intent that
     * is sent contains the following data for the currently playing track: "id"
     * - Integer: the database row ID "artist" - String: the name of the artist
     * "album" - String: the name of the album "track" - String: the name of the
     * track The intent has an action that is one of
     * "com.android.music.metachanged" "com.android.music.queuechanged",
     * "com.android.music.playbackcomplete" "com.android.music.playstatechanged"
     * respectively indicating that a new track has started playing, that the
     * playback queue has changed, that playback has stopped because the last
     * file in the list has been played, or that the play-state changed
     * (paused/resumed).
     */
    private void notifyChange(String what) {

        Intent i = new Intent(what);
        i.putExtra("id", Long.valueOf(getAudioId()));
        i.putExtra("artist", getArtistName());
        i.putExtra("album", getAlbumName());
        i.putExtra("track", getTrackName());
        i.putExtra(EXTRA_IS_PLAYING, isPlaying());
        i.putExtra("other", "meta_changed_buffer");
        i.putExtra("duration", duration());
        i.putExtra("position", position());
        i.putExtra("session", getAudioSessionId());
        i.putExtra("time_stamp", System.currentTimeMillis());
        sendBroadcast(i);

        if (what.equals(QUEUE_CHANGED)) {
            saveQueue(true);
        } else {
            saveQueue(false);
        }
        if(what.equals(PLAYSTATE_CHANGED)){
            sendBroadcast(new Intent("com.lewa.player.refreshspectrum"));
        }

        // Share this notification directly with our widgets
        if(isWidgetAdded){
            if(mAppWidgetProvider==null)
                mAppWidgetProvider=MediaAppWidgetProvider.getInstance();
            mAppWidgetProvider.notifyChange(this, what);
        }
		
	    notifyWidget4X1UI(what);
	    notifyWidget4X3UI(what);
    }

	private void notifyWidget4X3UI(final String what) {
		
		if(!isWidget4X3Added) {
			return;
		}
		
		/*new Thread() {
			public void run() {
				if(mWidget4x3==null) {
                	mWidget4x3 = Widget4x3.getInstance();
				}
	            mWidget4x3.notifyChange(MediaPlaybackService.this, what);
			}
		}.start();*/

		if(mWidget4x3==null) {
        	mWidget4x3 = Widget4x3.getInstance();
		}
        mWidget4x3.notifyChange(MediaPlaybackService.this, what);
	}

	private void updateWidget4X3UI(final int[] appWidgetIds) {

		if(!isWidget4X3Added) {
			return;
		}
		

		if(mWidget4x3==null) {
        	mWidget4x3 = Widget4x3.getInstance();
		}
        mWidget4x3.performUpdate(MediaPlaybackService.this, appWidgetIds);
	}

    private void notifyWidget4X1UI(final String what) {
        if(!isWidget4X1Added) {
            return;
        }

        if(mWidget4x1==null) {
            mWidget4x1 = Widget4x1.getInstance();
        }
        mWidget4x1.notifyChange(MediaPlaybackService.this, what);
    }

    private void updateWidget4X1UI(final int[] appWidgetIds) {
        if(!isWidget4X1Added) {
            return;
        }

        if(mWidget4x1==null) {
            mWidget4x1 = Widget4x1.getInstance();
        }
        
        mWidget4x1.performUpdate(MediaPlaybackService.this, appWidgetIds);
    }
	
    private void ensurePlayListCapacity(int size) {
        if (mPlayList == null || size > mPlayList.length) {
            // reallocate at 2x requested size so we don't
            // need to grow and copy the array for every
            // insert
            long[] newlist = new long[size * 2];
            int len = mPlayList != null ? mPlayList.length : mPlayListLen;
            for (int i = 0; i < len; i++) {
                newlist[i] = mPlayList[i];
            }
            mPlayList = newlist;
        }
        // FIXME: shrink the array when the needed size is much smaller
        // than the allocated size
    }

    // insert the list of songs at the specified position in the playlist
    private void addToPlayList(long[] list, int position) {
        int addlen = list.length;
        if (position < 0) { // overwrite
            mPlayListLen = 0;
            position = 0;
        }
        ensurePlayListCapacity(mPlayListLen + addlen);
        if (position > mPlayListLen) {
            position = mPlayListLen;
        }

        // move part of list after insertion point
        int tailsize = mPlayListLen - position;
        for (int i = tailsize; i > 0; i--) {
            if((position+i-addlen)>=0)
                mPlayList[position + i] = mPlayList[position + i - addlen];
        }

        // copy list into playlist
        for (int i = 0; i < addlen; i++) {
            mPlayList[position + i] = list[i];
        }
        if(mPlayList!=null)
            mPlayListLen += addlen;
        if (mPlayListLen == 0) {
            mCursor.close();
            mCursor = null;
            notifyChange(META_CHANGED);
        }
    }

    /**
     * Appends a list of tracks to the current playlist. If nothing is playing
     * currently, playback will be started at the first track. If the action is
     * NOW, playback will switch to the first of the new tracks immediately.
     *
     * @param list
     *            The list of tracks to append.
     * @param action
     *            NOW, NEXT or LAST
     */
    public void enqueue(long[] list, int action) {
        synchronized (this) {
            if (action == NEXT && mPlayPos + 1 < mPlayListLen) {
                addToPlayList(list, mPlayPos + 1);
                notifyChange(QUEUE_CHANGED);
            } else {
                // action == LAST || action == NOW || mPlayPos + 1 ==
                // mPlayListLen
                addToPlayList(list, Integer.MAX_VALUE);
                notifyChange(QUEUE_CHANGED);
                if (action == NOW) {
                    mPlayPos = mPlayListLen - list.length;
                    openCurrent();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                }
            }
            if (mPlayPos < 0) {
                mPlayPos = 0;
                openCurrent();
                play();
                notifyChange(META_CHANGED);
            }
        }
    }

    /**
     * Replaces the current playlist with a new list, and prepares for starting
     * playback at the specified position in the list, or a random position if
     * the specified position is 0.
     *
     * @param list
     *            The new list of tracks.
     */
    public void open(long[] list, int position) {
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_AUTO) {
                mShuffleMode = SHUFFLE_NORMAL;
            }
            long oldId = getAudioId();
            int listlength = list.length;
            boolean newlist = true;
            if (mPlayListLen == listlength) {
                // possible fast path: list might be the same
                newlist = false;
                for (int i = 0; i < listlength; i++) {
                    if (list[i] != mPlayList[i]) {
                        newlist = true;
                        break;
                    }
                }
            }
            if (newlist) {
                // this.removeTracks(0, Integer.MAX_VALUE);
                addToPlayList(list, -1);
                notifyChange(QUEUE_CHANGED);
            }
            int oldpos = mPlayPos;
            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mRand.nextInt(mPlayListLen);
            }
            mHistory.clear();
            saveBookmarkIfNeeded();

            // openCurrent();
            // if (oldId != getAudioId()) {
            // notifyChange(META_CHANGED);
            // }
            SharedPreferences music_settings = this.getSharedPreferences("Music_setting", 0);
            int a = music_settings.getInt("isFade", 0);
            if (a == 1) {
                mMediaplayerHandler.sendEmptyMessage(FADEOUTOPEN);
            } else {
                nextAfterFadeOut();
            }

        }
    }

    /**
     * Moves the item at index1 to index2.
     *
     * @param index1
     * @param index2
     */
    public void moveQueueItem(int index1, int index2) {
        synchronized (this) {
            if (index1 >= mPlayListLen) {
                index1 = mPlayListLen - 1;
            }
            if (index2 >= mPlayListLen) {
                index2 = mPlayListLen - 1;
            }
            if (index1 < index2) {
                long tmp = mPlayList[index1];
                for (int i = index1; i < index2; i++) {
                    mPlayList[i] = mPlayList[i + 1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index1 && mPlayPos <= index2) {
                    mPlayPos--;
                }
            } else if (index2 < index1) {
                long tmp = mPlayList[index1];
                for (int i = index1; i > index2; i--) {
                    mPlayList[i] = mPlayList[i - 1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index2 && mPlayPos <= index1) {
                    mPlayPos++;
                }
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    /**
     * Returns the current play list
     *
     * @return An array of integers containing the IDs of the tracks in the play
     *         list
     */
    public long[] getQueue() {
        synchronized (this) {
            int len = mPlayListLen;
            long[] list = new long[len];
            for (int i = 0; i < len; i++) {
                list[i] = mPlayList[i];
            }
            return list;
        }
    }

    private void openCurrent() {
        synchronized (this) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }

            if (mPlayListLen == 0) {
                return;
            }
            openCurrent1();
        }
    }

    private void openCurrent1() {
        stop(false);
        if (mPlayPos > mPlayList.length - 1)
            mPlayPos = 0;
        long audio_id=mPlayList[mPlayPos];
        Log.i(TAG, "Open song: " + audio_id);
        String id = String.valueOf(audio_id);
        if(audio_id<0&&Math.abs(audio_id)!=1){
            
            /**
             * retrieve from memory instead
             */
            Song onlineSong = Lewa.getPlayingSong(Math.abs(audio_id));
            //Log.i(TAG, "onlineSong = " + onlineSong);
            if(onlineSong != null) {
                MediaPlaybackService.isOnlinePlay = true;
                String track = onlineSong.getName();
                String artist = onlineSong.getArtist() == null ? null : onlineSong.getArtist().getName();
                Intent intent=new Intent(MediaPlaybackService.ONLINE_PLAY);
                intent.putExtra("songId", String.valueOf(Math.abs(audio_id)));
                intent.putExtra("type", Song.TYPE.ONLINE.name());
                if(null != track) {
                    intent.putExtra("songName", track);
                } else {	//add by sjxu 2014/06/20
                    String playingName = Lewa.getPlayingSongName();
                    if(null != playingName) {
                        intent.putExtra("songName", playingName);
                    }
                }

                if(null != artist) {
                    intent.putExtra("songAuthor", artist);
                } else {  //add by sjxu 2014/06/20
                    String playingArtist = Lewa.getPlayingSongArtist();
                    if(null != playingArtist) {
                        intent.putExtra("songAuthor", playingArtist);
                    }
                }
                sendBroadcast(intent);
            }else{
                next(true);
            }
            return;
        }
        mCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols,
                "_id=" + id, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            MediaPlaybackService.isOnlinePlay = false;
            open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id);
            // go to bookmark if needed
            /*
             * if (isPodcast()) { long bookmark = getBookmark(); // Start
             * playing a little bit before the bookmark, // so it's easier to
             * get back in to the narrative. seek(bookmark - 5000); }
             */
        }
    }

    /**
     * Opens the specified file and readies it for playback.
     *
     * @param path
     *            The full path of the file to be opened.
     */
    public void open(String path) {
        if(mPlayer==null)
            return;
        synchronized (this) {
            if (path == null) {
                return;
            }

            // if mCursor is null, try to associate path with a database cursor
            if (mCursor == null) {

                ContentResolver resolver = getContentResolver();
                Uri uri;
                String where;
                String selectionArgs[];
                if (path.startsWith("content://media/")) {
                    uri = Uri.parse(path);
                    where = null;
                    selectionArgs = null;
                } else {
                    uri = MediaStore.Audio.Media.getContentUriForPath(path);
                    where = MediaStore.Audio.Media.DATA + "=?";
                    selectionArgs = new String[] { path };
                }

                try {
                    mCursor = resolver.query(uri, mCursorCols, where,
                            selectionArgs, null);
                    if (mCursor != null) {
                        if (mCursor.getCount() == 0) {
                            mCursor.close();
                            mCursor = null;
                        } else {
                            mCursor.moveToNext();
                            ensurePlayListCapacity(1);
                            mPlayListLen = 1;
                            mPlayList[0] = mCursor.getLong(IDCOLIDX);
                            mPlayPos = 0;
                        }
                    }
                } catch (UnsupportedOperationException ex) {
                }
            }
            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (!mPlayer.isInitialized()) {
                stop(true);
                if (mOpenFailedCounter++ <= 5 && mPlayListLen > 1) {
                    // beware: this ends up being recursive because next() calls
                    // open() again.
                    if (mOpenFailedCounter != 0) {
                        // need to make sure we only shows this once
                        if (!mQuietMode) {
                            /*long id = mPlayList[mPlayPos];
                            String name = String
                                    .valueOf(MusicUtils.getSongName(
                                            getApplicationContext(), id)[0]);*/
                            
                            String message = getString(
                                    R.string.playcurrent_failed);//, name);
                            Toast.makeText(this, message, Toast.LENGTH_SHORT)
                                    .show();
                        }
//                        Log.d(LOGTAG, "Failed to open file for playback");
                    }
                    next(false);
                }else{
                    mOpenFailedCounter=0;
                }

            } else {
                mOpenFailedCounter = 0;
            }

        }
    }

    
    private boolean phoneIsInUse() {
         boolean phoneInUse = false;
         try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneInUse = !phone.isIdle();
         } catch (RemoteException e) {
            Log.w(TAG, "phone.isIdle() failed", e);
         }
         return phoneInUse;
    }

    /**
     * Starts playback of a previously opened file.
     */
    public void play() {
        Log.i(TAG, "play");

        if(phoneIsInUse()){
            Log.i(TAG, "is Calling return");
            isStatusCanCancel=true;
            mIsStatusPause = true;
            isNotification=true;
            mPausedByTransientLossOfFocus = false;
            isRingCome=true;
            return; 
        }

        
        isStatusCanCancel=false;
        mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(this
                .getPackageName(), MediaButtonIntentReceiver.class.getName()));

        if (mPlayer!=null && mPlayer.isInitialized()) {

            if(!OnlineLoader.isWiFiActive(this) && !OnlineLoader.IsConnection(this) && isOnlinePlay) {
                Toast.makeText(this, Lewa.string(R.string.no_network_text), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // if we are at the end of the song, go to the next song first
            long duration = mPlayer.duration();
            if (mRepeatMode != REPEAT_CURRENT && duration > 2000
                    && mPlayer.position() >= duration - 2000) {
                next(true);
            }
            
            // mMediaplayerHandler.sendEmptyMessage(FADEIN);
            SharedPreferences music_settings = this.getSharedPreferences("Music_setting", 0);
            int a = music_settings.getInt("isFade", 0);
            if (a == 1) {
                mPlayer.setVolume(0);
                mMediaplayerHandler.sendEmptyMessage(VOLUME_CLEARED_FADEIN);
            } else {
                mPlayer.setVolume(1.0f);
            }

            mPlayer.start();

            // make sure we fade in, in case a previous fadein was stopped
            // because
            // of another focus loss
            // mMediaplayerHandler.



            if (/*!mIsSupposedToBePlaying*/true) {
                mIsSupposedToBePlaying = true;
                notifyChange(PLAYSTATE_CHANGED);
                notifyChange(META_CHANGED);
                // add by wangliqiang
//                updateNotification();
            }

        } else if (mPlayListLen <= 0) {
            Log.i(TAG, "No track queued, start play.");
            // This is mostly so that if you press 'play' on a bluetooth headset
            // without every having played anything before, it will still play
            // something.
            if (!MusicUtils.mHasSongs
                    || !Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                notifyChange(META_CHANGED);
                return;
            }
            setShuffleMode(SHUFFLE_AUTO);
            // openCurrent();
            if(mPlayListLen>0)
                play();
        }
        queueNextRefresh(1000);
    }
    
    
    private static Object lockObject = new Object();    //this for synchroize generateIconDrawable(...) func
    private synchronized void updateNotification() {
        new Thread(){          
            public void run() {
                notifyPlayChanged();
            	Song song=Lewa.getPlayingSong();
            	//pr957592 add by wjhu begin
            	//when the music is not playing,cancel the notification
            	if (!isPlaying()) {
            	    if (notificationManager != null) {
            	        notificationManager.cancel(PLAYBACKSERVICE_STATUS);
                        return;
            	    }
            	}
            	//pr957592 add by wjhu end
                if(song==null||song.getName()==null)
                    return;
                if(!mIsSupposedToBePlaying&&!isNotification)
                    return;
                Intent intent;
                PendingIntent pIntent;
                intent = new Intent(Constants.ACTION_PLAY_VIEWER);
                intent.putExtra("collapse_statusbar", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                pIntent = PendingIntent.getActivity(MediaPlaybackService.this, 0, intent, 0);
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.statusbar);
                String track = song.getName();
                String artist = song.getArtist() == null ? null : song.getArtist().getName();
                views.setOnClickPendingIntent(R.id.player_status,pIntent );
                Bitmap artistBm = null; 

                if(artist != null && !MediaStore.UNKNOWN_STRING.equals(artist)) {
                    Bitmap b=MusicUtils.getBitmapFromMemoryCathe(artist.trim()+"notify");
                    if(b!=null&&!b.isRecycled()){   //get bitmap from cache
                        artistBm=b;
                    }else{  //get bitmap from sd
                        artistBm = MusicUtils.getLocalBitmap(MediaPlaybackService.this, LewaUtils.getArtistPicPath(artist.trim()),statusIconWidth,statusIconWidth);
                        if(artistBm == null) {  //need correct bitmap path when not found bitmap from sd 
                            String name = WidgetUtils.getAtristName(artist);
            			 String path = LewaUtils.getArtistPicPath(name);
            			  artistBm = MusicUtils.getLocalBitmap(MediaPlaybackService.this, path,statusIconWidth,statusIconWidth);
                        } else {    //add bitmap to memory cache
                            MusicUtils.addBitmapToMemoryCathe(artist.trim()+"notify", artistBm);
                        }
                    }
                }

                if(null == artistBm) {
                    if(artist == null || MediaStore.UNKNOWN_STRING.equals(artist)) {
                        artist = getString(R.string.unknown_artist_name);
                    }
                    artistBm = BitmapFactory.decodeResource(getResources(), R.drawable.app_music); 
                }

                if( (null != artistBm) && ( !artistBm.isRecycled() ) ){
                    BitmapDrawable drawable =new BitmapDrawable(artistBm);            
                    synchronized(lockObject) {
                        drawable = lewa.notifications.IconCustomizer.generateIconDrawable(drawable,true);
                    }
                    artistBm = drawable.getBitmap();
                    int width= getResources().getDimensionPixelSize(R.dimen.notification_img_width);                    
                    Matrix matrix = new Matrix();
                    int srcWidth = artistBm.getWidth();
                    int srcHeight = artistBm.getHeight();
                    float scaleHeight = (float)width / srcHeight;
                    float scaleWidth = (float)width / srcWidth;
                    matrix.postScale(scaleWidth, scaleHeight);
                    artistBm = Bitmap.createBitmap(artistBm, 0, 0, srcWidth, srcHeight, matrix, true); 
                }
                
                views.setImageViewBitmap(R.id.status_cover, artistBm);
                views.setTextViewText(R.id.status_song, track);
                views.setTextViewText(R.id.status_artist, artist);

                views.setOnClickPendingIntent(R.id.status_cover, pIntent);
                artist=MusicUtils.buildArtistName(artist);

                

                final boolean playing = isPlaying();
                if (playing) {
                    views.setImageViewResource(R.id.status_btn_pause,
                            R.drawable.statusbar_pause_selector);
                } else {
                    views.setImageViewResource(R.id.status_btn_pause,
                            R.drawable.statusbar_play_selector);
                }

                intent = new Intent(STATUSPAUSE_ACTION);
                intent.setClass(MediaPlaybackService.this, MediaPlaybackService.class);
                pIntent = PendingIntent.getService(MediaPlaybackService.this, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.status_btn_pause, pIntent);

                intent = new Intent(NEXT_ACTION);
                intent.setClass(MediaPlaybackService.this, MediaPlaybackService.class);
                pIntent = PendingIntent.getService(MediaPlaybackService.this, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.status_btn_next, pIntent);

                intent = new Intent(PREVIOUS_ACTION);
                intent.setClass(MediaPlaybackService.this, MediaPlaybackService.class);
                pIntent = PendingIntent.getService(MediaPlaybackService.this, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.status_btn_prev, pIntent);

                // intent = new Intent("my.nullaction");
                // //pIntent = PendingIntent.getService(this, 0, null, 0);
                // pIntent = PendingIntent.getService(this, 0, intent, 0);
                // views.setOnClickPendingIntent(R.id.player_status, pIntent);

                Notification status = new Notification();
                status.contentView = views;
                status.icon = R.drawable.play_status1;
                // modified by wangliqiang
                if (isStatusCanCancel) {
                    status.flags |= Notification.FLAG_AUTO_CANCEL;
                } else {
                    status.flags |= Notification.FLAG_ONGOING_EVENT;
                }
                status.contentIntent = PendingIntent.getService(MediaPlaybackService.this, 0, intent, 0);
                notificationManager = (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                // startForeground(PLAYBACKSERVICE_STATUS, status);
                String sdStatus = Environment.getExternalStorageState();
                if (sdStatus.equals(Environment.MEDIA_SHARED)
                        || sdStatus.equals(Environment.MEDIA_UNMOUNTED)
                        || sdStatus.equals(Environment.MEDIA_REMOVED)) {

                } else {
                    if(mIsSupposedToBePlaying||isNotification){
                        if (notificationManager != null) {
                            notificationManager.notify(PLAYBACKSERVICE_STATUS, status);
                            isNotification=false;
                        }
                    }
                }
            };
        }.start();

    }

    private void queueNextRefresh(long delay) {
        Message msg = mHandler.obtainMessage(REFRESH);
        mHandler.removeMessages(REFRESH);
        mHandler.sendMessageDelayed(msg, delay);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (REFRESH == msg.what) {
                if(isWidgetAdded){
                    if(mAppWidgetProvider==null) {
                        mAppWidgetProvider=MediaAppWidgetProvider.getInstance();
                    }
                    mAppWidgetProvider.performUpdate(MediaPlaybackService.this, null);
                 }

                updateWidget4X1UI(null);
                updateWidget4X3UI(null);
                if (isPlaying()) {
                    queueNextRefresh(1000);
                }
            } else if(WAIT_PLAY_NEXT == msg.what ){
                next(true);
            }
        }
    };

    private void stop(boolean remove_status_icon) {
        Log.i(TAG, "stop remove_status_icon = " + remove_status_icon);
        saveQueue(true);
        if (mPlayer!=null&&mPlayer.isInitialized()) {
            mPlayer.stop();
            mIsSupposedToBePlaying = false;
            // add by wangliqiang
            if(remove_status_icon&&mAppWidgetProvider!=null&&isWidgetAdded)
                mAppWidgetProvider.performUpdate(MediaPlaybackService.this,
                        appWidgetIds);

			if(remove_status_icon) {
				updateWidget4X3UI(appWidgetIds);
				updateWidget4X1UI(appWidgetIds);
			}
        }
        mFileToPlay = null;
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if (remove_status_icon) {
            gotoIdleState();
        }
    }

    /**
     * Stops playback.
     */
    public void stop() {
        stop(true);
    }

    /**
     * Pauses playback (call play() to resume)
     */
    public void pause() {
        Log.i(TAG, "pause");
        synchronized (this) {
            isNotification=true;
	      mIsStatusPause = true;
            isStatusCanCancel=true;
            mMediaplayerHandler.removeMessages(FADEIN);
            // mMediaplayerHandler.removeMessages(FADEOUTOPEN);
            if (isPlaying()) {

                saveBookmarkIfNeeded();
                SharedPreferences music_settings = this.getSharedPreferences(
                        "Music_setting", 0);
                int a = music_settings.getInt("isFade", 0);
                if (a == 1) {
                    mMediaplayerHandler.sendEmptyMessage(FADEOUTPAUSE);
                } else {
                    pauseAfterFadeOut();
                }
            }
        }
    }

    private void pauseAfterFadeOut() {

        mIsSupposedToBePlaying = false;
        mPlayer.pause();
        gotoIdleState();
        notifyChange(PLAYSTATE_CHANGED);
    }

    /**
     * Returns whether something is currently playing
     *
     * @return true if something is playing (or will be playing shortly, in case
     *         we're currently transitioning between tracks), false if not.
     */
    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    /*
     * Desired behavior for prev/next/shuffle:
     *
     * - NEXT will move to the next track in the list when not shuffling, and to
     * a track randomly picked from the not-yet-played tracks when shuffling. If
     * all tracks have already been played, pick from the full set, but avoid
     * picking the previously played track if possible. - when shuffling, PREV
     * will go to the previously played track. Hitting PREV again will go to the
     * track played before that, etc. When the start of the history has been
     * reached, PREV is a no-op. When not shuffling, PREV will go to the
     * sequentially previous track (the difference with the shuffle-case is
     * mainly that when not shuffling, the user can back up to tracks that are
     * not in the history).
     *
     * Example: When playing an album with 10 tracks from the start, and
     * enabling shuffle while playing track 5, the remaining tracks (6-10) will
     * be shuffled, e.g. the final play order might be 1-2-3-4-5-8-10-6-9-7.
     * When hitting 'prev' 8 times while playing track 7 in this example, the
     * user will go to tracks 9-6-10-8-5-4-3-2. If the user then hits 'next', a
     * random track will be picked again. If at any time user disables shuffling
     * the next/previous track will be picked in sequential order again.
     */

    public void prev() {
        if(mPlayListLen>=1){
            /*if(isOnlinePlay){ //not use
                sendBroadcast(new Intent("com.lewa.player.onlinetolocal"));
            }*/
            isOnlinePlay=false;
            isFromOnlinePlay=false;
        }
        synchronized (this) {
            if (mShuffleMode != SHUFFLE_NONE) {
                int hsize = mHistory.size();
                if (hsize == 0) {
                    // prev is a no-op
                    // return;
                    mPlayPos = mRand.nextInt(mPlayListLen);
                } else {
                    Integer pos = mHistory.remove(hsize - 1);
                    mPlayPos = pos.intValue();
                }
            }
            /*
             * else if (mShuffleMode == SHUFFLE_NORMAL) { // go to
             * previously-played track and remove it from the history int
             * histsize = mHistory.size(); if (histsize == 0) { // prev is a
             * no-op return; } Integer pos = mHistory.remove(histsize - 1);
             * mPlayPos = pos.intValue(); }
             */else {
                if (mPlayPos > 0) {
                    mPlayPos--;
                } else {
                    mPlayPos = mPlayListLen - 1;
                }
            }
            saveBookmarkIfNeeded();
            // stop(false);
            // openCurrent();
            // play();
            // notifyChange(META_CHANGED);

            SharedPreferences music_settings = this.getSharedPreferences(
                    "Music_setting", 0);
            int a = music_settings.getInt("isFade", 0);
            // Log.i("isFade",a+"");
            // this.fadeouttype = FADEOUTNEXT;
            if (a == 1) {
                mMediaplayerHandler.sendEmptyMessage(FADEOUTOPEN);
            } else {
                nextAfterFadeOut();
            }
        }
    }

    public void next(boolean force) {
        synchronized (this) {
            if (mPlayListLen <= 0) {
                if (!MusicUtils.mHasSongs
                        || !Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    notifyChange(META_CHANGED);
                    return;
                }
                setShuffleMode(SHUFFLE_AUTO);
                play();
            }
            /*  //not use
            if(isOnlinePlay){
                sendBroadcast(new Intent("com.lewa.player.onlinetolocal"));
            }*/
            isOnlinePlay=false;
            isFromOnlinePlay=false;

            /*
             * if (mShuffleMode == SHUFFLE_NORMAL) { // Pick random next track
             * from the not-yet-played ones // TODO: make it work right after
             * adding/removing items in the queue.
             *
             * // Store the current file in the history, but keep the history at
             * a // reasonable size if (mPlayPos >= 0) { mHistory.add(mPlayPos);
             * } if (mHistory.size() > MAX_HISTORY_SIZE) {
             * mHistory.removeElementAt(0); }
             *
             * int numTracks = mPlayListLen; int[] tracks = new int[numTracks];
             * for (int i=0;i < numTracks; i++) { tracks[i] = i; }
             *
             * int numHistory = mHistory.size(); int numUnplayed = numTracks;
             * for (int i=0;i < numHistory; i++) { int idx =
             * mHistory.get(i).intValue(); if (idx < numTracks && tracks[idx] >=
             * 0) { numUnplayed--; tracks[idx] = -1; } }
             *
             * // 'numUnplayed' now indicates how many tracks have not yet //
             * been played, and 'tracks' contains the indices of those //
             * tracks. if (numUnplayed <=0) { // everything's already been
             * played if (mRepeatMode == REPEAT_ALL || force) { //pick from full
             * set numUnplayed = numTracks; for (int i=0;i < numTracks; i++) {
             * tracks[i] = i; } } else { // all done gotoIdleState(); if
             * (mIsSupposedToBePlaying) { mIsSupposedToBePlaying = false;
             * notifyChange(PLAYSTATE_CHANGED); } return; } } int skip =
             * mRand.nextInt(numUnplayed); int cnt = -1; while (true) { while
             * (tracks[++cnt] < 0) ; skip--; if (skip < 0) { break; } } mPlayPos
             * = cnt; } else if (mShuffleMode == SHUFFLE_AUTO) {
             * //doAutoShuffleUpdate(); mPlayPos++; } else {
             */
            if (mShuffleMode != SHUFFLE_NONE) {
                // int a = SHUFFLE_NONE;
                // int b = SHUFFLE_NORMAL;
                mHistory.add(mPlayPos);
                mPlayPos = mRand.nextInt(mPlayListLen);
            } else {
                if (mPlayPos >= mPlayListLen - 1) {
                    // we're at the end of the list
                    if (mRepeatMode == REPEAT_NONE && !force) {
                        // all done
                        gotoIdleState();
                        mIsSupposedToBePlaying = false;
                        notifyChange(PLAYSTATE_CHANGED);
                        // add by wangliqiang
//                        updateNotification();
                        return;
                    } else if (mRepeatMode == REPEAT_ALL || force) {
                        mPlayPos = 0;
                    }
                } else {
                    mPlayPos++;
                }
                // }
            }
            saveBookmarkIfNeeded();
            SharedPreferences music_settings = this.getSharedPreferences(
                    "Music_setting", 0);
            int a = music_settings.getInt("isFade", 0);
            // Log.i("isFade",a+"");
            // this.fadeouttype = FADEOUTNEXT;
            if (a == 1) {
                mMediaplayerHandler.sendEmptyMessage(FADEOUTOPEN);
            } else {
                nextAfterFadeOut();
            }
            isStatusCanCancel=false;

        }
    }

    private void nextAfterFadeOut() {
        stop(false);
        openCurrent();
        if(!isOnlinePlay) {
            play();       //del by sjxu for test 
        }
        //notifyChange(META_CHANGED);	//this invoke move to paly()
        Intent intent = new Intent();
        intent.setAction(MusicUtils.UPDATE_NOWPLAYINGALBUM);
        // intent.putExtra("albumbitmap", bm);
        sendBroadcast(intent);
    }

    private void notifyPlayChanged() {
        if(!mIsSupposedToBePlaying) {
            return;
        }
        Intent screenIntent=new Intent("com.lewa.player.lockscreen");
        screenIntent.putExtra("track", Lewa.getPlayingSongName());
        String artistName = Lewa.getPlayingSongArtist();
        artistName = isCanRead(artistName);
        screenIntent.putExtra("artist", artistName);
        screenIntent.putExtra(EXTRA_IS_PLAYING, mIsSupposedToBePlaying);
        sendBroadcast(screenIntent);

    }

    private String isCanRead(String artistName) {
        String path = LewaUtils.getArtistPicPath(artistName);
        try {
            File f = new File(path);
            if(null == f || !f.exists()) {
                String name = WidgetUtils.getAtristName(artistName);
                path = LewaUtils.getArtistPicPath(name);
                f = new File(path);
                if(f.exists()) {
                    return name;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return artistName;
    }

    private void gotoIdleState() {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        if (!mIsStatusPause) {
            // stopForeground(true);
            if (notificationManager != null) {
                notificationManager.cancel(PLAYBACKSERVICE_STATUS);
            }
        } else {
            updateNotification();
            mIsStatusPause = false;
        }
    }

    private void saveBookmarkIfNeeded() {
        try {
            if (isPodcast()) {
                long pos = position();
                long bookmark = getBookmark();
                long duration = duration();
                if ((pos < bookmark && (pos + 10000) > bookmark)
                        || (pos > bookmark && (pos - 10000) < bookmark)) {
                    // The existing bookmark is close to the current
                    // position, so don't update it.
                    return;
                }
                if (pos < 15000 || (pos + 10000) > duration) {
                    // if we're near the start or end, clear the bookmark
                    pos = 0;
                }

                // write 'pos' to the bookmark field
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.BOOKMARK, pos);
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mCursor.getLong(IDCOLIDX));
                getContentResolver().update(uri, values, null, null);
            }
        } catch (SQLiteException ex) {
        }
    }

    // Make sure there are at least 5 items after the currently playing item
    // and no more than 10 items before.
    private void doAutoShuffleUpdate() {
        boolean notify = false;

        // remove old entries
        if (mPlayPos > 10) {
            removeTracks(0, mAutoShuffleList.length);
            notify = true;
        }
        mHistory.clear();
        // add new entries if needed

        long first = getAudioId();
        if (first < 0) {
            mAutoShuffleList = MusicUtils.getAllSongs(getApplicationContext());
            mPlayList = MusicUtils.getAllSongs(getApplicationContext());
            if (mPlayList == null || mPlayList != null && mPlayList.length == 0) {
                return;
            }
            first = mAutoShuffleList[mRand.nextInt(mPlayList.length)];
        }
//        Log.i("mAutoShuffleList", mAutoShuffleList.length + "");
//        Log.i("mPlayListLen", mPlayList.length + "");
        mPlayListLen = mAutoShuffleList.length;
        mPlayList = new long[mPlayListLen];
        for (int i = 0; i < mPlayListLen; i++) {
            mPlayList[i] = mAutoShuffleList[i];
        }
        /*
         * mPlayList[0] = first; mPlayListLen++;
         *
         * int to_add = mAutoShuffleList.length ;//- //(mPlayListLen - (mPlayPos
         * < 0 ? -1 : mPlayPos));
         *
         * while(mPlayListLen < to_add){//for (int i = 0; i <= to_add + 1; i++)
         * { // pick something at random from the list
         *
         * int lookback = mHistory.size(); int idx = -1; while(true) { idx =
         * mRand.nextInt(mAutoShuffleList.length); if (!wasRecentlyUsed(idx,
         * lookback)) { break; } //lookback /= 2; } if( mAutoShuffleList[idx] ==
         * mPlayList[0]){ continue; } mHistory.add(idx); if (mHistory.size() >
         * MAX_HISTORY_SIZE) { mHistory.remove(0); }
         * ensurePlayListCapacity(mPlayListLen + 1); mPlayList[mPlayListLen++] =
         * mAutoShuffleList[idx]; notify = true; }
         */
        if (notify) {
            notifyChange(QUEUE_CHANGED);
        }
    }

    // check that the specified idx is not in the history (but only look at at
    // most lookbacksize entries in the history)
    private boolean wasRecentlyUsed(int idx, int lookbacksize) {

        // early exit to prevent infinite loops in case idx == mPlayPos
        if (lookbacksize == 0) {
            return false;
        }

        int histsize = mHistory.size();
        if (histsize < lookbacksize) {
//            Log.d(LOGTAG, "lookback too big");
            lookbacksize = histsize;
        }
        int maxidx = histsize - 1;
        for (int i = 0; i < lookbacksize; i++) {
            long entry = mHistory.get(i);
            if (entry == idx) {
                return true;
            }
        }
        return false;
    }

    // A simple variation of Random that makes sure that the
    // value it returns is not equal to the value it returned
    // previously, unless the interval is 1.
    private static class Shuffler {
        private int mPrevious;
        private Random mRandom = new Random();

        public int nextInt(int interval) {
            if(interval==0)
                return -1;
            int ret;
            do {
                ret = mRandom.nextInt(interval);
            } while (ret == mPrevious && interval > 1);
            mPrevious = ret;
            return ret;
        }
    };

    private boolean makeAutoShuffleList() {
        ContentResolver res = getContentResolver();
        Cursor c = null;
        try {
            /*
             * c = res.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new
             * String[] {MediaStore.Audio.Media._ID},
             * MediaStore.Audio.Media.IS_MUSIC + "=1", null, null); if (c ==
             * null || c.getCount() == 0) { return false; } int len =
             * c.getCount(); long [] list = new long[len]; for (int i = 0; i <
             * len; i++) { c.moveToNext(); list[i] = c.getLong(0); }
             */
            long[] list = this.getQueue();
            // list = this.getQueue();
            mAutoShuffleList = list;
            return true;
        } catch (RuntimeException ex) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return false;
    }

    /**
     * Removes the range of tracks specified from the play list. If a file
     * within the range is the file currently being played, playback will move
     * to the next file after the range.
     *
     * @param first
     *            The first file to be removed
     * @param last
     *            The last file to be removed
     * @return the number of tracks deleted
     */
    public int removeTracks(int first, int last) {
        int numremoved = removeTracksInternal(first, last);
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numremoved;
    }

    private int removeTracksInternal(int first, int last) {
        synchronized (this) {
            if (last < first)
                return 0;
            if (first < 0)
                first = 0;
            if (last >= mPlayListLen)
                last = mPlayListLen - 1;

            boolean gotonext = false;
            if (first <= mPlayPos && mPlayPos <= last) {
                mPlayPos = first;
                gotonext = true;
            } else if (mPlayPos > last) {
                mPlayPos -= (last - first + 1);
            }
            int num = mPlayListLen - last - 1;
            for (int i = 0; i < num; i++) {
                mPlayList[first + i] = mPlayList[last + 1 + i];
            }
            mPlayListLen -= last - first + 1;

            if (gotonext&&!isOnlinePlay) {
                if (mPlayListLen == 0) {
                    stop(true);
                    mPlayPos = -1;
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                } else {
                    if (mPlayPos >= mPlayListLen) {
                        mPlayPos = 0;
                    }
                    boolean wasPlaying = isPlaying();
                    stop(false);
                    openCurrent();
                    if (wasPlaying) {
                        play();
                    }
                }
                if(mPlayListLen<=0)
                    MusicUtils.mHasSongs=false;
                notifyChange(META_CHANGED);
            }
            return last - first + 1;
        }
    }

    /**
     * Removes all instances of the track with the given id from the playlist.
     *
     * @param id
     *            The id to be removed
     * @return how many instances of the track were removed
     */
    public int removeTrack(long id) {
        int numremoved = 0;
        synchronized (this) {
            for (int i = 0; i < mPlayListLen; i++) {
                if (mPlayList[i] == id) {
                    numremoved += removeTracksInternal(i, i);
                    i--;
                }
            }
        }
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numremoved;
    }

    public void setShuffleMode(int shufflemode) {
        synchronized (this) {
            if (mShuffleMode == shufflemode && mPlayListLen > 0) {
                return;
            }
            mShuffleMode = shufflemode;
            if (mShuffleMode == SHUFFLE_AUTO) {
                if (makeAutoShuffleList()) {
                    mPlayListLen = 0;
                    doAutoShuffleUpdate();
                    if(mPlayListLen==0){
                        return;
                    }
                    if (mPlayPos == -1) {
                        mPlayPos = mRand.nextInt(mPlayListLen);
                    }
                    // mPlayPos = 0;
                    // Log.i("mPlayPos",""+mPlayPos);
                    if (position() < 0) {
                        openCurrent();
                    }
                    // if(isPlaying()) {
                    // play();
                    // }
                    if(!isOnlinePlay)
                        notifyChange(META_CHANGED);
                    saveQueue(true);
                    return;
                } else {
                    // failed to build a list of files to shuffle
                    mShuffleMode = SHUFFLE_NONE;
                }
            }
            saveQueue(false);
        }
    }

    public int getShuffleMode() {
        return mShuffleMode;
    }

    public void setRepeatMode(int repeatmode) {
        synchronized (this) {
            mRepeatMode = repeatmode;
            saveQueue(false);
        }
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public int getMediaMountedCount() {
        return mMediaMountedCount;
    }

    /**
     * Returns the path of the currently playing file, or null if no file is
     * currently playing.
     */
    public String getPath() {
        if(!isOnlinePlay){
            return mFileToPlay;
        }else{
            return trackName_online;
        }
    }

    /**
     * Returns the rowid of the currently playing file, or -1 if no file is
     * currently playing.
     */
    public long getAudioId() {
        synchronized (this) {
            if(!isOnlinePlay){
                if (mPlayPos >= 0 &&mPlayList!=null&&mPlayPos<mPlayList.length&& mPlayer!=null&&mPlayer.isInitialized()) {
                    return mPlayList[mPlayPos];
                }
            }else{
//                return -100;
                /**
                 * return id passed as well
                 */
                if (mPlayPos >= 0 &&mPlayList!=null&&mPlayPos<mPlayList.length) {
                    return mPlayList[mPlayPos];
                }
            }
        }
        return -1;
    }

    /**
     * Returns the position in the queue
     *
     * @return the position in the queue
     */
    public int getQueuePosition() {
        synchronized (this) {
            return mPlayPos;
        }
    }

    /**
     * Starts playing the track at the given position in the queue.
     *
     * @param pos
     *            The position in the queue of the track that will be played.
     */
    public void setQueuePosition(int pos) {
        synchronized (this) {
            stop(false);
            mPlayPos = pos;
            openCurrent();
            play();
            notifyChange(META_CHANGED);
            if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
            }
        }
    }

    public String getArtistName() {
      synchronized (this) {
            if(!isOnlinePlay){
                if (mCursor == null || mCursor.isClosed()||mCursor.getCount() == 0) {
                    return null;
                }
                try {
                    return mCursor.getString(mCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                } catch (Exception e) {
                    return null;
                }
            }else{           	
            	if(null == artistName_online) {
					artistName_online = Lewa.getPlayingSongArtist();	
            	}
                return artistName_online;
            }
        }
    }

    public long getArtistId() {
        synchronized (this) {
            if (mCursor == null || mCursor.isClosed()||mCursor.getCount() == 0) {
                return -1;
            }
            return mCursor.getLong(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            if(!isOnlinePlay){
                if (mCursor == null ||mCursor.isClosed()|| mCursor.getCount() == 0) {
                    return null;
                }
                return mCursor.getString(mCursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            }else{
                return albumName_online;
            }
        }
    }

    public long getAlbumId() {
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return -1;
            }
            return mCursor.getLong(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }
    }

    public String getTrackName() {
       synchronized (this) {
            if(!isOnlinePlay){
                if (mCursor == null || mCursor.isClosed()||mCursor.getCount() == 0) {
                    return null;
                }
                try {
                    return mCursor.getString(mCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                } catch (Exception e) {
                    return null;
                }
            }else{
            	
            	if(null == trackName_online) {
					trackName_online = Lewa.getPlayingSongName();					
            	}
                return trackName_online;
            }
        }
    }

    public String[] getTrackNameNext() {
        String[] TrackNameNext = new String[4];
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return null;
            }
            if (mCursor.getCount() >= 4) {
                for (int i = 0; i < 4; i++)
                    TrackNameNext[i] = mCursor
                            .getString(mCursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                mCursor.moveToNext();

            }
        }
        return TrackNameNext;

    }

    private boolean isPodcast() {
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return false;
            }
            return (mCursor.getInt(PODCASTCOLIDX) > 0);
        }
    }

    private long getBookmark() {
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return 0;
            }
            return mCursor.getLong(BOOKMARKCOLIDX);
        }
    }

    /**
     * Returns the duration of the file in milliseconds. Currently this method
     * returns -1 for the duration of MIDI files.
     */
    public long duration() {
        if (mPlayer!=null&&mPlayer.isInitialized()) {
            return mPlayer.duration();
        }
        return -1;
    }


    /**
     * Returns the current playback position in milliseconds
     */
    public long position() {
        if (mPlayer!=null&&mPlayer.isInitialized()) {
            return mPlayer.position();
        }
        return -1;
    }

    /**
     * Seeks to the position specified.
     *
     * @param pos
     *            The position to seek to, in milliseconds
     */
    public long seek(long pos) {
        if (mPlayer!=null&&mPlayer.isInitialized()) {
            if (pos < 0)
                pos = 0;
            if (pos > mPlayer.duration())
                pos = mPlayer.duration();
            return mPlayer.seek(pos);
        }
        return -1;
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId
     *            : the audio session ID.
     */
    public void setAudioSessionId(int sessionId) {
        synchronized (this) {
            mPlayer.setAudioSessionId(sessionId);
        }
    }

    /**
     * Returns the audio session ID.
     */
    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    private final BroadcastReceiver preferenceUpdateReceiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            levels = new short[5];
            String[] eqResult = (intent.getStringExtra("levles")).split(";");            
            for (int i = 0; i < 5; i++) {
                if (eqResult.length == 5) {
                    levels[i] = (short) (Float.valueOf(eqResult[i]) * 1);
                } else {
                    levels[i] = 0;
                }
            }
            updateDsp();
        }
    };

    protected void updateDsp() {
        try {
            if(mEqualizer!=null){
                mEqualizer.setEnabled(true);
                int len = levels.length;

                for (short i = 0; i < len; i++) {
                    mEqualizer.setBandLevel(i, levels[i]);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public long getDownProcess() {
        // TODO Auto-generated method stub
        return mPlayer.downloadprocess();
    }

    public int getBufferPercent(){
        return mPlayer.getBufferPercent();
    }

    /*
     * MultiPlayer
     *//**
     * Provides a unified interface for dealing with midi files and other
     * media files.
     */
    /*
     * private class MultiPlayer { private MediaPlayer mMediaPlayer = new
     * MediaPlayer(); private Handler mHandler; private boolean mIsInitialized =
     * false;
     *
     * public MultiPlayer() {
     * mMediaPlayer.setWakeMode(MediaPlaybackService.this,
     * PowerManager.PARTIAL_WAKE_LOCK); }
     *
     * public void setDataSource(String path) { try { mMediaPlayer.reset();
     * mMediaPlayer.setOnPreparedListener(null); if
     * (path.startsWith("content://")) {
     * mMediaPlayer.setDataSource(MediaPlaybackService.this, Uri.parse(path)); }
     * else { mMediaPlayer.setDataSource(path); }
     * mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
     * mMediaPlayer.prepare(); } catch (IOException ex) { // TODO: notify the
     * user why the file couldn't be opened mIsInitialized = false; return; }
     * catch (IllegalArgumentException ex) { // TODO: notify the user why the
     * file couldn't be opened mIsInitialized = false; return; }
     * mMediaPlayer.setOnCompletionListener(listener);
     * mMediaPlayer.setOnErrorListener(errorListener); Intent i = new
     * Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
     * i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
     * i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
     * sendBroadcast(i); mIsInitialized = true; }
     *
     * public boolean isInitialized() { return mIsInitialized; }
     *
     * public void start() { //MusicUtils.debugLog(new
     * Exception("MultiPlayer.start called")); mMediaPlayer.start(); }
     *
     * public void stop() { mMediaPlayer.reset(); mIsInitialized = false; }
     *//**
     * You CANNOT use this player anymore after calling release()
     */
    /*
     * public void release() { stop(); mMediaPlayer.release(); }
     *
     * public void pause() { mMediaPlayer.pause(); }
     *
     * public void setHandler(Handler handler) { mHandler = handler; }
     *
     * MediaPlayer.OnCompletionListener listener = new
     * MediaPlayer.OnCompletionListener() { public void onCompletion(MediaPlayer
     * mp) { // Acquire a temporary wakelock, since when we return from // this
     * callback the MediaPlayer will release its wakelock // and allow the
     * device to go to sleep. // This temporary wakelock is released when the
     * RELEASE_WAKELOCK // message is processed, but just in case, put a timeout
     * on it. mWakeLock.acquire(30000); mHandler.sendEmptyMessage(TRACK_ENDED);
     * mHandler.sendEmptyMessage(RELEASE_WAKELOCK); } };
     *
     * MediaPlayer.OnErrorListener errorListener = new
     * MediaPlayer.OnErrorListener() { public boolean onError(MediaPlayer mp,
     * int what, int extra) { switch (what) { case
     * MediaPlayer.MEDIA_ERROR_SERVER_DIED: mIsInitialized = false;
     * mMediaPlayer.release(); // Creating a new MediaPlayer and settings its
     * wakemode does not // require the media service, so it's OK to do this
     * now, while the // service is still being restarted mMediaPlayer = new
     * MediaPlayer(); mMediaPlayer.setWakeMode(MediaPlaybackService.this,
     * PowerManager.PARTIAL_WAKE_LOCK);
     * mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
     * return true; default: Log.d("MultiPlayer", "Error: " + what + "," +
     * extra); break; } return false; } };
     *
     * public long duration() { return mMediaPlayer.getDuration(); }
     *
     * public long position() { return mMediaPlayer.getCurrentPosition(); }
     *
     * public long seek(long whereto) { mMediaPlayer.seekTo((int) whereto);
     * return whereto; }
     *
     * public void setVolume(float vol) { mMediaPlayer.setVolume(vol, vol); }
     *
     * public void setAudioSessionId(int sessionId) {
     * mMediaPlayer.setAudioSessionId(sessionId); }
     *
     * public int getAudioSessionId() { return mMediaPlayer.getAudioSessionId();
     * } }
     */

    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still has
     * a remote reference to the stub.
     */
    static class ServiceStub extends IMediaPlaybackService.Stub {
        WeakReference<MediaPlaybackService> mService;

        ServiceStub(MediaPlaybackService service) {
            mService = new WeakReference<MediaPlaybackService>(service);
        }

        public void openFile(String path) {
            if (mService.get() == null) {
                return;
            }
            mService.get().open(path);
        }

        public void open(long[] list, int position) {
            if (mService.get() == null) {
                return;
            }
            mService.get().open(list, position);
        }

        public int getQueuePosition() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getQueuePosition();
        }

        public void setQueuePosition(int index) {
            if (mService.get() == null) {
                return;
            }
            mService.get().setQueuePosition(index);
        }

        public boolean isPlaying() {
            if (mService.get() == null) {
                return false;
            }
            return mService.get().isPlaying();
        }

        public void stop() {
            if (mService.get() == null) {
                return;
            }
            mService.get().stop();
        }

        public void pause() {
            if (mService.get() == null) {
                return;
            }
            mService.get().pause();
        }

        public void play() {
            if (mService.get() == null) {
                return;
            }
            mService.get().play();
        }

        public void prev() {
            if (mService.get() == null) {
                return;
            }
            mService.get().prev();
        }

        public void next() {
            if (mService.get() == null) {
                return;
            }
            mService.get().next(true);
        }

        public String getTrackName() {
            if (mService.get() == null) {
                return "";
            }
            return mService.get().getTrackName();
        }

        public String getAlbumName() {
            if (mService.get() == null) {
                return "";
            }
            return mService.get().getAlbumName();
        }

        public long getAlbumId() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getAlbumId();
        }

        public String getArtistName() {
            if (mService.get() == null) {
                return "";
            }
            return mService.get().getArtistName();
        }

        public long getArtistId() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getArtistId();
        }

        public void enqueue(long[] list, int action) {
            if (mService.get() == null) {
                return;
            }
            mService.get().enqueue(list, action);
        }

        public long[] getQueue() {
            if (mService.get() == null) {
                return null;
            }
            return mService.get().getQueue();
        }

        public void moveQueueItem(int from, int to) {
            if (mService.get() == null) {
                return;
            }
            mService.get().moveQueueItem(from, to);
        }

        public String getPath() {
            if (mService.get() == null) {
                return "";
            }
            return mService.get().getPath();
        }

        public long getAudioId() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getAudioId();
        }

        public long position() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().position();
        }

        public long duration() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().duration();
        }

        public long seek(long pos) {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().seek(pos);
        }

        public void setShuffleMode(int shufflemode) {
            if (mService.get() == null) {
                return;
            }
            mService.get().setShuffleMode(shufflemode);
        }

        public int getShuffleMode() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getShuffleMode();
        }

        public int removeTracks(int first, int last) {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().removeTracks(first, last);
        }

        public int removeTrack(long id) {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().removeTrack(id);
        }

        public void setRepeatMode(int repeatmode) {
            if (mService.get() == null) {
                return;
            }
            mService.get().setRepeatMode(repeatmode);
        }

        public int getRepeatMode() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getRepeatMode();
        }

        public int getMediaMountedCount() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getMediaMountedCount();
        }

        public int getAudioSessionId() {
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getAudioSessionId();
        }
        public long getDownProcess(){
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getDownProcess();
        }

        public int getBufferPercent(){
            if (mService.get() == null) {
                return -1;
            }
            return mService.get().getBufferPercent();
        }

        public void stopStreamPlayer() {
            if (mService.get() == null) {
                return;
            }
            mService.get().stopStreamPlayer();
        }

        public void removeStatusBar(){
            if (mService.get() == null) {
                return;
            }
            mService.get().removeStatusBar();
        }
        
        public void updateNotification(){
        	 if (mService.get() == null) {
                 return;
             }
             mService.get().updateNotification();
        }
    }


    private final IBinder mBinder = new ServiceStub(this);
    private int statusIconWidth;

    public void stopStreamPlayer() {
        // TODO Auto-generated method stub
        if(mPlayer!=null)
            mPlayer.stopStreamPlayer();
    }

    public void removeStatusBar() {
        // TODO Auto-generated method stub
        if(notificationManager!=null)
            notificationManager.cancel(PLAYBACKSERVICE_STATUS);
    }


    private void requestScanFile(Context context, String file, long musicId) {
        MediaScannerConnection.scanFile(context, new String[]{file},
                null, // mime types
                new ScanCompletedListener(musicId));
    }

    class ScanCompletedListener implements MediaScannerConnection.OnScanCompletedListener {
        long musicId = 0;
        public ScanCompletedListener(long musicId) {
            this.musicId = Math.abs(musicId) * -1;
            
        }
        
        public void onScanCompleted(String path, Uri uri) {

            notifyChange(META_CHANGED);
            //Log.i(TAG, "Scan finished, check and match download songs.");

            if(uri != null) {
                String localId = uri.getLastPathSegment();
                LewaUtils.logE(TAG, "path: "+path+" localId: "+localId);
                DBService.downloadDone(path, localId);
                long id = Long.parseLong(localId);
                int size = mPlayList.length;
                //Log.i(TAG, "musicId = " + musicId);
                for(int i = 0; i <  size; i++) {
                    //Log.i(TAG, "mPlayList[ " + i + "]" + mPlayList[i]);
                    if(mPlayList[i] == musicId) {
                        mPlayList[i] = id;
                    }
                }
            }

            DBService.matchDownload();

            
            Intent intent = new Intent(MediaPlaybackService.SONG_DOWNLOADED);
            Song localSong = DBService.findSongByPathFromMediaStore(path);
            intent.putExtra("localId", localSong.getId());
            sendBroadcast(intent);

        }
    };

    /*MediaScannerConnection.OnScanCompletedListener mListener = 
            new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    {
                        notifyChange(META_CHANGED);
                        Log.i(TAG, "Scan finished, check and match download songs.");

                        if(uri != null) {
                            String localId = uri.getLastPathSegment();
                            LewaUtils.logE(TAG, "path: "+path+" localId: "+localId);
                            DBService.downloadDone(path, localId);
                        }

                        DBService.matchDownload();
                        Intent intent = new Intent(MediaPlaybackService.SONG_DOWNLOADED);
						Song localSong = DBService.findSongByPathFromMediaStore(path);
						intent.putExtra("localId", localSong.getId());
                        sendBroadcast(intent);
                    }
                }
            };*/
}
