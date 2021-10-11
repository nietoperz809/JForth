package jforth.audio;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;

/**
 * from here: ftp://sccn.ucsd.edu/pub/virtualmedia/AePlayWave.java
 */

public class WaveTools {
    /**
     * Put a WAV file header audio data
     *
     * @param data   The audio
     * @param format The audio format
     * @return A complete wave file with header.
     * @throws Exception if smth. gone wrong
     */
    public static byte[] withWAVHeader(byte[] data, AudioFormat format) throws Exception {
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), format, data.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, baos);
        return baos.toByteArray();
    }

    /**
     * Load wave file and start playing
     *
     * @param file the file object
     * @return tha running clip
     * @throws Exception if smth gone wrong
     */
    public static Clip playWave(File file, boolean cont) throws Exception {
        byte[] buff = Files.readAllBytes(file.toPath());
        return playWave(buff, cont);
    }

    public static Clip playWave(byte[] data, boolean cont) throws Exception {
        final Clip clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
        InputStream inp = new BufferedInputStream(new ByteArrayInputStream(data));
        clip.open(AudioSystem.getAudioInputStream(inp));
        if (cont)
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
        return clip;
    }

    /**
     * Stop and close an audio clip
     *
     * @param clip the playing clip
     */
    public static void stopWave(Clip clip) {
        clip.stop();
        clip.close();
    }
}
