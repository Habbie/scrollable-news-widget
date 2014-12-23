package fr.gdi.android.news;

import android.net.Uri;

public class Constants
{
    public static final String PACKAGE = "fr.gdi.android.news"; //$NON-NLS-1$

    public static final String AUTHORITY = Constants.PACKAGE + ".data"; //$NON-NLS-1$
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY); //$NON-NLS-1$
    public static final Uri CONTENT_URI_NEWS = CONTENT_URI.buildUpon().appendEncodedPath("news").build(); //$NON-NLS-1$

    public static final String MALFORMED_ITEM_LINK_URL = "file:///android_asset/html/malformed_item.html"; //$NON-NLS-1$
}
