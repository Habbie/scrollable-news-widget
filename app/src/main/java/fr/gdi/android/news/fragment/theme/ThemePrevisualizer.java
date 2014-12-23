package fr.gdi.android.news.fragment.theme;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.model.Configuration;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.model.Theme;
import fr.gdi.android.news.receiver.ItemViewsFactory;
import fr.gdi.android.news.utils.IOUtils;
import fr.gdi.android.news.utils.image.ImageUtils;

public class ThemePrevisualizer
{

    private Context context;
    
    public ThemePrevisualizer(Context context)
    {
        this.context = context;
    }
    
    //remaining issue: dialog height!
    public void showThemePreview(Theme theme)
    {
        URL url = null;
        
        try
        {
            url = new URL("http://www.lipsum.com"); //$NON-NLS-1$
        }
        catch (MalformedURLException e)
        {

        }
        
        Configuration configuration = new Configuration(context);
        configuration.setTheme(theme);
        configuration.setBehaviour(new Behaviour(context));
        Feed feed = new Feed();
        feed.setURL(url);
        feed.setTitle("Lorem Ipsum");  //$NON-NLS-1$
        Item item = new Item(
                -1, url, null, "Lorem Ipsum",  //$NON-NLS-1$
                IOUtils.loadTextAsset(context, "text/lipsum.txt"),  //$NON-NLS-1$
                IOUtils.loadTextAsset(context, "text/lipsum.txt"),  //$NON-NLS-1$
                null, new Date(), false, false, null);
        item.setAuthor("L.I."); //$NON-NLS-1$
        item.setSource(feed);
        List<Item> items = new ArrayList<Item>();
        items.add(item);
        
        ItemViewsFactory viewsFactory = new ItemViewsFactory(context, configuration, items);
        RemoteViews rv = viewsFactory.getViewAt(0);
        
        View view = rv.apply(context, null);
        int bg = theme.getBackgroundColor();
        int alpha = theme.getBackgroundOpacity();
        int argb = ImageUtils.getColor(bg, alpha); 
        view.setBackgroundColor(argb);
        
        new AlertDialog.Builder(context).setView(view).show();        
        
    }

    
}
