package fr.gdi.android.news.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import android.text.Html;
import android.text.TextUtils;

public class StringUtils
{
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd MMM HH:mm"); //$NON-NLS-1$
    private static final Pattern p = Pattern.compile("http(s)?://(.)+"); //$NON-NLS-1$

    static 
    {
        DEFAULT_DATE_FORMAT.setLenient(true);
    }
    
    public static String stripTags(String text)
    {
        return text.replaceAll("<[^>]+>|<[^>]+/>", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public static String removeStart(String str, String remove)
    {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(remove))
        {
            return str;
        }
        if (str.startsWith(remove))
        {
            return str.substring(remove.length());
        }
        return str;
        
    }
    
    public static String formatDate(Date d, String dateFormat)
    {
        if ( d == null ) return "--"; //$NON-NLS-1$
        try
        {
            SimpleDateFormat format = null;
            if ( !TextUtils.isEmpty(dateFormat) ) 
            {
                format = new SimpleDateFormat(dateFormat);
                format.setLenient(true);
            }
            return formatDate(d, format);
        }
        catch ( Exception e ) 
        {
            return formatDate(d);
        }
    }
    
    public static String formatDate(Date d)
    {
        return formatDate(d, DEFAULT_DATE_FORMAT);
    }
    
    private static String formatDate(Date d, SimpleDateFormat format)
    {
        if ( format == null ) format = DEFAULT_DATE_FORMAT;
        
        return format.format(d);
    }
    
    public static String removeEnd(String str, String remove) 
    {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) 
        {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }
    
    public static String addHttpProtocolIfMissing(String s)
    {
        if ( !hasProtocol(s) ) return "http://" + s; //$NON-NLS-1$
        else return s;
    }
    
    public static boolean hasProtocol(String href)
    {
        return p.matcher(href).matches();
    }

    public static String cropString(String value, int maxWords)
    {
        value = stripTags(value); //remove all html tags
        value = Html.fromHtml(value).toString(); //decode any HTML entities left
        
        String[] parts = TextUtils.split(value, " "); //split words //$NON-NLS-1$
    
        if (maxWords < parts.length) //croppy, croppy
        {
            StringBuilder descriptionBuilder = new StringBuilder();
            for (int i = 0; i < maxWords; i++)
            {
                if (i > 0) descriptionBuilder.append(" "); //$NON-NLS-1$
                descriptionBuilder.append(parts[i]);
            }
                
            value = descriptionBuilder.toString().trim() + " ... "; //$NON-NLS-1$
        }
    
        return value;
    }
    
}
