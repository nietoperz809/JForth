package jforth.ControlWords;

import jforth.BaseWord;
import jforth.OStack;

/**
 * Created by Administrator on 4/25/2017.
 */
public class BreakLoopControlWord extends BaseWord
{
    public BreakLoopControlWord ()
    {
        super("", false, null);
    }

    @Override
    public Integer apply(OStack dataStack, OStack vStack)
    {
//        vStack.pop();
//        Object o2 = vStack.peek();
//        vStack.push(o2);
        return 1;
    }
}
