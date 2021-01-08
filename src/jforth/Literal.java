package jforth;


public class Literal extends BaseWord
{
    public Literal(Object o)
    {
        super("", false, null);
        this.obj = o;
    }

    public Integer apply(OStack dStack, OStack vStack)
    {
        dStack.push(obj);
        return 1;
    }


    private final Object obj;

}
