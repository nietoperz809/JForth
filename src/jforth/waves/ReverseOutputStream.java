package jforth.waves;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * Class to read litte-endian stuff
 */
class ReverseOutputStream extends DataOutputStream
{
    /**
     * Constructor: simply calls the super class
     * @param o OutputStream that is used
     */
    ReverseOutputStream (OutputStream o)
    {
        super(o);
    }

    /**
     * Saves a reversed (brain damaged) integer
     * @param i Integer to save
     * @throws Exception Exception from base class
     */
    void writeReverseInt(int i) throws Exception
    {
        int j = i << 24 | (i & 0xff00) << 8 | (i & 0xff0000) >>> 8 | (i >>> 24);
        writeInt(j);
    }

    /**
     * Saves a reversed (brain damaged) short value
     * @param i The value to be saved
     * @throws Exception Exception from base class
     */
    void writeReverseShort(short i) throws Exception
    {
        int j = i << 8 | i >>> 8;
        writeShort(j);
    }

    /**
     * Writes short[] array in reverse order
     * @param a Array to be saved
     * @throws Exception Exception from base class
     */
    void writeReverseShortArray(short[] a) throws Exception
    {
        byte[] b = new byte[a.length * 2];
        for (int s = 0; s < a.length; s++)
        {
            b[s * 2 + 1] = (byte) (a[s] >>> 8);
            b[s * 2] = (byte) (a[s] & 0xff);
        }
        write(b);
    }
}

