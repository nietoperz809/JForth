package jforth;

public final class DMatrixLiteral extends BaseWord
{
    public DMatrixLiteral(DoubleMatrix number)
    {
        super("", false, false, null);
        this.number = number;
    }

    public int execute(OStack dStack, OStack vStack)
    {
        dStack.push(number);
        return 1;
    }

    private final DoubleMatrix number;
}