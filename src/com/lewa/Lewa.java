package com.lewa;


import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.baidu.music.SDKEngine;
import com.baidu.music.SDKInterface;
import com.baidu.music.download.DownloadEntry;
import com.baidu.music.download.DownloadStatus;
import com.baidu.music.image.component.ImageManager2;
import com.baidu.music.manager.ImageManager;
import com.baidu.music.manager.JobManager;
import com.baidu.music.oauth.OAuthInterface;
import com.baidu.music.oauth.OAuthManager;
import com.baidu.music.oauth.OAuthInterface.onAuthorizeFinishListener;
import com.baidu.utils.LogUtil;
import com.lewa.kit.BitmapLruCache;
import com.lewa.kit.MyVolley;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.activity.LibraryActivity;
import com.lewa.player.db.DBService;
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.Artist;
import com.lewa.player.model.MusicDownloadStatus;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.player.online.AppDownloadManager;
import com.lewa.player.online.DownLoadAsync;
import com.lewa.player.online.DownLoadMusicManager;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.service.ConnectionListener;
import com.lewa.player.service.PlayerServiceConnector;
import com.lewa.util.Blur;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;
import com.lewa.util.NetUtils;
import com.lewa.util.StringUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.lewa.player.widget.WidgetUtils;
import com.lewa.util.BlurRunnable;
import com.lewa.player.helper.MusicFilesObserver;
import com.lewa.player.SleepModeManager;

//import java.util.concurrent.Executors;
import com.lewa.themes.ThemeClientApplication;
public class Lewa extends ThemeClientApplication{
    private static final String TAG = "Lewa";
    //public Messenger mService = null;
    public static final int DEFAULT_RADIUS = 30;//1;//5;//3; 
    public static boolean isShowedToast = false;

    private static String PERSIST_NAME = "lewa.persist";
    private static Context context;
    private static Resources res;
    private static LayoutInflater inflater;
    private static PlayerServiceConnector sPlayerServiceConnector;
    private final static PlayStatus playStatus = new PlayStatus();
    private static Song playingSong = null;
    private static Set<PlayStatusListener> sPlayStatusListeners = new HashSet<PlayStatusListener>();
    private static Set<Bitmap> sBitmapSet = new HashSet<Bitmap>();
    private static Map<Long, Song> sPlayingOnlineSongs = new HashMap<Long, Song>();
    private static SongCollection sPlayingCollection = null;
    private final static int BLUR_BACKGROUND_READY = 1;
    private static BitmapLruCache sBitmapCache = null;
    private static LruCache<String, Boolean> sEmptyBitmapCache = null;

