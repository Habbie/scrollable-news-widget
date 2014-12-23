package fr.gdi.android.news.model;

import java.io.IOException;
import java.io.Serializable;

import fr.gdi.android.news.R;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonWriter;

@SuppressWarnings("serial") //$NON-NLS-1$
public class Theme implements Serializable, Comparable<Theme>
{
    private static final String DEFAULT_NAME = "New behaviour"; //$NON-NLS-1$
    
    public static final int DEFAULT_THUMB_SIZE = 120;

    private long id;
    
    private String name = "Default theme"; //$NON-NLS-1$
    
    private boolean roundedCorners = true;
    private int backgroundColor = Color.BLACK;
    private int backgroundOpacity = 70;
    
    
    private int storyTitleColor = Color.WHITE;
    private int storyTitleFontSize = 14;
    private boolean storyTitleUppercase = false;
    private int storyTitleMaxLines = 2;
    private boolean storyTitleHide = false;
    
    private int storyDescriptionColor = Color.WHITE;
    private int storyDescriptionFontSize = 12;
    private int storyDescriptionMaxWordCount = 25;
    
    private boolean showFooter = true;
    private boolean footerUppercase = false;
    private int footerFontSize = 10;
    private int storyAuthorColor = Color.WHITE;
    private int storyDateColor = Color.WHITE;
    
    private int thumbnailSize = DEFAULT_THUMB_SIZE; 
    
    private boolean showWidgetTitle = true;
    private int widgetTitleColor = Color.WHITE;
    
    private int layout = 0;
    
    private int numColumns = 1;
    
    private String dateFormat = "dd MMM, HH:mm"; //$NON-NLS-1$
    
    public Theme(Context context)
    {
        this.name = getDefaultName(context);
    }
    
    public int getStoryTitleFontSize()
    {
        return storyTitleFontSize;
    }

    public void setStoryTitleFontSize(int storyTitleFontSize)
    {
        this.storyTitleFontSize = storyTitleFontSize;
    }

    public boolean isStoryTitleUppercase()
    {
        return storyTitleUppercase;
    }

    public void setStoryTitleUppercase(boolean storyTitleUppercase)
    {
        this.storyTitleUppercase = storyTitleUppercase;
    }

    public int getStoryDescriptionFontSize()
    {
        return storyDescriptionFontSize;
    }

    public void setStoryDescriptionFontSize(int storyDescriptionFontSize)
    {
        this.storyDescriptionFontSize = storyDescriptionFontSize;
    }

    public boolean isFooterUppercase()
    {
        return footerUppercase;
    }

    public void setFooterUppercase(boolean footerUppercase)
    {
        this.footerUppercase = footerUppercase;
    }

    public int getFooterFontSize()
    {
        return footerFontSize;
    }

    public void setFooterFontSize(int footerFontSize)
    {
        this.footerFontSize = footerFontSize;
    }

    public int getWidgetTitleColor()
    {
        return widgetTitleColor;
    }

    public void setWidgetTitleColor(int widgetTitleColor)
    {
        this.widgetTitleColor = widgetTitleColor;
    }

    public int getLayout()
    {
        return layout;
    }

