/*
*   Based on:
*   http://chicketen.blog.jp/archives/1579621.html
*   http://chicketen.blog.jp/archives/1622120.html
* */

package co.chimana.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.provider.MediaStore;

public class ZoomImageView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    private static final String TAG = ZoomImageView.class.getSimpleName();
    private Context context;
    private SurfaceHolder mHolder;
    private Bitmap mBitmap;
    private Matrix mMatrix;

    private ZoomViewWrapper p;

    private float mTranslateX, mTranslateY;
    private float maxTranslateX, minTranslateX;
    private float maxTranslateY, minTranslateY;

    private float actualImageWidth, actualImageHeight;
    private float mImageWidth, mImageHeight;
    private float parentViewWidth, parentViewHeight;

    private float mScale;
    private float mDefaultScale;
    private float mPrevX, mPrevY;
    private static float MAX_SCALE = 2.5f;

    private ScaleGestureDetector mScaleGestureDetector;
    private TranslationGestureDetector mTranslationGestureDetector;

    /**
     * constructor
     * @param context
     */
    public ZoomImageView(Context context) {
        super(context);
        this.context = context;
        mMatrix = new Matrix();
        mScale = 1.0f;
        mScaleGestureDetector = new ScaleGestureDetector(context, mOnScaleListener);
        mTranslationGestureDetector = new TranslationGestureDetector(mTranslationListener);

        getHolder().addCallback(this);

        setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "------- surfaceCreated ");

        p = (ZoomViewWrapper) this.getParent();

        calculateWrapperSize();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "------- surfaceChanged ");
        mHolder = holder;

        mTranslateX = mBitmap.getWidth() / 2;
        mTranslateY = mBitmap.getHeight() / 2;

        maxTranslateX = actualImageWidth / 2;
        minTranslateX = actualImageWidth - parentViewWidth;

        maxTranslateY = actualImageHeight / 2;
        minTranslateY = parentViewHeight - maxTranslateY;

        if(maxTranslateY == minTranslateY) {
            Log.d(TAG, "-------  Max + Min == equal -------");
        }

        Log.d(TAG, "-------  First MaxX == -------" + maxTranslateX);
        Log.d(TAG, "------- First MinX == -------" + minTranslateX);
        Log.d(TAG, "-------  First MaxY == -------" + maxTranslateY);
        Log.d(TAG, "------- First MinY == -------" + minTranslateY);

        present();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        context = null;
        mHolder = null;
        mBitmap = null;
        mMatrix = null;
        p = null;

        mScaleGestureDetector = null;
        mTranslationGestureDetector = null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        mTranslationGestureDetector.onTouch(v, event);

        present();
        return true;
    }

    /**
     * set bitmap
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        calculateBitmapSize();
    }

    /**
     * set image url string
     * @param uri
     */
    public void setImageURI(Uri uri) {
        Log.d(TAG, "------- setImageURI -------");
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver() , uri);
            calculateBitmapSize();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    /**
     * set image url string
     * @param uri
     */
    public void setImageURIString(String uri) {
        try
        {
            mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver() , Uri.parse(uri));
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    /**
     * update parent view's size
     */
    public void update() {
        Log.d(TAG, "------- update from listener -------");

        if(mHolder != null) {
            calculateWrapperSize();
        } else {
        }
    }

    /**
     * update size with matrix
     */
    private void present() {
        Log.d(TAG, "------- mScale = " + mScale);
        Log.d(TAG, "------- mTranslateX = " + mTranslateX);
        Log.d(TAG, "------- mTranslateY = " + mTranslateY);

        float ratioScale = mScale / mDefaultScale;

        // set minX
        if((maxTranslateX == minTranslateX) && (mScale == mDefaultScale)) {
            // minTranslateX = maxTranslateX;
        } else {
            minTranslateX = parentViewWidth - maxTranslateX * ratioScale;
//                Log.d(TAG, "------- minTranslateX ------- = " + minTranslateX);
        }

        // modify translateX if it's over the area
        if(mTranslateX > maxTranslateX * ratioScale) {
            mTranslateX = maxTranslateX * ratioScale;
        }

        // modify translateX if it's over the area
        if(mTranslateX < minTranslateX) {
            mTranslateX = minTranslateX;
        }

        // set minY
        if((maxTranslateY == minTranslateY) && (mScale == mDefaultScale)) {
            // minTranslateY = maxTranslateY;
        } else {
            minTranslateY = parentViewHeight - maxTranslateY * ratioScale;
//                Log.d(TAG, "------- minTranslateY ------- = " + minTranslateY);
        }

        // modify translateY if it's over the area
        if(mTranslateY > maxTranslateY * ratioScale) {
//                Log.d(TAG, "------- mTranslateY > maxTranslateY * mScale -------");
            mTranslateY = maxTranslateY * ratioScale;
        }

        // modify translateY if it's over the area
        if(mTranslateY < minTranslateY) {
//                Log.d(TAG, "------- mTranslateY > minTranslateY ------- = " + minTranslateY);
            mTranslateY = minTranslateY;
        }

        Log.d(TAG, "------- mTranslateX modified = " + mTranslateX);
        Log.d(TAG, "------- mTranslateY modified = " + mTranslateY);
        
        Canvas canvas = mHolder.lockCanvas();
        mMatrix.reset();
        mMatrix.postScale(mScale, mScale);
        mMatrix.postTranslate(-mBitmap.getWidth() / 2 * mScale, -mBitmap.getHeight() / 2 * mScale);
        mMatrix.postTranslate(mTranslateX, mTranslateY);

        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(mBitmap, mMatrix, null);

        mHolder.unlockCanvasAndPost(canvas);
        Log.d(TAG, "mScale ========" + mScale);
    }

    /**
     * calculate parent view size
     */
    private void calculateWrapperSize() {
        Log.d(TAG, "------- calculateWrapperSize! == -------");

        parentViewWidth = p.getWidth();
        parentViewHeight = p.getHeight();

        Log.d(TAG, "------- PARENT WIDTH = " + p.getWidth());
        Log.d(TAG, "------- PARENT HEIGHT = " + p.getHeight());

        float ratioW = parentViewWidth / mImageWidth;
        float ratioH = parentViewHeight / mImageHeight;

        Log.d(TAG, "------- RATIO WIDTH = " + ratioW);
        Log.d(TAG, "------- RATIO HEIGHT = " + ratioH);

        float tempW = 0;
        float tempH = 0;

        if (mImageWidth > parentViewWidth && mImageHeight > parentViewHeight) {
            Log.d(TAG, "------- mImageWidth > parentViewWidth && mImageHeight > parentViewHeight -------");

            if(ratioW > ratioH) {
                tempW = parentViewWidth;
                tempH = ratioW * mImageHeight;
            } else {
                tempW = ratioH * mImageWidth;
                tempH = parentViewHeight;
            }

        } else if (mImageWidth < parentViewWidth && mImageHeight < parentViewHeight) {
            Log.d(TAG, "------- mImageWidth < parentViewWidth && mImageHeight < parentViewHeight -------");

            if(ratioW > ratioH) {
                tempW = parentViewWidth;
                tempH = ratioW * mImageHeight;
            } else {
                tempW = ratioH * mImageWidth;
                tempH = parentViewHeight;
            }
        } else if (parentViewWidth > mImageWidth) {
            Log.d(TAG, "------- parentViewWidth > mImageWidth-------");

            tempW = parentViewWidth;
            tempH = ratioW * mImageHeight;
        } else if (parentViewHeight > mImageHeight) {
            Log.d(TAG, "------- parentViewHeight > mImageHeight -------");

            tempW = ratioH * mImageWidth;
            tempH = parentViewHeight;
        } else {
            Log.d(TAG, "------- FOR EXAMPLE, WIDTH == SAME -------");
            tempW = mImageWidth;
            tempH = mImageHeight;
        }

        actualImageWidth = tempW;
        actualImageHeight = tempH;
        
        this.getLayoutParams().width = (int) parentViewWidth;
        this.getLayoutParams().height = (int) parentViewHeight;

        this.requestLayout();

        if(ratioH > ratioW) {
            mDefaultScale = ratioH;
        } else {
            mDefaultScale = ratioW;
        }

        mScale = mDefaultScale;

        Log.d(TAG, "------- mDefaultScale = " + mDefaultScale);
        Log.d(TAG, "------- Actual width = " + actualImageWidth);
        Log.d(TAG, "------- Actual height = " + actualImageHeight);
        Log.d(TAG, "------- THIS WIDTH AFTER CHANGE = " + this.getWidth());
        Log.d(TAG, "------- THIS HEIGHT AFTER CHANGE = " + this.getHeight());
    }

    /**
     * calculate video size
     */
    private void calculateBitmapSize() {
        mImageWidth = mBitmap.getWidth();
        mImageHeight = mBitmap.getHeight();

        Log.d(TAG, "-------calculateBitmapSize: H == -------" + mImageHeight);
        Log.d(TAG, "-------calculateBitmapSize: W == -------" + mImageWidth);
    }

    private ScaleGestureDetector.SimpleOnScaleGestureListener mOnScaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScale *= detector.getScaleFactor();

            // min
            if(mScale < mDefaultScale) {
                mScale = mDefaultScale;
            }

            // max
            if(mScale > MAX_SCALE * mDefaultScale) {
                mScale = MAX_SCALE * mDefaultScale;
            }

            Log.d(TAG, "------- mScale ------- = " + mScale);

            return true;
        }
    };

    private TranslationGestureListener mTranslationListener = new TranslationGestureListener() {
        @Override
        public void onTranslationEnd(TranslationGestureDetector detector) {
        }

        @Override
        public void onTranslationBegin(TranslationGestureDetector detector) {
            mPrevX = detector.getX();
            mPrevY = detector.getY();
        }

        @Override
        public void onTranslation(TranslationGestureDetector detector) {
            float deltaX = detector.getX() - mPrevX;
            float deltaY = detector.getY() - mPrevY;
            mTranslateX += deltaX;
            mTranslateY += deltaY;
            mPrevX = detector.getX();
            mPrevY = detector.getY();
        }
    };
}