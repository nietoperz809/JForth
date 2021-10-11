package jforth.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * from here: ftp://sccn.ucsd.edu/pub/virtualmedia/AePlayWave.java
 */

public class WaveTools
{
    /**
     * Load wave file and start playing
     * @param file the file object
     * @return tha running clip
     * @throws Exception if smth gone wrong
     */
    public static Clip playWave (File file, boolean cont) throws Exception
    {
        byte[] buff = Files.readAllBytes(file.toPath());
        return playWave(buff, cont);
//        final Clip clip = (Clip) AudioSystem.getLine (new Line.Info (Clip.class));
//        clip.open (AudioSystem.getAudioInputStream (file));
//        if (cont)
//            clip.loop (Clip.LOOP_CONTINUOUSLY);
//        clip.start ();
//        return clip;
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
