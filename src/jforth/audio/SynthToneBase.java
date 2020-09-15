package jforth.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SynthToneBase
{
    @java.lang.FunctionalInterface
    interface FunctionalInterface
    {
        void doForChar (char c, SourceDataLine line);
    }

    public static final int SAMPLE_RATE = 22050;
    public static final float SECONDS = 0.5f;
    protected static final int SLEN = (int) (SECONDS * SAMPLE_RATE);
    protected static final byte[] pause = new byte[SLEN];

    static final AudioFormat af =
            new AudioFormat (SAMPLE_RATE, 8, 1, true, true);

    /**
     * Fill a buffer with sine data
     * @param f frequency
     * @param out output buffer
     */
    protected static void makeSingleWave (double f, byte[] out)
    {
        double period = (double) SAMPLE_RATE / f;
        for (int i = 0; i < out.length; i++)
        {
            double angle = 2d * Math.PI * i / period;
            out[i] = (byte) (Math.sin (angle) * 127f);
        }
    }

    /**
     * Play part or whole of a sound buffer
     * @param line output data line
     * @param tone the buffer
     * @param ms milliseconds of buffer to play
     */
    protected static void play (SourceDataLine line, byte[] tone, int ms)
    {
        int length = Math.min (SAMPLE_RATE * ms / 1000, tone.length);
        line.write (tone, 0, length);
    }

    /**
     * Open data line
     * @return the data line (on success)
     * @throws LineUnavailableException if it failed
     */
    protected static SourceDataLine openLine() throws LineUnavailableException
    {
        SourceDataLine line = AudioSystem.getSourceDataLine (af);
        line.open (af, SAMPLE_RATE);
        line.start ();
        play (line, pause, 500);
        return line;
    }

    /**
     * Close a previously opened data line
     * @param line to be closed
     */
    protected static void closeLine (SourceDataLine line)
    {
        line.drain ();
        line.close ();
    }

    /**
     * Play audio string depending on inferface
     * @param in string to be played
     * @param functionalInterface how chars are handled
     */
    protected static void playString (String in, FunctionalInterface functionalInterface)
    {
        try
        {
            SourceDataLine line = openLine();
            for (char c : in.toCharArray ())
            {
                functionalInterface.doForChar (c, line);
            }
            closeLine (line);
        }
        catch (Exception ignored)
        {
        }
    }
}
