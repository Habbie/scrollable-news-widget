package fr.gdi.android.news.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import fr.gdi.android.news.R;
import fr.gdi.android.news.fragment.config.WidgetConfigFragment;
import fr.gdi.android.news.receiver.NewsWidgetProvider;
import fr.gdi.android.news.receiver.WidgetUpdater;
import fr.gdi.android.news.utils.dialog.ToastUtils;

public class ConfigurationActivity extends Activity
{
    public static final String UPDATE_EXISTING = null;

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private WidgetConfigFragment mainFragment;
    
    private boolean updateExisting = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setResult(RESULT_CANCELED);
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) 
        {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            updateExisting = extras.getBoolean(UPDATE_EXISTING);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) 
        {
            finish();
        }
    }
    
    public void onButtonClick(View v)
    {
        switch ( v.getId() ) 
        {
            case R.id.ok: 
                if ( mainFragment == null ) 
                {
                    //should not happen that often but this might still 
                    //still happen if we mess with home launchers (update, 
                    //reset, etc. -- not sure exactly what causes this) 
                    ToastUtils.showWarning(this, getText(R.string.config_widget_not_saved).toString());
                    finish();
                    return;
                }
                
                Integer[] validationErrors = mainFragment.validate();
                if ( validationErrors.length == 0 ) 
                {
                    mainFragment.saveConfig();

                    Intent updateIntent = null;
                     
                    if ( updateExisting && !requiresRefresh() ) 
                    {
                        //update existing widget if feed list has not changed
                        updateIntent = new Intent(this, NewsWidgetProvider.class);
                        updateIntent.putExtra(NewsWidgetProvider.UPDATE_DATE_KEY, false);
                        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });
                    }
                    else
                    {
                        //bootstrap that thing: force fetch feed items, etc.  
                        updateIntent = new Intent(this, WidgetUpdater.class);
                        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                        updateIntent.putExtra(WidgetUpdater.FORCE_REFRESH, true);
                    }
                    updateIntent.setAction(WidgetUpdater.ACTION_UPDATE);
                    this.sendBroadcast(updateIntent);

                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    setResult(RESULT_OK, resultValue);

                    finish();
                }
                else 
                {
                    StringBuilder msg = new StringBuilder();
                    for (int resId : validationErrors)
                    {
                        String err = this.getText(resId).toString();
                        msg.append("- ").append(err).append(System.getProperty("line.separator"));   //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                        .setTitle(getText(R.string.config_widget_validation_errors))
                        .setMessage(msg.toString())
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                    });
                    
                    dialog.show();
                }
                break;
            case R.id.cancel:
                finish();
                break;
        }
        
        
    }
    
    @Override
    protected void onResume()
    {
        if (getFragmentManager().findFragmentById(android.R.id.content) == null)
        {
            mainFragment = new WidgetConfigFragment();
            Bundle args = new Bundle();
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            mainFragment.setArguments(args);
            getFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
        }
        
        super.onResume();
    }

    private boolean requiresRefresh()
    {   
        return mainFragment.requiresRefresh();  
    }
}
