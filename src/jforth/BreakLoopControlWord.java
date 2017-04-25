package jforth;

/**
 * Created by Administrator on 4/25/2017.
 */
public class BreakLoopControlWord extends BaseWord
{
    public BreakLoopControlWord ()
    {
        super("", false, false, null);
    }

    @Override
    public int execute (OStack dataStack, OStack variableStack)
    {
        return 1; // Not used
    }
}
