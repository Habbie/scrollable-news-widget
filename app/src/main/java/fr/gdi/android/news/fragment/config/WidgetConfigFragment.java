package fr.gdi.android.news.fragment.config;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.gdi.android.news.R;
import fr.gdi.android.news.activity.MainActivity;
import fr.gdi.android.news.data.dao.BehaviourDao;
import fr.gdi.android.news.data.dao.ConfigurationDao;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.FeedDao;
import fr.gdi.android.news.data.dao.ThemeDao;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.model.Configuration;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Theme;

public class WidgetConfigFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener
{
    private static final String LAUNCH_MAIN_ACTIVITY = "launch_main_activity"; //$NON-NLS-1$
    private static final String WIDGET_TITLE = "widget_title"; //$NON-NLS-1$
    private static final String BEHAVIOUR = "behaviour"; //$NON-NLS-1$
    private static final String THEME = "theme"; //$NON-NLS-1$
    private static final String FEEDS = "feeds"; //$NON-NLS-1$
 
    private static final int REQUEST_MAIN_ACTIVITY = 1;
    
    private int appWidgetId;
    
    private Configuration configuration;
    
    private Preference mainLauncherPreference;
    private Preference behaviourPreference;
    private Preference themePreference;
    private Preference feedsPreference;
    
    private FeedDao feedDao;
    private ThemeDao themeDao;
    private BehaviourDao behaviourDao;
    private ConfigurationDao configurationDao;
    private EditTextPreference widgetTitlePreference;
    
    private boolean refreshRequired = false;
    
