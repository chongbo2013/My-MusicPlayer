package com.lewa.util;

/**
 * Created by wuzixiu on 1/11/14.
 */
public class Constants {
    public static final String SETTINGS_KEY = "SETTINGS";
    public static final int ON = 1;
    public static final int OFF = 0;
    public static final String SETTINGS_KEY_DOWNLOAD_LRC = "DOWNLOAD_LRC";
    public static final int SETTINGS_DOWNLOAD_LRC_DEFAULT = 1;
    public static final int SETTINGS_DOWNLOAD_LRC_ON = 1;
    public static final int SETTINGS_DOWNLOAD_LRC_OFF = 0;
    public static final String SETTINGS_KEY_DOWNLOAD_AVATAR = "DOWNLOAD_AVATAR";
    public static final int SETTINGS_DOWNLOAD_AVATAR_DEFAULT = 1;
    public static final int SETTINGS_DOWNLOAD_AVATAR_ON = 1;
    public static final int SETTINGS_DOWNLOAD_AVATAR_OFF = 0;
    public static final String SETTINGS_SCREEN_LIGHT = "SCREEN_LIGHT";
    public static final int SETTINGS_SCREEN_LIGHT_DEFAULT = 0;
    public static final int SETTINGS_SCREEN_LIGHT_ON = 1;
    public static final int SETTINGS_SCREEN_LIGHT_OFF = 0;
    public static final String SETTINGS_AUTO_PLAY = "AUTO_PLAY";
    public static final int SETTINGS_AUTO_PLAY_DEFAULT = 0;

    public static final String BITMAP = "BITMAP";
    public static final String PATH = "PATH";
    public static final String ARTIST_NAME = "ARTIST_NAME";

    public static final String FIRST_OPEN = "FIRST_OPEN";

    public static final String ACTION_PLAY_VIEWER = "com.lewa.player.PLAY_VIEWER";
    
    public static String SRC_PATH="/LEWA/music/lrc/";
    
    public static String DEFAULT_BITRATES="128";
    
    public static boolean show_down_tip = false;
    
    public static final String SEARCH_HISTORY_TABLE="search_history";
    public static final String SEARCH_HISTORY_ID="id";
    public static final String SEARCH_HISTORY_TEXT="text";
    
    public static final int DOWNLOAD_REMOVE_STATUS_SUCCESS = 0 ;
    public static final int DOWNLOAD_REMOVE_STATUS_FAIL = -1 ;
    public static final int DOWNLOAD_REMOVE_STATUS_PAUSE = 1 ;
    
  /**
   *  when download song in pending status,then stop wifi,the status will change to STATUS_HTTP_DATA_ERROR
   *  not change to paused status,so we use this to instead of it.baidu will fix this later.
   */
    public static final int DOWNLOAD_STATUS_STOP = 800 ;
}
