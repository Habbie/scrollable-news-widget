package fr.gdi.android.news.utils.feed.load;

import fr.gdi.android.news.model.Feed;

public class ConnectFeedResult
{
    public static final int SUCCESS = 1;
    public static final int ERROR_CANNOT_CONNECT = 2;
    public static final int ERROR_CANNOT_SAVE_ITEMS = 4;
    
    private int state;
    
    private Feed feed;
    
    public ConnectFeedResult(int state, Feed feed)
    {
        this.state = state;
        this.feed = feed;
    }
    
    public int getState()
    {
        return state;
    }
    
    public Feed getFeed()
    {
        return feed;
    }
}
