package a.b.imgurandroid.android.activities;

import a.b.imgurandroid.android.R;
import a.b.imgurandroid.android.adapters.ImageListAdapter;
import a.b.imgurandroid.android.api.ImgurAPI;
import a.b.imgurandroid.android.api.pojo.GalleryData;
import a.b.imgurandroid.android.api.pojo.ImageData;
import a.b.imgurandroid.android.viewholders.ImageViewHolder;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final String TAG = "MainActivity";
    private Callback<GalleryData> callback;
    private ImgurAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //show default gallery

        this.callback = new Callback<GalleryData>() {
            @Override
            public void success(GalleryData data, Response response) {

                ListView listView = (ListView) findViewById(R.id.listView);
                //prob should recycle the adapters instead of making a new one every time

                //filter out only pngs and imgs and hope there are no weird cases
                List<ImageData> filteredData = new ArrayList<>(data.getData().size());
                for(ImageData image: data.getData())
                {
                    if(image.getIsAlbum() == false && (image.getType().equals("image/jpeg") || image.getType().equals("image/png")))
                    {
                        filteredData.add(image);
                    }
                }
                listView.setAdapter(new ImageListAdapter(getApplicationContext(), R.layout.list_item, filteredData));

                Log.d(TAG, response.toString());
                Log.d(TAG, "Size of Dataset: " + data.getData().size());
            }

            @Override
            public void failure(RetrofitError error){

            }
        };

        api = new ImgurAPI();
        api.showDefaultGallery(this.callback);

        //implement button listener
        Button searchButton = (Button) this.findViewById(R.id.button);
        searchButton.setOnClickListener(this);


        //implement onitemclick for listview
        ListView listView = (ListView) this.findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setClickable(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        EditText text = (EditText) findViewById(R.id.editText);
        this.api.searchImgur(text.getText().toString(), this.callback);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("URL", ((ImageListAdapter)parent.getAdapter()).list.get(position).getLink());

        this.startActivity(intent);
    }
}
