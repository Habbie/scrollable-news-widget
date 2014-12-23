package fr.gdi.android.news.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

public abstract class SortableAdapter<T extends Comparable<T>> extends ArrayAdapter<T> 
{

    public SortableAdapter(Context context, int textViewResourceId, List<T> objects)
    {
        super(context, textViewResourceId, objects);
    }
    
    @Override
    public void add(T object)
    {
        super.add(object);
        sort();
    }
    
    @Override
    public void addAll(Collection<? extends T> collection)
    {
        super.addAll(collection);
        sort();
    }
    
    @Override
    public void addAll(T... Ts)
    {
        addAll(Arrays.asList(Ts));
    }
    
    @Override
    public void remove(T object)
    {
        super.remove(object);
    }
    
    public void replace(List<T> items)
    {
        setNotifyOnChange(false);
        super.clear();
        super.addAll(items);
        notifyDataSetChanged();
    }
    
    protected void sort()
    {
        super.sort(new Comparator<T>() {
            @Override
            public int compare(T lhs, T rhs)
            {
                return lhs.compareTo(rhs);
            }
        });
    }
    
}
