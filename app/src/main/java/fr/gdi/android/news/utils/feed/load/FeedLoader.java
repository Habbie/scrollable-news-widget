package fr.gdi.android.news.utils.feed.load;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.FeedDao;
import fr.gdi.android.news.data.dao.ItemDao;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.preference.utils.PreferenceUtils;
import fr.gdi.android.news.utils.IOUtils;
import fr.gdi.android.news.utils.feed.load.FeedLoaderResult.State;
import fr.gdi.android.news.utils.feed.parse.FeedHandler;

public class FeedLoader
{
    public FeedLoaderResult loadFeed(Context context, Feed f) 
    {
        return loadFeed(context, f, false);
    }
    
    public FeedLoaderResult loadFeed(Context context, Feed f, boolean force) 
    {
        Date refreshDate = f.getRefresh();
        
        if ( !force && isFreshEnough(context, refreshDate) )
        {
            return new FeedLoaderResult(f, State.FRESH_ENOUGH);
        }
        
        FeedLoaderResult result = loadFeed(context, f.getURL());
        
        if ( result.getFeed() != null ) result.getFeed().setId(f.getId());
        
        return result;
    }

    public FeedLoaderResult loadFeed(Context context, URL url)
    {
        HttpURLConnection conn = null;
        
        try 
        {
            FeedHandler handler = new FeedHandler(context);
            
            conn = (HttpURLConnection) url.openConnection();
            
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", IOUtils.DEFAULT_USER_AGENT); //$NON-NLS-1$
            
            Feed feed = handler.handleFeed(conn);
            feed.setRefresh(new Date());
            
            return new FeedLoaderResult(feed, State.SUCCESS);
        }
        catch ( Exception e ) 
        {
            return new FeedLoaderResult(null, State.ERROR);
        }
        finally 
        {
            try { if ( conn != null ) conn.disconnect(); } catch ( Exception e ) { } 
        }
    }
    
    public List<Feed> loadAndSave(Context context, Map<Feed, Boolean> feeds, boolean force)
    {
        FeedDao feedDao = DaoUtils.getFeedDao(context);
        ItemDao itemDao = DaoUtils.getItemDao(context);
        
        List<Feed> results = new ArrayList<Feed>();
        List<Item> localItems = new ArrayList<Item>();
        
        for (Feed feed : feeds.keySet())
        {
            Boolean clear = feeds.get(feed);
            
            FeedLoaderResult result = null;
            
            try 
            {
                result = this.loadFeed(context, feed, force);            
            }
            catch ( Exception e ) 
            {
                Log.e(Constants.PACKAGE, "Unable to connect to feed", e); //$NON-NLS-1$
            }
            
            if ( result != null && result.isSuccess() ) 
            {
                try
                {
                    Feed resultFeed = result.getFeed();
                    feed.setRefresh(resultFeed.getRefresh());
                    feed = resultFeed;
                    
                    results.add(feed);
                    
                    feedDao.updateRefreshDate(feed);
                    
                    if ( clear != null && clear ) 
                    {
                        itemDao.deleteFeedItems(true, new Feed[] { feed });
                    }
                    
                    localItems.addAll(feed.getItems());
                }
                catch ( Exception e ) 
                {
                    Log.e(Constants.PACKAGE, "Unable to save feed items", e); //$NON-NLS-1$
                }            
            }
        }

        itemDao.save(localItems);
        
        return results;
    }
    
    private boolean isFreshEnough(Context context, Date refreshDate)
    {
        int refreshInterval = PreferenceUtils.getRefreshInterval(context); 
        if ( refreshDate != null && getMinuteDifference(refreshDate, new Date()) < refreshInterval )
        {
            return true;
        }
        return false;
    }
    
    private long getMinuteDifference(Date d1, Date d2) 
    {
        long millis = d2.getTime() - d1.getTime();
        return Math.abs(millis/(1000*60));
    }
    
    
}
