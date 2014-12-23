package fr.gdi.android.news.utils.image;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import fr.gdi.android.news.Constants;
import fr.gdi.android.news.R;
import fr.gdi.android.news.utils.IOUtils;

public class ImageUtils
{
    public static final String FULL = "full"; //$NON-NLS-1$
    
    public static final String DEFAULT_IMAGE_URI = "http://" + Constants.AUTHORITY + "/default"; //$NON-NLS-1$ //$NON-NLS-2$
    
    public static Bitmap downloadImage(String url) 
    {
        Bitmap bm = null;
        try 
        {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } 
        catch (IOException e) 
        {
            Log.e(Constants.PACKAGE, "Error getting bitmap " + e.getClass().getName()); //$NON-NLS-1$
        }
        return bm;
    } 
    
    public static boolean downloadAndCacheFullImage(Context context, String url)
    {
        File f = getImageCacheFile(url, FULL);
        
        Bitmap bmp = null;
        if ( !f.exists() )
        {
            bmp = downloadImage(url);
        
            if ( bmp == null ) 
            {
                //prevent future network requests
                bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_feed_icon);
            }

            if (bmp == null) return false;

            writeBitmap(f, bmp);
        }
        
        return true;
    }
    
    public static File downloadAndCacheImage(Context context, String url, int size)
    {
        File f = getImageCacheFile(url, Integer.toString(size));
        
        if (f.exists())
        {
            return f;
        }
        
        File full = getImageCacheFile(url, FULL);
        
        Bitmap bmp = null;
        if ( !full.exists() )
        {
            bmp = downloadImage(url);
        
            if ( bmp == null ) 
            {
                //prevent future network requests
                bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_feed_icon);
            }

            if (bmp == null) return null;

            writeBitmap(full, bmp);
        }
        else 
        {
            bmp = BitmapFactory.decodeFile(full.getAbsolutePath());
        }

        if ( bmp == null ) 
        {
            //shouldnot happen but this still happens (after the thumbnail cache has been clean) 
            bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_feed_icon);
        }

        return scaleImage(context, bmp, url, size);
    }

    private static void writeBitmap(File f, Bitmap bmp)
    {
        f.getParentFile().mkdirs();
        
        FileOutputStream fos = null;
        
        try 
        {
            fos = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 85, fos);
            fos.flush();
        }
        catch ( Exception e )
        {
            Log.e(Constants.PACKAGE, "Unable to write bitmap to " + f.getAbsolutePath(), e); //$NON-NLS-1$
        }
        finally 
        {
            IOUtils.close(fos);
        }
    }
    
    private static File scaleImage(Context context, Bitmap bmp, String url, int thumbSize)
    {
        int height = bmp.getHeight(), width = bmp.getWidth();
        
        int w = thumbSize, h = height * thumbSize / width;
     
        if ( w == 0 || h == 0 ) 
        {
            bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_feed_icon);
        }
        
        Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, false);

        File f = getImageCacheFile(url, Integer.toString(thumbSize));
        
        writeBitmap(f, scaled);
        
        return f;
    }

    public static void downloadAndCacheImage(final Context context, final String url, final int thumbSize, final IDownloadImageCallback callback)
    {
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... params)
            {
                return downloadAndCacheImage(context, url, thumbSize);
            }
            
            protected void onPostExecute(File f) 
            {
                if ( callback != null ) 
                {
                    if ( f != null && f.exists() ) callback.downloadCompleted(f);
                    else callback.downloadFailed();
                }
            };
        }.execute();
    }
    
    public static boolean isImageDownloaded(String url, String thumbSize)
    {
        return getImageCacheFile(url, thumbSize).exists();
    }
    
    public static File getThumbnailDir()
    {
        File cacheDir = IOUtils.getCacheDir();
        return new File(cacheDir, "images"); //$NON-NLS-1$
    }
    
    public static File getImageCacheFile(String url, String thumbSize)
    {
        String hash = getHash(url);
        File cacheDir = IOUtils.getCacheDir();
        
        File folder = new File(cacheDir, "images/" + thumbSize); //$NON-NLS-1$
        if (!folder.exists()) folder.mkdirs();
        
        File thumb = new File(folder, hash);
        
        return thumb;
    }
    
    private static String getHash(String url)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
            digest.update(url.getBytes());
            return new BigInteger(digest.digest()).toString(16);
        }
        catch (NoSuchAlgorithmException ex)
        {
            return URLEncoder.encode(url);
        }
    }

    public static File getDefaultIconSized(Context context, int thumbSize)
    {
        File folder = new File(IOUtils.getCacheDir(), "images/" + thumbSize); //$NON-NLS-1$
        if (!folder.exists()) folder.mkdirs();
        
        File thumb = new File(folder, "default"); //$NON-NLS-1$
        
        if (thumb.exists())
        {
            return thumb;
        }
        
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_feed_icon);
        
        int height = bmp.getHeight(), width = bmp.getWidth();
        int w = thumbSize, h = height * thumbSize / width;
        
        Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, false);
        writeBitmap(thumb, scaled);
        
        return thumb;
    }

    public static String extractFromText(Context context, String description)
    {
        if ( !TextUtils.isEmpty(description) ) 
        {
            try 
            {
                ImageExtractor extractor = new ImageExtractor(context);
                Html.fromHtml(description, extractor, null);
                return extractor.getImage();
            }
            catch ( Exception e )
            {
                Log.e(Constants.PACKAGE, "Unable to extract image from description", e); //$NON-NLS-1$
            }
        }
        return null;
    }
    
    public static int getColor(int plain, int alpha)
    {
        return Color.argb(alpha, Color.red(plain), Color.green(plain), Color.blue(plain));
    }
}
