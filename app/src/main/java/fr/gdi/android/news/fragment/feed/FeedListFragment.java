
package fr.gdi.android.news.fragment.feed;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import fr.gdi.android.news.R;
import fr.gdi.android.news.activity.FilePickerActivity;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.FeedDao;
import fr.gdi.android.news.fragment.feed.details.FeedDetailsFragment;
import fr.gdi.android.news.fragment.feed.details.IFeedDetailsCaller;
import fr.gdi.android.news.model.Feed;
import fr.gdi.android.news.utils.dialog.ConfirmDialog;
import fr.gdi.android.news.utils.dialog.IConfirmCallback;
import fr.gdi.android.news.utils.dialog.ToastUtils;
import fr.gdi.android.news.utils.feed.io.ExportFeedResult;
import fr.gdi.android.news.utils.feed.io.ExportFeedTask;
import fr.gdi.android.news.utils.feed.io.IExportFeedListener;
import fr.gdi.android.news.utils.feed.io.IImportFeedListener;
import fr.gdi.android.news.utils.feed.io.ImportFeedResult;
import fr.gdi.android.news.utils.feed.io.ImportFeedResult.State;
import fr.gdi.android.news.utils.feed.io.ImportFeedTask;
import fr.gdi.android.news.utils.feed.load.ConnectFeedTask;
import fr.gdi.android.news.utils.feed.load.FeedSearcher;

public class FeedListFragment extends ListFragment implements IFeedDetailsCaller, ISaveFeedListener, IImportFeedListener, IExportFeedListener
{
    private static final String LAST_POSITION_KEY = "lastPosition"; //$NON-NLS-1$
    private static final int ADD_FEED = 0, DELETE_FEED = 1, CONNECT_TO_FEED = 2;
    
    private static final int OPTION_IMPORT = 1;
    private static final int OPTION_EXPORT = 2;
    private static final int OPTION_REMOVE_ALL = 4;
    
    private static final int REQUEST_SELECT_OPML_FILE = 8;
    private static final int REQUEST_CREATE_OPML_FILE = 16;
    
    private FeedListAdapter adapter;
    
    private int selectedPosition = -1;
    
    private View commandView;

