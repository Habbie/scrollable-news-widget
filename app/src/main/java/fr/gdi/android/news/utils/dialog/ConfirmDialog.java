package fr.gdi.android.news.utils.dialog;

import fr.gdi.android.news.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ConfirmDialog
{
    public static final int OK = 1;
    public static final int CANCEL = 2;
    
    private Context context;
    
    private String title, message;
    
    private IConfirmCallback callback;

    private boolean negativeButton = true;
    
    public ConfirmDialog(Context context, String title, String message)
    {
        this.context = context;
        this.title = title;
        this.message = message;
    }

    public void setCallback(IConfirmCallback callback)
    {
        this.callback = callback;
    }
    
    public void setNegativeButton(boolean negativeButton)
    {
        this.negativeButton = negativeButton;
    }
    
    public AlertDialog show() 
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    if ( callback != null ) 
                    {
                        callback.dialogClosed(OK);
                    }
                }
            });
        
        if ( negativeButton ) 
        {
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    if ( callback != null ) 
                    {
                        callback.dialogClosed(CANCEL);
                    }
                }
            });
        }
        
        return builder.show();
    }
    
}
