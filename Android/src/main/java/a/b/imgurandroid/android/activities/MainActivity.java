package a.b.imgurandroid.android.activities;

import a.b.imgurandroid.android.R;
import a.b.imgurandroid.android.adapters.ImageListAdapter;
import a.b.imgurandroid.android.api.ImgurAPI;
import a.b.imgurandroid.android.api.ImgurCallback;
import a.b.imgurandroid.android.api.pojo.GalleryData;
import a.b.imgurandroid.android.managers.HistoryManager;
import android.app.Activity;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import retrofit.Call;
import retrofit.Callback;

import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener,AbsListView.OnScrollListener, TextWatcher {

    private final String TAG = "MainActivity";

    private HistoryManager historyManager;
    private SearchTask searchTask;

    private Callback<GalleryData> newSearchCallback;
    private Callback<GalleryData> scrollCallback;
    private Call<GalleryData> call;
    private ImgurAPI api;

    private ImageListAdapter adapter;

    private AtomicBoolean currentlyProcessingAPI;

    private EditText editText;
    private GridView gridView;

    private boolean hitBack = false;  //flag used to check if the user hit back

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.historyManager = new HistoryManager(this);

        //Get editText
        this.editText = (EditText) findViewById(R.id.editText);
        this.searchTask = null;

        //show default gallery
        this.gridView = (GridView) findViewById(R.id.gridView);

        //set default adapter list
        this.adapter = new ImageListAdapter(this.getApplicationContext(), R.layout.list_item);
        this.gridView.setAdapter(adapter);

        this.currentlyProcessingAPI = new AtomicBoolean(false);

        this.newSearchCallback = new ImgurCallback(this.adapter, this.currentlyProcessingAPI, true);
        this.scrollCallback = new ImgurCallback(this.adapter, this.currentlyProcessingAPI, false);

        //Initial api call, if history exists, call that, else call default gallery
        this.api = new ImgurAPI();

        String retrievedHistory = this.historyManager.peek();
        if(retrievedHistory != null && !retrievedHistory.equals(""))
        {
            this.editText.setText(retrievedHistory);
            Selection.setSelection(this.editText.getText(), retrievedHistory.length());
        }

        this.call = this.api.searchImgur(retrievedHistory, 0);
        this.call.enqueue(this.newSearchCallback);

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
//        this.call.enqueue(this.newSearchCallback);

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
        if(firstVisibleItem + visibleItemCount >= threshhold && this.adapter.dataSize != 0)
        {
            //call next page api if api is not currently being called already
            if(!this.currentlyProcessingAPI.get())
            {
                this.currentlyProcessingAPI.set(true);

                Log.d(TAG, "enq from onscroll");

                new CancelTask().execute(call);
                EditText text = (EditText) findViewById(R.id.editText);
                this.adapter.pagesLoaded++;
                this.call = this.api.searchImgur(text.getText().toString(), this.adapter.pagesLoaded);
                this.call.enqueue(this.scrollCallback);
            }
        }

        //Load more empty spaces
        if(firstVisibleItem + visibleItemCount + 5 >= totalItemCount && this.adapter.dataSize != 0)
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
        String editableStr = this.editText.getText().toString().trim();
        Log.d(TAG, "Editable Text: " + editableStr);

        if(s != null)
        {

            if(this.searchTask != null && !this.searchTask.isCancelled())
            {
                this.searchTask.cancel(true);
            }

            this.adapter.reset();
            this.searchTask = new SearchTask();
            this.searchTask.execute(editableStr);
        }

        if(this.hitBack)
        {
            this.hitBack = false;
        }
        else
        {
            //this.historyManager.push(editableStr);
        }
    }

    /**
     * load previous search result
     */
    @Override
    public void onBackPressed() {
        this.hitBack = true;

        //First one on the stack is always the current one
        this.historyManager.pop();
        String retreieveStr = this.historyManager.peek();

        if(retreieveStr == null)
        {
            super.onBackPressed();
            return;
        }
        this.editText.setText(retreieveStr);
        if (retreieveStr.length() > 0)
            this.editText.setSelection(retreieveStr.length());
    }

    /**
     * Created by zades on 10/26/2015.
     *
     * This is prob a very messy way to do it since the retrofit library also uses their own threading, but I couldn't
     * think of another easy way to count time elapse.  Maybe use a custom runnable?  That sounds dangerous though.
     *
     * Should also move thie outside something else
     */
    public class SearchTask extends AsyncTask<String, Void, GalleryData> {

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
                    Thread.sleep(1000);
                    if(this.isCancelled())
                    {
                        return null;
                    }

                    Log.d(TAG, "enq from searchtask");
                    new CancelTask().execute(call);
                    call = api.searchImgur(searchString, 0);
                    call.enqueue(newSearchCallback);
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

        }

        @Override
        protected void onPostExecute(GalleryData data)
        {
            //stores text after a non-cancelled search
            historyManager.push(editText.getText().toString());
        }
    }

    private class CancelTask extends AsyncTask<Call, Void, Void>
    {

        @Override
        protected Void doInBackground(Call... params) {
            Call call = params[0];
            call.cancel();
            return null;
        }
    }
}
