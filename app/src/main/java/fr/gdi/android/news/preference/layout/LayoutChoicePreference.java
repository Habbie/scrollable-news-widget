package fr.gdi.android.news.preference.layout;


import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;

//TODO: drop ItemLayout enum and use Styleable thingie instead
public class LayoutChoicePreference extends ListPreference
{   
    private LayoutChoicePreferenceAdapter layoutChoicePreferenceAdapter = null;
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<RadioButton> rButtonList;

    private int value;
    
    public LayoutChoicePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public LayoutChoicePreference(Context context)
    {
        super(context);
        init(context);
    }
    
    private void init(Context context) 
    {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        rButtonList = new ArrayList<RadioButton>();
    }
    
    @Override
    protected void onPrepareDialogBuilder(Builder builder)
    {
        builder.setPositiveButton(null, null);
        
        
        layoutChoicePreferenceAdapter = new LayoutChoicePreferenceAdapter(mContext);

        builder.setAdapter(layoutChoicePreferenceAdapter, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Log.d(Constants.PACKAGE, "LayoutChoicePreference click: " + which); //$NON-NLS-1$
            }
        });
    }

    public void setValue(int k)
    {
        this.value = k;
    }
    
    @Override
    public String getValue()
    {
        return Integer.toString(this.value);
    }
    
    
    private class LayoutChoicePreferenceAdapter extends BaseAdapter
    {        
        public LayoutChoicePreferenceAdapter(Context context)
        {

        }

        public int getCount()
        {
            return ItemLayout.values().length;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent)
        {  
            View v = convertView;
            ViewHolder holder = null;

            if(v == null)
            {                                                                   
                v = mInflater.inflate(R.layout.layout_choice_preference_row, parent, false);
                holder = new ViewHolder();
                holder.imageView = ((ImageView) v.findViewById(R.id.layout_image));
                holder.radioButton = (RadioButton) v.findViewById(R.id.radio_button);
                v.setTag(holder);
                
                v.setClickable(true);
                
                holder.radioButton.setClickable(false);
            }
            else
            {
                holder = (ViewHolder) v.getTag();
            }
            
            v.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    for(RadioButton rb : rButtonList)
                    {
                        if(rb.getId() != position)
                        {
                            rb.setChecked(false);
                        }
                    }
                    
                    value = position;
                    
                    callChangeListener(position);
                    
                    getDialog().dismiss();
                }
            });
            
            holder.imageView.setImageResource(ItemLayout.values()[position].getDrawableId());
            holder.radioButton.setId(position);
            holder.radioButton.setChecked(value==position);
            
            return v;
        }
        
        class ViewHolder
        {
            RadioButton radioButton;
            ImageView imageView;
        }
    }
}
