package a.b.imgurandroid.android.api;

import a.b.imgurandroid.android.adapters.ImageListAdapter;
import a.b.imgurandroid.android.api.pojo.GalleryData;
import a.b.imgurandroid.android.api.pojo.ImageData;
import android.util.Log;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zades on 10/27/2015.
 *
 */
public class ImgurCallback implements Callback<GalleryData>
{
    public static final String TAG = "IMGUR_CALLBACK";

    private ImageListAdapter adapter;
    private AtomicBoolean currentlyProcessingAPI;
    private boolean refreshList;

    public ImgurCallback(ImageListAdapter adapter, AtomicBoolean currentlyProcessingAPI, boolean refreshList)
    {
        this.refreshList = refreshList;
        this.adapter = adapter;
        this.currentlyProcessingAPI = currentlyProcessingAPI;
    }

    @Override
    public void onResponse(Response<GalleryData> response, Retrofit retrofit)
    {
        Log.d(TAG, "onResponse message " + response.message());

        //filter out only pngs and imgs and hope there are no weird cases
        GalleryData data = null;

        if(response == null || response.body() == null)
        {
            Log.e(TAG, "No body in response");
            return;
        }

        data = response.body();
        List<ImageData> filteredData =  new ArrayList<>(data.getData().size());
        for(ImageData image: data.getData())
        {
            if(image.getIsAlbum() == false && (image.getType().equals("image/jpeg") || image.getType().equals("image/png")))
            {
                filteredData.add(image);
            }
        }

        if(this.refreshList)
            this.adapter.reset();

        Log.d(TAG, "Size of Dataset: " + data.getData().size());
        this.adapter.dataSize = data.getData().size();
        this.adapter.addData(filteredData);

        this.currentlyProcessingAPI.set(false);
    }

    @Override
    public void onFailure(Throwable t)
    {
        Log.d(TAG, "Request failed");
        this.currentlyProcessingAPI.set(false);
    }
}
