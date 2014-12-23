package fr.gdi.android.news.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import fr.gdi.android.news.R;
import fr.gdi.android.news.fragment.behaviour.BehaviourListFragment;
import fr.gdi.android.news.fragment.config.GlobalSettingsFragment;
import fr.gdi.android.news.fragment.feed.FeedListFragment;
import fr.gdi.android.news.fragment.feed.IBackButtonAwareFragment;
import fr.gdi.android.news.fragment.help.HelpFragment;
import fr.gdi.android.news.fragment.item.FavoriteListFragment;
import fr.gdi.android.news.fragment.statistics.StatisticsFragment;
import fr.gdi.android.news.fragment.theme.ThemeListFragment;

public class MainActivity extends Activity
{
    private static final String VERSION_KEY = "whats_new_last_version"; //$NON-NLS-1$
    
    private static final String EXTRA_TAB_KEY = "tab"; //$NON-NLS-1$

    private static final int WHATS_NEW_OPTION_ID = 0; 
    
    private FeedListFragment feedListFragment;
    
    private ThemeListFragment themeListFragment;
    
    private BehaviourListFragment behavourListFragment;
    
    private Fragment currentFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        setDisplayOptions(bar);
        
        feedListFragment = new FeedListFragment(); 
        themeListFragment = new ThemeListFragment();
        behavourListFragment = new BehaviourListFragment();

        createTabs();
        
        if (savedInstanceState != null)
        {
            bar.setSelectedNavigationItem(savedInstanceState.getInt(EXTRA_TAB_KEY, 0));
        }
        
        init();
    }

    
    private void init()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int currentVersionNumber = 0;
        
        int savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0);
        
        try
        {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        }
        catch (Exception e)
        {
            
        }
        
        if (currentVersionNumber > savedVersionNumber)
        {
            showWhatsNewDialog();
            
            Editor editor = sharedPref.edit();
            
            editor.putInt(VERSION_KEY, currentVersionNumber);
            editor.commit();
        }
    }
    
    private void showWhatsNewDialog()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        
        View view = inflater.inflate(R.layout.whatsnew, null);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setView(view).setTitle(R.string.whats_new_option_text).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        
        builder.create().show();
    }
    
    private void createTabs() 
    {
        addTab(getText(R.string.main_tab_feeds), feedListFragment);
        addTab(getText(R.string.main_tab_themes), themeListFragment);
        addTab(getText(R.string.main_tab_behaviours), behavourListFragment);
        addTab(getText(R.string.main_tab_favorites), new FavoriteListFragment());
        addTab(getText(R.string.main_tab_settings), new GlobalSettingsFragment());
        addTab(getText(R.string.main_tab_stats), new StatisticsFragment());
        addTab(getText(R.string.main_tab_about), new HelpFragment());
    }
    
    private void addTab(CharSequence title, Fragment fragment)
    {
        ActionBar bar = getActionBar();
        ActionBar.Tab tab = bar.newTab().setText(title);
        tab.setTabListener(new TabSelectionListener(fragment));
        bar.addTab(tab);
    }
    
    private void setDisplayOptions(ActionBar bar)
    {
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_USE_LOGO);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_TAB_KEY, getActionBar().getSelectedNavigationIndex());
    }
    
    public void onFeedListCommandSelected(View v) 
    {
        feedListFragment.onCommandSelected(v);
    }
    
    public void onThemeListCommandSelected(View v) 
    {
        themeListFragment.onCommandSelected(v);
    }
    
    public void onBehaviourListCommandSelected(View v) 
    {
        behavourListFragment.onCommandSelected(v);
    }
    
    protected class TabSelectionListener implements ActionBar.TabListener
    {
        private Fragment fragment;
        
        public TabSelectionListener(Fragment fragment)
        {
            this.fragment = fragment;
        }
        
        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft)
        {
            String tag = fragment.getClass().getName();
            if (null != getFragmentManager().findFragmentByTag(tag)) {
                ft.attach(fragment);
                ft.show(fragment);
             }
             else {
                ft.add(R.id.fragment_place, fragment, tag);
             }
            currentFragment = fragment;
        }
        
        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft)
        {
            onTabSelected(tab, ft);
        }
        
        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft)
        {
            //ft.remove(fragment);
            ft.hide(fragment);
            currentFragment = null;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, WHATS_NEW_OPTION_ID, Menu.FIRST, R.string.whats_new_option_text);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case WHATS_NEW_OPTION_ID: 
                showWhatsNewDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed()
    {
        boolean handled = false;
        
        if ( currentFragment instanceof IBackButtonAwareFragment ) 
        {
            handled = ((IBackButtonAwareFragment) currentFragment).onBackPressed();
        }

        if ( !handled ) 
        {
            setResult(RESULT_OK);
            super.onBackPressed();                
        }
    }
    
}
