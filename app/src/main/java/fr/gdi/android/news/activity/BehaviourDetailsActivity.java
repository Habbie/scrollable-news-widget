package fr.gdi.android.news.activity;

import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.View;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.BehaviourDao;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.fragment.behaviour.BehaviourDetailsFragment;
import fr.gdi.android.news.fragment.theme.ThemeDetailsFragment;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.utils.dialog.ToastUtils;

public class BehaviourDetailsActivity extends PreferenceActivity
{
    private static final int VALIDATION_OK = -1;
    
    private BehaviourDetailsFragment mainFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if (getFragmentManager().findFragmentById(android.R.id.content) == null)
        {
            mainFragment = new BehaviourDetailsFragment();
            mainFragment.setArguments(getBundle());
            getFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
        }
    }

    private Bundle getBundle()
    {
        Bundle args = new Bundle();
        String key = BehaviourDetailsFragment.BEHAVIOUR_KEY;
        Serializable obj = getIntent().getSerializableExtra(key);
        args.putSerializable(key, obj);
        return args;
    }
    
    public void onButtonClick(View v)
    {
        switch ( v.getId() ) 
        {
            case R.id.ok:
                Behaviour behaviour = mainFragment.getBehaviour(); 
                int errId = validate(behaviour);
                if ( errId == -1 ) 
                {
                    Intent intent = new Intent();
                    intent.putExtra(ThemeDetailsFragment.THEME_KEY, behaviour);
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
    
    private int validate(Behaviour behaviour)
    {
        if ( TextUtils.isEmpty(behaviour.getName()) ) return R.string.behaviour_validation_name_not_set;
        
        BehaviourDao dao = DaoUtils.getBehaviourDao(this);
        List<Behaviour> behaviours = dao.getBehaviours(" where name=? ", new Object[] { behaviour.getName() }); //$NON-NLS-1$
        
        return (behaviours.size() == 0) || //behaviour not saved yet or name changed, expected count=0
               (behaviour.getId() > 0 && behaviours.size() == 1) ?  //behaviour already saved, expected count=1
                 VALIDATION_OK : R.string.behaviour_validation_name_exists;
    }
    
    
}