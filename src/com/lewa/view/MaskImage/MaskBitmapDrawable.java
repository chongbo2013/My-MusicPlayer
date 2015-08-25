package com.lewa.view.MaskImage;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.lewa.Lewa;
import com.lewa.player.R;

public class MaskBitmapDrawable extends BitmapDrawable {
    private static final String TAG = MaskBitmapDrawable.class.getName();
    private Paint mPaint = new Paint();
    private Path mPath;
    private float mViewSize = 0;
    private int mMaskColor = -1;

    public MaskBitmapDrawable(Resources mResources, Bitmap bitmap) {
        super(mResources, bitmap);
        mPath = PathHelper.doPath(Lewa.string(R.string.svg_200));
    }

    @Override
    public void draw(Canvas canvas) {
        if (mPath != null) {
            Bitmap bitmap = getBitmap();
            if (bitmap != null) {
                if (bitmap != null && canvas != null && mPath != null && mPaint != null) {
                    Matrix scaleMatrix = new Matrix();
                    RectF rectF = new RectF();
                    mPath.computeBounds(rectF, true);
                    float pathScale = mViewSize / rectF.width();
//                    Log.i(TAG, "View size: " + mViewSize + "\tPath size: " + rectF.height() + "\tPath scale: " + pathScale);
                    scaleMatrix.setScale(pathScale, pathScale, rectF.left, rectF.top);
                    mPath.transform(scaleMatrix);
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();

//                    Bitmap maskedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//                    Canvas maskCanvas = new Canvas(maskedBitmap);
//                    maskCanvas.drawColor(mMaskColor);
//
//                    maskCanvas.save(Canvas.ALL_SAVE_FLAG);
//                    maskCanvas.restore();

                    BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

                    //float mViewSize = 240f;
                    float c = 1;
                    if (w != mViewSize || h != mViewSize) {
                        c = mViewSize / Math.min(w, h);
                    }

                    Matrix localM = new Matrix();
                    float dx = 0;
                    float dy = 0;
                    float newx = w * c;
                    float newy = h * c;
                    if (newx > mViewSize) {
                        dx = (newx - mViewSize) / 2;
                    }
                    if (newy > mViewSize) {
                        dy = (newy - mViewSize) / 2;
                    }
                    localM.setScale(c, c);
                    localM.postTranslate(-dx, -dy);
                    bitmapShader.setLocalMatrix(localM);

//                    Log.i(TAG, "ShaderMatrix: scale-" + c + "\tdx:" + dx + "\tdy:" + dy);

                    mPaint.setAntiAlias(true);
                    mPaint.setStyle(Paint.Style.FILL);

                    if (mMaskColor != -1) {
                        Log.i(TAG, "Draw color mask.");
                        LinearGradient maskShader = new LinearGradient(0, 0, bitmap.getWidth(), bitmap.getHeight(), mMaskColor, mMaskColor, Shader.TileMode.CLAMP);
                        ComposeShader composeShader = new ComposeShader(bitmapShader, maskShader, PorterDuff.Mode.SRC_OVER);
                        mPaint.setShader(composeShader);
                    } else {
                        mPaint.setShader(bitmapShader);
                    }


//                    Paint maskPaint = new Paint();
//                    maskPaint.setAntiAlias(true);
//                    maskPaint.setStyle(Paint.Style.FILL);
//                    maskPaint.setShader(maskShader);

//                    Matrix pathMatrix = new Matrix();
//                    float pathScale = mViewSize / 180f;
//                    pathMatrix.setScale(pathScale, pathScale);
//                    mPath.transform(pathMatrix);
                    canvas.drawPath(mPath, mPaint);
                }
            }
        }
    }

    public void setPath(Path path) {
        this.mPath = path;
    }

    public void setViewSize(float viewSize) {
        this.mViewSize = viewSize;
    }

    public void setMaskColor(int maskColor) {
        this.mMaskColor = maskColor;
    }

}
