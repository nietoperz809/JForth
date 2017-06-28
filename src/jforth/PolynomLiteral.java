package jforth;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

public final class PolynomLiteral extends BaseWord
{
    public PolynomLiteral (PolynomialFunction str)
    {
        super("", false, false, null);
        this.str = str;
    }

    public int execute(OStack dStack, OStack vStack)
    {
        dStack.push(str);
        return 1;
    }

    private final PolynomialFunction str;
}
