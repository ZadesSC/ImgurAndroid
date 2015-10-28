package a.b.imgurandroid.android.activities;

import a.b.imgurandroid.android.R;
import a.b.imgurandroid.android.adapters.ImageListAdapter;
import a.b.imgurandroid.android.api.ImgurAPI;
import a.b.imgurandroid.android.api.ImgurCallback;
import a.b.imgurandroid.android.api.pojo.GalleryData;
import a.b.imgurandroid.android.api.pojo.ImageData;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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


public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener,AbsListView.OnScrollListener, TextWatcher {

    private final String TAG = "MainActivity";

    public static final String PREFERENCES = "myPrefs";
    public static final String LAST_SEARCH = "lastSearch";

    private SharedPreferences preferences;
    private SearchTask searchTask;

    private Callback<GalleryData> callback;
    private Call<GalleryData> call;
    private ImgurAPI api;

    private ImageListAdapter adapter;

    private AtomicBoolean currentlyProcessingAPI;
    private AtomicBoolean isDefaultGallery;

    private EditText editText;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        //Get editText
        this.editText = (EditText) findViewById(R.id.editText);
        this.searchTask = null;

        //show default gallery
        this.gridView = (GridView) findViewById(R.id.gridView);

        //set default adapter list
        this.adapter = new ImageListAdapter(this.getApplicationContext(), R.layout.list_item, new ArrayList<ImageData>(ImageListAdapter.DEFAULT_SIZE));
        this.gridView.setAdapter(adapter);

        this.currentlyProcessingAPI = new AtomicBoolean(false);
        this.isDefaultGallery = new AtomicBoolean(true);

        this.callback = new ImgurCallback(this.adapter, this.currentlyProcessingAPI);

        //Initial api call, if history exists, call that, else call default gallery
        this.api = new ImgurAPI();

        String history = this.preferences.getString(LAST_SEARCH, "");

        if(!history.equals(""))
        {
            this.isDefaultGallery.set(false);
            this.adapter.reset();
            this.call = this.api.searchImgur(history, 0);
            this.call.enqueue(this.callback);

            this.editText.setText(history);
        }
        else
        {
            currentlyProcessingAPI.set(true);
            this.call = api.showDefaultGallery(0);
            this.call.enqueue(this.callback);
        }

        //Listeners
        this.gridView.setOnItemClickListener(this);
        this.gridView.setClickable(true);
        this.gridView.setOnScrollListener(this);
        this.editText.addTextChangedListener(this);
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
//        this.isDefaultGallery.set(false);
//
//        this.adapter.reset();
//
//        new CancelTask().execute(this.call);
//        EditText text = (EditText) findViewById(R.id.editText);
//        this.call = this.api.searchImgur(text.getText().toString(), 0);
//        this.call.enqueue(this.callback);

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
        int threshhold = (int)Math.ceil(totalItemCount * 0.9);
        if(firstVisibleItem + visibleItemCount >= threshhold)
        {
            //call next page api if api is not currently being called already
            if(!this.currentlyProcessingAPI.get())
            {
                this.currentlyProcessingAPI.set(true);
                //if default gallery
                if(this.isDefaultGallery.get())
                {
                    //clean up later, this code snippet is repeated multiple times
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s)
    {
        if(s != null && s.length() > 0 && !this.editText.getText().toString().trim().equals(""))
        {

            if(this.searchTask != null && !this.searchTask.isCancelled())
            {
                this.searchTask.cancel(true);
            }

            this.searchTask = new SearchTask();
            this.searchTask.execute(this.editText.getText().toString());

            //add to history, even if it fails
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(LAST_SEARCH, this.editText.getText().toString());
            editor.commit();
        }
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

    /**
     * Created by zades on 10/26/2015.
     *
     * This is prob a very messy way to do it since the retrofit library also uses their own threading, but I couldn't
     * think of another easy way to count time elapse.  Maybe use a custom runnable?  That sounds dangerous though.
     *
     * Should also move thie outside something else
     */
    public class SearchTask extends AsyncTask<String, Void, GalleryData>
    {
        private AtomicBoolean pastSleep = new AtomicBoolean(false);

        @Override
        protected void onPreExecute()
        {
            adapter.reset();
        }

        @Override
        protected GalleryData doInBackground(String... params)
        {
            String searchString = params[0];


            //I don't know about this.  Looks ugly.  Has to be better way :/
            //Waits the async thread for half a second.  If more input is entered, this task will be canceled, else
            //it'll go ahead with the api call to imgur
            if(searchString != null)
            {
                try
                {
                    Thread.sleep(500);
                    pastSleep.set(true);

                    //reppeated code from onClick, should clean up later
                    //I think retrofit is thread safe...
                    isDefaultGallery.set(false);

                    //adapter.reset();

                    new CancelTask().execute(call);
                    call = api.searchImgur(searchString, 0);
                    call.enqueue(callback);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onCancelled()
        {
            //ideally we would return the user to the previous search results, but for now I want to get this working
            if(pastSleep.get())
            {
                adapter.reset();
                new CancelTask().execute(call);
            }
        }

    }
}
