package fr.gdi.android.news.fragment.feed.details;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import fr.gdi.android.news.R;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.StringUtils;

public class FeedDetailsBinder
{
    private FeedDetailsFragment fragment;
    
    public FeedDetailsBinder(FeedDetailsFragment f)
    {
        this.fragment = f;
    }
    
    void bind(View view, Feed feed, boolean isTemplateFeed)
    {
        fragment.titleView = (TextView) view.findViewById(R.id.title);
        fragment.urlView = (TextView) view.findViewById(R.id.url);
        
        if ( feed != null ) 
        {
            fragment.titleView.setText(feed.getTitle());

            if ( feed.getURL() != null ) 
            {
                fragment.initialUrl = feed.getURL().toString();
                fragment.urlView.setText(fragment.initialUrl);
            }
           
            View tv = view.findViewById(R.id.type);
            if ( !isTemplateFeed ) 
            {
                ((TextView)tv).setText(feed.getType());
            }
            else 
            {
                view.findViewById(R.id.type_wrapper).setVisibility(View.GONE);
            }
            
            tv = view.findViewById(R.id.id);
            ((TextView)tv).setText(Long.toString(feed.getId()));
            
            
            if ( feed.getHomePage() != null ) 
            {
                tv = view.findViewById(R.id.homepage);
                ((TextView)tv).setText(feed.getHomePage().toString());
            }
            
            if ( feed.getRefresh() != null ) 
            {
                tv = view.findViewById(R.id.refresh);
                String date = StringUtils.formatDate(feed.getRefresh());
                ((TextView)tv).setText(date);
            }
            else
            {
                view.findViewById(R.id.refresh_wrapper).setVisibility(View.GONE);
            }
            
            Context ctx = fragment.getActivity();
            fragment.getDialog().setTitle(isTemplateFeed ? ctx.getText(R.string.feed_add_title) : feed.getTitle());
        }
        else 
        {
            fragment.getDialog().setTitle(R.string.feed_add_title);
            view.findViewById(R.id.refresh_wrapper).setVisibility(View.GONE);
            view.findViewById(R.id.homepage_wrapper).setVisibility(View.GONE);
            view.findViewById(R.id.type_wrapper).setVisibility(View.GONE);
        }
    }
}
