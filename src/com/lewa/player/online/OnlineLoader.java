package com.lewa.player.online;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.util.Log;

import com.lewa.Lewa;
import com.lewa.il.MusicInterfaceLayer;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.Song;
import com.lewa.util.Constants;
import com.lewa.util.LewaUtils;
import com.lewa.util.NetUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import com.baidu.music.onlinedata.LyricManager.LyricDownloadListener;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;



public class OnlineLoader {

    public static final String UPDATEBG = "com.lewa.player.UpdateArtistBG";
    public static final String UPDATELRC = "com.lewa.player.UpdateLRC";
    public static final String GET_PIC_ACTION = "com.lewa.player.getPic";
    public static final String STOPDOWNLOAD = "com.lewa.player.stopDownload";

    public static final int ALBUMDOWNLOAD = 1;
    public static final int ARTISTDOWNLOAD = 0;
    public static SharedPreferences settings;
    public static Bitmap retbit;
    private static int w;
    private static int h;
    private static boolean isfromhome = false;

    static public class bitmapandString {
        //modify by zhaolei,120327,for artistImg save
        String artistName;   //long artistId;
        //end
        String albumStringFilename;
        Bitmap albumbitmap;
    }

    public static void getArtistImg(String artistName, int ifFavourit, int x, int y) {
        isfromhome = true;
        w = x;
        h = y;
        if (artistName == null) {
            SendtoUpdate(null, null);
            return;
        }

        if (Lewa.getIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_AVATAR, Constants.SETTINGS_DOWNLOAD_AVATAR_DEFAULT) == Constants.SETTINGS_DOWNLOAD_AVATAR_ON) {
            if (((IsConnection(Lewa.context())) ||
                    isWiFiActive(Lewa.context())) && !artistName.equalsIgnoreCase("<unknown>")) {
//                String url = getRequestUrl("SearchArtist", artistName);
//                DownLoadAsync downArtistImg = new DownLoadAsync();
                //modified by wangliqiang
                try {
                    IMediaPlaybackService service = Lewa.playerServiceConnector().service();
                    if (service != null) {
                        String name = service.getArtistName();
                        if (name != null) {
//                            name = MusicUtils.buildArtistName(name);
                            if (!name.equals(artistName))
                                return;
                        }
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//                DownLoadAsync downArtistImg = new DownLoadAsync(Lewa.context(), false);
//                downArtistImg.ifFavourit = ifFavourit;
//                downArtistImg.execute(artistName, null);
            } else {
                SendtoUpdate(null, null);
            }
        } else {
            SendtoUpdate(null, null);
        }
    }
    
    /**
     * title can not be null,artist can be null
     * @param title
     * @param artist
     */
    public static void getMusicImg(String title,String artist){
    	if(title==null){
    	   SendtoUpdate(null, null);
           return;
    	}
    	if (Lewa.getIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_AVATAR, Constants.SETTINGS_DOWNLOAD_AVATAR_DEFAULT) == Constants.SETTINGS_DOWNLOAD_AVATAR_ON){
    		if(NetUtils.isNetworkValid(Lewa.context())&&!LewaUtils.isNameUnknown(title)){
    			 try {
                     IMediaPlaybackService service = Lewa.playerServiceConnector().service();
                     if (service != null) {
                         String name = service.getArtistName();
                         if (name != null&&artist!=null) {
                             if (!name.equals(artist))
                                 return;
                         }
                     }
                 } catch (RemoteException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
    			 MusicInterfaceLayer.getInstance().downLoadLrcPicAsync(Lewa.context(), title, artist,MusicInterfaceLayer.DownloadType.ARTIST);
    		}else{
    			 SendtoUpdate(null, null);
    		}
    	}else{
    		 SendtoUpdate(null, null);
    	}	
    }

    public static int ifwifionly() {
        settings = Lewa.context().getSharedPreferences("Music_setting", 0);
        return settings.getInt("iswifi", 1);

    }

    public static void getSongLrc(String title, String artist) {
        if (Lewa.getIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_LRC, Constants.SETTINGS_DOWNLOAD_LRC_DEFAULT) == Constants.SETTINGS_DOWNLOAD_LRC_ON) {
            if (NetUtils.isNetworkValid(Lewa.context())) {
//                DownLoadLrc downLrc = new DownLoadLrc(Lewa.context());
//                downLrc.execute(TrackName, artistName, String.valueOf(songid));
            	MusicInterfaceLayer.getInstance().downLoadLrcPicAsync(Lewa.context(), title, artist,MusicInterfaceLayer.DownloadType.LYRIC);
            }
        }
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) Lewa.context().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (mConnectivityManager == null) return false;

        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static Boolean IsConnection(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED
                || connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING) {
            return true;
        } else if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED
                || connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    public static boolean isWiFiActive(Context inContext) {
        WifiManager mWifiManager = (WifiManager) inContext
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void SendtoUpdate(String path, String artistName) {
        if (path == null)
            return;
        Intent intent = new Intent();
        if (path != null && path.contains("/") && path.contains("")) {
            String name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(""));
            IMediaPlaybackService service = Lewa.playerServiceConnector().service();
            //TODO: check this

            Song playingSong = Lewa.getPlayStatus().getPlayingSong();

//            if (service != null && name != null && !name.equals(artistName))
//                return;
            intent.putExtra("name", name);
        }
//	    if(retbit!=null&&!retbit.isRecycled()){
//	        retbit.recycle();
//	    }
        if (!isfromhome) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            InputStream isImg = null;
            try {
                isImg = new FileInputStream(path);
                retbit = BitmapFactory.decodeStream(isImg, null, options);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
            } finally {
                try {
                    if (isImg != null)
                        isImg.close();
                } catch (IOException e) {
                }
                isImg = null;
            }
        } else {
            retbit = MusicUtils.getLocalBitmap(Lewa.context(), path, w, h);
            isfromhome = false;
        }
        if (retbit == null) {
            retbit = MusicUtils.getDefaultBg(Lewa.context(), R.drawable.bg_playlist_default);
        }
        intent.setAction(UPDATEBG);
        intent.putExtra(Constants.BITMAP, retbit);
        intent.putExtra(Constants.PATH, path);
        intent.putExtra(Constants.ARTIST_NAME, artistName);
        Lewa.context().sendBroadcast(intent);
    }

    public static void SendtoUpdate(String path, int w, int h) {
        Intent intent = new Intent();
        if (path != null && path.contains("/") && path.contains("")) {
            String name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(""));
            IMediaPlaybackService service = Lewa.playerServiceConnector().service();
            if (service != null && name != null && !name.equals(MusicUtils.getArtistNameOnlyFirst()))
                return;
            intent.putExtra("name", name);
        }
