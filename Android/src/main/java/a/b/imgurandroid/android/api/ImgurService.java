package a.b.imgurandroid.android.api;

import a.b.imgurandroid.android.api.pojo.GalleryData;
import a.b.imgurandroid.android.api.pojo.ImageData;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.Path;
import retrofit.http.Query;

import java.util.List;


/**
 * Created by zades on 10/7/2015.
 */
public interface ImgurService {

    @GET("/gallery/hot/viral/{page}")
    void getGallery(
            @Path("page") int page, Callback<GalleryData> callback
            );

    @GET("/gallery/search/time/{page}")
    void getSearchResults(
            @Query("q") String search, @Path("page") int page, Callback<GalleryData> callback
            );
}
