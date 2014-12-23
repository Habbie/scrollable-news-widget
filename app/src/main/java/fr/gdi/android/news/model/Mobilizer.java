package fr.gdi.android.news.model;

import fr.gdi.android.news.R;
import android.content.Context;
import android.text.TextUtils;

public enum Mobilizer
{
    NONE("None", "%1s"), //$NON-NLS-1$ //$NON-NLS-2$
    INSTAPAPER("Instapaper", "http://www.instapaper.com/m?u=%1s"), //$NON-NLS-1$ //$NON-NLS-2$
    READITLATER("ReadItLater", "http://text.readitlaterlist.com/v2/text?url=%1s"), //$NON-NLS-1$ //$NON-NLS-2$
    GOOGLE("Google", "http://www.google.com/gwt/x?u=%1s"); //$NON-NLS-1$ //$NON-NLS-2$
    
    private String name, urlTemplate;
    
    private Mobilizer(String name, String url)
    {
        this.name = name;
        this.urlTemplate = url;
    }
    
    public String format(String url)
    {
        if ( TextUtils.isEmpty(url) || url.startsWith("file://") ) return url; //$NON-NLS-1$
        return String.format(this.urlTemplate, url); 
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getUrlTemplate()
    {
        return urlTemplate;
    }
    
    public static Mobilizer fromName(String name) 
    {
        if ( TextUtils.equals(name, GOOGLE.getName()) ) return GOOGLE;
        if ( TextUtils.equals(name, INSTAPAPER.getName()) ) return INSTAPAPER;
        if ( TextUtils.equals(name, READITLATER.getName()) ) return READITLATER;
        
        return NONE;
    }
    
    public String getTranslatedName(Context context)
    {
        switch ( this ) 
        {
            case GOOGLE: return context.getText(R.string.Google).toString();
            case INSTAPAPER: return context.getText(R.string.InstaPaper).toString();
            case READITLATER: return context.getText(R.string.ReadItLater).toString();
            
        }
        
        return context.getText(R.string.None).toString();
    }
    
}