//	        if(retbit!=null&&!retbit.isRecycled()){
//	            retbit.recycle();
//	        }
        if (path != null) {
            retbit = MusicUtils.getLocalBitmap(Lewa.context(), path, w, h);
        } else {
            retbit = MusicUtils.getDefaultBg(Lewa.context(), R.drawable.bg_playlist_default, w, h);
        }
        if (retbit == null) {
            retbit = MusicUtils.getDefaultBg(Lewa.context(), R.drawable.bg_playlist_default, w, h);
        }
        intent.setAction(UPDATEBG);
        if (!retbit.isRecycled()) {
            intent.putExtra("backg", retbit);
            Lewa.context().sendBroadcast(intent);
        }
    }

    public static void SendtoUpdateFavourit(String path, int w, int h) {
//	       if(retbit!=null&&!retbit.isRecycled()){
//               retbit.recycle();
//               System.gc();
//           }
        Log.i("wangliqiang", "SendtoUpdateFavourit");
        retbit = MusicUtils.getLocalBitmap(Lewa.context(), path, w, h);
        if (retbit == null) {
            retbit = MusicUtils.getDefaultArtImg(Lewa.context());
        }
        Intent intent = new Intent();
        if (path != null && path.contains("/") && path.contains("")) {
            String name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(""));
            IMediaPlaybackService service = Lewa.playerServiceConnector().service();
            if (service != null && name != null && !name.equals(MusicUtils.getArtistNameOnlyFirst()))
                return;
            intent.putExtra("name", name);
        }
        intent.setAction(UPDATEBG);
        intent.putExtra("backg", retbit);
        intent.setFlags(5);

        Lewa.context().sendBroadcast(intent);
    }

    public static void SendtoUpdateFavourit(String path) {
        Bitmap retbit = BitmapFactory.decodeFile(path);
        if (retbit == null) {
            retbit = MusicUtils.getDefaultArtImg(Lewa.context());
        }
        Intent intent = new Intent();
        intent.setAction(UPDATEBG);
        intent.putExtra("backg", retbit);
        intent.setFlags(5);

        Lewa.context().sendBroadcast(intent);
    }

    public static void SendtoUpdateLRC(Context context, String title, String artistName, int state) {
        Intent intent = new Intent();
        intent.setAction(UPDATELRC);
        intent.putExtra("title", title);
        
        if(state == LyricDownloadListener.STATUS_SUCCESS) {
            String sdCardDir = Environment.getExternalStorageDirectory()
                        + Constants.SRC_PATH;
            File lrcfile = new File(sdCardDir + title + "-" + artistName + ".lrc");
            
            if(null == lrcfile || !lrcfile.exists()) {
                lrcfile = new File(sdCardDir + title + "-" + artistName + ".txt");
                lrcfile.renameTo(new File(lrcfile.toString().replaceAll(".txt", ".lrc")));
            }

            if(null == lrcfile || !lrcfile.exists()) {
                lrcfile = new File(sdCardDir + title + "-" + artistName + ".brc");
                lrcfile.renameTo(new File(lrcfile.toString().replaceAll(".brc", ".lrc")));
            }

            if(null == lrcfile || !lrcfile.exists()) {
                state =  LyricDownloadListener.STATUS_DOWNLOAD_FAIL;
                Toast.makeText(context, "dowanload lrc file sucess ! but we not found lrc file! ", Toast.LENGTH_SHORT).show();
            }
        }
        intent.putExtra("downStat", state);
        Lewa.context().sendBroadcast(intent);
    }
}
