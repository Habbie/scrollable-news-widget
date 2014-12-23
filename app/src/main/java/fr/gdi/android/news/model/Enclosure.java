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

@SuppressWarnings("serial") //$NON-NLS-1$
public class Enclosure implements Serializable
{
    
    private long mId = -1;
    private String mMime;
    private URL mURL;
    
    public Enclosure()
    {
    }
    
    public Enclosure(long id, String mime, URL url)
    {
        this.mId = id;
        this.mMime = mime;
        this.mURL = url;
    }
    
    public void setId(long id)
    {
        this.mId = id;
    }
    
    public long getId()
    {
        return mId;
    }
    
    public void setMime(String mime)
    {
        this.mMime = mime;
    }
    
    public String getMime()
    {
        return this.mMime;
    }
    
    public void setURL(URL url)
    {
        this.mURL = url;
    }
    
    public URL getURL()
    {
        return this.mURL;
    }
    
    public String toString()
    {
        return "{" + //$NON-NLS-1$
		     "id: " + this.mId +  //$NON-NLS-1$
		     ", mime: " + this.mMime +  //$NON-NLS-1$
		     ", url: " + this.mURL.toString() + //$NON-NLS-1$
		     "}"; //$NON-NLS-1$
    }
}
