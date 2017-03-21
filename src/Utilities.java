import org.apache.commons.math3.complex.Complex;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Administrator on 3/21/2017.
 */
public class Utilities
{
    static String formatComplex (Complex c)
    {
        double re = c.getReal();
        double im = c.getImaginary();
        if (im<0.0)
            return (""+re+""+im+"i");
        return (""+re+"+"+im+"i");
    }

    static Complex parseComplex (String in)
    {
        boolean negreal = false;
        if (in.startsWith("-"))
        {
            in = in.substring(1);
            negreal = true;
        }
        String[] parts = in.split("(-)|(\\+)");
        if (parts.length != 2)
            return null;
        if (!parts[1].endsWith("i"))
            return null;
        parts[1] = parts[1].substring(0, parts[1].length()-1);
        if (negreal)
            parts[0] = "-"+parts[0];
        if (in.substring(1).contains("-"))
            parts[1] = "-"+parts[1];
        return new Complex (Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    static String dir (String path)
    {
        StringBuilder sb = new StringBuilder();
        File[] filesInFolder = new File(path).listFiles();
        if (filesInFolder == null)
            return "";
        Arrays.sort(filesInFolder, new Comparator<File>()
        {
            public int compare(File f1, File f2)
            {
                if (f2.isDirectory())
                    return 1;
                return -1;
            }
        });
        for (final File fileEntry :  filesInFolder)
        {
            String formatted;
            if (fileEntry.isDirectory())
            {
                formatted = String.format("<%s>\n", fileEntry.getName());
            }
            else
            {
                formatted = String.format("%-15s = %d\n", fileEntry.getName(), fileEntry.length());
            }
            sb.append(formatted);
        }
        return sb.toString();
    }

}
