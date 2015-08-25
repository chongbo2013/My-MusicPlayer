package com.lewa.player.listener;

import android.graphics.Bitmap;

import com.lewa.player.model.PlayStatus;

/**
 * Created by wuzixiu on 1/5/14.
 */
public interface PlayStatusListener {
    public String getId();

    public void onPlayStatusChanged(PlayStatus status);

    public void onBackgroundReady(Bitmap bitmap);

    public void onBluredBackgroundReady(Bitmap bitmap);

    public void onStartGetBackground();

    public void onSongDownloaded(Long onlineId, Long localId);
}
