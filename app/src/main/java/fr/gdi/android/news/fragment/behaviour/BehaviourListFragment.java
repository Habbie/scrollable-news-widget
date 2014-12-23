package fr.gdi.android.news.fragment.behaviour;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.JsonWriter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.activity.BehaviourDetailsActivity;
import fr.gdi.android.news.activity.FilePickerActivity;
import fr.gdi.android.news.data.dao.BehaviourDao;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.exceptions.BehaviourProbablyInUseException;
import fr.gdi.android.news.model.Behaviour;
import fr.gdi.android.news.utils.dialog.ToastUtils;

public class BehaviourListFragment extends ListFragment implements IImportBehaviourListener
{
    private static final String BEHAVIOUR_FILE_EXTENSION = "snb"; //$NON-NLS-1$
    
    private static final String LAST_POSITION_KEY = "lastPosition";   //$NON-NLS-1$
    
    private static final int CONTEXT_MENU_DETAILS = 0;
    private static final int CONTEXT_MENU_DELETE = 1;
    
    private static final int REQUEST_BEHAVIOUR_DETAILS = 1;
    private static final int REQUEST_EXPORT_ALL = 2;
    private static final int REQUEST_IMPORT_DIALOG = 4;
    
    private ArrayAdapter<Behaviour> adapter;
    
    private int selectedPosition = -1;
    
