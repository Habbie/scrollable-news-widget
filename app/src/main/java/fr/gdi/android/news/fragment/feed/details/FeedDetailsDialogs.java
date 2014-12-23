package fr.gdi.android.news.fragment.feed.details;

import fr.gdi.android.news.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class FeedDetailsDialogs
{
    private Context context;
    
    public FeedDetailsDialogs(Context context)
    {
        this.context = context;
    }
    
    AlertDialog showInvalidUrlDialog(int titleId, int messageId, final String url)
    {
        String message = context.getText(messageId) + " " + context.getText(R.string.feed_open_in_browser); //$NON-NLS-1$
        return new AlertDialog.Builder(context)
                                .setTitle(titleId)
                                .setMessage(message) 
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(url));
                                        context.startActivity(i);
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
    }
    
    AlertDialog showFetchDialog()
    {
        return new AlertDialog.Builder(context)
                                .setMessage(R.string.feed_validating_message)
                                .setTitle(R.string.feed_validating_title)
                                .setCancelable(false)
                                .show();
    }
   
    AlertDialog showEmptyUrlDialog()
    {
        return new AlertDialog.Builder(context)
                                .setMessage(R.string.feed_missing_url_message)
                                .setTitle(R.string.feed_missing_url_title)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                    }
                                }).show();
    }
}
