package fr.gdi.android.news.data.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.text.TextUtils;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.data.DatabaseHelper;
import fr.gdi.android.news.exceptions.ThemeProbablyInUseException;
import fr.gdi.android.news.model.Theme;

public class ThemeDao extends AbstractDao<Theme>
{

    //we rely field/column homonymous mapping to simplify this mess 
    private static final List<String> COLUMNS = Arrays.asList(new String[] {
        "name", "roundedCorners", "backgroundColor", "backgroundOpacity", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "storyTitleColor", "storyTitleFontSize", "storyTitleUppercase", "storyTitleMaxLines", "storyTitleHide", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        "storyDescriptionColor", "storyDescriptionFontSize", "storyDescriptionMaxWordCount",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        "showFooter", "footerUppercase", "footerFontSize", "storyAuthorColor", "storyDateColor", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        "thumbnailSize",  //$NON-NLS-1$
        "showWidgetTitle", "widgetTitleColor", //$NON-NLS-1$ //$NON-NLS-2$
        "layout", "dateFormat", //$NON-NLS-1$ //$NON-NLS-2$
        "numColumns"
    });
    
    public ThemeDao(DatabaseHelper database)
    {
        super(database);
    }

    public long insert(Theme theme) 
    {
        long id= database.getWritableDatabase().insert("theme", null, getContentValues(theme)); //$NON-NLS-1$
        theme.setId(id);
        return id;
    }
    
    public void update(Theme theme) 
    {
        database.getWritableDatabase().update("theme", getContentValues(theme), "_id=?", new String[] {Long.toString(theme.getId())}); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public static ContentValues getContentValues(Theme theme)
    {
        ContentValues cv = new ContentValues();
        
        Field field = null;
        for (String column : COLUMNS)
        {
            try 
            {
                field = Theme.class.getDeclaredField(column);
                field.setAccessible(true);
                
                Object value = field.get(theme);
                if ( value == null ) 
                {
                    //?really?
                    cv.putNull(column);
                }
                else if ( Integer.class.equals(value.getClass()) )
                {
                    cv.put(column, (Integer) value);
                }
                else if ( Boolean.class.equals(value.getClass()) )
                {
                    cv.put(column, (Boolean) value);
                }
                else if ( String.class.equals(value.getClass()) )
                {
                    cv.put(column, (String) value);
                }
                else 
                {
                    Log.w(Constants.PACKAGE, "Unexpected column type: " + value.getClass().getName()); //$NON-NLS-1$
                }
            }
            catch ( Exception e ) 
            {
                Log.e(Constants.PACKAGE, "Unable to obtain value of field " + column, e); //$NON-NLS-1$
            }
        }
        
        return cv;
    }
    
    public void delete(Theme theme) throws ThemeProbablyInUseException
    {
        delete(theme.getId());
    }
    
    public void delete(long themeId) throws ThemeProbablyInUseException
    {
        try
        {
            database.executeUpdate("delete from theme where _id=?", new Object[] { themeId }); //$NON-NLS-1$
        }
        catch (SQLiteConstraintException e)
        {
            Log.e(Constants.PACKAGE, "Couldnot delete theme: " + e.getMessage()); //$NON-NLS-1$
            throw new ThemeProbablyInUseException();
        }
    }
    
    public Theme getTheme(int themeId) 
    {
        List<Theme> themes = getAll("where _id=?", new Object[] { themeId }); //$NON-NLS-1$
        if ( themes != null && themes.size() > 0 )
        {
            if ( themes.size() > 1 ) 
            {
                Log.w(Constants.PACKAGE, "Too many themes retrieved (themeId=" + themeId + "). Returning the first one."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return themes.get(0);
        }
        return null;
    }
    
    public List<Theme> getAll()
    {
        return getAll(null, new Object[0]);
    }
    
    public List<Theme> getAll(String whereClause, Object[] params)
    {
        List<Theme> themes = new ArrayList<Theme>();
        Cursor cursor = null;
        try 
        {
            cursor = database.executeQuery(getFindQuery(whereClause), params);
            while ( cursor.moveToNext() ) 
            {
                themes.add(getTheme(cursor));
            }
        }
        catch ( Exception e ) 
        {
            Log.e(Constants.PACKAGE, "Unable to retrieve themes", e); //$NON-NLS-1$
        }
        finally 
        {
            if ( cursor != null ) cursor.close();
        }
        return themes;
    }
    
    
    private Theme getTheme(Cursor cursor)
    {
        Theme theme = new Theme(database.getContext());

        long id = cursor.getLong(0);
        theme.setId(id);
        
        for (String col : COLUMNS)
        {
            setThemeField(theme, cursor, col);
        }
        
        return theme;
    }

    private void setThemeField(Theme theme, Cursor cursor, String column)
    {
        int u = COLUMNS.indexOf(column) + 1;
        
        if ( !cursor.isNull(u) )
        {
            Field field = null;
            try 
            {
                field = Theme.class.getDeclaredField(column);
                field.setAccessible(true);
                Class<?> type = field.getType();
                
                if ( Integer.class.equals(type) || int.class.equals(type))
                {
                    field.set(theme, cursor.getInt(u));
                }
                else if ( Boolean.class.equals(type) || boolean.class.equals(type))
                {
                    field.set(theme, getBoolean(cursor, u));
                }
                else if ( String.class.equals(type) ) 
                {
                    field.set(theme, cursor.getString(u));
                }
                else 
                {
                    Log.w(Constants.PACKAGE, "Couldnot resolve field type (" + column + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            catch ( Exception e ) 
            {
                Log.e(Constants.PACKAGE, "Couldnot resolve field type (" + column + ")", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            finally 
            {
                field.setAccessible(false);                    
            }
        }
    }

    
    
    private String getFindQuery(String whereClause) 
    {
        String sql = "select _id" ; //$NON-NLS-1$
        for (String  c : COLUMNS )
        {
            sql += ", " + c; //$NON-NLS-1$
        }
        
        sql += " from theme " + (!TextUtils.isEmpty(whereClause) ? whereClause : "") + " order by name"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        return sql;
    }
    
    @Override
    protected String getTableName()
    {
        return "theme"; //$NON-NLS-1$
    }
}
