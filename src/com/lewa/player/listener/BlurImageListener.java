package com.lewa.player.listener;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.lewa.Lewa;
import com.lewa.player.helper.ImageHelper;

import java.lang.ref.WeakReference;

/**
 * Created by wuzixiu on 12/31/13.
 */
public class BlurImageListener implements ImageLoader.ImageListener {
    private static final String TAG = BlurImageListener.class.getName();
    public static final int UPDATE_BG_IMAGE = 1;
    ImageView mIv;
    int mDefaultImageResId;
    int mErrorImageResId;
    WeakReference<Handler> mHandlerRef;
    //Keep an extra reference to bitmap returned from response, until blured.
    Bitmap bitmap = null;

    public BlurImageListener(ImageView iv, Handler handler, int defaultImageResId, int errorImageResId) {
        this.mHandlerRef = new WeakReference<Handler>(handler);
        this.mIv = iv;
        this.mDefaultImageResId = defaultImageResId;
        this.mErrorImageResId = errorImageResId;
    }

    void releaseBitmap() {
        bitmap = null;
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        if (response.getBitmap() != null) {
//            mIv.setImageBitmap(response.getBitmap());
            Lewa.holdBitmap(response.getBitmap());
            startBlur(response.getBitmap());
        } else if (mDefaultImageResId != 0) {
            mIv.setImageDrawable(Lewa.resources().getDrawable(mDefaultImageResId));
        }
        Log.i(TAG, "Blur task started.");
    }

    private void startBlur(final Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null) {
                    Bitmap blurredBitmap = ImageHelper.applyGaussianBlur(bitmap);
                    Lewa.releaseBitmap(bitmap);

                    if (mHandlerRef.get() != null) {
                        Message message = new Message();
                        message.obj = blurredBitmap;
                        message.what = UPDATE_BG_IMAGE;
                        mHandlerRef.get().sendMessage(message);
                    }
                }
            }
        }).start();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (mErrorImageResId != 0) {
//            mIv.setImageBitmap(blurBitmap((BitmapDrawable) Lewa.resources().getDrawable(mErrorImageResId)));
            mIv.setImageDrawable(Lewa.resources().getDrawable(mErrorImageResId));
        }
    }

}
