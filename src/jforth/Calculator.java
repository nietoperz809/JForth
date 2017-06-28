package jforth;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import scala.math.BigInt;

import java.util.function.BiFunction;

class Calculator
{
    static BigInt pow (BigInt a, BigInt b)
    {
        return a.pow(b.intValue());
    }

    static Double add (Double a, Double b)
    {
        return a + b;
    }

    static DoubleMatrix add (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.add(b);
        return new DoubleMatrix(res);
    }

    static DoubleMatrix sub (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.subtract(b);
        return new DoubleMatrix(res);
    }

    static DoubleMatrix mult (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.multiply(b);
        return new DoubleMatrix(res);
    }

    static DoubleMatrix div (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.multiply(MatrixUtils.inverse(b));
        return new DoubleMatrix(res);
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
        {
            return func.apply(getDouble(o1), getDouble(o2));
        }
        throw new Exception("Wrong args");
    }

    static public Double getDouble (Object o1) throws Exception
    {
        if (o1 instanceof BigInt)
        {
            return ((BigInt)o1).doubleValue();
        }
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

    static Complex doCalcComplex (Object o1, Object o2, BiFunction<Complex, Complex, Complex> func) throws Exception
    {
        if (areObjects(o1, o2, Complex.class))
        {
            return func.apply(getComplex(o1), getComplex(o2));
        }
        throw new Exception("Wrong args");
    }

    private static boolean areObjects (Object o1, Object o2, Class c)
    {
        return (c.isInstance(o1) || c.isInstance(o2));
    }

//    public static void main (String[] args)
//    {
//        Double c = 1.1;
//        Double d = 1.2;
//        String a ="lala";
//        String b = "kaka";
//        System.out.println(areObjects(a,b,Double.class));
//        System.out.println(areObjects(a,b,String.class));
//        System.out.println(areObjects(c,d,Double.class));
//        System.out.println(areObjects(c,d,String.class));
//        System.out.println(areObjects(c,a,String.class));
//        System.out.println(areObjects(c,a,Double.class));
//    }

    static public Complex getComplex (Object o1) throws Exception
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

    static Fraction doCalcFraction (Object o1, Object o2, BiFunction<Fraction, Fraction, Fraction> func) throws Exception
    {
        if (areObjects(o1, o2, Fraction.class))
        {
            return func.apply(getFrac(o1), getFrac(o2));
        }
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

    static BigInt doCalcBigInt (Object o1, Object o2, BiFunction<BigInt, BigInt, BigInt> func) throws Exception
    {
        if (areObjects(o1, o2, BigInt.class))
        {
            return func.apply(getBig(o1), getBig(o2));
        }
        throw new Exception("Wrong args");
    }

    static DoubleMatrix doCalcMatrix (Object o1, Object o2, BiFunction<DoubleMatrix, DoubleMatrix, DoubleMatrix> func) throws Exception
    {
        if (areObjects(o1, o2, DoubleMatrix.class))
        {
            return func.apply(getMatrix(o1), getMatrix(o2));
        }
        throw new Exception("Wrong args");
    }
    
    static public BigInt getBig (Object o1) throws Exception
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

    static private DoubleMatrix getMatrix (Object o1) throws Exception
    {
        if (o1 instanceof DoubleMatrix)
        {
            return (DoubleMatrix) o1;
        }
        throw new Exception("Wrong args");
    }
}
