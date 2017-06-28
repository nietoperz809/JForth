package tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
    public String toString ()
    {
        try
        {
            baos.flush();
        }
        catch (IOException e)
        {
            return e.toString();
        }
        return new String (baos.toByteArray(), StandardCharsets.UTF_8);
    }
}
