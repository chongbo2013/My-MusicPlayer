package com.lewa.player.activity;

import lewa.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import com.lewa.ExitApplication;
import com.baidu.music.manager.JobManager;
import com.baidu.music.onlinedata.OnlineManagerEngine;
import com.lewa.Lewa;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.online.AppDownloadManager;
import com.lewa.util.Constants;

/**
 * Created by wuzixiu on 1/6/14.
 */
public class BaseActivity extends ActionBarActivity {
    boolean hasBackground = false;
    PlayStatusListener mPlayStatusListener = null;

    @Override
    public void onStart() {
        super.onStart();
        ExitApplication exit = (ExitApplication) getApplication();
        exit.addActivity(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Lewa.registerPlayStatusListener(mPlayStatusListener);

        PlayStatus playStatus = Lewa.getPlayStatus();

        if (playStatus != null) {
            refreshPlayStatus(playStatus);

//            if (hasBackground && playStatus.getPlayingSong() != null && playStatus.getPlayingSong().getCoverUrl() != null) {
//                Lewa.getAndBlurImage(playStatus.getPlayingSong().getCoverUrl(), mPlayStatusListener);
//            }
            if (hasBackground && mPlayStatusListener != null) {
                Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);
            }
        }

        int ifBacklight = Lewa.getIntSetting(Constants.SETTINGS_SCREEN_LIGHT, Constants.SETTINGS_SCREEN_LIGHT_DEFAULT);

        if (ifBacklight == Constants.ON) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        int ifBacklight = Lewa.getIntSetting(Constants.SETTINGS_SCREEN_LIGHT, Constants.SETTINGS_SCREEN_LIGHT_DEFAULT);

        if (ifBacklight == Constants.ON) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	ExitApplication exit = (ExitApplication) getApplication();
        exit.removeActivity(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Lewa.unregisterPlayStatusListener(mPlayStatusListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int mProgress = PreferenceManager.getDefaultSharedPreferences(this).getInt("sleep_mode_time", 0);
        if(mProgress > 0) {
            getMenuInflater().inflate(R.menu.main_sleep, menu);
        } else {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        IMediaPlaybackService service = Lewa.playerServiceConnector().service();

        switch (id) {
            case R.id.music_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.song_synchronize:
                try {
                    if (service != null) {
                        service.stop();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                                + Environment.getExternalStorageDirectory())));
                break;
            case R.id.sleep_mode:
                new SleepModeManager(this);
                break;
            case R.id.exit:
            	AppDownloadManager.getInstance(getApplicationContext()).releaseDownloadManager();
                JobManager.stop();
                OnlineManagerEngine.getInstance(getApplicationContext()).releaseEngine();
                SleepModeManager.setSleepTime(Lewa.context(), 0);
                SleepModeManager.deleteSleepTime(Lewa.context());
                Lewa.removeNotify();
                try {
                    if (service != null && service.isPlaying()) {
                        MusicUtils.sService.seek(0);
                        service.stop();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                Intent exitIntent = new Intent(Intent.ACTION_MAIN);
                exitIntent.addCategory(Intent.CATEGORY_HOME);
                exitIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(exitIntent);
//                MusicUtils.unbindFromService(mToken);
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    void refreshPlayStatus(PlayStatus playStatus) {

    }
}
