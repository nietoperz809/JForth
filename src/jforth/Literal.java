package jforth;

import jforth.BaseWord;
import jforth.OStack;

public class Literal extends BaseWord
{
    public Literal(Object o)
    {
        super("", false, false, null);
        this.obj = o;
    }

    public int execute(OStack dStack, OStack vStack)
    {
        dStack.push(obj);
        return 1;
    }


    private final Object obj;

}
