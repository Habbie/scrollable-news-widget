package fr.gdi.android.news.fragment.statistics;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import fr.gdi.android.news.R;
import fr.gdi.android.news.utils.IOUtils;

class StatisticsBinderTask extends AsyncTask<Void, Void, Map<Integer, Object>> 
{
    private Context context;
    private View updateView;
    
    public StatisticsBinderTask(Context context, View updateView)
    {
        this.context = context;
        this.updateView = updateView;
    }
    
    protected Map<Integer, Object> doInBackground(Void... params)
    {
        final File cacheFolder = IOUtils.getCacheDir();
        final File appFolder = IOUtils.getAppDir(context).getParentFile();
        final File imageFolder = new File(cacheFolder, "images"); //$NON-NLS-1$
        
        Map<Integer, Object> result = new HashMap<Integer, Object>();
        
        Long cacheSpace = IOUtils.getFileSize(cacheFolder);
        Long internalSpace = IOUtils.getFileSize(appFolder);
        
        result.put(R.id.sdcard_space, IOUtils.formatFileSize(cacheSpace));
        result.put(R.id.internal_space, IOUtils.formatFileSize(internalSpace));
        result.put(R.id.total_space, IOUtils.formatFileSize(cacheSpace + internalSpace));
        result.put(R.id.thumbnails_cached, IOUtils.getFileCount(imageFolder));
        
        return result;
    }
    @Override
    protected void onPostExecute(Map<Integer, Object> result)
    {
        for (Integer id : result.keySet())
        {
            ((TextView) updateView.findViewById(id)).setText(result.get(id).toString());
        }
    }
}