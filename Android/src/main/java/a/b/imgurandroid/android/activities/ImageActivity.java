package a.b.imgurandroid.android.activities;

import a.b.imgurandroid.android.R;
import android.app.Activity;
import android.os.Bundle;

import android.widget.ImageView;
import com.squareup.picasso.Picasso;


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
