package com.lewa.player.online;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.db.DBService;
import com.lewa.util.LewaUtils;

import java.io.File;
import java.util.ArrayList;

public class DownLoadAllPicsAsync extends AsyncTask<Object, String, Integer>{
	
	public static final String ARTIST_PATH = "/LEWA/music/artist/";
	public static final String ALBUM_PATH = "/LEWA/music/album/";
	private String sDPath=Environment.getExternalStorageDirectory().getAbsolutePath();
	private boolean isAlbum;
	private int mCount = 0;
	private Context mContext;
	private boolean mIsStop = false;
    private File file;
	
	//private bitmapandString 
	@Override
	protected Integer doInBackground(Object... params) {
		// TODO Auto-generated method stub
	    mContext = (Context) params[1];
	    String flag = (String) params[2];
	    if ("1".equals(flag)) {
	        isAlbum = true;
	    } else {
	        isAlbum = false;
	    }
	    
	    if(mIsStop) {
	        mCount = 0;
	    } else {
	        mCount = getDownloadPicCount((Cursor) params[0], mContext);
	    }
        return mCount;
	}

    @Override
	protected void onPostExecute(Integer result) {
		// TODO Auto-generated method stub	    
	    
	    Intent intent = new Intent();
	    intent.setAction(OnlineLoader.STOPDOWNLOAD);
	    intent.putExtra("count", result);
	    if(isAlbum) {
	        intent.putExtra("isalbum", OnlineLoader.ALBUMDOWNLOAD);
	    } else {
	        intent.putExtra("isalbum", OnlineLoader.ARTISTDOWNLOAD);
	    }
	    mContext.sendBroadcast(intent);

	    this.cancel(true);
	} 
    
