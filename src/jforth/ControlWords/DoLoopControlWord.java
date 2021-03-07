package jforth.ControlWords;

import jforth.BaseWord;
import jforth.OStack;
import jforth.Utilities;

public final class DoLoopControlWord extends BaseWord
{
    public DoLoopControlWord ()
    {
        super("", false, null);
    }

    public Integer apply(OStack dStack, OStack vStack)
    {
        try
        {
            long o1 = Utilities.readLong(dStack);
            long o2;
            if (dStack.isEmpty())
            {
                o2 = Long.MAX_VALUE;
            }
            else
            {
                o2 = Utilities.readLong(dStack);
            }
            vStack.push(o2);
            vStack.push(o1);
            return 1;
        }
        catch (Exception ex)
        {
            return 0;
        }
    }
}
