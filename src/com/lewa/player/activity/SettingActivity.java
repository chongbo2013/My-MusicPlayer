package com.lewa.player.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.lewa.ExitApplication;
import com.lewa.Lewa;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.ScanFileService;
import com.lewa.player.ShakeListener;
import com.lewa.util.Constants;
import com.lewa.kit.ActivityHelper;
import com.lewa.util.LewaUtils;

import android.os.Build;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.widget.Toast;
import android.util.Log;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import com.lewa.player.SleepModeManager;
import com.baidu.music.manager.JobManager;
import com.baidu.music.onlinedata.OnlineManagerEngine;
import com.lewa.player.online.AppDownloadManager;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.ChooseAccountPreferenceFragment;



//lkzhou start
import android.os.RemoteException;
import android.preference.PreferenceManager;
import lewa.support.v7.app.ActionBar;
import lewa.support.v7.app.ActionBarActivity;
import lewa.support.v7.app.ActionBar.Tab;
import lewa.support.v7.app.ActionBar.TabListener;
import lewa.support.v7.app.ActionBar.LayoutParams;
import lewa.support.v7.view.ActionMode;
import lewa.support.v7.view.ActionMode.Callback;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
//lkzhou end


/**
 * Created by wuzixiu on 1/7/14.
 */
public class SettingActivity extends ActionBarActivity implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    private static final String TAG = "SettingActivity";

    private AlertDialog mDownloadDialog;
    ProgressDialog mProgress;

    private SharedPreferences music_settings;
    private SharedPreferences.Editor prefsPrivateEditor;

    private String ifBacklight;
    private String ifDownImg;
    private String ifDownLrcBio;
    //private String isflowhint;
    //	private String downStream;
//	private String ifWifi;
    private String[] mDowmStreamStr = null;
    private int mDownVal;
    SwitchPreference backlightCheBox;
    SwitchPreference downImgCheBox;
    SwitchPreference downLrcBioCheBox;
    //	ListPreference downStreamList;
