package com.lewa.player;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedList;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.Toast;

public class ScanFileService extends Service {
	public static final String UPDATE_ALL_AUDIO_FILES = "com.lewa.player.SCAN_FILE";
	public static final String UPDATE_ALL_AUDIO_FILES_COMPLETED = "com.lewa.player.SCAN_FILE_COMPLETED";
	private static final String TAG = "ScanFileService";
	private MediaScannerConnection mediaScannerConnection = null;
	private String[] storageDirectories;
	private String[] audioSuffix = new String[]{".mp3", ".aac", ".wav", ".amr", ".ape", ".ogg", ".m4a", ".mid", ".smf", ".imy", ".3gpp", ".3ga", ".wma"};	
	private boolean isScanningFinished = false;
	private boolean isOneFileScaned = true;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter(UPDATE_ALL_AUDIO_FILES);
		registerReceiver(mScanFileReceiver, filter);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		
		unregisterReceiver(mScanFileReceiver);
		isScanningFinished = true;
		if(mediaScannerConnection != null) {
			mediaScannerConnection.disconnect();
			mediaScannerConnection = null;
		}
		
		super.onDestroy();
	}
	
	private BroadcastReceiver mScanFileReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "mScanFileReceiver onReceive");
			isScanningFinished = false;
			connectMediaScanner();
		}
	};
	
	public void refreshExternalStorage(final Handler responseHandler){
		isScanningFinished = false;
		connectMediaScanner();	
	}
	
	private boolean isAudioFile(String fileAbsolutePath) {
		int index = fileAbsolutePath.lastIndexOf(".");
		if(index > 0){
			String suffixString  = fileAbsolutePath.substring(fileAbsolutePath.lastIndexOf(".")).toLowerCase();
			for(int i=0; i<audioSuffix.length; i++) {
				if (suffixString.equals(audioSuffix[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isValidDirectory(File file) {
		if (file.isDirectory()) {
			return !file.getName().startsWith(".");
		}
		return false;
	}
	
	private void listAllAudioFiles() {
		getExternalStorageDirectories();
		if(storageDirectories == null || storageDirectories.length == 0){
			return;
		}
		
		new Thread(new Runnable(){
			@Override
			public void run(){
				for(int i=0; i<storageDirectories.length; i++) {
					Log.i(TAG, "the volume path is " + storageDirectories[i]);
					if(isScanningFinished) {
						if(mediaScannerConnection != null) {
							mediaScannerConnection.disconnect();
							mediaScannerConnection = null;
						}
						break;
					}
					
					//scanDirNoRecursion(new File(storageDirectories[i]));
					LinkedList<File> list = new LinkedList<File>();
					File parentdir = new File(storageDirectories[i]);
					File children[] = parentdir.listFiles();
					if(children == null){
						continue;
					}
					for (int j = 0; j < children.length; j++) {
						if (isValidDirectory(children[j]))
							list.add(children[j]);
						else {
							String fileName = children[j].getAbsolutePath();
							if(!scanOneAudioFile(fileName)) {
								return ;
							}
						}
					}
					File tmp;
					while (!list.isEmpty()) {
						tmp = (File) list.removeFirst();
						if (tmp.isDirectory()) {
							children = tmp.listFiles();
							if (children == null)
								continue;
							for (int j = 0; j < children.length; j++) {
								if (isValidDirectory(children[j]))
									list.add(children[j]);
								else {
									String fileName = children[j].getAbsolutePath();
									if(!scanOneAudioFile(fileName)) {
										return ;
									}
								}
							}
						} else {
							String fileName = tmp.getAbsolutePath();
							if(!scanOneAudioFile(fileName)) {
								return ;
							}
						}
					}
				}
				
				isScanningFinished = true;
				ScanFileService.this.sendBroadcast(new Intent(UPDATE_ALL_AUDIO_FILES_COMPLETED));
				if(null != mediaScannerConnection) {
					mediaScannerConnection.disconnect();
					mediaScannerConnection = null;
				}
			}
		}).start();
	}
	
	private boolean scanOneAudioFile(String fileName) {
		if(null == mediaScannerConnection){
			isScanningFinished = true;
			return false;
		}
		
		if (isAudioFile(fileName)) {
			Log.i(TAG, fileName);
			try {
				mediaScannerConnection.scanFile(fileName, null);
				isOneFileScaned = false;
				
			} catch (Exception e) {
				return false;
			}
			
		}
		return true;
	}
	
	
	
	public void scanDirNoRecursion(File rootDir) {
		
	}
	
	private Handler scanCompleteHandler = new Handler() {
		@Override
		public void handleMessage(Message msg){
			ScanFileService.this.sendBroadcast(new Intent(UPDATE_ALL_AUDIO_FILES_COMPLETED));
			
		}
	};
	
	
	private MediaScannerConnection.MediaScannerConnectionClient client = new MediaScannerConnectionClient() {
		
		@Override
		public void onScanCompleted(String path, Uri uri) {
			isOneFileScaned = true;
			if(isScanningFinished && mediaScannerConnection != null) {
				Log.i(TAG, "onScanCompleted");
				mediaScannerConnection.disconnect();
				mediaScannerConnection = null;
				scanCompleteHandler.sendEmptyMessage(0);
			}
		}
		
		@Override
		public void onMediaScannerConnected() {
			if(mediaScannerConnection != null) {
				Log.i(TAG, "onMediaScannerConnected");
				
				listAllAudioFiles();
			}
		}
	};
			
	
	private void connectMediaScanner() {
		if(null == mediaScannerConnection) {
			mediaScannerConnection = new MediaScannerConnection(this, client);
			mediaScannerConnection.connect();
		}
	}
	
	private void getExternalStorageDirectories() {
        StorageManager storageManager = null;
        Method methodGetPaths = null;
        try {
            storageManager = (StorageManager) this.getSystemService(Activity.STORAGE_SERVICE);
            methodGetPaths = storageManager.getClass().getMethod("getVolumePaths");
            storageDirectories = (String[]) methodGetPaths.invoke(storageManager);
        } catch (Exception e) {
            e.printStackTrace();
            storageDirectories = new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()};
        }
    }

}