    private FeedSearcher feedSearcher;
    private FeedListDialogs dialogs;
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) 
    {
        Feed selected = getFeedAt(position); 
        if ( v.getId() == R.id.feedImage ) 
        {
            new ConnectFeedTask(getActivity()).execute(selected);
        }
        else 
        {
            FeedDetailsFragment newFragment = FeedDetailsFragment.newInstance(this, selected);
            newFragment.show(getFragmentManager(), "add_dialog");             //$NON-NLS-1$
        }
    }
    
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.feedSearcher = new FeedSearcher(this);
        this.dialogs = new FeedListDialogs(getActivity());
    }
    
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) 
    {
         View list_root = inflater.inflate(R.layout.simple_list, null);
         commandView = inflater.inflate(R.layout.feed_list_commands, null);
         return list_root;
    }
    
    private Feed getSelectedItem(ContextMenuInfo menuInfo)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        Feed selected = getFeedAt(info.position);
        return selected;
    }
    
    private Feed getFeedAt(int position)
    {
        //account for headerView (garbage!)
        return (Feed) getListAdapter().getItem(position - 1);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        ListView listView = getListView();
        listView.addHeaderView(commandView);
        
        setHasOptionsMenu(true); 
        
        if ( adapter == null ) 
        {
            FeedDao dao = DaoUtils.getFeedDao(getActivity());
            adapter = new FeedListAdapter(getActivity(), dao.getAll());
        }
        setListAdapter(adapter);
        
        listView.setCacheColorHint(0);
        
        registerForContextMenu(listView);
        
        if (savedInstanceState != null)
        {
            selectedPosition = savedInstanceState.getInt(LAST_POSITION_KEY, 0);
            if (selectedPosition != -1)
            {
                setSelection(selectedPosition);
                getListView().smoothScrollToPosition(selectedPosition);
            }
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
      if (v.getId()== getListView().getId()) 
      {
          Feed selected = getSelectedItem(menuInfo);
          String title = selected.getTitle(); 
          menu.setHeaderTitle(title); 
          int[] menuItems = new int[] { R.string.details, R.string.delete, R.string.preview };
          for (int i = 0; i<menuItems.length; i++) 
          {
              menu.add(Menu.NONE, i, i, menuItems[i]);
          }
      }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        Feed selected = getSelectedItem(item.getMenuInfo());
        
        switch ( item.getItemId() )
        {
            case ADD_FEED: 
                FeedDetailsFragment newFragment = FeedDetailsFragment.newInstance(this, selected);
                newFragment.show(getFragmentManager(), "add_dialog"); //$NON-NLS-1$
                break;
            case DELETE_FEED:
                FeedDao dao = DaoUtils.getFeedDao(getActivity());
                dao.delete(selected.getId()); 
                adapter.remove(selected);
                adapter.notifyDataSetChanged();
                break;
            case CONNECT_TO_FEED:
                new ConnectFeedTask(getActivity()).execute(selected);
                break;
          
        }
        return super.onContextItemSelected(item);
    }
    
    private synchronized FeedSearcher getFeedSearcher() 
    {
        if ( feedSearcher == null ) 
        {
            this.feedSearcher = new FeedSearcher(this);
        }
        return this.feedSearcher;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //magic strings
        MenuItem item = menu.add(Menu.NONE, OPTION_IMPORT, Menu.NONE, R.string.import_action);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        item.setIcon(R.drawable.arrow_down);
        
        item = menu.add(Menu.NONE, OPTION_EXPORT, Menu.NONE, R.string.export_action);
        item.setIcon(R.drawable.arrow_up);
        
        item = menu.add(Menu.NONE, OPTION_REMOVE_ALL, Menu.NONE, R.string.delete_all);
        item.setIcon(R.drawable.help);        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent = null;
        switch ( item.getItemId() ) 
        {
            case OPTION_IMPORT:
                intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.START_PATH, "/sdcard"); //$NON-NLS-1$
                //intent.putExtra(FilePickerActivity.CAN_SELECT_DIR, true);
                intent.putExtra(FilePickerActivity.FORMAT_FILTER, new String[] { "opml" }); //$NON-NLS-1$
                intent.putExtra(FilePickerActivity.SELECTION_MODE, FilePickerActivity.SelectionMode.MODE_OPEN);
                startActivityForResult(intent, REQUEST_SELECT_OPML_FILE);
                return true;
            case OPTION_EXPORT:
                intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.START_PATH, "/sdcard"); //$NON-NLS-1$
                //intent.putExtra(FilePickerActivity.CAN_SELECT_DIR, true);
                intent.putExtra(FilePickerActivity.FORMAT_FILTER, new String[] { "opml" }); //$NON-NLS-1$
                intent.putExtra(FilePickerActivity.SELECTION_MODE, FilePickerActivity.SelectionMode.MODE_CREATE);
                startActivityForResult(intent, REQUEST_CREATE_OPML_FILE);
                return true;
            case OPTION_REMOVE_ALL:
                ConfirmDialog dialog = new ConfirmDialog(getActivity(), 
                        getActivity().getText(R.string.confirmation).toString(), 
                        getActivity().getText(R.string.feed_delete_confirm).toString()
                        );
                dialog.setCallback(new IConfirmCallback() {
                    @Override
                    public void dialogClosed(int which)
                    {
                        if ( which == ConfirmDialog.OK )
                        {
                            FeedDao dao = DaoUtils.getFeedDao(getActivity());
                            dao.deleteAll();
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                dialog.show();
                return true;
        }
        return false;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK) 
        {
            if (requestCode == REQUEST_SELECT_OPML_FILE) 
            {
                String filePath = data.getStringExtra(FilePickerActivity.RESULT_PATH);
                new ImportFeedTask(getActivity(), this).execute(new File(filePath));
            }
            if (requestCode == REQUEST_CREATE_OPML_FILE) 
            {
                String filePath = data.getStringExtra(FilePickerActivity.RESULT_PATH);
                if ( TextUtils.isEmpty(filePath) ) 
                {
                    //should not happen
                    return;
                }
                
                String extension = ".opml"; //$NON-NLS-1$
                if ( !filePath.endsWith(extension) )
                {
                    filePath += extension; 
                }
                
                new ExportFeedTask(getActivity(), this).execute(new File(filePath));
            }
        }
    }
    
    @Override
    public void onExportFeedResult(ExportFeedResult result)
    {
        if ( result.getState() == fr.gdi.android.news.utils.feed.io.ExportFeedResult.State.SUCCESS ) 
        {
            ToastUtils.showInfo(getActivity(), getActivity().getText(R.string.feed_export_success) + " " + result.getFile().getAbsolutePath()); //$NON-NLS-1$
        }
        else 
        {
            ToastUtils.showInfo(getActivity(), getActivity().getText(R.string.feed_export_failure) + " " + result.getFile().getAbsolutePath()); //$NON-NLS-1$
        }
    }
    
    @Override
    public void onImportFeedResult(ImportFeedResult result)
    {
        State state = result.getState();
        if ( state == State.SUCCESS ) 
        {
            dialogs.showFeedSelectionDialog(result.getFeeds(), new IFeedSelectionListener() {
                @Override
                public void feedSelected(List<Feed> selectedFeeds)
                {
                    FeedDao dao = DaoUtils.getFeedDao(getActivity());
                    dao.insert(selectedFeeds);
                    adapter.addAll(selectedFeeds);
                    adapter.notifyDataSetChanged();
                    
                    dialogs.showFeedImportedDialog(selectedFeeds);
                }
            });
        }
        else 
        {
            ToastUtils.showWarning(getActivity(), state.toString(getActivity()));
        }
    }
    
    public void feedDetailsCanceled() 
    {
        
    }
    
    public void feedDetailsClosed(Feed feed, Feed originalFeed)
    {
        new SaveFeedTask(getActivity(), this).execute(feed, originalFeed);
    }
    
    public void saveFeedCallback(SaveFeedResult result) 
    {
        if ( result.isSuccess()  )
        {
            adapter.setNotifyOnChange(false);
            Feed feed = result.getFeed();
            if ( !result.isNewFeed() )
            {
                adapter.remove(feed);
            }
            adapter.add(feed);
            adapter.setNotifyOnChange(true);
            adapter.notifyDataSetChanged();
        }
        else 
        {
            new AlertDialog.Builder(getActivity())
                .setMessage(R.string.feed_export_error)
                .setTitle(R.string.error)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
        }
    }
    
    @Override
    public void onDestroyView()
    {
        setListAdapter(null);
        super.onDestroyView();
    }
    
    public void onCommandSelected(View v) 
    {
        switch(v.getId())
        {
            case  R.id.add:
                FeedDetailsFragment newFragment = FeedDetailsFragment.newInstance(this, null);
                newFragment.show(getFragmentManager(), "add_dialog"); //$NON-NLS-1$
                break;
            case R.id.search:
                getFeedSearcher().search();
                break;
            case R.id.lookup:
                getFeedSearcher().lookup();
                break;
        }  
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        selectedPosition = getListView().getFirstVisiblePosition();
        outState.putInt(LAST_POSITION_KEY, selectedPosition);
    }
}
