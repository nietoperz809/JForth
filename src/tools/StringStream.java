package tools;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by Administrator on 4/15/2017.
 */
public class StringStream
{
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final PrintStream ps = new PrintStream(baos);

    public OutputStream getOutputStream()
    {
        return baos;
    }

    public PrintStream getPrintStream()
    {
        return ps;
    }

    public void clear()
    {
        baos.reset();
    }

    @Override
    public String toString () {
        try
        {
            baos.flush();
            return baos.toString (StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return e.toString();
        }
    }
}
