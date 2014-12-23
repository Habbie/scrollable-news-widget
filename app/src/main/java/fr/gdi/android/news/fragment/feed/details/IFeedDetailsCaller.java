package fr.gdi.android.news.fragment.feed.details;

import fr.gdi.android.news.model.Feed;

public interface IFeedDetailsCaller
{
    void feedDetailsClosed(Feed feed, Feed originalfeed);
    
    void feedDetailsCanceled();
}
