/*
 * Copyright (C) 2010-2011 Mathieu Favez - http://mfavez.com
 *
 *
 * This file is part of FeedGoal.
 * 
 * FeedGoal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeedGoal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FeedGoal.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.gdi.android.news.model;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.text.TextUtils;

@SuppressWarnings("serial") //$NON-NLS-1$
public class Feed implements Serializable, Comparable<Feed>
{
    
    public static final String TYPE_RDF = "rdf"; //$NON-NLS-1$
    public static final String TYPE_RSS = "rss"; //$NON-NLS-1$
    public static final String TYPE_ATOM = "atom"; //$NON-NLS-1$
    
    private long mId = -1;
    private URL mURL;
    private URL mHomePage;
    private String mTitle;
    private String mType;
    private Date mRefresh = null;
    private boolean mEnabled = true;
    private List<Item> mItems;
    private int mOrder;
    private String imageUrl;
    
    private Integer color;
    
    public Feed()
    {
        mItems = new ArrayList<Item>();
    }
    
    public Feed(long id, URL url, URL homePage, String title, String type, Date refresh, boolean enabled, List<Item> items, int order)
    {
        super();
        this.mId = id;
        this.mURL = url;
        this.mHomePage = homePage;
        this.mTitle = title;
        this.mType = type;
        this.mRefresh = refresh;
        this.mEnabled = enabled;
        this.mItems = items;
        this.mOrder = order;
    }
    
    public void setId(long id)
    {
        this.mId = id;
    }
    
    public long getId()
    {
        return mId;
    }
    
    public void setURL(URL url)
    {
        this.mURL = url;
    }
    
    public URL getURL()
    {
        return this.mURL;
    }
    
    public void setHomePage(URL homepage)
    {
        this.mHomePage = homepage;
    }
    
    public URL getHomePage()
    {
        return this.mHomePage;
    }
    
    public void setTitle(String title)
    {
        this.mTitle = title;
    }
    
    public String getTitle()
    {
        return this.mTitle;
    }
    
    public void setType(String type)
    {
        this.mType = type;
    }
    
    public String getType()
    {
        return mType;
    }
    
    public void setRefresh(Date refresh)
    {
        mRefresh = refresh;
    }
    
    public Date getRefresh()
    {
        return mRefresh;
    }
    
    public void enable()
    {
        this.mEnabled = true;
    }
    
    public void disable()
    {
        this.mEnabled = false;
    }
    
    public void setEnabled(boolean b)
    {
        this.mEnabled = b;
    }
    
    public boolean isEnabled()
    {
        return this.mEnabled;
    }
    
    public void addItem(Item item)
    {
        this.mItems.add(item);
    }
    
    public void setItems(List<Item> items)
    {
        this.mItems = items;
    }
    
    public List<Item> getItems()
    {
        return this.mItems;
    }
    
    public int getOrderNo() 
    {
        return mOrder; 
    }
    
    public void setOrderNo(int order) 
    {
        this.mOrder = order;
    }
    
    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }
    
    public String getImageUrl()
    {
        return imageUrl;
    }
    
    public String toString()
    {
        String s = "{ " + "id:" + this.mId +  //$NON-NLS-1$ //$NON-NLS-2$
            ", url:" + this.mURL.toString() +  //$NON-NLS-1$
            ", homepage: " + this.mHomePage.toString() +  //$NON-NLS-1$
            ", title: " + this.mTitle + //$NON-NLS-1$
            ", type: " + this.mType +  //$NON-NLS-1$
            ", update: " + this.mRefresh.toString() + //$NON-NLS-1$
            ", enabled: " + this.mEnabled + //$NON-NLS-1$
            ", items: ["; //$NON-NLS-1$
        Iterator<Item> iterator = this.mItems.iterator();
        int u = 0;
        while (iterator.hasNext())
        {
            if (u > 0) s += ", "; //$NON-NLS-1$
            s += iterator.next().toString();
            u++;
        }
        s = s + "]}"; //$NON-NLS-1$
        return s;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof Feed) && ((Feed) o).getId() == getId();
    }
    
    @Override
    public int compareTo(Feed another)
    {
        if ( another == null || TextUtils.isEmpty(another.getTitle()) )
        {
            return -1;
        }
        
        if ( TextUtils.isEmpty(this.getTitle()) ) 
        {
            return 1;
        }
        
        return this.getTitle().compareToIgnoreCase(another.getTitle());
    }
    
    public void setColor(Integer color)
    {
        this.color = color;
    }
    
    public Integer getColor()
    {
        return color;
    }
}
