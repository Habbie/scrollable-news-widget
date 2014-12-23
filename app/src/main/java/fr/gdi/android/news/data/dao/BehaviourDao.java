package fr.gdi.android.news.data.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.data.DatabaseHelper;
import fr.gdi.android.news.exceptions.BehaviourProbablyInUseException;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.model.Mobilizer;

public class BehaviourDao extends AbstractDao<Behaviour>
{
    public BehaviourDao(DatabaseHelper database)
    {
        super(database);
    }
    
    public long insert(Behaviour b) 
    {
        long id = database.getWritableDatabase().insert("behaviour", null, getContentValues(b)); //$NON-NLS-1$
        b.setId(id);
        return id;
    }
    
    public void update(Behaviour b) 
    {
        database.getWritableDatabase().update("behaviour", getContentValues(b), "_id=?", new String[] {Long.toString(b.getId())}); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void delete(Behaviour b) throws BehaviourProbablyInUseException
    {
        delete(b.getId());
    }

    public void delete(long id) throws BehaviourProbablyInUseException
    {
        try
        {
            database.executeUpdate("delete from behaviour where _id=?", new Object[] { id }); //$NON-NLS-1$
        }
        catch (SQLiteConstraintException e)
        {
            Log.e(Constants.PACKAGE, "Couldnot delete behaviour: " + e.getMessage()); //$NON-NLS-1$
            throw new BehaviourProbablyInUseException();
        }
    }
    
    public Behaviour getBehaviour(int id) 
    {
        List<Behaviour> behaviours = getBehaviours("where _id=?", new Object[] { id }); //$NON-NLS-1$
        if ( behaviours != null && behaviours.size() > 0 )
        {
            if ( behaviours.size() > 1 ) 
            {
                Log.w(Constants.PACKAGE, "Too many themes retrieved (themeId=" + id + "). Returning the first one."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return behaviours.get(0);
        }
        return null;
    }
    
    public List<Behaviour> getAll()
    {
        return getBehaviours("", new Object[0]); //$NON-NLS-1$
    }
    
    public List<Behaviour> getBehaviours(String whereClause, Object[] params) 
    {
        String sql = "select " + //$NON-NLS-1$
            "f._id, f.name, f.mobilizer, " + //$NON-NLS-1$
            "f.use_builtin_browser, f.lookup_thumbnail_in_body, f.max_story_number, f.force_feed_as_author, f.hide_read_stories, f.clear_before_load, f.distribute_evenly " + //$NON-NLS-1$
            "from behaviour f " + whereClause + " " + //$NON-NLS-1$ //$NON-NLS-2$
            "order by upper(f.name)"; //$NON-NLS-1$

        List<Behaviour> behaviours = new ArrayList<Behaviour>();
        
        Cursor cursor = null;
        
        try
        {
            cursor = database.executeQuery(sql, params);
            while ( cursor.moveToNext() ) 
            {
               Behaviour f = getBehaviour(cursor);
               behaviours.add(f);
            }
        }
        finally 
        {
            if (cursor != null) cursor.close();
        }
        
        return behaviours;
    }
    
    private Behaviour getBehaviour(Cursor cursor)
    {
        Behaviour b = new Behaviour(database.getContext());
        
        b.setId(cursor.getLong(0));
        b.setName(cursor.getString(1));
        b.setMobilizer(Mobilizer.fromName(cursor.getString(2)));
        b.setUseBuiltInBrowser(getBoolean(cursor, 3));
        b.setLookupImageInBody(getBoolean(cursor, 4));
        b.setMaxNumberOfNews(cursor.getInt(5));
        b.setForceFeedAsAuthor(getBoolean(cursor, 6));
        b.setHideReadStories(getBoolean(cursor, 7));
        b.setClearBeforeLoad(getBoolean(cursor, 8));
        b.setDistributeEvenly(getBoolean(cursor, 9));
        
        return b;
    }
    
    public static ContentValues getContentValues(Behaviour b)
    {
        ContentValues cv = new ContentValues();
        
        cv.put("name", b.getName()); //$NON-NLS-1$
        cv.put("mobilizer", b.getMobilizer().getName()); //$NON-NLS-1$
        cv.put("use_builtin_browser", b.isUseBuiltInBrowser() ? 1 : 0); //$NON-NLS-1$
        cv.put("lookup_thumbnail_in_body", b.isLookupImageInBody() ? 1 : 0); //$NON-NLS-1$
        cv.put("max_story_number", b.getMaxNumberOfNews()); //$NON-NLS-1$
        cv.put("force_feed_as_author", b.isForceFeedAsAuthor()); //$NON-NLS-1$
        cv.put("hide_read_stories", b.isHideReadStories()); //$NON-NLS-1$
        cv.put("clear_before_load", b.isClearBeforeLoad()); //$NON-NLS-1$
        cv.put("distribute_evenly", b.shouldDistributeEvenly()); //$NON-NLS-1$
        
        return cv;
    }
    
    @Override
    protected String getTableName()
    {
        return "behaviour"; //$NON-NLS-1$
    }
}
