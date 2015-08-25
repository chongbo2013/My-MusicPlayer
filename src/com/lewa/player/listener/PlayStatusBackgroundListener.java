package com.lewa.player.listener;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.model.PlayStatus;

import java.lang.ref.WeakReference;

/**
 * Created by wuzixiu on 1/6/14.
 */
public class PlayStatusBackgroundListener implements PlayStatusListener {
    WeakReference<ImageView> ivRef = null;
    int mDefaultImageResId = R.drawable.cover;
    String mId = null;
    boolean mShowOriginal = false;

    public PlayStatusBackgroundListener(String id, ImageView iv) {
        mId = id;
        ivRef = new WeakReference<ImageView>(iv);
    }

    public PlayStatusBackgroundListener(String id, ImageView iv, boolean showOriginal) {
        this(id, iv);
        mShowOriginal = showOriginal;
    }

    public PlayStatusBackgroundListener(String id, ImageView iv, boolean showOriginal, int defaultImageResId) {
        this(id, iv, showOriginal);
        mDefaultImageResId = defaultImageResId;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public void onPlayStatusChanged(PlayStatus status) {

    }

    @Override
    public void onBackgroundReady(Bitmap bitmap) {
        if (ivRef != null) {
            ImageView iv = ivRef.get();

            if (iv != null) {
            	if (bitmap != null) {
            		iv.setImageBitmap(bitmap);
            	} else {
            		//pr 937782 add by wjhu
            		//to set default cover when there is no bitmap get
					iv.setImageResource(mDefaultImageResId);
				}
            }
        }
    }

    @Override
    public void onBluredBackgroundReady(Bitmap bitmap) {
        if (ivRef != null) {
            ImageView iv = ivRef.get();

            if (iv != null) {
            	if (bitmap != null) {
            		iv.setImageBitmap(bitmap);
            	} else {
            		//pr 937782 add by wjhu
            		//to set default cover when there is no bitmap get
					iv.setImageResource(mDefaultImageResId);
				}
                
            }
        }
    }

    @Override
    public void onStartGetBackground() {
        if (ivRef != null) {
            ImageView iv = ivRef.get();

            if (iv != null && mDefaultImageResId > 0) {
                //iv.setImageDrawable(Lewa.resources().getDrawable(mDefaultImageResId));
            }
        }
    }

    @Override
    public void onSongDownloaded(Long onlineId, Long localId) {

    }
}