    private static SharedPreferences sSettingsSp;
    private static boolean isTokenCertified = false;
    private AudioManager mAudioManager;
    private static AppDownloadManager appDownloadManager;
    private static String CACHEDIR="/music/cache";
    private static int IMAGECACHESIZE=5*1024*1024;  //5MB
    private static HashMap<Long, Song> downloadingSongs=new HashMap<Long, Song>();
    private static HashMap<Long, Song> stopDownloadSongs=new HashMap<Long, Song>();//stopDownloadSongs contains STATUS_RUNNING_PAUSED and STATUS_HTTP_DATA_ERROR
    private static HashMap<Long, Song> pausedDownloadSongs=new HashMap<Long, Song>();
    private static Handler handler =new Handler();

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        res = context.getResources();
        sPlayerServiceConnector = new PlayerServiceConnector(this);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sSettingsSp = getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        appDownloadManager=AppDownloadManager.getInstance(context);

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "onCreate e = " + e);
        }
        
        SleepModeManager.deleteSleepTime(context);
    }
    
    

    public void init() {
        DBService.init();

        //int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();   //not use
        int cacheSize = IMAGECACHESIZE;
        sBitmapCache = new BitmapLruCache(cacheSize);
        sEmptyBitmapCache = new LruCache<String, Boolean>(500);
        MyVolley.init(context, sBitmapCache);
        sPlayerServiceConnector.connectPlayer(null);
        
        ImageManager.init(context, ImageManager.POSTFIX_JPG, LewaUtils.getExternalPath(CACHEDIR), IMAGECACHESIZE);
        //JobManager.getInstance(); //not use

        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        f.addAction(MediaPlaybackService.META_CHANGED);
        f.addAction(MediaPlaybackService.SONG_DOWNLOADED);
        f.addAction(OnlineLoader.UPDATEBG);
        f.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mStatusReceiver, f);

        MusicFilesObserver.getInstance(context).startWatching();
    }

    
    private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Long songId = intent.getLongExtra("id", 0);
            int sessionId = intent.getIntExtra("session", -1);
            
            if (action.equals(MediaPlaybackService.META_CHANGED)) {
            	Log.i(TAG, "META_CHANGED SESSION ID IS " + sessionId);
                sPlayerServiceConnector.connectPlayer(new ConnectionListener() {
                    @Override
                    public void connected(IMediaPlaybackService service) {
                    	updatePlayStatus(service);
                        try {
                            playStatus.setPlaying(service.isPlaying());

                            if (songId >= 0) {
                                Log.i(TAG, "Playing local song: " + songId);
                                try {
                                    playingSong = DBService.findSongById(songId);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                if (playingSong != null) {
                                    playingSong.setType(Song.TYPE.LOCAL);
                                }

//                                playingSong = new Song(service.getAudioId());
//                                playingSong.setType(Song.TYPE.LOCAL);
//                                String songName = service.getTrackName();
//                                playingSong.setName(songName == null ? "" : songName);
//                                playingSong.setPath(service.getPath());
//                                playingSong.setArtist(new Artist(service.getArtistId(), service.getArtistName()));
//                                playingSong.setAlbum(new Album(service.getAlbumId(), service.getAlbumName()));
                            } else {
                                Log.i(TAG, "Playing online song: " + songId);
                                playingSong = sPlayingOnlineSongs.get(songId);

                                if (playingSong != null) {
                                    playingSong.setType(Song.TYPE.ONLINE);
                                    playingSong.setDuration(service.duration());
                                    Log.i(TAG, "Song duration: " + playingSong.getDuration());
                                }
                            }
                            playStatus.setPlayingSong(playingSong);

                            if (playingSong != null) {
                                Log.i(TAG, "Playing song: " + playingSong.toString());
                                String coverUrl = playingSong.getBigCoverUrl();

//                                if (StringUtils.isBlank(coverUrl)) {
//                                    coverUrl = playingSong.getCoverUrl();
//                                }
//
//                                if (!StringUtils.isBlank(coverUrl)) {
//                                    getAndBlurImage(coverUrl, null);
//                                } else {
                                Artist artist = playingSong.getArtist();

                                if (artist != null) {
                                    loadArtistAvatar(artist.getName(), null);
//                                    OnlineLoader.getArtistImg(artist.getName(), 1, 0, 0);
                                } else {
                                    loadArtistAvatar(null, null);
                                }

//                                }
                            } else {
                                Log.i(TAG, "Playing song is null.");
                            }

                            DBService.logRecentPlay(playingSong);
                            DBService.logPlayHistory(Lewa.getPlayingCollection(), playingSong);

                            playStatus.setRepeatMode(service.getRepeatMode());
                            playStatus.setShuffleMode(service.getShuffleMode());

                            for (PlayStatusListener playStatusListener : sPlayStatusListeners) {
                                playStatusListener.onPlayStatusChanged(playStatus);
                            }
                            Log.i(TAG, "Play service meta changed.");
                            service.updateNotification();
                            
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
                sPlayerServiceConnector.connectPlayer(new ConnectionListener() {
                    @Override
                    public void connected(IMediaPlaybackService service) {
                    	updatePlayStatus(service);
                        try {
                            playStatus.setPlaying(service.isPlaying());

                            Song song = new Song();
                            song.setId(service.getAudioId());
                            song.setName(service.getTrackName());
                            song.setArtist(new Artist(service.getArtistId(), service.getArtistName()));
                            song.setDuration(service.duration());

                            if(!MediaPlaybackService.isOnlinePlay) {
                                song.setType(Song.TYPE.LOCAL);
                            } else {
                                song.setType(Song.TYPE.ONLINE);
                            }
                            
                            if (playingSong == null) {
                                playingSong = song;
                                playStatus.setPlayingSong(playingSong);
                            }

                            for (PlayStatusListener playStatusListener : sPlayStatusListeners) {
                                playStatusListener.onPlayStatusChanged(playStatus);
                            }
                            Log.i(TAG, "Play status changed.");
                            service.updateNotification();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (action.equals(OnlineLoader.UPDATEBG)) {
                String path = intent.getStringExtra(Constants.PATH);
                Bitmap bitmap = decodeImage(path);

                if (bitmap != null) {
                    sEmptyBitmapCache.remove(getOrignLocalImageCacheKey(path));
                    onOriginImageReady(null, bitmap);

                    startBlur(bitmap, null, null);
                }
//                getAndBlurImage(path, null);
            } else if(action.equals(MediaPlaybackService.SONG_DOWNLOADED)) {
                if(sPlayStatusListeners != null) {
                    for (PlayStatusListener playStatusListener : sPlayStatusListeners) {
                        Long onlineId = intent.getLongExtra("onlineId", -1);
                        Long localId = intent.getLongExtra("localId", -1);
                        playStatusListener.onSongDownloaded(onlineId, localId);
                    }
                }
            }else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            	if(downloadingSongs.size()>0&&!NetUtils.isWiFiActive(context)){
            		pauseAllDownload();
            	}else if(downloadingSongs.size()>0&&NetUtils.isWiFiActive(context)){
            		resumeAllDownload();
            	}
            }
        }
    };

    private void updatePlayStatus(IMediaPlaybackService service){
    	Intent intent = new Intent("com.lewa.tuningmaster.PLAY_REQUEST_STATUS");
    	boolean isPlaying = false;
    	int ssesionId = -1;
    	Log.i(TAG, "RUI updatePlayStatus");
    	try{
    		isPlaying = service.isPlaying();
	    	
	    	if (isPlaying) {
	    		Log.i(TAG, "WEI PLAYING");
	    		ssesionId = service.getAudioSessionId();
				Log.i(TAG, "WEI SESSION ID IS " + ssesionId);
			} else {
				Log.i(TAG, "WEI PAUSED");
			}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	intent.putExtra("play_status", isPlaying);
    	intent.putExtra("session_id", ssesionId);
    	sendBroadcast(intent);
    }
    
    public static Lewa context() {
        return (Lewa) context;
    }

    public static Resources resources() {
        return res;
    }

    /**
     * use artist avatar instead currently, should be replaced with corresponding images returned
     * from baidu when the new api is applied.
     *
     * @param playStatusListener
     */
    public static void getAndBlurCurrentCoverUrl(PlayStatusListener playStatusListener) {
        BlurRunnable.blurDefaultBg(res, playStatusListener);
        if (playStatus == null || playStatus.getPlayingSong() == null) {
            return;
        }

        if (playingSong != null) {
            Artist artist = playingSong.getArtist();

            if (artist != null) {
                loadArtistAvatar(artist.getName(), playStatusListener);
                return;
            }
        }

        onStartGetImage(playStatusListener);
    }

    public static Bitmap getLocalImage(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }

        String key = getOrignLocalImageCacheKey(path);

        Bitmap cachedBitmap = sBitmapCache.getBitmap(key);
        if (cachedBitmap != null) {
            return cachedBitmap;
        } else {
            if (sEmptyBitmapCache.get(key) != null) {
                return null;
            }

            Bitmap bitmap = decodeImage(path);

            if (bitmap != null) {
                sBitmapCache.putBitmap(key, bitmap);
            } else {
                sEmptyBitmapCache.put(key, true);
                return null;
            }

            return bitmap;
        }
    }

    private static Bitmap decodeImage(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
		//modify config reduce used memory for bug 65410 
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static void loadArtistAvatar(String artistName, PlayStatusListener playStatusListener) {
        if (StringUtils.isBlank(artistName)) {
            onStartGetImage(playStatusListener);
            return;
        }

        String path = LewaUtils.getArtistPicPath(artistName);
        
        Bitmap cachedBlurBitmap = sBitmapCache.getBitmap(getBlurImageCacheKey(path));
        if (cachedBlurBitmap != null) {
            //do not return here, coz some view need original image
            onBlurImageReady(playStatusListener, cachedBlurBitmap);
        } else {
        	//pr 937782 add by wjhu
    		//to set default cover when there is no bitmap get
			onBlurImageReady(playStatusListener, null);
		}

        Bitmap localImage = getLocalImage(path);
//add by sjxu START for BUG 48773
		if(null == localImage) {
			String name = WidgetUtils.getAtristName(artistName);
			path = LewaUtils.getArtistPicPath(name);
			localImage = getLocalImage(path);
		}
//add by sjxu END for BUG 48773		
        if (localImage != null) {
            onOriginImageReady(playStatusListener, localImage);
            startBlur(localImage, null, playStatusListener);
            return;
        }

        onStartGetImage(playStatusListener);
        if(playingSong!=null)
        	OnlineLoader.getMusicImg(playingSong.getName(), artistName);
        return;
    }

    private static void onStartGetImage(PlayStatusListener playStatusListener) {
        if (playStatusListener != null) {
            playStatusListener.onStartGetBackground();
        } else {
            for (PlayStatusListener statusListener : sPlayStatusListeners) {
                statusListener.onStartGetBackground();
            }
        }
    }

    private static void onOriginImageReady(PlayStatusListener playStatusListener, Bitmap bitmap) {
        if (playStatusListener != null) {
            playStatusListener.onBackgroundReady(bitmap);
        } else {
            for (PlayStatusListener statusListener : sPlayStatusListeners) {
                statusListener.onBackgroundReady(bitmap);
            }
        }
    }

    private static void onBlurImageReady(PlayStatusListener playStatusListener, Bitmap bitmap) {
        if (playStatusListener != null) {
            playStatusListener.onBluredBackgroundReady(bitmap);
        } else {
            for (PlayStatusListener statusListener : sPlayStatusListeners) {
                statusListener.onBluredBackgroundReady(bitmap);
            }
        }
    }

    /**
     * been replaced by loadArtistAvatar(), coz only artist avatar is used as background currently.
     *
     * @param path
     * @param artistName
     * @param playStatusListener
     */
    public static void getAndBlurImage(final String path, final String artistName, PlayStatusListener playStatusListener) {
        if (StringUtils.isBlank(path)) {
            return;
        }

        //do not return here, coz some view need original image
        Bitmap cachedBitmap = sBitmapCache.getBitmap(getBlurImageCacheKey(path));
        if (cachedBitmap != null) {
            if (playStatusListener != null) {
                playStatusListener.onBluredBackgroundReady(cachedBitmap);
            } else {
                for (PlayStatusListener statusListener : sPlayStatusListeners) {
                    statusListener.onBluredBackgroundReady(cachedBitmap);
                }
            }
        }

        if (!path.startsWith("http")) {
            Bitmap bitmap = getLocalImage(path);

            if (bitmap != null) {
                for (PlayStatusListener statusListener : sPlayStatusListeners) {
                    statusListener.onBackgroundReady(bitmap);
                }
//                    startBlur(bitmap, url);
                return;
            }
        }

        for (PlayStatusListener statusListener : sPlayStatusListeners) {
            statusListener.onStartGetBackground();
        }

        if (path.startsWith("http")) {

            MyVolley.getImageLoader().get(path, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bitmap = response.getBitmap();

                    if (bitmap != null) {
                        Lewa.holdBitmap(bitmap);
                        for (PlayStatusListener statusListener : sPlayStatusListeners) {
                            statusListener.onBackgroundReady(bitmap);
                        }
//                    startBlur(bitmap, url);
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        } else {
            if (playingSong != null) {
                Artist artist = playingSong.getArtist();
                String title=playingSong.getName();
                if (artist != null) {
                    OnlineLoader.getMusicImg(title, artistName);
                    return;
                }
            }

            for (PlayStatusListener statusListener : sPlayStatusListeners) {
                statusListener.onStartGetBackground();
            }
        }
    }

    private static String getBlurImageCacheKey(String url) {
        return new StringBuilder(url.length() + 12).append("#blur#").append(url).toString();
    }

    private static String getOrignLocalImageCacheKey(String path) {
        return new StringBuilder(path.length() + 12).append("#origin#").append(path).toString();
    }

    public static void startBlur(final Bitmap bitmap, final String url, PlayStatusListener playStatusListener) {
        Log.i(TAG, "Start blur image.");

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        //Bitmap bluredBm = Blur.createBlurBitmap(bitmap, w, h, 10);
        Bitmap bluredBm = Blur.createBlurBitmap(bitmap, w, h, DEFAULT_RADIUS);
        onBlurImageReady(playStatusListener, bluredBm);
    }

    public static PlayerServiceConnector playerServiceConnector() {
        return sPlayerServiceConnector;
    }

    public static PlayStatus getPlayStatus() {
        return playStatus;
    }

    public static Song getPlayingSong() {
        return playingSong;
    }

    public static void setPlayingOnlineSongs(Set<Song> playingSongs) {
        sPlayingOnlineSongs.clear();

        for (Song song : playingSongs) {
            sPlayingOnlineSongs.put(Math.abs(song.getId()) * -1, song);
        }
    }

    public static Song getPlayingSong(Long id) {
        return sPlayingOnlineSongs.get(Math.abs(id) * -1);
    }

    public static void registerPlayStatusListener(PlayStatusListener playStatusListener) {
        if (playStatusListener != null) {
            sPlayStatusListeners.add(playStatusListener);
        }
    }

    public static void unregisterPlayStatusListener(PlayStatusListener playStatusListener) {
        if (playStatusListener != null) {
            sPlayStatusListeners.remove(playStatusListener);
        }
    }

    public static SharedPreferences getPersistPreferences() {
        return context().getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getPreferences() {
        return context().getSharedPreferences("mt", Context.MODE_PRIVATE);
    }

    public static SharedPreferences getPreferences(String name) {
        return context().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static Animation getAnimation(int resId) {
        return AnimationUtils.loadAnimation(context, resId);
    }

    public static LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
    }

    public static String string(int strId) {
        return context().getString(strId);
    }

    public static String string(int strId, Object... args) {
        return context().getString(strId, args);
    }

    public static void holdBitmap(Bitmap bitmap) {
        sBitmapSet.add(bitmap);
    }

    public static void releaseBitmap(Bitmap bitmap) {
        Log.i(TAG, "Release bitmap.");
        sBitmapSet.remove(bitmap);
    }


    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
//            else if (action.equals(AudioManager.VOLUME_CHANGED_ACTION)) {
//                if (mAudioManager == null)
//                    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                if (mAudioManager.isWiredHeadsetOn() && MediaPlaybackService.mIsSupposedToBePlaying) {
//                    int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                    int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//                    NumberFormat numberFormat = NumberFormat.getInstance();
//                    numberFormat.setMaximumFractionDigits(2);
//                    String result = numberFormat.format((float) volume / (float) maxVolume);
//                    if (Float.parseFloat(result) > 0.7) {
//                        //by Fanzhong
//                        Handler mHandler = new Handler();
//                        //mToast.show();
//                        mHandler.postDelayed(new Runnable() {
//                            public void run() {
//                                //mToast.cancel();
//                            }
//                        }, 1000);
//                        //END
//                    }
//
//                }
//
//
//            }
        }
    };

	


    public static int getIntSetting(String key) {
        return sSettingsSp.getInt(key, 0);
    }

    public static int getIntSetting(String key, int defaultValue) {
        return sSettingsSp.getInt(key, defaultValue);
    }

    public static void saveIntSetting(String key, int value) {
        Editor editor = sSettingsSp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setPlayingCollection(SongCollection songCollection) {
        sPlayingCollection = songCollection;
    }

    public static SongCollection getPlayingCollection() {
        return sPlayingCollection;
    }

    public static void downloadSong(Song song) {
    	if(!NetUtils.isNetworkValid(context)){
    		/*Added by ruiwei, for Modifying Toast style, 20150211, start*/
            Context activity = ExitApplication.getTopActivity();
            if(null == activity) {
            	activity = context;
            }
    		Toast.makeText(activity, R.string.no_network, 0).show();
    		/*Added by ruiwei, for Modifying Toast style, 20150211, end*/
    		return;
    	}
        try {
        	LewaUtils.logE(TAG, "downloadSong path: "+song.getPath()+" bitrate: "+song.getBitrate());
        	MusicDownloadStatus downStatus=new MusicDownloadStatus();
        	downStatus.setStatus(DownloadStatus.STATUS_PENDING);
        	song.setDownStatus(downStatus);
    		DBService.saveDownloadingSong(song.getId());
            DBService.saveSong(song);
            putDownloadSong(song);
//            downLoadMusicManager.downMusic(song.getId().intValue(), 0);
            appDownloadManager.addDownload(song.getId(), song.getBitrate(), song.isLossless());
            if(song.getName()!=null){
            	String message = context.getString(R.string.add_to_download, song.getName());
            	showToast(message);
            }
            Constants.show_down_tip=true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void delDownload(Song song){
    	appDownloadManager.delDownload(song.getId(),song.getBitrate(), song.isLossless());
    }
    
    public static void pauseAllDownload(){
    	try {
			synchronized (downloadingSongs) {
				AppDownloadManager appDownloadManager = AppDownloadManager.getInstance(context);
				for(Entry<Long, Song> entry:downloadingSongs.entrySet()){
					Song song = entry.getValue();
					DownloadEntry downloadEntry = appDownloadManager.getDownloadEntryInfo(song);
					if(downloadEntry!=null&&downloadEntry.getDownloadStatus() == DownloadStatus.STATUS_RUNNING){
						appDownloadManager.pauseDownload(song);
					}else{
						addStopDownloadSongs(song);
						Song onlineSong = DBService.findSongById(song.getId(), Song.TYPE.ONLINE);
					    onlineSong.setDownloadStatus(Constants.DOWNLOAD_STATUS_STOP);
					    DBService.saveSong(onlineSong);
					}
				}
				updateNotification(context, buildNotifyTitle(), getDownloadName(), true);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void resumeAllDownload(){
    	synchronized (downloadingSongs) {
    		AppDownloadManager appDownloadManager = AppDownloadManager.getInstance(context);
    		for(Entry<Long, Song> entry:downloadingSongs.entrySet()){
    			Song song = entry.getValue();
    			DownloadEntry downloadEntry = appDownloadManager.getDownloadEntryInfo(song);
    			if(downloadEntry!=null&&downloadEntry.getDownloadStatus() == DownloadStatus.STATUS_RUNNING_PAUSED){
    				appDownloadManager.resumeDownload(song);
    			}else {
    				appDownloadManager.addDownload(song.getId(), song.getBitrate(), song.isLossless());
    				removeStopDownloadSongs(song);
    			}
    		}
    		if(downloadingSongs.size()>0)
    			updateNotification(context, buildNotifyTitle(), getDownloadName(), false);
		}
    }
    
    public static void resumeDownload(Song song){
    	removeStopDownloadSongs(song);
		removePausedDownloadSongs(song);
		putDownloadSong(song);
    }
    
    public static void pauseDownload(Song song){
    	addStopDownloadSongs(song);
		addPausedDownloadSongs(song);
		updateNotify(Constants.DOWNLOAD_REMOVE_STATUS_PAUSE,song);
    }
    
    public static void removeDownloadSong(long musicId,int status){
    	synchronized (downloadingSongs) {
    		if(downloadingSongs.containsKey(musicId)){
        		Song song = downloadingSongs.get(musicId);
        		downloadingSongs.remove(musicId);
        		stopDownloadSongs.remove(musicId);
        		updateNotify(status, song);
        	}
		}
    }



	private static void updateNotify(int status, Song song) {
		int size=downloadingSongs.size();
		String s=getDefaultTitle(size);
		boolean isStatic = false;
		if(size>0){
			 s=Lewa.string(R.string.down_task_num,size).concat(s);
			 if(size == stopDownloadSongs.size()){
				 isStatic=true;
			 }
		}else{
			s=Lewa.string(R.string.download_done);
			if(status == Constants.DOWNLOAD_REMOVE_STATUS_SUCCESS){
				String name = song.getName();
				if(!TextUtils.isEmpty(name)){
					s = name.concat(s);
				}
			}
			isStatic=true;
		}
		updateNotification(context(),s,getDownloadName(),isStatic);
	}
    
    public static void putDownloadSong(Song song){
    	downloadingSongs.put(song.getId(), song);
    	String title = buildNotifyTitle();
    	updateNotification(context(),title,getDownloadName(),false);
    }



	private static String buildNotifyTitle() {
		int size = downloadingSongs.size();
		String title = getDefaultTitle(size);
    	if(size > 1){
    		title = Lewa.string(R.string.down_task_num, downloadingSongs.size()).concat(title);
    	}else if(size == 1){
    		for(Entry<Long,Song> entry : downloadingSongs.entrySet()){
    			Song song = entry.getValue();
    			if(song!=null){
	    			String name =song.getName();
	    			if(!TextUtils.isEmpty(name))
	    				title = name.concat(title);
    			}
    		}
    	}
		return title;
	}



	private static String getDefaultTitle(int size) {
		String title = null;
		LewaUtils.logE(TAG, "downloading size :"+size+"paused size: "+stopDownloadSongs.size());
		if(stopDownloadSongs.size() == size){
			title = Lewa.string(R.string.download_paused);
		}else{
			title = Lewa.string(R.string.downloading);
    	}
		return title;
	}
    
    public static HashMap<Long, Song> getDownloadingSong(){
    	return downloadingSongs;
    }
    
    public static void updateNotification(Context context,String title,String text,boolean isStatic){
    	 if(notificationManager==null)
    		 notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		 Notification.Builder builder=new Notification.Builder(context);
		 builder.setContentTitle(title);
		 if(!isStatic){
			 builder.setSmallIcon(R.drawable.stat_sys_download);
			 builder.setContentText(text);
		 }else{
			 builder.setSmallIcon(R.drawable.stat_sys_download_done_static);
			 builder.setContentText(string(R.string.open_download));
			 builder.setAutoCancel(true);
		 }
		 Intent intent=new Intent(context, LibraryActivity.class);
		 intent.putExtra("tag", "download");
		 PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intent, 0);
		 builder.setContentIntent(pendingIntent);
		 notificationManager.notify(NOTIFICATION_ID, builder.build());
	}
    
    public static void removeNotify(){
    	if(notificationManager==null)
   		 	notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	//notificationManager.cancel(NOTIFICATION_ID);
    	notificationManager.cancelAll();
    	LewaUtils.logE(TAG, "removeNotify");
    }
    
    private static void showToast(String msg){
    	/*Added by ruiwei, for Modifying Toast style, 20150211, start*/
        Context activity = ExitApplication.getTopActivity();
        Log.i("RUIWEI", "ACTIVITY IS " + activity);
        if(null == activity) {
        	activity = context;
        }
        
		final Toast toast=Toast.makeText(activity , msg, Toast.LENGTH_SHORT);
		/*Added by ruiwei, for Modifying Toast style, 20150211, end*/
		toast.setText(msg);
		toast.setDuration(0);
		toast.show();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				toast.cancel();
			}
		}, 1000);
	}
    
    private static String getDownloadName(){
    	StringBuilder builder = new StringBuilder();
    	Set<Entry<Long, Song>> sets = downloadingSongs.entrySet();
    	for(Entry<Long, Song> entry: sets){
    		Song song = entry.getValue();
    		String title = song.getName();
    		if(!TextUtils.isEmpty(title)){
    			if(builder.length()==0){
    				builder.append(title);
    			}else{
    				builder.append(",").append(title);
    			}
    		}
    	}
    	return builder.toString();
    }
    
    public static void setRepeatAndShuffleMode(int repeatMode,int shuffleMode){
    	playStatus.setRepeatMode(repeatMode);
    	playStatus.setShuffleMode(shuffleMode);
    }
    
    public static String getPlayingSongName(){
    	if(playStatus != null){
    		Song playingSong= playStatus.getPlayingSong();
    		if(playingSong != null)
    			return playingSong.getName();
    	}
    	return null;
    }
    
    public static String getPlayingSongArtist(){
    	if(playStatus != null){
    		Song playingSong= playStatus.getPlayingSong();
    		if(playingSong != null){
	    		Artist artist = playingSong.getArtist();
	    		if(artist != null)
	    			return artist.getName();
    		}
    	}
    	return null;
    }
    
    public static void addStopDownloadSongs(Song song){
    	if(song!=null)
    		stopDownloadSongs.put(song.getId(), song);
    }
    
    public static void removeStopDownloadSongs(Song song){
    	if(song!=null)
    		stopDownloadSongs.remove(song.getId());
    }
    
    public static void addPausedDownloadSongs(Song song){
    	if(song!=null)
    		pausedDownloadSongs.put(song.getId(), song);
    }
    
    public static void removePausedDownloadSongs(Song song){
    	if(song!=null)
    		pausedDownloadSongs.remove(song.getId());
    }
    
    public static HashMap<Long, Song> pausedDownloadSongs(){
    	return pausedDownloadSongs;
    }
    
    public static HashMap<Long, Song> stopDownloadSongs(){
    	return stopDownloadSongs;
    }
    
	private static final int NOTIFICATION_ID=110;
	private static NotificationManager notificationManager;
	public static int DOWN_PAUSE_NUM=0;
	public static int DOWN_FAIL_NUM=0;
}
