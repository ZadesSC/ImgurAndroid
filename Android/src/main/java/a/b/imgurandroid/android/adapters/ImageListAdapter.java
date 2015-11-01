package a.b.imgurandroid.android.adapters;

import a.b.imgurandroid.android.R;
import a.b.imgurandroid.android.api.pojo.ImageData;
import a.b.imgurandroid.android.viewholders.ImageViewHolder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zades on 10/9/2015.
 */
public class ImageListAdapter extends ArrayAdapter<ImageData>
{
    public static final int DEFAULT_SIZE = 10;

    private final Context context;
    public List<ImageData> list;
    public int imageLocation;
    public int pagesLoaded; //for paging
    public int dataSize;

    public ImageListAdapter(Context context, int resource) {
        super(context, resource, new ArrayList<ImageData>());

        //create and fill custom arraylist
        this.list = new ArrayList<>(DEFAULT_SIZE);
        for (int x = 0; x < DEFAULT_SIZE; x++)
        {
            this.list.add(new ImageData());
        }

        this.context = context;
        this.imageLocation = 0;
        this.pagesLoaded = 0;
        this.dataSize = 0;
        this.notifyDataSetChanged();
    }

    //Called to reset adapter for new data
    public synchronized void reset()
    {
        this.list = new ArrayList<ImageData>(DEFAULT_SIZE);
        this.imageLocation = 0;
        this.pagesLoaded = 0;
        this.notifyDataSetChanged();
    }

    /**
     * adds images from api call
     * @param images
     */
    public synchronized void addData(List<ImageData> images)
    {
        for(ImageData data: images)
        {
            if(this.imageLocation >= this.list.size())
            {
                this.loadMoreData();
            }
            this.list.set(this.imageLocation, data);
            this.imageLocation++;
        }
        this.notifyDataSetChanged();
    }

    /**
     * Loads empty spaces
     */
    public synchronized void loadMoreData()
    {
        this.list.add(new ImageData());
        this.notifyDataSetChanged();
        //Log.d("loadmoredata", "test");
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


        Picasso.with(getContext()).load(data.getLink()).fit().centerCrop().into(viewHolder.image);

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
