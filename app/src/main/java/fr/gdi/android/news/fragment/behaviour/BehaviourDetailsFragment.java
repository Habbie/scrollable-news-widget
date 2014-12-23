package fr.gdi.android.news.fragment.behaviour;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.gdi.android.news.R;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.model.Mobilizer;

//doesn't feel right but is there any drastically better way? 
public class BehaviourDetailsFragment extends PreferenceFragment implements OnPreferenceChangeListener
{

    public static final String BEHAVIOUR_KEY = "theme"; //$NON-NLS-1$

    private Behaviour behaviour;

    private static final String BEHAVIOUR_NAME = "behaviour_name"; //$NON-NLS-1$
    private static final String MAX_NUMBER_OF_NEWS = "max_number_of_news"; //$NON-NLS-1$
    private static final String MOBILIZER = "mobilizer"; //$NON-NLS-1$
    private static final String USE_BUILTIN_BROWSER = "use_builtin_brower"; //$NON-NLS-1$
    private static final String LOOKUP_IMAGES_IN_BODY = "lookup_images_in_body"; //$NON-NLS-1$
    private static final String FORCE_AUTHOR = "force_feed_as_author"; //$NON-NLS-1$
    private static final String HIDE_READ_STORIES = "hide_read_stories"; //$NON-NLS-1$
    private static final String CLEAR_BEFORE_LOAD = "clear_before_load"; //$NON-NLS-1$
    private static final String DISTRIBUTE_EVENLY = "distribute_evenly"; //$NON-NLS-1$
    
    private EditTextPreference behaviourNamePreference;

    private EditTextPreference maxNumberOfNewsPreference;

    private ListPreference mobilizerPreference;

    private CheckBoxPreference useBuiltInBrowserPreference;

    private CheckBoxPreference hideReadStoriesPreference;

    private CheckBoxPreference lookupImagesInBodyPreference;
    
    private CheckBoxPreference forceAuthorPreference;
    
    private CheckBoxPreference clearBeforeLoadPreference;
    
    private CheckBoxPreference distributeEvenlyPreference;
    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null )
        {
            behaviour = (Behaviour) getArguments().getSerializable(BEHAVIOUR_KEY);
        }
        addPreferencesFromResource(R.xml.behaviour_details);
        
        setup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.buttonized_list, null);
    }
    
    private void setup()
    {
        behaviourNamePreference = (EditTextPreference) findPreference(BEHAVIOUR_NAME);
        maxNumberOfNewsPreference = (EditTextPreference) findPreference(MAX_NUMBER_OF_NEWS);
        mobilizerPreference = (ListPreference) findPreference(MOBILIZER);
        useBuiltInBrowserPreference = (CheckBoxPreference) findPreference(USE_BUILTIN_BROWSER);
        lookupImagesInBodyPreference = (CheckBoxPreference) findPreference(LOOKUP_IMAGES_IN_BODY);
        forceAuthorPreference = (CheckBoxPreference) findPreference(FORCE_AUTHOR);
        hideReadStoriesPreference = (CheckBoxPreference) findPreference(HIDE_READ_STORIES);
        clearBeforeLoadPreference = (CheckBoxPreference) findPreference(CLEAR_BEFORE_LOAD);
        distributeEvenlyPreference = (CheckBoxPreference) findPreference(DISTRIBUTE_EVENLY);
        
        translateMobilizerNames();
        
        behaviourNamePreference.setOnPreferenceChangeListener(this);
        maxNumberOfNewsPreference.setOnPreferenceChangeListener(this);
        mobilizerPreference.setOnPreferenceChangeListener(this);
        useBuiltInBrowserPreference.setOnPreferenceChangeListener(this);
        lookupImagesInBodyPreference.setOnPreferenceChangeListener(this);
        forceAuthorPreference.setOnPreferenceChangeListener(this);
        hideReadStoriesPreference.setOnPreferenceChangeListener(this);
        clearBeforeLoadPreference.setOnPreferenceChangeListener(this);
        distributeEvenlyPreference.setOnPreferenceChangeListener(this);
        
        setPreferenceValues();
    }

    private void translateMobilizerNames()
    {
        CharSequence[] mobilizerEntries = mobilizerPreference.getEntries();
        CharSequence[] newEntries = new String[mobilizerEntries.length];
        for (int u = 0; u < mobilizerEntries.length; u++)
        {
            CharSequence s = mobilizerEntries[u];
            Mobilizer m = Mobilizer.fromName(s.toString());
            newEntries[u] = m.getTranslatedName(getActivity());
        }
    }
    
    private void setPreferenceValues() 
    {
        behaviourNamePreference.setText(behaviour.getName());
        maxNumberOfNewsPreference.setText(Integer.toString(behaviour.getMaxNumberOfNews()));
        useBuiltInBrowserPreference.setChecked(behaviour.isUseBuiltInBrowser());
        lookupImagesInBodyPreference.setChecked(behaviour.isLookupImageInBody());
        mobilizerPreference.setValue(behaviour.getMobilizer().getName());
        forceAuthorPreference.setChecked(behaviour.isForceFeedAsAuthor());
        hideReadStoriesPreference.setChecked(behaviour.isHideReadStories());
        clearBeforeLoadPreference.setChecked(behaviour.isClearBeforeLoad());
        distributeEvenlyPreference.setChecked(behaviour.shouldDistributeEvenly());
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        if ( behaviour == null ) return false;

        String k = preference.getKey();
        
        if ( TextUtils.equals(k, BEHAVIOUR_NAME) ) 
        {
            behaviour.setName((String) newValue);
        }
        if ( TextUtils.equals(k, MAX_NUMBER_OF_NEWS) ) 
        {
            behaviour.setMaxNumberOfNews(getInteger(newValue));
        }
        if ( TextUtils.equals(k, LOOKUP_IMAGES_IN_BODY) ) 
        {
            behaviour.setLookupImageInBody((Boolean) newValue);
        }
        if ( TextUtils.equals(k, MOBILIZER) ) 
        {
            behaviour.setMobilizer(Mobilizer.fromName((String) newValue));
        }
        if ( TextUtils.equals(k, FORCE_AUTHOR) ) 
        {
            behaviour.setForceFeedAsAuthor((Boolean) newValue);
        }
        if ( TextUtils.equals(k, USE_BUILTIN_BROWSER) ) 
        {
            behaviour.setUseBuiltInBrowser((Boolean) newValue);
        }
        if ( TextUtils.equals(k, HIDE_READ_STORIES) ) 
        {
            behaviour.setHideReadStories((Boolean) newValue);
        }
        if ( TextUtils.equals(k, CLEAR_BEFORE_LOAD) ) 
        {
            behaviour.setClearBeforeLoad((Boolean) newValue);
        }
        if ( TextUtils.equals(k, DISTRIBUTE_EVENLY) ) 
        {
            behaviour.setDistributeEvenly((Boolean) newValue);
        }
        
        return true;
    }
    
    private int getInteger(Object newValue)
    {
        if ( newValue instanceof String ) 
        {
            try 
            {
                return Integer.parseInt((String) newValue);
            }
            catch ( Exception e ) 
            {
                return 0;
            }
        }
        if ( newValue instanceof Integer ) return (Integer) newValue;
        return 0;
    }
    
    public Behaviour getBehaviour()
    {
        return behaviour;
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }
}
