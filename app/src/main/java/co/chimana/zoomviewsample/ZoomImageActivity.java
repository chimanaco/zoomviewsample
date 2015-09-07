package co.chimana.zoomviewsample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import co.chimana.view.ZoomImageView;
import co.chimana.view.ZoomViewWrapper;

/**
 * Created by chimanaco on 9/7/15.
 */
public class ZoomImageActivity extends Activity {
    private Context context;

    private ZoomViewWrapper wrapper;
    private ZoomImageView imageView;
    private static final String TAG = ZoomVideoActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        // layout
        setContentView(R.layout.activity_image);

        // wrapper
        wrapper = (ZoomViewWrapper) findViewById(R.id.ImageLayout);

        // image view
        imageView = new ZoomImageView(context);

        // bitmap from drawable
        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(), R.drawable.image);

        // set Bitmap
        imageView.setBitmap(bitmap);

        // add view
        wrapper.addView(imageView, 0);
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        // if it has a imageView, remove it
        if(wrapper.getChildCount() == 1) {
            wrapper.removeView(imageView);
        }

        wrapper = null;
        imageView = null;
        context = null;

        super.onDestroy();
    }
}




