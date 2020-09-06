package jforth;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * from here: ftp://sccn.ucsd.edu/pub/virtualmedia/AePlayWave.java
 */

public class SynchronousWavePlayer
{
    private static final Charset thisCharset = StandardCharsets.ISO_8859_1;

    /**
     * Play wave file from disk
     * @param wavfile a disk file
     */
    public static void playSound (String wavfile)
    {
        try
        {
            File soundFile = new File(wavfile);
            InputStream inp = new BufferedInputStream(new FileInputStream(soundFile));
            playSound(inp);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Play wave from an array
     * @param data Array containing wav data+header
     */
    public static void playSound (byte[] data)
    {
        try
        {
            InputStream inp  = new BufferedInputStream(new ByteArrayInputStream (data));
            playSound(inp);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Play wave from Stream
     * @param inp Stream containing wave data + header
     */
    public static void playSound (InputStream inp)
    {
        try
        {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(inp);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Run the SAM module and convert a text to speech data
     * @param words Text to be spoken
     * @return String containing wave file
     * @throws Exception if smth gone wrong
     */
    public static String SAMtoWaveString (String words) throws Exception
    {
        words = words.replace('-','_');
        String res = Utilities.extractResource("sam.exe");
        Process process = new ProcessBuilder(
                res, "-stdout", "dummy", words)
                .start();
        InputStream is = process.getInputStream();

        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (is, thisCharset)))
        {
            int c;
            while ((c = reader.read()) != -1)
            {
                textBuilder.append((char) c);
            }
        }
        is.close();
        return textBuilder.toString();
    }
}
