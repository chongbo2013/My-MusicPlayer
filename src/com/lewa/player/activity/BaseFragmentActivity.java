package com.lewa.player.activity;

import lewa.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.baidu.music.manager.JobManager;
import com.baidu.music.onlinedata.OnlineManagerEngine;
import com.lewa.ExitApplication;
import com.lewa.Lewa;
import com.lewa.kit.ActivityHelper;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.db.DBService;
import com.lewa.player.listener.CallbackPlayListener;
import com.lewa.player.listener.PlayStatusListener;
import com.lewa.player.model.PlayStatus;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.player.online.AppDownloadManager;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.service.PlayerServiceConnector;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;

import android.media.MediaScannerConnection;
import android.os.Build;
import java.sql.SQLException;
import android.util.Log;
/**
 * Created by wuzixiu on 1/6/14.
 */
public class BaseFragmentActivity extends ActionBarActivity {
    private static final String TAG = "BaseFragmentActivity";
    boolean hasBackground = false;
    PlayStatusListener mPlayStatusListener = null;
    public static final int CREATE_PLAYLIST_RESULT_CODE = 1;
    private AlertDialog mDownloadDialog;
    private boolean mIsDownloadStop;
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mScanListener, intentFilter);
        registerReceiver(scanSdFilesReceiver, intentFilter);
        ExitApplication exit = (ExitApplication) getApplication();
        exit.addActivity(this);
    }

    @Override
    public void onStart() {
        super.onStart();
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
            if (mPlayStatusListener != null) {  //hasBackground && 
                //Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);
            }
        }

        int firstOpen = Lewa.getIntSetting(Constants.FIRST_OPEN, 1);
        mHandler.sendEmptyMessage(firstOpen);

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
    public void onStop() {
        super.onStop();
        Lewa.unregisterPlayStatusListener(mPlayStatusListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScanListener);
        unregisterReceiver(scanSdFilesReceiver);
        ExitApplication exit = (ExitApplication) getApplication();
        exit.removeActivity(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
        // 清除菜单内容后由条件判断语句决定如何生成选项菜单
        /*menu.clear();
        int mProgress = PreferenceManager.getDefaultSharedPreferences(this).getInt("sleep_mode_time", 0);
        if(mProgress > 0) {
            getMenuInflater().inflate(R.menu.main_sleep, menu);
        } else {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*int mProgress = PreferenceManager.getDefaultSharedPreferences(this).getInt("sleep_mode_time", 0);
        if(mProgress > 0) {
            getMenuInflater().inflate(R.menu.main_sleep, menu);
        } else {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;*/
        return super.onCreateOptionsMenu( menu);
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
                Lewa.playerServiceConnector().pause();
                syncMusic();
                break;
            case R.id.sleep_mode:
                new SleepModeManager(this);
                break;
            case R.id.exit:
            	AppDownloadManager.getInstance(Lewa.context()).releaseDownloadManager();
                JobManager.stop();
                OnlineManagerEngine.getInstance(Lewa.context()).releaseEngine();
                SleepModeManager.setSleepTime(Lewa.context(), 0);
                SleepModeManager.deleteSleepTime(Lewa.context());
                Lewa.removeNotify();
                try {
                    if (service != null && service.isPlaying()) {
                        service.seek(0);
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


    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(BaseFragmentActivity.this);
            }
            if (mDownloadDialog != null) {
                mDownloadDialog.dismiss();
                mDownloadDialog = null;
            }
            mIsDownloadStop = false;
        }
    };

	private void syncMusic() {
        if(LewaUtils.isSDcardMounted()) {
            if(Build.VERSION.SDK_INT < 19) {    //platform version below 
            // android 4.4
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                                + Environment.getExternalStorageDirectory())));
            } else {
				
                MediaScannerConnection.scanFile(this, new String[] {
                        Environment.getExternalStorageDirectory().getPath()
                        }, null, null);
            }
        } else {
            Toast.makeText(this,getString(R.string.no_sdcard_message_text), 0).show();
        }

    }

    private BroadcastReceiver scanSdFilesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
                if (mProgress == null || !mProgress.isShowing()) {
                    mProgress = ProgressDialog.show(BaseFragmentActivity.this,
                            getString(R.string.synchron_title),
                            getString(R.string.synchron_message), true);
                    mProgress.setCancelable(true);
                }

            }
            if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                if (mProgress != null && mProgress.isShowing()) {
                    mProgress.dismiss();
                    mProgress = null;
                }
                refreshSongsInfo();
            }
        }
    };

    protected void refreshSongsInfo() {
        
    }

    

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1 && MusicUtils.mHasSongs) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BaseFragmentActivity.this);
                builder.setMessage(R.string.first_dialog_message);
                builder.setTitle(R.string.first_dialog_title);
                builder.setPositiveButton(R.string.dialog_positive,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent();
                                intent.setClass(BaseFragmentActivity.this,
                                        SelectFolderActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        });
                builder.setNegativeButton(R.string.dialog_negative,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                if (!isFinishing())
                    builder.create().show();

                Lewa.saveIntSetting(Constants.FIRST_OPEN, 0);
            }
        }
    };

    public void setLongClickSong(Song song){

    }

    void refreshPlayStatus(PlayStatus playStatus) {
//        Log.d(TAG, "Play status changed: " + playStatus.getPlayingSong().getId() + "\t" + playStatus.ge);
    }

    public void prepareForPlay(final CallbackPlayListener callbackPlayListener) {
        if(!OnlineLoader.isWiFiActive(this) && OnlineLoader.isNetworkAvailable() ) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle(R.string.traffic_tip_text)
//                    .setMessage(R.string.traffic_tip_message_text)
//                    .setPositiveButton(getResources().getString(R.string.continue_play_text), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
                            callbackPlayListener.execute();
//                        }
//                    })
//                    .setNegativeButton(getResources().getString(R.string.cancel_cn_text), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
//                            ActivityHelper.goLibraryMine(BaseFragmentActivity.this);
//                        }
//                    }).create().show();

        } else if(!OnlineLoader.isWiFiActive(this) && !OnlineLoader.isNetworkAvailable()) {//!OnlineLoader.IsConnection(getActivity())){
            /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_network)
                    .setMessage(R.string.no_network_text)
                    .setPositiveButton(getResources().getString(R.string.ok_cn_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();                            
                        }
                    }) .create().show();*/
             Toast.makeText(this, Lewa.string(R.string.no_network_text), Toast.LENGTH_SHORT).show();
        } else {
            callbackPlayListener.execute();
        }

    }

}
