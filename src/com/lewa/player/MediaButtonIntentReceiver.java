/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.lewa.Lewa;
import com.lewa.player.service.PlayerServiceConnector;



/**
 * 
 */
public class MediaButtonIntentReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaButtonIntentReceiver";
    private static final int MSG_LONGPRESS_TIMEOUT = 1;
    private static final int LONG_PRESS_DELAY = 1000;
    private static final String LEWA_PLAY_REQUEST_STATUS = "com.lewa.tuningmaster.PLAY_REQUEST_STATUS";
	private static final String LEWA_PLAY_RESPONSE_STATUS = "com.lewa.tuningmaster.PLAY_RESPONSE_STATUS";
	private static final String LEWA_PLAY_EXTRA_KEY_STATUS = "play_status";
	private static final String LEWA_PLAY_EXTRA_KEY_SESSION_ID = "session_id";
    private static long mLastClickTime = 0;
    private static boolean mDown = false;
    private static boolean mLaunched = false;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LONGPRESS_TIMEOUT:
/*                    if (!mLaunched) {
                        Context context = (Context)msg.obj;
                        Intent i = new Intent();
                        i.putExtra("autoshuffle", "true");
                        i.setClass(context, MusicBrowserActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                        mLaunched = true;
                    }*/
                    break;
            }
        }
    };
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Log.i(TAG, "onReceive intentAction = " + intentAction);
        
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
			//add this start for bug 61129
            //if(!isServiceRunning(context, "com.lewa.player.MediaPlaybackService")) {
                //return;
            //}

			//add this end for bug 61129
            Intent i = new Intent(context, MediaPlaybackService.class);
            i.setAction(MediaPlaybackService.SERVICECMD);
            i.putExtra(MediaPlaybackService.CMDNAME, MediaPlaybackService.CMDPAUSE);
            context.startService(i);
        } else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction) || intentAction.equals("com.lewa.lockscreen.control")) {
            KeyEvent event = (KeyEvent)
                    intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return;
            }

            int keycode = event.getKeyCode();
            int action = event.getAction();
            long eventtime = event.getEventTime();

            // single quick press: pause/resume. 
            // double press: next track
            // long press: start auto-shuffle mode.
            
            String command = null;
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    command = MediaPlaybackService.CMDSTOP;
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    command = MediaPlaybackService.CMDTOGGLEPAUSE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    command = MediaPlaybackService.CMDNEXT;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    command = MediaPlaybackService.CMDPREVIOUS;
                    break;
            }

            if (command != null) {
                if (action == KeyEvent.ACTION_DOWN) {
                    if (mDown) {
                        if (MediaPlaybackService.CMDTOGGLEPAUSE.equals(command)
                                && mLastClickTime != 0 
                                && eventtime - mLastClickTime > LONG_PRESS_DELAY) {
                            mHandler.sendMessage(
                                    mHandler.obtainMessage(MSG_LONGPRESS_TIMEOUT, context));
                        }
                    } else {
                        // if this isn't a repeat event

                        // The service may or may not be running, but we need to send it
                        // a command.
                        Intent i = new Intent(context, MediaPlaybackService.class);
                        i.setAction(MediaPlaybackService.SERVICECMD);
                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK &&
                                eventtime - mLastClickTime < 300) {
                            i.putExtra(MediaPlaybackService.CMDNAME, MediaPlaybackService.CMDNEXT);
                            context.startService(i);
                            mLastClickTime = 0;
                        } else {
                            i.putExtra(MediaPlaybackService.CMDNAME, command);
                            context.startService(i);
                            mLastClickTime = eventtime;
                        }

                        mLaunched = false;
                        mDown = true;
                    }
                } else {
                    mHandler.removeMessages(MSG_LONGPRESS_TIMEOUT);
                    mDown = false;
                }
                if (isOrderedBroadcast()) {
                    abortBroadcast();
                }
            }
        } else if(LEWA_PLAY_REQUEST_STATUS.equals(intentAction)) {
        	Log.i("vvv", "LEWA_PLAY_REQUEST_STATUS");
			PlayerServiceConnector connector = Lewa.playerServiceConnector();
			Intent statusIntent = new Intent(LEWA_PLAY_RESPONSE_STATUS);
			boolean isPlaying = false;
			int ssessionId = -1;
			if(null != connector) {
				isPlaying = connector.isPlaying();
				if(isPlaying) {
					IMediaPlaybackService service = connector.service();
					try {
						ssessionId = service.getAudioSessionId();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			Log.i("vvv", "SEND RESPONSE");
			statusIntent.putExtra(LEWA_PLAY_EXTRA_KEY_STATUS, isPlaying);
			statusIntent.putExtra(LEWA_PLAY_EXTRA_KEY_SESSION_ID, ssessionId);
			context.sendBroadcast(statusIntent);
        }
    }

    public static boolean isServiceRunning(Context mContext,String className) {
           boolean isRunning = false;
           ActivityManager activityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE); 
           List<ActivityManager.RunningServiceInfo> serviceList 
                      = activityManager.getRunningServices(50);

           if (!(serviceList.size()>0)) {
               return false;
           }

           for (int i=0; i<serviceList.size(); i++) {
               if (serviceList.get(i).service.getClassName().equals(className) == true) {
                   isRunning = true;
                   break;
               }
           }
           return isRunning;
       }
    
    

}
