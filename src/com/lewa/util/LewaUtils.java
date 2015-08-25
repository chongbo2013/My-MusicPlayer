package com.lewa.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.lewa.Lewa;
import com.lewa.player.MusicUtils;
import com.lewa.player.model.Playlist;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.lewa.player.R;
/**
 * Created by Administrator on 13-12-29.
 */
public class LewaUtils {

    public static final String PRE_SCAN_STATUS_KEY = "isScaned";

    public static final String HISTORY_PLAYLIST_ID = "history_playlist_id";
    public static final String HISTORY_PLAYLIST_TYPE = "history_playlist_type";
    public static final String HISTORY_PLAYLIST_NAME = "history_playlist_name";
    public static final String HISTORY_PLAYLIST_COVER = "history_playlist_cover";
    public static final String HISTORY_PLAYLIST_BDCODE = "history_playlist_bdcode";
    public static final String HISTORY_PLAYLIST_KEY = "history_playlist";

    public static final String CUR_PLAYLIST_ID = "cur_playlist_id";
    public static final String CUR_PLAYLIST_TYPE = "cur_playlist_type";
    public static final String CUR_PLAYLIST_NAME = "cur_playlist_name";
    public static final String CUR_PLAYLIST_COVER = "cur_playlist_cover";
    public static final String CUR_PLAYLIST_KEY = "cur_playlist";
    private static boolean DEBUG=true;
    
    public static void logE(String TAG,String msg){
    	if(DEBUG)
    		Log.e(TAG, msg);
    }

	public static void logI(String TAG,String msg){
    	if(DEBUG)
    		Log.e(TAG, msg);
    }
	
