package fr.gdi.android.news.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.gdi.android.news.R;

import android.content.Context;

@SuppressWarnings("serial") //$NON-NLS-1$
public class Configuration implements Serializable
{    
    private static final String DEFAULT_TITLE = "Latest news (%d)"; //$NON-NLS-1$
    
    private long id;
    
    private Theme theme;
    
    private Behaviour behaviour;
    
    private List<Feed> feeds = new ArrayList<Feed>();

    private int appWidgetId;
    
    private String widgetTitle;
    
    public Configuration(Context context)
    {
        this.widgetTitle = getDefaultTitle(context);
    }
    
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Theme getTheme()
    {
        return theme;
    }

    public void setTheme(Theme theme)
    {
        this.theme = theme;
    }

    public Behaviour getBehaviour()
    {
        return behaviour;
    }

    public void setBehaviour(Behaviour behaviour)
    {
        this.behaviour = behaviour;
    }

    public List<Feed> getFeeds()
    {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds)
    {
        this.feeds = feeds;
    }

    public int getAppWidgetId()
    {
        return appWidgetId;
    }
    
    public void setAppWidgetId(int appWidgetId)
    {
        this.appWidgetId = appWidgetId;
    }

    public String getWidgetTitle()
    {
        return widgetTitle;
    }
    
    public void setWidgetTitle(String widgetTitle)
    {
        this.widgetTitle = widgetTitle;
    }
    
    private String getDefaultTitle(Context context)
    {
        return context == null ? DEFAULT_TITLE : context.getText(R.string.defaults_widget_title).toString();
    }
}
