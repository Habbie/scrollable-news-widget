package fr.gdi.android.news.utils.feed.io;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.FeedDao;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.IOUtils;
import fr.gdi.android.news.utils.feed.io.ExportFeedResult.State;

public class ExportFeedTask extends AsyncTask<File, Void, ExportFeedResult>
{
    private Context context;
    
    private IExportFeedListener listener;
    
    private ProgressDialog dlg;
    
    public ExportFeedTask(Context context, IExportFeedListener listener)
    {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        dlg = ProgressDialog.show(context, 
                context.getText(R.string.feed_export_task_title), 
                context.getText(R.string.feed_export_task_message), true, false);
    }
    
    @Override
    protected ExportFeedResult doInBackground(File... params)
    {
        File file = params[0];
        
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file, false);
            
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8"); //$NON-NLS-1$
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true); //$NON-NLS-1$
            
            serializer.startTag(null, "opml"); //$NON-NLS-1$
            serializer.attribute(null, "version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
            
            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z"); //$NON-NLS-1$
            
            serializer.startTag(null, "head"); //$NON-NLS-1$
            
            serializer.startTag(null, "title"); //$NON-NLS-1$
            serializer.text("Android Scrollable News Export"); //$NON-NLS-1$
            serializer.endTag(null, "title"); //$NON-NLS-1$
            
            serializer.startTag(null, "dateCreated"); //$NON-NLS-1$
            serializer.text(format.format(new Date()));
            serializer.endTag(null, "dateCreated"); //$NON-NLS-1$
            
            serializer.endTag(null, "head"); //$NON-NLS-1$
            
            serializer.startTag(null, "body"); //$NON-NLS-1$
            
            FeedDao dao = DaoUtils.getFeedDao(context);
            List<Feed> feeds = dao.getAll(); 
            for (Feed feed : feeds)
            {
                serializer.startTag(null, "outline"); //$NON-NLS-1$
                
                String title = feed.getTitle(), type = feed.getType();
                URL url = feed.getURL();
                Integer color = feed.getColor();
                
                if ( !TextUtils.isEmpty(title) ) //should always be true
                {
                    serializer.attribute(null, "text", title); //$NON-NLS-1$
                    serializer.attribute(null, "title", title); //$NON-NLS-1$
                }
                
                if ( !TextUtils.isEmpty(type) ) //might happen
                {
                    serializer.attribute(null, "type", type); //$NON-NLS-1$
                }
                
                if ( url != null ) //should always be true
                {
                    serializer.attribute(null, "xmlUrl", url.toString()); //$NON-NLS-1$
                }

                if ( color != null ) 
                {
                    serializer.attribute(null, "color", Integer.toString(color)); //$NON-NLS-1$
                }
                
                serializer.endTag(null, "outline"); //$NON-NLS-1$
            }
            
            serializer.endTag(null, "body"); //$NON-NLS-1$
            serializer.endTag(null, "opml"); //$NON-NLS-1$
            serializer.endDocument();
            
            serializer.flush();
            
            return new ExportFeedResult(file, State.SUCCESS); 
        } 
        catch ( Exception e )
        {
            Log.e(Constants.PACKAGE, "Unable to export feeds", e); //$NON-NLS-1$
            return new ExportFeedResult(file, State.ERROR);
        }
        finally
        {
            IOUtils.close(fos);
        }
    }
    
    @Override
    protected void onPostExecute(ExportFeedResult result)
    {
        dlg.dismiss();
        if ( listener != null ) listener.onExportFeedResult(result);
    }
    
}
