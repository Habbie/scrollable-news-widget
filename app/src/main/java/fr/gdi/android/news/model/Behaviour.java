package fr.gdi.android.news.model;

import java.io.IOException;
import java.io.Serializable;

import fr.gdi.android.news.R;

import android.content.Context;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonWriter;

@SuppressWarnings("serial") //$NON-NLS-1$
public class Behaviour implements Serializable, Comparable<Behaviour>
{
    private static final String DEFAULT_NAME = "New behaviour"; //$NON-NLS-1$
    
    public static final int DEFAULT_NEWS_NUMBER = 25;
    
    private long _id;

    private String name;
    
    private Mobilizer mobilizer = Mobilizer.NONE;
    
    private boolean useBuiltInBrowser = true;
    
    private int maxNumberOfNews = DEFAULT_NEWS_NUMBER;
    
    private boolean lookupImageInBody = false;
    
    private boolean forceFeedAsAuthor = false;
    
    private boolean hideReadStories = false;

    private boolean clearBeforeLoad = true;
    
    private boolean distributeEvenly = false;
    
    public Behaviour(Context context)
    {
        this.name = getDefaultName(context);
    }
    
    public Mobilizer getMobilizer()
    {
        return mobilizer;
    }    
    
    public void setMobilizer(Mobilizer mobilizer)
    {
        this.mobilizer = mobilizer;
    }
    
    
    
    public boolean isUseBuiltInBrowser()
    {
        return useBuiltInBrowser;
    }
    public void setUseBuiltInBrowser(boolean useBuiltInBrowser)
    {
        this.useBuiltInBrowser = useBuiltInBrowser;
    }
    
    public int getMaxNumberOfNews()
    {
        return maxNumberOfNews;
    }
    public void setMaxNumberOfNews(int maxNumberOfNews)
    {
        this.maxNumberOfNews = maxNumberOfNews;
    }
    
    public boolean isLookupImageInBody()
    {
        return lookupImageInBody;
    }
    public void setLookupImageInBody(boolean lookupImageInBody)
    {
        this.lookupImageInBody = lookupImageInBody;
    }
    
    public long getId()
    {
        return _id;
    }
    public void setId(long _id)
    {
        this._id = _id;
    }
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    
    public boolean isForceFeedAsAuthor()
    {
        return forceFeedAsAuthor;
    }
    public void setForceFeedAsAuthor(boolean forceFeedAsAuthor)
    {
        this.forceFeedAsAuthor = forceFeedAsAuthor;
    }

    public void setHideReadStories(boolean hideReadStories)
    {
        this.hideReadStories = hideReadStories;
    }
    
    public boolean isHideReadStories()
    {
        return hideReadStories;
    }
    
    @Override
    public int compareTo(Behaviour another)
    {
        if ( another == null ) 
        {
            return -1;
        }
        
        if ( this.getName() == null ) 
        {
            return 1;
        }
        
        return this.getName().compareToIgnoreCase(another.getName());
    }
    
    @Override
    public boolean equals(Object o)
    {
        return (o instanceof Behaviour) && ((Behaviour) o).getId() == this.getId();
    }
    
    public void toJson(JsonWriter writer) throws IOException
    {
        writer.beginObject()
            .name("name").value(name) //$NON-NLS-1$
            .name("mobilizer").value(mobilizer.getName()) //$NON-NLS-1$
            .name("useBuiltInBrowser").value(useBuiltInBrowser) //$NON-NLS-1$
            .name("maxNumberOfNews").value(maxNumberOfNews) //$NON-NLS-1$
            .name("lookupImageInBody").value(lookupImageInBody) //$NON-NLS-1$
            .name("forceFeedAsAuthor").value(forceFeedAsAuthor) //$NON-NLS-1$
            .name("hideReadStories").value(hideReadStories) //$NON-NLS-1$
            .name("distributeEvenly").value(distributeEvenly) //$NON-NLS-1$
            .name("clearBeforeLoad").value(clearBeforeLoad) //$NON-NLS-1$
        .endObject();   
    }
    
    public void fromJson(JsonReader reader) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext()) 
        {
            String name = reader.nextName();
            
            if (TextUtils.equals(name, "name")) //$NON-NLS-1$
            {
                this.setName(reader.nextString());
            }
            else if (TextUtils.equals(name, "mobilizer")) //$NON-NLS-1$
            {
                this.setMobilizer(Mobilizer.fromName(reader.nextString()));
            }
            else if (TextUtils.equals(name, "useBuiltInBrowser")) //$NON-NLS-1$
            {
                this.setUseBuiltInBrowser(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "maxNumberOfNews")) //$NON-NLS-1$
            {
                this.setMaxNumberOfNews(reader.nextInt());
            }
            else if (TextUtils.equals(name, "lookupImageInBody")) //$NON-NLS-1$
            {
                this.setLookupImageInBody(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "forceFeedAsAuthor")) //$NON-NLS-1$
            {
                this.setForceFeedAsAuthor(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "hideReadStories")) //$NON-NLS-1$
            {
                this.setHideReadStories(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "clearBeforeLoad")) //$NON-NLS-1$
            {
                this.setClearBeforeLoad(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "distributeEvenly")) //$NON-NLS-1$
            {
                this.setDistributeEvenly(reader.nextBoolean());
            }
            else
            {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private String getDefaultName(Context context)
    {
        return context == null ? DEFAULT_NAME : context.getText(R.string.defaults_behaviour_name).toString();
    }
    
    public boolean isClearBeforeLoad()
    {
        return clearBeforeLoad;
    }
    
    public void setClearBeforeLoad(boolean clearBeforeLoad)
    {
        this.clearBeforeLoad = clearBeforeLoad;
    }
    
    public void setDistributeEvenly(boolean shouldDistributeEvenly)
    {
        this.distributeEvenly = shouldDistributeEvenly;
    }
    
    public boolean shouldDistributeEvenly()
    {
        return distributeEvenly;
    }
}
