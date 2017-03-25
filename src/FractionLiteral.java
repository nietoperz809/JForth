import org.apache.commons.math3.fraction.Fraction;

public final class FractionLiteral extends BaseWord
{
    public FractionLiteral(Fraction number)
    {
        super("", false, false, null);
        this.number = number;
    }

    public int execute(OStack dStack, OStack vStack)
    {
        dStack.push(number);
        return 1;
    }

    private final Fraction number;
}
