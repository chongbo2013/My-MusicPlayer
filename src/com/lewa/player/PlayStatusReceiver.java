package com.lewa.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.lewa.Lewa;
import com.lewa.player.service.PlayerServiceConnector;

public class PlayStatusReceiver extends BroadcastReceiver {
	private static final String LEWA_PLAY_REQUEST_STATUS = "com.lewa.tuningmaster.PLAY_REQUEST_STATUS";
	private static final String LEWA_PLAY_RESPONSE_STATUS = "com.lewa.tuningmaster.PLAY_RESPONSE_STATUS";
	private static final String LEWA_PLAY_EXTRA_KEY_STATUS = "play_status";
	private static final String LEWA_PLAY_EXTRA_KEY_SESSION_ID = "session_id";

	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i("vvv", "action is " + action);
		if(LEWA_PLAY_REQUEST_STATUS.equals(action)) {
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

}