//	CheckBoxPreference downStreamCheBox;
    private String[] mBgStr;
    private String mBgkey;
    //    private String ifDownInfo;
    private String isShake;
    private String shake_degree;
    private String scrubber;
    private String isFade;
    SwitchPreference shakeCheBox;
    SwitchPreference fadeCheBox;
    Preference shakedegreePref;
    Preference adjustScrubberPref;

    private String song_sync;
    private String sleep_mode;
    private String exit_app;
    Preference songSYnc;
    Preference sleepMode;
    Preference exitApp;
    int mShakeProgress;
    //    private SwitchPreference downInfoCheBox;
    private SwitchPreference flowHintBox;
    //    private SwitchPreference showSpectrumBox;
    private String isShowSpectrum;
    private String isFilterSongs;
    private SwitchPreference filterSongsBox;
    private Boolean isChangeBg = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar){
        	actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        }
        
        FragmentManager fragmentManager = getFragmentManager();  
        FragmentTransaction fragmentTransaction =   
            fragmentManager.beginTransaction();  
        ChooseAccountPreferenceFragment fragment1 = new ChooseAccountPreferenceFragment();  
        fragmentTransaction.replace(android.R.id.content, fragment1);          
        fragmentTransaction.addToBackStack(null);   
        fragmentTransaction.commit(); 
        
    }
    
    
    public void initAccountPreferenceFragment(PreferenceFragment fragment) {
    	fragment.addPreferencesFromResource(R.xml.music_preference);

        ifBacklight = fragment.getResources().getString(R.string.setting_backlight_key);
        backlightCheBox = (SwitchPreference) fragment.findPreference(ifBacklight);

        ifDownImg = fragment.getResources().getString(R.string.setting_downimg_key);
        downImgCheBox = (SwitchPreference) fragment.findPreference(ifDownImg);

//        ifDownInfo = getResources().getString(R.string.setting_downInfo_key);
//        downInfoCheBox = (SwitchPreference) findPreference(ifDownInfo);

        ifDownLrcBio = fragment.getResources().getString(R.string.setting_downlrc_key);
        downLrcBioCheBox = (SwitchPreference) fragment.findPreference(ifDownLrcBio);

        isShake = fragment.getResources().getString(R.string.setting_shake_key);
        shakeCheBox = (SwitchPreference) fragment.findPreference(isShake);

        isFade = fragment.getResources().getString(R.string.setting_fade_key);
        fadeCheBox = (SwitchPreference) fragment.findPreference(isFade);

        //isflowhint = getResources().getString(R.string.setting_flowhint_key);
        //flowHintBox = (SwitchPreference) findPreference(isflowhint);

        isShowSpectrum = fragment.getResources().getString(R.string.setting_show_spectrum_key);
//        showSpectrumBox = (SwitchPreference) findPreference(isShowSpectrum);

        isFilterSongs = fragment.getResources().getString(R.string.setting_filtersongs_key);
        filterSongsBox = (SwitchPreference) fragment.findPreference(isFilterSongs);

        mBgkey = fragment.getResources().getString(R.string.setting_bg_key);
        mBgStr = fragment.getResources().getStringArray(R.array.bg_choise);

        backlightCheBox.setOnPreferenceChangeListener(SettingActivity.this);
        backlightCheBox.setOnPreferenceClickListener(SettingActivity.this);
        downImgCheBox.setOnPreferenceChangeListener(SettingActivity.this);
        downImgCheBox.setOnPreferenceClickListener(SettingActivity.this);
        downLrcBioCheBox.setOnPreferenceChangeListener(SettingActivity.this);
        downLrcBioCheBox.setOnPreferenceClickListener(SettingActivity.this);
//        downInfoCheBox.setOnPreferenceChangeListener(this);
//        downInfoCheBox.setOnPreferenceClickListener(this);
        fadeCheBox.setOnPreferenceChangeListener(SettingActivity.this);
        fadeCheBox.setOnPreferenceClickListener(SettingActivity.this);
        //flowHintBox.setOnPreferenceChangeListener(SettingActivity.this);
        //flowHintBox.setOnPreferenceClickListener(SettingActivity.this);
//        showSpectrumBox.setOnPreferenceChangeListener(this);
//        showSpectrumBox.setOnPreferenceClickListener(this);

        filterSongsBox.setOnPreferenceChangeListener(SettingActivity.this);
        filterSongsBox.setOnPreferenceClickListener(SettingActivity.this);
//         downStreamCheBox.setOnPreferenceChangeListener(this);
//         downStreamCheBox.setOnPreferenceClickListener(this);


        Preference folderPreference = fragment.findPreference("music_select_folder");
        folderPreference.setOnPreferenceClickListener(SettingActivity.this);

        shakeCheBox.setOnPreferenceChangeListener(SettingActivity.this);
        shakeCheBox.setOnPreferenceClickListener(SettingActivity.this);

        shake_degree = fragment.getResources().getString(R.string.shake_degree_key);
        song_sync = fragment.getResources().getString(R.string.song_synchronize);
        sleep_mode = fragment.getResources().getString(R.string.sleep_mode);
        exit_app = fragment.getResources().getString(R.string.exit);
        scrubber = "music_EQ";
        shakedegreePref = (Preference) fragment.findPreference(shake_degree);
        shakedegreePref.setOnPreferenceClickListener(SettingActivity.this);
        if (shakeCheBox.isChecked()) {
            shakedegreePref.setEnabled(true);
        } else {
            shakedegreePref.setEnabled(false);
        }

        songSYnc = (Preference) fragment.findPreference(song_sync);
        songSYnc.setOnPreferenceClickListener(SettingActivity.this);

        sleepMode = (Preference) fragment.findPreference(sleep_mode);
        sleepMode.setOnPreferenceClickListener(SettingActivity.this);
        

        exitApp = (Preference) fragment.findPreference(exit_app);
        exitApp.setOnPreferenceClickListener(SettingActivity.this);

        adjustScrubberPref = fragment.findPreference(scrubber);
        adjustScrubberPref.setOnPreferenceClickListener(SettingActivity.this);

//         if(!MusicUtils.mHasSongs){
//             folderPreference.setEnabled(false);
//         }else{
//             folderPreference.setEnabled(true);
//         }
        IntentFilter intentFilter = new IntentFilter();
        /*intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mScanListener, intentFilter);
        registerReceiver(scanSdFilesReceiver, intentFilter);*/
        
        intentFilter = new IntentFilter();
        intentFilter.addAction(ScanFileService.UPDATE_ALL_AUDIO_FILES_COMPLETED);
        registerReceiver(mScanFileCompletedReceiver, intentFilter);
        music_settings = SettingActivity.this.getSharedPreferences("Music_setting", 0);

        prefsPrivateEditor = music_settings.edit();
    }
    
   /* public class ChooseAccountPreferenceFragment extends PreferenceFragment {
    	   //将原actvity中与PreferenceActivity相关的方法移植到此处
    	        @Override
    	        public void onCreate(Bundle savedInstanceState) {  
    	            super.onCreate(savedInstanceState);  
    	            initAccountPreferenceFragment(this);
    	       
    	            
    	        } 
    	        
//    	        @Override
//    	        public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
//    	        	return false;
//    	        }
    }*/

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Intent intent = new Intent(this.getApplicationContext(), ScanFileService.class);
        //intent.setPackage("com.lewa.player");
        startService(intent);
        setSwitch();
    }

    protected void onDestroy() {
       /* unregisterReceiver(mScanListener);
        unregisterReceiver(scanSdFilesReceiver);*/
        
        unregisterReceiver(mScanFileCompletedReceiver);
        stopService(new Intent(this.getApplicationContext(), ScanFileService.class));
        super.onDestroy();
        if(isCloseApp) {
        	android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public void onWindowFocusChanged (boolean hasFocus) {

        if(hasFocus) {
            resetSeelpModeSummary();
        }
    }

    public void resetSeelpModeSummary() {
        int mProgress = PreferenceManager.getDefaultSharedPreferences(this).getInt("sleep_mode_time", 0);
        if(0 == mProgress) {
            sleepMode.setSummary(this.getResources().getString(R.string.sleep_close_ok));
        } else {
            sleepMode.setSummary(this.getResources().getString(R.string.sleep_start_ok));
        }
    }

    
    private void setSwitch() {
        resetSeelpModeSummary();
        
        int isScreenOn = Lewa.getIntSetting(Constants.SETTINGS_SCREEN_LIGHT, Constants.SETTINGS_SCREEN_LIGHT_DEFAULT);
        if (isScreenOn == Constants.ON) {
            backlightCheBox.setChecked(true);
        } else {
            backlightCheBox.setChecked(false);
        }
        int isDownImgSwitchOn = Lewa.getIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_AVATAR, Constants.SETTINGS_DOWNLOAD_AVATAR_DEFAULT);
        if (isDownImgSwitchOn == Constants.SETTINGS_DOWNLOAD_AVATAR_ON) {
            downImgCheBox.setChecked(true);
        } else {
            downImgCheBox.setChecked(false);
        }

        int isDownLyricSwitchOn = Lewa.getIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_LRC, Constants.SETTINGS_DOWNLOAD_LRC_DEFAULT);
        if (isDownLyricSwitchOn == Constants.SETTINGS_DOWNLOAD_LRC_ON) {
            downLrcBioCheBox.setChecked(true);
        } else {
        	downLrcBioCheBox.setChecked(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        if (preference.getKey().equals(ifBacklight)) {
            if (!(backlightCheBox.isChecked()) && newValue.toString().equals("true")) {
                backlightCheBox.setChecked(true);
                Lewa.saveIntSetting(Constants.SETTINGS_SCREEN_LIGHT, Constants.ON);
            } else if (backlightCheBox.isChecked() && newValue.toString().equals("false")) {
                backlightCheBox.setChecked(false);
                Lewa.saveIntSetting(Constants.SETTINGS_SCREEN_LIGHT, Constants.OFF);
            }
        } else if (preference.getKey().equals(ifDownImg)) {
            if (!(downImgCheBox.isChecked()) && newValue.toString().equals("true")) {
                downImgCheBox.setChecked(true);
                Lewa.saveIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_AVATAR, Constants.SETTINGS_DOWNLOAD_AVATAR_ON);
            } else if (downImgCheBox.isChecked() && newValue.toString().equals("false")) {
                downImgCheBox.setChecked(false);
                Lewa.saveIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_AVATAR, Constants.SETTINGS_DOWNLOAD_AVATAR_OFF);
            }
        }
        /*else if (preference.getKey().equals(ifDownInfo)) {
            if (!(downInfoCheBox.isChecked()) && newValue.toString().equals("true")) {
                downInfoCheBox.setChecked(true);
                prefsPrivateEditor.putInt("downInfo", 1).commit();
            } else if (downInfoCheBox.isChecked() && newValue.toString().equals("false")) {
                downInfoCheBox.setChecked(false);
                prefsPrivateEditor.putInt("downInfo", 0).commit();
            }
        } */
        else if (preference.getKey().equals(ifDownLrcBio)) {
            if (!(downLrcBioCheBox.isChecked()) && newValue.toString().equals("true")) {
                downLrcBioCheBox.setChecked(true);
                Lewa.saveIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_LRC, Constants.SETTINGS_DOWNLOAD_LRC_ON);
            } else if (downLrcBioCheBox.isChecked() && newValue.toString().equals("false")) {
                downLrcBioCheBox.setChecked(false);
                Lewa.saveIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_LRC, Constants.SETTINGS_DOWNLOAD_LRC_OFF);
            }
        }
