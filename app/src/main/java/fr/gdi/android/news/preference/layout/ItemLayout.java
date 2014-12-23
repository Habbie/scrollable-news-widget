package fr.gdi.android.news.preference.layout;

import fr.gdi.android.news.R;

public enum ItemLayout
{
    LAYOUT_1(R.drawable.layout_1, R.layout.item_layout_1, true, true),
    LAYOUT_2(R.drawable.layout_2, R.layout.item_layout_2, true, true),
    LAYOUT_3(R.drawable.layout_3, R.layout.item_layout_3, true, false),
    LAYOUT_4(R.drawable.layout_4, R.layout.item_layout_4, true, false),
    LAYOUT_5(R.drawable.layout_5, R.layout.item_layout_5, false, false);
    
    private int resId, layoutId;
    private boolean showThumbnail, cropAuthor;
    private ItemLayout(int res, int layoutId, boolean showThumbnail, boolean cropAuthor)
    {
        this.resId = res;
        this.layoutId = layoutId;
        this.showThumbnail = showThumbnail;
        this.cropAuthor = cropAuthor;
    }
    
    public int getDrawableId()
    {
        return resId;
    }
    
    public int getLayoutId()
    {
        return layoutId;
    }
    
    public boolean isShowThumbnail()
    {
        return showThumbnail;
    }
    
    public boolean isCropAuthor()
    {
        return cropAuthor;
    }
}
