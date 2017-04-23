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
        return a+b;
    }

    static Double sub (Double a, Double b)
    {
        return a-b;
    }

    static Double mult (Double a, Double b)
    {
        return a*b;
    }

    static Double div (Double a, Double b)
    {
        return a/b;
    }

    static Double doCalcDouble (Object o1, Object o2, BiFunction<Double, Double, Double> func) throws Exception
    {
        Double b1;
        Double b2;
        if (o1 instanceof Double)
        {
            b1 = (Double)o1;
            if (o2 instanceof Long)
            {
                b2 = ((Long)o2).doubleValue();
            }
            else if (o2 instanceof Double)
            {
                b2 = (Double)o2;
            }
            else
                throw new Exception ("Wrong args");
        }
        else if (o2 instanceof Double)
        {
            b2 = (Double)o2;
            if (o1 instanceof Long)
            {
                b1 = ((Long)o1).doubleValue();
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

    static Complex doCalcComplex (Object o1, Object o2, BiFunction<Complex, Complex, Complex> func) throws Exception
    {
        Complex b1;
        Complex b2;
        if (o1 instanceof Complex)
        {
            b1 = (Complex)o1;
            if (o2 instanceof Long)
            {
                b2 = new Complex((Long)o2);
            }
            else if (o2 instanceof Double)
            {
                b2 = new Complex((Double)o2);
            }
            else if (o2 instanceof BigInt)
            {
                b2 = new Complex(((BigInt)o2).longValue());
            }
            else
                throw new Exception ("Wrong args");
        }
        else if (o2 instanceof Complex)
        {
            b2 = (Complex)o2;
            if (o1 instanceof Long)
            {
                b1 = new Complex((Long)o1);
            }
            else if (o1 instanceof Double)
            {
                b1 = new Complex((Double)o1);
            }
            else if (o1 instanceof BigInt)
            {
                b1 = new Complex(((BigInt)o1).longValue());
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

    static Fraction doCalcFraction (Object o1, Object o2, BiFunction<Fraction, Fraction, Fraction> func) throws Exception
    {
        Fraction b1;
        Fraction b2;
        if (o1 instanceof Fraction)
        {
            b1 = (Fraction)o1;
            if (o2 instanceof Long)
            {
                b2 = new Fraction((Long)o2);
            }
            else if (o2 instanceof Double)
            {
                b2 = new Fraction((Double)o2);
            }
            else if (o2 instanceof BigInt)
            {
                b2 = new Fraction(((BigInt)o2).longValue());
            }
            else
                throw new Exception ("Wrong args");
        }
        else if (o2 instanceof Fraction)
        {
            b2 = (Fraction)o2;
            if (o1 instanceof Long)
            {
                b1 = new Fraction((Long)o1);
            }
            else if (o1 instanceof Double)
            {
                b1 = new Fraction((Double)o1);
            }
            else if (o1 instanceof BigInt)
            {
                b1 = new Fraction(((BigInt)o1).longValue());
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
    
    
    static BigInt doCalcBigInt (Object o1, Object o2, BiFunction<BigInt, BigInt, BigInt> func) throws Exception
    {
        BigInt b1;
        BigInt b2;
        if (o1 instanceof BigInt)
        {
            b1 = (BigInt)o1;
            if (o2 instanceof Long)
            {
                b2 = BigInt.apply((Long)o2);
            }
            else if (o2 instanceof Double)
            {
                b2 = BigInt.apply(((Double)o2).longValue());
            }
            else if (o2 instanceof BigInt)
            {
                b2 = (BigInt)o2;
            }
            else
                throw new Exception ("Wrong args");
        }
        else if (o2 instanceof BigInt)
        {
            b2 = (BigInt)o2;
            if (o1 instanceof Long)
            {
                b1 = BigInt.apply((Long)o1);
            }
            else if (o1 instanceof Double)
            {
                b1 = BigInt.apply(((Double)o1).longValue());
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
}