//	    if(preference.getKey().equals(ifWifi)){
//	        mDownVal = Integer.valueOf((String)newValue);
//            if(mDowmStreamStr != null && mDownVal >=0 && mDownVal < mDowmStreamStr.length)
//                preference.setSummary(mDowmStreamStr[mDownVal]);
//            if(mDownVal == 0) {
//                prefsPrivateEditor.putInt("iswifi", 1).commit();
//            } else {
//                prefsPrivateEditor.putInt("iswifi", 0).commit();
//            }
//        }
        else if (preference.getKey().equals(isShake)) {
            if (!(shakeCheBox.isChecked()) && newValue.toString().equals("true")) {
                shakeCheBox.setChecked(true);
                shakedegreePref.setEnabled(true);
                prefsPrivateEditor.putInt("shake", 1).commit();
            } else if (shakeCheBox.isChecked() && newValue.toString().equals("false")) {
                shakeCheBox.setChecked(false);
                shakedegreePref.setEnabled(false);
                prefsPrivateEditor.putInt("shake", 0).commit();
            }
            Intent intent = new Intent();
            intent.setAction(MediaPlaybackService.SHAKE);
            sendBroadcast(intent);
        } else if (preference.getKey().equals(mBgkey)) {
            /*mBgVal = Integer.valueOf((String) newValue);
            if (mBgStr != null && mBgVal >= 0 && mBgVal < mBgStr.length) {
                bgList.setSummary(mBgStr[mBgVal]);
            }
            prefsPrivateEditor.putInt("playerbg", mBgVal).commit();
            Intent intent = new Intent("com.lewa.plaer.bgchanged");
            sendBroadcast(intent);
            isChangeBg = true;*/
        } else if (preference.getKey().equals(isFade)) {
            if (!(fadeCheBox.isChecked()) && newValue.toString().equals("true")) {
                fadeCheBox.setChecked(true);
                prefsPrivateEditor.putInt("isFade", 1).commit();
                //Log.i("test","test1");
            } else if (fadeCheBox.isChecked() && newValue.toString().equals("false")) {
                fadeCheBox.setChecked(false);
                prefsPrivateEditor.putInt("isFade", 0).commit();
                //Log.i("test","test0");
            }
//        } else if (preference.getKey().equals(isflowhint)) {
//            if (!(flowHintBox.isChecked()) && newValue.toString().equals("true")) {
//                flowHintBox.setChecked(true);
//                prefsPrivateEditor.putInt("isFlowHint", 1).commit();
//            } else if (flowHintBox.isChecked() && newValue.toString().equals("false")) {
//                flowHintBox.setChecked(false);
//                prefsPrivateEditor.putInt("isFlowHint", 0).commit();
//            }
        } else if (preference.getKey().equals(isShowSpectrum)) {
            /*if (!(showSpectrumBox.isChecked()) && newValue.toString().equals("true")) {
                showSpectrumBox.setChecked(true);
                prefsPrivateEditor.putInt("isShowSpectrum", 1).commit();
            } else if (showSpectrumBox.isChecked() && newValue.toString().equals("false")) {
                showSpectrumBox.setChecked(false);
                prefsPrivateEditor.putInt("isShowSpectrum", 0).commit();
            }*/
        } else if (preference.getKey().equals(isFilterSongs)) {
            if (!(filterSongsBox.isChecked()) && newValue.toString().equals("true")) {
                filterSongsBox.setChecked(true);
                prefsPrivateEditor.putInt("isFilterSongs", 1).commit();
            } else if (filterSongsBox.isChecked() && newValue.toString().equals("false")) {
                filterSongsBox.setChecked(false);
                prefsPrivateEditor.putInt("isFilterSongs", 0).commit();
            }
            Intent intent = new Intent();
            intent.setAction("com.lewa.player.filterSongs");
            sendBroadcast(intent);
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // TODO Auto-generated method stub
        if (preference.getKey().equals(isFade)) {
            if (fadeCheBox.isChecked()) {
                prefsPrivateEditor.putInt("isFade", 1).commit();
                //Log.i("test","test1");
            } else {
                prefsPrivateEditor.putInt("isFade", 0).commit();

                //Log.i("test","test0");
            }
        } else if (preference.getKey().equals(ifBacklight)) {
            if (backlightCheBox.isChecked()) {
                Lewa.saveIntSetting(Constants.SETTINGS_SCREEN_LIGHT, Constants.ON);
            } else {
                Lewa.saveIntSetting(Constants.SETTINGS_SCREEN_LIGHT, Constants.OFF);
            }
        } else if (preference.getKey().equals(ifDownImg)) {
            if (downImgCheBox.isChecked()) {
                Lewa.saveIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_AVATAR, Constants.SETTINGS_DOWNLOAD_AVATAR_ON);
            } else {
                Lewa.saveIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_AVATAR, Constants.SETTINGS_DOWNLOAD_AVATAR_OFF);
            }
