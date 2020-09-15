package jforth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileBlob
{
    String _path;
    byte[] content;

    public FileBlob (String path) throws IOException
    {
        _path = path;
        content = Files.readAllBytes(Paths.get(path));
    }

    public void put (String dest) throws IOException
    {
        Files.write (Paths.get(dest), content);
        _path = dest;
    }

    public void xor (String key)
    {
        byte[] k2 = key.getBytes (StandardCharsets.ISO_8859_1);
        int idx = 0;
        for (int i=0; i<content.length; i++)
        {
            content[i] = (byte) (content[i]^k2[idx]);
            idx = (idx+1)%k2.length;
        }
    }

    public String getPath()
    {
        return _path;
    }

    public int getSize()
    {
        return content.length;
    }

    public String asString()
    {
        return new String (content, StandardCharsets.ISO_8859_1);
    }
}
