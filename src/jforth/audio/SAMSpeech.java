package jforth.audio;

import tools.ResourceLoader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

public class SAMSpeech
{
    static Method meth;

    /*
     * Load the sam!
     */
    static
    {
        try
        {
            byte[] clbytes = ResourceLoader.extractResource ("SamClass");
            Class<?> cl = ResourceLoader.loadClassfromArray (clbytes, "samtool.SamClass");
            meth = cl.getMethod("xmain", PrintStream.class, String[].class);
        }
        catch (Exception e)
        {
            System.out.println ("Init failed: "+e);
        }
    }

    /**
     * Generate WAV from text input using SAM
     * @param txt text to speak
     * @return WAV data
     * @throws Exception if smth. went wrong
     */
    public static byte[] doSam (String txt) throws Exception
    {
        String[] arg = {"-stdout","dummy",txt};
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        PrintStream p = new PrintStream (ba);
        meth.invoke(null, p, arg);
        return ba.toByteArray ();
    }
}
