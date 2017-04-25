package jforth;

/**
 * Created by Administrator on 4/25/2017.
 */
public class BreakLoopControlWord extends BaseWord
{
    private final int indexIncrement;

    public BreakLoopControlWord (int indexIncrement)
    {
        super("", false, false, null);
        this.indexIncrement = indexIncrement;
    }

    @Override
    public int execute (OStack dataStack, OStack variableStack)
    {
        return 1;
    }
}
