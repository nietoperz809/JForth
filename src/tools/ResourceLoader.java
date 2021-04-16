package tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.util.Objects;


public class ResourceLoader
{
    public static Class<?> loadClassfromArray (byte[] arr, String name) throws ClassNotFoundException
    {
        ByteArrayClassLoader loa = new ByteArrayClassLoader (arr);
        return loa.loadClass (name);
    }

    /**
     * Copy resource from jar to temp folder
     *
     * @param name name of resource
     * @return Full path to extracted file
     * @throws IOException if smth gone wrong
     */
    static public String extractResource (String name, boolean overwrite) throws IOException
    {
        String tempName = System.getProperty ("java.io.tmpdir") + name;
        if (!new File (tempName).exists () || overwrite)
        {
            InputStream is = ClassLoader.getSystemResourceAsStream (name);
            BufferedInputStream bis = new BufferedInputStream (Objects.requireNonNull (is));
            OutputStream os = new FileOutputStream (tempName);
            byte[] buff = new byte[1024];
            for (; ; )
            {
                int r = bis.read (buff);
                if (r == -1)
                {
                    break;
                }
                os.write (buff, 0, r);
            }
            bis.close ();
            os.close ();
        }
        return tempName;
    }

    public static Image loadImageFromRessource (String name)
    {
        InputStream is = ClassLoader.getSystemResourceAsStream (name);
        try
        {
            return ImageIO.read(is);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get byte array from resource bundle
     * @param name what resource
     * @return the resource as byte array
     * @throws Exception if smth. went wrong
     */
    static public byte[] extractResource (String name) throws Exception
    {
        InputStream is = ClassLoader.getSystemResourceAsStream (name);

        ByteArrayOutputStream out = new ByteArrayOutputStream ();
        byte[] buffer = new byte[1024];
        while (true)
        {
            int r = is.read (buffer);
            if (r == -1)
            {
                break;
            }
            out.write (buffer, 0, r);
        }

        return out.toByteArray ();
    }
}
