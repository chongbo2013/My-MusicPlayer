package com.lewa.player.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.lewa.player.R;
import com.lewa.player.activity.PlayActivity;
import com.lewa.player.activity.SongHoundActivity;

import 	android.graphics.Bitmap;
import  android.graphics.Canvas;
import 	android.graphics.Rect;
import 	android.graphics.Paint;
import 	java.lang.Exception;
import 	android.graphics.Bitmap.Config;
import android.graphics.PorterDuffXfermode;
import 	android.graphics.PorterDuff.Mode;
import 	android.graphics.RectF;
import android.view.View;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import android.content.res.Resources;
import android.content.ComponentName;
import android.os.Environment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.lewa.player.online.DownLoadAllPicsAsync;

import com.lewa.util.Constants;
import android.graphics.BitmapFactory;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.MediaStore;

import com.lewa.player.activity.PlayActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import 	android.graphics.Matrix;
import com.lewa.view.lyric.Sentence;
import com.lewa.view.lyric.Lyric;
import com.lewa.view.lyric.PlayListItem;
import com.baidu.music.onlinedata.LyricManager.LyricDownloadListener;
import com.lewa.util.LewaUtils;


import java.io.File;
import com.lewa.player.widget.WidgetUtils;
import com.lewa.player.online.OnlineLoader;
import com.lewa.Lewa;


/**
 * Created by Administrator on 13-11-24.
 */
public class Widget4x3 extends AppWidgetProvider {
	private static final String TAG = "Widget4x3";
	
	

	private static Widget4x3 sInstance = null;
	private Bitmap artistBm = null;
	
	private String artistName= null;
	private String showTitleName = null;
	private String showArtistName = null;

	private String titleName = null;
	private static Boolean isUpdate = false; //type is static because onEnabled() fun modify isUpdate`s value but isUpdate`s value not modify in performUpdate() 
	private String preLyr = null;
	private static String curLyr = null;
	private String nextLyr = null;
	
	private static String lastArtistName = null;
	
