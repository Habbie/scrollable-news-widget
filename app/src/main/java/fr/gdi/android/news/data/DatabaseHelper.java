package fr.gdi.android.news.data;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.BehaviourDao;
import fr.gdi.android.news.data.dao.ThemeDao;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.model.Theme;
import fr.gdi.android.news.utils.IOUtils;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 6;
    
    private static final String DATABASE_NAME = "fr.gdi.android.news.db"; //$NON-NLS-1$
    private static final String DATABASE_PATH = "database/database.ddl"; //$NON-NLS-1$
    private static final String UPDATE_FOLDER_PATH = "database/updates/"; //$NON-NLS-1$
    
    private Context context;
    
    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    
    @Override
    public synchronized SQLiteDatabase getReadableDatabase()
    {
        SQLiteDatabase db = super.getReadableDatabase();
        db.setLockingEnabled(true);
        return db;
    }
    
    @Override
    public synchronized SQLiteDatabase getWritableDatabase()
    {
        SQLiteDatabase db = super.getWritableDatabase();
        db.setLockingEnabled(false);
        return db;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        execBatch(db, DATABASE_PATH);
        
        //insert initial sample data
        createSampleInstances(db);
    }

    private void execBatch(SQLiteDatabase database, String ddlFilePath)
    {
        String batch = IOUtils.loadTextAsset(context, ddlFilePath);
        
        try 
        {
            String[] instrs = TextUtils.split(batch, "-- break");  //$NON-NLS-1$
            for (String ddl : instrs)
            {
                ddl = ddl.trim();
                if ( !TextUtils.isEmpty(ddl) ) 
                {
                    database.execSQL(ddl);
                }
            }

            
        }
        catch ( Exception e ) 
        {
            Log.e(Constants.PACKAGE, "Unable to install database",e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

    private void createSampleInstances(SQLiteDatabase db)
    {
        db.execSQL("insert into feed (title, url, type, image_url) values (?, ?, ?, ?)",  //$NON-NLS-1$
                   new Object[] { 
                        "Reuters: World News",  //$NON-NLS-1$
                        "http://feeds.reuters.com/reuters/worldNews",  //$NON-NLS-1$
                        "rss",  //$NON-NLS-1$
                        "http://www.reuters.com/resources_v2/images/reuters125.png"  //$NON-NLS-1$
                    });
        
        
        db.insert("theme", null, ThemeDao.getContentValues(getDefaultDarkTheme())); //$NON-NLS-1$
        db.insert("theme", null, ThemeDao.getContentValues(getDefaultLightTheme())); //$NON-NLS-1$

        db.insert("behaviour", null, BehaviourDao.getContentValues(new Behaviour(getContext()))); //$NON-NLS-1$
    }

    private Theme getDefaultDarkTheme()
    {
        Theme defaultDarkTheme = new Theme(context);
        defaultDarkTheme.setName(context.getText(R.string.defaults_dark_theme).toString());
        return defaultDarkTheme;
    }
    
    private Theme getDefaultLightTheme()
    {
        Theme defaultLightTheme = new Theme(context);
        defaultLightTheme.setName(context.getText(R.string.defaults_light_theme).toString());
        defaultLightTheme.setBackgroundColor(Color.WHITE);
        defaultLightTheme.setBackgroundOpacity(70);
        defaultLightTheme.setStoryAuthorColor(Color.BLACK);
        defaultLightTheme.setStoryDateColor(Color.BLACK);
        defaultLightTheme.setStoryTitleColor(Color.BLACK);
        defaultLightTheme.setStoryDescriptionColor(Color.BLACK);
        return defaultLightTheme;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        int inc = oldVersion + 1;
        while ( inc <= newVersion ) 
        {
            execBatch(db, UPDATE_FOLDER_PATH + inc + ".ddl"); //$NON-NLS-1$
            inc++;
        }
    }
    
    public void onOpen(SQLiteDatabase db) 
    {
        super.onOpen(db);
        
        if (!db.isReadOnly()) 
        {
            db.execSQL("PRAGMA foreign_keys=ON;"); //$NON-NLS-1$
        }
    }

    
    public Cursor executeQuery(String query, Object[] parameters, IQueryCallback callback) 
    {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransactionNonExclusive();
        Cursor cursor = null;
        try 
        {
            cursor = database.rawQuery(query, convertToStrings(parameters));
            if ( callback != null ) 
            {
                callback.doInTransaction(cursor);
            }
            database.setTransactionSuccessful();
            return cursor;
        }
        finally 
        {
            database.endTransaction();
        }
    }
    
    public Cursor executeQuery(String query, Object[] parameters) 
    {
        return executeQuery(query, parameters, null);
    }

    //TODO: ok for single statements. needs to be refined  
    //    : for multi-statement transactions (main cases)
    public void executeUpdate(String query, Object[] parameters) 
    {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransactionNonExclusive();
        try 
        {
            database.execSQL(query, parameters);
            database.setTransactionSuccessful();
        }
        finally 
        {
            database.endTransaction();
        }
    }
    
    //TODO: this doesn't seem right, but then again android is one the crappiest 
    //    : framework around today, so perhaps this is the right way to do it
    private String[] convertToStrings(Object[] parameters) 
    {
        String[] s = new String[parameters.length];
        
        for (int i = 0; i < parameters.length; i++)
        {
            Object o = parameters[i];
            if ( o == null ) 
            {
                s[i] = null;
            }
            else
            {
                if ( o instanceof Date ) 
                {
                    s[i] = Long.toString(((Date) o).getTime());
                }
                else 
                {
                    s[i] = o.toString();
                }
            }
        }
        
        return s;
    }
    
    public Context getContext()
    {
        return context;
    }
}
