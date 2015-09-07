package co.chimana.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ZoomViewWrapper extends FrameLayout {
    private static final String TAG = ZoomViewWrapper.class.getSimpleName();

    /**
     * constructor
     * @param context
     */
    public ZoomViewWrapper(Context context) {
        this(context, null);
    }

    /**
     * constructor
     * @param context
     * @param attrs
     */
    public ZoomViewWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * constructor
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ZoomViewWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * on size changed
     * @param w
     * @param h
     */
    protected void onSizeChanged (int w, int h) {
        OnLayoutChangeListener listener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                                       int oldBottom) {
                // its possible that the layout is not complete in which case
                // we will get all zero values for the positions, so ignore the event
                if (left == 0 && top == 0 && right == 0 && bottom == 0) {
                    return;
                }

                ViewGroup vg = (ViewGroup) v;

                if (vg.getChildCount() > 0) {
                    View vc = vg.getChildAt(0);

                    // Do what you need to do with the height/width since they are now set
                    Log.d(TAG, "------- CLASS =====" + vc.getClass().getSimpleName());

                    String className = vc.getClass().getSimpleName();

                    if (className.equals("TouchVideoView")) {
                        ZoomVideoView tvv = (ZoomVideoView) vc;
                        tvv.update();

                        // remove listener
                        removeOnLayoutChangeListener(this);
                    } else if(className.equals("TouchImageView")) {
                        ZoomImageView tiv = (ZoomImageView) vc;
                        tiv.update();

                        // remove listener
                        removeOnLayoutChangeListener(this);
                    }
                }
            }
        };

        // add listener
        addOnLayoutChangeListener(listener);
    }

    /**
     * Update Local Camera
     */
    public void updateLocalCamera(float[] size) {
        Log.d(TAG, "------- Update Camera Matrix! ");

        if (this.getChildCount() > 0) {
            ZoomCameraView vc = (ZoomCameraView) this.getChildAt(0);
            vc.updateMatrix(size[0], size[1]);
        }
    }

    /**
     * SwipeDown
     */
    public void displayControl() {
        Log.d(TAG, "------- LET'S SWIPE DOWN! ");

        if (this.getChildCount() > 0) {
            View vc = this.getChildAt(1);

            vc.setVisibility(VISIBLE);
        }
    }
}
