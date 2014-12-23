package fr.gdi.android.news.fragment.feed.details;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fr.gdi.android.news.R;
import fr.gdi.android.news.activity.ColorPickerActivity;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.StringUtils;
import fr.gdi.android.news.utils.dialog.ToastUtils;

public class FeedDetailsFragment extends DialogFragment implements View.OnClickListener
{
    private static final String IS_TEMPLATE_KEY = "isTemplateFeed"; //$NON-NLS-1$

    private static final String FEED_KEY = "feed"; //$NON-NLS-1$

    private static final int REQUEST_SELECT_COLOR = 1; 
    
    private Feed feed;
    
    private IFeedDetailsCaller caller;
    
    String initialUrl;
    
    TextView titleView, urlView;
    
    private String type;
    private URL homepage;
    private String feedImage;
    private Integer color;
    
    private boolean isTemplateFeed;
    
    private FeedDetailsBinder binder;
    private FeedDetailsDialogs dialogs;
    
    
    public FeedDetailsFragment()
    {
        this.binder = new FeedDetailsBinder(this);
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        dialogs = new FeedDetailsDialogs(getActivity());
    }
    
    public static FeedDetailsFragment newInstance(IFeedDetailsCaller caller, Feed feed, boolean isTemplateFeed) 
    {
        FeedDetailsFragment f = new FeedDetailsFragment();
        f.caller = caller;

        Bundle args = new Bundle();
        if ( feed != null ) { 
            args.putSerializable(FEED_KEY, feed);
            args.putBoolean(IS_TEMPLATE_KEY, isTemplateFeed);
            f.setArguments(args);
        }
       
        return f;
    }
    
    public static FeedDetailsFragment newInstance(IFeedDetailsCaller caller, Feed feed) 
    {
        return newInstance(caller, feed, false);
    }

    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null )
        {
            feed = (Feed) getArguments().getSerializable(FEED_KEY);
            isTemplateFeed = (Boolean)  getArguments().getBoolean(IS_TEMPLATE_KEY);
            if ( feed != null ) 
            {
                this.type = feed.getType();
                this.homepage = feed.getHomePage();
                this.feedImage = feed.getImageUrl();
                this.color = feed.getColor();
            }
        }
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        View v = inflater.inflate(R.layout.feed_details_dialog, container, false);
        
        binder.bind(v, feed, isTemplateFeed);
        
        v.findViewById(R.id.pick_color_btn).setOnClickListener(this);
        v.findViewById(R.id.clear_color_btn).setOnClickListener(this);
        
        v.findViewById(R.id.cancel).setOnClickListener(this);
        v.findViewById(R.id.ok).setOnClickListener(this);
        
        return v;
    }
    
    @Override
    public void onClick(View v)
    {
        switch ( v.getId() ) 
        {
            case R.id.pick_color_btn:
                Intent intent = new Intent(getActivity(), ColorPickerActivity.class);
                if ( color != null ) intent.putExtra(ColorPickerActivity.INTENT_DATA_INITIAL_COLOR, color);
                startActivityForResult(intent, REQUEST_SELECT_COLOR);
                break;
            case R.id.clear_color_btn:
                color = null;
                break;
            case R.id.ok:
                URL feedUrl = getUrl();
                
                if ( feedUrl != null )
                {
                    if ( !TextUtils.equals(feedUrl.toString(), initialUrl) || isTemplateFeed )
                    {
                        getFetchTask().execute(feedUrl);
                    }
                    else 
                    {
                        callback();
                    }
                }
                else 
                {
                    if ( TextUtils.isEmpty(urlView.getText()) )
                    {
                        dialogs.showEmptyUrlDialog();
                    }
                    else 
                    {
                        dialogs.showInvalidUrlDialog(R.string.feed_invalid_parameters_title, R.string.feed_invalid_parameters_details, urlView.getText().toString());
                    }
                }
                break;
            case R.id.cancel: 
                getDialog().dismiss();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK) 
        {
            if (requestCode == REQUEST_SELECT_COLOR) 
            {
                color = data.getIntExtra(ColorPickerActivity.RESULT_COLOR, 0);
            }
        }
    }
    
    private URL getUrl()
    {
        CharSequence url = urlView.getText();
        
        if ( TextUtils.isEmpty(url) ) return null;
        
        try 
        {
            url = StringUtils.addHttpProtocolIfMissing(url.toString());
            return new URL(url.toString());
        }
        catch ( MalformedURLException e ) 
        {
            return null;
        }
    }

    private String getTitle()
    {
        CharSequence title = titleView.getText();
        return title.toString();
    }

    private void callback()
    {
        getDialog().dismiss();
        
        Feed f = new Feed();
        f.setTitle(getTitle());
        f.setURL(getUrl());
        f.setType(type);
        f.setHomePage(homepage);
        f.setImageUrl(feedImage);
        f.setColor(color);
        
        if ( caller != null ) 
        {
            if ( feed != null && !isTemplateFeed )  
            {
                f.setId(feed.getId());
                caller.feedDetailsClosed(f, feed);
            }
            else 
            {
                caller.feedDetailsClosed(f, null);
            }
        }
        else
        {
            ToastUtils.showError(getActivity(), R.string.feed_details_invalid_state);
        }
    }

    private FetchTask getFetchTask()
    {
        FetchTask task = new FetchTask(getActivity(), new FetchTaskListener() {
            private AlertDialog dlg;
            @Override
            public void onPreExecute()
            {
                dlg = dialogs.showFetchDialog();
            }
            @Override
            public void onPostExecute(Feed f)
            {
                dlg.dismiss();
                if ( f == null )
                {
                    dialogs.showInvalidUrlDialog(R.string.feed_validation_failure_title, R.string.feed_validation_failure_details, urlView.getText().toString());
                }
                else 
                {
                    if ( TextUtils.isEmpty(getTitle()) ) 
                    {
                        titleView.setText(f.getTitle());
                    }
                    
                    type = f.getType();
                    homepage = f.getHomePage();
                    feedImage = f.getImageUrl();
                    
                    callback();
                }
            }
        });
        return task;
    }
}
