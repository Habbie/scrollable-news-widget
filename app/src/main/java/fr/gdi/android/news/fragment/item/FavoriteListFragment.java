package fr.gdi.android.news.fragment.item;

import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.model.Item;

public class FavoriteListFragment extends ItemListFragment
{
    private static final int RELOAD_OPTION_ID = 0; 
    
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.add(Menu.NONE, RELOAD_OPTION_ID, Menu.NONE, getActivity().getText(R.string.reload_action));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch ( item.getItemId() ) 
        {
            case RELOAD_OPTION_ID:
                loadItems();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    protected void loadItems()
    {
        this.itemDao = DaoUtils.getItemDao(getActivity());
        List<Item> items = itemDao.getFavorites();
        setItems(items);
    }
    
    @Override
    protected void setFavorite(Item item)
    {
        super.setFavorite(item);
        loadItems();
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        loadItems();
    }
}
