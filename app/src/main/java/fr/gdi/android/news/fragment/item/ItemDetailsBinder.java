package fr.gdi.android.news.fragment.item;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import fr.gdi.android.news.R;
import fr.gdi.android.news.data.dao.DaoUtils;
import fr.gdi.android.news.data.dao.ItemDao;
import fr.gdi.android.news.model.Item;
import fr.gdi.android.news.model.Mobilizer;
import fr.gdi.android.news.utils.DeviceUtils;

public class ItemDetailsBinder implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    private Activity context;
    
    private View view; 
    
    private WebView webView;
    
    private Item item;
    
    private List<Item> items;
    
    private IItemLoadListener loadListener;
    
    private ItemDao dao;
    
    private ArrayAdapter<Item> adapter; 
    
    private Spinner spinner;
    
    private Mobilizer currentMobilizer = Mobilizer.NONE;
    
    public ItemDetailsBinder(Activity context)
    {
        this.context = context;
        dao = DaoUtils.getItemDao(context);
    }

    public void setLoadListener(IItemLoadListener loadListener)
    {
        this.loadListener = loadListener;
    }
    
    public void setView(View view)
    {
        this.view = view;

        this.webView = (WebView) this.view.findViewById(R.id.item_full);
        configureWebView();
    }
    
    private void configureWebView()
    {
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setLightTouchEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginsEnabled(true);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setBuiltInZoomControls(true);

        if(DeviceUtils.isOnline(context)) //should we parameterize this?
        {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }
        else
        {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)
            {
                context.setProgress(progress * 100);
            }
        });
        
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                Toast.makeText(context, description, Toast.LENGTH_SHORT).show();
            }
            
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                loadListener.startLoading();
            }
            
            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                if ( context != null && item != null ) //sanity check
                {
                    context.setTitle(item.getTitle());
                    if ( loadListener != null ) loadListener.itemLoaded(item);
                }
            }
        });
    }

    public void stopLoading()
    {
        this.webView.stopLoading();
    }
    
    public void bind(long currentId, long[] ids, Mobilizer mobilizer)
    {
        this.items = dao.getItemsById(ids);   
        
        spinner = (Spinner) view.findViewById(R.id.story_select);
        adapter = new ArrayAdapter<Item>(context, android.R.layout.simple_spinner_item, this.items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        
        Button prevBtn = (Button) view.findViewById(R.id.prev);
        Button nextBtn = (Button) view.findViewById(R.id.next);
        
        prevBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        
        setItem(currentId, mobilizer);
    }

    private void setItem(long currentId, Mobilizer mobilizer)
    {
        setItem(dao.getItem(currentId), mobilizer);
    }
    
    private void setItem(Item item, Mobilizer mobilizer)
    {
        this.item = item;
        this.currentMobilizer = mobilizer;
        
        if ( item == null ) return;

        context.setTitle(item.getTitle());
        
        String url = item.getLinkURL();
        
        url =  mobilizer.format(url);
        
        webView.loadUrl(url);
        
        spinner.setOnItemSelectedListener(null);
        spinner.setSelection(items.indexOf(item));
        spinner.setOnItemSelectedListener(this);
    }
    
    @Override
    public void onClick(View v)
    {
        int currentIndex = 0;
        if ( item != null ) 
        {
            currentIndex = items.indexOf(item);
            if ( currentIndex < 0 ) return;
        }
        
        int inc = 0;
        switch (v.getId())
        {
            case R.id.prev:
                if ( currentIndex == 0 )
                {
                    //last item, no next item
                    return; 
                }
                inc = -1;
                break;
            case R.id.next:
                if ( currentIndex == items.size() - 1 ) 
                {
                    //last item, no next item
                    return; 
                }
                inc = 1;
                break;
            default:
                return;
        }
        
        int nextIndex = currentIndex + inc;
        
        setItem(adapter.getItem(nextIndex), currentMobilizer);
    }
    
    @Override
    public void onItemSelected(AdapterView<?> listView, View view, int position, long id)
    {
        Item item = adapter.getItem(position);
        setItem(item.getId(), currentMobilizer);
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        
    }
    
}
