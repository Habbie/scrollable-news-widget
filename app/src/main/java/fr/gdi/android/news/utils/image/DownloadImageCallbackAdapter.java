package fr.gdi.android.news.utils.image;

public abstract class DownloadImageCallbackAdapter implements IDownloadImageCallback
{
    public void downloadCompleted(java.io.File f) {};
    
    public void downloadFailed() {};
}
