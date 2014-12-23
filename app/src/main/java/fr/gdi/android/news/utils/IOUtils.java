package fr.gdi.android.news.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class IOUtils
{
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    
    public static final String EXTERNAL_STORAGE = "/Android/data/fr.gdi.android.news"; //$NON-NLS-1$
    
    public static final String DEFAULT_USER_AGENT = "SNW/2.0"; //$NON-NLS-1$
    
    public static String loadTextAsset(Context context, String path) 
    {
        InputStream is = null;
        try
        {
            AssetManager am = context.getAssets();
            is = am.open(path);
            return IOUtils.toString(is);
        }
        catch ( Exception e ) 
        {
            //swallow
            return null;
        }
        finally 
        {
            IOUtils.close(is);
        }
    }
    
    public static String toString(InputStream is) throws Exception
    {
        if (is != null)
        {
            Writer writer = new StringWriter();
            
            char[] buffer = new char[1024];
            try
            {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //$NON-NLS-1$
                int n;
                while ((n = reader.read(buffer)) != -1)
                {
                    writer.write(buffer, 0, n);
                }
            }
            finally
            {
                is.close();
            }
            return writer.toString();
        }
        else
        {
            return ""; //$NON-NLS-1$
        }
    }
    
    public static byte[] toByteArray(InputStream input) throws IOException 
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }
    
    public static int copy(InputStream input, OutputStream output) throws IOException 
    {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }
    
    public static long copyLarge(InputStream input, OutputStream output) throws IOException 
    {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
    
    public static void close(InputStream is)
    {
        if ( is != null ) 
        {
            try 
            {
                is.close();
            }
            catch ( Exception e ) 
            {
                //swallow
            }
        }
    }
    
    public static void close(OutputStream is)
    {
        if ( is != null ) 
        {
            try 
            {
                is.close();
            }
            catch ( Exception e ) 
            {
                //swallow
            }
        }
    }
    
    public static byte[] getRemoteURLBytes(String urlSpec)
    {
        
        InputStream stream = null;
        try
        {
            stream = getRemoteURLStream(urlSpec);
            return toString(stream).getBytes();
        }
        catch (Exception e)
        {
            Log.e(IOUtils.class.getName(), "Unable to fetch image", e); //$NON-NLS-1$
            return null;
        }
        finally
        {
            IOUtils.close(stream);
        }
    }
    
    public static InputStream getRemoteURLStream(String urlSpec) 
    {
        return getRemoteURLStream(urlSpec, null);
    }
    
    public static InputStream getRemoteURLStream(String urlSpec, String userAgent)
    {
        try
        {
            URL url = new URL(urlSpec);
            URLConnection connection = url.openConnection();
            if ( TextUtils.isEmpty(userAgent) ) connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT); //$NON-NLS-1$
            connection.setUseCaches(true);
            connection.connect();
            
            return connection.getInputStream();
        }
        catch (Exception e)
        {
            Log.e(IOUtils.class.getName(), "Unable to fetch image", e); //$NON-NLS-1$
            return null;
        }
    }
    
    public static File getCacheDir()
    {
        File cacheDir = Environment.getExternalStorageDirectory();
        File folder = new File(cacheDir, EXTERNAL_STORAGE + "/.cache"); //$NON-NLS-1$
        return folder;
    }
    
    public static File getAppDir(Context context)
    {
        return context.getApplicationContext().getFilesDir();
    }
    
    public static Long getFileSize(File folder)
    {
        long foldersize = 0;
        
        File[] filelist = folder.listFiles();
        
        if ( filelist != null ) 
        {
            for (int i = 0; i < filelist.length; i++)
            {
                if (filelist[i].isDirectory())
                {
                    foldersize += getFileSize(filelist[i]);
                }
                else
                {
                    foldersize += filelist[i].length();
                }
            }
        }
        
        return foldersize;
    }
    

    public static int getFileCount(File folder)
    {
        int fileCount = 0;
        
        File[] filelist = folder.listFiles();
        for (int i = 0; i < filelist.length; i++)
        {
            if (filelist[i].isDirectory())
            {
                fileCount += getFileCount(filelist[i]);
            }
            else
            {
                fileCount += 1;
            }
        }
        
        return fileCount;
    }
    
    public static String formatFileSize(Long fileSizeByte)
    {
        if ( fileSizeByte < 1024 ) return fileSizeByte + "B"; //$NON-NLS-1$
        
        DecimalFormat fmt = new DecimalFormat("#.##"); //$NON-NLS-1$
        
        Double fileSizeKB = new Double(fileSizeByte / 1024);
        if ( fileSizeKB < 1024 )
        {
            return fmt.format(fileSizeKB) + "kB"; //$NON-NLS-1$
        }
            
        Double fileSizeMB = new Double(fileSizeKB / 1024);
        return fmt.format(fileSizeMB) + "MB"; //$NON-NLS-1$
    }
    
    public static void delete(File f)
    {
        if (f != null && f.isDirectory())
        {
            File[] children = f.listFiles();
            for (File child : children)
            {
                delete(child);
            }
        }
        
        f.delete();
    }
    
    public static void empty(File f)
    {
        if ( f != null && f.isDirectory() ) 
        {
            File[] children = f.listFiles();
            for (File child : children)
            {
                delete(child);
            }
        }
    }
}
