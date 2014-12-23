package fr.gdi.android.news;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.os.Environment;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import fr.gdi.android.news.data.DatabaseHelper;
import fr.gdi.android.news.data.dao.AbstractDao;
import fr.gdi.android.news.data.dao.BehaviourDao;
import fr.gdi.android.news.data.dao.ConfigurationDao;
import fr.gdi.android.news.data.dao.FeedDao;
import fr.gdi.android.news.data.dao.ItemDao;
import fr.gdi.android.news.data.dao.ThemeDao;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.model.Configuration;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.model.Theme;
import fr.gdi.android.news.preference.utils.PreferenceUtils;
import fr.gdi.android.news.utils.IOUtils;

public class SimpleNewsApplication extends Application
{
    private static final String THEME_HC_TITLE_UPDATED = "theme_hc_title_updated"; //$NON-NLS-1$

    private DatabaseHelper helper;
    
    private static final Map<Class<?>, Class<?>> DAO_TYPES = new HashMap<Class<?>, Class<?>>();
    private Map<Class<?>, AbstractDao<?>> daos = new HashMap<Class<?>, AbstractDao<?>>();
    
    static {
        DAO_TYPES.put(Feed.class, FeedDao.class);
        DAO_TYPES.put(Item.class, ItemDao.class);
        DAO_TYPES.put(Theme.class, ThemeDao.class);
        DAO_TYPES.put(Behaviour.class, BehaviourDao.class);
        DAO_TYPES.put(Configuration.class, ConfigurationDao.class);
    }
    
    private File externalCache;
    
    @Override
    public void onCreate()
    {
        helper = getDatabaseHelper();
        
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        {
            File cacheRoot = IOUtils.getCacheDir();

            externalCache = new File(cacheRoot, "data"); //$NON-NLS-1$
            
            boolean isExternalCacheAvailable = true;
            if (!externalCache.exists())
            {
                isExternalCacheAvailable = externalCache.mkdirs();
            }
            if (!isExternalCacheAvailable)
            {
                externalCache = null;
            }
        }
        
        forceWidgetTitleOnHoneyComb();
        
        super.onCreate();
    }

    private void forceWidgetTitleOnHoneyComb()
    {
        boolean titleUpdated = PreferenceUtils.getBoolean(this, THEME_HC_TITLE_UPDATED, false);
        if ( VERSION.SDK_INT <= VERSION_CODES.HONEYCOMB_MR2 && !titleUpdated ) 
        {
            helper.executeUpdate("update theme set showWidgetTitle=?", new Object[] { true }); //$NON-NLS-1$
            PreferenceUtils.setBoolean(this, THEME_HC_TITLE_UPDATED, true);
        }
    }
    
    @Override
    public File getCacheDir()
    {
        if (externalCache != null)
        {
            return externalCache;
        }
        else
        {
            return super.getCacheDir();
        }
    }
    
    @Override
    public void onTerminate()
    {
        helper.close();
    }
    
    public synchronized DatabaseHelper getDatabaseHelper()
    {
        if ( helper == null ) 
        {
            helper = new DatabaseHelper(this);
        }
        return helper;
    }
    
    @SuppressWarnings("all")
    public synchronized <T> AbstractDao<T> getDao(Class<T> c)
    {
        return getDao(c, false);
    }
            
    
    @SuppressWarnings("all")
    public synchronized <T> AbstractDao<T> getDao(Class<T> c, boolean reuse)
    {
        if ( !reuse ) 
        {
            return instantiateDao(c);
        }
        
        if ( !daos.containsKey(c) && DAO_TYPES.containsKey(c) ) 
        {
            AbstractDao dao = instantiateDao(c);
            if ( dao != null ) 
            {
                daos.put(c, dao);
            }
        }
        return (AbstractDao<T>) daos.get(c);
    }
    
    @SuppressWarnings("all")
    private <T> AbstractDao<T> instantiateDao(Class<T> c) 
    {
        try
        {
            AbstractDao dao = (AbstractDao) DAO_TYPES.get(c).getConstructor(DatabaseHelper.class).newInstance(getDatabaseHelper());
            return dao;
        }
        catch (Exception e)
        {
            Log.e(Constants.PACKAGE, "Unable to instantiate daos for " + c.getName(), e); //$NON-NLS-1$
            return null;
        }
    }
}
