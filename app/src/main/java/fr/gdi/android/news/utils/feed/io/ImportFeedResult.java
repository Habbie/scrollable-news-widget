package fr.gdi.android.news.utils.feed.io;

import java.util.List;

import android.content.Context;

import fr.gdi.android.news.R;
import fr.gdi.android.news.model.Feed;

public class ImportFeedResult
{
    public static enum State 
    {
        SUCCESS, INVALID_FILE, NO_FEED_FOUND, ERROR;
        
        public String toString(Context context) 
        {
            int resId = getResId();
            return context.getText(resId).toString();
        }
        
        private int getResId()
        {
            if ( this == INVALID_FILE )
            {
                return R.string.feed_import_result_invalid_file; 
            }
            
            if ( this == NO_FEED_FOUND )
            {
                return R.string.feed_import_result_no_feed_found;
            }
            
            if ( this == ERROR )
            {
                return R.string.feed_import_result_parse_error;
            }
            
            return R.string.success;
        }
    }
    
    private State state;
    
    private List<Feed> feeds;

    public ImportFeedResult(State state)
    {
        this(state, null);
    }
    
    public ImportFeedResult(State state, List<Feed> feeds)
    {
        this.state = state;
        this.feeds = feeds;
    }

    public State getState()
    {
        return state;
    }

    public List<Feed> getFeeds()
    {
        return feeds;
    }
    
    
}
