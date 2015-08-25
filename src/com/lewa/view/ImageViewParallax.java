package com.lewa.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.util.LewaUtils;


public class ImageViewParallax extends ImageView {

    private float positionOffset = 0;
    private int curPage = 1;
    private Paint paint;
    int width;
    int height;

    private final static String TAG = "ImageViewParallax";


    public ImageViewParallax(Context context) {
        super(context);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        paint = new Paint();
    }

    public ImageViewParallax(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        paint = new Paint();
    }

    public void reset(int position, float positionOffset) {
        this.positionOffset = positionOffset;
        this.curPage = position;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        BitmapDrawable drawable = (BitmapDrawable) getDrawable();

        if (drawable == null) return;

        Bitmap bitmap = drawable.getBitmap();

        if (bitmap == null) {
            Log.w(TAG, "No bitmap in image view.");
            return;
        }
        // 获得图片的宽高
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        LewaUtils.logE(TAG, "Bitmap size: " + bitmapHeight + " * " + bitmapWidth);

        int targetHeight = canvas.getHeight();

        // 计算缩放比例
        float heightScale = ((float) targetHeight) / bitmapHeight;
        float scaleWidth = bitmapWidth * heightScale;

        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.setScale(heightScale, heightScale);
//        float maxOffset = bitmapWidth - canvas.getWidth() > 0 ? bitmapWidth - canvas.getWidth() : bitmapWidth - width;
        float offset = (scaleWidth - canvas.getWidth()) * (curPage + positionOffset) / -2;
        matrix.postTranslate(offset, 0);
        canvas.concat(matrix);
        drawable.draw(canvas);
       /* canvas.drawColor(Lewa.resources().getColor(R.color.white_text));

        int srcLeft = 0;
        int srcRight = 0;
        int destLeft = 0;
        int destRight = 0;

        float canvasDownHeightScale = (float)bitmapHeight / canvas.getHeight();
        float canvasDownWidth = canvas.getWidth() * canvasDownHeightScale;

        LewaUtils.logE(TAG, "Canvas size: " + canvas.getHeight() + ", " + canvas.getWidth() + ", scale: " + canvasDownHeightScale);
        float bitmapUpHeightScale = (float)canvas.getHeight() / bitmapHeight;
        float bitmapUpWidth = bitmapWidth * bitmapUpHeightScale;
        if (bitmapWidth > canvasDownWidth) {
        	LewaUtils.logE(TAG, "Bitmap wider.");
            srcLeft = (int) ((bitmapWidth - canvasDownWidth) * (curPage + positionOffset) / 2);
            srcRight = Math.round(srcLeft + canvasDownWidth);
            destLeft = 0;
            destRight = canvas.getWidth();
        } else {
            srcLeft = 0;
            srcRight = bitmapWidth;
            destLeft = (int) ((canvas.getWidth() - bitmapUpWidth) * (curPage + positionOffset) / 2);
            destRight = Math.round(destLeft + bitmapUpWidth);
        }

        LewaUtils.logE(TAG, "SRC: " + srcLeft + ", " + srcRight + ", canvas down width: " + canvasDownWidth);
        LewaUtils.logE(TAG, "DEST: " + destLeft + ", " + destRight);
        Rect srcRect = new Rect(srcLeft, 0, srcRight, bitmapHeight);
        Rect destRect = new Rect(destLeft, 0, destRight, canvas.getHeight());

        canvas.drawBitmap(bitmap, srcRect, destRect, paint);*/
    }

}
