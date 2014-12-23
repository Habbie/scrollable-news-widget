package fr.gdi.android.news.utils.feed.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.text.Html;
import fr.gdi.android.news.R;
import fr.gdi.android.news.fragment.feed.FeedListFragment;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.StringUtils;

public class SearchFeedTask extends AbstractSearchTask
{
    private static final String FIND_URL = "https://ajax.googleapis.com/ajax/services/feed/find?v=1.0&q="; //$NON-NLS-1$
    
    public SearchFeedTask(FeedListFragment fragment)
    {
        super(fragment);
    }
    
    @Override
    protected String getMessage()
    {
        return context.getText(R.string.feed_searching_message).toString();
    }
    
    @Override
    protected Void doInBackground(String... args)
    {
        String value = args[0];
        InputStream in = null;
        try
        {
            String url = FIND_URL + URLEncoder.encode(value, "UTF-8"); //$NON-NLS-1$
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            
            String json = ""; //$NON-NLS-1$
            
            in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                str.append(line + "\n"); //$NON-NLS-1$
            }
            json = str.toString();
            
            JSONObject object = new JSONObject(json);
            if (object.has("responseData")) //$NON-NLS-1$
            {
                object = object.getJSONObject("responseData"); //$NON-NLS-1$
                if (object.has("entries")) //$NON-NLS-1$
                {
                    JSONArray array = object.getJSONArray("entries"); //$NON-NLS-1$
                    for (int u = 0, l = array.length(); u < l; u++)
                    {
                        JSONObject o = array.getJSONObject(u);
                        String feedUrl = o.getString("url"); //$NON-NLS-1$
                        String feedTitle = StringUtils.stripTags(o.getString("title")); //$NON-NLS-1$
                        feedTitle = Html.fromHtml((String) feedTitle).toString();
                        Feed feed = new Feed();
                        feed.setTitle(feedTitle);
                        feed.setURL(new URL(feedUrl));
                        feeds.add(feed);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    // swallow
                }
            }
        }
        return null;
    }
    
}