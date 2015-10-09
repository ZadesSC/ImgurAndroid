package a.b.imgurandroid.android.activities;

import a.b.imgurandroid.android.R;
import a.b.imgurandroid.android.adapters.ImageListAdapter;
import a.b.imgurandroid.android.api.ImgurAPI;
import a.b.imgurandroid.android.api.pojo.GalleryData;
import a.b.imgurandroid.android.api.pojo.ImageData;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zades on 10/9/2015.
 */
public class ImageActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //setup image
        String url = this.getIntent().getStringExtra("URL");
        ImageView imageView = (ImageView) this.findViewById(R.id.imageActivtyImageView);
        imageView.setImageDrawable(null);

        Picasso.with(this)
                .load(url)
                .into(imageView);
    }
}
