package fr.gdi.android.news.receiver;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class NewsWidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new ItemViewsFactory(this.getApplicationContext(), intent);
    }
}