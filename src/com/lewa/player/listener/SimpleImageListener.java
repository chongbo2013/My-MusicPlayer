package com.lewa.player.listener;

import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.lewa.Lewa;

/**
 * Created by wuzixiu on 12/31/13.
 */
public class SimpleImageListener implements ImageLoader.ImageListener {
    ImageView mIv;
    int mDefaultImageResId;
    int mErrorImageResId;
    int mSize;
    String mSvg;

    public SimpleImageListener(ImageView iv, int defaultImageResId, int errorImageResId) {
        this.mIv = iv;
        this.mDefaultImageResId = defaultImageResId;
        this.mErrorImageResId = errorImageResId;
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        mIv.setImageBitmap(response.getBitmap());
        if (response.getBitmap() != null) {
            mIv.setImageBitmap(response.getBitmap());
        } else if (mDefaultImageResId != 0) {
            mIv.setImageDrawable(Lewa.resources().getDrawable(mDefaultImageResId));
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (mErrorImageResId != 0) {
            mIv.setImageDrawable(Lewa.resources().getDrawable(mErrorImageResId));
        }
    }
}