	public static synchronized Widget4x3 getInstance() {
        if (sInstance == null) {
            sInstance = new Widget4x3();
        }
        return sInstance;
    }
	
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
		
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
		SharedPreferences sp=context.getSharedPreferences("Music", Context.MODE_PRIVATE);
        Editor editor =sp.edit();
        editor.putBoolean("is4X3WidgetAdded", false).commit();
        MediaPlaybackService.isWidget4X3Added=false;
		isUpdate = false;
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
		SharedPreferences sp=context.getSharedPreferences("Music", Context.MODE_PRIVATE);
        Editor editor =sp.edit();
        editor.putBoolean("is4X3WidgetAdded", true).commit();
        MediaPlaybackService.isWidget4X3Added=true;
		isUpdate = true;
    }

	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                             int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		LewaUtils.logI(TAG, "--onAppWidgetOptionsChanged--");
		isUpdate = true;
	}


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
		LewaUtils.logI(TAG, "onReceive action:" + intent.getAction());
        if(intent.getAction().equals("com.imhipi.music.widget.4x3")) {
            RemoteViews appWidgetView = new RemoteViews(context.getPackageName(), R.layout.widget_4x3_layout);
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, Widget4x3.class), appWidgetView);
        }

		if(intent.getAction().equals("action:android.intent.action.LOCALE_CHANGED")) {
		//	isUpdate = true;
		}

		if (intent.getAction().equals(OnlineLoader.UPDATELRC)) {
            int stat = intent.getIntExtra("downStat", -1);
			//LewaUtils.logI(TAG, "update lrc state : " + stat);
            if (stat == LyricDownloadListener.STATUS_SUCCESS) {
                isUpdate = true;	//update lrc file
                
            } else {	//download lrc fail
				RemoteViews appWidgetView = new RemoteViews(context.getPackageName(), R.layout.widget_4x3_layout);
				curLyr = setLyricNOlrc(context, 1);
				appWidgetView.setTextViewText(R.id.lru_cur, curLyr);
				AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, Widget4x3.class), appWidgetView);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
		LewaUtils.logI(TAG, "onUpdate");
        defaultAppWidget(context, appWidgetIds);
        
        Intent serviceIntent = new Intent(context, MediaPlaybackService.class);
        context.startService(serviceIntent);
        
        // Send broadcast intent to any running MediaPlaybackService so it can
        // wrap around with an immediate update.
        Intent updateIntent = new Intent(MediaPlaybackService.SERVICECMD);
        updateIntent.putExtra(MediaPlaybackService.CMDNAME,
                MusicUtils.CMDAPPWIDGETUPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);

		isUpdate = true;
    }

	 /**
     * Initialize given widgets to default state, where we launch Music on default click
     * and hide actions if service not running.
     */
    private void defaultAppWidget(Context context, int[] appWidgetIds) {
        final Resources res = context.getResources();
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_4x3_layout);

        views.setViewVisibility(R.id.widget_artistname, View.GONE);

		views.setViewVisibility(R.id.widget_trackname, View.VISIBLE);
        if(MusicUtils.mHasSongs) {
        	views.setTextViewText(R.id.widget_trackname, context.getText(R.string.click_to_shuffle));
        } else {
        	views.setTextViewText(R.id.widget_trackname, context.getText(R.string.phone_no_songs));
        }

		useDefBitmap(context.getResources(), views);
        
        views.setTextViewText(R.id.widget_currenttime, "0:00");
        views.setTextViewText(R.id.widget_endtime, "0:00");
        views.setProgressBar(R.id.widget_progressbar, 1000, 0, false);
                
        linkButtons(context, views, false /* not playing */);
        pushUpdate(context, appWidgetIds, views);
    }
	
	private void pushUpdate(Context context, int[] appWidgetIds, RemoteViews views) {
        // Update specific list of appWidgetIds if given, otherwise default to all
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        /*if (appWidgetIds != null) {
			Log.i(TAG, "111111");
            gm.updateAppWidget(appWidgetIds, views);
        } else {
        	Log.i(TAG, "22222");
            gm.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }*/
        gm.updateAppWidget(new ComponentName(context, this.getClass()), views);
    }
	
	private void linkButtons(Context context, RemoteViews views, boolean playerActive) {
		//Log.i(TAG, "linkButtons");
        // Connect up various buttons and touch events
        Intent intent;
        PendingIntent pendingIntent;
        
        final ComponentName serviceName = new ComponentName(context, MediaPlaybackService.class);
        
        intent = new Intent(context, PlayActivity.class);
        //intent.putExtra("collapse_statusbar", true);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                //| Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
		bundle.putBoolean(PlayActivity.FROM_WIDGET, true);
		intent.putExtras(bundle);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		//views.setOnClickPendingIntent(R.id.widget_album, pendingIntent);
        views.setOnClickPendingIntent(R.id.view_a, pendingIntent);

        intent = new Intent(MediaPlaybackService.PREVIOUS_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_prev, pendingIntent);
		  
        intent = new Intent(MediaPlaybackService.TOGGLEPAUSE_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.widget_play, pendingIntent);
        
        intent = new Intent(MediaPlaybackService.NEXT_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.widget_next, pendingIntent);        

		intent = new Intent(MediaPlaybackService.TOGGLE_LYR_ACTION);
		intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.lyr_ctl, pendingIntent); 
    }

	 /**
     * Check against {@link AppWidgetManager} if there are any instances of this widget.
     */
    private boolean hasInstances(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, this.getClass()));
        return (appWidgetIds.length > 0);
    }

    /**
     * Handle a change notification coming over from {@link MediaPlaybackService}
     */
    public void notifyChange(MediaPlaybackService service, String what) {
        if (hasInstances(service)) {
            if (MediaPlaybackService.META_CHANGED.equals(what) ||
                    MediaPlaybackService.PLAYSTATE_CHANGED.equals(what)) {
                artistBm = null;
                performUpdate(service, null);
            }
        }
    }

	/**
     * Update all active widget instances by pushing changes 
     */
    public void performUpdate(MediaPlaybackService service, int[] appWidgetIds) {     	
		//LewaUtils.logI(TAG, "performUpdate");
		
        final Resources res = service.getResources();
        final RemoteViews views = new RemoteViews(service.getPackageName(), R.layout.widget_4x3_layout);
        
        titleName = service.getTrackName();
        artistName = service.getArtistName();
        
        showTitleName = titleName;
        showArtistName = artistName;
        CharSequence errorState = null;
        String title_blank = service.getString(R.string.title_blank);
        String artist_blank = service.getString(R.string.artist_blank);
        
        int len = 0;
                
        // Format title string with track number, or show SD card message
        String status = Environment.getExternalStorageState();
		
        if (status.equals(Environment.MEDIA_SHARED) ||
                status.equals(Environment.MEDIA_UNMOUNTED) || 
                status.equals(Environment.MEDIA_REMOVED)) {
            
            if (android.os.Environment.isExternalStorageRemovable()) {
                errorState = res.getText(R.string.no_sdcard_title_text);
            } else {
                errorState = res.getText(R.string.nousb_title);
            }
        } else if (titleName == null || (!MusicUtils.mHasSongs && !MediaPlaybackService.isOnlinePlay)) { //&& !MediaPlaybackService.isOnlinePlay add by sjxu for bug 52127
        	if(MusicUtils.mHasSongs) {
        		errorState = res.getText(R.string.click_to_shuffle);
        	} else {
        		errorState = res.getText(R.string.phone_no_songs);
        	}
            views.setTextViewText(R.id.widget_currenttime, "0:00");
            views.setTextViewText(R.id.widget_endtime, "0:00");
            views.setProgressBar(R.id.widget_progressbar, 1000, 0, false);
        }
        //modified by wangliqiang
        if (errorState != null && MusicUtils.isSdMounted) {
            views.setViewVisibility(R.id.widget_trackname, View.VISIBLE);
            views.setViewVisibility(R.id.widget_artistname, View.GONE);

			views.setTextViewText(R.id.widget_trackname, errorState);
			views.setTextViewText(R.id.widget_currenttime, "0:00");
            views.setTextViewText(R.id.widget_endtime, "0:00");
			useDefBitmap(service.getResources(), views);
			
        }else if(errorState != null && !MusicUtils.isSdMounted){
        	views.setViewVisibility(R.id.widget_trackname, View.VISIBLE);
			views.setViewVisibility(R.id.widget_artistname, View.GONE);
			
          	views.setTextViewText(R.id.widget_currenttime, "0:00");
			views.setTextViewText(R.id.widget_endtime, "0:00");
			views.setTextViewText(R.id.widget_trackname, errorState);
			
			if(!errorState.equals(res.getText(R.string.nosdcard_title))) {							
				useDefBitmap(service.getResources(), views);
			}
			MusicUtils.isSdMounted=true;
        } else if(errorState ==null) {
            // No error, so show normal titles
            views.setViewVisibility(R.id.widget_trackname, View.VISIBLE);
            views.setViewVisibility(R.id.widget_artistname, View.VISIBLE);

            views.setTextViewText(R.id.widget_trackname, showTitleName);
			if (MediaStore.UNKNOWN_STRING.equals(showArtistName)) {
				views.setTextViewText(R.id.widget_artistname, Lewa.context()
						.getString(R.string.unknown_artist_name));
			} else {
				views.setTextViewText(R.id.widget_artistname, showArtistName);
			}
  
            Context context=service.getApplicationContext();
            String currentTime =  MusicUtils.makeTimeString(context, service.position() / 1000);
            String totalTime = MusicUtils.makeTimeString(context, service.duration() / 1000);
            int progress = 0;
            if(service.duration() != 0) {
                progress = (int) (service.position() * 1000 / service.duration());
            }
            views.setTextViewText(R.id.widget_currenttime, currentTime);
            views.setTextViewText(R.id.widget_endtime, totalTime);
            views.setProgressBar(R.id.widget_progressbar, 1000, progress, false);
			updateLyr(service.position(), views);
            artistName=MusicUtils.buildArtistName(artistName);
			boolean isNeedAnim = false;
            if((MediaPlaybackService.widgetartist3name == null) || (artistName != null && !MediaPlaybackService.widgetartist3name.equals(artistName))) {
                isUpdate=true;
				lastArtistName = MediaPlaybackService.widgetartist3name;	//save last song artist name
				isNeedAnim = true;
            } else if((MediaPlaybackService.widgetrack3name == null ) || (titleName != null && !MediaPlaybackService.widgetrack3name.equals(titleName))) {
				isUpdate=true;	//this is for refreshing lrc when play a player`s some songs 
            }

			
            if(artistName != null && isUpdate){
                MediaPlaybackService.widgetartist3name = artistName;
				MediaPlaybackService.widgetrack3name = titleName;
                setBitmap(service, views, true, isNeedAnim);
				//initLyric(service);	//init lyrc when user show lrc		//		
				if(null != sentences) {
					sentences.clear();
					sentences = null;
				}
            } 
        }
        
        // Set correct drawable for pause state
        final boolean playing = service.isPlaying();
        if (playing) {
            views.setImageViewResource(R.id.widget_play, R.drawable.widget_fxt_pause_selector);
        } else {
            views.setImageViewResource(R.id.widget_play, R.drawable.widget_fxt_play_selector);
        } 

		//LewaUtils.logI(TAG, "isShowLyr = " + MediaPlaybackService.isShowLyr);
		if(MediaPlaybackService.isShowLyr) {
			//views.setImageViewResource(R.id.lyr_control, R.drawable.widget_music_lrc_off);
			views.setImageViewResource(R.id.widget_album_cover, R.drawable.widget_music_43_mask5);
			views.setViewVisibility(R.id.lru_last, View.VISIBLE);
			views.setViewVisibility(R.id.lru_cur, View.VISIBLE);
			views.setViewVisibility(R.id.lru_next, View.VISIBLE);
		} else {
			//views.setImageViewResource(R.id.lyr_control, R.drawable.widget_music_lrc_on);
			views.setImageViewResource(R.id.widget_album_cover, R.drawable.widget_music_43_mask2);
			views.setViewVisibility(R.id.lru_last, View.INVISIBLE);
			views.setViewVisibility(R.id.lru_cur, View.INVISIBLE);
			views.setViewVisibility(R.id.lru_next, View.INVISIBLE);
		}

		if(lastLyrFlag != MediaPlaybackService.isShowLyr) {			
			setBitmap(service, views, MediaPlaybackService.isShowLyr, false);
			lastLyrFlag = MediaPlaybackService.isShowLyr;
		}

		if(null == sentences) {
			if(lastLyrFlag) {
				initLyric(service);
			}
		}

        // Link actions buttons to intents
        linkButtons(service, views, playing);
        
        pushUpdate(service, appWidgetIds, views);
    }

	static long  animTime = 0;
	boolean lastLyrFlag = true;
	
	public void useDefBitmap(Resources res, final RemoteViews views) {
		LewaUtils.logI(TAG, "useDefBitmap");
		/*Bitmap defArtist = BitmapFactory.decodeResource(res, R.drawable.cover).copy(Bitmap.Config.ARGB_8888, true);
		if(null == defArtist) {
			LewaUtils.logI(TAG, "null == defArtist"); 
		} 

		WidgetUtils.BgBitmap bg = WidgetUtils.getWidgetBG(res, defArtist, R.drawable.widget_music_43_mask0);
		if(null != bg ) {
				views.setImageViewBitmap(R.id.widget_bg, bg.bitmap);
				resetTextColor(bg.color, views);
		} else {
			isUpdate = true;
		}
		
		Bitmap artistNew = WidgetUtils.getBlurBitmap(res, defArtist, R.drawable.widget_music_43_mask1);
		views.setImageViewBitmap(R.id.widget_album, artistNew);
		
		if(null != defArtist && !defArtist.isRecycled()) {
			defArtist.recycle();
			defArtist = null;
		}*/
	}  

	public void resetTextColor(int color, final RemoteViews views) {
		LewaUtils.logI(TAG, "resetTextColor color = " + Integer.toHexString(color));
		int textColor = 0xffffffff;
		if(color > WidgetUtils.thresholdColor) {
			textColor = 0xff606060;
			if(MediaPlaybackService.isShowLyr) {				
				views.setImageViewResource(R.id.lyr_control, R.drawable.widget_music_lrc_off_black);
			} else {
				views.setImageViewResource(R.id.lyr_control, R.drawable.widget_music_lrc_on_black);
			}
		} else {
			textColor = 0xffffffff;
			if(MediaPlaybackService.isShowLyr) {
				views.setImageViewResource(R.id.lyr_control, R.drawable.widget_music_lrc_off);
			} else {
				views.setImageViewResource(R.id.lyr_control, R.drawable.widget_music_lrc_on);
			} 
		} 
		//views.setImageViewResource(R.id.lyr_control, resId);
		views.setTextColor(R.id.widget_trackname, textColor);
		views.setTextColor(R.id.widget_artistname, textColor);
	}
	
	public void setBitmap(MediaPlaybackService service, final RemoteViews views) {
		setBitmap(service, views, true, true);
	}

	boolean animFlag = false;
	
    public void setBitmap(MediaPlaybackService service, final RemoteViews views, boolean isShowLyr, boolean isNeedAnim) {
		//LewaUtils.logI(TAG, "setBitmap ################# isNeedAnim = " + isNeedAnim);
        Context context = service.getApplicationContext();  
        String unknown = context.getString(R.string.unknown_artist_name);
		//LewaUtils.logI(TAG, "lastArtistName = " +lastArtistName);
		//LewaUtils.logI(TAG, "MediaPlaybackService.widgetartist3name = " +MediaPlaybackService.widgetartist3name);
		if(null == unknown || null == MediaPlaybackService.widgetartist3name) {
			return;
		}
		
        if(!(unknown.equals(MediaPlaybackService.widgetartist3name.trim()))) {
			
			Bitmap lastBitmap = null;

            if(artistBm == null || isUpdate) {
                MusicUtils.recyleBitmap(artistBm);
                int width=context.getResources().getDimensionPixelSize(R.dimen.widget_4x3_img_width);
				//Log.i(TAG, "width = " + width);
                artistBm = MusicUtils.getLocalBitmap(context, Environment.getExternalStorageDirectory().getAbsolutePath()+DownLoadAllPicsAsync.ARTIST_PATH+MediaPlaybackService.widgetartist3name.trim()+".jpg",width,width);

				if(null == artistBm) {
					String artName = WidgetUtils.getAtristName(MediaPlaybackService.widgetartist3name.trim());
					artistBm = MusicUtils.getLocalBitmap(context, Environment.getExternalStorageDirectory().getAbsolutePath()+DownLoadAllPicsAsync.ARTIST_PATH+artName.trim()+".jpg",width,width);
				}
				
				if(null != artistBm) {
					isUpdate=false;					
				}

				//!isUpdate && 
				if((null != lastArtistName) && !(lastArtistName.equals(MediaPlaybackService.widgetartist3name)) && isNeedAnim) {
					lastBitmap = MusicUtils.getLocalBitmap(context, Environment.getExternalStorageDirectory().getAbsolutePath()+DownLoadAllPicsAsync.ARTIST_PATH+lastArtistName.trim()+".jpg",width,width);					
				}

				
				lastArtistName = MediaPlaybackService.widgetartist3name;
            }
			
			
			//LewaUtils.logI(TAG,"lastBitmap = " + lastBitmap);
			if(isNeedAnim) {
				if(animFlag){
					
					RemoteViews subView = new RemoteViews(service.getPackageName(), R.layout.widget_4x3_layout_anim_in_a);	
					views.removeAllViews(R.id.widget_bg_layout);  
					views.removeAllViews(R.id.widget_bg_layout_b);  
				    views.addView(R.id.widget_bg_layout, subView);  

					if(null != lastBitmap) {
						RemoteViews lastView = new RemoteViews(service.getPackageName(), R.layout.widget_4x3_layout_anim_out_a);	
						views.removeAllViews(R.id.widget_last_bg_layout);  
						views.removeAllViews(R.id.widget_last_bg_layout_b);  
					    views.addView(R.id.widget_last_bg_layout, lastView);
					}
				} else {
					RemoteViews subView = new RemoteViews(service.getPackageName(), R.layout.widget_4x3_layout_anim_in_b);	
					views.removeAllViews(R.id.widget_bg_layout);  
					views.removeAllViews(R.id.widget_bg_layout_a);  
				    views.addView(R.id.widget_bg_layout, subView); 

					if(null != lastBitmap) {
						RemoteViews lastView = new RemoteViews(service.getPackageName(), R.layout.widget_4x3_layout_anim_out_a);	
						views.removeAllViews(R.id.widget_last_bg_layout);  
						views.removeAllViews(R.id.widget_last_bg_layout_a);  
					    views.addView(R.id.widget_last_bg_layout, lastView);
					}
				}
				
				animFlag = !animFlag;
			}
			
			WidgetUtils.BgBitmap bg = WidgetUtils.getWidgetBG(service.getResources(), artistBm, R.drawable.widget_music_43_mask0);
			if(null != bg ) {
				views.setImageViewBitmap(R.id.widget_bg, bg.bitmap);
				resetTextColor(bg.color, views);
			} else {
				views.setImageViewBitmap(R.id.widget_bg, null);
				resetTextColor(0xff000000, views);
				isUpdate = true;
			}

			if(null != lastBitmap) {
				WidgetUtils.BgBitmap lastBg = WidgetUtils.getWidgetBG(service.getResources(), lastBitmap, R.drawable.widget_music_43_mask0);
				if(null != lastBg ) {
					views.setImageViewBitmap(R.id.widget_last_bg, lastBg.bitmap);
				}
			}
			
			
			Bitmap artist = null;
			if(isShowLyr) {
				artist = WidgetUtils.getBlurBitmap(service.getResources(),artistBm, R.drawable.widget_music_43_mask4);
			} else {
				artist = WidgetUtils.getBlurBitmap(service.getResources(),artistBm, R.drawable.widget_music_43_mask1);
			}
			views.setImageViewBitmap(R.id.widget_album, artist);

			Bitmap lastArtist = null;
			if(null != lastBitmap) {
				lastArtist = WidgetUtils.getBlurBitmap(service.getResources(),lastBitmap, R.drawable.widget_music_43_mask1);				
				views.setImageViewBitmap(R.id.widget_last_album, lastArtist);				
			}
						
			
			if(null != artistBm && !artistBm.isRecycled()) {
				artistBm.recycle();
				artistBm = null;
			}

			if(null != lastBitmap && !lastBitmap.isRecycled()) {
				lastBitmap.recycle();
				lastBitmap = null;
			}
        }
    }


	
	private void updateLyr(long time, RemoteViews views) {		
		//LewaUtils.logI(TAG, "curLyr = " + curLyr);
		if(null != sentences && 0 != sentences.size()) {
			int listSize = sentences.size();
			for (int i = 0; i < listSize; i++) {
	            Sentence sentence = sentences.get(i);
	            if (sentence.isInTime(time)) {
					if(sentence.getContent().equals(curLyr)) {
						return;
					}
					preLyr = curLyr;
					curLyr = sentence.getContent();
					if((i+1) < listSize) {
						nextLyr = sentences.get(i+1).getContent();
					} else {
						nextLyr = null;
					}
					
				}
			}     
		}
		views.setTextViewText(R.id.lru_last, preLyr);
		views.setTextViewText(R.id.lru_cur, curLyr);
		views.setTextViewText(R.id.lru_next, nextLyr);
	}
	
	List<Sentence> sentences = null;
	boolean isDownloading = false;
	private void initLyric(MediaPlaybackService service) {
		LewaUtils.logI(TAG, "initLyric");
		
		if(null != sentences) {
			sentences.clear();
			sentences = null;
		}

		if(null == service.getTrackName()) {
			return;
		}
		
		if(!service.getTrackName().equals(MediaPlaybackService.onDownloadingArtistName)	) {
			MediaPlaybackService.onDownloadingArtistName = service.getTrackName();
			preLyr = null;
			curLyr = null;
			nextLyr = null;		
			isDownloading = false;	//if this song lrc is not exist , this flag will enable download lrc file from networks
		}
		
		PlayListItem currentLrc = new PlayListItem(service.getTrackName(), null, 0L, true);
		String sdCardDir = Environment.getExternalStorageDirectory()
                        + Constants.SRC_PATH;
		File lrcFile = new File(sdCardDir + service.getTrackName() + "-" + service.getArtistName()
                        + ".lrc"); 

		LewaUtils.logI(TAG, "isDownloading = " + isDownloading);
		if((null == lrcFile || !lrcFile.exists()) && !isDownloading) {
			LewaUtils.logI(TAG, "Do not have Lrc");
			OnlineLoader.getSongLrc(service.getTrackName(), service.getArtistName()); //download LRC from network			
			setLyricNOlrc(service.getApplicationContext(), 0);			//update hint
			isDownloading = true;			
			return;
		} else if(null != lrcFile && lrcFile.exists()){
			Lyric mLyric = new Lyric(lrcFile, currentLrc, service.duration());
			sentences = mLyric.list;
			isDownloading = false;
			curLyr = null;	//clear curLyr`s content from setLyricNOlrc() 
		} else if(isDownloading) {
			//...
		}
		
		
	}

	private String setLyricNOlrc(Context context, int ifnolrc) {

        int resourceId = R.string.loadlrc;
        if (ifnolrc == 1) {
            resourceId = R.string.lrc_down_notfound;
        }
        if (Lewa.getIntSetting(Constants.SETTINGS_KEY_DOWNLOAD_LRC, Constants.SETTINGS_DOWNLOAD_LRC_DEFAULT) == Constants.SETTINGS_DOWNLOAD_LRC_ON) {
            if (!OnlineLoader.isNetworkAvailable()) {
                resourceId = R.string.no_network;
            }
        } else {
            resourceId = R.string.download_lrc_off_hint;
        }

		curLyr =  context.getString(resourceId);
		return curLyr;
    }
}

