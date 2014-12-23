package fr.gdi.android.news.fragment.feed;

import fr.gdi.android.news.model.Feed;

public class SaveFeedResult
{
    private boolean success, newFeed;
    private Feed feed;
    
    public SaveFeedResult(boolean success, boolean newFeed, Feed feed)
    {
        this.success = success;
        this.newFeed = newFeed;
        this.feed = feed;
    }
    
    public boolean isNewFeed()
    {
        return newFeed;
    }
    
    public boolean isSuccess()
    {
        return success;
    }
    
    public Feed getFeed()
    {
        return feed;
    }
    
}