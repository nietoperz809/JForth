

public final class DListLiteral extends BaseWord
{
    public DListLiteral(DoubleSequence number)
    {
        super("", false, false, null);
        this.number = number;
    }

    public int execute(OStack dStack, OStack vStack)
    {
        dStack.push(number);
        return 1;
    }

    private final DoubleSequence number;
}
