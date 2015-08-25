package com.lewa.player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			new CopyPresetMusicAsyncTask(context).execute();
		}
	}
	
	private class CopyPresetMusicAsyncTask extends AsyncTask<Object, Integer, Object> {
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
						Log.i(TAG, "copy music :" + name);
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
						Log.i(TAG, "scan music :" + name);
						MediaScannerConnection.scanFile(context, new String[]{MUSIC_FOLDER + name}, null, null);
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
		
	}

}
