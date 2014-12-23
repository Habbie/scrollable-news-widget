package fr.gdi.android.news.preference.utils;

import java.util.Date;

import fr.gdi.android.news.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class PreferenceUtils
{
    private static final String LAST_WIDGET_UPDATE_PREFIX = "LAST_WIDGET_UPDATE_"; //$NON-NLS-1$
    public static final String REFRESH_INTERVAL = "refresh_interval_in_minutes"; //$NON-NLS-1$
    private static final String AUTO_MARK_AS_READ = "auto_mark_as_read"; //$NON-NLS-1$
    private static final String UPDATE_IF_WIFI = "only_update_if_wifi_on"; //$NON-NLS-1$
    private static final String EMBEDDED_IMAGE_MIN_WIDTH = "embedded_image_min_width"; //$NON-NLS-1$
    private static final String EAGER_FETCH_THUMBNAILS = "eager_fetch_thumbnails";  //$NON-NLS-1$
    private static final String DEBUG_MODE = "debug_mode"; //$NON-NLS-1$
    private static final String PREFERE_SHAPE_OVER_NINEPATCH = "prefer_shape_over_ninepatch"; //$NON-NLS-1$
    
    private static final String DEFAULT_REFRESH_INTERVAL = "180"; //$NON-NLS-1$
    private static final String DEFAULT_EMBEDDED_IMAGE_MIN_WIDTH = "240"; //$NON-NLS-1$
    
    
    public static int getRefreshInterval(Context context)
    {
        return getInteger(context, REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL);
    }
    
    public static void setRefreshInterval(Context context, int interval)
    {
        setInteger(context, REFRESH_INTERVAL, interval);
    }
    
    public static int getEmbeddedImageMinWidth(Context context)
    {
        return getInteger(context, EMBEDDED_IMAGE_MIN_WIDTH, DEFAULT_EMBEDDED_IMAGE_MIN_WIDTH);
    }
    
    public static void setEmbeddedImageMinWidth(Context context, int val)
    {
        setInteger(context, EMBEDDED_IMAGE_MIN_WIDTH, val);
    }
    
    public static boolean shouldAutoMarkAsRead(Context context)
    {
        return getBoolean(context, AUTO_MARK_AS_READ, false);
    }
    
    public static void setAutoMarkAsRead(Context context, boolean val)
    {
        setBoolean(context, AUTO_MARK_AS_READ, val);
    }    
    
    public static boolean isDebugModeEnabled(Context context)
    {
        return getBoolean(context, DEBUG_MODE, false);
    }
    
    public static void enableDebugMode(Context context, boolean val)
    {
        setBoolean(context, DEBUG_MODE, val);
    }    
    
    public static boolean shouldUpdateOnlyIfWifiOn(Context context)
    {
        return getBoolean(context, UPDATE_IF_WIFI, false);
    }
    
    public static void setUpdateOnlyIfWifiOn(Context context, boolean val)
    {
        setBoolean(context, UPDATE_IF_WIFI, val);
    }

    private static void preferenceUnavailable(String methodName)
    {
        Log.w(Constants.PACKAGE, "[" + methodName + "] Cannot access preference store: passed context is null."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static boolean shouldFetchThumbnailsEagerly(Context context)
    {
        return getBoolean(context, EAGER_FETCH_THUMBNAILS, true);
    }

    public static void setFetchThumbnailsEagerly(Context context, boolean val)
    {
        setBoolean(context, EAGER_FETCH_THUMBNAILS, val);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue)
    {
        if ( context == null )
        {
            preferenceUnavailable(key);
            return defaultValue;
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(key, defaultValue);
    }
    
    public static void setBoolean(Context context, String key, boolean value)
    {
        if ( context == null )
        {
            preferenceUnavailable(key);
            return;
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    private static int getInteger(Context context, String key, String defaultValue)
    {
        if ( context == null )
        {
            preferenceUnavailable(key);
            return Integer.parseInt(defaultValue);
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String val = prefs.getString(key, defaultValue);
        
        if ( TextUtils.isEmpty(val) )
        {
            return Integer.parseInt(defaultValue);
        }
      
        try
        {
            return Integer.parseInt(val);
        }
        catch (NumberFormatException e)
        {
            return Integer.parseInt(defaultValue);
        }
    }
    
    private static void setInteger(Context context, String key, int value)
    {
        if ( context == null )
        {
            preferenceUnavailable(key);
            return;
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
        edit.putString(key, new Integer(value).toString());
        edit.commit();
    }

    public static boolean doesPreferShapeOverNinePatch(Context context)
    {
        return getBoolean(context, PREFERE_SHAPE_OVER_NINEPATCH, false);
    }
    public static void setPreferShapeOverNinePatch(Context context, boolean val)
    {
        setBoolean(context, PREFERE_SHAPE_OVER_NINEPATCH, val);
    }

    public static Date getLastUpdateDate(Context context, int appWidgetId)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long l = prefs.getLong(LAST_WIDGET_UPDATE_PREFIX + appWidgetId, -1);
        
        if ( l == -1 ) 
        {
            return null;
        }
        
        return new Date(l);
    }

    public static void setLastUpdateDate(Context context, int appWidgetId, Date value)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
        edit.putLong(LAST_WIDGET_UPDATE_PREFIX + appWidgetId, value.getTime());
        edit.commit();
    }

}
