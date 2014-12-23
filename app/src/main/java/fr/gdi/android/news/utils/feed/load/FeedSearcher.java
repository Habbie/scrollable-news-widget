package fr.gdi.android.news.utils.feed.load;

import fr.gdi.android.news.R;
import fr.gdi.android.news.fragment.feed.FeedListFragment;
import fr.gdi.android.news.utils.dialog.PromptDialog;
import fr.gdi.android.news.utils.feed.search.AbstractSearchTask;
import fr.gdi.android.news.utils.feed.search.LookupFeedTask;
import fr.gdi.android.news.utils.feed.search.SearchFeedTask;

public class FeedSearcher
{
    private FeedListFragment caller;
    
    public FeedSearcher(FeedListFragment fragment)
    {
        this.caller = fragment;
    }
    
    public void search()
    {
        doSearch(new SearchFeedTask(caller), R.string.feed_search_title, R.string.feed_search_prompt);
    }
    
    public void lookup()
    {
        doSearch(new LookupFeedTask(caller), R.string.feed_lookup_title, R.string.feed_lookup_prompt);
    }
    
    private void doSearch(final AbstractSearchTask task, final int title, final int hint)
    {
        PromptDialog dlg = new PromptDialog(caller.getActivity(), title, hint) {
            @Override
            public boolean onOkClicked(String input)
            {
                task.execute(input);
                return true; 
            }
        };
        dlg.show();
    }
}
