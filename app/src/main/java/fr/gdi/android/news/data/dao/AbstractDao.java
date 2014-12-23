package fr.gdi.android.news.data.dao;

import java.net.URL;
import java.util.Date;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.data.DatabaseHelper;

/**
 * this package could benefit from some light refactoring
 * as there is quite similar code in the various DAOs
 */
public abstract class AbstractDao<T>
{
    protected DatabaseHelper database;
    
    public AbstractDao(DatabaseHelper database)
    {
        this.database = database;
    }
    
    public int count()
    {
        return count(null, null);
    }
    
    public int count(String whereClause, Object[] params)
    {
        String sql = "select count(*) from " + getTableName(); //$NON-NLS-1$
        
        if ( !TextUtils.isEmpty(whereClause) ) 
        {
            sql += " " + whereClause; //$NON-NLS-1$
        }
        
        Cursor cursor = null;
        try 
        {
            int count = -1;
            cursor = database.executeQuery(sql, params != null ? params : new Object[0]);
            if ( cursor.moveToNext() ) 
            {
                count = cursor.getInt(0);
            }
            return count;
        }
        catch ( Exception e ) 
        {
            Log.e(Constants.PACKAGE, "Unable to retrieve " + getTableName() + " count", e); //$NON-NLS-1$ //$NON-NLS-2$
            return -1;
        }
        finally 
        {
            if ( cursor != null ) cursor.close();
        }
    }
    
    protected abstract String getTableName();
    
    protected URL getUrl(Cursor cursor, int index)
    {
        URL url = null;
        try 
        {
            String iu = cursor.getString(index);
            if ( !TextUtils.isEmpty(iu) ) 
            {
                url = new URL(iu);
            }
        }
        catch ( Exception e ) 
        {
            
        }
        return url;
    }
    
    protected Date getDate(Cursor cursor, int index)
    {
        if ( !cursor.isNull(index) ) {
            long time = cursor.getLong(index);
            return new Date(time);
        }
        else return null;
    }
    
    protected boolean getBoolean(Cursor cursor, int i)
    {
        if ( cursor.isNull(i) ) return true; 
        return cursor.getInt(i) > 0;
    }

}
