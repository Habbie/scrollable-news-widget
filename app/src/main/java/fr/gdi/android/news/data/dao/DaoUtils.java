package fr.gdi.android.news.data.dao;

import android.content.Context;
import fr.gdi.android.news.SimpleNewsApplication;
import fr.gdi.android.news.data.DatabaseHelper;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.model.Configuration;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.model.Theme;

public class DaoUtils
{
    public static FeedDao getFeedDao(Context context) 
    {
        return (FeedDao) getDao(context, Feed.class);
    }
    
    
    public static ItemDao getItemDao(Context context) 
    {
        ItemDao dao = (ItemDao) getDao(context, Item.class);
        dao.setFeedDao(getFeedDao(context));
        return dao;
    }

    public static BehaviourDao getBehaviourDao(Context context)
    {
        return (BehaviourDao) getDao(context, Behaviour.class);
    }

    public static ThemeDao getThemeDao(Context context)
    {
        return (ThemeDao) getDao(context, Theme.class);
    }
    
    public static ConfigurationDao getConfigurationDao(Context context)
    {
        ConfigurationDao dao = (ConfigurationDao) getDao(context, Configuration.class);
        dao.setFeedDao(getFeedDao(context));
        dao.setThemeDao(getThemeDao(context));
        dao.setBehaviourDao(getBehaviourDao(context));
        return dao;
    }
    
    public static <T> AbstractDao<T> getDao(Context context, Class<T> c)
    {
        return ((SimpleNewsApplication) context.getApplicationContext()).getDao(c);
    }
    
    public static String getDatabasePath(Context context)
    {
        DatabaseHelper helper = ((SimpleNewsApplication) context.getApplicationContext()).getDatabaseHelper();
        return helper.getReadableDatabase().getPath();
    }
}
