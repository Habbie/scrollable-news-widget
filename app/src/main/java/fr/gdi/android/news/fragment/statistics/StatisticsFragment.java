package fr.gdi.android.news.fragment.statistics;

import java.io.File;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import fr.gdi.android.news.R;
import fr.gdi.android.news.SimpleNewsApplication;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.FeedDao;
import fr.gdi.android.news.data.dao.ItemDao;
import fr.gdi.android.news.receiver.NewsWidgetProvider;
import fr.gdi.android.news.receiver.WidgetUpdater;
import fr.gdi.android.news.utils.DeviceUtils;
import fr.gdi.android.news.utils.IOUtils;
import fr.gdi.android.news.utils.dialog.ConfirmDialog;
import fr.gdi.android.news.utils.dialog.IConfirmCallback;
import fr.gdi.android.news.utils.image.ImageUtils;

public class StatisticsFragment extends Fragment
{
    private static final int OPTION_CLEAR_WEB_CACHE = 4;
    private static final int OPTION_CLEAR_THUMB_CACHE = 1;
    private static final int OPTION_CLEAR_ITEM_TABLE = 2;
    
    private StatisticsBinder binder;
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.binder = new StatisticsBinder(activity);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.statistics, null);
        
        binder.bind(v);
        
        return v;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.add(Menu.NONE, OPTION_CLEAR_WEB_CACHE, Menu.NONE, R.string.stat_delete_app_cache);
        menu.add(Menu.NONE, OPTION_CLEAR_THUMB_CACHE, Menu.NONE, R.string.stat_delete_thumbnails);
        menu.add(Menu.NONE, OPTION_CLEAR_ITEM_TABLE, Menu.NONE, R.string.stat_delete_all_news);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int itemId = item.getItemId();
        if ( itemId == OPTION_CLEAR_ITEM_TABLE || itemId == OPTION_CLEAR_THUMB_CACHE || itemId == OPTION_CLEAR_WEB_CACHE ) 
        {
            ConfirmDialog dialog = new ConfirmDialog(getActivity(), 
                    getActivity().getText(R.string.confirmation).toString(), 
                    getActivity().getText(R.string.stat_confirm_message).toString()); 
            dialog.setCallback(new IConfirmCallback() {
                @Override
                public void dialogClosed(int which)
                {
                    if ( which == ConfirmDialog.OK ) 
                    {
                        new AsyncCommand(getActivity(), binder, getView()).execute(itemId);
                    }
                }
            });
            dialog.show();
        }
        return false;
    }
    
    private static class AsyncCommand extends AsyncTask<Integer, Void, Void>
    {
        private ProgressDialog progressDialog; 
        
        private Context context; 
        private StatisticsBinder binder;
        private View view;
        
        public AsyncCommand(Context context, StatisticsBinder binder, View view)
        {
            this.binder = binder;
            this.context = context;
            this.view = view;
        }
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(context.getText(R.string.deleting));
            progressDialog.show();
        }
        
        @Override
        protected void onPostExecute(Void result)
        {
            progressDialog.dismiss();
            binder.bind(view);
        }
        
        protected Void doInBackground(Integer... params) 
        {
            switch (params[0])
            {
                case OPTION_CLEAR_WEB_CACHE:
                    File cacheDir = ((SimpleNewsApplication) context.getApplicationContext()).getCacheDir();
                    IOUtils.empty(cacheDir);
                    break;
                case OPTION_CLEAR_THUMB_CACHE:
                    IOUtils.empty(ImageUtils.getThumbnailDir());
                    FeedDao feedDao = DaoUtils.getFeedDao(context);
                    feedDao.resetFeedThumbnails();
                    break;
                case OPTION_CLEAR_ITEM_TABLE:
                    ItemDao itemDao = DaoUtils.getItemDao(context);
                    itemDao.clear();
                    //broadcast changes without triggering a network reload
                    Intent updateIntent = new Intent(context, NewsWidgetProvider.class);
                    updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, DeviceUtils.getAllWidgetIds(context));
                    updateIntent.setAction(WidgetUpdater.ACTION_UPDATE);
                    context.sendBroadcast(updateIntent);
                    break;
                default:
                    break;
            }
            
            return null;
        };
    }
}
