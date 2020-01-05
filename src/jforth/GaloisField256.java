package jforth;

/**
 * Galois field 2^8
 */
public class GaloisField256
{
    // define the Size & Prime Polynomial of this Galois field (2^8)
    private static final int GF = 256;

    // establish global Log and Antilog arrays
    private static final int[] Log = new int[GF];
    private static final int[] ALog = new int[GF];

    // static initializer
    static
    {
        int i;
        /*
            For illustration we will take the widely-used case of GF(256),
            the Galois field for 2^8, with a prime polynomial of x^8 + x^5 + x^3 + x^2 + 1
            whose equivalent value P is binary 100101101 or decimal 301.
        */
        int PP = 301;
        Log[0] = 1-GF;
        ALog[0] = 1;
        for (i=1; i<GF; i++)
        {
            ALog[i] = ALog[i-1] * 2;
            if (ALog[i] >= GF)
                ALog[i] ^= PP;
            Log[ALog[i]] = i;
        }
    }

    public static int[] getLog()
    {
        return Log.clone();
    }

    public static int[] getALog()
    {
        return ALog.clone();
    }

    public static int Product (int A, int B)
    {
        if ((A == 0) || (B == 0))
            return (0);
        A = A & 0xff;
        B = B & 0xff;
        return (ALog[(Log[A] + Log[B]) % (GF-1)]) & 0xff;
    }

    public static int Quotient (int A, int B)
    {
        if (B == 0)
        {
            return GF;
        }
        if (A == 0)
            return (0);
        A = A & 0xff;
        B = B & 0xff;
        return (ALog[(Log[A] - Log[B] + (GF-1)) % (GF-1)]) & 0xff;
    }

    public static int Sum (int A, int B)
    {
        return ((A & 0xff) ^ (B & 0xff));
    }

    public static int Difference (int A, int B)
    {
        return Sum (A, B);
    }
}
