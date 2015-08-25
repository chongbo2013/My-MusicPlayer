package com.lewa.player.online;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.lewa.player.MusicUtils;
//import com.ting.mp3.android.onlinedata.LyricManager;

import java.io.File;


public class DownLoadLrc extends AsyncTask<String, Integer, Long> {

	@Override
	protected Long doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		return null;
	}
/*    private Context mContext ;
    public static String SRC_PATH="/LEWA/music/lrc/";
	public DownLoadLrc(Context context){
	    this.mContext=context;
	};
	private int mlrcDownStat = 0;
	//private bitmapandString 
	@Override
	protected Long doInBackground(String... params) {
	    LyricManager lrcManager = MusicUtils.getLyricManager(mContext);
	    lrcManager.getLyricFile(params[0], params[1], Environment.getExternalStorageDirectory()+SRC_PATH);
	    File file=new File(Environment.getExternalStorageDirectory()+SRC_PATH+params[0]+"-"+params[1]+".lrc");
	    if(file.exists()){
	        mlrcDownStat=0;
	        }else{
	            mlrcDownStat=-1;
	        }
	  
		return Long.valueOf(params[2]);
	}

	
	@Override
	protected void onPostExecute(Long result) {
//		if(result > 0) {
//			OnlineLoader.SendtoUpdateLRC(result, mlrcDownStat);
//		}
	}
	*/
}
