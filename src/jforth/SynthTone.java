package jforth;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SynthTone
{
    static final AudioFormat af =
            new AudioFormat (Note.SAMPLE_RATE, 8, 1, true, true);

    public static boolean playMorseString (String in)
    {
        try
        {
            SourceDataLine line = AudioSystem.getSourceDataLine (af);

            line.open (af, Note.SAMPLE_RATE);
            line.start ();
            play (line, Note.REST, 500);
            for (char c : in.toCharArray ())
            {
                if (c == '·')
                    playShort (line);
                else if (c == '-')
                    playLong (line);
                else if (c == ' ')
                    play (line, Note.REST, 200);
            }
            line.drain ();
            line.close ();
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

//    // Test
//    public static void main (String[] args) throws LineUnavailableException
//    {
//        SourceDataLine line = AudioSystem.getSourceDataLine (af);
//
//        line.open (af, Note.SAMPLE_RATE);
//        line.start ();
//        play (line, Note.REST, 500);
//        playShort (line);
//        playLong (line);
//        line.drain ();
//        line.close ();
//    }

    private static void playShort (SourceDataLine line)
    {
        play (line, Note.C4, 50);
        play (line, Note.REST, 100);
    }

    private static void playLong (SourceDataLine line)
    {
        play (line, Note.C4, 200);
        play (line, Note.REST, 100);
    }

    private static void play (SourceDataLine line, Note note, int ms)
    {
        ms = Math.min (ms, (int)(Note.SECONDS * 1000));
        int length = Note.SAMPLE_RATE * ms / 1000;
        line.write (note.data (), 0, length);
    }
}

enum Note
{
    REST, A4, A4$, B4, C4;
    public static final int SAMPLE_RATE = 11000;
    public static final float SECONDS = 0.5f;
    private byte[] sin = new byte[(int)(SECONDS * SAMPLE_RATE)];

    Note ()
    {
        int n = this.ordinal ();
        if (n > 0)
        {
            double exp = ((double) n - 1) / 12d;
            double f = 440d * Math.pow (2d, exp);
            for (int i = 0; i < sin.length; i++)
            {
                double period = (double) SAMPLE_RATE / f;
                double angle = 2d * Math.PI * i / period;
                sin[i] = (byte) (Math.sin (angle) * 127f);
            }
        }
    }

    public byte[] data ()
    {
        return sin;
    }
}