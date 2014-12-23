/*
 * Copyright (C) 2010-2011 Mathieu Favez - http://mfavez.com
 *
 *
 * This file is part of FeedGoal - http://feedgoal.org
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

package fr.gdi.android.news.utils.feed.parse;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import fr.gdi.android.news.Constants;
import fr.gdi.android.news.model.Enclosure;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.utils.EncodingUtils;
import fr.gdi.android.news.utils.IOUtils;

public class FeedHandler extends DefaultHandler
{
    
    private Feed mFeed;
    private Item mItem;
    private Enclosure mEnclosure;
    
    
    public static List<String> IMAGE_MIME_TYPES = Arrays.asList(new String[] { "image/jpg", "image/jpeg", "image/gif", "image/png", "image/bmp" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    
    // Allowed Namespaces -- see also: yahoo thingie
    private static final Set<String> NAMESPACES = new HashSet<String>(Arrays.asList(new String[] { 
            "",  //$NON-NLS-1$
            "http://www.w3.org/2005/Atom",  //$NON-NLS-1$
            "http://purl.org/rss/1.0/modules/content/", //$NON-NLS-1$
            "http://purl.org/rss/1.0/",  //$NON-NLS-1$
            "http://purl.org/dc/elements/1.1/", //$NON-NLS-1$
            "http://search.yahoo.com/mrss/", //$NON-NLS-1$
    }));
        
    private boolean isType = false;
    private boolean isAuthor = false;
    private boolean isFeed = false;
    private boolean isItem = false;
    private boolean isTitle = false;
    private boolean isLink = false;
    private boolean isPubdate = false;
    private boolean isGuid = false;
    private boolean isDescription = false;
    private boolean isContent = false;
    private boolean isImage = false;
    
    /* used to escape the <source> element in Atom format */
    private boolean isSource = false; 
    
    private boolean isEnclosure = false;
    
    private String mVersion;
    
    /* href attribute from link element in Atom  format and enclosures for Atom and RSS formats */
    private String mHrefAttribute; 
    
    /* Enclosure MIME type attribute from link element for RSS and Atom formats */
    private String mMimeAttribute; 
    
    private int maxItems = Integer.MAX_VALUE;
    private int mNbrItems = 0;
    
    private StringBuffer mSb;
    
    public FeedHandler(Context ctx)
    {
        
    }
    
    public void startDocument() throws SAXException
    {
        mFeed = new Feed();
    }
    
    public void endDocument() throws SAXException
    {
        Date now = new Date();
        mFeed.setRefresh(now);
    }
    
    public boolean hasType(String value, String... types)
    {
        for (String type : types)
        {
            if ( value.equalsIgnoreCase(type) ) return true;
        }
        return false;
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        Log.d(Constants.PACKAGE, "startElement / "+uri+" / "+localName+" / "+qName);
        // Only consider elements from allowed third-party namespaces
        if (NAMESPACES.contains(uri))
        {
            mSb = new StringBuffer();
            String value = localName.trim();
            
            if (hasType(value, "rss", "rdf")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                isType = true;
                if (hasType(value, "rss") && attributes != null) //$NON-NLS-1$
                {
                    if (attributes.getValue("version") != null) //$NON-NLS-1$
                    {
                        mVersion = attributes.getValue("version"); //$NON-NLS-1$
                    }
                }
            }
            else if (hasType(value,"feed")) //$NON-NLS-1$
            {
                isType = true;
                isFeed = true;
            }
            else if (hasType(value, "channel")) //$NON-NLS-1$
            {
                isFeed = true;
            }
            else if (hasType(value, "item", "entry")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                mItem = new Item();
                mItem.setSource(mFeed);
                isItem = true;
                mNbrItems++;
            }
            else if (hasType(value, "title"))  //$NON-NLS-1$
            {
                isTitle = true;
            }
            else if (hasType(value, "author")) //$NON-NLS-1$
            {
                isAuthor = true;
            }
            else if (hasType(value, "link")) //$NON-NLS-1$
            {
                isLink = true;
                // Get attributes from link element for Atom format
                if (attributes != null)
                {
                    // Enclosure for Atom format
                    if (attributes.getValue("rel") != null) //$NON-NLS-1$
                    {
                        mMimeAttribute = attributes.getValue("type"); //$NON-NLS-1$
                        if (hasType(attributes.getValue("rel"), "enclosure")) //$NON-NLS-1$ //$NON-NLS-2$
                        {
                            mEnclosure = new Enclosure();
                            isEnclosure = true;
                        }

                        Log.d(Constants.PACKAGE, "rel="+attributes.getValue("rel"));
                        if (hasType(attributes.getValue("rel"), "related")) {
                            Log.d(Constants.PACKAGE, "rel=related, setting isLink to False");
                            isLink = false;
                        }
                    }
                    mHrefAttribute = attributes.getValue("href"); //$NON-NLS-1$
                }
            }
            else if (hasType(value, "pubDate", "published", "date"))  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            {
                isPubdate = true;
            }
            else if (hasType(value, "guid", "id")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                isGuid = true;
            }
            else if (hasType(value, "description", "summary")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                isDescription =  !TextUtils.equals(qName, "media:description"); //$NON-NLS-1$ //surely not the best way to handle this...
            }
            else if (hasType(value, "encoded", "content")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                if ( TextUtils.equals(qName, "media:content") ) //$NON-NLS-1$ //not the best way to handle this neither...
                {
                    if (attributes.getValue("type") != null && attributes.getValue("url") != null) //$NON-NLS-1$ 
                    {
                        String type = attributes.getValue("type");
                        String url = attributes.getValue("url");
                        if ( IMAGE_MIME_TYPES.contains(type) && mItem.getImage() == null) 
                        {
                            try 
                            {
                                mItem.setImage(new URL(url));
                            }
                            catch ( Exception e ) 
                            {
                                //swallow
                            }   
                        }
                    }
                }
                else 
                {                    
                    isContent = true;
                }
            }
            else if (hasType(value, "source")) //$NON-NLS-1$
            {
                isSource = true;
            }
            else if (hasType(value, "image", "thumbnail"))  //$NON-NLS-1$ //$NON-NLS-2$
            {
                isImage = true;
                if ( isItem )
                {
                    try 
                    {
                        String url = attributes.getValue("url"); //$NON-NLS-1$
                        mItem.setImage(new URL(url));
                    }
                    catch ( Exception e ) 
                    {
                        //swallow
                    }
                }
            }
            else if (hasType(value, "enclosure")) //$NON-NLS-1$
            {
                // Enclosure for RSS format
                if (attributes != null)
                {
                    mEnclosure = new Enclosure();
                    mMimeAttribute = attributes.getValue("type"); //$NON-NLS-1$
                    mHrefAttribute = attributes.getValue("url"); //$NON-NLS-1$
                    isEnclosure = true;
                }
            }
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        Log.d(Constants.PACKAGE, "endElement / "+uri+" / "+localName+" / "+qName+" / "+mSb.toString());

        // Only consider elements from allowed third-party namespaces
        if (NAMESPACES.contains(uri))
        {
            String value = localName.trim();
            
            if (hasType(value, "rss")) //$NON-NLS-1$
            {
                mFeed.setType(Feed.TYPE_RSS);
                isType = false;
            }
            else if (hasType(value, "feed")) //$NON-NLS-1$
            {
                mFeed.setType(Feed.TYPE_ATOM);
                isType = false;
                isFeed = false;
            }
            else if (hasType(value, "rdf")) //$NON-NLS-1$
            {
                mFeed.setType(Feed.TYPE_RDF);
                isType = false;
            }
            else if (hasType(value, "channel")) //$NON-NLS-1$
            {
                isFeed = false;
            }
            else if (hasType(value, "item", "entry")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                if (mNbrItems <= maxItems)
                {
                    if (mItem.getGuid() == null && mItem.getLink() != null) mItem.setGuid(mItem.getLink().toString());
                    mFeed.addItem(mItem);
                }
                isItem = false;
            }
            else if (hasType(value, "title")) //$NON-NLS-1$
            {
                if (!isSource)
                {
                    if (isItem) 
                    {
                        mItem.setTitle(Html.fromHtml(mSb.toString().trim()).toString());
                    }
                    else if (isFeed) 
                    {
                        mFeed.setTitle(Html.fromHtml(mSb.toString().trim()).toString());
                    }
                }
                else
                {
                    if (isItem)
                    {
                        mItem.setOriginalSource(Html.fromHtml(mSb.toString().trim()).toString());
                    }
                }
                isTitle = false;
            }
            else if (hasType(value, "link") && !isSource) //$NON-NLS-1$
            {
                if (isItem)
                {
                    try
                    {
                        if (isEnclosure)
                        {
                            // Enclosure for Atom format
                            mEnclosure.setMime(mMimeAttribute);
                            mEnclosure.setURL(new URL(mHrefAttribute));
                            mItem.addEnclosure(mEnclosure);
                            mMimeAttribute = null;
                            isEnclosure = false;
                        }
                        else if (mItem.getLink() == null || TextUtils.equals(mMimeAttribute, "text/html")) //$NON-NLS-1$
                        {
                            if (isLink) {
                                if (mHrefAttribute != null) {
                                    Log.d(Constants.PACKAGE, "replacing link [" + mItem.getLinkURL() + "] with link [" + mHrefAttribute + "]");
                                    mItem.setLink(new URL(mHrefAttribute));
                                } else {
                                    Log.d(Constants.PACKAGE, "replacing link [" + mItem.getLinkURL() + "] with link [" + mSb.toString().trim() + "]");

                                    mItem.setLink(new URL(mSb.toString().trim()));
                                }
                            }
                        }
                    }
                    catch (MalformedURLException mue)
                    {
                        Log.e(this.getClass().getName(), "Unable to parse item link: " + (mHrefAttribute != null ? mHrefAttribute : mSb.toString()) + "Ignoring.", mue); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                else if (isFeed && mFeed.getHomePage() == null)
                {
                    try
                    {
                        if (mSb != null && mSb.toString() != "")  //$NON-NLS-1$
                        {
                            // RSS
                            mFeed.setHomePage(new URL(mSb.toString().trim()));
                        }
                        else if (mMimeAttribute == "text/html") //$NON-NLS-1$
                        {
                            // Atom
                            mFeed.setHomePage(new URL(mHrefAttribute));
                        }
                    }
                    catch (MalformedURLException mue)
                    {
                        Log.w(this.getClass().getName(), "Unable to set feed homepage: " + (mSb != null && mSb.toString() != "" ? mSb.toString() : mHrefAttribute) + ". Ignoring.", mue); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
                mHrefAttribute = null;
                isLink = false;
            }
            else if (hasType(value, "updated")) //$NON-NLS-1$
            {
                if (isItem)
                {
                    mItem.setUpdateDate(DateParser.parseDate(mSb.toString().trim()));
                }
            }
            else if (hasType(value, "pubDate", "published", "date")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            {
                if (isItem)
                {
                    mItem.setPubdate(DateParser.parseDate(mSb.toString().trim()));
                }
                isPubdate = false;
            }
            else if (hasType(value, "guid", "id") && !isSource) //$NON-NLS-1$ //$NON-NLS-2$
            {
                if (isItem) 
                {
                    mItem.setGuid(mSb.toString().trim());
                }
                isGuid = false;
            }
            else if (hasType(value, "description", "summary")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                if (isItem && !TextUtils.equals(qName, "media:description")) 
                {
                    //????
                    //mItem.setContent(mSb.toString());
                    mItem.setDescription(mSb.toString().trim());
                }
                isDescription = false;
            }
            else if (hasType(value, "encoded", "content")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                if (isItem) 
                {
                    //mItem.setContent(removeContentSpanObjects(mSb).toString().trim() + System.getProperty("line.separator"));
                    mItem.setContent(mSb.toString().trim());
                }
                isContent = false;
            }
            else if (value.equalsIgnoreCase("source")) //$NON-NLS-1$
            {
                isSource = false;
            }
            else if (hasType(value, "creator") && isItem) //$NON-NLS-1$
            {
                mItem.setOriginalAuthor(mSb.toString().trim());
            }
            else if (hasType(value, "author") && isItem) //$NON-NLS-1$
            {
                if (TextUtils.equals(mFeed.getType(), Feed.TYPE_RSS) && TextUtils.equals(mVersion, "0.91")) //$NON-NLS-1$
                {
                    mItem.setOriginalAuthor(mSb.toString().trim());
                }
                isAuthor = false;
            }
            else if (hasType(value, "name") && isAuthor && isItem) //$NON-NLS-1$
            {
                mItem.setOriginalAuthor(mSb.toString().trim());
            }
            else if (hasType(value, "url"))  //$NON-NLS-1$
            {
                if ( isFeed && isImage ) 
                {
                    try 
                    {
                        mFeed.setImageUrl(mSb.toString());
                    }
                    catch ( Exception e ) 
                    {
                        //swallow
                    }
                }
            }
            else if (hasType(value, "enclosure")) //$NON-NLS-1$
            {
                if (isItem)
                {
                    try
                    {
                        if (!TextUtils.isEmpty(mMimeAttribute) && IMAGE_MIME_TYPES.contains(mMimeAttribute.toLowerCase()))
                        {
                            if (mItem.getImage() == null)
                            {
                                mItem.setImage(new URL(mHrefAttribute));
                            }
                        }
                        else
                        {
                            // Enclosure for RSS format
                            mEnclosure.setMime(mMimeAttribute);
                            mEnclosure.setURL(new URL(mHrefAttribute));
                            mItem.addEnclosure(mEnclosure);
                            mMimeAttribute = null;
                            mHrefAttribute = null;
                        }
                    }
                    catch (MalformedURLException mue)
                    {
                        Log.w(this.getClass().getName(), "Unable to parse enclosure url: " + mHrefAttribute + ". Ignoring.", mue); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                isEnclosure = false;
            }
        }
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (isType || isTitle || isLink || isPubdate || isGuid || isDescription || isContent) mSb.append(new String(ch, start, length));
    }
    
    public Feed handleFeed(HttpURLConnection connection) throws IOException, SAXException, ParserConfigurationException
    {
        // connection.setRequestProperty("Accept-Encoding", "UTF-8");
        URL url = connection.getURL();
        InputStream stream = connection.getInputStream();
        
        byte[] bytes = IOUtils.toByteArray(stream);
        
        String encoding = getEncoding(connection, bytes);
        
        String s = new String(bytes, encoding);
        
        InputSource is = new InputSource(new StringReader(s));
        
        getParser().parse(is);
        
        // Reordering the list of items, first item parsed (most recent) -> last item in the list
        Collections.reverse(mFeed.getItems());
        mFeed.setURL(url);
        if (mFeed.getHomePage() == null)
        {
            mFeed.setHomePage(url);
        }
        
        return mFeed;
    }
    
    private String getEncoding(HttpURLConnection connection, byte[] bytes)
    {
        String encoding = null;
        try
        {
            encoding = EncodingUtils.getEncoding(connection, bytes);
        }
        catch (Exception e)
        {
            Log.w(this.getClass().getName(), "Couldnot retrieve encoding", e); //$NON-NLS-1$
        }
        
        if (TextUtils.isEmpty(encoding))
        {
            encoding = "UTF-8"; //$NON-NLS-1$
        }
        return encoding;
    }
    
    private XMLReader getParser() throws SAXException, ParserConfigurationException
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        xr.setContentHandler(this);
        return xr;
    }
    
    /*
    private Spanned removeContentSpanObjects(StringBuffer sb)
    {
        SpannableStringBuilder spannedStr = (SpannableStringBuilder) Html.fromHtml(sb.toString().trim());
        Object[] spannedObjects = spannedStr.getSpans(0, spannedStr.length(), Object.class);
        for (int i = 0; i < spannedObjects.length; i++)
        {
            // if (!(spannedObjects[i] instanceof URLSpan) &&
            // !(spannedObjects[i] instanceof StyleSpan))
            if (spannedObjects[i] instanceof ImageSpan)
            {
                spannedStr.replace(spannedStr.getSpanStart(spannedObjects[i]), spannedStr.getSpanEnd(spannedObjects[i]), "");
            }
            // spannedStr.removeSpan(spannedObjects[i]);
        }
        // spannedStr.clearSpans();
        return spannedStr;
    }
    */
}
