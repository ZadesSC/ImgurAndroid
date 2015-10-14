package a.b.imgurandroid.android.activities;

import a.b.imgurandroid.android.R;
import a.b.imgurandroid.android.adapters.ImageListAdapter;
import a.b.imgurandroid.android.api.ImgurAPI;
import a.b.imgurandroid.android.api.pojo.GalleryData;
import a.b.imgurandroid.android.api.pojo.ImageData;
import android.app.Activity;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener,AbsListView.OnScrollListener {

    private final String TAG = "MainActivity";
    private Callback<GalleryData> callback;
    private Call<GalleryData> call;
    private ImgurAPI api;

    private ImageListAdapter adapter;

    private AtomicBoolean currentlyProcessingAPI;
    private AtomicBoolean isDefaultGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //show default gallery
        GridView gridView = (GridView) findViewById(R.id.gridView);

        //set default adapter list
        ArrayList<ImageData> data = new ArrayList<ImageData>(100);
        this.adapter = new ImageListAdapter(this.getApplicationContext(), R.layout.list_item, data);
        gridView.setAdapter(adapter);


        this.currentlyProcessingAPI = new AtomicBoolean(false);
        this.isDefaultGallery = new AtomicBoolean(true);

        //move this section out?
        this.callback = new Callback<GalleryData>() {
            @Override
            public void onResponse(Response<GalleryData> response, Retrofit retrofit) {
                Log.d(TAG, "message " + response.message());
                //prob should recycle the adapters instead of making a new one every time

                //filter out only pngs and imgs and hope there are no weird cases
                GalleryData data = response.body();
                List<ImageData> filteredData = new ArrayList<ImageData>(data.getData().size());
                for(ImageData image: data.getData())
                {
                    if(image.getIsAlbum() == false && (image.getType().equals("image/jpeg") || image.getType().equals("image/png")))
                    {
                        filteredData.add(image);
                    }
                }

                //adapter = new ImageListAdapter(getApplicationContext(), R.layout.list_item, filteredData);
                adapter.addData(filteredData);


                currentlyProcessingAPI.set(false);

                Log.d(TAG, "Size of Dataset: " + data.getData().size());

            }

            @Override
            public void onFailure(Throwable t) {

            }
        };


        api = new ImgurAPI();
        currentlyProcessingAPI.set(true);
        this.call = api.showDefaultGallery(0);
        this.call.enqueue(this.callback);

        //implement button listener
        Button searchButton = (Button) this.findViewById(R.id.button);
        searchButton.setOnClickListener(this);


        //implement onitemclick for listview
        gridView.setOnItemClickListener(this);
        gridView.setClickable(true);

        //add scroll listener
        gridView.setOnScrollListener(this);
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

        this.adapter.reset();

        new CancelTask().execute(this.call);
        EditText text = (EditText) findViewById(R.id.editText);
        this.call = this.api.searchImgur(text.getText().toString(), 0);
        this.call.enqueue(this.callback);

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
        //this part looks disgusting, refactor later if I have time
        int threshhold = (int)Math.ceil(this.adapter.imageLocation * 0.9);
        if(firstVisibleItem + visibleItemCount >= threshhold)
        {
            //call next page api if api is not currently being called already
            if(!this.currentlyProcessingAPI.get())
            {
                this.currentlyProcessingAPI.set(true);
                //if default gallery
                if(this.isDefaultGallery.get())
                {
                    //clean up later, this section is repeated multiple times
                    new CancelTask().execute(this.call);
                    this.call = this.api.showDefaultGallery(this.adapter.pagesLoaded);
                    this.call.enqueue(this.callback);

                    this.adapter.pagesLoaded++;
                }
                else
                {
                    new CancelTask().execute(this.call);
                    EditText text = (EditText) findViewById(R.id.editText);
                    this.call = this.api.searchImgur(text.getText().toString(), this.adapter.pagesLoaded);
                    this.call.enqueue(this.callback);

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


    //for canceling calls until the issue gets fixed
    //https://github.com/square/okhttp/issues/1592
    private class CancelTask extends AsyncTask<Call, Void, Void>
    {
        @Override
        protected Void doInBackground(Call...params)
        {
            Call call = params[0];
            call.cancel();
            return null;
        }
    }
}
