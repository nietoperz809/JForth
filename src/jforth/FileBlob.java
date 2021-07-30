package jforth;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileBlob implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    String _path;
    byte[] _content;
    private static final Charset _cs = StandardCharsets.ISO_8859_1;

    private FileBlob()
    {

    }

    public FileBlob (byte[] c, String n)
    {
        _content = c;
        _path = n;
    }

    /**
     * Constructor (read file from disk)
     * @param path Path to file
     * @throws IOException smth gone wrong
     */
    public FileBlob (String path) throws IOException
    {
        _path = path;
        _content = Files.readAllBytes(Paths.get(path));
    }

    /**
     * Append String to BLOB
     * @param dat the string
     */
    public void append (String dat)
    {
        byte[] bdat = dat.getBytes (_cs);
        byte[] bx = new byte[_content.length + bdat.length];
        System.arraycopy (_content, 0, bx, 0, _content.length);
        System.arraycopy (bdat, 0, bx, _content.length, bdat.length);
        _content = bx;
    }

    public void append (FileBlob other)
    {
        byte[] bx = new byte[_content.length + other._content.length];
        System.arraycopy (_content, 0, bx, 0, _content.length);
        System.arraycopy (other._content, 0, bx, _content.length, other._content.length);
        _content = bx;
    }

    /**
     * Create from String
     * @param dat source string
     * @return the new BLOB
     */
    public static FileBlob fromStringData (String dat)
    {
        FileBlob b = new FileBlob ();
        b._path = "<fromString>";
        b._content = dat.getBytes (_cs);
        return b;
    }

    public void put (String dest) throws IOException
    {
        Files.write (Paths.get(dest), _content);
        _path = dest;
    }

    public void xor (String key)
    {
        byte[] k2 = key.getBytes (_cs);
        int idx = 0;
        for (int i = 0; i< _content.length; i++)
        {
            _content[i] = (byte) (_content[i]^k2[idx]);
            idx = (idx+1)%k2.length;
        }
    }

    public String getPath()
    {
        return _path;
    }

    public int getSize()
    {
        return _content.length;
    }

    public String asString()
    {
        return new String (_content, _cs);
    }

    public byte[] get_content ()
    {
        return _content;
    }

    public byte[] get_partial (int from, int to)
    {
        byte[] res = new byte[to-from];
        System.arraycopy(_content, from, res, 0, to-from);
        return res;
    }

    public void set_content (byte[] _content)
    {
        this._content = _content;
    }
}
