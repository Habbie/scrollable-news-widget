package fr.gdi.android.news.receiver;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.ConfigurationDao;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.ItemDao;
import fr.gdi.android.news.fragment.item.ItemDetailsFragment;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.model.Configuration;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.model.Theme;
import fr.gdi.android.news.preference.layout.ItemLayout;
import fr.gdi.android.news.utils.StringUtils;
import fr.gdi.android.news.utils.image.ImageUtils;

public class ItemViewsFactory implements RemoteViewsFactory
{
    
    private static final String FILE_PROTOCOL = "file://"; //$NON-NLS-1$

    private int appWidgetId;

    private List<Item> items = new ArrayList<Item>();
    private Context context;
    
    private ItemDao itemDao;
    private ConfigurationDao configurationDao;
    
    private Configuration configuration;
    
    public ItemViewsFactory(Context context, Intent intent)
    {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        
        itemDao = DaoUtils.getItemDao(context);
        configurationDao = DaoUtils.getConfigurationDao(context);
    }
    
    //special constructor used to previsualize a theme 
    public ItemViewsFactory(Context context, Configuration configuration, List<Item> items)
    {
        this.context = context;
        this.configuration = configuration;
        this.items = items;
    }
    
    public void onCreate()
    {
        
    }
    
    @Override
    public void onDestroy()
    {
        items.clear();
    }
    
    
    @Override
    public boolean hasStableIds()
    {
        return true;
    }
    
    @Override
    public int getCount()
    {
        return items.size();
    }
    
    @Override
    public int getViewTypeCount()
    {
        //VERY important. unless we correctly position
        //the candidate views total count, switching 
        //layout dynamically causes the launcher to crash
        return ItemLayout.values().length;
    }
    
    @Override
    public long getItemId(int position)
    {
        //OOBE may happen ?
        return items.get(position).getId();
    }
    
    @Override
    public void onDataSetChanged()
    {
        configuration = configurationDao.getConfigurationByWidgetId(appWidgetId);
        
        if ( configuration == null ) 
        {
            Log.e(Constants.PACKAGE, "A non recoverable error occured while updating data: configuration is null [should not happen]. Ignoring request."); //$NON-NLS-1$
            return;
        }
        
        List<Feed> feeds = configuration.getFeeds();
        
        long[] feedIds = new long[feeds.size()];
        
        for (int u = 0; u < feeds.size(); u++)
        {
            Feed feed = feeds.get(u);
            feedIds[u] = feed.getId();
        }
        
        Behaviour behaviour = configuration.getBehaviour();
        boolean unreadOnly = behaviour.isHideReadStories();
        int maxNews = behaviour.getMaxNumberOfNews();
        boolean distribute = behaviour.shouldDistributeEvenly();
        items = itemDao.getItems(maxNews <= 0 ? Behaviour.DEFAULT_NEWS_NUMBER : maxNews, unreadOnly, distribute, feedIds);
    }
    
