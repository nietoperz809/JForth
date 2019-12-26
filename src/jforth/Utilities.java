package jforth;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import tools.TwoFuncs;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.function.BiFunction;

/**
 * Created by Administrator on 3/21/2017.
 */
public class Utilities
{
    private static final String BUILD_NUMBER = "1540";
    private static final String BUILD_DATE = "12/26/2019 11:13:30 PM";

    public static final String buildInfo = "JForth, Build: " + Utilities.BUILD_NUMBER + ", " + Utilities.BUILD_DATE
            + " -- " + System.getProperty("java.version");


    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String printHexBinary(byte[] data)
    {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    public static int hexToBin(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        }
        return -1;
    }

    public static byte[] parseHexBinary(String s) {
        final int len = s.length();

        // "111" is not a valid hex encoding.
        if (len % 2 != 0) {
            throw new IllegalArgumentException("hexBinary needs to be even-length: " + s);
        }

        byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int h = hexToBin(s.charAt(i));
            int l = hexToBin(s.charAt(i + 1));
            if (h == -1 || l == -1) {
                throw new IllegalArgumentException("contains illegal character for hexBinary: " + s);
            }

            out[i / 2] = (byte) (h * 16 + l);
        }

        return out;
    }

    public static BigInteger factorial (long n)
    {
        BigInteger fact = BigInteger.valueOf(1);
        for (int i = 1; i <= n; i++)
        {
            fact = fact.multiply (BigInteger.valueOf(i));
        }
        return fact;
    }

    public static BigInteger fib(long n)
    {
        BigInteger a = BigInteger.valueOf(0);
        BigInteger b = BigInteger.valueOf(1);
        BigInteger c;
        for (long j=2 ; j<=n ; j++)
        {
            c =  a.add(b);
            a = b;
            b = c;
        }

        return a;
    }

    /**
     * Makes seconds from hh:mm:ss format
     * @param in input string
     * @return value in seconds
     */
    public static Long parseTimer (String in)
    {
        String[] parts = in.split(":");
        if (parts.length != 3)
            return null;
        try
        {
            int h = Integer.parseInt(parts[0]);
            if (h<0)
                return null;
            int m = Integer.parseInt(parts[1]);
            if (m<0 || m>59)
                return null;
            int s = Integer.parseInt(parts[2]);
            if (s<0 || s>59)
                return null;
            return (long)(3600*h + 60*m + s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public static String toTimeView (Long in)
    {
        int h = (int)(in/3600);
        int s = (int)(in%3600);
        int m = (int)(s/60);
        s %= 60;
        return String.format("%d:%02d:%02d", h,m,s);
    }

    public static byte[] toRawByteArray (String in)
    {
        char[] chars = in.toCharArray();
        byte[] bytes = new byte[chars.length*2];
        for(int i=0;i<chars.length;i++)
        {
            bytes[i*2] = (byte) (chars[i] >> 8);
            bytes[i*2+1] = (byte) chars[i];
        }
        return bytes;
    }

    public static char[] fromRawByteArray (byte[] bytes)
    {
        char[] chars2 = new char[bytes.length/2];
        for(int i=0;i<chars2.length;i++)
            chars2[i] = (char) ((bytes[i*2] << 8) + (bytes[i*2+1] & 0xFF));
        return chars2;
    }

    public static void terminateSoon (int delay)
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

    public static String formatDouble (Double d)
    {
        String outstr = Double.toString(d);
        if (outstr.endsWith(".0"))
            outstr = outstr.substring(0, outstr.length() - 2);
        if (outstr.equals("-0"))
            outstr = "0";
        return outstr;
    }

    public static String formatComplex (Complex c)
    {
        String re = formatDouble(c.getReal());
        String im = formatDouble(c.getImaginary());
        if (im.equals("0"))
        {
            return re;
        }
        if (im.charAt(0)=='-')
            return re + im + "i";
        return re + "+" + im + "i";
    }

    static Complex parseComplex (String in, int base)
    {
        if (base != 10)
        {
            return null;
        }
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
        {
            return null;
        }
        String[] parts = in.split("/");
        if (parts.length != 2)
        {
            return null;
        }
        try
        {
            return new Fraction(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public static String formatFraction (Fraction f)
    {
        if (f.getDenominator() == 1)
            return ""+f.getNumerator();
        return f.getNumerator() + "/" + f.getDenominator();
    }

    public static boolean del (String path)
    {
        File f = new File(path);
        return f.delete();
    }

    public static String dir (String path)
    {
        StringBuilder sb = new StringBuilder();
        path = path.replace('/','\\');
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

// --Commented out by Inspection START (10/3/2017 10:45 AM):
//    public static Object deepCopy (Object o)
//    {
//        try
//        {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            new ObjectOutputStream(baos).writeObject(o);
//
//            ByteArrayInputStream bais =
//                    new ByteArrayInputStream(baos.toByteArray());
//
//            return new ObjectInputStream(bais).readObject();
//        }
//        catch (Exception ex)
//        {
//            return null;
//        }
//    }
// --Commented out by Inspection STOP (10/3/2017 10:45 AM)

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

    public static BigInteger parseBigInt (String word, int base)
    {
        try
        {
            if (word.endsWith("L"))
            {
                word = word.substring(0, word.length() - 1);
                return new BigInteger (word, base);
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
            return parseTimer (word);
        }
    }

    public static Double parseDouble (String word, int base)
    {
        if (base != 10)
        {
            return null;
        }
        try
        {
            return Double.parseDouble(word);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

// --Commented out by Inspection START (10/3/2017 10:45 AM):
//    public static void saveObject (String name, Object obj) throws IOException
//    {
//        String j1 = JsonWriter.objectToJson(obj);
//        PrintWriter p = new PrintWriter(name + ".json");
//        p.println(JsonWriter.formatJson(j1));
//        p.close();
//    }
// --Commented out by Inspection STOP (10/3/2017 10:45 AM)

// --Commented out by Inspection START (10/3/2017 10:45 AM):
//    public static Object loadObject (String name) throws IOException
//    {
//        byte[] b = Files.readAllBytes(Paths.get(name + ".json"));
//        String s = new String(b);
//        return JsonReader.jsonToJava(s);
//    }
// --Commented out by Inspection STOP (10/3/2017 10:45 AM)

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
            in = in.substring(1) + last;
        }
        return in;
    }

    public static double readDouble (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        return getDouble(o);
    }

    static public boolean canBeDouble (Object o1)
    {
        if (o1 instanceof BigInteger)
        {
            return true;
        }
        if (o1 instanceof Double)
        {
            return true;
        }
        if (o1 instanceof Long)
        {
            return true;
        }
        if (o1 instanceof Fraction)
        {
            return true;
        }
        if (o1 instanceof Complex)
        {
            return true;
        }
        return false;
    }

    static public Double getDouble (Object o1) throws Exception
    {
        if (o1 instanceof BigInteger)
        {
            return ((BigInteger) o1).doubleValue();
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
            return nume / denom;
        }
        if (o1 instanceof Complex)
        {
            Complex c = (Complex)o1;
            if (c.getImaginary() == 0.0)
                return c.getReal();
        }
        throw new Exception("Wrong args");
    }

    public static long readLong (OStack dStack) throws Exception
    {
        return getLong(dStack.pop());
    }

    public static FileInputStream readFileInputStream (OStack dStack)
    {
        return (FileInputStream)dStack.pop();
    }


    public static long getLong (Object o) throws Exception
    {
        if (o instanceof BigInteger)
        {
            return ((BigInteger) o).longValue();
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
            if (nume % denom == 0)
            {
                return nume / denom;
            }
        }
        if (o instanceof Complex)
        {
            Complex c = (Complex)o;
            return ((Double)c.getReal()).longValue();
        }
        throw new Exception("Wrong or no Type on Stack");
    }

    public static BigInteger readBig (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        return getBig(o);
    }

    private static BigInteger getBig (Object o1) throws Exception
    {
        if (o1 instanceof BigInteger)
        {
            return (BigInteger) o1;
        }
        if (o1 instanceof Long)
        {
            return BigInteger.valueOf((Long) o1);
        }
        if (o1 instanceof Double)
        {
            return BigInteger.valueOf(((Double) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    public static void fileSave (ArrayList<String> as, String filename) throws Exception
    {
            FileWriter fw = new FileWriter(filename);
            for (String str : as)
            {
                fw.write(str + "\n");
            }
            fw.close();
    }

    public static ArrayList<String> fileLoad (String fileName) throws Exception
    {
        ArrayList<String> ret = new ArrayList<>();
        BufferedReader file = new BufferedReader(new FileReader(fileName));
        while (file.ready())
        {
            String s = file.readLine();
            if (s == null)
            {
                break;
            }
            s = s.trim();
            if (!s.isEmpty())
            {
                ret.add(s);
            }
        }
        file.close();
        return ret;
    }

    public static String readStringOrNull (OStack dstack)
    {
        try
        {
            return readString (dstack);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static String readString (OStack dStack) throws Exception
    {
        Object o = dStack.pop();
        if (o instanceof String)
        {
            return StringEscape.unescape((String) o);
        }
        if (o instanceof Long)
        {
            return o.toString();
        }
        if (o instanceof Double)
        {
            return o.toString();
        }
        if (o instanceof BigInteger)
        {
            return o.toString();
        }
        throw new Exception("Wrong or no Type on Stack");
    }

    public static Complex readComplex (OStack dStack) throws Exception
    {
        return getComplex(dStack.pop());
    }

    private static Complex getComplex (Object o1) throws Exception
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
        else if (o1 instanceof BigInteger)
        {
            return new Complex(((BigInteger) o1).longValue());
        }
        throw new Exception("Wrong args");
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

    public static Vector3D readVector3D (OStack dStack) throws Exception
    {
        DoubleSequence ds = readDoubleSequence(dStack);
        double i2;
        if (ds.length() > 3 || ds.length() < 2)
            throw new Exception("wrong size");
        if (ds.length() == 2)
            i2 = 0;
        else
            i2 = ds.pick(2);
        return new Vector3D(ds.pick(0), ds.pick(1), i2);
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

    public static Fraction pow (Fraction f, Long n)
    {
        int denom = (int) Math.pow(f.getDenominator(), n);
        int num = (int) Math.pow(f.getNumerator(), n);
        return Fraction.getReducedFraction(num, denom);
    }

    public static BigInteger pow (BigInteger a, BigInteger b)
    {
        return a.pow(b.intValue());
    }

    public static Double add (Double a, Double b)
    {
        return a + b;
    }

    public static DoubleMatrix add (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.add(b);
        return new DoubleMatrix(res);
    }

    public static DoubleMatrix sub (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.subtract(b);
        return new DoubleMatrix(res);
    }

    public static DoubleMatrix mult (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.multiply(b);
        return new DoubleMatrix(res);
    }

    public static DoubleMatrix div (DoubleMatrix a, DoubleMatrix b)
    {
        BlockRealMatrix res = a.multiply(MatrixUtils.inverse(b));
        return new DoubleMatrix(res);
    }

    public static Double sub (Double a, Double b)
    {
        return a - b;
    }

    public static Double mult (Double a, Double b)
    {
        return a * b;
    }

    public static Double div (Double a, Double b)
    {
        return a / b;
    }

    public static Double doCalcDouble (Object o1, Object o2, TwoFuncs<Double, Double, Double> func) throws Exception
    {
        if (o1 instanceof Double || o2 instanceof Double)
        {
            return func.apply(getDouble(o1), getDouble(o2));
        }
        throw new Exception("Wrong args");
    }

    public static Complex doCalcComplex (Object o1, Object o2, TwoFuncs<Complex, Complex, Complex> func) throws Exception
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

    public static Fraction doCalcFraction (Object o1, Object o2, TwoFuncs<Fraction, Fraction, Fraction> func) throws Exception
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
        if (o1 instanceof BigInteger)
        {
            return new Fraction(((BigInteger) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    public static BigInteger doCalcBigInt (Object o1,
                                           Object o2,
                                           TwoFuncs<BigInteger, BigInteger, BigInteger> func) throws Exception
    {
        if (areBothObjectsOfType(o1, o2, BigInteger.class))
        {
            return func.apply(getBig(o1), getBig(o2));
        }
        throw new Exception("Wrong args");
    }

    public static DoubleMatrix doCalcMatrix (Object o1, Object o2, TwoFuncs<DoubleMatrix, DoubleMatrix, DoubleMatrix> func) throws Exception
    {
        if (areBothObjectsOfType(o1, o2, DoubleMatrix.class))
        {
            return func.apply(getMatrix(o1), getMatrix(o2));
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

    static public boolean containsIgnoreCase (String original, String pattern)
    {
        if (original == null || pattern == null)
            return true;
        return original.toUpperCase().contains(pattern.toUpperCase());
    }

    /**
     * Copy resource from jar to temp polder
     * @param name name of resource
     * @return Full path to extracted file
     * @throws IOException if smth gone wrong
     */
    static public String extractResource (String name) throws IOException
    {
        String tempName = System.getProperty("java.io.tmpdir")+name;
        if (!new File (tempName).exists())
        {
            InputStream inStream = ClassLoader.getSystemResourceAsStream(name);
            OutputStream os = new FileOutputStream(tempName);
            byte[] buff = new byte[1024];
            for (; ; )
            {
                int r = inStream.read(buff);
                if (r == -1)
                {
                    break;
                }
                os.write(buff, 0, r);
            }
            inStream.close();
            os.close();
        }
        return tempName;
    }

}
