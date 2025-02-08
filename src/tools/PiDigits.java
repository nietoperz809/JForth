package tools;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: 19.12.2009 Time: 22:41:12
 * To change this template use File | Settings | File Templates.
 */
public class PiDigits
{
    private static BigDecimal arctan(int inverseX, int scale)
    {
        BigDecimal result, numer, term;
        BigDecimal invX = BigDecimal.valueOf(inverseX);
        BigDecimal invX2
                = BigDecimal.valueOf((long)inverseX * inverseX);

        numer = BigDecimal.ONE.divide(invX,
                scale, BigDecimal.ROUND_HALF_EVEN);

        result = numer;
        int i = 1;
        do
        {
            numer = numer.divide(invX2, scale, BigDecimal.ROUND_HALF_EVEN);
            int denom = 2 * i + 1;
            term = numer.divide(BigDecimal.valueOf(denom), scale, BigDecimal.ROUND_HALF_EVEN);
            if ((i % 2) != 0)
            {
                result = result.subtract(term);
            }
            else
            {
                result = result.add(term);
            }
            i++;
        }
        while (term.compareTo(BigDecimal.ZERO) != 0);
        return result;
    }

    public static BigDecimal computePi(int digits)
    {
        int scale = digits + 5;
        BigDecimal arctan1_5 = arctan(5, scale);
        BigDecimal arctan1_239 = arctan(239, scale);
        BigDecimal pi = arctan1_5.multiply(BigDecimal.valueOf(4)).subtract(arctan1_239).multiply(BigDecimal.valueOf(4));
        return pi.setScale(digits, BigDecimal.ROUND_HALF_UP);
    }

    public static String getPiString(int digits)
    {
        return computePi(digits).toString().replace(".", "");
    }

    public static String getPiString(int start, int last)
    {
        String pi = getPiString(last);
        return pi.substring(start);
    }

    public static void main(String[] args) {
        System.out.println(getPiString(100));
    }
}
