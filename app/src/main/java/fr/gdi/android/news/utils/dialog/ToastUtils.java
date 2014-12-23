package fr.gdi.android.news.utils.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import fr.gdi.android.news.R;

public class ToastUtils
{
    private static enum MessageType
    {
        ERROR, WARNING, INFO, SUCCESS
    }
    
    public static void showError(Context context, int resId)
    {
        showError(context, context.getText(resId).toString());
    }
    
    public static void showError(Context context, String message)
    {
        showMessage(context, message, MessageType.ERROR);
    }
    
    public static void showWarning(Context context, int resId)
    {
        showWarning(context, context.getText(resId).toString());
    }
    
    public static void showWarning(Context context, String message)
    {
        showMessage(context, message, MessageType.WARNING);
    }
    
    public static void showInfo(Context context, int resId)
    {
        showInfo(context, context.getText(resId).toString());
    }
    
    public static void showInfo(Context context, String message)
    {
        showMessage(context, message, MessageType.INFO);
    }
    
    public static void showSuccess(Context context, int resId)
    {
        showSuccess(context, context.getText(resId).toString());
    }
    
    public static void showSuccess(Context context, String message)
    {
        showMessage(context, message, MessageType.SUCCESS);
    }
    
    private static void showMessage(Context context, String message, MessageType type)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast_error_layout, null, false);
        
        ImageView image = (ImageView) layout.findViewById(R.id.image);
        switch (type)
        {
            case ERROR:
                image.setImageResource(R.drawable.error);
                break;
            case WARNING:
                image.setImageResource(R.drawable.warning);
                break;
            case INFO:
                image.setImageResource(R.drawable.info);
                break;
            case SUCCESS:
                image.setImageResource(R.drawable.success);
                break;
            default:
                break;
        }
        
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);
        
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 15, 15);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