    public void setLayout(int layout)
    {
        this.layout = layout;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isRoundedCorners()
    {
        return roundedCorners;
    }

    public void setRoundedCorners(boolean roundedCorners)
    {
        this.roundedCorners = roundedCorners;
    }

    public int getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    public int getBackgroundOpacity()
    {
        if ( backgroundOpacity < 0 ) return 0;
        if ( backgroundOpacity > 255 ) return 255;
        return backgroundOpacity;
    }

    public void setBackgroundOpacity(int alpha)
    {
        this.backgroundOpacity = alpha;
    }

    public int getStoryTitleColor()
    {
        return storyTitleColor;
    }

    public void setStoryTitleColor(int storyTitleColor)
    {
        this.storyTitleColor = storyTitleColor;
    }

    public int getStoryDescriptionColor()
    {
        return storyDescriptionColor;
    }

    public void setStoryDescriptionColor(int storyDescriptionColor)
    {
        this.storyDescriptionColor = storyDescriptionColor;
    }

    public int getStoryDescriptionMaxWordCount()
    {
        return storyDescriptionMaxWordCount;
    }

    public void setStoryDescriptionMaxWordCount(int storyDescriptionMaxWordCount)
    {
        this.storyDescriptionMaxWordCount = storyDescriptionMaxWordCount;
    }

    public int getStoryAuthorColor()
    {
        return storyAuthorColor;
    }

    public void setStoryAuthorColor(int storyAuthorColor)
    {
        this.storyAuthorColor = storyAuthorColor;
    }

    public int getStoryDateColor()
    {
        return storyDateColor;
    }

    public void setStoryDateColor(int storyDateColor)
    {
        this.storyDateColor = storyDateColor;
    }

    public boolean isShowWidgetTitle()
    {
        return showWidgetTitle;
    }

    public void setShowWidgetTitle(boolean showWidgetTitle)
    {
        this.showWidgetTitle = showWidgetTitle;
    }

    public boolean isShowFooter()
    {
        return showFooter;
    }

    public void setShowFooter(boolean showFooter)
    {
        this.showFooter = showFooter;
    }

    public int getThumbnailSize()
    {
        return thumbnailSize;
    }

    public void setThumbnailSize(int thumbnailSize)
    {
        this.thumbnailSize = thumbnailSize;
    }
    
    public int getStoryTitleMaxLines()
    {
        return storyTitleMaxLines;
    }

    public void setStoryTitleMaxLines(int storyMaxLines)
    {
        this.storyTitleMaxLines = storyMaxLines;
    }
    
    public boolean isStoryTitleHide()
    {
        return storyTitleHide;
    }

    public void setStoryTitleHide(boolean storyTitleHide)
    {
        this.storyTitleHide = storyTitleHide;
    }
    
    public void setDateFormat(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }
    
    public String getDateFormat()
    {
        return dateFormat;
    }
    
    public void setNumColumns(int numColumns)
    {
        this.numColumns = numColumns;
    }
    public int getNumColumns()
    {
        return numColumns < 1 ? 1 : (numColumns > 3 ? 3 : numColumns);
    }
    
    @Override
    public int compareTo(Theme another)
    {
        if ( another == null ) 
        {
            return -1;
        }
        
        if ( this.getName() == null ) 
        {
            return 1;
        }
        
        return this.getName().compareToIgnoreCase(another.getName());
    }
    
    @Override
    public boolean equals(Object o)
    {
        return (o instanceof Theme) && ((Theme) o).getId() == this.getId();
    }
    
    
    //TODO replace all this scary json stuff with a generic reflection-based serializer
    public void toJson(JsonWriter writer) throws IOException
    {
        writer.beginObject()
            .name("name").value(name) //$NON-NLS-1$
            .name("roundedCorners").value(roundedCorners) //$NON-NLS-1$
            .name("backgroundColor").value(backgroundColor) //$NON-NLS-1$
            .name("backgroundOpacity").value(backgroundOpacity) //$NON-NLS-1$
            .name("storyTitleColor").value(storyTitleColor) //$NON-NLS-1$
            .name("storyTitleFontSize").value(storyTitleFontSize) //$NON-NLS-1$
            .name("storyTitleUppercase").value(storyTitleUppercase) //$NON-NLS-1$
            .name("storyTitleMaxLines").value(storyTitleMaxLines) //$NON-NLS-1$
            .name("storyTitleHide").value(storyTitleHide) //$NON-NLS-1$
            .name("storyDescriptionColor").value(storyDescriptionColor) //$NON-NLS-1$
            .name("storyDescriptionFontSize").value(storyDescriptionFontSize) //$NON-NLS-1$
            .name("storyDescriptionMaxWordCount").value(storyDescriptionMaxWordCount) //$NON-NLS-1$
            .name("showFooter").value(showFooter) //$NON-NLS-1$
            .name("footerUppercase").value(footerUppercase) //$NON-NLS-1$
            .name("footerFontSize").value(footerFontSize) //$NON-NLS-1$
            .name("storyAuthorColor").value(storyAuthorColor) //$NON-NLS-1$
            .name("storyDateColor").value(storyDateColor) //$NON-NLS-1$
            .name("thumbnailSize").value(thumbnailSize) //$NON-NLS-1$
            .name("showWidgetTitle").value(showWidgetTitle) //$NON-NLS-1$
            .name("widgetTitleColor").value(widgetTitleColor) //$NON-NLS-1$
            .name("layout").value(layout) //$NON-NLS-1$
            .name("dateFormat").value(dateFormat) //$NON-NLS-1$
            .name("numColumns").value(numColumns) //$NON-NLS-1$
        .endObject();
    }
    
    
    public void fromJson(JsonReader reader) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext()) 
        {
            String name = reader.nextName();
            
            if (TextUtils.equals(name, "name")) //$NON-NLS-1$
            {
                this.setName(reader.nextString());
            }
            else if (TextUtils.equals(name, "roundedCorners")) //$NON-NLS-1$
            {
                this.setRoundedCorners(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "backgroundColor")) //$NON-NLS-1$
            {
                this.setBackgroundColor(reader.nextInt());
            }
            else if (TextUtils.equals(name, "backgroundOpacity")) //$NON-NLS-1$
            {
                this.setBackgroundOpacity(reader.nextInt());
            }
            else if (TextUtils.equals(name, "storyTitleColor")) //$NON-NLS-1$
            {
                this.setStoryTitleColor(reader.nextInt());
            }
            else if (TextUtils.equals(name, "storyTitleFontSize")) //$NON-NLS-1$
            { 
                this.setStoryTitleFontSize(reader.nextInt());
            }
            else if (TextUtils.equals(name, "storyTitleUppercase")) //$NON-NLS-1$
            {
                this.setStoryTitleUppercase(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "storyTitleMaxLines")) //$NON-NLS-1$
            {
                this.setStoryTitleMaxLines(reader.nextInt());
            }
            else if (TextUtils.equals(name, "storyTitleHide")) //$NON-NLS-1$
            {
                this.setStoryTitleHide(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "storyDescriptionColor")) //$NON-NLS-1$
            {
                this.setStoryDescriptionColor(reader.nextInt());
            }
            else if (TextUtils.equals(name, "storyDescriptionFontSize")) //$NON-NLS-1$
            {
                this.setStoryDescriptionFontSize(reader.nextInt());
            }
            else if (TextUtils.equals(name, "storyDescriptionMaxWordCount")) //$NON-NLS-1$
            {
                this.setStoryDescriptionMaxWordCount(reader.nextInt());
            }
            else if (TextUtils.equals(name, "showFooter")) //$NON-NLS-1$
            {
                this.setShowFooter(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "footerUppercase")) //$NON-NLS-1$
            {
                this.setFooterUppercase(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "footerFontSize")) //$NON-NLS-1$
            {
                this.setFooterFontSize(reader.nextInt());
            }
            else if (TextUtils.equals(name, "storyAuthorColor")) //$NON-NLS-1$
            {
                this.setStoryAuthorColor(reader.nextInt());
            }
            else if (TextUtils.equals(name, "storyDateColor")) //$NON-NLS-1$
            {
                this.setStoryDateColor(reader.nextInt());
            }
            else if (TextUtils.equals(name, "thumbnailSize")) //$NON-NLS-1$
            {
                this.setThumbnailSize(reader.nextInt());
            }
            else if (TextUtils.equals(name, "showWidgetTitle")) //$NON-NLS-1$
            {
                this.setShowWidgetTitle(reader.nextBoolean());
            }
            else if (TextUtils.equals(name, "widgetTitleColor")) //$NON-NLS-1$
            {
                this.setWidgetTitleColor(reader.nextInt());
            }
            else if (TextUtils.equals(name, "layout")) //$NON-NLS-1$
            {
                this.setLayout(reader.nextInt());
            }
            else if (TextUtils.equals(name, "dateFormat")) //$NON-NLS-1$
            {
                this.setDateFormat(reader.nextString());
            }
            else if (TextUtils.equals(name, "numColumns")) //$NON-NLS-1$
            {
                this.setNumColumns(reader.nextInt());
            }
            else
            {
                reader.skipValue();
            }
        }
        reader.endObject();
    }
    
    private String getDefaultName(Context context)
    {
        return context == null ? DEFAULT_NAME : context.getText(R.string.defaults_theme_name).toString();
    }
}
