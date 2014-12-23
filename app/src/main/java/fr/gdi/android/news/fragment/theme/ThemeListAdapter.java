package fr.gdi.android.news.fragment.theme;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.SortableAdapter;
import fr.gdi.android.news.model.Theme;

public class ThemeListAdapter extends SortableAdapter<Theme>
{
    
    private int viewResourceId;
    
    
    public ThemeListAdapter(Context context, List<Theme> themes)
    {
        super(context, R.layout.theme_list_row, themes);
        
        sort();

        this.viewResourceId = R.layout.theme_list_row;
        
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
        
        final Theme theme = getItem(position);
        if (theme != null)
        {
            ((TextView) v.findViewById(R.id.theme_name)).setText(theme.getName());
        }
        
        return v;
    }

}
