package co.chimana.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;

import java.io.IOException;
import java.util.List;

public class ZoomCameraView extends TextureView implements TextureView.SurfaceTextureListener, View.OnTouchListener {

    private static final String TAG = "CameraPreview";
    private Camera mCamera;
    Camera.Parameters mParameters;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private ZoomViewWrapper p;
    private Matrix mMatrix;

    private float mTranslateX, mTranslateY;
    private float maxTranslateX, minTranslateX;
    private float maxTranslateY, minTranslateY;

    private float actualPreviewWidth, actualPreviewHeight;
    private float parentViewWidth, parentViewHeight;

    private float mScale;
    private float mPrevX, mPrevY;
    private static float MAX_SCALE = 2.5f;

    boolean isPreviewPlaying = false;

    private ScaleGestureDetector mScaleGestureDetector;
    private TranslationGestureDetector mTranslationGestureDetector;

    private boolean isNexus5 = false;
    private boolean isNexus6 = false;
    private boolean isNexus7 = false;

    /**
     * Constructor
     * @param context
     * @param isN5
     * @param isN6
     * @param isN7
     * @param context
     */
    public ZoomCameraView(Context context, boolean isN5, boolean isN6, boolean isN7) {
        super(context);

        Log.d(TAG, "------- TouchCameraView Constructor");

        isNexus5 = isN5;
        isNexus6 = isN6;
        isNexus7 = isN7;

        // matrix for scale
        mMatrix = new Matrix();
        mScale = 1.0f;

        // for Nexus7
        if(isNexus7) {
            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCamera = Camera.open(i);
                }
            }
        } else {
            int cameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
            mCamera = Camera.open(cameraType);
        }

        // for Nexus6
        if(isNexus6) {
            mCamera.setDisplayOrientation(270);
        } else {
            mCamera.setDisplayOrientation(90);
        }

        mCamera.startPreview();

        // supported preview sizes
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        for(Camera.Size str: mSupportedPreviewSizes)
            Log.e(TAG, str.width + "/" + str.height);

        // gesture detector
        mScaleGestureDetector = new ScaleGestureDetector(context, mOnScaleListener);
        mTranslationGestureDetector = new TranslationGestureDetector(mTranslationListener);

        // listener
        setOnTouchListener(this);
        setSurfaceTextureListener(this);
    }

    /**
     *
     * @param surfaceTexture
     * @param i
     * @param i2
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Log.d(TAG, "------- onSurfaceTextureAvailable ");
        Log.d(TAG, "------- this W = " + this.getWidth());
        Log.d(TAG, "------- this H = " + this.getHeight());

        updateMatrix(this.getWidth(), this.getHeight());

        // set preview size and make any resize, rotate or reformatting changes here
        // start preview with new settings
        try {
            mParameters = mCamera.getParameters();
            mParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(mParameters);
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }

        try {
            mCamera.setPreviewTexture(surfaceTexture);
            isPreviewPlaying = true;
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

    /**
     *
     * @param surfaceTexture
     * @param i
     * @param i2
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    /**
     *
     * @param surfaceTexture
     * @return
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    /**
     *
     * @param surfaceTexture
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    /**
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        float ratio;
        if(mPreviewSize.height >= mPreviewSize.width)
            ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
        else
            ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

        // One of these methods should be used, second method squishes preview slightly
        if(!isNexus5) {
            setMeasuredDimension(width, (int) (width * ratio));
        } else {
            setMeasuredDimension((int) (width * ratio), height);
        }
    }

    /**
     * get Optimal PreviewSize
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    /**
     * update size with matrix
     */
    public void present() {
        Log.d(TAG, "------- mScale = " + mScale);
        Log.d(TAG, "------- mTranslateX = " + mTranslateX);
        Log.d(TAG, "------- mTranslateY = " + mTranslateY);
        Log.d(TAG, "-------  First MaxX == -------" + maxTranslateX);
        Log.d(TAG, "------- First MinX == -------" + minTranslateX);
        Log.d(TAG, "-------  First MaxY == -------" + maxTranslateY);
        Log.d(TAG, "------- First MinY == -------" + minTranslateY);
        Log.d(TAG, "------- this W == -------" + this.getWidth());
        Log.d(TAG, "------- this H == -------" + this.getHeight());
        Log.d(TAG, "------- W == -------" + parentViewWidth);
        Log.d(TAG, "------- H == -------" + parentViewHeight);

        // set minX
        if((this.getWidth() == parentViewWidth) && (mScale == 1.0f)) {
        } else {
            minTranslateX = parentViewWidth - maxTranslateX;
        }

        // modify translateX if it's wider than the area
        if(mTranslateX > maxTranslateX) {
            mTranslateX = maxTranslateX;
        }

        // modify translateX if it's shorter than the area
        if(mTranslateX < minTranslateX) {
            mTranslateX = minTranslateX;
        }

        // set minY
        if((this.getHeight() == parentViewHeight) && (mScale == 1.0f)) {
        } else {
            minTranslateY = parentViewHeight - maxTranslateY;
        }

        // modify translateY if it's higher than the area
        if(mTranslateY > maxTranslateY) {
            mTranslateY = maxTranslateY;
        }

        // modify translateY if it's shorter than the area
        if(mTranslateY < minTranslateY) {
            mTranslateY = minTranslateY;
        }

        Log.d(TAG, "------- mTranslateX modified = " + mTranslateX);
        Log.d(TAG, "------- mTranslateY modified = " + mTranslateY);

        // execute matrix
        mMatrix.reset();
        mMatrix.postScale(mScale, mScale);
        mMatrix.postTranslate(-this.getWidth() / 2 * mScale, -this.getHeight() / 2 * mScale);
        mMatrix.postTranslate(mTranslateX, mTranslateY);
        this.setTransform(mMatrix);
    }

    /**
     * updateMatrix
     * @param w
     * @param h
     */
    public void updateMatrix(float w, float h) {
        Log.d(TAG, "------- update Matrix ");

        parentViewWidth = w;

        if(h > this.getHeight()) {
            parentViewHeight = this.getHeight();
        } else {
            parentViewHeight = h;
        }

        updateScale();

        present();
    }

    /**
     * update preview scale
     */
    private void updateScale() {
        // get center
        mTranslateX = parentViewWidth / 2;
        mTranslateY = parentViewHeight / 2;

        // get actual size
        actualPreviewWidth = this.getWidth() * mScale;
        actualPreviewHeight = this.getHeight() * mScale;

        // max / min for X
        maxTranslateX = actualPreviewWidth / 2;
        minTranslateX = actualPreviewWidth - maxTranslateX;

        // max / min for Y
        maxTranslateY = actualPreviewHeight / 2;
        minTranslateY = actualPreviewHeight - maxTranslateY;

        if(maxTranslateY == minTranslateY) {
            Log.d(TAG, "-------  MaxY = MinY!");
        }

        Log.d(TAG, "------- MaxX = " + maxTranslateX);
        Log.d(TAG, "------- MinX = " + minTranslateX);
        Log.d(TAG, "------- MaxY = " + maxTranslateY);
        Log.d(TAG, "------- MinY = " + minTranslateY);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        mTranslationGestureDetector.onTouch(v, event);

        present();
        return true;
    }

    /**
     *
     */
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
            if(mScale < 1.0) {
                mScale = 1.0f;
            }

            // max
            if(mScale > MAX_SCALE) {
                mScale = MAX_SCALE;
            }

            Log.d(TAG, "------- mScale ------- = " + mScale);

            updateScale();

            return true;
        }
    };

    /**
     *
     */
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