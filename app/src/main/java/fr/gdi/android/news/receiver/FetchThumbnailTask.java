package fr.gdi.android.news.receiver;

import java.net.URL;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.ItemDao;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.utils.image.ImageUtils;

/**
 * TODO: Use a queue instead of serializing donwload 
 *     : requests + make the queue size parametrable 
 */
final class FetchThumbnailTask extends AsyncTask<Void, Void, Void>
{
    private final List<Feed> result;
    
    private Context context;

    private int[] appWidgetIds;

    FetchThumbnailTask(WidgetReloadTask parentTask, List<Feed> result)
    {
        this.result = result;
        this.context = parentTask.context;
        this.appWidgetIds = parentTask.appWidgetIds;
    }
    
    @Override
    protected void onPreExecute() 
    {
        Intent intent = new Intent(context, NewsWidgetProvider.class);
        intent.setAction(NewsWidgetProvider.SET_LOADING);
        intent.putExtra(NewsWidgetProvider.FETCHING_THUMBNAILS_KEY, true);
        intent.putExtra(NewsWidgetProvider.UPDATE_DATE_KEY, true);
        intent.putExtra(NewsWidgetProvider.LOADING_KEY, false);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }
    
    @Override
    protected Void doInBackground(Void... params)
    {
        ItemDao itemDao = DaoUtils.getItemDao(context);
        for (Feed feed : result)
        {
            List<Item> items = feed.getItems();
            for (Item item : items)
            {
                try 
                {
                    if ( item == null || item.isMalformedLink() ) continue; //may happen?
                    
                    item = itemDao.getItem(item.getLinkURL());
                    if (item != null && item.getImage() == null)
                    {
                        String image = ImageUtils.extractFromText(context, item.getDescription());
                        if ( !TextUtils.isEmpty(image) ) 
                        {
                            try 
                            {
                                boolean done = ImageUtils.downloadAndCacheFullImage(context, image);
                                if ( done ) 
                                {
                                    //referenced image is correct. Save that reference
                                    item.setImage(new URL(image));
                                    itemDao.updateImage(item);
                                }
                                else
                                {
                                    itemDao.setDefaultImage(item);
                                }
                            }
                            catch ( Exception e )
                            {
                                Log.w(Constants.PACKAGE, "Could not retrieve item thumbnail. Using default instead. " + image + " (" + e.getClass().getName() + ")"); //$NON-NLS-1$
                                itemDao.setDefaultImage(item);
                            }
                        }   
                        else
                        {
                            //no image found in body. Prevent later lookup 
                            itemDao.setDefaultImage(item);
                        }
                    }
                    else
                    {
                        // check item.getImage() nullity? 
                        boolean done = item.getImage() == null ? false : ImageUtils.downloadAndCacheFullImage(context, item.getImage().toString());
                        if ( !done ) 
                        {
                            itemDao.setDefaultImage(item);
                        }
                    }
                }
                catch (Exception e) 
                {
                    Log.w(Constants.PACKAGE, "Error loading thumbnail", e); //$NON-NLS-1$
                }
            }   
        }
        
        return null;
    }
    
    @Override
    protected void onPostExecute(Void result) 
    {
        Log.d(Constants.PACKAGE, "Done fetching thumbnails. Sending broadcast"); //$NON-NLS-1$
        
        Intent intent = new Intent(context, NewsWidgetProvider.class);
        intent.setAction(WidgetUpdater.ACTION_UPDATE);
        intent.putExtra(NewsWidgetProvider.UPDATE_DATE_KEY, false);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }
    
}