package co.chimana.zoomviewsample;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String sampleList[] = new String [] {
                "Camera Sample",
                "Image Sample",
                "Video Simple"
        };
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sampleList));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, ZoomCameraActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, ZoomImageActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, ZoomVideoActivity.class));
                break;
        }
    }
}
