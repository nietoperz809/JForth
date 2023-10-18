package tools;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Prime factorization
 */
public class PollardRho {
    private final static BigInteger ZERO = new BigInteger("0");
    private final static BigInteger ONE  = new BigInteger("1");
    private final static BigInteger TWO  = new BigInteger("2");
    private final static SecureRandom random = new SecureRandom();

    public static BigInteger rho(BigInteger N) {
        BigInteger divisor;
        BigInteger c  = new BigInteger(N.bitLength(), random);
        BigInteger x  = new BigInteger(N.bitLength(), random);
        BigInteger xx = x;

        // check divisibility by 2
        if (N.mod(TWO).compareTo(ZERO) == 0) return TWO;

        do {
            x  =  x.multiply(x).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            divisor = x.subtract(xx).gcd(N);
        } while((divisor.compareTo(ONE)) == 0);

        return divisor;
    }

    public static ArrayList<BigInteger> factor (BigInteger N) {
        ArrayList<BigInteger> al = new ArrayList<>();
        factor (N, al);
        Collections.sort(al);
        return al;
    }

    private static void factor(BigInteger N, ArrayList<BigInteger> al) {
        if (N.compareTo(ONE) == 0)
            return;
        if (N.isProbablePrime(20))
        {
            al.add(N);
            return;
        }
        BigInteger divisor = rho(N);
        factor(divisor, al);
        factor(N.divide(divisor), al);
        return;
    }

//
//    public static void main(String[] args) {
//        BigInteger N = new BigInteger(args[0]);
//        factor(N);
//    }
}