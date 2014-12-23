package fr.gdi.android.news.fragment.item;

import java.io.File;

import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.model.Theme;
import fr.gdi.android.news.utils.StringUtils;
import fr.gdi.android.news.utils.image.DownloadImageCallbackAdapter;
import fr.gdi.android.news.utils.image.ImageUtils;

public class ItemBinder
{
 
    public void bind(View v, Item item)
    {
        boolean forceFeedTitleAsAuthor = false;
        
        TextView titleView = (TextView) v.findViewById(R.id.item_title);
        TextView descriptionView = (TextView) v.findViewById(R.id.item_description);
        TextView authorView = (TextView) v.findViewById(R.id.item_author);
        TextView dateView = (TextView) v.findViewById(R.id.item_date);
        TextView linkView = (TextView) v.findViewById(R.id.item_link);
        TextView idView = (TextView) v.findViewById(R.id.item_id);
        
        titleView.setText(item.getTitle().toString());
        linkView.setText(item.getLinkURL());
        idView.setText(Long.toString(item.getId()));
        
        String description = item.getDescription();
        if ( !TextUtils.isEmpty(description) ) 
        {
            //todo words vs. chars
            description = StringUtils.stripTags(description);
            if ( description.length() > 300 ) description = description.substring(0, 300) + "..."; //$NON-NLS-1$
            descriptionView.setText(Html.fromHtml(description));
        }
        
        String author = TextUtils.isEmpty(item.getAuthor()) ? item.getOriginalAuthor() : item.getAuthor();
        if (  TextUtils.isEmpty(author) || forceFeedTitleAsAuthor ) 
        {
            author = item.getSource().getTitle();
            if ( author.length() > 20 ) author = author.substring(0, 20);
            
        }
        authorView.setText(author);
        
        String date = StringUtils.formatDate(item.getDate());
        if ( item.getDate() != null ) dateView.setText(date);
        
        bindImage(v, item);
         
    }
    
    private void bindImage(View v, Item item)
    {
        final ImageView imageView = (ImageView) v.findViewById(R.id.thumbnail);
        
        if ( item.getImage() != null )
        {
            String url = item.getImage().toString();
            
            File localFile = ImageUtils.getImageCacheFile(url, Integer.toString(Theme.DEFAULT_THUMB_SIZE));
            if ( localFile.exists() ) 
            {
                imageView.setImageURI(Uri.parse("file://" + localFile.getAbsolutePath())); //$NON-NLS-1$
                return;
            }
            else 
            {
                ImageUtils.downloadAndCacheImage(v.getContext(), url, Theme.DEFAULT_THUMB_SIZE, new DownloadImageCallbackAdapter() {
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
        else
        {
            //TODO
        }
        
        //if we reach this point url is null OR image has not been downloaded 
        imageView.setImageResource(R.drawable.default_feed_icon);
    }

}
