package jforth.waves;

public class Wave16
{
    /**
     * Upper level constant
     */
    public static final double MAX_VALUE = Short.MAX_VALUE;
    /**
     * Lower level constant
     */
    public static final double MIN_VALUE = Short.MIN_VALUE;
    /**
     * Math constants
     */
    public static final double PI = Math.PI;
    public static final double PI2 = 2.0 * PI;
    public static final double ASIN1 = Math.asin(1.0);
    /**
     * Sampling rate
     */
    private final int samplingRate;
    public WaveFormType waveType = WaveFormType.OFF;
    /**
     * Data array that holds sampling data
     */
    public double[] data;

    public Wave16 (int size, int rate, WaveFormType t)
    {
        this(size, rate);
        waveType = t;
    }

    /**
     * Builds a new com.soundgen.pittbull.soundgen.Wave16 object
     *
     * @param size Size of array
     * @param rate Sampling rate
     */
    public Wave16 (int size, int rate)
    {
        data = new double[size];
        samplingRate = rate;
    }

    @Override
    public String toString()
    {
        char[] res = new char[data.length];
        for (int s = 0; s < data.length; s++)
        {
            res[s] = (char) data[s];
        }
        return new String(res);
    }

    public byte[] toByteArray ()
    {
        byte[] res = new byte[data.length * 2];
        for (int s = 0; s < data.length; s++)
        {
            res[s * 2+1] = (byte) ((short) data[s] >>> 8);
            res[s * 2] = (byte) ((short) data[s] & 0xff);
        }
        return res;
    }

    public Wave16 deriveAndFitValues ()
    {
        double f1;
        double f2;
        Wave16 out = createEmptyCopy();

        for (int s = 0; s < (data.length - 1); s++)
        {
            f1 = data[s];
            f2 = data[s + 1];
            out.data[s] = f2 - f1;
        }
        // Last sample
        out.data[data.length - 1] = out.data[data.length - 2];
        out.data = fitValues(out.data);
        return out;
    }

    /**
     * Local factory function that builds a new SamplingData16 object from this one
     * All samples are empty
     *
     * @return The new object
     */
    public Wave16 createEmptyCopy ()
    {
        return new Wave16(data.length, samplingRate);
        //out.setName(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    public static double[] fitValues (double[] in)
    {
        double[] out = new double[in.length];
        Wave16.Wave16AmplitudeInfo am = new Wave16.Wave16AmplitudeInfo();
        am.calc(in);
        double div = am.span / (Wave16.MAX_VALUE - Wave16.MIN_VALUE);
        am.min = am.min / div;
        for (int s = 0; s < in.length; s++)
        {
            out[s] = in[s] / div + Wave16.MIN_VALUE - am.min;
            if (Double.isInfinite(out[s]) || Double.isNaN(out[s]))
            {
                out[s] = 0.0;
            }
        }
        return out;
    }

    /**
     * Write int as 4 bytes into array in reverse order
     * @param i the Int
     * @param dest destination array
     * @param offs offset that are written to
     */
    private static void intToBytes (int i, byte[] dest, int offs)
    {
        dest[offs] = (byte) (i & 0x00FF);
        dest[1+offs] = (byte) ((i >> 8) & 0x000000FF);
        dest[2+offs] = (byte) ((i >> 16) & 0x000000FF);
        dest[3+offs] = (byte) ((i >> 24) & 0x000000FF);
    }

    /**
     * Create Wav header for PCM signed, 16 bits, mono, 11025 samples/sec
     * @param raw raw sample data
     * @return header+samples
     */
    public static byte[] makeHeader11025 (byte[] raw)
    {
        byte[] headerdata =
                {
                        0x52, 0x49, 0x46, 0x46, 0x38, 0x00, 0x00, 0x00, 0x57, 0x41,
                        0x56, 0x45, 0x66, 0x6D, 0x74, 0x20, 0x10, 0x00, 0x00, 0x00,
                        0x01, 0x00, 0x01, 0x00, 0x11, 0x2B, 0x00, 0x00, 0x22, 0x56,
                        0x00, 0x00, 0x02, 0x00, 0x10, 0x00, 0x64, 0x61, 0x74, 0x61,
                        0x14, 0x00, 0x00, 0x00,
                };

        intToBytes(raw.length+44-8, headerdata, 4);
        intToBytes(raw.length, headerdata, 40);

        byte[] ret = new byte[raw.length+headerdata.length];
        System.arraycopy(headerdata, 0,ret , 0, headerdata.length);
        System.arraycopy(raw, 0, ret, headerdata.length, raw.length);
        return ret;
    }

//    public static void main (String[] args)
//    {
//        byte[] blah = makeHeader11025("fick dich".getBytes());
//        System.out.println(Arrays.toString(blah));
//    }

    //////////////////////////////////////////////////////////////////


    static class Wave16AmplitudeInfo
    {
        /**
         * Minimum amplitude
         */
        public double min;
        /**
         * Maximum amplitude
         */
        public double max;
        /**
         * Total amplitude span
         */
        public double span;

        Wave16AmplitudeInfo ()
        {
        }

        /**
         * Does calculation so that members are valid
         *
         * @param arr Array to be used as base object
         */
        public void calc (double arr[])
        {
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;

            // Find min and max
            for (double anIn : arr)
            {
                // Force forbidden values to zero
                if (Double.isInfinite(anIn) || Double.isNaN(anIn))
                {
                    anIn = 0.0;
                }

                if (anIn < min)
                {
                    min = anIn;
                }
                if (anIn > max)
                {
                    max = anIn;
                }
            }
            span = max - min;
        }

        public String toString ()
        {
            return "Min:" + min + " Max:" + max + " Span:" + span;
        }
    }
}