    @Override
    public RemoteViews getLoadingView()
    {
        //return null;
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.loading_view);
        if ( configuration != null && configuration.getTheme() != null ) 
        {
            rv.setInt(R.id.empty_view, "setTextColor", configuration.getTheme().getStoryDescriptionColor()); //$NON-NLS-1$
        }
        return rv;
    }
    
    public RemoteViews getInvalidView()
    {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.loading_view);
        rv.setInt(R.id.empty_view, "setTextColor", configuration.getTheme().getStoryDescriptionColor()); //$NON-NLS-1$
        rv.setTextViewText(R.id.empty_view, "..."); //$NON-NLS-1$
        return rv;
    }
    
    public RemoteViews getViewAt(int position)
    {
        Theme theme = configuration.getTheme();
        
        if ( position < 0 || position > items.size() - 1 )
        {
            return getInvalidView();
        }
        
        Item item = items.get(position);

        int layoutId = ItemLayout.values()[theme.getLayout()].getLayoutId();
        RemoteViews rv = new RemoteViews(context.getPackageName(), layoutId);
        
        rv.setTextViewText(R.id.item_link, item.getLinkURL());
        rv.setTextViewText(R.id.id, Long.toString(item.getId()));
        
        setupStoryTitle(theme, item, rv);
        setupStoryDescription(theme, item, rv);
        setupFooter(theme, item, rv);
        
        bindImage(rv, theme, item);
        
        /* create fill-in intent */
        Bundle extras = new Bundle();
        extras.putLong(ItemDetailsFragment.INITIAL_ITEM_ID_KEY, item.getId());
        extras.putLongArray(ItemDetailsFragment.ID_LIST_KEY, getItemIds());
        extras.putString(ItemDetailsFragment.INITIAL_URL_KEY, item.getLinkURL());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.item_list_row, fillInIntent);

        return rv;
    }

    private void setupFooter(Theme theme, Item item, RemoteViews rv)
    {
        if ( !theme.isShowFooter() ) 
        {
            rv.setViewVisibility(R.id.item_author, View.GONE);
            rv.setViewVisibility(R.id.item_date, View.GONE);
            return;
        }
        
        rv.setViewVisibility(R.id.item_author, View.VISIBLE);
        rv.setViewVisibility(R.id.item_date, View.VISIBLE);
        
        boolean forceFeedTitleAsAuthor = configuration.getBehaviour().isForceFeedAsAuthor();
        boolean uppercase = theme.isFooterUppercase();
        int fontSize = theme.getFooterFontSize();
        int authorColor = theme.getStoryAuthorColor();
        int dateColor = theme.getStoryDateColor();
        
        String author = TextUtils.isEmpty(item.getAuthor()) ? item.getOriginalAuthor() : item.getAuthor();
        if (  TextUtils.isEmpty(author) || forceFeedTitleAsAuthor ) 
        {
            author = item.getSource().getTitle();
            if ( author.length() > 20 && ItemLayout.values()[theme.getLayout()].isCropAuthor() ) 
            {
                author = author.substring(0, 19) + ".."; //$NON-NLS-1$
            }
        }
        
        author = uppercase ? author.toUpperCase() : author;
        rv.setTextViewText(R.id.item_author, author);
        rv.setInt(R.id.item_author, "setTextColor", authorColor); //$NON-NLS-1$
        rv.setFloat(R.id.item_author, "setTextSize", fontSize); //$NON-NLS-1$
        
        String date = StringUtils.formatDate(item.getDate(), theme.getDateFormat());
        date = uppercase ? date.toUpperCase() : date;
        if ( item.getDate() != null ) 
        {
            rv.setTextViewText(R.id.item_date, date);
            rv.setInt(R.id.item_date, "setTextColor", dateColor); //$NON-NLS-1$
            rv.setFloat(R.id.item_date, "setTextSize", fontSize); //$NON-NLS-1$
        }
    }

    private void setupStoryDescription(Theme theme, Item item, RemoteViews rv)
    {
        int maxWords = theme.getStoryDescriptionMaxWordCount();
        String description = item.getDescription();

        if ( maxWords == 0 || TextUtils.isEmpty(description) )
        {
            rv.setViewVisibility(R.id.item_description, View.GONE);
            return;
        }
        rv.setViewVisibility(R.id.item_description, View.VISIBLE);
        
        description = StringUtils.cropString(description, maxWords);
        rv.setTextViewText(R.id.item_description, description);
        
        int storyDescriptionColor = theme.getStoryDescriptionColor();
        rv.setInt(R.id.item_description, "setTextColor", storyDescriptionColor); //$NON-NLS-1$

        int storyDescriptionFontSize = theme.getStoryDescriptionFontSize();
        storyDescriptionFontSize = storyDescriptionFontSize == 0 ? 12 : storyDescriptionFontSize;
        rv.setFloat(R.id.item_description, "setTextSize", storyDescriptionFontSize); //$NON-NLS-1$
        
    }

    private void setupStoryTitle(Theme theme, Item item, RemoteViews rv)
    {
        if ( theme.isStoryTitleHide() ) 
        {
            rv.setViewVisibility(R.id.item_title, View.GONE);
            return;
        }
        
        String title = item.getTitle();
        if ( theme.isStoryTitleUppercase() ) title = title.toUpperCase();
        if ( !item.isRead() ) 
        {
            SpannableString span = new SpannableString(title);
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0); 
            rv.setTextViewText(R.id.item_title, span);
        }
        else 
        {
            rv.setTextViewText(R.id.item_title, title);
        }

        int storyTitleMaxLines = theme.getStoryTitleMaxLines();
        storyTitleMaxLines = storyTitleMaxLines == 0 ? 2 : storyTitleMaxLines;
        rv.setInt(R.id.item_title, "setMaxLines",  storyTitleMaxLines); //$NON-NLS-1$

        int storyTitleColor = theme.getStoryTitleColor();
        Integer feedColor = item.getSource().getColor();
        if ( feedColor != null && feedColor != 0 ) storyTitleColor = feedColor;
        rv.setInt(R.id.item_title, "setTextColor", storyTitleColor); //$NON-NLS-1$

        int storyTitleFontSize = theme.getStoryTitleFontSize();
        storyTitleFontSize = storyTitleFontSize == 0 ? 14 : storyTitleFontSize;
        rv.setFloat(R.id.item_title, "setTextSize", storyTitleFontSize); //$NON-NLS-1$
    }
    
    private long[] getItemIds()
    {
        int count = getCount();
        long[] ids = new long[count];
        
        for (int i = 0; i < count; i++)
        {
            Item item = items.get(i);
            ids[i] = item.getId();
        }
        
        return ids;
    }

    //bindImage is cflow getViewAt. Even though the documentation says it is ok to perform 
    //long-running operations in getViewAt, this doesn't feel right. However trying to 
    //improve things up with async image loader and defered image binding doesn't seem 
    //to work correctly (image not set on first load)
    //This method seems WAY too complex (4)...
    private void bindImage(final RemoteViews rv, Theme theme, final Item item)
    {
        boolean hideImage = !ItemLayout.values()[theme.getLayout()].isShowThumbnail();
        
        if ( hideImage ) 
        {
            rv.setViewVisibility(R.id.thumbnail, View.GONE);
            return;
        }
       
        rv.setViewVisibility(R.id.thumbnail, View.VISIBLE);            

        final int thumbSize = theme.getThumbnailSize() <= 0 ? Theme.DEFAULT_THUMB_SIZE : theme.getThumbnailSize();
        
        if ( item.getImage() != null )
        {
            String url = item.getImage().toString();
            
            if ( !TextUtils.equals(ImageUtils.DEFAULT_IMAGE_URI, url) )
            {
                File localFile = ImageUtils.getImageCacheFile(url, Integer.toString(thumbSize));
                if ( localFile.exists() ) 
                {
                    //thumbnail has already been downloaded. just set it
                    rv.setImageViewUri(R.id.thumbnail, Uri.parse(FILE_PROTOCOL + localFile.getAbsolutePath()));
                    return;
                }
                else 
                {
                    //poor-man solution
                    boolean imageDownloaded = setViewImage(rv, url, thumbSize);
                    if ( imageDownloaded ) 
                    {
                        //all went well this time
                        return;
                    }
                    else 
                    {
                        //specified image is not valid. Prevent later downloads
                        itemDao.setDefaultImage(item);
                    }
                }
            }
        }
        else if ( configuration.getBehaviour().isLookupImageInBody() )
        {
            //try to get image from body -- should only happen once
            String image = ImageUtils.extractFromText(context, item.getDescription());
            if ( !TextUtils.isEmpty(image) ) 
            {
                try 
                {
                    boolean imageDownloaded = setViewImage(rv, image, thumbSize);
                    if ( imageDownloaded ) 
                    {
                        //referenced image is correct. Save that reference
                        item.setImage(new URL(image));
                        itemDao.updateImage(item);
                        return;
                    }
                    else
                    {
                        itemDao.setDefaultImage(item);
                    }
                }
                catch ( Exception e )
                {
                    Log.w(Constants.PACKAGE, "Couldnot persist item while setting extracted image reference. image ref=" + image, e); //$NON-NLS-1$
                }
            }   
            else
            {
                //no image found in body. Prevent later lookup 
                itemDao.setDefaultImage(item);
            }
        }
        
        //set default image
        rv.setImageViewUri(R.id.thumbnail, Uri.parse(FILE_PROTOCOL + ImageUtils.getDefaultIconSized(context, thumbSize)));
    }

    private boolean setViewImage(final RemoteViews rv, String url, int thumbSize)
    {
        if ( url != null )
        {
            ImageUtils.downloadAndCacheImage(context, url, thumbSize);
            File localFile = ImageUtils.getImageCacheFile(url, Integer.toString(thumbSize));
            if ( localFile.exists() ) 
            {
                rv.setImageViewUri(R.id.thumbnail, Uri.parse(FILE_PROTOCOL + localFile.getAbsolutePath()));
                return true;
            }
        }
        
        return false;
    }
    
}
