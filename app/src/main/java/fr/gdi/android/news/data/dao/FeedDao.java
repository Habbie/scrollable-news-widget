package fr.gdi.android.news.data.dao;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.data.DatabaseHelper;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Item;

public class FeedDao extends AbstractDao<Feed>
{
    public FeedDao(DatabaseHelper database)
    {
        super(database);
    }
    
    public List<Feed> getAll()
    {
        return getFeeds("", new Object[0]); //$NON-NLS-1$
    }
    
    public List<Feed> getFeedsByWidget(int appWidgetId)
    {
        String sql = "select " + //$NON-NLS-1$
            "f._id, f.url, f.homepage, f.type, f.title, f.refreshDate, " + //$NON-NLS-1$
            "f.orderno, f.enabled, f.description, f.image_url, f.color " + //$NON-NLS-1$
            "from config_feed cf " + //$NON-NLS-1$
            "left join feed f on f._id=cf.feed_id " + //$NON-NLS-1$
            "left join config c on c._id=cf.config_id " + //$NON-NLS-1$
            "where c.widget_id=?" + //$NON-NLS-1$
            "order by upper(f.title)"; //$NON-NLS-1$
        
        return execute(sql, new Object[] { appWidgetId });
    }
    
    
    
    public List<Feed> getFeeds(String whereClause, Object[] params)
    {
        
        String sql = "select " + //$NON-NLS-1$
            		 "f._id, f.url, f.homepage, f.type, f.title, f.refreshDate, " + //$NON-NLS-1$
            		 "f.orderno, f.enabled, f.description, f.image_url, f.color " + //$NON-NLS-1$
            		 "from feed f " + whereClause + " " + //$NON-NLS-1$ //$NON-NLS-2$
            		 "order by upper(f.title)"; //$NON-NLS-1$
       
        return execute(sql, params);
        
    }
    
    private List<Feed> execute(String sql, Object[] params) 
    {
        List<Feed> feeds = new ArrayList<Feed>();
        
        Cursor cursor = null;
        
        try
        {
            cursor = database.executeQuery(sql, params);
            while ( cursor.moveToNext() ) 
            {
                Feed f = getFeed(cursor);
                feeds.add(f);
            }
        }
        finally 
        {
            if (cursor != null) cursor.close();
        }
        
        return feeds;
    }
    
    public Feed getById(long feedId)
    {
        List<Feed> feeds = getFeeds("where _id=?", new Object[] { feedId }); //$NON-NLS-1$
        return feeds.size() > 0 ? feeds.get(0) : null;
    }
    
    public void insert(List<Feed> feeds) 
    {
        for (Feed feed : feeds)
        {
            insert(feed);
        }
    }
    
    public long insert(Feed feed)
    {
        String title = feed.getTitle(), 
            url = feed.getURL().toString(), 
            type = feed.getType(), 
            homepage = feed.getHomePage() != null ? feed.getHomePage().toString() : null,
            imageUrl = feed.getImageUrl() != null ? feed.getImageUrl().toString() : null;

        ContentValues values = new ContentValues();
        
        values.put("title", title); //$NON-NLS-1$
        values.put("url", url); //$NON-NLS-1$
        values.put("type", type); //$NON-NLS-1$
        values.put("homepage", homepage); //$NON-NLS-1$
        values.put("image_url", imageUrl); //$NON-NLS-1$
        values.put("color", feed.getColor()); //$NON-NLS-1$
        
        long id = database.getWritableDatabase().insert("feed", null, values); //$NON-NLS-1$
        
        feed.setId(id);
        
        return id;
    }
    
    public void delete(Feed feed)
    {
        delete(feed.getId());
    }
    

    public void delete(long id)
    {
        database.executeUpdate("delete from feed where _id=?", new Object[] { id }); //$NON-NLS-1$
    }
    
    public void update(Feed feed, boolean deleteItems)
    {
        String title = feed.getTitle(), 
            url = feed.getURL().toString(), 
            type = feed.getType(), 
            homepage = feed.getHomePage() != null ? feed.getHomePage().toString() : null,
            imageUrl = feed.getImageUrl() != null ? feed.getImageUrl().toString() : null;;
            
        database.executeUpdate("update feed set title=?, url=?, type=?, homepage=?, image_url=?, color=? where _id=?", new Object[] { title, url, type, homepage, imageUrl, feed.getColor(), feed.getId() }); //$NON-NLS-1$
        
        if ( deleteItems ) 
        {
            database.executeUpdate("delete from item where feed_id=?", new Object[] { feed.getId() }); //$NON-NLS-1$
        }
    }
    

    public void updateRefreshDate(Feed feed)
    {
        if ( feed.getRefresh() != null ) 
        {
            database.executeUpdate("update feed set refreshDate=? where _id=?", new Object[] { feed.getRefresh().getTime(), feed.getId() }); //$NON-NLS-1$
        }
    }
    
    //magic numbers
    private Feed getFeed(Cursor cursor)
    {
        int mId = cursor.getInt(0);
        String mTitle = cursor.getString(4);
        String mType = cursor.getString(3);
        String imageUrl = cursor.getString(9); 
        
        URL mUrl = getUrl(cursor, 1);
        URL mHomePage = getUrl(cursor, 2);
        
        Date mRefresh = getDate(cursor, 5);
        
        boolean mEnabled = getBoolean(cursor, 7);
        int mOrder = cursor.getInt(6);
        
        Feed feed = new Feed(mId, mUrl, mHomePage, mTitle, mType, mRefresh, mEnabled, new ArrayList<Item>(), mOrder);
        feed.setImageUrl(imageUrl);
        if ( !cursor.isNull(10) ) feed.setColor(cursor.getInt(10));
        
        return feed;
    }

    public void deleteAll()
    {
        database.executeUpdate("delete from feed", new Object[] { }); //$NON-NLS-1$
    }


    public String getMostActiveFeed()
    {
        String sql = "select count(i._id) as c, f.title from item i " + //$NON-NLS-1$
        		"left join feed f on f._id=i.feed_id " + //$NON-NLS-1$
        		"group by i.feed_id order by c desc limit 1"; //$NON-NLS-1$
        
        Cursor cursor = null;
        String title = "Unknown"; //$NON-NLS-1$
        try
        {
            cursor = database.executeQuery(sql, new Object[0]);
            if ( cursor.moveToNext() ) 
            {
                title = cursor.getString(1);
            }
        }
        catch ( Exception e ) 
        {
            Log.e(Constants.PACKAGE, "Unable to retrieve most active feed title", e); //$NON-NLS-1$
        }
        finally
        {
            if ( cursor != null ) cursor.close();
        }

        return title;
        
        
    }
    
    @Override
    protected String getTableName()
    {
        return "feed"; //$NON-NLS-1$
    }

    public void resetAllDates()
    {
        database.executeUpdate("update feed set refreshDate=NULL", new Object[0]); //$NON-NLS-1$
    }

    public void resetFeedThumbnails()
    {
        database.executeUpdate("update feed set image_url=NULL", new Object[0]); //$NON-NLS-1$
    }
    
}
