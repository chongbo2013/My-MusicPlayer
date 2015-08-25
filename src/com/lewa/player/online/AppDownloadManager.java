package com.lewa.player.online;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.baidu.music.download.DownloadEntry;
import com.baidu.music.download.DownloadManager;
import com.baidu.music.download.DownloadStatus;
import com.baidu.music.download.DownloadManager.DownloadProgressListener;
import com.lewa.Lewa;
import com.lewa.il.MusicInterfaceLayer.OnDownloadProgressChangeListener;
import com.lewa.il.MusicInterfaceLayer.OnDownloadStatusChangeListener;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.db.DBService;
import com.lewa.player.model.MusicDownloadStatus;
import com.lewa.player.model.Song;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;
import com.lewa.player.R;

public class AppDownloadManager {
	private static final String TAG = "AppDownloadManager";
	private static AppDownloadManager appDownloadManager=null;
	private DownloadManager downloadManager=null;
	private OnDownloadProgressChangeListener onDownloadProgressChangeListener=null;
	private OnDownloadStatusChangeListener onDownloadStatusChangeListener=null;
	private static final String SAVE_PATH= Environment.getExternalStorageDirectory()+"/LEWA/music/mp3";
	private int DOWNLOADMAXSIZE = 3;
	private Context mContext;
	private AppDownloadManager(Context context) {
		if(downloadManager==null)
			downloadManager = DownloadManager.getInstance(context);
		mContext=context;
		downloadManager.setSavePath(SAVE_PATH);
		downloadManager.setMaxDownloadingSize(DOWNLOADMAXSIZE);
		downloadManager.openDownloadDB(context);
	}
	public static AppDownloadManager getInstance(Context context){
		if(appDownloadManager==null)
			appDownloadManager=new AppDownloadManager(context);
		return appDownloadManager;
	}
	
	public void addDownload(long musicId,String bitrate,boolean isLossless){
            int losslessFlag = isLossless ? DownloadManager.DOWNLOAD_TYPE_LOSSLESS : DownloadManager.DOWNLOAD_TYPE_DEFAULT;
		addDownloadListener(musicId, new LewaDownloadProgressListener(musicId,bitrate,losslessFlag));
		downloadManager.addDownload(musicId, bitrate, losslessFlag);
	}
	
	public void delDownload(long musicId,String bitrate,boolean isLossless){
            int losslessFlag = isLossless ? DownloadManager.DOWNLOAD_TYPE_LOSSLESS : DownloadManager.DOWNLOAD_TYPE_DEFAULT;
		downloadManager.deleteDownload(musicId, bitrate, losslessFlag);
	}
	
	public void addDownloadListener(long musicId,DownloadProgressListener listener){
		downloadManager.addDownloadListener(musicId, listener);
	}
	
	public void delDownloadListener(long musicId){
		downloadManager.deleteDownloadListener(musicId);
	}
	
	public void pauseDownload(Song song){
		LewaUtils.logE(TAG, "pauseDownload");
		long musicId=song.getId();
		String bitrate=song.getBitrate();
		//boolean isLossless=song.isLossless();
            int losslessFlag = song.isLossless() ? DownloadManager.DOWNLOAD_TYPE_LOSSLESS : DownloadManager.DOWNLOAD_TYPE_DEFAULT;
		downloadManager.pauseDownload(musicId, bitrate, losslessFlag);
		downloadManager.deleteDownloadListener(musicId);
//		Lewa.removeDownloadSong(musicId,Constants.DOWNLOAD_REMOVE_STATUS_PAUSE);
		Lewa.pauseDownload(song);
	}
	
	public void resumeDownload(Song song){
		LewaUtils.logE(TAG, "resumeDownload");
		long musicId=song.getId();
		String bitrate=song.getBitrate();
		//boolean isLossless=song.isLossless();
            int losslessFlag = song.isLossless() ? DownloadManager.DOWNLOAD_TYPE_LOSSLESS : DownloadManager.DOWNLOAD_TYPE_DEFAULT;
		downloadManager.resumeDownload(musicId, bitrate, losslessFlag);
		downloadManager.addDownloadListener(musicId, new LewaDownloadProgressListener(musicId,bitrate,losslessFlag));
		Lewa.resumeDownload(song);
	}
	
	public void setOnDownloadProgressChangeListener(
			OnDownloadProgressChangeListener onDownloadProgressChangeListener) {
		this.onDownloadProgressChangeListener = onDownloadProgressChangeListener;
	}
	
	public void removeOnDownloadProgressChangeListener(){
		this.onDownloadProgressChangeListener = null;
	}

	public void setOnDownloadStatusChangeListener(
			OnDownloadStatusChangeListener onDownloadStatusChangeListener) {
		this.onDownloadStatusChangeListener = onDownloadStatusChangeListener;
	}

