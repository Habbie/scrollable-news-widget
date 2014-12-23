package fr.gdi.android.news.fragment.theme;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.fragment.theme.IImportThemeListener.Error;
import fr.gdi.android.news.model.Theme;

public class ThemeImportTask extends AsyncTask<String, Void, List<Theme>> 
{
    private Context context;
    
    private IImportThemeListener listener;
    
    
    public ThemeImportTask(Context context, IImportThemeListener listener)
    {
        this.context = context;
        this.listener = listener;
    }
    
    @Override
    protected List<Theme> doInBackground(String... params)
    {
        try
        {
            String filePath = params[0];
            
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            JsonReader reader = new JsonReader(fileReader);
            
            final List<Theme> themes = new ArrayList<Theme>();
            
            reader.beginArray();
            while (reader.hasNext()) 
            {
                Theme theme = new Theme(context);
                theme.fromJson(reader);
                themes.add(theme);
            }

            reader.endArray();
            
            reader.close();
            
            return themes;
        }
        catch (Exception e)
        {
            Log.e(Constants.PACKAGE, "Unable to read themes", e); //$NON-NLS-1$
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(final List<Theme> themes)
    {
        if ( themes == null ) 
        {
            listener.onThemeImportError(Error.FILE_PARSE_ERROR);
            return;
        }
        
        final int count = themes.size(); 
        if ( count == 0 ) 
        {
            listener.onThemeImportError(Error.NO_THEME_FOUND);
            return;
        }
        
        String[] themeNames = new String[count];
        for ( int u = 0; u < count; u++ )
        {
            Theme theme = themes.get(u);
            themeNames[u] = theme.getName();
        }
        
        final boolean[] selection = new boolean[count];
        new AlertDialog.Builder(context)
            .setTitle(R.string.theme_import_title)
            .setMultiChoiceItems(themeNames, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked)
                {
                    selection[which] = isChecked;
                }
            })
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    List<Theme> selected = new ArrayList<Theme>();
                    for (int u = 0; u < count; u++ )
                    {
                        if ( selection[u] ) 
                        {
                            selected.add(themes.get(u));
                        }
                    }

                    dialog.dismiss();
                    
                    listener.onThemeSelected(selected);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            })
            .show();
    }
}
