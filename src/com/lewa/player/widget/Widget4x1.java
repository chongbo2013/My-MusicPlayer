package com.lewa.player.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.lewa.Lewa;
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
import android.util.Log;
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
import com.lewa.player.widget.WidgetUtils;


/**
 * Created by Administrator on 13-11-24.
 */
public class Widget4x1 extends AppWidgetProvider {

	private static final String TAG = "Widget4x1";

	private static Widget4x1 sInstance = null;
	private Bitmap artistBm = null;
	private String artistName= null;
	private String showTitleName = null;
	private String showArtistName = null;

	private String titleName = null;
	public  static Boolean isUpdate = false; //public because MediaPlayBackService need invoke
	private static String lastArtistName = null;

	public static synchronized Widget4x1 getInstance() {
        if (sInstance == null) {
            sInstance = new Widget4x1();
        }
        return sInstance;
    } 
	
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
		Log.i(TAG, "onDeleted");
		
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
		SharedPreferences sp=context.getSharedPreferences("Music", Context.MODE_PRIVATE);
        Editor editor =sp.edit();
        editor.putBoolean("is4X1WidgetAdded", false).commit();
        MediaPlaybackService.isWidget4X1Added=false;
		//Log.i(TAG, "onDisabled");
		//Log.i(TAG, "onDisabled isWidget4X1Added = " + MediaPlaybackService.isWidget4X1Added);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
		SharedPreferences sp=context.getSharedPreferences("Music", Context.MODE_PRIVATE);
        Editor editor =sp.edit();
        editor.putBoolean("is4X1WidgetAdded", true).commit();
        MediaPlaybackService.isWidget4X1Added=true;
		//Log.i(TAG, "onEnabled isWidget4X1Added = " + MediaPlaybackService.isWidget4X1Added);
		isUpdate=true;
    }

	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                             int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		Log.i(TAG, "--onAppWidgetOptionsChanged--");
		isUpdate = true;
	}


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i("widget_4x1", "onReceive action:" + intent.getAction());
        if (intent.getAction().equals("com.imhipi.music.widget.4x1")) {
            RemoteViews appWidgetView = new RemoteViews(context.getPackageName(), R.layout.widget_4x1_layout);
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, Widget4x1.class), appWidgetView);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
		
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
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_4x1_layout);

        //views.setViewVisibility(R.id.widget_trackname, View.GONE);
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
            gm.updateAppWidget(appWidgetIds, views);
        } else {       	
            gm.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }*/
        Log.i(TAG, "pushUpdate");
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
		views.setOnClickPendingIntent(R.id.widget_album, pendingIntent);
                 
        intent = new Intent(MediaPlaybackService.TOGGLEPAUSE_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.widget_play, pendingIntent);
        //views.setOnClickPendingIntent(R.id.widget_worning_tip, pendingIntent);
        
        intent = new Intent(MediaPlaybackService.NEXT_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.widget_next, pendingIntent);        
    }

    /**
     * init all widgets
     */
    private void initView(Context context, RemoteViews widgetView, AppWidgetManager appWidgetManager) {
        Intent coverIntent = new Intent(context, PlayActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean(PlayActivity.FROM_WIDGET, true);
		coverIntent.putExtras(bundle);
		
        PendingIntent coverPendingIntent = PendingIntent.getActivity(context, 0, coverIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.bt_cover, coverPendingIntent);

        Intent houndIntent = new Intent(context, SongHoundActivity.class);
        PendingIntent houndPendingIntent = PendingIntent.getActivity(context, 0, houndIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.bt_recognize, houndPendingIntent);
        appWidgetManager.updateAppWidget(new ComponentName(context, Widget4x1.class), widgetView);
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
    	Log.i(TAG, "performUpdate");
        final Resources res = service.getResources();
        final RemoteViews views = new RemoteViews(service.getPackageName(), R.layout.widget_4x1_layout);
        
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
        } else if (titleName == null || (!MusicUtils.mHasSongs && !MediaPlaybackService.isOnlinePlay)) {	//&& !MediaPlaybackService.isOnlinePlay add by sjxu for bug 52127
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
            views.setTextViewText(R.id.widget_endtime, "0:00");
			views.setTextViewText(R.id.widget_currenttime, "0:00");
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
            artistName=MusicUtils.buildArtistName(artistName);
			boolean isNeedAnim = false;
            if((MediaPlaybackService.widgetartistname == null) || (artistName != null && !MediaPlaybackService.widgetartistname.equals(artistName))) {
                isUpdate = true;
				lastArtistName = MediaPlaybackService.widgetartistname;
				isNeedAnim = true;
            } else if((MediaPlaybackService.widgeTrackName == null) || (titleName != null && !MediaPlaybackService.widgeTrackName.equals(titleName))) {
				isUpdate = true;
            }
			
            if(artistName != null && isUpdate){
                MediaPlaybackService.widgetartistname = artistName;
				MediaPlaybackService.widgeTrackName = titleName;
                setBitmap(service, views, isNeedAnim);
            }
        }
        
        // Set correct drawable for pause state
        final boolean playing = service.isPlaying();
        if (playing) {
            views.setImageViewResource(R.id.widget_play, R.drawable.widget_fxo_pause_selector);
        } else {
            views.setImageViewResource(R.id.widget_play, R.drawable.widget_fxo_play_selector);
        }

        // Link actions buttons to intents
        linkButtons(service, views, playing);
        
        pushUpdate(service, appWidgetIds, views);
    }

	public void resetTextColor(int color, final RemoteViews views) {
		
		Log.i(TAG, "color = " + Integer.toHexString(color));
		int textColor = 0xffffffff;
		if(color > WidgetUtils.thresholdColor) {
			textColor = 0xff606060;
		} else {
			textColor = 0xffffffff;
		}
			
		views.setTextColor(R.id.widget_endtime, textColor);
		views.setTextColor(R.id.widget_currenttime, textColor);
		views.setTextColor(R.id.widget_trackname, textColor);
		views.setTextColor(R.id.widget_artistname, textColor);
	}
	
	public void useDefBitmap(Resources res, final RemoteViews views) {
		
	}

	boolean animFlag = false;
    public void setBitmap(MediaPlaybackService service, final RemoteViews views, boolean isNeedAnim) {
		//Log.i(TAG, " ********setBitmap******");
        Context context = service.getApplicationContext();  
        String unknown = context.getString(R.string.unknown_artist_name);
        if(!(unknown.equals(MediaPlaybackService.widgetartistname.trim()))) {
//                artistBm = getArtistBitmap(artistName.trim());
			Bitmap lastBitmap = null;
            if(artistBm == null|| isUpdate) {
                MusicUtils.recyleBitmap(artistBm);
                int width=context.getResources().getDimensionPixelSize(R.dimen.widget_img_width);
				
                artistBm = MusicUtils.getLocalBitmap(context, Environment.getExternalStorageDirectory().getAbsolutePath()+DownLoadAllPicsAsync.ARTIST_PATH+MediaPlaybackService.widgetartistname.trim()+".jpg",width,width);
				if(null == artistBm) {
					Log.i(TAG, "MediaPlaybackService.widgetartistname.trim() = " + MediaPlaybackService.widgetartistname.trim());
					String artName = WidgetUtils.getAtristName(MediaPlaybackService.widgetartistname.trim());
					Log.i(TAG, "artName = " + artName);
					artistBm = MusicUtils.getLocalBitmap(context, Environment.getExternalStorageDirectory().getAbsolutePath()+DownLoadAllPicsAsync.ARTIST_PATH+artName.trim()+".jpg",width,width);
				}
				
				if(null != artistBm) {
					isUpdate = false;
				}

				if(null != lastArtistName && !(lastArtistName.equals(MediaPlaybackService.widgetartistname)) && isNeedAnim) {
					lastBitmap = MusicUtils.getLocalBitmap(context, Environment.getExternalStorageDirectory().getAbsolutePath()+DownLoadAllPicsAsync.ARTIST_PATH+lastArtistName.trim()+".jpg",width,width);					
				}
				lastArtistName = MediaPlaybackService.widgetartist3name;
            }
			            
			/*if(null == artistBm) {
				views.setImageViewBitmap(R.id.widget_bg, null);
				views.setImageViewBitmap(R.id.widget_album, null);
				resetTextColor(0xff000000, views);
				return;
				//artistBm = BitmapFactory.decodeResource(service.getResources(), R.drawable.cover).copy(Bitmap.Config.ARGB_8888, true);
			}*/

			if(isNeedAnim) {
				if(animFlag){
					
					RemoteViews albumView = new RemoteViews(service.getPackageName(), R.layout.widget_4x1_layout_album_anim_in_a);	
					views.removeAllViews(R.id.widget_album_layout);  
					views.removeAllViews(R.id.widget_album_layout_b);  
				    views.addView(R.id.widget_album_layout, albumView);  

					RemoteViews bgView = new RemoteViews(service.getPackageName(), R.layout.widget_4x1_layout_bg_anim_in_a);	
					views.removeAllViews(R.id.widget_bg_layout);  
					views.removeAllViews(R.id.widget_bg_layout_b);  
				    views.addView(R.id.widget_bg_layout, bgView);  

					if(null != lastBitmap) {
						RemoteViews lastAlbumView = new RemoteViews(service.getPackageName(), R.layout.widget_4x1_layout_album_anim_out_a);	
						views.removeAllViews(R.id.widget_last_album_layout);  
						views.removeAllViews(R.id.widget_last_album_layout_b);  
					    views.addView(R.id.widget_last_album_layout, lastAlbumView);  

						RemoteViews lastBgView = new RemoteViews(service.getPackageName(), R.layout.widget_4x1_layout_bg_anim_out_a);	
						views.removeAllViews(R.id.widget_last_bg_layout);  
						views.removeAllViews(R.id.widget_last_bg_layout_b);  
					    views.addView(R.id.widget_last_bg_layout, lastBgView);  
					}
				} else {
					RemoteViews albumView = new RemoteViews(service.getPackageName(), R.layout.widget_4x1_layout_album_anim_in_b);	
					views.removeAllViews(R.id.widget_album_layout);  
					views.removeAllViews(R.id.widget_album_layout_a);  
				    views.addView(R.id.widget_album_layout, albumView);  

					RemoteViews bgView = new RemoteViews(service.getPackageName(), R.layout.widget_4x1_layout_bg_anim_in_b);	
					views.removeAllViews(R.id.widget_bg_layout);  
					views.removeAllViews(R.id.widget_bg_layout_a);  
				    views.addView(R.id.widget_bg_layout, bgView);  

					if(null != lastBitmap) {
						RemoteViews lastAlbumView = new RemoteViews(service.getPackageName(), R.layout.widget_4x1_layout_album_anim_out_a);	
						views.removeAllViews(R.id.widget_last_album_layout);  
						views.removeAllViews(R.id.widget_last_album_layout_b);  
					    views.addView(R.id.widget_last_album_layout, lastAlbumView);  

						RemoteViews lastBgView = new RemoteViews(service.getPackageName(), R.layout.widget_4x1_layout_bg_anim_out_a);	
						views.removeAllViews(R.id.widget_last_bg_layout);  
						views.removeAllViews(R.id.widget_last_bg_layout_b);  
					    views.addView(R.id.widget_last_bg_layout, lastBgView);  
					}
				}
				
				animFlag = !animFlag;
			}
	
			WidgetUtils.BgBitmap bg = WidgetUtils.getWidgetBG(service.getResources(), artistBm, R.drawable.widget_music_41_mask0);
			if(null != bg ) {
				if(0x0 == bg.color) {
					Bitmap defBitmap = MusicUtils.getLocalBitmap(context, Environment.getExternalStorageDirectory().getAbsolutePath()+DownLoadAllPicsAsync.ARTIST_PATH+MediaPlaybackService.widgetartistname.trim()+".jpg",500, 500);
					bg = WidgetUtils.getWidgetBG(service.getResources(), defBitmap, R.drawable.widget_music_41_mask0);
					if(null != defBitmap && !defBitmap.isRecycled()) {
						defBitmap.recycle();
						defBitmap = null;
					}
				}
				views.setImageViewBitmap(R.id.widget_bg, bg.bitmap);
				resetTextColor(bg.color, views);
			} else {
				views.setImageViewBitmap(R.id.widget_bg, null);
				resetTextColor(0xff000000, views);
				isUpdate = true;
			}

			if(null != lastBitmap) {
				WidgetUtils.BgBitmap lastBg = WidgetUtils.getWidgetBG(service.getResources(), lastBitmap, R.drawable.widget_music_41_mask0);
				if(null != lastBg ) {
					views.setImageViewBitmap(R.id.widget_last_bg, lastBg.bitmap);
				}
			}
			
			Bitmap artist = WidgetUtils.getBlurBitmap(service.getResources(), artistBm, R.drawable.widget_music_41_mask1);           
			views.setImageViewBitmap(R.id.widget_album, artist);

			//Bitmap lastArtist = null;
			if(null != lastBitmap) {
				Bitmap lastArtist = WidgetUtils.getBlurBitmap(service.getResources(),lastBitmap, R.drawable.widget_music_41_mask1);				
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
    
}

