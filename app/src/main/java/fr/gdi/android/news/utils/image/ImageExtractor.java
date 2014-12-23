package fr.gdi.android.news.utils.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import fr.gdi.android.news.preference.utils.PreferenceUtils;

class ImageExtractor implements ImageGetter
{
    private String image;
    
    private Context context;
    
    public ImageExtractor(Context context)
    {
        this.context = context;
    }
    
    @Override
    public Drawable getDrawable(String source)
    {
        //issue-10 google news hack
        if ( source != null && source.startsWith("//") ) source = "http:" + source;
        
        Bitmap bmp = ImageUtils.downloadImage(source);
        
        if ( bmp == null ) return null;
        
        int w = bmp.getWidth(); 
        //int h = bmp.getHeight();
        
        if (this.image == null && w >= PreferenceUtils.getEmbeddedImageMinWidth(context))
        {
            //only consider the first image that matches the size spec.
            this.image = source;
        }
        
        return null;
    }
    
    public String getImage()
    {
        return image;
    }
}
