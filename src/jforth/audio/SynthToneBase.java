package jforth.audio;

import jforth.forthwords.PredefinedWords;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@java.lang.FunctionalInterface
interface FunctionalInterface
{
    void doForChar (char c, SourceDataLine line);
}

public class SynthToneBase
{
    public static final int SAMPLE_RATE = 22050;
    public static final float SECONDS = 0.5f;
    protected static final int SLEN = (int) (SECONDS * SAMPLE_RATE)/3;
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

    public static void AudiotoBrowser(byte[] wav, PredefinedWords predefinedWords)
    {
        String encoded = Base64.getEncoder().encodeToString(wav);
        predefinedWords._jforth._out.print("audBytes" + encoded);
    }

    protected static void playIntoBuffer (ByteArrayOutputStream baos, byte[] tone, int ms)
    {
        int length = Math.min (SAMPLE_RATE * ms / 1000, tone.length);
        baos.write (tone, 0, length);
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

    public static byte[] createSingleToneWav (int freq, int ms) throws Exception {
        byte[] out = new byte[SAMPLE_RATE * ms / 1000];
        makeSingleWave (freq, out);
        return WaveTools.withWAVHeader (out, af);
    }

    public static void playSingleTone (int freq, int ms)
    {
        if (ms < 1 || freq < 1)
            return;
        try
        {
            SourceDataLine line = AudioSystem.getSourceDataLine (af);
            line.open (af, SAMPLE_RATE);
            line.start ();
            // play (line, pause, 500);

            byte[] out = new byte[SAMPLE_RATE * ms / 1000];
            makeSingleWave (freq, out);
            play (line, out, ms);

            line.drain ();
            line.close ();
        }
        catch (Exception ignored)
        {
            //System.out.println (ignored);
        }
    }
}
