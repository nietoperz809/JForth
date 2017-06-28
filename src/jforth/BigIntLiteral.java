package jforth;

import scala.math.BigInt;

public final class BigIntLiteral extends BaseWord
{
    public BigIntLiteral(BigInt number)
    {
        super("", false, false, null);
        this.number = number;
    }

    public int execute(OStack dStack, OStack vStack)
    {
        dStack.push(number);
        return 1;
    }

    private final BigInt number;
}
