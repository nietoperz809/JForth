package jforth.waves;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SynthToneBase
{
    @java.lang.FunctionalInterface
    interface FunctionalInterface
    {
        void doForChar (char c, SourceDataLine line);
    }

    public static final int SAMPLE_RATE = 11000;
    public static final float SECONDS = 0.5f;
    protected static final int SLEN = (int) (SECONDS * SAMPLE_RATE);
    protected static final byte[] pause = new byte[SLEN];

    static final AudioFormat af =
            new AudioFormat (SAMPLE_RATE, 8, 1, true, true);

    protected static void makeSingleWave (double f, byte[] out)
    {
        for (int i = 0; i < out.length; i++)
        {
            double period = (double) SAMPLE_RATE / f;
            double angle = 2d * Math.PI * i / period;
            out[i] = (byte) (Math.sin (angle) * 127f);
        }
    }

    protected static void play (SourceDataLine line, byte[] tone, int ms)
    {
        int length = SAMPLE_RATE * ms / 1000;
        length = Math.min (length, tone.length);
        line.write (tone, 0, length);
    }

    protected static void playString (String in, FunctionalInterface functionalInterface)
    {
        try
        {
            SourceDataLine line = AudioSystem.getSourceDataLine (af);
            line.open (af, SAMPLE_RATE);
            line.start ();
            play (line, pause, 500);

            for (char c : in.toCharArray ())
            {
                functionalInterface.doForChar (c, line);
            }

            line.drain ();
            line.close ();
        }
        catch (Exception ignored)
        {
        }
    }

}
