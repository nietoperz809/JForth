package jforth;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.charset.Charset;

/**
 * from here: ftp://sccn.ucsd.edu/pub/virtualmedia/AePlayWave.java
 */

public class WavePlayer extends Thread
{
    private BufferedInputStream istream;
    private static final Object syncObject = new Object();
    private static final Charset thisCharset = Charset.forName("ISO-8859-1");

    /**
     * Run the SAM module and convert a text to speech data
     * @param words Text to be spoken
     * @return String containing wave file
     * @throws Exception if smth gone wrong
     */
    public static String toWaveString (String words) throws Exception
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

    /**
     * LOad a file to be spoken
     * @param wavfile file path
     * @throws Exception if smth gone wrong
     */
    public void loadFile (String wavfile) throws Exception
    {
        File soundFile = new File(wavfile);
        istream = new BufferedInputStream(new FileInputStream(soundFile));
    }

    /**
     * load a string to be spoken
     * @param data the string containing a wave file
     */
    public void loadString (String data)
    {
        istream = new BufferedInputStream(new ByteArrayInputStream (data.getBytes(thisCharset)));
    }

// --Commented out by Inspection START (1/28/2018 7:46 PM):
//    public void setVolume (float value)
//    {
//        pan.setValue(value);
//    }
// --Commented out by Inspection STOP (1/28/2018 7:46 PM)

    /**
     * Thread starting point
     */
    public void run ()
    {
        synchronized (syncObject)
        {
            internalRun();
        }
    }

    /**
     * Player code executed inside thread
     */
    private void internalRun ()
    {
        AudioInputStream audioInputStream;
        try
        {
            audioInputStream = AudioSystem.getAudioInputStream(istream);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
            return;
        }

        AudioFormat format = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        SourceDataLine auline;
        try
        {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        if (auline.isControlSupported(FloatControl.Type.SAMPLE_RATE))
        {
            FloatControl pan = (FloatControl) auline
                    .getControl(FloatControl.Type.SAMPLE_RATE);
            pan.setValue(44000.0f);
        }

        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[524288];

        try
        {
            while (nBytesRead != -1)
            {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0)
                {
                    auline.write(abData, 0, nBytesRead);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            auline.drain();
            auline.close();
        }
    }
}
