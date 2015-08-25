package com.lewa.player.listener;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.lewa.Lewa;
import com.lewa.view.MaskImage.MaskBitmapDrawable;
import com.lewa.view.MaskImage.PathHelper;

/**
 * Created by wuzixiu on 12/31/13.
 */
public class NetImageListener implements ImageLoader.ImageListener {
    ImageView mIv;
    int mDefaultImageResId;
    int mErrorImageResId;
    int mSize;
    String mSvg;

    public NetImageListener(ImageView iv, int defaultImageResId, int errorImageResId) {
        this.mIv = iv;
        this.mDefaultImageResId = defaultImageResId;
        this.mErrorImageResId = errorImageResId;
    }

    public NetImageListener(ImageView iv, int defaultImageResId, int errorImageResId, String svg, int size) {
        this(iv, defaultImageResId, errorImageResId);
        this.mSvg = svg;
        this.mSize = size;
    }

    public NetImageListener(ImageView iv, int defaultImageResId, int errorImageResId, int svgStrResId, int size) {
        this(iv, defaultImageResId, errorImageResId);
        this.mSvg = Lewa.string(svgStrResId);
        this.mSize = size;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (mErrorImageResId != 0) {
            mIv.setImageDrawable(buildMaskDrawable(((BitmapDrawable) Lewa.resources().getDrawable(mErrorImageResId)).getBitmap()));
//            mIv.setImageResource(mErrorImageResId);
        }
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        if (response.getBitmap() != null) {
            mIv.setImageDrawable(buildMaskDrawable(response.getBitmap()));
        } else if (mDefaultImageResId != 0) {
            mIv.setImageDrawable(buildMaskDrawable(((BitmapDrawable) Lewa.resources().getDrawable(mDefaultImageResId)).getBitmap()));
//            mIv.setImageResource(mDefaultImageResId);
        }
    }

    private MaskBitmapDrawable buildMaskDrawable(Bitmap bitmap) {
        MaskBitmapDrawable maskBitmapDrawable = new MaskBitmapDrawable(Lewa.resources(), bitmap);

        if (mSize > 0) {
            maskBitmapDrawable.setViewSize(mSize);
        }

        if (mSvg != null && mSvg.trim().length() > 0) {
            maskBitmapDrawable.setPath(PathHelper.doPath(mSvg));
        }

        return maskBitmapDrawable;
    }
}
