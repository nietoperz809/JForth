package jforth;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.charset.Charset;

/**
 * from here: ftp://sccn.ucsd.edu/pub/virtualmedia/AePlayWave.java
 */

public class WavePlayer extends Thread
{
    private SourceDataLine auline = null;
    private FloatControl pan;
    private BufferedInputStream istream;
    public static final Charset thisCharset = Charset.forName("ISO-8859-1");

    public static String toWaveString (String words) throws Exception
    {
        String res = Utilities.extractResource("sam.exe");
        Process process = new ProcessBuilder(
                res, "-stdout", "dummy", words)
                .start();
        InputStream is = process.getInputStream();

        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (is, thisCharset)))
        {
            int c = 0;
            while ((c = reader.read()) != -1)
            {
                textBuilder.append((char) c);
            }
        }
        is.close();
        return textBuilder.toString();
    }

    public void loadFile (String wavfile) throws Exception
    {
        File soundFile = new File(wavfile);
        istream = new BufferedInputStream(new FileInputStream(soundFile));
    }

    public void loadString (String data) throws Exception
    {
        istream = new BufferedInputStream(new ByteArrayInputStream (data.getBytes(thisCharset)));
    }

    public void setVolume (float value)
    {
        pan.setValue(value);
    }

    public void run ()
    {
        AudioInputStream audioInputStream = null;
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
            pan = (FloatControl) auline
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
