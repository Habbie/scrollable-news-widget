package fr.gdi.android.news.fragment.behaviour;

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
import fr.gdi.android.news.fragment.behaviour.IImportBehaviourListener.Error;
import fr.gdi.android.news.model.Behaviour;

public class BehaviourImportTask extends AsyncTask<String, Void, List<Behaviour>> 
{
    private Context context;
    
    private IImportBehaviourListener listener;
    
    
    public BehaviourImportTask(Context context, IImportBehaviourListener listener)
    {
        this.context = context;
        this.listener = listener;
    }
    
    @Override
    protected List<Behaviour> doInBackground(String... params)
    {
        try
        {
            String filePath = params[0];
            
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            JsonReader reader = new JsonReader(fileReader);
            
            final List<Behaviour> behaviours = new ArrayList<Behaviour>();
            
            reader.beginArray();
            while (reader.hasNext()) 
            {
                Behaviour behaviour = new Behaviour(context);
                behaviour.fromJson(reader);
                behaviours.add(behaviour);
            }

            reader.endArray();
            
            reader.close();
            
            return behaviours;
        }
        catch (Exception e)
        {
            Log.e(Constants.PACKAGE, "Unable to read behaviours", e); //$NON-NLS-1$
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(final List<Behaviour> behaviours)
    {
        if ( behaviours == null ) 
        {
            listener.onBehaviourImportError(Error.FILE_PARSE_ERROR);
            return;
        }
        
        final int count = behaviours.size(); 
        if ( count == 0 ) 
        {
            listener.onBehaviourImportError(Error.NO_BEHAVIOUR_FOUND);
            return;
        }
        
        String[] behaviourNames = new String[count];
        for ( int u = 0; u < count; u++ )
        {
            Behaviour behaviour = behaviours.get(u);
            behaviourNames[u] = behaviour.getName();
        }
        
        final boolean[] selection = new boolean[count];
        new AlertDialog.Builder(context)
            .setTitle(R.string.behaviour_import_title)
            .setMultiChoiceItems(behaviourNames, null, new DialogInterface.OnMultiChoiceClickListener() {
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
                    List<Behaviour> selected = new ArrayList<Behaviour>();
                    for (int u = 0; u < count; u++ )
                    {
                        if ( selection[u] ) 
                        {
                            selected.add(behaviours.get(u));
                        }
                    }

                    dialog.dismiss();
                    
                    listener.onBehaviourSelected(selected);
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
