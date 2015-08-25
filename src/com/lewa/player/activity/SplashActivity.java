package com.lewa.player.activity;

import lewa.support.v7.app.ActionBarActivity;
import lewa.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.baidu.music.manager.JobManager;
import com.lewa.Lewa;
import com.lewa.kit.ActivityHelper;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;

/**
 * Created by wuzixiu on 11/21/13.
 */
public class SplashActivity extends ActionBarActivity {
    private final static String TAG = "Splash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_sdcard_title_text)
                    .setMessage(R.string.no_sdcard_message_text)
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ok_cn_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            JobManager.stop();
                            SleepModeManager.setSleepTime(Lewa.context(), 0);
                            SleepModeManager.deleteSleepTime(Lewa.context());
                            Intent exitIntent = new Intent(Intent.ACTION_MAIN);
                            exitIntent.addCategory(Intent.CATEGORY_HOME);
                            exitIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(exitIntent);
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }).create().show();
            return;
        } else {
            new SplashScreen(this).execute();
        }
    }

    public class SplashScreen extends AsyncTask<Void, Void, Void> {
    	ActionBarActivity activity;

        public SplashScreen(ActionBarActivity activity) {
            this.activity = activity;
        }

        protected void onPreExecute() {
        }

        protected Void doInBackground(Void... ignore) {
            return null;
        }

        protected void onPostExecute(Void ignore) {
            ActivityHelper.goLibrary(activity);
            finish();
			overridePendingTransition(0, 0);
        }
    }
}