    private List<Behaviour> declaredBehaviours;
    private List<Theme> declaredThemes;
    private List<Feed> declaredFeeds;
    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null )
        {
            appWidgetId = getArguments().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
            configuration = configurationDao.getConfigurationByWidgetId(appWidgetId);
            
            declaredBehaviours = behaviourDao.getAll();
            declaredThemes = themeDao.getAll();
            declaredFeeds = feedDao.getAll();
            
            if ( configuration == null )
            {
                configuration = new Configuration(getActivity());
                configuration.setAppWidgetId(appWidgetId);
            }
        }
        addPreferencesFromResource(R.xml.widget_config);
        
        setup();
    }
    
    private void setup()
    {
        mainLauncherPreference = findPreference(LAUNCH_MAIN_ACTIVITY);
        widgetTitlePreference = (EditTextPreference) findPreference(WIDGET_TITLE);
        behaviourPreference = findPreference(BEHAVIOUR);
        themePreference = findPreference(THEME);
        feedsPreference = findPreference(FEEDS);
        
        widgetTitlePreference.setText(configuration.getWidgetTitle());
        widgetTitlePreference.setOnPreferenceChangeListener(this);
        
        mainLauncherPreference.setOnPreferenceClickListener(this);
        feedsPreference.setOnPreferenceClickListener(this);
        behaviourPreference.setOnPreferenceClickListener(this);
        themePreference.setOnPreferenceClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.buttonized_list, null);
    }
    
    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        String key = preference.getKey();
        if ( TextUtils.equals(key, LAUNCH_MAIN_ACTIVITY) ) 
        {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivityForResult(intent, REQUEST_MAIN_ACTIVITY);
            return true;
        }
        
        if ( TextUtils.equals(key, FEEDS) ) 
        {
            openFeedSelectionDialog();
            return true;
        }
        
        if ( TextUtils.equals(key, BEHAVIOUR) ) 
        {
            openBehaviourSelectionDialog();
            return true;
        }
        
        if ( TextUtils.equals(key, THEME) ) 
        {
            openThemeSelectionDialog();
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch ( requestCode ) 
        {
            case REQUEST_MAIN_ACTIVITY:
                //always reload feeds, behaviours and themes
                declaredBehaviours = behaviourDao.getAll();
                declaredThemes = themeDao.getAll();
                declaredFeeds = feedDao.getAll();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
   
    private void openBehaviourSelectionDialog()
    {
        if ( declaredBehaviours.size() > 0 ) 
        {
            String[] behaviourNames = new String[declaredBehaviours.size()];
            for (int u = 0; u < declaredBehaviours.size(); u++)
            {
                Behaviour behaviour = declaredBehaviours.get(u);
                behaviourNames[u] = behaviour.getName();
            }

            int initialSelection = declaredBehaviours.indexOf(configuration.getBehaviour());
            initialSelection = initialSelection < 0 ? -1 : initialSelection;
            new AlertDialog.Builder(getActivity())
                .setTitle(R.string.config_widget_select_behaviour)
                .setSingleChoiceItems(behaviourNames, initialSelection, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Behaviour selected = declaredBehaviours.get(which);
                        configuration.setBehaviour(selected);
                        dialog.dismiss();
                    }
            }).show();
        }
        else 
        {
            showMissingObjectDialog(R.string.object_behaviour);
        }
    }

    private void openThemeSelectionDialog()
    {
        if ( declaredThemes.size() > 0 ) 
        {
            String[] themeNames = new String[declaredThemes.size()];
            for (int u = 0; u < declaredThemes.size(); u++)
            {
                Theme theme = declaredThemes.get(u);
                themeNames[u] = theme.getName();
            }

            int initialSelection = declaredThemes.indexOf(configuration.getTheme());
            initialSelection = initialSelection < 0 ? -1 : initialSelection;
            new AlertDialog.Builder(getActivity())
                .setTitle(R.string.config_widget_select_theme)
                .setSingleChoiceItems(themeNames, initialSelection, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Theme selected = declaredThemes.get(which);
                        configuration.setTheme(selected);
                        dialog.dismiss();
                    }
            }).show();
        }
        else 
        {
            showMissingObjectDialog(R.string.object_theme);
        }
    }
    
    
    private void openFeedSelectionDialog()
    {
        if ( declaredFeeds.size() == 0 ) 
        {
            showMissingObjectDialog(R.string.object_feed);
            return;
        }
        
        final List<Feed> widgetFeeds = configuration.getFeeds();
        
        final String[] options = new String[declaredFeeds.size()];
        final boolean[] selections = new boolean[options.length];

        for (int u = 0; u < declaredFeeds.size(); u++)
        {
            Feed feed = declaredFeeds.get(u);
            options[u] = feed.getTitle();
            selections[u] = widgetFeeds.contains(feed); 
        }
        
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.config_widget_select_feeds)
                .setMultiChoiceItems(options, selections, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        selections[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        List<Feed> selected = new ArrayList<Feed>();
                        
                        for (int i = 0; i < selections.length; i++)
                        {
                            boolean b = selections[i];
                            if ( b )
                            {
                                selected.add(declaredFeeds.get(i));
                            }
                        }
                        
                        if ( !refreshRequired )  
                        {
                            //original state not managed: 
                            //we ignore the case when list changes twice 
                            //and finally retrieves its original state
                            
                            if ( selected.size() != widgetFeeds.size() ) 
                            {
                                refreshRequired = true;
                            }
                            else
                            {
                                List<Feed> copy1 = new ArrayList<Feed>(widgetFeeds);
                                copy1.removeAll(selected);
                                if ( copy1.size() > 0 ) 
                                {
                                    refreshRequired = true;
                                }
                            }
                        }
                        
                        configuration.setFeeds(selected);
                        
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showMissingObjectDialog(int object)
    {
        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.warning)
            .setMessage(getEmptyCollectionWarning(object))
            .setIcon(R.drawable.warning)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            }).show();
    }
    
    
    private String getEmptyCollectionWarning(int resId)
    {
        String object = getActivity().getText(resId).toString();
        String msg = getActivity().getText(R.string.config_widget_empty_collection).toString();
        return String.format(msg, object, object);
    }

    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        String key = preference.getKey();
        if ( TextUtils.equals(WIDGET_TITLE, key) )
        {
            configuration.setWidgetTitle((String) newValue);
            return true;
        }
        return false;
    }

    @Override
    public void onAttach(Activity activity)
    {
        feedDao = DaoUtils.getFeedDao(activity);
        themeDao = DaoUtils.getThemeDao(activity);
        behaviourDao = DaoUtils.getBehaviourDao(activity);
        configurationDao = DaoUtils.getConfigurationDao(activity);
        
        super.onAttach(activity);
    }
    
    public void saveConfig()
    {
        configurationDao.saveOrInsert(configuration);
    }

    public Integer[] validate()
    {
        List<Integer> errors = new ArrayList<Integer>();
        
        if ( configuration.getBehaviour() == null ) 
        {
            if ( declaredBehaviours.size() == 1 ) 
            {
                configuration.setBehaviour(declaredBehaviours.get(0));
            }
            else
            {
                errors.add(R.string.config_widget_behaviour_not_set);
            }
        }
        
        if ( configuration.getTheme() == null ) 
        {
            if ( declaredThemes.size() == 1 ) 
            {
                configuration.setTheme(declaredThemes.get(0));
            }
            else
            {
                errors.add(R.string.config_widget_theme_not_set);
            }
        }
        
        if ( configuration.getFeeds().size() == 0 ) 
        {
            errors.add(R.string.config_widget_no_feed_selected);
        }
        
        return errors.toArray(new Integer[errors.size()]);
    }
    
    public boolean requiresRefresh()
    {
        return refreshRequired;
    }
}
