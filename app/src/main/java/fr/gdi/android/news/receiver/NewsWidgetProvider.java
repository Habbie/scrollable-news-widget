package fr.gdi.android.news.receiver;

import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.activity.ConfigurationActivity;
import fr.gdi.android.news.activity.ItemDetailsActivity;
import fr.gdi.android.news.data.dao.ConfigurationDao;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.fragment.item.ItemDetailsFragment;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.model.Configuration;
import fr.gdi.android.news.model.Mobilizer;
import fr.gdi.android.news.model.Theme;
import fr.gdi.android.news.preference.utils.PreferenceUtils;
import fr.gdi.android.news.service.ServiceRegistration;
import fr.gdi.android.news.utils.StringUtils;

public class NewsWidgetProvider extends AppWidgetProvider
{
    public static final String ITEM_CLICK = Constants.PACKAGE + ".widget.ITEM_CLICK"; //$NON-NLS-1$
    public static final String SET_LOADING = Constants.PACKAGE + ".widget.SET_LOADING";; //$NON-NLS-1$
    
    public static final String FETCHING_THUMBNAILS_KEY = "fetching_thumbnails"; //$NON-NLS-1$
    public static final String LOADING_KEY = "loading"; //$NON-NLS-1$
    public static final String UPDATE_DATE_KEY = "omit_date"; //$NON-NLS-1$

    
    @Override
    public void onReceive(Context context, Intent intent) 
    {
        if (intent.getAction().equals(ITEM_CLICK)) 
        {
            forwardClickIntent(context, intent);
        }
        else if (intent.getAction().equals(WidgetUpdater.ACTION_UPDATE))
        {
            int[] appWidgetIds = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            boolean updateDate = intent.getExtras().getBoolean(UPDATE_DATE_KEY, true);
            doUpdate(context, appWidgetManager, appWidgetIds, false, false, updateDate);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, getListViewId());
        }
        else if (intent.getAction().equals(SET_LOADING))
        {
            int[] appWidgetIds = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            boolean loading = intent.getExtras().getBoolean(LOADING_KEY, false);
            boolean fetchingThumbnails = intent.getExtras().getBoolean(FETCHING_THUMBNAILS_KEY, false);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            doUpdate(context, appWidgetManager, appWidgetIds, loading, fetchingThumbnails, false);
        }
        else 
        {
            Log.d(Constants.PACKAGE, "Unrecognized message received: " + intent.getAction()); //$NON-NLS-1$
            super.onReceive(context, intent);
        }
    }

    private void forwardClickIntent(Context context, Intent intent)
    {
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        
        if ( appWidgetId == -1 ) return; 

        ConfigurationDao configurationDao = DaoUtils.getConfigurationDao(context);
        Configuration configuration = configurationDao.getConfigurationByWidgetId(appWidgetId);
        
        if ( configuration == null ) 
        {
            //Configuration should not be null, yet a NPE was reported, so we   
            //  just take extra care to not let uncaught exceptions bubble up.
            //Two options here: either we warn the user or we do not...
            //Not sure what's the best way to deal with that. Silently swallow  
            //  the error for now.
            
            //ToastUtils.showError(context, R.string.feed_details_invalid_state);
            
            return;
        }
        
        Behaviour behaviour = configuration.getBehaviour();
        boolean useBuiltInBrowser = behaviour.isUseBuiltInBrowser(); 
        Mobilizer mobilizer = behaviour.getMobilizer();
        
        if ( useBuiltInBrowser ) 
        {
            intent.setClass(context, ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsFragment.MOBILIZER_KEY, mobilizer.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | 
                    Intent.FLAG_ACTIVITY_NO_HISTORY);
            context.startActivity(intent);
        }
        else 
        {
            String url = intent.getStringExtra(ItemDetailsFragment.INITIAL_URL_KEY);
            url = mobilizer.format(url);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(url));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        }
    }
    
    //http://developer.android.com/guide/topics/appwidgets/index.html#collections
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        doUpdate(context, appWidgetManager, appWidgetIds, false, false, false);
    }

    private void doUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, boolean loading, boolean fetchingThumbnails, boolean updateDate)
    {
        ConfigurationDao configurationDao = DaoUtils.getConfigurationDao(context);
        
        Configuration configuration = null;
        
        for (int i = 0; i < appWidgetIds.length; i++)
        {
            int appWidgetId = appWidgetIds[i];
            
            configuration = configurationDao.getConfigurationByWidgetId(appWidgetId);
            Theme theme = getTheme(configuration, context);
            
            Intent svcIntent = new Intent(context, NewsWidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.setData(Constants.CONTENT_URI_NEWS.buildUpon().appendEncodedPath(Integer.toString(appWidgetId)).build());

            //configure widget collection
            RemoteViews widget = new RemoteViews(context.getPackageName(), getWidgetLayout(theme));
            widget.setRemoteAdapter(appWidgetId, getListViewId(), svcIntent);
            widget.setEmptyView(getListViewId(), R.id.empty_view);
            widget.setViewVisibility(R.id.loading_icon, loading || fetchingThumbnails ? View.VISIBLE : View.GONE);
            widget.setImageViewResource(R.id.loading_icon, fetchingThumbnails ? R.drawable.downloading : R.drawable.loading);
            
            //configure primary views
            setupBackground(context, theme, widget);
            setupTitle(context, configuration, widget, loading || fetchingThumbnails, updateDate, appWidgetId);
            
            //read story pending intent template
            Intent onItemClickIntent = new Intent(context, NewsWidgetProvider.class);
            onItemClickIntent.setAction(NewsWidgetProvider.ITEM_CLICK);
            onItemClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, onItemClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setPendingIntentTemplate(getListViewId(), clickPendingIntent);
            
            //feed reload pending intent; force network access
            Intent intent = new Intent(context, WidgetUpdater.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra(WidgetUpdater.FORCE_REFRESH, true);
            intent.setAction(WidgetUpdater.ACTION_UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.active_title_reload, pendingIntent);
            
            //widget refresh pending intent; no nework access: only reads db 
            intent = new Intent(context, NewsWidgetProvider.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });
            intent.setAction(WidgetUpdater.ACTION_UPDATE);
            intent.putExtra(NewsWidgetProvider.UPDATE_DATE_KEY, false);
            pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.active_title_refresh, pendingIntent);
            
            //widget config pending intent; launches widget config activity
            intent = new Intent(context, ConfigurationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra(ConfigurationActivity.UPDATE_EXISTING, true);
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.active_title_config, pendingIntent);
            
            handleDebugMode(context, widget);
            
            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }
    }

    protected int getWidgetLayout(Theme theme) 
    {
        final int numColumns = theme.getNumColumns();
        
        switch (numColumns)
        {
            case 1: return R.layout.widget;
            case 2: return R.layout.widget_2;
            case 3: return R.layout.widget_3;        
        }
        
        return R.layout.widget;
    }
    
    protected int getListViewId()
    {
        return R.id.news_list;
    }

    
    private void handleDebugMode(Context context, RemoteViews widget)
    {
        if ( PreferenceUtils.isDebugModeEnabled(context) ) 
        {
            widget.setInt(R.id.active_title_config, "setBackgroundColor", Color.RED); //$NON-NLS-1$
            widget.setTextViewText(R.id.active_title_config, "Config"); //$NON-NLS-1$
            
            widget.setInt(R.id.active_title_refresh, "setBackgroundColor", Color.GREEN); //$NON-NLS-1$
            widget.setTextViewText(R.id.active_title_refresh, "Refresh"); //$NON-NLS-1$
            
            widget.setInt(R.id.active_title_reload, "setBackgroundColor", Color.BLUE); //$NON-NLS-1$
            widget.setTextViewText(R.id.active_title_reload, "Reload"); //$NON-NLS-1$
        }
        else 
        {
            widget.setInt(R.id.active_title_config, "setBackgroundColor", 0); //$NON-NLS-1$
            widget.setTextViewText(R.id.active_title_config, ""); //$NON-NLS-1$
            
            widget.setInt(R.id.active_title_refresh, "setBackgroundColor", 0); //$NON-NLS-1$
            widget.setTextViewText(R.id.active_title_refresh, ""); //$NON-NLS-1$
            
            widget.setInt(R.id.active_title_reload, "setBackgroundColor", 0); //$NON-NLS-1$
            widget.setTextViewText(R.id.active_title_reload, ""); //$NON-NLS-1$
        }
    }

    private Theme getTheme(Configuration configuration, Context context)
    {
        Theme theme;
        if ( configuration == null ) 
        {
            theme = new Theme(context); //might happen when configuring 
        }
        else 
        {
            theme = configuration.getTheme();
        }
        return theme;
    }

    //todo: refactor-me (too many parameters)
    private void setupTitle(Context context, 
                Configuration configuration, 
                RemoteViews widget, 
                boolean forceVisibility, 
                boolean updateDate, 
                int appWidgetId)
    {
        Theme theme = getTheme(configuration, context);
        
        int titleColor = theme.getWidgetTitleColor();
        widget.setTextColor(R.id.widget_title, titleColor);

        if ( forceVisibility || theme.isShowWidgetTitle() )
        {
            widget.setViewVisibility(R.id.header, View.VISIBLE);
            widget.setViewVisibility(R.id.title_separator, View.VISIBLE);
            
            String title = ""; //$NON-NLS-1$
            
            if ( theme.isShowWidgetTitle() ) 
            {
                title = configuration != null ? configuration.getWidgetTitle() : (context.getText(R.string.defaults_widget_title).toString());
                title = getWidgetTitle(context, title, theme, updateDate, appWidgetId);
            }
            else 
            {
                //title = context.getText(R.string.loading).toString();
                title = ""; //$NON-NLS-1$
            }

            widget.setViewVisibility(R.id.widget_title, theme.isShowWidgetTitle() ? View.VISIBLE : View.INVISIBLE);
            widget.setViewVisibility(R.id.edit_button, theme.isShowWidgetTitle() ? View.VISIBLE : View.INVISIBLE);
            
            widget.setTextViewText(R.id.widget_title, title);
        }
        else 
        {
            widget.setViewVisibility(R.id.header, View.GONE);
            widget.setViewVisibility(R.id.title_separator, View.GONE);
        }
    }

    private String getWidgetTitle(Context context, String title, Theme theme, boolean updateDate, int appWidgetId)
    {
        String dateFormat = theme.getDateFormat();
        
        if ( !TextUtils.isEmpty(title) ) 
        {
            Date date = new Date();
            if ( !updateDate ) 
            {
                //do not update the title date: get the last stored one
                //if not found the current date is used
                date = PreferenceUtils.getLastUpdateDate(context, appWidgetId);
                if ( date == null ) date = new Date();
            }
            else 
            {
                //store last update date 
                PreferenceUtils.setLastUpdateDate(context, appWidgetId, date);
            }
            title = title.replaceAll("%d", StringUtils.formatDate(date, dateFormat)); //$NON-NLS-1$
        }
        else 
        {
            title = ""; //force empty string //$NON-NLS-1$
        }
        
        return title;
    }
    
    private void setupBackground(Context context, Theme theme, RemoteViews widget)
    {
        int res = theme.isRoundedCorners() ? getRoundedCornerResId(context) : R.drawable.square_white_png;
        widget.setImageViewResource(R.id.background, res);
       
        widget.setInt(R.id.background, "setColorFilter", theme.getBackgroundColor()); //$NON-NLS-1$
        widget.setInt(R.id.background, "setAlpha", theme.getBackgroundOpacity()); //$NON-NLS-1$
    }
    
    private int getRoundedCornerResId(Context context)
    {
        return PreferenceUtils.doesPreferShapeOverNinePatch(context) ? 
                    R.drawable.rounded_corners_white : R.drawable.rounded_white_png;
    }

    @Override
    public void onEnabled(Context context)
    {
        ServiceRegistration.register(context);
    }
    
    @Override
    public void onDisabled(Context context)
    {
        ServiceRegistration.unregister(context);
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
        
        ConfigurationDao dao = DaoUtils.getConfigurationDao(context);
        for (int appWidgetId : appWidgetIds)
        {
            dao.removeConfiguration(appWidgetId);
        }
    }
}
