package fr.gdi.android.news.utils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;

public class CharsetDetector
{
    
    public static Charset detectCharset(byte[] bytes)
    {
        Map<String, Charset> charsets = Charset.availableCharsets();
        return detectCharset(bytes, charsets.keySet().toArray(new String[charsets.size()]));
    }
    
    public static Charset detectCharset(byte[] bytes, String[] charsets)
    {
        
        Charset charset = null;
        
        for (String charsetName : charsets)
        {
            charset = detectCharset(bytes, Charset.forName(charsetName));
            if (charset != null)
            {
                break;
            }
        }
        
        return charset;
    }
    
    private static Charset detectCharset(byte[] bytes, Charset charset)
    {
        try
        {
            CharsetDecoder decoder = charset.newDecoder();
            decoder.reset();
            
            boolean identified = identify(bytes, decoder);
            
            if (identified)
            {
                return charset;
            }
            else
            {
                return null;
            }
            
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    private static boolean identify(byte[] bytes, CharsetDecoder decoder)
    {
        try
        {
            decoder.decode(ByteBuffer.wrap(bytes));
        }
        catch (CharacterCodingException e)
        {
            return false;
        }
        return true;
    }
    
    private CharsetDetector()
    {
        
    }
}