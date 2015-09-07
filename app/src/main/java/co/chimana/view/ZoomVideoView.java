/*
*   Based on:
*   http://chicketen.blog.jp/archives/1579621.html
*   http://chicketen.blog.jp/archives/1622120.html
*   http://www.binpress.com/tutorial/video-cropping-with-texture-view/21
* */

package co.chimana.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.io.IOException;

public class ZoomVideoView extends TextureView implements TextureView.SurfaceTextureListener, View.OnTouchListener {
    private String uriPath;
    private Surface mSurface;
    private Matrix mMatrix;

    private ZoomViewWrapper p;

    private float mTranslateX, mTranslateY;
    private float maxTranslateX, minTranslateX;
    private float maxTranslateY, minTranslateY;

    private float actualVideoWidth, actualVideoHeight;
    private float mVideoWidth, mVideoHeight;
    private float parentViewWidth, parentViewHeight;

    private float mScale;
    private float mPrevX, mPrevY;
    private ScaleGestureDetector mScaleGestureDetector;
    private TranslationGestureDetector mTranslationGestureDetector;

    private static float MAX_SCALE = 2.5f;

    // Log tag.
    private static final String TAG = ZoomVideoView.class.getName();

    // MediaPlayer instance to control playback of video file.
    private MediaPlayer mMediaPlayer;

