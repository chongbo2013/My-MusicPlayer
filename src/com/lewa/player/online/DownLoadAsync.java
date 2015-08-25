package com.lewa.player.online;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.RemoteException;

import com.baidu.music.model.LrcPic;
import com.baidu.music.model.LrcPicList;
import com.baidu.music.onlinedata.OnlineManagerEngine;
import com.baidu.music.onlinedata.SearchManager;
import com.baidu.music.onlinedata.SearchManager.LrcPicSearchListener;
import com.lewa.Lewa;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DownLoadAsync extends AsyncTask<String, String, Void> {

	@Override
	protected Void doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    /*public static final String ARTIST_PATH = "/LEWA/music/artist/";
    public static final String ALBUM_PATH = "/LEWA/music/album/";
    private boolean isAlbum = false;
    public int ifFavourit = 0;
    private Context mContext;

    // add by lqwang
    public DownLoadAsync(Context context, boolean isAlbum) {
        this.mContext = context;
        this.isAlbum = isAlbum;
    }

    protected Void doInBackground(String... params) {

        SearchDateListener searchDateListener = new SearchDateListener(
                params[0], params[1]);
        if (!isAlbum) {
            MusicUtils.getOnlineSearchDataManager(mContext)
                    .searchSingerPhotoAsync(params[0], searchDateListener);
        } else {
            MusicUtils.getOnlineSearchDataManager(mContext)
                    .searchAlbumPictureAsync(params[0], params[1],
                            searchDateListener);
        }
        return null;

    }

    protected void onPostExecute() {
        this.cancel(true);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }


    // add by lqwang
    private class SearchDateListener implements OnlineSearchDataManager.OnlineSearchDataResultListener {
        private String artistName;
        private String albumName;

        public SearchDateListener(String artist, String album) {
            this.artistName = artist;
            this.albumName = album;
        }

        @Override
        public void onGetSearchSuggestionComplete(ArrayList<String> arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSearchAlbumPictureComplete(String albumUrl) {
            // TODO Auto-generated method stub
//            new Job() {
//                @Override
//                public void run() {
            if (albumUrl == null) {
                if (!albumName.contains(mContext.getString(R.string.unknownwidget)) && !albumName.toLowerCase().contains("unknown")) {
                    Intent updataTokenIntent = new Intent(MusicUtils.UPDATE_TOKEN);
                    mContext.sendBroadcast(updataTokenIntent);
                }
                return;
            }
            Callback callback = new Callback(artistName, albumName);
            ImageManager.request(albumUrl, callback, 128, 256, 0,
                    false, false, ImageManager.TYPE_NONE);
//                }
//            }.start();
        }

        @Override
        public void onSearchLyricComplete(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSearchSingerPhotoComplete(String photoUrl) {
            // TODO Auto-generated method stub
//            new Job() {
//                @Override
//                public void run() {
            if (photoUrl == null) {
                if (!artistName.contains(mContext.getString(R.string.unknownwidget)) && !artistName.toLowerCase().contains("unknown")) {
                    Intent updataTokenIntent = new Intent(MusicUtils.UPDATE_TOKEN);
                    mContext.sendBroadcast(updataTokenIntent);
                }
                return;
            }
            Callback callback = new Callback(artistName, albumName);
            ImageManager.request(photoUrl, callback, 128, 256, 0,
                    false, false, ImageManager.TYPE_NONE);

//                }
//            }.start();
        }

        @Override
        public void onSearchSongComplete(SearchResultData arg0) {
            // TODO Auto-generated method stub

        }

    }

    private class Callback implements IImageLoadCallback {
        private String artistName;
        private String albumName;
        private String path = null;

        public Callback(String artist, String album) {
            this.artistName = artist;
            this.albumName = album;
        }

        @Override
        public void onLoad(final String url, final Image image) {
            new Thread() {
                public void run() {
                    if (!isAlbum) {
                        path = getArtistPath(artistName);
                        if (image != null) {
                            image.save(path);
                        }
                        try {
                            IMediaPlaybackService service = Lewa.playerServiceConnector().service();
                            if (service != null && service.getArtistName() != null && !service.getArtistName().contains(artistName)) {
                                return;
                            }
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (url == null || image == null) {
                            OnlineLoader.SendtoUpdate(null, artistName);
                        } else {
                            OnlineLoader.SendtoUpdate(path, artistName);
                            Intent intent = new Intent("com.lewa.player.updateDownloadImage");
                            mContext.sendBroadcast(intent);
                        }

                    } else {
                        path = getAlbumPath(albumName);
                        if (image != null) {
                            image.save(path);
                        }

                    }
                }

                ;
            }.start();

        }

    }

    public static String getArtistPath(String artist) {
        if (StringUtils.isBlank(artist)) {
            return null;
        } else {
            String cleanArtistName = MusicUtils.buildArtistName(artist);
            return Environment.getExternalStorageDirectory()
                    + ARTIST_PATH + cleanArtistName + ".jpg";
        }
    }


    public static String getAlbumPath(String album) {
        if (StringUtils.isBlank(album)) {
            return null;
        } else {
            String cleanAlbumName = MusicUtils.buildArtistName(album);
            return Environment.getExternalStorageDirectory()
                    + ALBUM_PATH + cleanAlbumName + ".jpg";
        }
    }*/

}
