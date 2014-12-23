package fr.gdi.android.news.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

/* 
 * Extracted from Rome parser
 */
public class EncodingUtils
{
    
    @SuppressWarnings("serial")
    static class EncodingException extends RuntimeException
    {
        String _bomEncoding, _xmlGuessEncoding, _xmlEncoding, _contentTypeMime, _contentTypeEncoding;
        
        EncodingException(String msg, String bomEnc, String xmlGuessEnc, String xmlEnc, InputStream is)
        {
            this(msg, null, null, bomEnc, xmlGuessEnc, xmlEnc);
        }
        
        EncodingException(String msg, String ctMime, String ctEnc, String bomEnc, String xmlGuessEnc, String xmlEnc)
        {
            super(msg);
            _contentTypeMime = ctMime;
            _contentTypeEncoding = ctEnc;
            _bomEncoding = bomEnc;
            _xmlGuessEncoding = xmlGuessEnc;
            _xmlEncoding = xmlEnc;
        }
    }
    
    private static final int BUFFER_SIZE = 4096;
    
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
    private static final String US_ASCII = "US-ASCII"; //$NON-NLS-1$
    private static final String UTF_16BE = "UTF-16BE"; //$NON-NLS-1$
    private static final String UTF_16LE = "UTF-16LE"; //$NON-NLS-1$
    private static final String UTF_16 = "UTF-16"; //$NON-NLS-1$
    
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=([.[^; ]]*)"); //$NON-NLS-1$
    
    private static final Pattern ENCODING_PATTERN = Pattern.compile("<\\?xml.*encoding[\\s]*=[\\s]*((?:\".[^\"]*\")|(?:'.[^']*'))", Pattern.MULTILINE); //$NON-NLS-1$
    
    private static final MessageFormat RAW_EX_1 = new MessageFormat("Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] encoding mismatch"); //$NON-NLS-1$
    
    private static final MessageFormat RAW_EX_2 = new MessageFormat("Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] unknown BOM"); //$NON-NLS-1$
    
    private static final MessageFormat HTTP_EX_1 = new MessageFormat(
            "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], BOM must be NULL"); //$NON-NLS-1$
    
    private static final MessageFormat HTTP_EX_2 = new MessageFormat(
            "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], encoding mismatch"); //$NON-NLS-1$
    
    private static final MessageFormat HTTP_EX_3 = new MessageFormat(
            "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], Invalid MIME"); //$NON-NLS-1$
    
    public static String getEncoding(HttpURLConnection connection, byte[] bytes) throws IOException
    {
        String httpContentType = connection.getContentType();
        BufferedInputStream pis = new BufferedInputStream(new ByteArrayInputStream(bytes), BUFFER_SIZE);
        String cTMime = getContentTypeMime(httpContentType);
        String cTEnc = getContentTypeEncoding(httpContentType);
        String bomEnc = getBOMEncoding(pis);
        String xmlGuessEnc = getXMLGuessEncoding(pis);
        String xmlEnc = getXmlPrologEncoding(pis, xmlGuessEnc);
        String encoding = calculateHttpEncoding(cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc, pis, true);
        
        return encoding;
    }
    
