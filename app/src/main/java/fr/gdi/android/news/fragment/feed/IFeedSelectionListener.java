package fr.gdi.android.news.fragment.feed;

import java.util.List;

import fr.gdi.android.news.model.Feed;

public interface IFeedSelectionListener
{
    void feedSelected(List<Feed> selection);
}
