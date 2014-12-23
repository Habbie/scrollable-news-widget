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

import fr.gdi.android.news.Constants;

@SuppressWarnings("serial") //$NON-NLS-1$
public class Item  implements Serializable, Comparable<Item>
{
    
    private String originalSource;
    private long mId = -1;
    private URL mLink;
    private String mGuid;
    private String mTitle;
    private String mDescription;
    private String mContent;
    private URL mImage = null;
    private Date mPubdate;
    private Date updateDate;
    private boolean mFavorite = false;
    private boolean mRead = false;
    private List<Enclosure> mEnclosures;
    private String author;
    private Feed source;
    private String originalAuthor;
    
    public Item()
    {
        mEnclosures = new ArrayList<Enclosure>();
        mPubdate = new Date();
    }
    
    public Item(long id, URL link, String guid, String title, String description, String content, URL image, Date pubdate, boolean favorite, boolean read,
            List<Enclosure> enclosures)
    {
        super();
        this.mId = id;
        this.mLink = link;
        this.mGuid = guid;
        this.mTitle = title;
        this.mDescription = description;
        this.mContent = content;
        this.mImage = image;
        this.mPubdate = pubdate;
        this.mFavorite = favorite;
        this.mRead = read;
        this.mEnclosures = enclosures;
    }
    
    public void setId(long id)
    {
        this.mId = id;
    }
    
    public long getId()
    {
        return mId;
    }
    
    public void setLink(URL link)
    {
        this.mLink = link;
    }
    
    public URL getLink()
    {
        return this.mLink;
    }
    
    public String getLinkURL()
    {
        return mLink == null ? Constants.MALFORMED_ITEM_LINK_URL : mLink.toString();
    }
    
    public void setGuid(String guid)
    {
        this.mGuid = guid;
    }
    
    public String getGuid()
    {
        return mGuid;
    }
    
    public void setTitle(String title)
    {
        this.mTitle = title;
    }
    
    public String getTitle()
    {
        return this.mTitle;
    }
    
    public void setDescription(String description)
    {
        this.mDescription = description;
    }
    
    public String getDescription()
    {
        return mDescription;
    }
    
    public void setContent(String content)
    {
        this.mContent = content;
    }
    
    public String getContent()
    {
        return mContent;
    }
    
    public void setImage(URL image)
    {
        this.mImage = image;
    }
    
    public URL getImage()
    {
        return this.mImage;
    }
    
    public void setPubdate(Date pubdate)
    {
        this.mPubdate = pubdate;
    }
    
    public Date getPubdate()
    {
        return this.mPubdate;
    }
    
    public void favorite()
    {
        this.mFavorite = true;
    }
    
    public void unfavorite()
    {
        this.mFavorite = false;
    }
    
    public void setFavorite(boolean state)
    {
        this.mFavorite = state;
    }
    
    public boolean isFavorite()
    {
        return this.mFavorite;
    }
    
    public void read()
    {
        this.mRead = true;
    }
    
    public void unread()
    {
        this.mRead = false;
    }
    
    public void setRead(boolean b)
    {
        this.mRead = b;
    }
    
    public boolean isRead()
    {
        return this.mRead;
    }
    
    public void addEnclosure(Enclosure enclosure)
    {
        this.mEnclosures.add(enclosure);
    }
    
    public void setEnclosures(List<Enclosure> enclosures)
    {
        this.mEnclosures = enclosures;
    }
    
    public List<Enclosure> getEnclosures()
    {
        return this.mEnclosures;
    }
    
    @Override
    public String toString()
    {
        return mTitle;
    }
    
    public String toDebugString()
    {
        String s = "{ " + //$NON-NLS-1$
        		"id: " + this.mId + //$NON-NLS-1$
        		", link: " + this.mLink.toString() + //$NON-NLS-1$
        		", guid: " + this.mGuid + //$NON-NLS-1$
        		", title: " + this.mTitle + //$NON-NLS-1$
        		", description: " + this.mDescription + //$NON-NLS-1$
        		", content: " + this.mContent + //$NON-NLS-1$
        		", image: " + this.mImage.toString() + //$NON-NLS-1$
        		", pubdate: " + this.mPubdate.toString() + //$NON-NLS-1$
        		", favorite: " + this.mFavorite + //$NON-NLS-1$
        		", read: " + this.mRead + //$NON-NLS-1$
    		    " items: [";//$NON-NLS-1$
        Iterator<Enclosure> iterator = this.mEnclosures.iterator();
        int u = 0;
        while (iterator.hasNext())
        {
            if (u > 0) s += ", ";//$NON-NLS-1$
            s = s + iterator.next().toString();
            u++;
        }
        s += "]}"; //$NON-NLS-1$
        return s;
    }
    
    public String getAuthor()
    {
        return author;
    }
    
    public void setAuthor(String author)
    {
        this.author = author;
    }
    
    public void setSource(Feed source)
    {
        this.source = source;
    }
    
    public Feed getSource()
    {
        return source;
    }
    
    public void setUpdateDate(Date updateDate)
    {
        this.updateDate = updateDate;
    }
    
    public Date getUpdateDate()
    {
        return updateDate;
    }
  
    public void setOriginalSource(String sourceTitle)
    {
        this.originalSource = sourceTitle;
    }
    
    public String getOriginalSource()
    {
        return originalSource;
    }
    
    public void setOriginalAuthor(String originalAuthor)
    {
        this.originalAuthor = originalAuthor;
    }
    
    public String getOriginalAuthor()
    {
        return originalAuthor;
    }
    
    @Override
    public int compareTo(Item another)
    {
        if ( another == null ) return -1;
        
        Date d1 = this.getDate(), d2 = another.getDate();
        
        if ( d1 == null ) return d2 == null ? 0 : 1;
        
        if ( d2 == null ) return -1;
        
        return d1.compareTo(d2);
    }
    
    public Date getDate() 
    {
        return this.getUpdateDate() != null ? this.getUpdateDate() : this.getPubdate();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if ( !(o instanceof Item) ) return false;
        return this.mId == ((Item) o).getId();
    }

    public boolean isMalformedLink()
    {
        return TextUtils.equals(getLinkURL(), Constants.MALFORMED_ITEM_LINK_URL);
    }
}
