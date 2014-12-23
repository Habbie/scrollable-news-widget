package fr.gdi.android.news.fragment.feed;

import java.io.File;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.image.DownloadImageCallbackAdapter;
import fr.gdi.android.news.utils.image.ImageUtils;

public class FeedBinder
{
    public static final int FEED_ICON_SIZE = 48;
    
    private boolean hideImage;
    
    
    public void setImageHidden(boolean b)
    {
        this.hideImage = b;
    }
    
    public void bind(View v, final Feed feed)
    {
        TextView urlView = (TextView) v.findViewById(R.id.feed_url);
        TextView titleView = (TextView) v.findViewById(R.id.feed_title);
        
        if (urlView != null) urlView.setText(feed.getURL().toString());
        if (titleView != null) titleView.setText(feed.getTitle());
        
        bindImage(v, feed);
        
    }

    private void bindImage(View v, final Feed feed)
    {
        final ImageView imageView = (ImageView) v.findViewById(R.id.feedImage);
        if ( hideImage ) 
        {
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(0, RelativeLayout.LayoutParams.FILL_PARENT));
        }
        else 
        {
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(48, RelativeLayout.LayoutParams.FILL_PARENT));
            
            String url = feed.getImageUrl(); 
            
            if ( !TextUtils.isEmpty(url) )
            {
                File localFile = ImageUtils.getImageCacheFile(url, Integer.toString(FEED_ICON_SIZE));
                if ( localFile.exists() ) 
                {
                    imageView.setImageURI(Uri.parse("file://" + localFile.getAbsolutePath())); //$NON-NLS-1$
                    return;
                }
                else 
                {
                    ImageUtils.downloadAndCacheImage(v.getContext(), url, FEED_ICON_SIZE, new DownloadImageCallbackAdapter() {
                        @Override
                        public void downloadCompleted(File f)
                        {
                            try 
                            {
                                imageView.setImageURI(Uri.parse("file://" + f.getAbsolutePath())); //$NON-NLS-1$
                            }
                            catch ( Exception e ) 
                            {
                                //imageView not valid anymore?
                                Log.w(Constants.PACKAGE, "Couldnot set imageView content after download", e); //$NON-NLS-1$
                            }
                        }
                    });
                }
            }
        }
        
        //if we reach this point url is null OR image has not been downloaded yet 
        imageView.setImageResource(R.drawable.default_feed_icon);
    }
}
