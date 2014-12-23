package fr.gdi.android.news.fragment.theme;

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
import fr.gdi.android.news.activity.FilePickerActivity;
import fr.gdi.android.news.activity.ThemeDetailsActivity;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.ThemeDao;
import fr.gdi.android.news.exceptions.ThemeProbablyInUseException;
import fr.gdi.android.news.model.Theme;
import fr.gdi.android.news.utils.dialog.ToastUtils;

public class ThemeListFragment extends ListFragment implements IImportThemeListener
{
    private static final String THEME_FILE_EXTENSION = "snt";  //$NON-NLS-1$

    private static final String LAST_POSITION_KEY = "lastPosition"; //$NON-NLS-1$
    
    private static final int REQUEST_THEME_DETAILS = 1;
    private static final int REQUEST_EXPORT_ALL = 2;
    private static final int REQUEST_IMPORT_DIALOG = 4;
    
    private static final int CONTEXT_MENU_DETAILS = 0;
    private static final int CONTEXT_MENU_DELETE = 1;
    private static final int CONTEXT_MENU_PREVIEW = 2;
    
    private ArrayAdapter<Theme> adapter;
    
    private int selectedPosition = -1;
    
    private View commandView;
    
    private ThemePrevisualizer previsualizer;
    
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) 
    {
        
        Theme selected = getThemeAt(position);
        
        openThemeDetails(selected);
    }
    
    private Theme getThemeAt(int position)
    {
        position = position - 1; //to account for the header view
        return (Theme) getListAdapter().getItem(position);
    }
    
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) 
    {
         View list_root = inflater.inflate(R.layout.simple_list, null);
         commandView = inflater.inflate(R.layout.theme_list_commands, null);
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
            ThemeDao dao = DaoUtils.getThemeDao(getActivity());
            List<Theme> themes = dao.getAll();
            adapter = new ThemeListAdapter(getActivity(), themes);
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
        
        previsualizer = new ThemePrevisualizer(getActivity());
    }
    
    private Theme getSelectedItem(ContextMenuInfo menuInfo)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        Theme selected = getThemeAt(info.position);
        return selected;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        if (v.getId()== getListView().getId()) 
        {
            Theme selected = getSelectedItem(menuInfo);
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
        Theme selected = getSelectedItem(item.getMenuInfo());
        
        switch ( item.getItemId() )
        {
            case CONTEXT_MENU_DETAILS: 
                openThemeDetails(selected);
                break;
            case CONTEXT_MENU_DELETE:
                ThemeDao dao = DaoUtils.getThemeDao(getActivity());
                try
                {
                    dao.delete(selected.getId()); 
                    adapter.remove(selected);
                    adapter.notifyDataSetChanged();
                }
                catch ( ThemeProbablyInUseException e ) 
                {
                    ToastUtils.showError(getActivity(), R.string.theme_delete_ref);
                }
                break;
            case CONTEXT_MENU_PREVIEW:
                previsualizer.showThemePreview(selected);
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
                openThemeDetails(new Theme(getActivity()));
                break;
            case R.id.import_command:
                intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.START_PATH, "/sdcard"); //$NON-NLS-1$
                intent.putExtra(FilePickerActivity.FORMAT_FILTER, new String[] { THEME_FILE_EXTENSION }); 
                intent.putExtra(FilePickerActivity.SELECTION_MODE, FilePickerActivity.SelectionMode.MODE_CREATE);
                startActivityForResult(intent, REQUEST_IMPORT_DIALOG);
                break;
            case R.id.export_command:
                intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.START_PATH, "/sdcard"); //$NON-NLS-1$
                intent.putExtra(FilePickerActivity.FORMAT_FILTER, new String[] { THEME_FILE_EXTENSION }); 
                intent.putExtra(FilePickerActivity.SELECTION_MODE, FilePickerActivity.SelectionMode.MODE_CREATE);
                startActivityForResult(intent, REQUEST_EXPORT_ALL);
                break;
        }  
    }
    

    private void openThemeDetails(Theme theme)
    {
        Intent intent = new Intent(getActivity(), ThemeDetailsActivity.class);
        intent.putExtra(ThemeDetailsFragment.THEME_KEY, theme);
        startActivityForResult(intent, REQUEST_THEME_DETAILS);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK) 
        {
            if (requestCode == REQUEST_THEME_DETAILS) 
            {
                ThemeDao dao = DaoUtils.getThemeDao(getActivity());
                Theme theme = (Theme) data.getSerializableExtra(ThemeDetailsFragment.THEME_KEY);
                if ( theme.getId() == 0 ) 
                {
                    dao.insert(theme);
                    adapter.add(theme);
                    adapter.notifyDataSetChanged();
                }
                else 
                {
                    dao.update(theme);
                    adapter.remove(theme);
                    adapter.add(theme);
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
                    
                    String extension = "." + THEME_FILE_EXTENSION; //$NON-NLS-1$
                    if ( !filePath.endsWith(extension) )
                    {
                        filePath += extension; 
                    }
                    
                    export(filePath);
                    String message = getActivity().getText(R.string.theme_export_success).toString();
                    ToastUtils.showSuccess(getActivity(), String.format(message, filePath));
                }
                catch (IOException e)
                {
                    Log.e(Constants.PACKAGE, "Unable to export themes", e); //$NON-NLS-1$
                    ToastUtils.showError(getActivity(), R.string.theme_export_failure);
                }
            }
            if (requestCode == REQUEST_IMPORT_DIALOG) 
            {
                String filePath = data.getStringExtra(FilePickerActivity.RESULT_PATH);
                new ThemeImportTask(getActivity(), this).execute(filePath);
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
        ThemeDao dao = DaoUtils.getThemeDao(getActivity());
        List<Theme> themes = dao.getAll();
        for (Theme theme : themes)
        {
            theme.toJson(writer);
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
    public void onThemeImportError(Error err)
    {
        switch (err)
        {
            case NO_THEME_FOUND:
                ToastUtils.showWarning(getActivity(), R.string.theme_import_empty);
                break;
            case FILE_PARSE_ERROR:
                ToastUtils.showError(getActivity(), R.string.theme_import_error);
                break;
        }        
    }
    
    @Override
    public void onThemeSelected(List<Theme> selected)
    {
        ThemeDao dao = DaoUtils.getThemeDao(getActivity());
        
        List<Theme> imported = new ArrayList<Theme>();
        
        int count = adapter.getCount();
        for (Theme theme : selected)
        {
            boolean existing = false;
            for (int u = 0; u < count; u++)
            {
                if ( TextUtils.equals(theme.getName(), adapter.getItem(u).getName()) ) 
                {
                    existing = true;
                    break;
                }
            }
            if ( !existing ) 
            {
                imported.add(theme);
                dao.insert(theme);
            }
        }
        
        boolean warn = imported.size() < selected.size();
        
        if ( imported.size() > 0 ) 
        {
            adapter.addAll(imported);
            adapter.notifyDataSetChanged();
            String msg = getActivity().getText(R.string.theme_import_success).toString();
            if ( !warn ) ToastUtils.showSuccess(getActivity(), msg);
            else 
            {
                ToastUtils.showWarning(getActivity(), msg + " " + getActivity().getText(R.string.name_conflict_import_warning)); //$NON-NLS-1$
            }
        }
        else 
        {
            ToastUtils.showWarning(getActivity(), R.string.theme_import_failure);
        }
    }
}
