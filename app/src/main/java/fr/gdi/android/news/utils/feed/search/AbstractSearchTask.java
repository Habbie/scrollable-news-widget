package fr.gdi.android.news.utils.feed.search;

import java.util.ArrayList;
import java.util.List;

import fr.gdi.android.news.R;
import fr.gdi.android.news.fragment.feed.FeedListAdapter;
import fr.gdi.android.news.fragment.feed.FeedListFragment;
import fr.gdi.android.news.fragment.feed.details.FeedDetailsFragment;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.dialog.ToastUtils;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public abstract class AbstractSearchTask extends AsyncTask<String, Void, Void>
{
    protected ProgressDialog dlg;
    protected List<Feed> feeds = new ArrayList<Feed>();
    
    protected FeedListFragment caller;
    
    protected Context context;
    
    public AbstractSearchTask(FeedListFragment fragment)
    {
        this.context = fragment.getActivity();
        this.caller = fragment;
    }
    
    @Override
    protected void onPreExecute()
    {
        dlg = ProgressDialog.show(context,
                context.getText(R.string.feed_searching_title).toString(),
                getMessage(), true, false);
    }
    
    @Override
    protected void onPostExecute(Void result)
    {
        dlg.dismiss();
        
        if (feeds.size() == 0)
        {
            ToastUtils.showWarning(context, R.string.feed_search_no_result);
        }
        else
        {
            showFoundFeeds();
        }
    }
    
    protected abstract String getMessage();
    
    protected void showFoundFeeds()
    {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    }).create();
        
        dialog.setTitle(String.format(context.getText(R.string.feed_search_match_found).toString(), feeds.size()));
        
        final ListView lv = new ListView(context);
        dialog.setView(lv);
        lv.setAdapter(new FeedListAdapter(context, feeds, true));
        
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id)
            {
                int p = position;
                Feed selected = (Feed) lv.getAdapter().getItem(p);
                dialog.dismiss();
                FeedDetailsFragment newFragment = FeedDetailsFragment.newInstance(caller, selected, true);
                newFragment.show(caller.getFragmentManager(), "add_dialog"); //$NON-NLS-1$
            }
        });
        
        dialog.show();
    }
}