//        } else if (preference.getKey().equals(isflowhint)) {
//            if (flowHintBox.isChecked()) {
//                prefsPrivateEditor.putInt("isFlowHint", 1).commit();
//            } else {
//                prefsPrivateEditor.putInt("isFlowHint", 0).commit();
//            }
        } else if (preference.getKey().equals(isShowSpectrum)) {
//            if (showSpectrumBox.isChecked()) {
//                prefsPrivateEditor.putInt("isShowSpectrum", 1).commit();
//            } else {
//                prefsPrivateEditor.putInt("isShowSpectrum", 0).commit();
//            }
        } else if (preference.getKey().equals(isFilterSongs)) {
            if (filterSongsBox.isChecked()) {
                prefsPrivateEditor.putInt("isFilterSongs", 1).commit();
            } else {
                prefsPrivateEditor.putInt("isFilterSongs", 0).commit();
            }
            Intent intent = new Intent();
            intent.setAction("com.lewa.player.filterSongs");
            sendBroadcast(intent);
        }
        /*else if (preference.getKey().equals(ifDownInfo)) {
            if (downInfoCheBox.isChecked()) {
                prefsPrivateEditor.putInt("downInfo", 1).commit();
            } else {
                prefsPrivateEditor.putInt("downInfo", 0).commit();
            }
        } */
        else if (preference.getKey().equals(ifDownLrcBio)) {
            if (downLrcBioCheBox.isChecked()) {
                Lewa.saveIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_LRC, Constants.SETTINGS_DOWNLOAD_LRC_ON);
            } else {
                Lewa.saveIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_LRC, Constants.SETTINGS_DOWNLOAD_LRC_OFF);
            }
        } else if (preference.getKey().equals(isShake)) {
            if (shakeCheBox.isChecked()) {
                prefsPrivateEditor.putInt("shake", 1).commit();
            } else {
                prefsPrivateEditor.putInt("shake", 0).commit();
            }
            Intent intent = new Intent();
            intent.setAction(MediaPlaybackService.SHAKE);
            sendBroadcast(intent);
        } else if (preference.getKey().equals(shake_degree)) {
            LinearLayout inputLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.shake_degree_set, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.shake_degree_title));
            builder.setView(inputLayout);
