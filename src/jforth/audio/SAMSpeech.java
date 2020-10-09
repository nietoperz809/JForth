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
            byte[] clbytes = ResourceLoader.extractResource ("samclass.class");
            Class<?> cl = ResourceLoader.loadClassfromArray (clbytes, "samclass");
            meth = cl.getMethod("xmain", PrintStream.class, String[].class);
        }
        catch (Exception e)
        {
            System.out.println ("Init failed: "+e);
        }
    }

    private static void swap2 (byte[] in, int off)
    {
        byte tmp = in[0+off];
        in[0+off] = in[1+off];
        in[1+off] = tmp;
    }

    private static void swap4 (byte[] in, int off)
    {
        byte tmp = in[0+off];
        in[0+off] = in[3+off];
        in[3+off] = tmp;
        tmp = in[1+off];
        in[1+off] = in[2+off];
        in[2+off] = tmp;
    }

    /**
     * Genetrate WAV from text input using SAM
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
        byte[] result = ba.toByteArray ();
        swap4 (result, 4);
        swap4 (result, 16);
        swap2 (result, 20);
        swap2 (result, 22);
        swap4 (result, 24);
        swap4 (result, 28);
        swap2 (result, 32);
        swap2 (result, 34);
        swap4 (result, 40);
        result[44] = (byte)0xcd;
        result[45] = (byte)0xcd;
        result[46] = (byte)0xcd;
        return result;
    }
}
