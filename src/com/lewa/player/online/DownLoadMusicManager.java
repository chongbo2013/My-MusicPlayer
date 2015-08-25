package com.lewa.player.online;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import android.content.Context;

import org.xml.sax.DTDHandler;

import com.baidu.music.model.Music;
import com.baidu.music.onlinedata.OnlineManagerEngine;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
//import com.ting.mp3.android.onlinedata.OnlineManagerEngine;
//import com.ting.mp3.android.onlinedata.OnlineSongDetailsDataManager;
//import com.ting.mp3.android.onlinedata.OnlineSongDetailsDataManager.OnlineSongDetailsDataResultListener;
//import com.ting.mp3.android.onlinedata.xml.type.SongDetail;
//import com.ting.mp3.android.onlinedata.xml.type.SongUrlDetail;

import android.R.integer;
import android.app.DownloadManager;
//import android.app.LewaDownloadManager;
//import android.app.LewaDownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.lewa.player.R;

public class DownLoadMusicManager {
   /* private Context context;
    private OnlineManagerEngine engine;
    public  static LewaDownloadManager downloadManager;
    private OnlineSongDetailsDataManager songDetailsDataManager;
    private Handler handler = new Handler();
    private static Map<Long, SongDetail> detailMap;
    private long lastUpdateTime=0;
    private static final String SAVE_PATH= Environment.getExternalStorageDirectory()+"/LEWA/music/mp3";
    private int tag=-1;
    private BroadcastReceiver successReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            long completeDownloadId = intent.getLongExtra(
                    LewaDownloadManager.EXTRA_DOWNLOAD_ID, -1);
            SongDetail detail=detailMap.get(completeDownloadId);
            
            if(detail!=null){
                intent=new Intent(MediaPlaybackService.UPDATE_STATUS);
                intent.putExtra("songId", Long.parseLong(detail.mSongId));
                String fullpath=getFullPath(detail);
                intent.putExtra("data", fullpath);
                intent.putExtra("downFinish", true);
                //intent.putExtra("fullpath", fullpath.substring(fullpath.lastIndexOf("/")+1));
                context.sendBroadcast(intent);
                intent.setAction(MediaPlaybackService.SCANMUSIC);
                intent.putExtra("fullpath", fullpath);
                intent.putExtra("trackTitle", detail.mTitle);
                context.sendBroadcast(intent);
                MusicUtils.delDownloadSong(detail.mSongId, context);
            }
            detailMap.remove(completeDownloadId);
            if(MusicUtils.isFirst){
                MusicUtils.insertFolderPath(context, new String[]{SAVE_PATH});
                MusicUtils.isFirst=false;
            }
        }
    };
    private OnlineSongDetailsDataResultListener detailsListener = new OnlineSongDetailsDataResultListener() {

        @Override
        public void onGetSongDetailComplete(SongDetail detail) {
            // TODO Auto-generated method stub
            if(detail==null)
                return;
            List<SongUrlDetail> songUrlDetails = detail.mSongUrls;
            if(songUrlDetails==null)
                return;
            for (SongUrlDetail urlDetail : songUrlDetails) {
                String mFileBitrate = urlDetail.mFileBitrate;
                String fileLink = urlDetail.mFileLink;
                if (mFileBitrate != null && mFileBitrate.equals("128")
                        && fileLink != null) {
                    LewaDownloadManager.Request request = new Request(
                            Uri.parse(fileLink));
                    String mAuthor=detail.mAuthor;
                    String mAlbumTitle=detail.mAlbumTitle;
                    String mTitle=detail.mTitle;
                    if(mAuthor!=null&&mAuthor.contains(" "))
                        mAuthor=mAuthor.replace(" ", "_");
                    if(mAlbumTitle!=null&&mAlbumTitle.contains(" "))
                        mAlbumTitle=mAlbumTitle.replace(" ", "_");
                    if(mTitle!=null&&mTitle.contains(" "))
                        mTitle=mTitle.replace(" ", "_");
                    File file = Environment.getExternalStoragePublicDirectory("LEWA/music/mp3");
                    if (!file.exists()||!file.isDirectory()) {
                        file.mkdirs();
                    };
                    try {
                        request.setDestinationInExternalPublicDir("LEWA/music/mp3",
                                mAuthor + "-" + mAlbumTitle + "-"
                                        + mTitle + ".mp3");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        return;
                    }
                    request.setNotiExtras(String.valueOf(tag));
                    request.setNotificationVisibility(LewaDownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    if(downloadManager!=null){
                        long downloadId=-1;
						try {
							downloadId = downloadManager.enqueue(request);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        if (downloadId == -1) {
                            Intent intent=new Intent(MediaPlaybackService.UPDATE_STATUS);
                            intent.putExtra("songId", Long.valueOf(detail.mSongId));
                            intent.putExtra("failure", true);
                            context.sendBroadcast(intent);
                        } else {
                            detailMap.put(downloadId, detail);
                        }
                    }
                }
            }
        }

    };
    private DownloadsChangeObserver downloadObserver;

    public DownLoadMusicManager(Context context) {
        super();
        this.context = context;
        MediaPlaybackService.STATUS=context.getString(R.string.online_downloading);
        if (downloadManager == null)
            downloadManager = (LewaDownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
        if (engine == null) {
            engine = MusicUtils.getEngine(context);
        }
        if (songDetailsDataManager == null) {
            songDetailsDataManager = engine
                    .getOnlineSongDetailsDataManager(context);
        }
        if(detailMap==null)
            detailMap=new HashMap<Long, SongDetail>();
        downloadObserver = new DownloadsChangeObserver(
                handler, context);
        context.getContentResolver().registerContentObserver(
                LewaDownloadManager.CONTENT_URI, true,
                downloadObserver);
        IntentFilter filter=new IntentFilter();
        filter.addAction(LewaDownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(successReceiver, filter);
    }

    public void downMusic(int songId,int tag) {
        songDetailsDataManager.getSongDetailAsync(songId, detailsListener);
        this.tag=tag;
    }

    public int resumeDownload(int id){
        return downloadManager.resumeDownload(id);
    }

    private class DownloadsChangeObserver extends ContentObserver {
        private Context context;

        public DownloadsChangeObserver(Handler handler, Context context) {
            super(handler);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            // TODO Auto-generated method stub
            if(System.currentTimeMillis()-lastUpdateTime<5000)
                return;
            lastUpdateTime=System.currentTimeMillis();
            if(detailMap!=null&&detailMap.size()>0){
                Intent intent=new Intent(MediaPlaybackService.UPDATE_STATUS);
                Set<Entry<Long, SongDetail>> sets= detailMap.entrySet();
                for(Entry<Long, SongDetail> entry:sets){
                    if(downloadManager!=null){
                        int[] currentByte = downloadManager.getDownloadBytes(entry.getKey());
                        int status=downloadManager.getStatusById(entry.getKey());
                        if(currentByte[1]!=0){
                            long progress = 100 * currentByte[0] / currentByte[1];
                            if(progress<=100&&progress>=0){
                                intent.putExtra("progress", progress);
                                intent.putExtra("songId", Long.valueOf(entry.getValue().mSongId));
                                intent.putExtra("status", status);
                                intent.putExtra("downId", entry.getKey());
                                this.context.sendBroadcast(intent);
                            }
                        }
                    }
                }
            }
        }
    }

    public String getFullPath(SongDetail detail){
        String mAuthor=detail.mAuthor;
        String mAlbumTitle=detail.mAlbumTitle;
        String mTitle=detail.mTitle;
        if(mAuthor!=null&&mAuthor.contains(" "))
            mAuthor=mAuthor.replace(" ", "_");
        if(mAlbumTitle!=null&&mAlbumTitle.contains(" "))
            mAlbumTitle=mAlbumTitle.replace(" ", "_");
        if(mTitle!=null&&mTitle.contains(" "))
            mTitle=mTitle.replace(" ", "_");
        if(mAuthor!=null&&mAlbumTitle!=null&&mTitle!=null){
            return SAVE_PATH+"/"+mAuthor+"-"+mAlbumTitle+"-"+mTitle+".mp3";
        }
        return null;
    }

    public int getStatus(int downId){
        return downloadManager.getStatusById(downId);
    }

    public void release(){
        context.unregisterReceiver(successReceiver);
        context.getContentResolver().unregisterContentObserver(downloadObserver);
        downloadManager=null;
    }
*/
}