//            builder.setTitle(R.string.clear_history);
            SeekBar seek = (SeekBar) inputLayout.findViewById(R.id.shake_seekbar);
            seek.setMax(20);
            mShakeProgress = MusicUtils.getIntPref(this, "shake_degree", ShakeListener.DEFAULT_SHAKE_DEGREE);
            seek.setProgress(mShakeProgress - 5);
            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub
                    mShakeProgress = progress + 5;
                }
            });

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    prefsPrivateEditor.putInt("shake_degree", mShakeProgress).commit();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                }
            });

            builder.show();
        } else if (preference.getKey().equals("music_select_folder")) {
            Intent intent = new Intent().setClass(this, SelectFolderActivity.class);
            startActivity(intent);
        } else if (preference.getKey().equals(scrubber)) {
           Intent intent = new Intent().setClass(this, MusicEQActivity.class);
           startActivity(intent);
        }else if (preference.getKey().equals(song_sync)) {
            Lewa.playerServiceConnector().pause();
            syncMusic();
        }else if (preference.getKey().equals(sleep_mode)) {
            new SleepModeManager(this);
            resetSeelpModeSummary();
        } else if (preference.getKey().equals(exit_app)) {
            IMediaPlaybackService service = Lewa.playerServiceConnector().service();
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

                //pr952965 modify by wjhu begin
                //destroy all activities and stop service
                ExitApplication exit = (ExitApplication) getApplication();
                exit.finishAll();
                Intent stopIntent = new Intent();
                stopIntent.setClass(SettingActivity.this, MediaPlaybackService.class);
                Lewa.playerServiceConnector().releasePlayer();
                SettingActivity.this.stopService(stopIntent);
                //pr952965 modify by wjhu end
                Intent exitIntent = new Intent(Intent.ACTION_MAIN);
                exitIntent.addCategory(Intent.CATEGORY_HOME);
                exitIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(exitIntent);
                isCloseApp = true;
                this.finish();
//                MusicUtils.unbindFromService(mToken);
                //android.os.Process.killProcess(android.os.Process.myPid());
        }

        return false;
    }
    
    BroadcastReceiver mScanFileCompletedReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
                mProgress = null;
            }
			
		}
    	
    };
    
    boolean isCloseApp = false;
    private void syncMusic() {
        Log.i(TAG, "syncMusic");
        if(LewaUtils.isSDcardMounted()) {
            if(Build.VERSION.SDK_INT < 19) {    //platform version below 
            // android 4.4
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                                + Environment.getExternalStorageDirectory())));
            } else {
                /*MediaScannerConnection.scanFile(this, new String[] {
                        Environment.getExternalStorageDirectory().getPath()
                        }, null, null);*/
            	sendBroadcast(new Intent(ScanFileService.UPDATE_ALL_AUDIO_FILES));
            	
            	mProgress = ProgressDialog.show(SettingActivity.this,
                        getString(R.string.synchron_title),
                        getString(R.string.synchron_message), true);
                mProgress.setCancelable(true);
            }
        } else {
            Toast.makeText(this,getString(R.string.no_sdcard_message_text), 0).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
//                Intent intent = new Intent(this, MusicMainEntryActivity.class);
//                startActivity(intent);
                finish();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isChangeBg) {
//            Intent intent = new Intent(this, MusicMainEntryActivity.class);
//            startActivity(intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

     private BroadcastReceiver scanSdFilesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
                mProgress = ProgressDialog.show(SettingActivity.this,
                        getString(R.string.synchron_title),
                        getString(R.string.synchron_message), true);
                mProgress.setCancelable(true);
            }
            if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                if (mProgress != null && mProgress.isShowing()) {
                    mProgress.dismiss();
                    mProgress = null;
                }
            }
        }
    };

     private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(SettingActivity.this);
            }
            if (mDownloadDialog != null) {
                mDownloadDialog.dismiss();
                mDownloadDialog = null;
            }
        }
    };
}
