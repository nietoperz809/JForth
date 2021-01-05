package jforth;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import tools.TwoFuncs;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by Administrator on 4/23/2017.
 */
public class PolySupport
{
    /**
     * Make antiderivate of polynomial
     * @param p Input polynomial
     * @return Polynomial that is the integral of p
     */
    public static PolynomialFunction antiDerive (PolynomialFunction p)
    {
        double[] in = p.getCoefficients();
        double[] out = new double[in.length+1];
        out[0] = 0.0; // constant
        for (int s=1; s<out.length; s++)
        {
            out[s] = in[s-1]/s;
        }
        return new PolynomialFunction(out);
    }

    /**
     * Internal function to reduce a polynomial
     * @param a input polynomial
     * @return new length
     */
    private static int polylength (final double[] a)
    {
        int d = a.length - 1;
        while(d >= 0  && Math.abs(a[d]) < 1E-12)
            d--;
        return d + 1;
    }

    /**
     * Divide polynomials
     * @param here Dividend
     * @param p Divisor
     * @return Quotient
     */
    public static PolynomialFunction polyDiv (PolynomialFunction here, PolynomialFunction p)
    {
        return polyDivComplete(here, p)[0];
    }

    /**
     * Get ramainder of polynomial division
     * @param here Dividend
     * @param p Divisor
     * @return Remainder
     */
    public static PolynomialFunction polyMod (PolynomialFunction here, PolynomialFunction p)
    {
        return polyDivComplete(here, p)[1];
    }

    /**
     * Workhorse to divide polynomials
     * @param here Dividend
     * @param p Divisor
     * @return Array of 2
     */
    private static PolynomialFunction[] polyDivComplete (PolynomialFunction here, PolynomialFunction p)
    {
        final int dq = here.degree() - p.degree() + 1;
        if(dq <= 0)
            return new PolynomialFunction[] {new PolynomialFunction(new double[]{0.0}), here};
        final double[] rest = Arrays.copyOf(here.getCoefficients(), polylength(here.getCoefficients()));
        final double[] quotient = new double[dq];
        final int dr = p.degree();
        final double c = p.getCoefficients()[dr];
        for(int i = dq - 1; i >= 0; i--)
        {
            final double q = rest[dr + i]/c;
            quotient[i] = q;
            for(int j = 0; j <= dr; j++)
                rest[i + j] -= q*p.getCoefficients()[j];
        }
        return new PolynomialFunction[] {new PolynomialFunction(quotient), new PolynomialFunction(rest)};
    }

    /**
     * Executes function on two PolynomialFunction
     * @param o1 Object that may be a PolynomialFunction
     * @param o2 Object that may be a PolynomialFunction
     * @param func Function to be applied
     * @return Result of applied function
     * @throws Exception If it is not possible to call the function
     */
    public static PolynomialFunction execute (Object o1, Object o2, TwoFuncs<PolynomialFunction, PolynomialFunction, PolynomialFunction> func) throws Exception
    {
        PolynomialFunction b1;
        PolynomialFunction b2;
        if (o1 instanceof PolynomialFunction)
        {
            b1 = (PolynomialFunction)o1;
            if (o2 instanceof Long)
            {
                b2 = new PolynomialFunction (new double[]{((Long)o2).doubleValue()});
            }
            else if (o2 instanceof Double)
            {
                b2 = new PolynomialFunction(new double[]{(Double)o2});
            }
            else if (o2 instanceof BigInteger)
            {
                b2 = new PolynomialFunction (new double[]{((BigInteger)o2).doubleValue()});
            }
            else if (o2 instanceof PolynomialFunction)
            {
                b2 = (PolynomialFunction)o2;
            }
            else
                throw new Exception ("Wrong args");
        }
        else if (o2 instanceof PolynomialFunction)
        {
            b2 = (PolynomialFunction)o2;
            if (o1 instanceof Long)
            {
                b1 = new PolynomialFunction(new double[]{((Long)o1).doubleValue()});
            }
            else if (o1 instanceof Double)
            {
                b1 = new PolynomialFunction (new double[]{(Double)o1});
            }
            else if (o1 instanceof BigInteger)
            {
                b1 = new PolynomialFunction (new double[]{((BigInteger)o1).doubleValue()});
            }
            else
                throw new Exception ("Wrong args");
        }
        else
        {
            throw new Exception ("Wrong args");
        }
        return func.apply(b1, b2);
    }

    /**
     * Pops PolynomialFunction off the stack
     * @param dStack A Stack
     * @return The PolynomialFunction
     * @throws Exception If TOS isn't a PolynomialFunction
     */
    public static PolynomialFunction readPoly (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        if (o instanceof PolynomialFunction)
        {
            return (PolynomialFunction)o;
        }
        throw new Exception ("Wrong or no Type on Stack");
    }

    public static String formatPoly (PolynomialFunction p)
    {
        String n = p.toString().replaceAll("\\s", "");
        StringBuilder sb = new StringBuilder();
        boolean isNum = false;
        for (char c : n.toCharArray())
        {
            if (c == 'x' && isNum)
                sb.append('*');
            isNum = Character.isDigit(c);
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Rounds a polynomial
     * @param p the poly
     * @param r rounding value, must be multiple of 10
     * @return a rounded poly
     */
    public static PolynomialFunction roundPoly (PolynomialFunction p, double r)
    {
        double[] coeff = p.getCoefficients();
        for (int s=0; s<coeff.length; s++)
        {
            coeff[s] = Math.round(r * coeff[s]) / r;
        }
        return new PolynomialFunction(coeff);
    }
}
