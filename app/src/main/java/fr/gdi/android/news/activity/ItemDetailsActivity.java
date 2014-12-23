package fr.gdi.android.news.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import fr.gdi.android.news.fragment.item.ItemDetailsFragment;

public class ItemDetailsActivity extends Activity
{
    private ItemDetailsFragment mainFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        if (getFragmentManager().findFragmentById(android.R.id.content) == null) 
        {
            mainFragment = new ItemDetailsFragment();
            getFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }
}
