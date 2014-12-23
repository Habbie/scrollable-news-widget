package fr.gdi.android.news.utils.feed.io;

import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.feed.io.ImportFeedResult.State;


public class ImportFeedTask extends AsyncTask<File, Void, ImportFeedResult>
{
    private ProgressDialog dlg;
    
    private Context context; 
    
    private IImportFeedListener listener;
    
    public ImportFeedTask(Context context, IImportFeedListener callback)
    {
        this.context = context;
        this.listener = callback;
    }
    
    @Override
    protected void onPreExecute()
    {
        dlg = ProgressDialog.show(context, 
                context.getText(R.string.feed_import_task_title), 
                context.getText(R.string.feed_import_task_message), true, false);
    }
    
    @Override
    protected ImportFeedResult doInBackground(File... params)
    {
        File file = params[0];
        
        if ( file == null || !file.exists() ) 
        {
            return new ImportFeedResult(State.INVALID_FILE);
        }
        
        try
        {
            List<Feed> feeds = getFeeds(file);
            
            if ( feeds.size() == 0 ) 
            {
                return new ImportFeedResult(State.NO_FEED_FOUND);
            }
            
            return new ImportFeedResult(State.SUCCESS, feeds);
        }
        catch (SAXException e)
        {
            return new ImportFeedResult(State.INVALID_FILE);
        }
        catch (Exception e)
        {
            Log.e(Constants.PACKAGE, "Unable to import feeds", e); //$NON-NLS-1$
            return new ImportFeedResult(State.ERROR);
        }
    }
    
    private List<Feed> getFeeds(File file) throws Exception
    {
        final List<Feed> feeds = new ArrayList<Feed>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLReader saxReader = parser.getXMLReader();
        saxReader.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
            {
                if ("outline".equals(localName)) //$NON-NLS-1$
                {
                    try
                    {
                        Feed feed = new Feed();
    
                        int titleIndex = attributes.getIndex("title"); //$NON-NLS-1$
                        int xmlUrlIndex = attributes.getIndex("xmlUrl"); //$NON-NLS-1$
                        
                        if ( titleIndex < 0 || xmlUrlIndex < 0 )
                        {
                            //malformed feed declaration. just ignore it
                            return;
                        }
                        
                        String title = attributes.getValue(titleIndex); 
                        feed.setTitle(title);
    
                        String url = attributes.getValue(xmlUrlIndex); 
                        feed.setURL(new URL(url));
                        
                        int typeIndex = attributes.getIndex("type"); //$NON-NLS-1$
                        if ( typeIndex >= 0 ) 
                        {
                            //type is optional
                            String type = attributes.getValue(typeIndex); 
                            feed.setType(type);                        
                        }
    
                        int colorIndex = attributes.getIndex("color"); //$NON-NLS-1$
                        if ( colorIndex >= 0 ) 
                        {
                            //color is optional
                            String color = attributes.getValue(colorIndex); 
                            try 
                            {
                                if ( !TextUtils.isEmpty(color) ) 
                                {
                                    feed.setColor(Integer.parseInt(color));
                                }
                            }
                            catch ( NumberFormatException e ) 
                            {
                                //cannot parse color. just ignore it
                                Log.w(Constants.PACKAGE, "[Import] Unable to parse feed color: " + color); //$NON-NLS-1$
                            }
                        }
                        
                        feeds.add(feed);
                    }
                    catch (MalformedURLException e)
                    {
                        Log.w(Constants.PACKAGE, "Unable to parse opml entry: malformed URL", e); //$NON-NLS-1$
                    }
                }
            }
            
        });
        
        saxReader.parse(new InputSource(new FileReader(file)));
        
        return feeds;
    }
    
    @Override
    protected void onPostExecute(ImportFeedResult result)
    {
        dlg.dismiss();
        if ( this.listener != null ) 
        {
            listener.onImportFeedResult(result);
        }
    }
}
