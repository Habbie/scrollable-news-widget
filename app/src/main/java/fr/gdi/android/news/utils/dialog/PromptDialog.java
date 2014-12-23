package fr.gdi.android.news.utils.dialog;

import fr.gdi.android.news.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * helper for Prompt-Dialog creation
 */
public abstract class PromptDialog extends AlertDialog.Builder implements OnClickListener
{
    private EditText input;
    
    private String message;
    
    public PromptDialog(Context context, int title, int message)
    {
        super(context);
    
        setTitle(title);
        
        this.message = context.getString(message);
        
        init(context);
    }
    
    public PromptDialog(Context context, String title, String message)
    {
        super(context);
       
        setTitle(title);
        
        this.message = message;
        
        init(context);
    }

    private void init(Context context)
    {
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.prompt_dialog, null);
        setView(v);
        
        input = (EditText) v.findViewById(R.id.input);
        
        input.setHint(message);
        
        setPositiveButton(R.string.ok, this);
        setNegativeButton(R.string.cancel, this);
    }
    
    /**
     * will be called when "cancel" pressed. closes the dialog. can be
     * overridden.
     * 
     * @param dialog
     */
    public void onCancelClicked(DialogInterface dialog)
    {
        dialog.dismiss();
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if (which == DialogInterface.BUTTON_POSITIVE)
        {
            if (onOkClicked(input.getText().toString()))
            {
                dialog.dismiss();
            }
        }
        else
        {
            onCancelClicked(dialog);
        }
    }
    
    /**
     * called when "ok" pressed.
     * 
     * @param input
     * @return true, if the dialog should be closed. false, if not.
     */
    abstract public boolean onOkClicked(String input);
}