    private int getDownloadPicCount(Cursor cursor, Context context) {
        if(cursor.isClosed())
            return 0;
        int cursorCount = cursor.getCount();
        if(cursorCount <= 0) {
            return 0;
        }
             
        ArrayList<String> downloadNameList = new ArrayList<String>();
        ArrayList<String> artistNameList = new ArrayList<String>();
        String unknownArtist = context.getString(R.string.unknown_artist_name);
        String unknownAlbum = context.getString(R.string.unknown_album_name);
        boolean unknown;        
                   
        cursor.moveToFirst();
        while (!cursor.isClosed()&&!cursor.isAfterLast()) {
            if (mIsStop) {
                return 0;
            }
            if (isAlbum) {
                String albumArt = cursor.getString(cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                String albumName = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
                String artistName = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
                unknown = unknownArtist.equals(artistName) || unknownAlbum.equals(albumName)
                        || "<unknown>".equals(artistName) || "<unknown>".equals(albumName)
                        || "".equals(artistName) || "".equals(albumName);
                    
                if (albumArt == null && !unknown && !downloadNameList.contains(albumName)) {
                    downloadNameList.add(albumName);
                    artistNameList.add(artistName);
                }
                
            } else {
                String artistName = cursor.getString(cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));
                artistName= MusicUtils.buildArtistName(artistName);
//                Bitmap bm = MusicUtils.getLocalBitmap(context, artistName,
//                        Environment.getExternalStorageDirectory().getAbsolutePath()+DownLoadAsync.ARTIST_PATH);
                String path=LewaUtils.getArtistPicPath(artistName);
                File file=new File(path);
                unknown = unknownArtist.equals(artistName) 
                        || "<unknown>".equals(artistName) || "".equals(artistName) ;
                if (!file.exists() && !unknown) {                    
                    downloadNameList.add(artistName);
                 // modified by wangliqiang   
                 // artistNameList.add("-2222");
                    artistNameList.add(artistName);
                }                
            }
            cursor.moveToNext();
        }
      
        int listSize = downloadNameList.size();
        int albumCount = 0;
        int artistCount = 0;
//        DownLoadSync downLoadSync =new DownLoadSync(mContext);
        for(int i = 0; i < listSize; i++) {
            if(mIsStop) {
                break;
            }
            Intent intent = new Intent();
            intent.setAction(OnlineLoader.GET_PIC_ACTION);
            if(isAlbum) {
                file = new File(sDPath+ALBUM_PATH+downloadNameList.get(i)+".jpg");
                if(!file.exists()){
                    /*String url=downLoadSync.downLoadImg(artistNameList.get(i), downloadNameList.get(i),isAlbum);
                    downLoadSync.downLoadInfo(artistNameList.get(i),downloadNameList.get(i),isAlbum);*/
//                    if(url!=null)
                        albumCount++;
                    intent.putExtra("isalbum", OnlineLoader.ALBUMDOWNLOAD);
                    intent.putExtra("count", albumCount);
                    mContext.sendBroadcast(intent);
                }
            } else {
//                urlCode = "SearchArtist";
                file=new File(sDPath+ARTIST_PATH+artistNameList.get(i)+".jpg");
                if(!file.exists()){
                   /* String url=downLoadSync.downLoadImg(artistNameList.get(i),downloadNameList.get(i),isAlbum);
                    downLoadSync.downLoadInfo(artistNameList.get(i),downloadNameList.get(i),isAlbum);*/
                    String[] songlist=null;
                    if(artistNameList.get(i)!=null&&!artistNameList.get(i).contains("?"))
                        songlist= DBService.getSongListForArtist(mContext, artistNameList.get(i));
                    if(songlist!=null){
                    for(int j=0;j<songlist.length;j++){
//                        downLoadSync.downLoadLrc(songlist[j],artistNameList.get(i));
                    }
                    }
//                    if(url!=null)
                        artistCount++;
                    intent.putExtra("isalbum", OnlineLoader.ARTISTDOWNLOAD);
                    intent.putExtra("count", artistCount);
                    mContext.sendBroadcast(intent);
                }
            }
          
            
        }
        return albumCount + artistCount;
    }
/*
    private bitmapandString downImg(String requestUrl, String albumName, String artistName) {

        Bitmap bitmap = null;
        bitmapandString bundle = new bitmapandString();
        URL url;
        
        String ImageUrl = null;
        try {
            String startfix = requestUrl.substring(0, requestUrl.indexOf("keyword=") + 8);
            String keyword = requestUrl.substring(requestUrl.indexOf("keyword=") + 8, requestUrl.indexOf("&api_sig"));
            String encodeUrl = URLEncoder.encode(keyword);
            String strurl = startfix + encodeUrl + requestUrl.substring(requestUrl.indexOf("&api_sig"), requestUrl.length());
            url = new URL(strurl);
            
            URLConnection conn = url.openConnection();
            
            try {
                // Get the response
                StringBuilder builder = new StringBuilder();
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                for (String s = rd.readLine(); s != null; s = rd.readLine()) { 
                    builder.append(s); 
                }
            
                JSONObject jsonObject = new JSONObject(builder.toString());
                String rsp = jsonObject.getString("rsp"); 
                JSONObject jsonImg = new JSONObject(rsp);                  
                
                if(isAlbum) {
                    String imgInfo = jsonImg.getString("Album"); 
                    JSONArray imgInfoarray = new JSONArray(imgInfo);
                    
                    int count = imgInfoarray.length();
                    for(int i = 0; i < count;i++) {
                        String albumImageUrl = imgInfoarray.get(i).toString();
                        JSONObject jsonImageUrl = new JSONObject(albumImageUrl); 
                        String ArtistName = jsonImageUrl.getString("ArtistNameList");                                       
                        
                        if(ArtistName != null && ArtistName.equals(artistName)) {
                            albumImageUrl = imgInfoarray.get(i).toString();
                            ImageUrl = jsonImageUrl.getString("SmallImageUrl");
                            break;
                        }                       
                    } 
                } else {                    
                    String artistInfo = jsonImg.getString("Artist"); 
                    JSONArray artistInfoarray = new JSONArray(artistInfo);
                    String artistImageUrl = artistInfoarray.get(0).toString();  
                    JSONObject jsonImageUrl = new JSONObject(artistImageUrl); 
                    ImageUrl = jsonImageUrl.getString("ImageUrl"); 
                }
                
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                bundle.albumbitmap = null;
                return bundle;
            } 

            URL coverUrl = new URL(ImageUrl);
            HttpURLConnection con = (HttpURLConnection) coverUrl.openConnection();
            con.setDoInput(true);
            con.connect();
            InputStream inputStream = con.getInputStream();
            
            bitmap = BitmapFactory.decodeStream(inputStream); 
            inputStream.close();
            
            
            bundle.albumbitmap = bitmap;
            bundle.artistName = albumName;
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            bundle.albumbitmap = null;
            return bundle;
        } finally {
            
        }
 
        return bundle;
    }
    
    private boolean bitmaptoSDcard(bitmapandString albumtofile) {

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {   
            String pathStr = "";
            if(isAlbum) {
                pathStr = ALBUM_PATH;
            } else {
                pathStr = ARTIST_PATH;
            }
            String sdCardDir = Environment.getExternalStorageDirectory() + pathStr;
            File file = new File(sdCardDir, albumtofile.artistName);
            file.getParentFile().mkdirs();
            
            if(!file.exists()) {
                try {
                    file.createNewFile();                   
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try 
                {
                    FileOutputStream out = new FileOutputStream(file);
                    if(albumtofile.albumbitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
                    {
                        out.flush();
                        out.close();
                        return true;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }                
            }            
        }
        return false;
    }
	*/
    public void setStopFlag(boolean flag) {
        mIsStop = flag;
    }
    
    public boolean getStopFlag() {
        return mIsStop;
    }
}