    private View commandView;
    
    
    
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) 
    {
        Behaviour selected = getBehaviourAt(position);
        
        openBehaviourDetails(selected);
    }
    
    private Behaviour getBehaviourAt(int position)
    {
        position = position - 1; //to account for the header view
        return (Behaviour) getListAdapter().getItem(position);
    }
    
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) 
    {
         View list_root = inflater.inflate(R.layout.simple_list, null);
         commandView = inflater.inflate(R.layout.behaviour_list_commands, null);
         return list_root;
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
            BehaviourDao dao = DaoUtils.getBehaviourDao(getActivity());
            List<Behaviour> behaviours = dao.getAll();
            adapter = new BehaviourListAdapter(getActivity(), behaviours);
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
    
    private Behaviour getSelectedItem(ContextMenuInfo menuInfo)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        Behaviour selected = getBehaviourAt(info.position);
        return selected;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        if (v.getId()== getListView().getId()) 
        {
            Behaviour selected = getSelectedItem(menuInfo);
            String title = selected.getName(); 
            menu.setHeaderTitle(title); 
            int[] menuItems = new int[] { R.string.details, R.string.delete }; 
            for (int i = 0; i<menuItems.length; i++) 
            {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        Behaviour selected = getSelectedItem(item.getMenuInfo());
        
        switch ( item.getItemId() )
        {
            case CONTEXT_MENU_DETAILS: 
                openBehaviourDetails(selected);
                break;
            case CONTEXT_MENU_DELETE:
                BehaviourDao dao = DaoUtils.getBehaviourDao(getActivity());
                try
                {
                    dao.delete(selected.getId()); 
                    adapter.remove(selected);
                    adapter.notifyDataSetChanged();
                }
                catch ( BehaviourProbablyInUseException e ) 
                {
                    ToastUtils.showError(getActivity(), R.string.behaviour_delete_ref);
                }
                break;
        }
        return super.onContextItemSelected(item);
    }
    
    public void onCommandSelected(View v) 
    {
        Intent intent = null;
        switch(v.getId())
        {
            case  R.id.add_command:
                openBehaviourDetails(new Behaviour(getActivity()));
                break;
            case R.id.import_command:
                intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.START_PATH, "/sdcard"); //$NON-NLS-1$
                intent.putExtra(FilePickerActivity.FORMAT_FILTER, new String[] { BEHAVIOUR_FILE_EXTENSION }); 
                intent.putExtra(FilePickerActivity.SELECTION_MODE, FilePickerActivity.SelectionMode.MODE_CREATE);
                startActivityForResult(intent, REQUEST_IMPORT_DIALOG);
                break;
            case R.id.export_command:
                intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.START_PATH, "/sdcard"); //$NON-NLS-1$
                intent.putExtra(FilePickerActivity.FORMAT_FILTER, new String[] { BEHAVIOUR_FILE_EXTENSION }); //$NON-NLS-1$
                intent.putExtra(FilePickerActivity.SELECTION_MODE, FilePickerActivity.SelectionMode.MODE_CREATE);
                startActivityForResult(intent, REQUEST_EXPORT_ALL);
                break;
        }  
    }

    private void openBehaviourDetails(Behaviour behaviour)
    {
        Intent intent = new Intent(getActivity(), BehaviourDetailsActivity.class);
        intent.putExtra(BehaviourDetailsFragment.BEHAVIOUR_KEY, behaviour);
        startActivityForResult(intent, REQUEST_BEHAVIOUR_DETAILS);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK) 
        {
            if (requestCode == REQUEST_BEHAVIOUR_DETAILS) 
            {
                BehaviourDao dao = DaoUtils.getBehaviourDao(getActivity());
                Behaviour behaviour = (Behaviour) data.getSerializableExtra(BehaviourDetailsFragment.BEHAVIOUR_KEY);
                if ( behaviour.getId() == 0 ) 
                {
                    dao.insert(behaviour);
                    adapter.add(behaviour);
                    adapter.notifyDataSetChanged();
                }
                else 
                {
                    dao.update(behaviour);
                    adapter.remove(behaviour);
                    adapter.add(behaviour);
                    adapter.notifyDataSetChanged();
                }
            }
            if (requestCode == REQUEST_EXPORT_ALL) 
            {
                String filePath = data.getStringExtra(FilePickerActivity.RESULT_PATH);
                try
                {
                    if ( TextUtils.isEmpty(filePath) ) 
                    {
                        //should not happen
                        return;
                    }
                    
                    String extension = "." + BEHAVIOUR_FILE_EXTENSION; //$NON-NLS-1$
                    if ( !filePath.endsWith(extension) )
                    {
                        filePath += extension; 
                    }
                    
                    export(filePath);
                    String message = getActivity().getText(R.string.behaviour_export_success).toString();
                    ToastUtils.showSuccess(getActivity(), String.format(message, filePath));
                }
                catch (IOException e)
                {
                    Log.e(Constants.PACKAGE, "Unable to export themes", e); //$NON-NLS-1$
                    ToastUtils.showError(getActivity(), R.string.behaviour_export_failure);
                }
            }
            if (requestCode == REQUEST_IMPORT_DIALOG) 
            {
                String filePath = data.getStringExtra(FilePickerActivity.RESULT_PATH);
                new BehaviourImportTask(getActivity(), this).execute(filePath);
            }
        }
    }
    
    private void export(String filePath) throws IOException
    {
        File file = new File(filePath);
        FileWriter fileWriter = new FileWriter(file);
        JsonWriter writer = new JsonWriter(fileWriter);
        writer.setIndent("    "); //$NON-NLS-1$
        
        writer.beginArray();
        BehaviourDao dao = DaoUtils.getBehaviourDao(getActivity());
        List<Behaviour> behaviours = dao.getAll();
        for (Behaviour behaviour : behaviours)
        {
            behaviour.toJson(writer);
        }
        writer.endArray();
        
        writer.close();
    }
    
    @Override
    public void onDestroyView()
    {
        setListAdapter(null);
        super.onDestroyView();
    }

    @Override
    public void onBehaviourSelected(List<Behaviour> selected)
    {
        BehaviourDao dao = DaoUtils.getBehaviourDao(getActivity());
        
        List<Behaviour> imported = new ArrayList<Behaviour>();
        
        int count = adapter.getCount();
        for (Behaviour behaviour : selected)
        {
            boolean existing = false;
            for (int u = 0; u < count; u++)
            {
                if ( TextUtils.equals(behaviour.getName(), adapter.getItem(u).getName()) ) 
                {
                    existing = true;
                    break;
                }
            }
            if ( !existing ) 
            {
                imported.add(behaviour);
                dao.insert(behaviour);
            }
        }
        
        boolean warn = imported.size() < selected.size();
        
        if ( imported.size() > 0 ) 
        {
            adapter.addAll(imported);
            adapter.notifyDataSetChanged();
            String msg = getActivity().getText(R.string.behaviour_import_success).toString();
            if ( !warn ) ToastUtils.showSuccess(getActivity(), msg);
            else 
            {
                String warning = getActivity().getText(R.string.name_conflict_import_warning).toString();
                ToastUtils.showWarning(getActivity(), msg + " " + warning); //$NON-NLS-1$
            }
        }
        else 
        {
            ToastUtils.showWarning(getActivity(), R.string.behaviour_import_failure);
        }
    }
    
    @Override
    public void onBehaviourImportError(Error err)
    {
        switch (err)
        {
            case NO_BEHAVIOUR_FOUND:
                ToastUtils.showWarning(getActivity(), R.string.behaviour_import_empty);
                break;
            case FILE_PARSE_ERROR:
                ToastUtils.showError(getActivity(), R.string.behaviour_import_error);
                break;
        }
    }
}
