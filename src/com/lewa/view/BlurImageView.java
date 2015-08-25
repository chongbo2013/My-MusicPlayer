package com.lewa.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.helper.ImageHelper;
import com.lewa.view.MaskImage.MaskBitmapDrawable;
import com.lewa.view.MaskImage.PathHelper;

/**
 * Created by wuzixiu on 12/10/13.
 */
@RemoteView
public class BlurImageView extends ImageView {
    private static final String TAG = BlurImageView.class.getName();

    public BlurImageView(Context context) {
        super(context);
        Log.i(TAG, "Create BlurImageView.");
    }

    public BlurImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "Create BlurImageView.");
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.i(TAG, "On draw BlurImageView.");

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable();
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();

            if (bitmap != null) {
                Bitmap blurdBitmap = ImageHelper.applyGaussianBlur(bitmap);
//                setImageBitmap(blurdBitmap);
//                getMatrix();
//                canvas.drawBitmap(blurdBitmap, 0, 0, null);
                canvas.drawBitmap(blurdBitmap, getMatrix(), null);
            }
        }

    }
}