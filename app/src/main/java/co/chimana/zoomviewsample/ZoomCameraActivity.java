package co.chimana.zoomviewsample;

import android.app.Activity;
import android.os.Bundle;

import co.chimana.view.ZoomCameraView;
import co.chimana.view.ZoomViewWrapper;

/**
 * Created by chimanaco on 9/7/15.
 */
public class ZoomCameraActivity extends Activity {
    private ZoomViewWrapper wrapper;
    private ZoomCameraView cameraView;
    private boolean isNexus5 = false;
    private boolean isNexus6 = false;
    private boolean isNexus7 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout
        setContentView(R.layout.activity_camera);

        // wrapper
        wrapper = (ZoomViewWrapper) findViewById(R.id.CameraLayout);

        // camera view
        cameraView = new ZoomCameraView(getApplicationContext(), isNexus5, isNexus6, isNexus7);

        // add view
        wrapper.addView(cameraView);
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        // if it has a cameraView, remove it
        if(wrapper.getChildCount() == 1) {
            wrapper.removeView(cameraView);
        }
        
        wrapper = null;
        cameraView = null;

        super.onDestroy();
    }
}

