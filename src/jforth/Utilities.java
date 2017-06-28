package jforth;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import scala.math.BigInt;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 3/21/2017.
 */
public class Utilities
{
    public static final String BUILD_NUMBER = "680";
    public static final String BUILD_DATE = "06/28/2017 04:49:29 PM";

    public static final String buildInfo = "JForth, Build: " + Utilities.BUILD_NUMBER + ", " + Utilities.BUILD_DATE;

    static String formatComplex (Complex c)
    {
        double re = c.getReal();
        double im = c.getImaginary();
        if (im == 0.0)
        {
            return ("" + re);
        }
        return ("" + re + "+" + im + "i");
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
        {
            return null;
        }
        if (!parts[1].endsWith("i"))
        {
            return null;
        }
        parts[1] = parts[1].substring(0, parts[1].length() - 1);
        if (negreal)
        {
            parts[0] = "-" + parts[0];
        }
        if (in.substring(1).contains("-"))
        {
            parts[1] = "-" + parts[1];
        }
        return new Complex(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    static Fraction parseFraction (String in)
    {
        String[] parts = in.split("/");
        if (parts.length != 2)
        {
            return null;
        }
        return new Fraction(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    static String formatFraction (Fraction f)
    {
        return f.getNumerator() + "/" + f.getDenominator();
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
        {
            return "";
        }
        Arrays.sort(filesInFolder, (f1, f2) ->
        {
            if (f2.isDirectory())
            {
                return 1;
            }
            return -1;
        });
        for (final File fileEntry : filesInFolder)
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

    public static Object deepCopy (Object o)
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
        return removeTrailingZero(v, false);
    }

    public static String removeTrailingZero (double v, boolean round)
    {
        String str;
        if (round)
        {
            v = Math.round(10000.0 * v) / 10000.0;
        }
        str = "" + v;
        if (str.endsWith(".0"))
        {
            return str.substring(0, str.length() - 2);
        }
        return str;
    }

    public static BigInt parseBigInt (String word, int base)
    {
        try
        {
            if (word.endsWith("L"))
            {
                word = word.substring(0, word.length() - 1);
                return BigInt.apply(word, base);
            }
            return null;
        }
        catch (Exception ignored)
        {
            return null;
        }
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

    public static List<String> splitEqually (String text, int size)
    {
        try
        {
            List<String> ret = new ArrayList<>((text.length() + size - 1) / size);
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

    public static List<DoubleSequence> splitEqually (DoubleSequence text, int size)
    {
        try
        {
            List<DoubleSequence> ret = new ArrayList<>((text.length() + size - 1) / size);
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
        {
            return null;
        }
        if (!in.startsWith("\""))
        {
            return null;
        }
        if (!in.endsWith("\""))
        {
            return null;
        }
        return in.substring(1, in.length() - 1);
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
            in = in.substring(1, in.length()) + last;
        }
        return in;
    }

    public static double readDouble (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        return Calculator.getDouble(o);
    }

    public static long readLong (OStack dStack) throws Exception
    {
        return getLong(dStack.pop());
    }

    public static long getLong (Object o) throws Exception
    {
        if (o instanceof BigInt)
        {
            return ((BigInt) o).longValue();
        }
        if (o instanceof Double)
        {
            return ((Double) o).longValue();
        }
        if (o instanceof Long)
        {
            return (Long) o;
        }
        throw new Exception("Wrong or no Type on Stack");
    }

    public static BigInt readBig (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        return Calculator.getBig(o);
    }


    public static String readString (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        if (o instanceof String)
        {
            return (String) o;
        }
        if (o instanceof Long)
        {
            return o.toString();
        }
        if (o instanceof Double)
        {
            return o.toString();
        }
        if (o instanceof BigInt)
        {
            return o.toString();
        }
        throw new Exception("Wrong or no Type on Stack");
    }

    public static Complex readComplex (OStack dStack) throws Exception
    {
        return Calculator.getComplex(dStack.pop());
    }

    public static double[] parseCSVtoDoubleArray (String in)
    {
        String vals[] = in.split(",");
        double[] out = new double[vals.length];
        for (int s = 0; s < vals.length; s++)
        {
            out[s] = Double.parseDouble(vals[s]);
        }
        return out;
    }

    public static DoubleSequence readDoubleSequence (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        if (o instanceof DoubleSequence)
        {
            return (DoubleSequence) o;
        }
        if (o instanceof String)
        {
            return new DoubleSequence((String) o);
        }
        throw new Exception("Wrong or no Type on Stack");
    }
}
