package jforth.waves;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;

public class SynthTone
{
    public static final int SAMPLE_RATE = 11000;
    public static final float SECONDS = 0.5f;
    private static final int SLEN = (int)(SECONDS * SAMPLE_RATE);
    
    private static final byte[] sin = new byte[SLEN];
    private static final byte[] pause = new byte[SLEN];

    private static final byte[] dtmf1 = new byte[SLEN];
    private static final byte[] dtmf2 = new byte[SLEN];
    private static final byte[] dtmf3 = new byte[SLEN];
    private static final byte[] dtmfa = new byte[SLEN];
    private static final byte[] dtmf4 = new byte[SLEN];
    private static final byte[] dtmf5 = new byte[SLEN];
    private static final byte[] dtmf6 = new byte[SLEN];
    private static final byte[] dtmfb = new byte[SLEN];
    private static final byte[] dtmf7 = new byte[SLEN];
    private static final byte[] dtmf8 = new byte[SLEN];
    private static final byte[] dtmf9 = new byte[SLEN];
    private static final byte[] dtmfc = new byte[SLEN];
    private static final byte[] dtmfstar = new byte[SLEN];
    private static final byte[] dtmf0 = new byte[SLEN];
    private static final byte[] dtmfsharp = new byte[SLEN];
    private static final byte[] dtmfd = new byte[SLEN];
    private static final HashMap<Character, byte[]> dtmfMap = new HashMap<>();

    static final AudioFormat af =
            new AudioFormat (SAMPLE_RATE, 8, 1, true, true);

    static
    {
        makeSingleWave (1000d, sin);

        makeDoubleWave (1209, 697, dtmf1, '1');
        makeDoubleWave (1336, 697, dtmf2, '2');
        makeDoubleWave (1477, 697, dtmf3, '3');
        makeDoubleWave (1633, 697, dtmfa, 'A');

        makeDoubleWave (1209, 770, dtmf4, '4');
        makeDoubleWave (1336, 770, dtmf5, '5');
        makeDoubleWave (1477, 770, dtmf6, '6');
        makeDoubleWave (1633, 770, dtmfb, 'B');

        makeDoubleWave (1209, 852, dtmf7, '7');
        makeDoubleWave (1336, 852, dtmf8, '8');
        makeDoubleWave (1477, 852, dtmf9, '9');
        makeDoubleWave (1633, 852, dtmfc, 'C');

        makeDoubleWave (1209, 941, dtmfstar, '*');
        makeDoubleWave (1336, 941, dtmf0, '0');
        makeDoubleWave (1477, 941, dtmfsharp, '#');
        makeDoubleWave (1633, 941, dtmfd, 'D');
    }

    private static void makeSingleWave (double f, byte[] out)
    {
        for (int i = 0; i < out.length; i++)
        {
            double period = (double) SAMPLE_RATE / f;
            double angle = 2d * Math.PI * i / period;
            out[i] = (byte) (Math.sin (angle) * 127f);
        }
    }

    private static void makeDoubleWave (double f1, double f2, byte[] out, char key)
    {
        for (int i = 0; i < out.length; i++)
        {
            double p1 = (double) SAMPLE_RATE / f1;
            double p2 = (double) SAMPLE_RATE / f2;
            double angle1 = 2d * Math.PI * i / p1;
            double angle2 = 2d * Math.PI * i / p2;
            double tmp = Math.sin (angle1)*127f + Math.sin (angle2)*127f;
            out[i] = (byte)(tmp/2d);
        }
        dtmfMap.put (key, out);
    }

    public static void playDtmfString (String in)
    {
        try
        {
            SourceDataLine line = AudioSystem.getSourceDataLine (af);
            line.open (af, SAMPLE_RATE);
            line.start ();
            play (line, pause, 500);

            for (char c : in.toCharArray ())
            {
                if (c == ' ')
                {
                    play (line, pause, 50);
                    continue;
                }
                byte[] tone = dtmfMap.get (c);
                if (tone != null)
                {
                    play (line, tone, 150);
                }
            }

            line.drain ();
            line.close ();
        }
        catch (Exception ignored)
        {
        }
    }

    public static void playMorseString (String in)
    {
        try
        {
            SourceDataLine line = AudioSystem.getSourceDataLine (af);
            line.open (af, SAMPLE_RATE);
            line.start ();
            play (line, pause, 500);

            for (char c : in.toCharArray ())
            {
                if (c == '·')
                    playShort (line);
                else if (c == '-')
                    playLong (line);
                else if (c == ' ')
                    play (line, pause, 200);
            }

            line.drain ();
            line.close ();
        }
        catch (Exception ignored)
        {
        }
    }

//    // Test
//    public static void main (String[] args) throws LineUnavailableException
//    {
//        SourceDataLine line = AudioSystem.getSourceDataLine (af);
//
//        line.open (af, SAMPLE_RATE);
//        line.start ();
//        play (line, pause, 500);
//        play (line, dtmf1, 500);
//        line.drain ();
//        line.close ();
//    }

    private static void playShort (SourceDataLine line)
    {
        play (line, sin, 50);
        play (line, pause, 100);
    }

    private static void playLong (SourceDataLine line)
    {
        play (line, sin, 200);
        play (line, pause, 100);
    }

    private static void play (SourceDataLine line, byte[] tone, int ms)
    {
        ms = Math.min (ms, (int)(SECONDS * 1000));
        int length = SAMPLE_RATE * ms / 1000;
        line.write (tone, 0, length);
    }
}
