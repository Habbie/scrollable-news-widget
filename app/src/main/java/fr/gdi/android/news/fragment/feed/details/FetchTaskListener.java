package fr.gdi.android.news.fragment.feed.details;

import fr.gdi.android.news.model.Feed;

public interface FetchTaskListener
{
    void onPreExecute();
    void onPostExecute(Feed feed);
}
