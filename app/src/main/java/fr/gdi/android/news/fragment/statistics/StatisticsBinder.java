package fr.gdi.android.news.fragment.statistics;

import java.io.File;

import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.FeedDao;
import fr.gdi.android.news.data.dao.ItemDao;
import fr.gdi.android.news.utils.IOUtils;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class StatisticsBinder
{
    private Context context;
    
    private FeedDao feedDao;
    private ItemDao itemDao;
    
    public StatisticsBinder(Context context)
    {
        this.context = context;
        this.itemDao = DaoUtils.getItemDao(context);
        this.feedDao = DaoUtils.getFeedDao(context);
    }
    
    public void bind(final View v)
    {
        ((TextView) v.findViewById(R.id.feed_count)).setText(Integer.toString(feedDao.count()));
        ((TextView) v.findViewById(R.id.items_saved)).setText(Integer.toString(itemDao.count()));
        ((TextView) v.findViewById(R.id.items_read)).setText(Integer.toString(itemDao.count("where read=?", new Object[] {1}))); //$NON-NLS-1$
        ((TextView) v.findViewById(R.id.favorites)).setText(Integer.toString(itemDao.count("where favorite=?", new Object[] {1}))); //$NON-NLS-1$
        ((TextView) v.findViewById(R.id.most_active)).setText(feedDao.getMostActiveFeed());
        
        ((TextView) v.findViewById(R.id.db_size)).setText(getDatabaseSize());

        final TextView totalSpaceView = (TextView) v.findViewById(R.id.total_space);
        final TextView sdCardSpaceView = (TextView) v.findViewById(R.id.sdcard_space);
        final TextView internalSpaceView = (TextView) v.findViewById(R.id.internal_space);
        final TextView thumbnailsCachedView = (TextView) v.findViewById(R.id.thumbnails_cached);
        
        
        totalSpaceView.setText(R.string.stat_counting);
        sdCardSpaceView.setText(R.string.stat_counting);
        thumbnailsCachedView.setText(R.string.stat_counting);
        internalSpaceView.setText(R.string.stat_counting);
        
        new StatisticsBinderTask(context, v).execute();    
    }
    
    private String getDatabaseSize()
    {
        File f = new File(DaoUtils.getDatabasePath(context));
        return IOUtils.formatFileSize(f.length());
    }
    
}
