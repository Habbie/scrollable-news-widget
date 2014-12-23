package fr.gdi.android.news.data.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.data.DatabaseHelper;
import fr.gdi.android.news.model.Configuration;
import fr.gdi.android.news.model.Feed;

public class ConfigurationDao extends AbstractDao<Configuration>
{

    private FeedDao feedDao;
    private ThemeDao themeDao;
    private BehaviourDao behaviourDao;
    
    public ConfigurationDao(DatabaseHelper database)
    {
        super(database);
    }
    
    public List<Configuration> getConfigurationByWidgetIds(int[] appWidgetIds)
    {
        Object[] params = new Object[appWidgetIds.length]; 
        
        StringBuilder whereClause = new StringBuilder("widget_id in ("); //$NON-NLS-1$
        
        for (int i = 0; i < appWidgetIds.length; i++)
        {
            if ( i != 0 ) whereClause.append(", "); //$NON-NLS-1$
            whereClause.append("?"); //$NON-NLS-1$
            params[i] = appWidgetIds[i];
        }
        
        whereClause.append(")"); //$NON-NLS-1$
        
        return select(whereClause.toString(), params);
    }
    
    public Configuration getConfigurationByWidgetId(int appWidgetId)
    {
        List<Configuration> configs = select("widget_id=?", new Object[] { appWidgetId }); //$NON-NLS-1$
        return configs.size() > 0 ? configs.get(0) : null;
    }

    private List<Configuration> select(String whereClause, Object[] params)
    {
        String sql = "select _id, widget_id, theme_id, behaviour_id, widget_title " + //$NON-NLS-1$
        		"from config where " + whereClause; //$NON-NLS-1$
        
        Cursor cursor = null;
        
        List<Configuration> configs = new ArrayList<Configuration>();

        try 
        {
            cursor = database.executeQuery(sql, params);
            
            Configuration config = null;
            
            while ( cursor.moveToNext() ) 
            {
                config = getConfiguration(cursor);
                List<Feed> feeds = feedDao.getFeedsByWidget(config.getAppWidgetId());
                config.setFeeds(feeds);
                configs.add(config);
            }
        }
        catch (Exception e) 
        {
            Log.e(Constants.PACKAGE, "Unable to create Configuration from Cursor", e); //$NON-NLS-1$
        }
        finally 
        {
            if ( cursor != null ) cursor.close();
        }

        return configs;
    }


    public long saveOrInsert(Configuration configuration)
    {
        if ( configuration.getId() == 0 ) 
        {
            long id = database.getWritableDatabase().insert("config", null, getContentValues(configuration)); //$NON-NLS-1$
            configuration.setId(id);
        }
        else 
        {
            database.getWritableDatabase().update("config", getContentValues(configuration), "_id=?", new String[] {Long.toString(configuration.getId())}); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        updateConfigFeeds(configuration);
        
        return configuration.getId();
    }
    
    
    private void updateConfigFeeds(Configuration configuration)
    {
        database.executeUpdate("delete from config_feed where config_id=?", new Object[] {configuration.getId()}); //$NON-NLS-1$
        List<Feed> feeds = configuration.getFeeds();
        String insertFeedRefSql = "insert into config_feed (config_id, feed_id) values (?, ?)"; //$NON-NLS-1$
        for (Feed feed : feeds)
        {
            Feed f = feedDao.getById(feed.getId());
            if ( f != null ) database.executeUpdate(insertFeedRefSql, new Object[] { configuration.getId(), feed.getId() }); 
        }
    }

    private ContentValues getContentValues(Configuration configuration)
    {
        ContentValues cv = new ContentValues();
        
        cv.put("theme_id", configuration.getTheme().getId()); //$NON-NLS-1$
        cv.put("behaviour_id", configuration.getBehaviour().getId()); //$NON-NLS-1$
        cv.put("widget_id", configuration.getAppWidgetId()); //$NON-NLS-1$
        cv.put("widget_title", configuration.getWidgetTitle()); //$NON-NLS-1$
        
        return cv;
    }

    //n+2 queries. should be ok though
    private Configuration getConfiguration(Cursor cursor)
    {
        Configuration config = new Configuration(database.getContext());
        
        config.setId(cursor.getLong(0));
        config.setAppWidgetId(cursor.getInt(1));
        
        config.setTheme(themeDao.getTheme(cursor.getInt(2)));
        config.setBehaviour(behaviourDao.getBehaviour(cursor.getInt(3)));
        
        if ( !cursor.isNull(4) ) config.setWidgetTitle(cursor.getString(4));
        
        return config;
    }
    
    public void setFeedDao(FeedDao feedDao)
    {
        this.feedDao = feedDao;
    }
    
    public void setThemeDao(ThemeDao themeDao)
    {
        this.themeDao = themeDao;
    }
    
    public void setBehaviourDao(BehaviourDao behaviourDao)
    {
        this.behaviourDao = behaviourDao;
    }

    public void removeConfiguration(Configuration configuration)
    {
        database.executeUpdate("delete from config where _id=?", new Object[] { configuration.getId() }); //$NON-NLS-1$
    }

    public void removeConfiguration(int appWidgetId)
    {
        database.executeUpdate("delete from config where widget_id=?", new Object[] { appWidgetId }); //$NON-NLS-1$
    }
    
    public int[] getConfiguredWidgetIds()
    {
        String sql = "select distinct widget_id from config"; //$NON-NLS-1$
        
        Cursor cursor = null;
        
        try
        {
            List<Integer> idList = new ArrayList<Integer>();
            cursor = database.executeQuery(sql, new Object[0]);
            if ( cursor.moveToNext() ) 
            {
                idList.add(cursor.getInt(0));
            }
            
            int[] ids = new int[idList.size()];
            for (int u = 0; u < idList.size(); u++)
            {
                Integer id = idList.get(u);
                ids[u] = id;
            }
            
            return ids;
        }
        catch ( Exception e ) 
        {
            Log.e(Constants.PACKAGE, "Unable to retrieve configured widget ids", e); //$NON-NLS-1$
            return new int[0];
        }
        finally
        {
            if ( cursor != null ) cursor.close();
        }
    }

    @Override
    protected String getTableName()
    {
        return "config"; //$NON-NLS-1$
    }
    
}
