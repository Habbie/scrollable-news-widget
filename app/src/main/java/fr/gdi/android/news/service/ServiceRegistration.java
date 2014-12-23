package fr.gdi.android.news.service;

import fr.gdi.android.news.preference.utils.PreferenceUtils;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class ServiceRegistration
{
    private static PendingIntent serviceIntent = null;
    
    public static void register(Context context)
    {
        int minutes = PreferenceUtils.getRefreshInterval(context);
        register(context, minutes);
    }
    
    public static void register(Context context, int minutes)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        if (serviceIntent != null) alarmManager.cancel(serviceIntent);
        
        final int updateInterval = minutes*60*1000; 
        
        if (updateInterval != 0)
        {
            if (serviceIntent == null)
            {
                Intent intent = new Intent(context, UpdateService.class);
                serviceIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            
            alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                    SystemClock.elapsedRealtime() + updateInterval, 
                    updateInterval,
                    serviceIntent);
        }
    }
    
    public static void unregister(Context context)
    {
        if (serviceIntent != null)
        {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(serviceIntent);
            serviceIntent = null;
        }
    }
    
}
