package fr.gdi.android.news.fragment.behaviour;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.SortableAdapter;
import fr.gdi.android.news.model.Behaviour;

public class BehaviourListAdapter extends SortableAdapter<Behaviour>
{
    
    private int viewResourceId;
    
    
    public BehaviourListAdapter(Context context, List<Behaviour> behaviours)
    {
        super(context, R.layout.behaviour_list_row, behaviours);
        
        sort();

        this.viewResourceId = R.layout.behaviour_list_row;
        
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
        
        final Behaviour behaviour = getItem(position);
        if (behaviour != null)
        {
            ((TextView) v.findViewById(R.id.behaviour_name)).setText(behaviour.getName());
        }
        
        return v;
    }

}
