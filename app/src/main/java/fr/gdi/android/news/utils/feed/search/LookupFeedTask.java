package fr.gdi.android.news.utils.feed.search;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import fr.gdi.android.news.R;
import fr.gdi.android.news.fragment.feed.FeedListFragment;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.CharsetDetector;
import fr.gdi.android.news.utils.IOUtils;
import fr.gdi.android.news.utils.StringUtils;

public class LookupFeedTask extends AbstractSearchTask 
{
    private static final Pattern LINK_PATTERN = Pattern.compile("<link[^>]+>", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    private static final Pattern LINK_TYPE_PATTERN = Pattern.compile("type=('|\")([^('|\")]*)('|\")", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    private static final Pattern LINK_HREF_PATTERN = Pattern.compile("href=('|\")([^('|\")]*)('|\")", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    private static final Pattern LINK_TITLE_PATTERN = Pattern.compile("title=('|\")([^('|\")]*)('|\")", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    private static final List<String> KNOWN_FEED_TYPES = Arrays.asList(new String[] { 
            "application/rss+xml", "application/atom+xml", "application/rss xml", "application/atom xml", }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    public LookupFeedTask(FeedListFragment fragment)
    {
        super(fragment);
    }
    
    @Override
    protected String getMessage()
    {
        return context.getText(R.string.feed_lookup_message).toString();
    }
    
    protected Void doInBackground(String... urls)
    {
        String url = urls[0];

        url = StringUtils.addHttpProtocolIfMissing(url);
        
        try
        {
            
            
            byte[] bytes = IOUtils.getRemoteURLBytes(url);
            
            String s = new String(bytes, CharsetDetector.detectCharset(bytes).name());
            
           
            Matcher m = LINK_PATTERN.matcher(s);
            while (m.find())
            {
                String g = m.group();
                Matcher m2 = LINK_TYPE_PATTERN.matcher(g);
                if (m2.find())
                {
                    String attr = m2.group();
                    String type = attr.substring(6, attr.length() - 1);
                    
                    if (KNOWN_FEED_TYPES.contains(type))
                    {
                        Feed feed = null;
                        Matcher m3 = LINK_HREF_PATTERN.matcher(g);
                        
                        if (m3.find())
                        {
                            attr = m3.group();
                            String href = attr.substring(6, attr.length() - 1);
                            href = makeAbsolute(href, url);
                            feed = new Feed();
                            feed.setURL(new URL(href));
                        }
                        
                        if (feed != null)
                        {
                            Matcher m4 = LINK_TITLE_PATTERN.matcher(g);
                            if (m4.find())
                            {
                                attr = m4.group();
                                String title = attr.substring(7, attr.length() - 1);
                                feed.setTitle(title);
                            }
                            feeds.add(feed);
                        }
                    }
                }
                
            }
        }
        catch (Exception e)
        {
            Log.w(this.getClass().getName(), "Unable to load feeds from " + url, e); //$NON-NLS-1$
        }
        return null;
    }
    
    private String makeAbsolute(String href, String base)
    {
        if ( StringUtils.hasProtocol(href) ) return href;
        return StringUtils.removeEnd(base, "/") + "/" + StringUtils.removeStart(href, "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
