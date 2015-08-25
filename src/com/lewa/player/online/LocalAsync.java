package com.lewa.player.online;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.RemoteException;

import com.lewa.Lewa;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.util.LewaUtils;

import java.io.File;

public class LocalAsync {

    private Context mContext;
    // private long preArtistId = -1;
    private int ifFavouritBG = 0;

    private static String preArtistName = "";
    private String artistName;
    private int w;
    private int h;

    public LocalAsync(Context context) {
        mContext = context;
    }

    public LocalAsync(Context context, int m) {
        mContext = context;
        ifFavouritBG = m;
    }

    public class bitmapandid {
        Bitmap b;
        long aid;
    }

    public class bitmapandname {
        Bitmap b;
        String aName;
    }

    // modify by zhaolei,120322,for artistImg save
    public void LocalArtistImg(String artistName) {
        if (artistName == null) {
            return;
        }
        // if(MediaStore.UNKNOWN_STRING.equals(artistName)) { //
        // preArtistName.equals(artistName)
        // return;
        // } else {
        // preArtistName = artistName;
        LocalImgAsync getLocalImg = new LocalImgAsync();
        getLocalImg.execute(artistName);
        // }
    }


    public class LocalImgAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
//            bitmapandname bitmapCacha = new bitmapandname();
            // InputStream isImg = null;
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String sdCardDir = Environment.getExternalStorageDirectory()
                        + LewaUtils.ARTIST_PATH;
                try {
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize = 1;
//                    options.inPurgeable = true;
//                    options.inInputShareable = true;
//                    options.inDither = false;
//                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    // modified by wangliqiang
                    String pathString = sdCardDir + arg0[0] + ".jpg";
                    File isImg = new File(pathString);
                    // bitmapCacha.b = BitmapFactory.decodeStream(isImg, null,
                    // null);
                    if (isImg.exists()) {
//                        OnlineLoader.setContext(mContext);
                        if (ifFavouritBG != 1) {
                            OnlineLoader.SendtoUpdate(pathString, arg0[0]);// modified by wangliqiang
                        } else {
                            OnlineLoader.SendtoUpdateFavourit(pathString);
                        }

                        return pathString;
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    // bitmapCacha.aName = arg0[0];
                    return arg0[0];
                }
                // finally {
                // try {
                // if(isImg != null)
                // isImg.close();
                // } catch (IOException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                // isImg = null;
                // }
            }
            // bitmapCacha.aName = arg0[0];
            return arg0[0];
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            // super.onPostExecute(result);
            File file = new File(result);
            if (!file.exists()) {
                downArtistImg(result);
            }
            try {
                this.finalize();
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // this.cancel(true);

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public void downArtistImg(String artistName) {
//        OnlineLoader.setContext(mContext);
        OnlineLoader.getArtistImg(artistName, ifFavouritBG, w, h);
    }

    public void setArtistImg(String artistName) {
        if (artistName != null) {
            try {
                IMediaPlaybackService service = Lewa.playerServiceConnector().service();
                if (service != null && service.getArtistName() != null && !service.getArtistName().equals(artistName))
                    return;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            artistName = MusicUtils.buildArtistName(artistName);
            LocalArtistImg(artistName);
        } else {
            downArtistImg(artistName);

        }
//        }
    }

    public void restorePreArtistName() {
        preArtistName = "";
    }
}
