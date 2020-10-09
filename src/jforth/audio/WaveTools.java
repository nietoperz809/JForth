package jforth.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * from here: ftp://sccn.ucsd.edu/pub/virtualmedia/AePlayWave.java
 */

public class WaveTools
{
   // private static final Charset thisCharset = StandardCharsets.ISO_8859_1;

//    /**
//     * Play wave file from disk
//     * @param wavfile a disk file
//     */
//    public static void playSound (String wavfile)
//    {
//        try
//        {
//            File soundFile = new File(wavfile);
//            InputStream inp = new BufferedInputStream(new FileInputStream(soundFile));
//            playSound(inp);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }

//    /**
//     * Play wave from an array
//     * @param data Array containing wav data+header
//     */
//    public static Clip playSound (byte[] data)
//    {
//        try
//        {
//            InputStream inp  = new BufferedInputStream(new ByteArrayInputStream (data));
//            return playSound(inp);
//        }
//        catch (Exception e)
//        {
//            return null;
//        }
//    }
//
//    /**
//     * Play wave from Stream
//     * @param inp Stream containing wave data + header
//     */
//    public static Clip playSound (InputStream inp)
//    {
//        try
//        {
//            AudioInputStream audioIn = AudioSystem.getAudioInputStream(inp);
//            Clip clip = AudioSystem.getClip();
//            clip.open(audioIn);
//            clip.start();
//            return clip;
//        }
//        catch (Exception e)
//        {
//            return null;
//        }
//    }

//    /**
//     * Run the SAM module and convert a text to speech data
//     * @param words Text to be spoken
//     * @return String containing wave file
//     * @throws Exception if smth gone wrong
//     */
//    public static byte[] SAMtoWaveBytes (String words) throws Exception
//    {
//        words = words.replace('-','_');
//        String res = Utilities.extractResource("sam.exe", false);
//        ProcessBuilder pb = new ProcessBuilder(
//                res, "-stdout", "dummy", words);
//        Process p = pb.start();
//
//        InputStream is = p.getInputStream();
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        while (true)
//        {
//            int r = is.read(buffer);
//            if (r == -1) break;
//            out.write(buffer, 0, r);
//        }
//
//        return out.toByteArray();
//    }

    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Load wave file and start playing
     * @param file the file object
     * @return tha running clip
     * @throws Exception if smth gone wrong
     */
    public static Clip playWave (File file, boolean cont) throws Exception
    {
        final Clip clip = (Clip) AudioSystem.getLine (new Line.Info (Clip.class));
        clip.open (AudioSystem.getAudioInputStream (file));
        if (cont)
            clip.loop (Clip.LOOP_CONTINUOUSLY);
        clip.start ();
        return clip;
    }

    public static Clip playWave (byte[] data, boolean cont) throws Exception
    {
        final Clip clip = (Clip) AudioSystem.getLine (new Line.Info (Clip.class));
        InputStream inp  = new BufferedInputStream(new ByteArrayInputStream (data));
        clip.open (AudioSystem.getAudioInputStream (inp));
        if (cont)
            clip.loop (Clip.LOOP_CONTINUOUSLY);
        clip.start ();
        return clip;
    }

    /**
     * Stop and close an audio clip
     * @param clip the playing clip
     */
    public static void stopWave (Clip clip)
    {
        clip.stop ();
        clip.close ();
    }
}
