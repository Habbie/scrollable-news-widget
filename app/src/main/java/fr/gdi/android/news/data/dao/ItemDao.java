package fr.gdi.android.news.data.dao;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.data.DatabaseHelper;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.utils.image.ImageUtils;

public class ItemDao extends AbstractDao<Item>
{
    private FeedDao feedDao;
    
    public ItemDao(DatabaseHelper database)
    {
        super(database);
    }

    public void setFeedDao(FeedDao feedDao)
    {
        this.feedDao = feedDao;
    }
    
    public long[] save(List<Item> items)
    {
        long[] ids = new long[items.size()];
        for (int u= 0; u < items.size(); u++)
        {
            Item item = items.get(u);
            ids[u] = insert(item);
        }
        return ids;
    }
    
    public long insert(Item item) 
    {
        try 
        {
            ContentValues values = new ContentValues();
            values.put("feed_id", item.getSource().getId()); //$NON-NLS-1$
            values.put("url", item.getLinkURL());  //$NON-NLS-1$
            values.put("guid", item.getGuid()); //$NON-NLS-1$
            values.put("title", item.getTitle()); //$NON-NLS-1$
            values.put("author", item.getAuthor()); //$NON-NLS-1$
            
            Long pubDate = item.getPubdate() != null ? item.getPubdate().getTime() : null;
            
            if ( pubDate != null ) values.put("pub_date", pubDate);  //$NON-NLS-1$
            if ( item.getUpdateDate() != null ) values.put("update_date", item.getUpdateDate().getTime()); //$NON-NLS-1$
            else if ( pubDate != null ) values.put("update_date", pubDate); //$NON-NLS-1$
            
            values.put("image_url", item.getImage() != null ? item.getImage().toString() : null); //$NON-NLS-1$
            values.put("original_source", item.getOriginalSource()); //$NON-NLS-1$
            values.put("original_author", item.getOriginalAuthor()); //$NON-NLS-1$
            values.put("read", item.isRead() ? 1 : 0);  //$NON-NLS-1$
            values.put("favorite", item.isFavorite()); //$NON-NLS-1$
            
            String description = item.getDescription();
            if ( TextUtils.isEmpty(description) ) 
            {
                //use content if description is empty
                //this might prove rather lengthy but
                //hopefully this should be exceptional 
                description = item.getContent(); 
            }
            values.put("description", description); //$NON-NLS-1$
    
            long id = database.getWritableDatabase().insert("item", null, values); //$NON-NLS-1$
            
            item.setId(id);
            
            return id;
        }
        catch ( Exception e ) 
        {
            Log.e(Constants.PACKAGE, "Error inserting item", e); //$NON-NLS-1$
            return -1;
        }
    }
    
    public List<Item> getItems(int limit, long... feedIds) 
    {
        return getItems(limit, true, false, feedIds);
    }
    
