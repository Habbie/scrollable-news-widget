package fr.gdi.android.news.utils.feed.load;

import fr.gdi.android.news.model.Feed;

public class FeedLoaderResult
{
    public static enum State
    {
        SUCCESS, ERROR, FRESH_ENOUGH;
    }
    
    private Feed feed;
    
    private State state;

    public FeedLoaderResult(Feed feed, State state)
    {
        this.feed = feed;
        this.state = state;
    }

    public Feed getFeed()
    {
        return feed;
    }

    public State getState()
    {
        return state;
    }
    
    public boolean isSuccess()
    {
        return state == State.SUCCESS;
    }
    
    public boolean isError()
    {
        return state == State.ERROR;
    }
    
    public boolean isFreshEnough()
    {
        return state == State.FRESH_ENOUGH;
    }
}
