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
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener,AbsListView.OnScrollListener {

    private final String TAG = "MainActivity";
    private Callback<GalleryData> callback;
    private ImgurAPI api;

    private ImageListAdapter adapter;

    private AtomicBoolean currentlyProcessingAPI;
    private AtomicBoolean isDefaultGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //show default gallery
        this.adapter = null;

        this.currentlyProcessingAPI = new AtomicBoolean(false);
        this.isDefaultGallery = new AtomicBoolean(true);

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

                //adapter = new ImageListAdapter(getApplicationContext(), R.layout.list_item, filteredData);
                adapter.addData(filteredData);

                listView.setAdapter(adapter);

                currentlyProcessingAPI.set(false);

                Log.d(TAG, response.toString());
                Log.d(TAG, "Size of Dataset: " + data.getData().size());
            }

            @Override
            public void failure(RetrofitError error){
                currentlyProcessingAPI.set(false);
            }
        };

        //set default adapter list
        ArrayList<ImageData> data = new ArrayList<ImageData>(100);
        this.adapter = new ImageListAdapter(this.getApplicationContext(), R.layout.list_item, data);

        api = new ImgurAPI();
        currentlyProcessingAPI.set(true);
        api.showDefaultGallery(this.callback, 0);

        //implement button listener
        Button searchButton = (Button) this.findViewById(R.id.button);
        searchButton.setOnClickListener(this);


        //implement onitemclick for listview
        ListView listView = (ListView) this.findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setClickable(true);

        //add scroll listener
        listView.setOnScrollListener(this);
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
        this.isDefaultGallery.set(false);

        this.adapter.list = new ArrayList<>(100);
        this.adapter.notifyDataSetChanged();

        EditText text = (EditText) findViewById(R.id.editText);
        this.api.searchImgur(this.callback, text.getText().toString(), 0);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("URL", ((ImageListAdapter)parent.getAdapter()).list.get(position).getLink());

        this.startActivity(intent);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        //Load more data
        //Loads next page once 90% through first page, rounds up
        int threshhold = (int)Math.ceil(this.adapter.imageLocation * 0.9);
        if(firstVisibleItem + visibleItemCount >= threshhold)
        {
            //call next page api if api is not currently being called already
            if(!this.currentlyProcessingAPI.get())
            {
                //if default gallery
                if(this.isDefaultGallery.get())
                {
                    this.api.showDefaultGallery(this.callback, this.adapter.pagesLoaded);
                    this.adapter.pagesLoaded++;
                }
                else
                {
                    EditText text = (EditText) findViewById(R.id.editText);
                    this.api.searchImgur(this.callback, text.getText().toString(), this.adapter.pagesLoaded);
                    this.adapter.pagesLoaded++;
                }
            }
        }

        //Load more empty spaces
        if(firstVisibleItem + visibleItemCount + 5 >= totalItemCount)
        {
            this.adapter.loadMoreData();
        }

        Log.d("onscroll", firstVisibleItem + " " + visibleItemCount + " " + totalItemCount );
    }
}
