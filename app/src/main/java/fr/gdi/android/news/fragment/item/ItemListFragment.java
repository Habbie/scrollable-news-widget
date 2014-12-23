
package fr.gdi.android.news.fragment.item;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import fr.gdi.android.news.R;
import fr.gdi.android.news.activity.ItemDetailsActivity;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.ItemDao;
import fr.gdi.android.news.model.Item;

public class ItemListFragment extends ListFragment
{
    public static final String FEED_ID_KEY = "feed_id"; //$NON-NLS-1$
    
    private static final String LAST_POSITION_KEY = "lastPosition"; //$NON-NLS-1$
    private static final int READ = 0, MARK_READ = 1, SET_FAVORITE = 2;
    
    private ItemListAdapter adapter;
    
    private int selectedPosition = -1;
    
    protected ItemDao itemDao; 
    
    private long feedId = -1;
    
    public void setFeedId(long feedId) 
    {
        if ( itemDao != null ) 
        {
            List<Item> items = itemDao.getItems(20, feedId);
            setItems(items);
        }
        else 
        {
            this.feedId = feedId;
        }
    }
    
    protected void setItems(List<Item> items)
    {
        if ( adapter != null ) 
        {
            adapter.replace(items);
            adapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) 
    {
        Item selected = (Item) getListAdapter().getItem(position);
        
        read(selected);
    }

    private void read(Item selected)
    {
        if ( selected != null ) { 
            Intent intent = new Intent(getActivity(), ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsFragment.INITIAL_ITEM_ID_KEY, selected.getId());
            intent.putExtra(ItemDetailsFragment.ID_LIST_KEY, this.getItemIds());
            getActivity().startActivity(intent);
        }
    }
    
    private long[] getItemIds()
    {
        int count = adapter.getCount();
        long[] ids = new long[count];
        
        for (int i = 0; i < count; i++)
        {
            Item item = adapter.getItem(i);
            ids[i] = item.getId();
        }
        
        return ids;
    }
    
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.itemDao = DaoUtils.getItemDao(getActivity());
        loadItems();
    }
    
    protected void loadItems()
    {
        if ( feedId != -1 ) 
        {
            List<Item> items = itemDao.getItems(20, true, false, feedId);
            setItems(items);
        }
    }
    
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) 
    {
         return inflater.inflate(R.layout.simple_list, null);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        ListView listView = getListView();

        setHasOptionsMenu(true); 
        
        if ( adapter == null ) 
        {
            List<Item> items = new ArrayList<Item>(); //itemDao.getItems(feed..., limit) 
            adapter = new ItemListAdapter(getActivity(), items);
        }
        setListAdapter(adapter);
        
        listView.setCacheColorHint(0);
        
        registerForContextMenu(listView);
        
        if (savedInstanceState != null)
        {
            selectedPosition = savedInstanceState.getInt(LAST_POSITION_KEY, 0);
            if (selectedPosition != -1)
            {
                setSelection(selectedPosition);
                getListView().smoothScrollToPosition(selectedPosition);
            }
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
      if (v.getId()== getListView().getId()) 
      {
          AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
          Item selected = (Item) getListAdapter().getItem(info.position); 
          String title = selected.getTitle(); 
          menu.setHeaderTitle(title); 
          int[] menuItems = new int[] { 
                  R.string.item_read_story, 
                  selected.isRead() ? R.string.item_mark_unread : R.string.item_mark_read, 
                  selected.isFavorite() ? R.string.item_remove_from_favorites : R.string.item_add_to_favorites 
          }; //magic strings
          for (int i = 0; i<menuItems.length; i++) 
          {
              menu.add(Menu.NONE, i, i, menuItems[i]);
          }
      }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Item selected = (Item) getListAdapter().getItem(info.position); 
        
        switch ( item.getItemId() )
        {
            case READ: 
                read(selected);
                break;
            case MARK_READ:
                DaoUtils.getItemDao(getActivity()).markRead(selected);
                break;
            case SET_FAVORITE:
                setFavorite(selected);
                break;
          
        }
        return super.onContextItemSelected(item);
    }
    
    protected void setFavorite(Item item)
    {
        DaoUtils.getItemDao(getActivity()).markFavorite(item);
    }

    public void feedDetailsCanceled() 
    {
        
    }
    
    @Override
    public void onDestroyView()
    {
        setListAdapter(null);
        super.onDestroyView();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        selectedPosition = getListView().getFirstVisiblePosition();
        outState.putInt(LAST_POSITION_KEY, selectedPosition);
    }
}
