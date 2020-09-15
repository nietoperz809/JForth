package jforth;

import java.io.IOException;
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
        return new String (content);
    }
}
