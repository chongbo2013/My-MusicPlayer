package com.lewa;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.baidu.music.helper.MediaScanner;
import com.lewa.player.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Messenger;
import android.os.IBinder;

//import java.util.concurrent.Executors;
//import android.util.Log;

//import java.util.concurrent.Executors;
//import android.util.Log;

/**
 * Created by wuzixiu on 1/8/14.
 */
public class ExitApplication extends Lewa {
    private static List<Activity> mainActivity = new ArrayList<Activity>();
    public Messenger mService = null;

    //by Fanzhong
    private Toast mToast;
    //end

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub

        }
    };
    

    @Override
    public void onCreate() {
    	//new CopyPresetMusicAsyncTask(getApplicationContext()).execute();
        super.onCreate();
        //by Fanzhong
        /*Added by ruiwei, for Modifying Toast style, 20150211, start*/
        Context activity = getTopActivity();        
        if(null == activity){
        	activity = getApplicationContext();
        }
        mToast = Toast.makeText(activity, getString(R.string.Hearing_loss), 0);
        /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
        bindService(new Intent("lewa.lockscreen.service.LockScreenService"), mConnection,
                Context.BIND_AUTO_CREATE);

    }

    public List<Activity> MainActivity() {
        return mainActivity;
    }

    public void addActivity(Activity act) {
        mainActivity.add(act);
    }


    public void removeActivity(Activity act) {
		// pr952965 modify by wjhu
		// mainActivity.remove(act);
    }
    
    public void rmAActivity(Activity a) {
    	mainActivity.remove(a);
    }

    /*Added by ruiwei, for Modifying Toast style, 20150211, start*/
    public static Activity getTopActivity(){
    	Activity topActivity = null;
        if(mainActivity.size() > 0){
        	topActivity = mainActivity.get(mainActivity.size()-1);
        }
        return topActivity;
    }
    /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
    
    public void finishAll() {
        for (Activity act : mainActivity) {
            if (!act.isFinishing()) {
                act.finish();
            }
        }
        mainActivity.clear();
		// mainActivity = null;
		// pr952965 modify by wjhu
		// unregisterReceiver(receiver);
		// unbindService(mConnection);
    }
    
    /*private class CopyPresetMusicAsyncTask extends AsyncTask<Object, Integer, Object> {
    	String[] musicNames = new String []{"Electro_Funk.mp3", "Last_Night_Trippin_Signature.mp3", "Midnight.mp3"};
		private Context context;
		public CopyPresetMusicAsyncTask(Context context) {
			this.context = context.getApplicationContext();
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			String flagName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.musicflag";
			if(new File(flagName).exists()) {
				return null;
			}
			
			String MUSIC_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/";
			File folder =  new File(MUSIC_FOLDER);
			if(! folder.exists()) {
				folder.mkdirs();
			}
			byte[] buffer = new byte[1024];
			for(String name : musicNames) {
				try {
					File outFile = new File(MUSIC_FOLDER + name);
					if(! outFile.exists()) {
						//Log.i(TAG, "copy music :" + name);
						InputStream ips = context.getAssets().open(name);
						OutputStream ops = new FileOutputStream(outFile);
						
						while(true) {
							int read = ips.read(buffer);
							if (read > 0) {
								ops.write(buffer, 0, read);
							}
							if(read < 1024) {
								break;
							}
						}
						
						ips.close();
						ops.close();
						//Log.i(TAG, "scan music :" + name);
						MediaScannerConnection.scanFile(context, new String[]{name}, null, null);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			File flagFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.musicflag");
			try {
				flagFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}*/
    
}
