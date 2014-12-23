package fr.gdi.android.news.activity;

import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.View;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.ThemeDao;
import fr.gdi.android.news.fragment.theme.ThemeDetailsFragment;
import fr.gdi.android.news.model.Theme;
import fr.gdi.android.news.utils.dialog.ToastUtils;

public class ThemeDetailsActivity extends PreferenceActivity
{
    private static final int VALIDATION_OK = -1;
    
    private ThemeDetailsFragment mainFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if (getFragmentManager().findFragmentById(android.R.id.content) == null)
        {
            mainFragment = new ThemeDetailsFragment();
            mainFragment.setArguments(getBundle());
            getFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
        }
    }

    private Bundle getBundle()
    {
        Bundle args = new Bundle();
        String key = ThemeDetailsFragment.THEME_KEY;
        Serializable obj = getIntent().getSerializableExtra(key);
        args.putSerializable(key, obj);
        return args;
    }
    
    public void onButtonClick(View v)
    {
        switch ( v.getId() ) 
        {
            case R.id.ok:
                if ( mainFragment == null ) 
                {
                    ToastUtils.showError(this, R.string.theme_not_saved_invalid_state);
                    this.finish();
                    break;
                }
                Theme theme = mainFragment.getTheme(); 
                int errId = validate(theme);
                if ( errId == -1 ) 
                {
                    Intent intent = new Intent();
                    intent.putExtra(ThemeDetailsFragment.THEME_KEY, theme);
                    setResult(RESULT_OK, intent);
                    this.finish();
                }
                else 
                {
                    ToastUtils.showError(this, errId);
                }
                break;
            case R.id.cancel:
                setResult(RESULT_CANCELED);
                this.finish();
                break;
        }
    }
    
    private int validate(Theme theme)
    {
        if ( TextUtils.isEmpty(theme.getName()) ) return R.string.theme_validation_name_not_set;
        
        ThemeDao dao = DaoUtils.getThemeDao(this);
        List<Theme> themes = dao.getAll(" where name=? ", new Object[] { theme.getName() }); //$NON-NLS-1$

        return (themes.size() == 0) || //theme not saved yet or name changed, expected count=0
               (theme.getId() > 0 && themes.size() == 1) ?  //theme already saved, expected count=1
                 VALIDATION_OK : R.string.theme_validation_name_exists;
    }
    
}