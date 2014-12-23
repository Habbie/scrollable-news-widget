package fr.gdi.android.news.fragment.config;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import fr.gdi.android.news.R;
import fr.gdi.android.news.preference.utils.PreferenceUtils;
import fr.gdi.android.news.service.ServiceRegistration;

public class GlobalSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener
{
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.global_settings);
        setup();
    }

    private void setup()
    {
        EditTextPreference refreshPreference = (EditTextPreference) findPreference("refresh_interval_in_minutes"); //$NON-NLS-1$
        refreshPreference.setText(Integer.toString(PreferenceUtils.getRefreshInterval(getActivity())));
        refreshPreference.setOnPreferenceChangeListener(this);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        if ( TextUtils.equals(preference.getKey(), PreferenceUtils.REFRESH_INTERVAL) )
        {
            try
            {
                int minutes = Integer.parseInt((String) newValue);
                ServiceRegistration.register(getActivity(), minutes);
                return true;                                
            }
            catch ( Exception e )
            {
                return false;
            }
        }
        return false;
    }
}
