package fr.gdi.android.news.fragment.item;

import fr.gdi.android.news.model.Item;

public interface IItemLoadListener
{
    void itemLoaded(Item item);
    void startLoading();
}
