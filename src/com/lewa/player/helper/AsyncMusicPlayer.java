package com.lewa.player.helper;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.baidu.music.player.StreamPlayer;
import com.baidu.music.player.StreamPlayer.OnBufferingUpdateListener;
import com.baidu.music.player.StreamPlayer.OnCompletionListener;
import com.baidu.music.player.StreamPlayer.OnErrorListener;
import com.baidu.music.player.StreamPlayer.OnBlockListener;
import com.baidu.music.player.StreamPlayer.OnDownloadErrorListener;
import com.baidu.music.player.StreamPlayer.OnPreparedListener;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.util.LewaUtils;
import com.baidu.music.model.BaseObject;
import com.lewa.Lewa;
import android.os.Message;
import com.lewa.player.R;


import java.io.File;

public class AsyncMusicPlayer {
    private final static String TAG = "AsyncMusicPlayer";//.class.getName();

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private StreamPlayer mStreamPlayer;
    private int onlineMusicSessionId = -1;
    private Handler mHandler;
    private boolean mIsInitialized = false;
    private PowerManager.WakeLock mWakeLock;
    private Context mContext;
    public static String SAVE_PATH = Environment.getExternalStorageDirectory() + "/LEWA/music/mp3";
    private int bufferPercent;


    public AsyncMusicPlayer(Context context) {
        mContext = context;
        // mMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
    }

    private void acquireWakeLock() {
        if (this.mWakeLock == null)
            return;
        this.mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (this.mWakeLock == null)
            return;
        this.mWakeLock.release();
    }

    private String musicPath = null;
    public static final int FINISH_COMPLETE = 0x00;
    public static final int FINISH_ERROR = 0x01;

    public String getMusicPath() {
        return musicPath;
    }