    /**
     * constructor
     * @param context
     */
    public ZoomVideoView(Context context) {
        super(context);
        mMatrix = new Matrix();
        mScale = 1.0f;
        mScaleGestureDetector = new ScaleGestureDetector(context, mOnScaleListener);
        mTranslationGestureDetector = new TranslationGestureDetector(mTranslationListener);

        setOnTouchListener(this);
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Log.d(TAG, "------- onSurfaceTextureAvailable == -------");

        mSurface = new Surface(surfaceTexture);
        calculateWrapperSize();

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(uriPath);
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();

            // Play video when the media source is ready for playback.
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        Log.d(TAG, "------- onSurfaceTextureSizeChanged == -------");
        mTranslateX = this.getWidth() / 2;
        mTranslateY = this.getHeight() / 2;

        maxTranslateX = actualVideoWidth / 2;
        minTranslateX = actualVideoWidth - parentViewWidth;

        maxTranslateY = actualVideoHeight / 2.0f;
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
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    /**
     * set video uri string
     */
    public void setVideo(String uri) {
        uriPath = uri;
        calculateVideoSize();
    }

    /**
     * update parent view's size
     */
    public void update() {
        Log.d(TAG, "------- update from listner == -------");

        if(mSurface != null) {
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

        // set minX
        if((maxTranslateX == minTranslateX) && (mScale == 1.0f)) {
            // minTranslateX = maxTranslateX;
        } else {
            minTranslateX = parentViewWidth - maxTranslateX * mScale;
//                Log.d(TAG, "------- minTranslateX ------- = " + minTranslateX);
        }

        // modify translateX if it's over the area
        if(mTranslateX > maxTranslateX * mScale) {
            mTranslateX = maxTranslateX * mScale;
        }

        // modify translateX if it's over the area
        if(mTranslateX < minTranslateX) {
            mTranslateX = minTranslateX;
        }

        // set minY
        if((maxTranslateY == minTranslateY) && (mScale == 1.0f)) {
            // minTranslateY = maxTranslateY;
        } else {
            minTranslateY = parentViewHeight - maxTranslateY * mScale;
//                Log.d(TAG, "------- minTranslateY ------- = " + minTranslateY);
        }

        // modify translateY if it's over the area
        if(mTranslateY > maxTranslateY * mScale) {
//                Log.d(TAG, "------- mTranslateY > maxTranslateY * mScale -------");
            mTranslateY = maxTranslateY * mScale;
        }

        // modify translateY if it's over the area
        if(mTranslateY < minTranslateY) {
//                Log.d(TAG, "------- mTranslateY > minTranslateY ------- = " + minTranslateY);
            mTranslateY = minTranslateY;
        }

        Log.d(TAG, "------- mTranslateX modified = " + mTranslateX);
        Log.d(TAG, "------- mTranslateY modified = " + mTranslateY);

        mMatrix.reset();
        mMatrix.postScale(mScale, mScale);
        mMatrix.postTranslate(-this.getWidth() / 2 * mScale, -this.getHeight() / 2 * mScale);
        mMatrix.postTranslate(mTranslateX, mTranslateY);
        this.setTransform(mMatrix);
    }

    /**
     * calculate parent view size
     */
    private void calculateWrapperSize() {
        Log.d(TAG, "------- calculateWrapperSize! == -------");

        p = (ZoomViewWrapper) this.getParent();
        parentViewWidth = p.getWidth();
        parentViewHeight = p.getHeight();

        Log.d(TAG, "------- PARENT WIDTH = " + p.getWidth());
        Log.d(TAG, "------- PARENT HEIGHT = " + p.getHeight());

        float ratioW = parentViewWidth / mVideoWidth;
        float ratioH = parentViewHeight / mVideoHeight;

        Log.d(TAG, "------- RATIO WIDTH = " + ratioW);
        Log.d(TAG, "------- RATIO HEIGHT = " + ratioH);

        float tempW = 0;
        float tempH = 0;

        if (mVideoWidth > parentViewWidth && mVideoHeight > parentViewHeight) {
            Log.d(TAG, "------- mVideoWidth > parentViewWidth && mVideoHeight > parentViewHeight -------");

            if(ratioW > ratioH) {
                tempW = parentViewWidth;
                tempH = ratioW * mVideoHeight;
            } else {
                tempW = ratioH * mVideoWidth;
                tempH = parentViewHeight;
            }

        } else if (mVideoWidth < parentViewWidth && mVideoHeight < parentViewHeight) {
            Log.d(TAG, "------- mVideoWidth < parentViewWidth && mVideoHeight < parentViewHeight -------");

            if(ratioW > ratioH) {
                tempW = parentViewWidth;
                tempH = ratioW * mVideoHeight;
            } else {
                tempW = ratioH * mVideoWidth;
                tempH = parentViewHeight;
            }
        } else if (parentViewWidth > mVideoWidth) {
            Log.d(TAG, "------- parentViewWidth > mVideoWidth-------");

            tempW = parentViewWidth;
            tempH = ratioW * mVideoHeight;
        } else if (parentViewHeight > mVideoHeight) {
            Log.d(TAG, "------- parentViewHeight > mVideoHeight -------");

            tempW = ratioH * mVideoWidth;
            tempH = parentViewHeight;
        } else {
            Log.d(TAG, "------- FOR EXAMPLE, WIDTH == SAME -------");
            tempW = mVideoWidth;
            tempH = mVideoHeight;
        }

        actualVideoWidth = tempW;
        actualVideoHeight = tempH;

        this.getLayoutParams().width = (int) tempW;
        this.getLayoutParams().height = (int) tempH;
        this.requestLayout();

        mScale = 1.0f;

        Log.d(TAG, "------- Actual width = " + actualVideoWidth);
        Log.d(TAG, "------- Actual height = " + actualVideoHeight);
        Log.d(TAG, "------- THIS WIDTH AFTER CHANGE = " + this.getWidth());
        Log.d(TAG, "------- THIS HEIGHT AFTER CHANGE = " + this.getHeight());
    }

    /**
     * calculate video size
     */
    private void calculateVideoSize() {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(uriPath);

        String height = metaRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        mVideoHeight = Float.parseFloat(height);
        mVideoWidth = Float.parseFloat(width);

        Log.d(TAG, "-------calculateVideoSize: H == -------" + mVideoHeight);
        Log.d(TAG, "-------calculateVideoSize: W == -------" + mVideoWidth);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        mTranslationGestureDetector.onTouch(v, event);

        present();
        return true;
    }

    private ScaleGestureDetector.SimpleOnScaleGestureListener mOnScaleListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
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
            if(mScale < 1.0) {
                mScale = 1.0f;
            }

            // max
            if(mScale > MAX_SCALE) {
                mScale = MAX_SCALE;
            }

            Log.d(TAG, "------- mScale ------- = " + mScale);

            return true;
        }
    };

    private TranslationGestureListener mTranslationListener
            = new TranslationGestureListener() {
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