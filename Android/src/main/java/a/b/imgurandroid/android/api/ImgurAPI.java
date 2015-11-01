package a.b.imgurandroid.android.api;

import a.b.imgurandroid.android.api.pojo.GalleryData;
import a.b.imgurandroid.android.inteceptors.LoggingInterceptor;
import com.squareup.okhttp.OkHttpClient;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

import javax.security.auth.callback.Callback;

/**
 * Created by zades on 10/7/2015.
 */
public class ImgurAPI {

    private String endpoint = "https://api.imgur.com/3/";
    private String clientId = "69654bb494e403e";

    private final Retrofit retrofit;
    private final ImgurService service;

    public ImgurAPI()
    {
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new LoggingInterceptor());

        this.retrofit = new Retrofit.Builder()
                .baseUrl(this.endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        this.service = retrofit.create(ImgurService.class);
    }

    /**
     * Searches imgur, if strinig is empty, return default gallery
     * @param search
     * @param page
     * @return
     */
    public Call<GalleryData> searchImgur(String search, int page)
    {
        if(search == null ||search.equals(""))
        {
            return this.service.getGallery(page, "Client-ID " + this.clientId);
        }
        else
        {
            return this.service.getSearchResults(page, "Client-ID " + this.clientId, search);
        }
    }
}
