package jforth;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import scala.math.BigInt;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by Administrator on 3/21/2017.
 */
public class Utilities
{
    public static final String BUILD_NUMBER = "801";
    public static final String BUILD_DATE = "09/25/2017 11:18:16 AM";

    public static final String buildInfo = "JForth, Build: " + Utilities.BUILD_NUMBER + ", " + Utilities.BUILD_DATE;

    static void terminate (int delay)
    {
        new Thread(() ->
        {
            try
            {
                Thread.sleep(delay);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            System.exit(0);
        }).start();
    }

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

    static Complex parseComplex (String in, int base)
    {
        if (base != 10)
            return null;
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

    static Fraction parseFraction (String in, int base)
    {
        if (base != 10)
            return null;
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

    public static Double parseDouble (String word, int base)
    {
        if (base != 10)
            return null;
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
        return getDouble(o);
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
        if (o instanceof Fraction)
        {
            int denom = ((Fraction) o).getDenominator();
            int nume = ((Fraction) o).getNumerator();
            if (nume%denom == 0)
            {
                return nume/denom;
            }
        }
        throw new Exception("Wrong or no Type on Stack");
    }

    public static BigInt readBig (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        return getBig(o);
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
        return getComplex(dStack.pop());
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

    static Fraction pow (Fraction f, Long n)
    {
        int denom = (int)Math.pow (f.getDenominator(), n);
        int num = (int)Math.pow (f.getNumerator(), n);
        return Fraction.getReducedFraction(num, denom);
    }

    static BigInt pow (BigInt a, BigInt b)
    {
        return a.pow(b.intValue());
    }

    static Double add (Double a, Double b)
    {
        return a + b;
    }

    static DoubleMatrix add (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.add(b);
        return new DoubleMatrix(res);
    }

    static DoubleMatrix sub (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.subtract(b);
        return new DoubleMatrix(res);
    }

    static DoubleMatrix mult (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.multiply(b);
        return new DoubleMatrix(res);
    }

    static DoubleMatrix div (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.multiply(MatrixUtils.inverse(b));
        return new DoubleMatrix(res);
    }

    static Double sub (Double a, Double b)
    {
        return a - b;
    }

    static Double mult (Double a, Double b)
    {
        return a * b;
    }

    static Double div (Double a, Double b)
    {
        return a / b;
    }

    static Double doCalcDouble (Object o1, Object o2, BiFunction<Double, Double, Double> func) throws Exception
    {
        if (o1 instanceof Double || o2 instanceof Double)
        {
            return func.apply(getDouble(o1), getDouble(o2));
        }
        throw new Exception("Wrong args");
    }

    static public Double getDouble (Object o1) throws Exception
    {
        if (o1 instanceof BigInt)
        {
            return ((BigInt)o1).doubleValue();
        }
        if (o1 instanceof Double)
        {
            return (Double) o1;
        }
        if (o1 instanceof Long)
        {
            return ((Long) o1).doubleValue();
        }
        if (o1 instanceof Fraction)
        {
            double denom = ((Fraction) o1).getDenominator();
            double nume = ((Fraction) o1).getNumerator();
            return nume/denom;
        }
        throw new Exception("Wrong args");
    }

    static Complex doCalcComplex (Object o1, Object o2, BiFunction<Complex, Complex, Complex> func) throws Exception
    {
        if (areBothObjectsOfType(o1, o2, Complex.class))
        {
            return func.apply(getComplex(o1), getComplex(o2));
        }
        throw new Exception("Wrong args");
    }

    private static boolean areBothObjectsOfType (Object o1, Object o2, Class c)
    {
        return (c.isInstance(o1) || c.isInstance(o2));
    }

    static public Complex getComplex (Object o1) throws Exception
    {
        if (o1 instanceof Complex)
        {
            return (Complex) o1;
        }
        if (o1 instanceof Long)
        {
            return new Complex((Long) o1);
        }
        else if (o1 instanceof Double)
        {
            return new Complex((Double) o1);
        }
        else if (o1 instanceof BigInt)
        {
            return new Complex(((BigInt) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    static Fraction doCalcFraction (Object o1, Object o2, BiFunction<Fraction, Fraction, Fraction> func) throws Exception
    {
        if (areBothObjectsOfType(o1, o2, Fraction.class))
        {
            return func.apply(getFrac(o1), getFrac(o2));
        }
        throw new Exception("Wrong args");
    }

    static private Fraction getFrac (Object o1) throws Exception
    {
        if (o1 instanceof Fraction)
        {
            return (Fraction) o1;
        }
        if (o1 instanceof Long)
        {
            return new Fraction((Long) o1);
        }
        if (o1 instanceof Double)
        {
            return new Fraction((Double) o1);
        }
        if (o1 instanceof BigInt)
        {
            return new Fraction(((BigInt) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    static BigInt doCalcBigInt (Object o1, Object o2, BiFunction<BigInt, BigInt, BigInt> func) throws Exception
    {
        if (areBothObjectsOfType(o1, o2, BigInt.class))
        {
            return func.apply(getBig(o1), getBig(o2));
        }
        throw new Exception("Wrong args");
    }

    static DoubleMatrix doCalcMatrix (Object o1, Object o2, BiFunction<DoubleMatrix, DoubleMatrix, DoubleMatrix> func) throws Exception
    {
        if (areBothObjectsOfType(o1, o2, DoubleMatrix.class))
        {
            return func.apply(getMatrix(o1), getMatrix(o2));
        }
        throw new Exception("Wrong args");
    }

    static public BigInt getBig (Object o1) throws Exception
    {
        if (o1 instanceof BigInt)
        {
            return (BigInt) o1;
        }
        if (o1 instanceof Long)
        {
            return BigInt.apply((Long) o1);
        }
        if (o1 instanceof Double)
        {
            return BigInt.apply(((Double) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    static private DoubleMatrix getMatrix (Object o1) throws Exception
    {
        if (o1 instanceof DoubleMatrix)
        {
            return (DoubleMatrix) o1;
        }
        throw new Exception("Wrong args");
    }
}
