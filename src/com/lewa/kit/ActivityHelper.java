package com.lewa.kit;

import android.app.Activity;
import android.content.Intent;

import com.lewa.player.R;
import com.lewa.player.activity.EditPlaylistActivity;
import com.lewa.player.activity.LibraryActivity;
import com.lewa.player.activity.PlayActivity;
import android.content.Context;

import com.lewa.player.activity.SettingActivity;
import com.lewa.player.model.Artist;
import com.lewa.player.activity.ArtistAlbumListActivity;


public class ActivityHelper {

    public static void goLibrary(Activity activity) {
        Intent intent = new Intent(activity, LibraryActivity.class);
        activity.startActivity(intent);
    }


	public static void leaveLibraryAnim(Activity activity) {
		activity.overridePendingTransition(lewa.R.anim.sub_activity_open_enter, R.anim.keep);
	} 

    public static void goLibraryAnim(Activity activity) {
        Intent intent = new Intent(activity, LibraryActivity.class);
        activity.startActivity(intent);
		leavePlayAnim(activity);
    }
	public static void leavePlayAnim(Activity activity) {
        activity.overridePendingTransition(R.anim.keep, lewa.R.anim.sub_activity_close_exit);       
    }

    public static void goLibraryMine(Activity activity) {
        Intent intent = new Intent(activity, LibraryActivity.class);
        intent.setAction(LibraryActivity.ARG_LIBRARY);
        activity.startActivity(intent);
    }

    public static void goPlay(Activity activity) {
        Intent intent = new Intent(activity, PlayActivity.class);
        intent.setAction(PlayActivity.ARG_PLAY);
        activity.startActivity(intent);
    }

	public static void goPlayAnim(Activity activity) {
		Intent intent = new Intent(activity, PlayActivity.class);
		activity.startActivity(intent);
		leaveLibraryAnim(activity);
	}

	public static void goPlayWithClearTopAnim(Activity activity, boolean isRandomAll) {
		Intent sPlayIntent = new Intent();
		if(isRandomAll) {
             	sPlayIntent.putExtra("isRandomAll", true);
		}
            sPlayIntent.setClass(activity, PlayActivity.class);
            sPlayIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(sPlayIntent);
		leaveLibraryAnim(activity);
	}

	public static void goSettingAnim(Activity activity) {
		Intent intent = new Intent(activity, SettingActivity.class);
		activity.startActivity(intent);
		
	}

	public static void goArtistAlbumList(Activity activity, Artist artist) {
		Intent intent = new Intent(activity, ArtistAlbumListActivity.class);

		intent.putExtra(ArtistAlbumListActivity.ARG_ARTIST, artist);
		
        activity.startActivity(intent);
		
	}



    public static void goEditPlaylist(Activity activity, Long playlistId) {
        Intent intent = new Intent(activity, EditPlaylistActivity.class);
        if (playlistId != null) {
            intent.putExtra(EditPlaylistActivity.ARG_PLAYLIST_ID, playlistId);
        }

        activity.startActivity(intent);
    }

}
