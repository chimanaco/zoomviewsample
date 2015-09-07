package co.chimana.zoomviewsample;

import android.app.Activity;
import android.os.Bundle;

import co.chimana.view.ZoomVideoView;
import co.chimana.view.ZoomViewWrapper;

/**
 * Created by chimanaco on 9/7/15.
 */
public class ZoomVideoActivity extends Activity {
    private ZoomViewWrapper wrapper;
    private ZoomVideoView videoView;
    private String uriPath = "/sdcard/DCIM/Camera/movie.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout
        setContentView(R.layout.activity_video);

        // wrapper
        wrapper = (ZoomViewWrapper) findViewById(R.id.VideoLayout);

        // video view
        videoView = new ZoomVideoView(getApplicationContext());

        // set video
        videoView.setVideo(uriPath);

        // add view
        wrapper.addView(videoView, 0);
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        // if it has a videoView, remove it
        if(wrapper.getChildCount() == 1) {
            wrapper.removeView(videoView);
        }

        wrapper = null;
        videoView = null;

        super.onDestroy();
    }
}




