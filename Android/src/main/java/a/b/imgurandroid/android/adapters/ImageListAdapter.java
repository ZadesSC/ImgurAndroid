package a.b.imgurandroid.android.adapters;

import a.b.imgurandroid.android.R;
import a.b.imgurandroid.android.api.pojo.ImageData;
import a.b.imgurandroid.android.viewholders.ImageViewHolder;
import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by zades on 10/9/2015.
 */
public class ImageListAdapter extends ArrayAdapter<ImageData>
{
    private final Context context;
    public List<ImageData> list;

    public ImageListAdapter(Context context, int resource, List<ImageData> images)
    {
        super(context, resource, images);
        this.list = images;
        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        ImageData data = list.get(position);
        ImageViewHolder viewHolder;

        if(view == null)
        {
            view = LayoutInflater.from(this.context).inflate(R.layout.list_item, parent, false);
            viewHolder = new ImageViewHolder();
            viewHolder.image = (ImageView) view.findViewById(R.id.imageView);
            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ImageViewHolder) view.getTag();
        }

        Picasso.with(getContext()).load(data.getLink()).resize(512,512).centerInside().into(viewHolder.image);
        return view;
    }

    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public ImageData getItem(int position)
    {
        return list.get(position);
    }

}
