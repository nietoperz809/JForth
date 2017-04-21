package jforth;

import scala.math.BigInt;

import java.util.function.BiFunction;

/**
 * Created by Administrator on 4/21/2017.
 */
public class BigIntCalculator
{
    private BigInt result;

    BigInt getResult()
    {
        return result;
    }

    static BigInt pow (BigInt a, BigInt b)
    {
        return a.pow(b.intValue());
    }

    BigIntCalculator (Object o1, Object o2, BiFunction<BigInt, BigInt, BigInt> func) throws Exception
    {
        BigInt b1 = null;
        BigInt b2 = null;
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
        result = func.apply(b1, b2);
    }
}
