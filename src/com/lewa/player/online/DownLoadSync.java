package com.lewa.player.online;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;

import java.util.List;

public class DownLoadSync {
   /* public static final String ARTIST_PATH = "/LEWA/music/artist/";
    public static final String ALBUM_PATH = "/LEWA/music/album/";
    public static final String LRC_PATH="/LEWA/music/lrc";
    private boolean isAlbum;
    private Context mContext;
    private OnlineManagerEngine onlineManagerEngine;
    private OnlineSearchDataManager searchDataManager;
    private SearchResultData resultData;
    private SQLiteDatabase ldb;
    private LyricManager lrcManager;
    private List<SearchResultSong> list;
    public DownLoadSync(Context context){
        this.mContext=context;
        onlineManagerEngine = MusicUtils.getEngine(mContext);
        searchDataManager = onlineManagerEngine.getOnlineSearchDataManager(mContext);
        lrcManager = onlineManagerEngine.getLyricManager(mContext);
    }
    
    public String downLoadImg(String artist,String album,boolean isAlbum){
        this.isAlbum=isAlbum;
        String photoUrl=null;
        if(isAlbum){
            photoUrl=searchDataManager.searchAlbumPictureSync(album, artist);
        }else{ 
            photoUrl =searchDataManager.searchSingerPhotoSync(artist);
        }
        if(photoUrl!=null&&!photoUrl.equals("")){
            Callback callback = new Callback(artist,album);
            ImageManager.request(photoUrl, callback, 128, 256, 0,
                            false, false, ImageManager.TYPE_DEFAULT_RECT_IMAGE);
            return photoUrl;
        }else {
            return null;
        }
        
    }
    
    public void downLoadLrc(String songName,String songArtist){
        lrcManager.getLyricFile(songName, songArtist, Environment.getExternalStorageDirectory()+LRC_PATH);
//        LRC_COUNT++;
        if (songName != null) {
            resultData = onlineManagerEngine
                    .getOnlineSearchDataManager(mContext)
                    .searchSongSync(songName, 1, 10);
        }
        if (resultData != null && resultData.mItems != null
                && resultData.mItems.size() > 0) {
            list = resultData.mItems;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).mAuthor
                        .equalsIgnoreCase(songArtist)) {
                    String albumId = list.get(i).mAlbumId;
                    AlbumListItemData albumListItemData =onlineManagerEngine
                            .getOnlineAlbumDataManager(mContext)
                            .getAlbumSongListSync(mContext,
                                    Integer.parseInt(albumId));
                    if (albumListItemData == null) {
                        continue;
                    }

                    List<AlbumListItemSongData> songDatas = albumListItemData.mItems;
                    String albumid = albumListItemData.mAlbumId;
                    String author = albumListItemData.mAuthor;
                    String albumtitle = albumListItemData.mTitle;
                    String songstotal = albumListItemData.mSongsTotal;
                    String publishtime = albumListItemData.mPublishTime;
                    String info = albumListItemData.mInfo;
                    StringBuilder sb = new StringBuilder();
                    if (songDatas != null && songDatas.size() > 0) {
                        for (int j = 0; j < songDatas.size(); j++) {
                            String songtitle = songDatas.get(j).mTitle;
                            if (j == 0) {
                                sb.append("01 " + songtitle);
                            } else if (j < 9) {
                                sb.append(",0" + (j + 1) + " "
                                        + songtitle);
                            } else {
                                sb.append("," + (j + 1) + " "
                                        + songtitle);
                            }

                        }
                        String[] infos = new String[] { albumid,
                                albumtitle, songstotal,
                                publishtime, sb.toString(), author,
                                info };
                        String songstitle = sb.toString();
                        //TODO: save album, and artist maybe
//                        DBService.saveAlbum()
//                        if(dbHelper!=null){
//                            if(albumid!=null){
//                                dbHelper.addAlbumDetail(infos);
////                                ALBUNINFO_COUNT++;
//                            }
////                            long songid=MusicUtils.getSongId(mContext, songName);
////                            if(String.valueOf(songid)!=null&&albumId!=null)
////                                dbHelper.addSongAlbum(String.valueOf(songid),
////                                albumId);
//                        }
                        break;
                    } 
                }
           }
           
        }
    }
    public void downLoadInfo(String artist,String album,boolean isAlbum){
        this.isAlbum=isAlbum;
        if(isAlbum)
        {       resultData = searchDataManager.searchSongSync(album, 1, 1);
                saveAlbumInfo(resultData);   
//            searchDataManager.searchSongAsync(album, 1, 1,albumListener);
        }else{
//            searchDataManager.searchSongAsync(artist, 1, 1,artistListener); 
            resultData=searchDataManager.searchSongSync(artist, 1, 1);
            saveArtistInfo(resultData);
        }
        if(resultData!=null){
            int errorcode=resultData.getErrorCode();
            if(errorcode==101||errorcode==102||errorcode==110){
                Intent updataTokenIntent=new Intent(MusicUtils.UPDATE_TOKEN);
                if(mContext!=null)
                    mContext.sendBroadcast(updataTokenIntent);
             }
        }
    }
    
    private class Callback implements IImageLoadCallback {
        private String artistName;
        private String albumName;
        private String path=null;
        public Callback(String artist, String album) {
            this.artistName = artist;
            this.albumName = album;
        }

        @Override
        public void onLoad(String url, Image image) {
            if (!isAlbum) {
               path = Environment.getExternalStorageDirectory()
                        + ARTIST_PATH + artistName + ".jpg";
                if (image != null)
                    image.save(path);
                if (url == null || image == null) {
                    OnlineLoader.SendtoUpdate(null, null);
                }else {
                    OnlineLoader.SendtoUpdate(path, artistName);
                }
            }else{
                path=Environment.getExternalStorageDirectory()+ALBUM_PATH+albumName+".jpg";
                if(image!=null)
                    image.save(path);
            }
        }
    }
      
    private void saveAlbumInfo(SearchResultData searchResultData){
        if (searchResultData != null
                && searchResultData.mAlbumInfo != null) {

            String albumid = searchResultData.mAlbumInfo.mAlbumId;
            AlbumListItemData lists = onlineManagerEngine
                            .getOnlineAlbumDataManager(
                                    mContext)
                            .getAlbumSongListSync(
                                    mContext,
                                    Integer.parseInt(albumid));
                    if (lists != null&&lists.getItems()!=null) {
                        String albumtitle = lists.mTitle;
                        String songstotal = lists.mSongsTotal;
                        String publishtime = lists.mPublishTime;
                        String author = lists.mAuthor;
                        String info = lists.mInfo;
                        List<AlbumListItemSongData> songs = lists
                                .getItems();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < songs
                                .size(); i++) {
                            String songtitle = songs
                                    .get(i).mTitle;
                            if (i == 0) {
                                sb.append("01 "
                                        + songtitle);
                            } else if (i < 9) {
                                sb.append(",0"
                                        + (i + 1) + " "
                                        + songtitle);
                            } else {
                                sb.append("," + (i + 1)
                                        + " "
                                        + songtitle);
                            }
                        }
                        String songstitle = sb.toString();
                        String[] infos = new String[] {
                                albumid, albumtitle,
                                songstotal,
                                publishtime,
                                sb.toString(), author,
                                info };
                        //TODO: save album
//                        dbHelper.addAlbumDetail(infos);
                    } 
          
        } 
    }
    private void saveArtistInfo(SearchResultData searchResultData){
        String artistid=null;
        if (resultData == null) {
            return;
        }
        if (resultData.mArtistInfo != null)
           artistid = resultData.mArtistInfo.mArtistId;
        if (artistid == null){
            return;
            }
        SingerDetails details = onlineManagerEngine
                        .getOnlineSingerDataManager(mContext)
                        .getSingerDetailsSync(mContext,
                                Integer.parseInt(artistid));
                if (details != null) {
                    String name = details.mName;
                    String albumstotal = details.mAlbumsTotal;
                    String area=null;
                    if(details.mArea!=null&&!details.mArea.equals("")&&mContext!=null){
                      
                    if (Integer.parseInt(details.mArea) == 0) {
                        area =mContext.getString(R.string.inland);
                    } else if (Integer.parseInt(details.mArea) == 1) {
                        area = mContext.getString(R.string.hk_tw);
                    } else if (Integer.parseInt(details.mArea) == 2) {
                        area = mContext.getString(R.string.west);
                    } else if (Integer.parseInt(details.mArea) == 3) {
                        area = mContext.getString(R.string.japan_korea);
                    } else {
                        area = mContext.getString(R.string.other);
                    }
                    String bloodtype = details.mBloodType;
                    String company = details.mCompany;
                    String country = details.mCountry;
                    String height = details.mHeight;
                    String intro = details.mIntro;
                    String birthday = details.mSingerBirth;
                    String star = details.mSingerStar;
                    String songstotal = details.mSongsTotal;
                    String weight = details.mWeight;
                    if(area==null)
                        area="";
                    String[] infos = new String[] { name,
                            albumstotal, area, bloodtype,
                            company, country, height, intro,
                            birthday, star, songstotal, weight };
                        //TODO: save album
//                        dbHelper.addArtistDetail(infos);
////                        ARTISTINFO_COUNT++;
                } 
            }
    }*/
}
