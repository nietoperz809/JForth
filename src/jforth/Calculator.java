package jforth;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import scala.math.BigInt;

import java.util.function.BiFunction;

public class Calculator
{
    static BigInt pow (BigInt a, BigInt b)
    {
        return a.pow(b.intValue());
    }

    static Double add (Double a, Double b)
    {
        return a + b;
    }

    static Double sub (Double a, Double b)
    {
        return a - b;
    }

    static Double mult (Double a, Double b)
    {
        return a * b;
    }

    static Double div (Double a, Double b)
    {
        return a / b;
    }

    static Double doCalcDouble (Object o1, Object o2, BiFunction<Double, Double, Double> func) throws Exception
    {
        if (o1 instanceof Double || o2 instanceof Double)
            return func.apply(getDoub(o1), getDoub(o2));
        throw new Exception("Wrong args");
    }

    static private Double getDoub (Object o1) throws Exception
    {
        if (o1 instanceof Double)
        {
            return (Double) o1;
        }
        if (o1 instanceof Long)
        {
            return ((Long) o1).doubleValue();
        }
        throw new Exception("Wrong args");
    }

    static private Complex getComp (Object o1) throws Exception
    {
        if (o1 instanceof Complex)
        {
            return (Complex) o1;
        }
        if (o1 instanceof Long)
        {
            return new Complex((Long) o1);
        }
        else if (o1 instanceof Double)
        {
            return new Complex((Double) o1);
        }
        else if (o1 instanceof BigInt)
        {
            return new Complex(((BigInt) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    static Complex doCalcComplex (Object o1, Object o2, BiFunction<Complex, Complex, Complex> func) throws Exception
    {
        if (o1 instanceof Complex || o2 instanceof Complex)
            return func.apply(getComp(o1), getComp(o2));
        throw new Exception("Wrong args");
    }

    static Fraction doCalcFraction (Object o1, Object o2, BiFunction<Fraction, Fraction, Fraction> func) throws Exception
    {
        if (o1 instanceof Fraction || o2 instanceof Fraction)
            return func.apply(getFrac(o1), getFrac(o2));
        throw new Exception("Wrong args");
    }

    static private Fraction getFrac (Object o1) throws Exception
    {
        if (o1 instanceof Fraction)
        {
            return (Fraction) o1;
        }
        if (o1 instanceof Long)
        {
            return new Fraction((Long) o1);
        }
        if (o1 instanceof Double)
        {
            return new Fraction((Double) o1);
        }
        if (o1 instanceof BigInt)
        {
            return new Fraction(((BigInt) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    static private BigInt getBig (Object o1) throws Exception
    {
        if (o1 instanceof BigInt)
        {
            return (BigInt) o1;
        }
        if (o1 instanceof Long)
        {
            return BigInt.apply((Long) o1);
        }
        if (o1 instanceof Double)
        {
            return BigInt.apply(((Double) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    static BigInt doCalcBigInt (Object o1, Object o2, BiFunction<BigInt, BigInt, BigInt> func) throws Exception
    {
        if (o1 instanceof BigInt || o2 instanceof BigInt)
            return func.apply(getBig(o1), getBig(o2));
        throw new Exception("Wrong args");
    }
}
