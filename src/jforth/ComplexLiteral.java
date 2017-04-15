package jforth;

import org.apache.commons.math3.complex.Complex;

public final class ComplexLiteral extends BaseWord
{
    public ComplexLiteral(Complex number)
    {
        super("", false, false, null);
        this.number = number;
    }

    public int execute(OStack dStack, OStack vStack)
    {
        dStack.push(number);
        return 1;
    }

    private final Complex number;
}
