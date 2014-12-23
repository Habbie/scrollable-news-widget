package fr.gdi.android.news.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import fr.gdi.android.news.preference.utils.PreferenceUtils;
import fr.gdi.android.news.receiver.WidgetUpdater;
import fr.gdi.android.news.utils.DeviceUtils;

public class UpdateService extends Service
{
    private WakeLock wakeLock;
    
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
       
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        wakeLock.acquire();
        
        if (shouldUpdate())
        {
            boolean force = intent.getBooleanExtra(WidgetUpdater.FORCE_REFRESH, false);
            
            if ( intent != null && intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
            {
                int widgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                update(getApplicationContext(), widgetId, force);
            }
            else 
            {
                updateAll(getApplicationContext(), force);
            }
        }
        
        stopSelf(startId);
        return START_STICKY;
    }
    
    private boolean shouldUpdate()
    {
        boolean isOnline = DeviceUtils.isOnline(getApplicationContext());
        
        if ( PreferenceUtils.shouldUpdateOnlyIfWifiOn(this) ) 
        {
            boolean isWifiOn = DeviceUtils.isWifiOn(this);
            return isOnline && isWifiOn;
        }
        
        return isOnline;
    }
    
    public void onDestroy()
    {
        super.onDestroy();
        wakeLock.release();
    }
    
    public void update(Context context, Integer widgetId, boolean force)
    {
        Intent updateIntent = new Intent(context, WidgetUpdater.class);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        updateIntent.putExtra(WidgetUpdater.FORCE_REFRESH, force);
        updateIntent.setAction(WidgetUpdater.ACTION_UPDATE);
        context.sendBroadcast(updateIntent);
    }
    
    public final void updateAll(Context ctx, boolean force)
    {
        int[] appWidgetIds = DeviceUtils.getAllWidgetIds(ctx);
        for (int appWidgetId : appWidgetIds)
        {
            update(ctx, appWidgetId, force);
        }
    }
    
}
