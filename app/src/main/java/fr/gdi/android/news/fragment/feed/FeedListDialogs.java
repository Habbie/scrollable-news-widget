package fr.gdi.android.news.fragment.feed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import fr.gdi.android.news.R;
import fr.gdi.android.news.model.Feed;

public class FeedListDialogs
{
    
    private Context context;
    
    public FeedListDialogs(Context context)
    {
        this.context = context;
    }
    
    public AlertDialog showFeedSelectionDialog(List<Feed> initialSet, final IFeedSelectionListener listener)
    {
        final Map<String, Feed> titleMapping = new HashMap<String, Feed>();
        for (Feed feed : initialSet)
        {
            titleMapping.put(feed.getTitle(), feed);
        }
        
        Set<String> keys = titleMapping.keySet();
        final String[] titles = keys.toArray(new String[keys.size()]);
        final List<Feed> selectedFeeds = new ArrayList<Feed>();
        
        return new AlertDialog.Builder(context)
                .setTitle(R.string.feed_import_title)
                .setMultiChoiceItems(titles, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        Feed selected = titleMapping.get(titles[which]);
                        if (isChecked)
                        {
                            selectedFeeds.add(selected);
                        }
                        else
                        {
                            selectedFeeds.remove(selected);
                        }
                    }
                }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        if ( listener != null ) listener.feedSelected(selectedFeeds);
                    }
                }).show();
    }
    
    public AlertDialog showFeedImportedDialog(List<Feed> selectedFeeds)
    {
        String message = String.format(context.getText(R.string.feed_import_success).toString(), selectedFeeds.size());
        return new AlertDialog.Builder(context)
                .setTitle(R.string.info).setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                }).show();
    }
    
}