    private static String calculateRawEncoding(String bomEnc, String xmlGuessEnc, String xmlEnc, InputStream is) throws IOException
    {
        String encoding;
        if (bomEnc == null)
        {
            if (xmlGuessEnc == null || xmlEnc == null)
            {
                encoding = UTF_8;
            }
            else if (xmlEnc.equals(UTF_16) && (xmlGuessEnc.equals(UTF_16BE) || xmlGuessEnc.equals(UTF_16LE)))
            {
                encoding = xmlGuessEnc;
            }
            else
            {
                encoding = xmlEnc;
            }
        }
        else if (bomEnc.equals(UTF_8))
        {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(UTF_8))
            {
                throw new EncodingException(RAW_EX_1.format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }), bomEnc, xmlGuessEnc, xmlEnc, is);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_8))
            {
                throw new EncodingException(RAW_EX_1.format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }), bomEnc, xmlGuessEnc, xmlEnc, is);
            }
            encoding = UTF_8;
        }
        else if (bomEnc.equals(UTF_16BE) || bomEnc.equals(UTF_16LE))
        {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc))
            {
                throw new IOException(RAW_EX_1.format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }));
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_16) && !xmlEnc.equals(bomEnc))
            {
                throw new EncodingException(RAW_EX_1.format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }), bomEnc, xmlGuessEnc, xmlEnc, is);
            }
            encoding = bomEnc;
        }
        else
        {
            throw new EncodingException(RAW_EX_2.format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }), bomEnc, xmlGuessEnc, xmlEnc, is);
        }
        return encoding;
    }
    
    private static String calculateHttpEncoding(String cTMime, String cTEnc, String bomEnc, String xmlGuessEnc, String xmlEnc, InputStream is, boolean lenient)
            throws IOException
    {
        String encoding;
        if (lenient & xmlEnc != null)
        {
            encoding = xmlEnc;
        }
        else
        {
            boolean appXml = isAppXml(cTMime);
            boolean textXml = isTextXml(cTMime);
            if (appXml || textXml)
            {
                if (cTEnc == null)
                {
                    if (appXml)
                    {
                        encoding = calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc, is);
                    }
                    else
                    {
                        encoding = US_ASCII;
                    }
                }
                else if (bomEnc != null && (cTEnc.equals(UTF_16BE) || cTEnc.equals(UTF_16LE)))
                {
                    throw new EncodingException(HTTP_EX_1.format(new Object[] { cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc }), cTMime, cTEnc, bomEnc,
                            xmlGuessEnc, xmlEnc);
                }
                else if (cTEnc.equals(UTF_16))
                {
                    if (bomEnc != null && bomEnc.startsWith(UTF_16))
                    {
                        encoding = bomEnc;
                    }
                    else
                    {
                        throw new EncodingException(HTTP_EX_2.format(new Object[] { cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc }), cTMime, cTEnc, bomEnc,
                                xmlGuessEnc, xmlEnc);
                    }
                }
                else
                {
                    encoding = cTEnc;
                }
            }
            else
            {
                throw new EncodingException(HTTP_EX_3.format(new Object[] { cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc }), cTMime, cTEnc, bomEnc, xmlGuessEnc,
                        xmlEnc);
            }
        }
        return encoding;
    }
    
    private static boolean isAppXml(String mime)
    {
        return mime != null
                && (mime.equals("application/xml") || mime.equals("application/xml-dtd") || mime.equals("application/xml-external-parsed-entity") || (mime //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .startsWith("application/") && mime.endsWith("+xml"))); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    // indicates if the MIME type belongs to the TEXT XML family
    private static boolean isTextXml(String mime)
    {
        return mime != null
                && (mime.equals("text/xml") || mime.equals("text/xml-external-parsed-entity") || (mime.startsWith("text/") && mime.endsWith("+xml"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    
    private static String getXMLGuessEncoding(BufferedInputStream is) throws IOException
    {
        String encoding = null;
        int[] bytes = new int[4];
        is.mark(4);
        bytes[0] = is.read();
        bytes[1] = is.read();
        bytes[2] = is.read();
        bytes[3] = is.read();
        is.reset();
        
        if (bytes[0] == 0x00 && bytes[1] == 0x3C && bytes[2] == 0x00 && bytes[3] == 0x3F)
        {
            encoding = UTF_16BE;
        }
        else if (bytes[0] == 0x3C && bytes[1] == 0x00 && bytes[2] == 0x3F && bytes[3] == 0x00)
        {
            encoding = UTF_16LE;
        }
        else if (bytes[0] == 0x3C && bytes[1] == 0x3F && bytes[2] == 0x78 && bytes[3] == 0x6D)
        {
            encoding = UTF_8;
        }
        return encoding;
    }
    
    private static String getXmlPrologEncoding(BufferedInputStream is, String guessedEnc) throws IOException
    {
        String encoding = null;
        if (guessedEnc != null)
        {
            byte[] bytes = new byte[BUFFER_SIZE];
            is.mark(BUFFER_SIZE);
            int offset = 0;
            int max = BUFFER_SIZE;
            int c = is.read(bytes, offset, max);
            int firstGT = -1;
            while (c != -1 && firstGT == -1 && offset < BUFFER_SIZE)
            {
                offset += c;
                max -= c;
                c = is.read(bytes, offset, max);
                firstGT = new String(bytes, 0, offset).indexOf(">"); //$NON-NLS-1$
            }
            if (firstGT == -1)
            {
                if (c == -1)
                {
                    // throw new IOException("Unexpected end of XML stream");
                    Log.i(EncodingUtils.class.getName(), "XML prolog or ROOT element not found on first " + offset + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
                    return encoding;
                }
                else
                {
                    // throw new
                    // IOException("XML prolog or ROOT element not found on first "
                    // + offset + " bytes");
                    Log.i(EncodingUtils.class.getName(), "XML prolog or ROOT element not found on first " + offset + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
                    return encoding;
                }
            }
            int bytesRead = offset;
            if (bytesRead > 0)
            {
                is.reset();
                Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes, 0, firstGT + 1), guessedEnc);
                BufferedReader bReader = new BufferedReader(reader);
                StringBuffer prolog = new StringBuffer();
                String line = bReader.readLine();
                while (line != null)
                {
                    prolog.append(line);
                    line = bReader.readLine();
                }
                Matcher m = ENCODING_PATTERN.matcher(prolog);
                if (m.find())
                {
                    encoding = m.group(1).toUpperCase();
                    encoding = encoding.substring(1, encoding.length() - 1);
                }
            }
        }
        return encoding;
    }
    
    private static String getContentTypeMime(String httpContentType)
    {
        String mime = null;
        if (httpContentType != null)
        {
            int i = httpContentType.indexOf(";"); //$NON-NLS-1$
            mime = ((i == -1) ? httpContentType : httpContentType.substring(0, i)).trim();
        }
        return mime;
    }
    
    private static String getContentTypeEncoding(String httpContentType)
    {
        String encoding = null;
        if (httpContentType != null)
        {
            int i = httpContentType.indexOf(";"); //$NON-NLS-1$
            if (i > -1)
            {
                String postMime = httpContentType.substring(i + 1);
                Matcher m = CHARSET_PATTERN.matcher(postMime);
                encoding = (m.find()) ? m.group(1) : null;
                encoding = (encoding != null) ? encoding.toUpperCase() : null;
            }
            if (encoding != null && ((encoding.startsWith("\"") && encoding.endsWith("\"")) || (encoding.startsWith("'") && encoding.endsWith("'")))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            {
                encoding = encoding.substring(1, encoding.length() - 1);
            }
        }
        return encoding;
    }
    
    private static String getBOMEncoding(BufferedInputStream is) throws IOException
    {
        String encoding = null;
        int[] bytes = new int[3];
        is.mark(3);
        bytes[0] = is.read();
        bytes[1] = is.read();
        bytes[2] = is.read();
        
        if (bytes[0] == 0xFE && bytes[1] == 0xFF)
        {
            encoding = UTF_16BE;
            is.reset();
            is.read();
            is.read();
        }
        else if (bytes[0] == 0xFF && bytes[1] == 0xFE)
        {
            encoding = UTF_16LE;
            is.reset();
            is.read();
            is.read();
        }
        else if (bytes[0] == 0xEF && bytes[1] == 0xBB && bytes[2] == 0xBF)
        {
            encoding = UTF_8;
        }
        else
        {
            is.reset();
        }
        return encoding;
    }
}