    public static Playlist getPlayHistory() {
        try {
            String jsonData = Lewa.getPersistPreferences().getString(HISTORY_PLAYLIST_KEY, "{}");
            JSONObject json = new JSONObject(jsonData);
            if(json.length() == 0) {
                return null;
            } else {
                Playlist playlist = new Playlist();
                if(json.has(HISTORY_PLAYLIST_ID)) {
                    playlist.setId(json.getLong(HISTORY_PLAYLIST_ID));
                }
                if(json.has(HISTORY_PLAYLIST_TYPE)) {
                    playlist.setType(Playlist.TYPE.valueOf(json.getString(HISTORY_PLAYLIST_TYPE)));
                }
                if(json.has(HISTORY_PLAYLIST_NAME)) {
                    playlist.setName(json.getString(HISTORY_PLAYLIST_NAME));
                }
                if(json.has(HISTORY_PLAYLIST_COVER)) {
                    playlist.setCoverUrl(json.getString(HISTORY_PLAYLIST_COVER));
                }
                return playlist;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updatePlayHistory(Playlist playlist) {
        if(playlist == null) return;
        SharedPreferences.Editor editor = Lewa.getPersistPreferences().edit();
        Map<String, Object> playlistMap = new HashMap<String, Object>();
        playlistMap.put(HISTORY_PLAYLIST_ID, playlist.getId());
        playlistMap.put(HISTORY_PLAYLIST_TYPE, playlist.getType().name());
        playlistMap.put(HISTORY_PLAYLIST_NAME, playlist.getName());
        playlistMap.put(HISTORY_PLAYLIST_COVER, playlist.getCoverUrl());
        JSONObject json = new JSONObject(playlistMap);

        editor.putString(HISTORY_PLAYLIST_KEY, json.toString());
        editor.commit();
    }

    public static Playlist getCurPlaylist() {
        try {
            String jsonData = Lewa.getPersistPreferences().getString(CUR_PLAYLIST_KEY, "{}");
            JSONObject json = new JSONObject(jsonData);
            if(json.length() == 0) {
                return null;
            } else {
                Playlist playlist = new Playlist();
                if(json.has(CUR_PLAYLIST_ID)) {
                    playlist.setId(json.getLong(CUR_PLAYLIST_ID));
                }
                if(json.has(CUR_PLAYLIST_TYPE)) {
                    playlist.setType(Playlist.TYPE.valueOf(json.getString(CUR_PLAYLIST_TYPE)));
                }
                if(json.has(CUR_PLAYLIST_NAME)) {
                    playlist.setName(json.getString(CUR_PLAYLIST_NAME));
                }
                if(json.has(CUR_PLAYLIST_COVER)) {
                    playlist.setCoverUrl(json.getString(CUR_PLAYLIST_COVER));
                }
                if(json.has(HISTORY_PLAYLIST_BDCODE)) {
                    playlist.setCoverUrl(json.getString(HISTORY_PLAYLIST_BDCODE));
                }
                return playlist;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateCurPlaylist(Playlist playlist) {
        if(playlist == null) return;
        SharedPreferences.Editor editor = Lewa.getPersistPreferences().edit();
        Map<String, Object> playlistMap = new HashMap<String, Object>();
        playlistMap.put(CUR_PLAYLIST_ID, playlist.getId());
        playlistMap.put(CUR_PLAYLIST_TYPE, playlist.getType().name());
        playlistMap.put(CUR_PLAYLIST_NAME, playlist.getName());
        playlistMap.put(CUR_PLAYLIST_COVER, playlist.getCoverUrl());
        playlistMap.put(HISTORY_PLAYLIST_BDCODE, playlist.getBdCode());
        JSONObject json = new JSONObject(playlistMap);

        editor.putString(CUR_PLAYLIST_KEY, json.toString());
        editor.commit();
    }

    public static boolean getScanStatus() {
        return Lewa.getPersistPreferences().getBoolean(PRE_SCAN_STATUS_KEY, false);
    }

    public static void saveScanStatus(boolean value) {
        SharedPreferences.Editor editor = Lewa.getPersistPreferences().edit();
        editor.putBoolean(PRE_SCAN_STATUS_KEY, value);
        editor.commit();
    }
    
    public static boolean isSDcardMounted(){
    	String state=Environment.getExternalStorageState();
    	return state.equals(Environment.MEDIA_MOUNTED);
    }
 public static String getExternalPath(String path){
    	if(path!=null){
    		return Environment.getExternalStorageDirectory()+path;
    	}
    	return path;
    }
    
    public static String getArtistPicPath(String artist) {
        if (StringUtils.isBlank(artist)) {
            return null;
        } else {
            String cleanArtistName = MusicUtils.buildArtistName(artist);
            return Environment.getExternalStorageDirectory()
                    + ARTIST_PATH + cleanArtistName + ".jpg";
        }
    }
    
    public static boolean isNameUnknown(String name){
    	if(!StringUtils.isBlank(name))
    		return name.equalsIgnoreCase("<unknown>");
    	return true;
    }
    
    public static SpannableStringBuilder highlight(Context context,String s,String filter){
        SpannableStringBuilder spannableStringBuilder=new SpannableStringBuilder(s);
        if(!TextUtils.isEmpty(filter)){
	        String sString=s.toLowerCase().trim();
	        String filterString=filter.toLowerCase().trim();
	        if(sString.contains(filterString)){
	            int start=sString.indexOf(filterString);
	            int end=start+filterString.length();
	            spannableStringBuilder.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.white_text)),     //R.color.blue_text
                                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	        }
        }
        return spannableStringBuilder;
    }

    public static int calculteFastIndexPaddingTop(Context context) {
        final float PADDING_TOP_DP = 25.0f;
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(PADDING_TOP_DP * scale + 0.5f);
    }
    
    /**
     * baidu jar return path may end with null. so,use this to change it
     * @param path
     * @return
     */
    public static String checkPathSuffix(String path){
    	try {
			if(!TextUtils.isEmpty(path)){
				if(path.endsWith(".null")){
					File file = new File(path);
					if(file.exists()){
						path = path.replace(".null", ".mp3");
						file.renameTo(new File(path));
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return path;
    }
    public static final String ARTIST_PATH = "/LEWA/music/artist/";
}
