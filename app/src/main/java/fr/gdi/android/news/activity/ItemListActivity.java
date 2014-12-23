package fr.gdi.android.news.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import fr.gdi.android.news.fragment.item.ItemListFragment;

public class ItemListActivity extends Activity
{
    private ItemListFragment mainFragment;
    
    private long feedId;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        setupFragment();

        onResume();
    }

    private void setupFragment()
    {
        if (getFragmentManager().findFragmentById(android.R.id.content) == null) 
        {
            mainFragment = new ItemListFragment();
            getFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
        }
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent newIntent = new Intent(this, MainActivity.class);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(newIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
    @Override
    protected void onResume()
    {
        super.onResume();
        Intent intent = getIntent();
        if ( intent.hasExtra(ItemListFragment.FEED_ID_KEY) ) 
        {
            feedId = getIntent().getExtras().getLong(ItemListFragment.FEED_ID_KEY);
        }
        
        if ( mainFragment == null ) 
        {
            setupFragment();
        }
        
        mainFragment.setFeedId(feedId);
    }

}
