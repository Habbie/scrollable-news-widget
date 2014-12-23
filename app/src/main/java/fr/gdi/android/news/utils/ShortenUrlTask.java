package fr.gdi.android.news.utils;

import java.util.LinkedList;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import fr.gdi.android.news.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
 
public class ShortenUrlTask extends AsyncTask<String, Void, String> 
{
 
    private ProgressDialog postDialog;
    
    private Context context;
    
    private HttpClient httpClient;
    
    private IHttpCallback callback;
    
    public ShortenUrlTask(Context context, IHttpCallback iHttpCallback)
    {
        this.context = context;
        this.callback = iHttpCallback;
        
        HttpParams parameters = new BasicHttpParams();
        HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(parameters, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(parameters, false);
        HttpConnectionParams.setTcpNoDelay(parameters, true);
        HttpConnectionParams.setSocketBufferSize(parameters, 8192);
        
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80)); //$NON-NLS-1$
        ClientConnectionManager tsccm = new ThreadSafeClientConnManager(parameters, schReg);
        httpClient = new DefaultHttpClient(tsccm, parameters);
    }
    
    @Override
    protected void onPreExecute() 
    {
        postDialog = new ProgressDialog(context);
        postDialog.setTitle(R.string.utils_shorten_url_title);
        postDialog.setMessage(context.getText(R.string.utils_shorten_url_message));
        postDialog.setCancelable(true);
        postDialog.setIndeterminate(true);
        
        postDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0)
            {
                ShortenUrlTask.this.cancel(true);
            }
        });
        
        postDialog.show();
    }
    
    @Override
    protected void onCancelled()
    {
        super.onCancelled();
    }
    
    @Override
    protected String doInBackground(String... params) 
    {
        String response = null;
        try 
        {
            HttpPost post = new HttpPost("http://urly.de/api"); //$NON-NLS-1$
            LinkedList<BasicNameValuePair> out = new LinkedList<BasicNameValuePair>();
            out.add(new BasicNameValuePair("url", params[0])); //$NON-NLS-1$
            post.setEntity(new UrlEncodedFormEntity(out, HTTP.UTF_8));
            post.setParams(getParams());
            response = httpClient.execute(post, new BasicResponseHandler());
            
        } 
        catch (Exception e) 
        {
            Log.e(fr.gdi.android.news.Constants.PACKAGE, "Unable to shorten link", e); //$NON-NLS-1$
        } 
        return response;
    }
    
    protected void onPostExecute(String s) 
    {
        postDialog.dismiss();
        if ( callback != null ) 
        {
            callback.execute(s);
        }
    }
    
    public HttpParams getParams() 
    {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setUseExpectContinue(params, false);
        return params;
    }
}