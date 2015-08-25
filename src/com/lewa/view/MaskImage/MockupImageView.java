package com.lewa.view.MaskImage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;

/**
 * Created by wuzixiu on 12/10/13.
 */
@RemoteView
public class MockupImageView extends ImageView {
    private static final String TAG = MockupImageView.class.getName();
    private Paint mPaint = new Paint();
    private Path mPath;
    private float mViewSize = 180f;

    public MockupImageView(Context context) {
        super(context);
        Log.i(TAG, "Create MockupImageView.");
    }

    public MockupImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "Create MockupImageView.");
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw");
        super.onDraw(canvas);
    }

    public void setPath(Path path) {
        mPath = path;
    }

    public void setViewSize(float viewSize) {
        mViewSize = viewSize;
    }

}