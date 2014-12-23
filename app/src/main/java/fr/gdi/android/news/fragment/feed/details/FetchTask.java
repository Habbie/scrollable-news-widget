package fr.gdi.android.news.fragment.feed.details;

import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.feed.load.FeedLoader;

public class FetchTask extends AsyncTask<URL, Void, Feed>
{
    private Context context;
    
    private FetchTaskListener listener;
    
    private FeedLoader loader;
    
    public FetchTask(Context context, FetchTaskListener cb)
    {
        this.context = context;
        this.listener = cb;
        this.loader = new FeedLoader();
    }
    
    @Override
    protected Feed doInBackground(URL... params)
    {
        if ( params.length == 0 ) 
        {
            throw new IllegalArgumentException("WidgetReloadTask needs an URL input (received none)."); //$NON-NLS-1$
        }
        
        URL url = params[0];
        
        return loader.loadFeed(context, url).getFeed();
    }
    
    @Override
    protected void onPreExecute()
    {
        if ( listener != null ) listener.onPreExecute();
    }
    
    @Override
    protected void onPostExecute(Feed result)
    {
        if ( listener != null ) 
        {
            listener.onPostExecute(result);
        }
    }
}