    public List<Item> getItems(int limit, boolean unreadOnly, boolean distribute, long... feedIds) 
    {
        List<Item> items = new ArrayList<Item>();
        String uo = unreadOnly ? " (i.read is null or i.read=0) and " : ""; //$NON-NLS-1$ //$NON-NLS-2$
      
        String sql = "select " + //$NON-NLS-1$
                " i._id, i.feed_id, i.url, i.guid, i.title, i.author, i.pub_date, i.update_date, " + //$NON-NLS-1$
                " i.description, i.image_url, i.original_source, i.original_author, i.read, i.favorite " + //$NON-NLS-1$
                " from item i " + //$NON-NLS-1$
                " where "; //$NON-NLS-1$
        
        if ( distribute ) 
        {
                sql += " ( " + //$NON-NLS-1$
                "   select count(*) from item as i1 " + //$NON-NLS-1$
                "   where i.feed_id = i1.feed_id and i1.pub_date > i.pub_date " +  //$NON-NLS-1$
                "   order by i1.update_date desc, i1.pub_date desc, i1.oid desc " +   //$NON-NLS-1$
                " ) <= " + (((int) limit/feedIds.length) - 1) + " and "; //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        sql += uo + " i.feed_id in ("; //$NON-NLS-1$ 
        
        Object[] params = new Object[feedIds.length]; 
        for (int u = 0; u < feedIds.length; u++ )
        {
            if ( u != 0 ) sql += ", "; //$NON-NLS-1$
            sql += "?"; //$NON-NLS-1$
            params[u] = feedIds[u];
        }
        
        sql += ") order by update_date desc, pub_date desc, _id desc limit " + limit; //$NON-NLS-1$
      
        
        Cursor cursor = null;
        try
        {
            cursor = database.executeQuery(sql, params);
            while ( cursor.moveToNext() ) 
            {
               items.add(getItem(cursor));
            }
            return items;
        }
        finally
        {
            if ( cursor != null ) cursor.close();
        }
    }

    public List<Item> getItemsById(long... itemIds) 
    {
        List<Item> items = new ArrayList<Item>();
        String sql = "select " + //$NON-NLS-1$
                " _id, feed_id, url, guid, title, author, pub_date, update_date, " + //$NON-NLS-1$
                " description, image_url, original_source, original_author, read, favorite " + //$NON-NLS-1$
                " from item " + //$NON-NLS-1$
                " where _id in ("; //$NON-NLS-1$
        
        Object[] params = new Object[itemIds.length]; 
        for (int u = 0; u < itemIds.length; u++ )
        {
            if ( u != 0 ) sql += ", "; //$NON-NLS-1$
            sql += "?"; //$NON-NLS-1$
            params[u] = itemIds[u];
        }
        
        
        sql += ") order by update_date desc, pub_date desc, _id desc "; //$NON-NLS-1$
        
        Cursor cursor = null;
        try
        {
            cursor = database.executeQuery(sql, params);
            while ( cursor.moveToNext() ) 
            {
               items.add(getItem(cursor));
            }
            return items;
        }
        finally
        {
            if ( cursor != null ) cursor.close();
        }
    }
    
    public Item getItem(long itemId)
    {
        return get("where _id=?", new Object[] { itemId }); //$NON-NLS-1$
    }
    
    public Item getItem(String link)
    {
        return get("where url=?", new Object[] { link }); //$NON-NLS-1$
    }
    
    private Item get(String whereClause, Object[] params)
    {
        Item item = null;
        Cursor cursor = null;
        
        try
        {
            String sql = "select " + //$NON-NLS-1$
                         " _id, feed_id, url, guid, title, author, pub_date, update_date, " + //$NON-NLS-1$
                         " description, image_url, original_source, original_author, read, favorite " + //$NON-NLS-1$
                         " from item " + //$NON-NLS-1$
                         whereClause;
            cursor = database.executeQuery(sql, params);
            if ( cursor.moveToNext() ) 
            {
               item = getItem(cursor);
            }
            return item;
        }
        finally
        {
            if ( cursor != null ) cursor.close();
        }
    }
    
    
    //magic numbers
    private Item getItem(Cursor cursor) 
    {
        Item item = new Item();
        item.setId(cursor.getLong(0));
        item.setSource(feedDao.getById(cursor.getLong(1)));
        
        String url = cursor.getString(2);
        try
        {
            item.setLink(new URL(url));
        }
        catch (MalformedURLException e)
        {
            Log.e(Constants.PACKAGE, "Unable to parse back item url: " + url, e); //$NON-NLS-1$
            try
            {
                item.setLink(new URL(Constants.MALFORMED_ITEM_LINK_URL));
            }
            catch (MalformedURLException e1)
            {
                //swallow
            }
        }
        
        item.setGuid(cursor.getString(3));
        item.setTitle(cursor.getString(4));
        item.setAuthor(cursor.getString(5));
        
        item.setPubdate(getDate(cursor, 6));
        item.setUpdateDate(getDate(cursor, 7));
        
        item.setDescription(cursor.getString(8));
        
        String imageUrl = cursor.getString(9);
        try 
        {
            if ( !TextUtils.isEmpty(imageUrl) ) item.setImage(new URL(imageUrl));
        }
        catch (MalformedURLException e)
        {
            Log.e(Constants.PACKAGE, "Unable to parse back item image url: " + url, e); //$NON-NLS-1$
        }
        
        item.setOriginalSource(cursor.getString(10));
        item.setOriginalAuthor(cursor.getString(11));
        
        item.setRead(getBoolean(cursor, 12));
        item.setFavorite(getBoolean(cursor, 13));
        
        return item;
    }

    public void markRead(Item item)
    {
        boolean b = !item.isRead();
        database.executeUpdate("update item set read=? where _id=?", new Object[] { b ? 1 : 0, item.getId() }); //$NON-NLS-1$
        item.setRead(b);
    }

    public void markRead(long[] ids)
    {
        String placeHolders = ""; //$NON-NLS-1$
        Object[] params = new Object[ids.length];
        for ( int u = 0; u < ids.length; u++ ) 
        {
            placeHolders += (u != 0 ? ", " : "") + "? "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            params[u] = ids[u];
        }
        database.executeUpdate("update item set read=1 where _id in (" + placeHolders + ")", params); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void updateImage(Item item)
    {
        database.executeUpdate("update item set image_url=? where _id=?", new Object[] { item.getImage().toString(), item.getId() }); //$NON-NLS-1$
    }
    
    @Override
    protected String getTableName()
    {
        return "item"; //$NON-NLS-1$
    }
    
    public void markFavorite(Item item)
    {
        boolean b = !item.isFavorite();
        database.executeUpdate("update item set favorite=? where _id=?", new Object[] { b ? 1 : 0, item.getId() }); //$NON-NLS-1$
        item.setFavorite(b);
    }

    public List<Item> getFavorites()
    {
        List<Item> items = new ArrayList<Item>();
        
        String sql = "select " + //$NON-NLS-1$
            " _id, feed_id, url, guid, title, author, pub_date, update_date, " + //$NON-NLS-1$
            " description, image_url, original_source, original_author, read, favorite " + //$NON-NLS-1$
            " from item where favorite=? " + //$NON-NLS-1$
            " order by update_date desc, pub_date desc, _id desc"; //$NON-NLS-1$
        
        Object[] params = new Object[] { 1 }; 
        
        Cursor cursor = null;
        try
        {
            cursor = database.executeQuery(sql, params);
            while ( cursor.moveToNext() ) 
            {
               items.add(getItem(cursor));
            }
        }
        catch ( Exception e ) 
        {
            Log.e(Constants.PACKAGE, "Unable to load favorites", e); //$NON-NLS-1$
        }
        finally
        {
            if ( cursor != null ) cursor.close();
        }

        return items;
    }
    
    public void setDefaultImage(Item item)
    {
        try 
        {
            if ( item.getId() > 0 )
            {
                item.setImage(new URL(ImageUtils.DEFAULT_IMAGE_URI));
                updateImage(item);
            }
        }
        catch ( Exception e ) 
        {
            Log.w(Constants.PACKAGE, "Couldnot set default image Image URI", e); //$NON-NLS-1$
        }
    }

    public void clear()
    {
        feedDao.resetAllDates();
        database.executeUpdate("delete from item", new Object[0]); //$NON-NLS-1$
    }
    
    public void deleteFeedItems(boolean keepFavorites, Feed... feeds) 
    {
        StringBuilder whereClause = new StringBuilder("where feed_id in ("); //$NON-NLS-1$
        Object[] params = new Object[feeds.length];
        for (int u = 0; u < feeds.length; u++)
        {
            Feed feed = feeds[u];
            if ( u > 0 ) whereClause.append(", ");  //$NON-NLS-1$
            whereClause.append("?"); //$NON-NLS-1$
            params[u] = feed.getId();
        }
        whereClause.append(") "); //$NON-NLS-1$
        
        if ( keepFavorites ) 
        {
            whereClause.append(" and (favorite is null or favorite = 0)"); //$NON-NLS-1$
        }
        
        database.executeUpdate("delete from item " + whereClause, params); //$NON-NLS-1$
    }
    
}
