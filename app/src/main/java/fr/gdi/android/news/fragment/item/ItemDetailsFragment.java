package fr.gdi.android.news.fragment.item;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.ItemDao;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.model.Mobilizer;
import fr.gdi.android.news.preference.utils.PreferenceUtils;
import fr.gdi.android.news.utils.IHttpCallback;
import fr.gdi.android.news.utils.ShortenUrlTask;
import fr.gdi.android.news.utils.StringUtils;

public class ItemDetailsFragment extends Fragment implements IItemLoadListener
{
    public static final String INITIAL_ITEM_ID_KEY = "initial"; //$NON-NLS-1$
    public static final String INITIAL_URL_KEY = "initial_url";  //$NON-NLS-1$
    public static final String ID_LIST_KEY = "ids"; //$NON-NLS-1$
    public static final String MOBILIZER_KEY = "mobilizer"; //$NON-NLS-1$
    
    private static final int OPTION_MARK_READ = 1;
    private static final int OPTION_MARK_ALL_READ = 16;
    private static final int OPTION_MARK_FAVORITE = 2;
    private static final int OPTION_SHARE = 4;
    private static final int OPTION_STOP_LOADING = 8;
    
    private static final int OPTION_MARK_READ_INDEX = 0,
                                OPTION_MARK_ALL_READ_INDEX = OPTION_MARK_READ_INDEX + 1,
                                OPTION_MARK_FAVORITE_INDEX = OPTION_MARK_ALL_READ_INDEX + 1,
                                OPTION_SHARE_INDEX = OPTION_MARK_FAVORITE_INDEX + 1,
                                OPTION_STOP_LOADING_INDEX = OPTION_SHARE_INDEX + 1;
    
    private ItemDetailsBinder binder;
 
    private ItemDao dao;
    
    private Item currentItem;
 
    private boolean loaded;
    
    private static final SimpleDateFormat SHARE_DATE_FORMAT = new SimpleDateFormat("dd MMM yy, HH:mm"); //$NON-NLS-1$
    
    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        this.binder = new ItemDetailsBinder(activity);
        this.binder.setLoadListener(this);
        this.dao = DaoUtils.getItemDao(getActivity());
        super.onAttach(activity);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true); 
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.add(Menu.NONE, OPTION_MARK_READ, Menu.NONE, R.string.item_mark_read);
        menu.add(Menu.NONE, OPTION_MARK_ALL_READ, Menu.NONE, R.string.item_mark_all_read);
        menu.add(Menu.NONE, OPTION_MARK_FAVORITE, Menu.NONE, R.string.item_add_to_favorites);
        menu.add(Menu.NONE, OPTION_SHARE, Menu.NONE, R.string.item_share_option);
        menu.add(Menu.NONE, OPTION_STOP_LOADING, Menu.NONE, R.string.item_stop_loading);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        menu.getItem(OPTION_MARK_READ_INDEX).setVisible(currentItem != null);
        menu.getItem(OPTION_MARK_ALL_READ_INDEX).setVisible(currentItem != null);
        menu.getItem(OPTION_MARK_FAVORITE_INDEX).setVisible(currentItem != null);
        menu.getItem(OPTION_SHARE_INDEX).setVisible(currentItem != null && !currentItem.isMalformedLink());
        menu.getItem(OPTION_STOP_LOADING_INDEX).setVisible(!loaded);
        
        if ( currentItem != null ) 
        {
            menu.getItem(OPTION_MARK_READ_INDEX).setTitle(currentItem.isRead() ? R.string.item_mark_unread : R.string.item_mark_read);
            menu.getItem(OPTION_MARK_FAVORITE_INDEX).setTitle(!currentItem.isFavorite() ? R.string.item_add_to_favorites : R.string.item_remove_from_favorites);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if ( currentItem != null ) 
        {
            switch ( item.getItemId() ) 
            {
                case OPTION_MARK_READ:
                    this.dao.markRead(currentItem);
                    return true;
                case OPTION_MARK_ALL_READ:
                    Intent intent = getActivity().getIntent();
                    long[] ids = intent.getLongArrayExtra(ID_LIST_KEY);
                    this.dao.markRead(ids);
                    currentItem.setRead(true);
                    return true;
                case OPTION_MARK_FAVORITE:
                    this.dao.markFavorite(currentItem);
                    return true;
                case OPTION_SHARE:
                    shareItem();
                    return true;
                case OPTION_STOP_LOADING:
                    this.binder.stopLoading();
                    return true;
            }
        }
        return false;
    }
    
    private void shareItem()
    {
        if ( currentItem == null || currentItem.isMalformedLink() ) return;
        
        new ShortenUrlTask(getActivity(), new IHttpCallback() {
            @Override
            public void execute(String response)
            {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain"); //$NON-NLS-1$
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, currentItem.getTitle());
                intent.putExtra(android.content.Intent.EXTRA_TEXT, getShareBody(response));
                startActivity(Intent.createChooser(intent, getActivity().getText(R.string.item_share_title)));                
            }
        }).execute(currentItem.getLinkURL());
    }
    
    private String getShareBody(String url)
    {
        StringBuilder bodyBuilder = new StringBuilder();
        
        String NL = System.getProperty("line.separator"); //$NON-NLS-1$
        
        String description = StringUtils.stripTags(currentItem.getDescription());
        
        String date = SHARE_DATE_FORMAT.format(currentItem.getDate());
        
        bodyBuilder.append(currentItem.getTitle()).append(" (") //$NON-NLS-1$
                   .append(date).append("): ").append(NL).append(NL) //$NON-NLS-1$
                   .append(description).append(NL).append(NL)
                   .append(url).append(NL).append(NL)
                   .append(getActivity().getText(R.string.item_source))
                   .append(" ").append(currentItem.getSource().getTitle()); //$NON-NLS-1$
        
        return bodyBuilder.toString();
    }
    @Override
    public void itemLoaded(Item item)
    {
        currentItem = item;
        if ( PreferenceUtils.shouldAutoMarkAsRead(getActivity()) && !item.isRead() )
        {
            dao.markRead(item);
        }
        
        loaded = true;
        
        if ( getActivity() != null )
        {
            //might be null if/when navigating away too quickly
            getActivity().invalidateOptionsMenu();
        }
    }
    
    @Override
    public void startLoading()
    {
        loaded = false;
        
        if ( getActivity() != null )
        {
            //might be null if/when navigating away too quickly
            getActivity().invalidateOptionsMenu();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        View v = inflater.inflate(R.layout.item_details, container, false);
        
        binder.setView(v);

        rebind();
        
        return v;
    }
    
    public void rebind()
    {
        Intent intent = getActivity().getIntent();
        long initial = intent.getLongExtra(INITIAL_ITEM_ID_KEY, -1);
        long[] ids = intent.getLongArrayExtra(ID_LIST_KEY);
        String mobilizerName = intent.getStringExtra(MOBILIZER_KEY);
        Mobilizer mobilizer = Mobilizer.fromName(mobilizerName);
            
        binder.bind(initial, ids, mobilizer);
    }
    

}