    public void setDataSource(final String path) {
        Log.i(TAG, "setDataSource path = " + path);
        musicPath = path;
        boolean isLong = true;
        try {
            Long.parseLong(path);
        } catch (NumberFormatException e) {
            isLong = false;
        }
        
        if (!MediaPlaybackService.isOnlinePlay || !isLong) {
            try {
                if (mStreamPlayer != null) {
                    stopPlayOnlineMusic();
                    mStreamPlayer.release();
                    mStreamPlayer = null;
                }

                mMediaPlayer.reset();
                mMediaPlayer.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    mMediaPlayer.setDataSource(mContext, Uri.parse(path));
                } else {
                    mMediaPlayer.setDataSource(path);
                }
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
            } catch (Exception ex) {
                // TODO: notify the user why the file couldn't be opened
                mIsInitialized = false;
                return;
            }
            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.setOnErrorListener(errorListener);
/*	            Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
                i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
	            i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
	            sendBroadcast(i);*/
            mIsInitialized = true;
        } else {
            if (mStreamPlayer == null) {
                initStreamPlayer();
            }
            
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            
            mIsInitialized = true;
            if (isLong){
                if (mStreamPlayer == null) return;
                if (mStreamPlayer.isPlaying()) {
                    stopPlayOnlineMusic();
                }
            LewaUtils.logE(TAG, "Play online song *: " + path);
                try {
                    mStreamPlayer.prepare(Math.abs(Long.parseLong(path)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }

    private void initStreamPlayer() {
        OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
            @Override
            public void onPrepared() {
            	Log.i("VVV", "ONLINE PLAY, SESSION ID IS " + mStreamPlayer.getSessionId());
            	onlineMusicSessionId = mStreamPlayer.getSessionId();
            	if(!mStreamPlayer.isPlaying())
            		//mStreamPlayer.start();
            		
            		mHandler.sendEmptyMessage(MediaPlaybackService.PLAY_ONLINE_SONG);
            }
        };

        OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {

            @Override
            public void onBufferingUpdate(int percent) {
                // TODO Auto-generated method stub
                bufferPercent = percent;
            }


            @Override
            public void onBufferingEnd() {
                // TODO Auto-generated method stub
                bufferPercent = 100;

            }
        };

        OnErrorListener mErrorListener = new OnErrorListener() {

            @Override
            public boolean onError(int arg0) {
                
                LewaUtils.logE(TAG, "streamplayer error code : "+arg0);


                Message msg = mHandler.obtainMessage(MediaPlaybackService.ERROR_HINT);
                switch(arg0) {
                    case BaseObject.ERROR_NETWORK_UNAVAILABLE:
                        msg.obj = Lewa.string(R.string.no_network_text);                        
                        mHandler.sendEmptyMessage(MediaPlaybackService.ERROR_NETWORK);
                        break;
                    case BaseObject.ERROR_FAV_FAILED:
                        msg.obj = Lewa.string(R.string.no_copyright_text);
                    default:
                        playFinish(FINISH_ERROR);
                        break;

                }
                msg.sendToTarget();
                return true;
            }
        };

        OnBlockListener mOnBlockListener = new OnBlockListener() {
            public void onBlocked() {
                LewaUtils.logE(TAG, "onBlocked ");
                Message msg = mHandler.obtainMessage(MediaPlaybackService.ERROR_HINT);
                msg.obj = Lewa.string(R.string.no_buffer_text);   
                msg.sendToTarget();
                mHandler.sendEmptyMessage(MediaPlaybackService.ERROR_NETWORK);
                //playFinish();
            }
        };

        OnDownloadErrorListener mDownloadErrorListener = new OnDownloadErrorListener() {

            @Override
            public void onDownloadError(java.lang.Throwable ex) {
                LewaUtils.logE(TAG, "onDownloadError : "+ex);
                if(ex instanceof java.net.SocketException) {
                    //this error is handled by mOnBlockListener and mErrorListener : pause 
                } else {
                    playFinish(FINISH_ERROR);
                }
                
            }
        };

        OnCompletionListener mCompletionListener = new OnCompletionListener() {

            @Override
            public void onCompletion() {
                // TODO Auto-generated method stub
                
                playFinish(FINISH_COMPLETE);
                
            }
        };
        mStreamPlayer = new StreamPlayer(mContext.getApplicationContext());
//        mStreamPlayer.setAutoSave(true);
        File file = new File(SAVE_PATH);
        if (!file.exists()) {
            file.mkdir();
            MusicUtils.isFirst = true;
        }
//        mStreamPlayer.setSavePath(SAVE_PATH);
        mStreamPlayer.setOnCompletionListener(mCompletionListener);
        mStreamPlayer.setOnPreparedListener(mOnPreparedListener);

        mStreamPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);

        mStreamPlayer.setOnErrorListener(mErrorListener);
        mStreamPlayer.setOnDownloadErrorListener(mDownloadErrorListener);
        mStreamPlayer.setOnBlockListener(mOnBlockListener);
    }

    private void playFinish(int state) {
        Log.i(TAG, "playFinish");
        //mContext.sendBroadcast(new Intent("com.lewa.player.onlinetolocal")); //not use
        
        if(Lewa.getPlayStatus().getRepeatMode() != MediaPlaybackService.REPEAT_CURRENT  
                    || FINISH_ERROR == state) {
            MediaPlaybackService.isOnlinePlay = false;
        }
        
        if(null != mStreamPlayer) {
            mStreamPlayer.reset();
        }

        
        acquireWakeLock();
        Message msg = mHandler.obtainMessage();
        msg.what = MediaPlaybackService.TRACK_ENDED;
        msg.obj = state;
        mHandler.sendMessage(msg);
        //mHandler.sendEmptyMessage(MediaPlaybackService.TRACK_ENDED);
        mHandler.sendEmptyMessage(MediaPlaybackService.RELEASE_WAKELOCK);
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public void start() {
        //MusicUtils.debugLog(new Exception("MultiPlayer.start called"));
        if (!MediaPlaybackService.isOnlinePlay) {
            if (mStreamPlayer != null){
                	stopPlayOnlineMusic();
                }
            mMediaPlayer.start();
        } else {
            if (mStreamPlayer == null) {
                initStreamPlayer();
            }
            mMediaPlayer.stop();
            if(mStreamPlayer.isPlaying()) {
                stopPlayOnlineMusic();
            }
            mIsInitialized = true;
            //Log.i(TAG, "stream start  mStreamPlayer.isPrepared() " + mStreamPlayer.isPrepared());
            if(!mStreamPlayer.isPrepared()) {
                String musicId = String.valueOf(mStreamPlayer.getMusicId());
                mStreamPlayer.reset();
                mStreamPlayer.release();
                mStreamPlayer = null;
                setDataSource(musicId);
            } else {
            	Log.i(TAG, "mStreamPlayer.start() session id is " + mStreamPlayer.getSessionId() );
                startPlayOnlineMusic();
            }
        }

    }

	private void startPlayOnlineMusic() {
		onlineMusicSessionId = mStreamPlayer.getSessionId();
		Log.i(TAG, "startPlayOnlineMusic ID IS " + onlineMusicSessionId);
		mStreamPlayer.start();
	}

	private void stopPlayOnlineMusic() {
		Log.i(TAG, "stopPlayOnlineMusic");
		onlineMusicSessionId = -1;
		mStreamPlayer.stop();
	}

    public void stop() {
        if (!MediaPlaybackService.isOnlinePlay) {
            mMediaPlayer.stop();
        } else {
            if (mStreamPlayer != null)
				stopPlayOnlineMusic();
        }
    }

    public void release() {
        stop();
        if (!MediaPlaybackService.isOnlinePlay) {
            mMediaPlayer.release();
        } else {
            if (mStreamPlayer != null)
                mStreamPlayer.release();
        }
    }

    public void pause() {
        if (!MediaPlaybackService.isOnlinePlay) {
            mMediaPlayer.pause();
        } else {
            if (mStreamPlayer != null)
                mStreamPlayer.pause();
        }
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            // Acquire a temporary wakelock, since when we return from
            // this callback the MediaPlayer will release its wakelock
            // and allow the device to go to sleep.
            // This temporary wakelock is released when the RELEASE_WAKELOCK
            // message is processed, but just in case, put a timeout on it.
            acquireWakeLock();
            if (mHandler.hasMessages(MediaPlaybackService.TRACK_ENDED))
                mHandler.removeMessages(MediaPlaybackService.TRACK_ENDED);
            Message msg = mHandler.obtainMessage();
            msg.what = MediaPlaybackService.TRACK_ENDED;
            msg.obj = FINISH_COMPLETE;
            mHandler.sendMessage(msg);
            //mHandler.sendEmptyMessage(MediaPlaybackService.TRACK_ENDED);
            mHandler.sendEmptyMessage(MediaPlaybackService.RELEASE_WAKELOCK);
        }
    };

    MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    new Thread() {
                        public void run() {
                            // TODO Auto-generated method stub
                            mIsInitialized = false;
                            mMediaPlayer.release();
                            // Creating a new MediaPlayer and settings its wakemode does not
                            // require the media service, so it's OK to do this now, while the
                            // service is still being restarted
                            mMediaPlayer = new MediaPlayer();
                            //mMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
                            //mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
                        }
                    }.start();
                    return true;
                default:
	                    
                    break;
            }
            return false;
        }
    };

    public long duration() {
        if (!MediaPlaybackService.isOnlinePlay) {
            try {
                return mMediaPlayer.getDuration();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                return 0;
            }
        } else {
            if (mStreamPlayer == null)
                initStreamPlayer();
            return mStreamPlayer.duration();
        }
    }

    public long position() {
        if (!MediaPlaybackService.isOnlinePlay) {
            try {
                return mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                return 0;
            }
        } else {
            if (mStreamPlayer == null)
                return 0;
            return mStreamPlayer.position();
        }
    }

    public long downloadprocess() {
        if (mStreamPlayer != null) {
            return mStreamPlayer.downloadProgress();
        } else {
            return 0;
        }
    }

    public long seek(long whereto) {
        if (!MediaPlaybackService.isOnlinePlay) {
            mMediaPlayer.seekTo((int) whereto);
        } else {
            if (mStreamPlayer != null)
                mStreamPlayer.seek((int) whereto);
        }
        return whereto;
    }

    public void setVolume(float vol) {
        if (!MediaPlaybackService.isOnlinePlay) {
            mMediaPlayer.setVolume(vol, vol);
        } else {
//            if (mStreamPlayer != null)
//                mStreamPlayer.setVolume(vol);
        }
    }

    public void setAudioSessionId(int sessionId) {
        mMediaPlayer.setAudioSessionId(sessionId);
    }

    public int getAudioSessionId() {
        if (MediaPlaybackService.isOnlinePlay) {
        	if (mStreamPlayer == null){
                	return 0;
            } else{
            	return onlineMusicSessionId;
            }
        } else {
        	return mMediaPlayer.getAudioSessionId();
        }
    }

    public int getBufferPercent() {
        return bufferPercent;
    }

    public void stopStreamPlayer() {
        if (mStreamPlayer != null)
			stopPlayOnlineMusic();
    }

}
