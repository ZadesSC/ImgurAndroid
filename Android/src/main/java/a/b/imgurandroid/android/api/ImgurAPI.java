package a.b.imgurandroid.android.api;

import a.b.imgurandroid.android.api.pojo.GalleryData;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

import java.util.List;

/**
 * Created by zades on 10/7/2015.
 */
public class ImgurAPI {

    private String endpoint = "https://api.imgur.com/3";
    private String clientId = "69654bb494e403e";

    private final RestAdapter restAdapter;
    private final ImgurService service;

    public ImgurAPI()
    {
        this.restAdapter = new RestAdapter.Builder()
                .setEndpoint(this.endpoint)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade requestFacade) {
                        requestFacade.addHeader("Authorization", "Client-ID " + clientId);
                    }
                }).build();

        this.service = restAdapter.create(ImgurService.class);
    }

    public void searchImgur(Callback<GalleryData> callback, String search, int page)
    {
        this.service.getSearchResults(search, page, callback);
    }

    public void showDefaultGallery(Callback<GalleryData> callback, int page)
    {
        this.service.getGallery(page, callback);
    }
}
