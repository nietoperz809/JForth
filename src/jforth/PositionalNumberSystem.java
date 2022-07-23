package jforth;

import java.math.BigInteger;

/**
 * Class to convert Numbers into Strings and vice versa
 */
public class PositionalNumberSystem
{
    private final String charset;

    private static PositionalNumberSystem instance;

    static public  PositionalNumberSystem getPsnInst() {
        return instance;
    }

    public void newInstance (String cs) {
        instance = new PositionalNumberSystem (cs);
    }

    static {
        instance = new PositionalNumberSystem ("123");
    }

    /**
     * Constructor
     * @param cs  Charset to use. Length of cs is also the base
     */
    public PositionalNumberSystem(String cs)
    {
        charset = cs;
    }

    /**
     * Internal function that recursively converts a numeric value into a string
     * @param number Input value to be converted
     * @param base Base of number system of the output string
     * @param str Temporary
     * @return The result string
     */
    private String nsHelper (BigInteger number, int base, String str)
    {
        if (number.compareTo(BigInteger.ZERO) != 0)
        {
            str = nsHelper (number.divide(BigInteger.valueOf(base)), base, str);
            str += charset.charAt (number.mod(BigInteger.valueOf(base)).intValue());
        }
        return str;
    }

    /**
     * Converts number into string
     * @param number input number
     * @return the string
     */
    public String toString (BigInteger number)
    {
        if (number.compareTo(BigInteger.ZERO) == 0)
            return ""+charset.charAt(0);
        return nsHelper (number, charset.length(), "");
    }

    /**
     * Converts string to number
     * @param number input number
     * @return output string
     * @throws Exception if input contains illegal chars
     */
    public BigInteger toNumber (String number) throws Exception
    {
        BigInteger exp = BigInteger.ONE;
        BigInteger sum = BigInteger.ZERO;
        int len = number.length() - 1;
        int idx;
        char c;
        for (int n=len; n>=0; n--)
        {
            c = number.charAt(n);
            idx = charset.indexOf(c);
            if (idx < 0)
                throw new Exception ("Input Set Mismatch");
            sum = sum.add(BigInteger.valueOf(idx).multiply(exp));
            exp = exp.multiply(BigInteger.valueOf (charset.length()));
        }
        return sum;
    }

    /**
     * Test code
     * @param args not used
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        PositionalNumberSystem cn = new PositionalNumberSystem("ab"); //("0123456789abcdef");
        
        String s = cn.toString(BigInteger.valueOf(123456789));
        System.out.println(s);

        BigInteger i = cn.toNumber(s);
        System.out.println(i);
    }
}
