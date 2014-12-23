package fr.gdi.android.news.utils.feed.io;

import java.io.File;

public class ExportFeedResult
{
    public static enum State 
    {
        SUCCESS, ERROR;
    }
    
    private State state;
    
    private File file;
    
    public ExportFeedResult(File file, State state)
    {
        this.state = state;
        this.file = file;
    }
    
    public State getState()
    {
        return state;
    }
    
    public File getFile()
    {
        return file;
    }
}
