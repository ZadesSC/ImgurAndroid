package a.b.imgurandroid.android.api;

import a.b.imgurandroid.android.api.pojo.GalleryData;
import a.b.imgurandroid.android.api.pojo.ImageData;
import retrofit.Call;
import retrofit.Callback;
import retrofit.http.*;

import java.util.List;


/**
 * Created by zades on 10/7/2015.
 */
public interface ImgurService {

    @GET("gallery/hot/viral/{page}")
    Call<GalleryData> getGallery(
            @Path("page") int page, @Header("Authorization") String auth
            );

    @GET("gallery/search/time/{page}")
    Call<GalleryData> getSearchResults(
            @Path("page") int page, @Header("Authorization") String auth, @Query("q") String search
            );
}
