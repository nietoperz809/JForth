package jforth;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import scala.math.BigInt;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 3/21/2017.
 */
public class Utilities
{
    public static final String BUILD_NUMBER = "333";
    public static final String BUILD_DATE = "04/15/2017 10:20:51 AM";

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

    static Fraction parseFraction (String in)
    {
        String[] parts = in.split("/");
        if (parts.length != 2)
            return null;
        return new Fraction (Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    static String formatFraction (Fraction f)
    {
        return f.getNumerator()+"/"+f.getDenominator();
    }

    static boolean del (String path)
    {
        File f = new File(path);
        return f.delete();
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

    public static Object deepCopy( Object o )
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new ObjectOutputStream(baos).writeObject(o);

            ByteArrayInputStream bais =
                    new ByteArrayInputStream(baos.toByteArray());

            return new ObjectInputStream(bais).readObject();
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static String removeTrailingZero (double v)
    {
        String str = ""+v;
        if (str.endsWith(".0"))
            return str.substring(0, str.length()-2);
        return str;
    }

    public static Long parseLong (String word, int base)
    {
        try
        {
            return Long.parseLong(word, base);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    public static Double parseDouble (String word)
    {
        try
        {
            return Double.parseDouble(word);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    public static void saveObject (String name, Object obj) throws IOException
    {
        String j1 = JsonWriter.objectToJson(obj);
        PrintWriter p = new PrintWriter(name + ".json");
        p.println(JsonWriter.formatJson(j1));
        p.close();
    }

    public static Object loadObject (String name) throws IOException
    {
        byte[] b = Files.readAllBytes(Paths.get(name + ".json"));
        String s = new String(b);
        return JsonReader.jsonToJava(s);
    }

    public static List<String> splitEqually(String text, int size)
    {
        try
        {
            List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);
            for (int start = 0; start < text.length(); start += size)
            {
                ret.add(text.substring(start, Math.min(text.length(), start + size)));
            }
            return ret;
        }
        catch (Exception unused)
        {
            return null;
        }
    }

    public static List<DoubleSequence> splitEqually(DoubleSequence text, int size)
    {
        try
        {
            List<DoubleSequence> ret = new ArrayList<DoubleSequence>((text.length() + size - 1) / size);
            for (int start = 0; start < text.length(); start += size)
            {
                ret.add(text.subList(start, Math.min(text.length(), start + size)));
            }
            return ret;
        }
        catch (Exception unused)
        {
            return null;
        }
    }

    public static String parseString (String in)
    {
        if (in.length() < 2)
            return null;
        if (!in.startsWith("\""))
            return null;
        if (!in.endsWith("\""))
            return null;
        return in.substring(1, in.length()-1);
    }

    public static PolynomialFunction antiDerive (PolynomialFunction p)
    {
        double[] in = p.getCoefficients();
        double[] out = new double[in.length+1];
        out[0] = 0.0; // constant
        for (int s=1; s<out.length; s++)
        {
            out[s] = in[s-1]/s;
        }
        return new PolynomialFunction(out);
    }

    public static String rotRight (String in, int num)
    {
        while (num-- != 0)
        {
            char last = in.charAt(in.length() - 1);
            in = "" + last + in.substring(0, in.length() - 1);
        }
        return in;
    }

    public static String rotLeft (String in, int num)
    {
        while (num-- != 0)
        {
            char last = in.charAt(0);
            in = in.substring(1, in.length())+last;
        }
        return in;
    }

    public static double readDouble (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        if (o instanceof BigInt)
        {
            return ((BigInt)o).doubleValue();
        }
        if (o instanceof Double)
        {
            return (Double)o;
        }
        if (o instanceof Long)
        {
            return ((Long)o).doubleValue();
        }
        throw new Exception ("Wrong or no Type on Stack");
    }

    public static long readLong (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        if (o instanceof BigInt)
        {
            return ((BigInt)o).longValue();
        }
        if (o instanceof Double)
        {
            return ((Double)o).longValue();
        }
        if (o instanceof Long)
        {
            return (Long)o;
        }
        throw new Exception ("Wrong or no Type on Stack");
    }

    public static String readString (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        if (o instanceof String)
        {
            return (String)o;
        }
        if (o instanceof Long)
        {
            return ((Long)o).toString();
        }
        if (o instanceof Double)
        {
            return ((Double)o).toString();
        }
        if (o instanceof BigInt)
        {
            return ((BigInt)o).toString();
        }
        throw new Exception ("Wrong or no Type on Stack");
    }

    public static DoubleSequence readDoubleSequence (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        if (o instanceof DoubleSequence)
        {
            return (DoubleSequence)o;
        }
        throw new Exception ("Wrong or no Type on Stack");
    }

}
