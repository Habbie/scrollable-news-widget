package fr.gdi.android.news.fragment.item;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.SortableAdapter;
import fr.gdi.android.news.model.Item;

public class ItemListAdapter extends SortableAdapter<Item>
{
    private int viewResourceId;
    
    private ItemBinder binder;
    
    
    public ItemListAdapter(Context context, List<Item> items)
    {
        super(context, R.layout.item_layout_1, items);
       
        sort();
        
        binder = new ItemBinder();
            
        viewResourceId = R.layout.item_layout_1;
        
        setNotifyOnChange(true);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        
        final Item item = getItem(position);
        if (item != null)
        {
            binder.bind(v, item);
        }
        
        return v;
    }
    
    
    
}
