package fr.gdi.android.news.fragment.help;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;

public class HelpFragment extends Fragment
{
     
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.help, null);
        
        String version = getVersionName(); 
        ((TextView) v.findViewById(R.id.title)).setText(getActivity().getText(R.string.app_name) + " " + version); //$NON-NLS-1$
        
        TextView textView = (TextView) v.findViewById(R.id.source);
        String html = getActivity().getText(R.string.help_source).toString();
        textView.setText(Html.fromHtml(html));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        
        return v;
    }
    
    private String getVersionName() 
    {
        try
        {
            Context ctx = getActivity(); 
            PackageInfo pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return pi.versionName;
        }
        catch (NameNotFoundException e)
        {
            Log.w(Constants.PACKAGE, "Cannot retrieve version name", e); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
    }
}