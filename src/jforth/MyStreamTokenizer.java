package jforth;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

public class MyStreamTokenizer extends StreamTokenizer
{
    public MyStreamTokenizer (Reader r)
    {
        super(r);
    }

    private int dots = 0;

    private boolean allDots (String in)
    {
        if (in == null)
            return false;
        for (char c : in.toCharArray())
        {
            if (c!='.')
                return false;
        }
        return true;
    }

    @Override
    public int nextToken () throws IOException
    {
        if (dots != 0)
        {
            dots--;
            sval = ".";
            return (int)nval;
        }
        int tt = super.nextToken();
        if (allDots(sval))
        {
            dots = sval.length();
            dots--;
            sval = ".";
            return (int)nval;
        }
        return tt;
    }
}