	public void removeOnDownloadStatusChangeListener(){
		this.onDownloadStatusChangeListener=null;
	}
	
	public void releaseDownloadManager(){
		try {
			/*HashMap<Long, Song> downloadingSongs=Lewa.getDownloadingSong();
			Set<Entry<Long, Song>> entrys=downloadingSongs.entrySet();
			for(Entry<Long, Song> entry : entrys){
				Song song = entry.getValue();
				pauseDownload(song);
				MusicDownloadStatus downStatus =new MusicDownloadStatus();
				downStatus.setStatus(DownloadStatus.STATUS_RUNNING_PAUSED);
				DBService.saveSong(song);
			}*/
			if(downloadManager!=null)
				downloadManager.closeDownloadDB(mContext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DownloadEntry getDownloadEntryInfo(Song song){
            int losslessFlag = song.isLossless() ? DownloadManager.DOWNLOAD_TYPE_LOSSLESS : DownloadManager.DOWNLOAD_TYPE_DEFAULT;
		return downloadManager.getDownloadEntryInfo(song.getId(), song.getBitrate(), losslessFlag);
	}


	private class LewaDownloadProgressListener implements DownloadProgressListener{
		private long musicId;
		private String bitrate;
		private int isLossless;
		private int status;
		public LewaDownloadProgressListener(long musicId,String bitrate,int isLossless){
			this.musicId=musicId;
			this.bitrate=bitrate;
			this.isLossless=isLossless;
		}
		@Override
		public void onDownloadProgressChanged(long musicId,long mCurrentBytes,long mTotalBytes) {
			// TODO Auto-generated method stub
			DownloadEntry downloadEntry=downloadManager.getDownloadEntryInfo(musicId, bitrate, isLossless);
			if(downloadEntry!=null)
				status=downloadEntry.getDownloadStatus();
			if(onDownloadProgressChangeListener!=null)
				onDownloadProgressChangeListener.onProgressChange(musicId, mCurrentBytes, mTotalBytes,status);
			LewaUtils.logE(TAG, "onDownloadProgressChanged status : "+ downloadEntry.getDownloadStatus());
		}

		@Override
		public void onDownloadStatusChanged(long musicId,int status) {
			// TODO Auto-generated method stub
			try {
				LewaUtils.logE(TAG, "onDownloadStatusChange status : "+ status+"id : "+musicId);
				if(onDownloadStatusChangeListener!=null)
					onDownloadStatusChangeListener.onStatusChange(musicId, status);
				if(status==DownloadStatus.STATUS_SUCCESS||status==DownloadStatus.STATUS_ALREADY_EXIST){
					DownloadEntry downloadEntry=downloadManager.getDownloadEntryInfo(musicId, bitrate, isLossless);
					String fullPath=downloadEntry.getFullPath();
					fullPath = LewaUtils.checkPathSuffix(fullPath);//baidu jar may return fullpath endwith null.so we must check it.baidu will fix this bug later
				    Song onlineSong = DBService.findSongById(musicId, Song.TYPE.ONLINE);
				    onlineSong.setDownloadStatus(status);
				    onlineSong.setPath(fullPath);
				    DBService.saveSong(onlineSong);
					Intent intent=new Intent(MediaPlaybackService.SCANMUSIC);
					intent.putExtra("music_id", musicId);
					intent.putExtra("fullpath", fullPath);
					intent.putExtra("trackTitle", downloadEntry.getTrackTitle());
					mContext.sendBroadcast(intent);
					downloadManager.deleteDownloadListener(musicId);
					Lewa.removeDownloadSong(musicId,Constants.DOWNLOAD_REMOVE_STATUS_SUCCESS);
				}else{
					if(status == DownloadStatus.STATUS_HTTP_DATA_ERROR){
						if(Lewa.pausedDownloadSongs().containsKey(musicId)){
							status = DownloadStatus.STATUS_RUNNING_PAUSED;
						}else if(Lewa.stopDownloadSongs().containsKey(musicId)){
							return;
						}
					}
					checkIfFailed(musicId,status);
					
				    Song onlineSong = DBService.findSongById(musicId, Song.TYPE.ONLINE);
				    onlineSong.setDownloadStatus(status);
				    DBService.saveSong(onlineSong);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	private void checkIfFailed(long musicId, int status) {
		// TODO Auto-generated method stub
		if(status >= DownloadStatus.STATUS_BAD_REQUEST && status<=DownloadStatus.STATUS_URL_NOT_FOUND){
			Lewa.removeDownloadSong(musicId,Constants.DOWNLOAD_REMOVE_STATUS_FAIL);
		}else if(status == DownloadStatus.STATUS_SONG_COPY_ERR || status == DownloadStatus.STATUS_SONG_ID_ERR){
			Lewa.removeDownloadSong(musicId,Constants.DOWNLOAD_REMOVE_STATUS_FAIL);
		}
	}
	
}
