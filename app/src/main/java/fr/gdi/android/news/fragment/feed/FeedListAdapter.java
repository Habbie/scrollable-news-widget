package fr.gdi.android.news.fragment.feed;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.SortableAdapter;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.feed.load.ConnectFeedTask;

public class FeedListAdapter extends SortableAdapter<Feed>
{
    private int viewResourceId;
    
    private FeedBinder binder;
    
    public FeedListAdapter(Context context, List<Feed> feeds)
    {
        super(context, R.layout.feed_list_row, feeds);
        
        super.sort();

        this.viewResourceId = R.layout.feed_list_row;
        this.binder = new FeedBinder();
        
        setNotifyOnChange(true);
    }
    
    
    
    public FeedListAdapter(Context context, List<Feed> feeds, boolean hideImage)
    {
        this(context, feeds);
        this.binder.setImageHidden(hideImage);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        
        final Feed feed = getItem(position);
        if (feed != null)
        {
            binder.bind(v, feed);
            ImageView imageView = (ImageView) v.findViewById(R.id.feedImage);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    new ConnectFeedTask(getContext()).execute(feed);
                }
            });
        }
        
        return v;
    }

    
}
