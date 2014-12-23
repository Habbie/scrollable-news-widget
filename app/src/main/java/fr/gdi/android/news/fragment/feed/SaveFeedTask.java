package fr.gdi.android.news.fragment.feed;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.FeedDao;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.image.ImageUtils;

public class SaveFeedTask extends AsyncTask<Feed, Void, SaveFeedResult>
{
    private AlertDialog dlg;
    
    private Context context;
    
    private ISaveFeedListener listener;
    
    public SaveFeedTask(Context context, ISaveFeedListener listener)
    {
        this.context = context;
        this.listener = listener;
    }
    
    @Override
    public void onPreExecute()
    {
        dlg = new AlertDialog.Builder(context)
                             .setCancelable(false)
                             .setMessage(R.string.feed_saving_message)
                             .setTitle(R.string.feed_saving_title)
                             .show();
    }
    @Override
    protected SaveFeedResult doInBackground(Feed... params)
    {
        Feed updated = params[0];
        Feed original = params.length > 1 ? params[1] : null;
        
        boolean newFeed = original == null;
        
        FeedDao feedDao = DaoUtils.getFeedDao(context);
        
        if ( newFeed ) 
        {
            feedDao.insert(updated);
        }
        else 
        {
            boolean urlChanged = !TextUtils.equals(original.getURL().toString().trim(), updated.getURL().toString().trim());
            feedDao.update(updated,  urlChanged);
        }
                
        String imageUrl = updated.getImageUrl();
        if ( !TextUtils.isEmpty(imageUrl) )
        {
            boolean isImageDownloaded = ImageUtils.isImageDownloaded(imageUrl, ImageUtils.FULL);
            
            if ( !isImageDownloaded ) 
            {
                ImageUtils.downloadAndCacheImage(context, imageUrl, FeedBinder.FEED_ICON_SIZE);
            }
        }

        return new SaveFeedResult(true, newFeed, updated);
    }
    
    @Override
    protected void onPostExecute(SaveFeedResult result)
    {
        dlg.dismiss();
        if ( listener != null ) 
        {
            listener.saveFeedCallback(result);
        }
    }
}
