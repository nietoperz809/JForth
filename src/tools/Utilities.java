package tools;

import jforth.*;
import jforth.seq.DoubleMatrix;
import jforth.seq.DoubleSequence;
import jforth.seq.StringSequence;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.datatransfer.DataFlavor.stringFlavor;
import static org.mathIT.numbers.Numbers.euclid;

/**
 * Created by Administrator on 3/21/2017.
 */
public class Utilities {

    private static final ExecutorService globalExecutor = Executors.newFixedThreadPool(5);
    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    private static int getExecutorFreeSlots() {
        int tc = ((ThreadPoolExecutor) globalExecutor).getActiveCount();
        int tm = ((ThreadPoolExecutor) globalExecutor).getCorePoolSize();
        //System.out.println(tc + "/" + tm);
        return tm - tc;
    }

    public static void executeThread(Runnable r) {
        if (getExecutorFreeSlots() <= 0) {
            System.out.println("Thread pool exhausted");
        }
        globalExecutor.submit(r);
    }

    public static String getClipBoardString() {
        Clipboard clipboard = getDefaultToolkit().getSystemClipboard();
        Transferable clipData = clipboard.getContents(clipboard);
        if (clipData != null) {
            try {
                if (clipData.isDataFlavorSupported(stringFlavor)) {
                    return (String) (clipData.getTransferData(stringFlavor));
                }
            } catch (UnsupportedFlavorException | IOException ufe) {
                System.err.println("getClipoardString fail");
            }
        }
        return null;
    }

    public static String textFileToString(String path) throws IOException {
        File f = new File(path);
        List<String> lines = Files.readAllLines(f.toPath());
        return String.join("\n", lines.toArray(new String[0]));
    }

    public static String formatTimeDuration(long duration) {
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static Object formatTime(long time, String format) {
        if (format == null) {
            return time;
        }
        if (format.equals("~")) {
            return formatTimeDuration(time);
        }
        if (format.equals("#")) {
            format = "EEE, d MMM yyyy HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Timestamp timestamp = new Timestamp(time);
        return sdf.format(timestamp);
    }

    public static String printHexBinary(byte[] data) {
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

    public static BigInteger factorial(long n) {
        BigInteger fact = BigInteger.valueOf(1);
        for (int i = 1; i <= n; i++) {
            fact = fact.multiply(BigInteger.valueOf(i));
        }
        return fact;
    }

    public static BigInteger fib(long n) {
        BigInteger a = BigInteger.valueOf(0);
        BigInteger b = BigInteger.valueOf(1);
        BigInteger c;
        for (long j = 2; j <= n; j++) {
            c = a.add(b);
            a = b;
            b = c;
        }
        return a;
    }

    /**
     * Makes seconds from hh:mm:ss format
     *
     * @param in input string
     * @return value in seconds
     */
    public static Long parseTimer(String in) {
        String[] parts = in.split(":");
        if (parts.length != 3) {
            return null;
        }
        try {
            int h = Integer.parseInt(parts[0]);
            if (h < 0) {
                return null;
            }
            int m = Integer.parseInt(parts[1]);
            if (m < 0 || m > 59) {
                return null;
            }
            int s = Integer.parseInt(parts[2]);
            if (s < 0 || s > 59) {
                return null;
            }
            return 3600L * h + 60 * m + s;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String toTimeView(Long in) {
        int h = (int) (in / 3600);
        int s = (int) (in % 3600);
        int m = s / 60;
        s %= 60;
        return String.format("%d:%02d:%02d", h, m, s);
    }

    public static String toDateView(Long in) {
        Date date = new Date(in);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy h:mm, a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    /**
     * Rank two strings similarity in terms of distance The lower the number,
     * the more similar these strings are to each other See:
     * http://en.wikipedia.org/wiki/Levenshtein_distance#Computing_Levenshtein_distance
     *
     * @param s
     * @param t
     * @return Distance (higher is better)
     */
    public static int levenshteinDistance(String s, String t) {
        if (s == null || t == null || s.length() == 0 || t.length() == 0) {
            return -1;
        }

        s = s.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
        t = t.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
        int m = s.length();
        int n = t.length();
        int[][] dist = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            dist[i][0] = i;
        }
        for (int i = 1; i <= n; i++) {
            dist[0][i] = i;
        }
        for (int j = 1; j <= n; j++) {
            for (int i = 1; i <= m; i++) {
                if (s.charAt(i - 1) == t.charAt(j - 1)) {
                    dist[i][j] = dist[i - 1][j - 1];
                } else {
                    int del = dist[i - 1][j] + 1;
                    int insert = dist[i][j - 1] + 1;
                    int sub = dist[i - 1][j - 1] + 1;
                    dist[i][j] = Math.min(Math.min(del, insert), sub);
                }
            }
        }
        return Math.max(m, n) - dist[m][n];
    }


    public static void terminateSoon(int delay) {
        new Thread(() ->
        {
            try {
                TimeUnit.MILLISECONDS.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }).start();
    }

    public static String formatDouble(Double d) {
        String outstr = Double.toString(d);
        if (outstr.endsWith(".0")) {
            outstr = outstr.substring(0, outstr.length() - 2);
        }
        if (outstr.equals("-0")) {
            outstr = "0";
        }
        return outstr;
    }

    public static String formatComplex(Complex c) {
        String re = formatDouble(c.getReal());
        String im = formatDouble(c.getImaginary());
        if (im.equals("0")) {
            return re;
        }
        if (re.equals("0")) {
            return im + "i";
        }
        if (im.charAt(0) == '-') {
            return re + im + "i";
        }
        return re + "+" + im + "i";
    }

    static Complex parseOnlyImaginary(String in) {
        int sign = in.charAt(0) == '-' ? -1 : 1;
        if (sign == -1) {
            in = in.substring(1);
        }
        if (in.charAt(in.length() - 1) != 'i') {
            return null;
        }
        String num = in.substring(0, in.length() - 1);
        try {
            int inum = Integer.parseInt(num);
            return new Complex(0, inum * sign);
        } catch (NumberFormatException e) {
            /* ignored */
        }
        return null;
    }

    public static Complex parseComplex(String in, int base) {
        if (base != 10) {
            return null;
        }
        Complex cpl = parseOnlyImaginary(in);
        if (cpl != null) {
            return cpl;
        }
        boolean negfirst = false;
        if (in.startsWith("-")) {
            in = in.substring(1);
            negfirst = true;
        }
        String[] parts = in.split("(-)|(\\+)");
        if (parts.length != 2) {
            return null;
        }
        if (!parts[1].endsWith("i")) {
            return null;
        }
        parts[1] = parts[1].substring(0, parts[1].length() - 1);
        if (negfirst) {
            parts[0] = "-" + parts[0];
        }
        if (in.substring(1).contains("-")) {
            parts[1] = "-" + parts[1];
        }
        return new Complex(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    public static Fraction parseFraction(String in, int base) {
        if (base != 10) {
            return null;
        }
        String[] parts = in.split("/");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new Fraction(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String formatFraction(Fraction f) {
        return f.getNumerator() + "/" + f.getDenominator();
    }

    public static Object deepCopy(Object o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new ObjectOutputStream(baos).writeObject(o);

            ByteArrayInputStream bais =
                    new ByteArrayInputStream(baos.toByteArray());

            return new ObjectInputStream(bais).readObject();
        } catch (Exception ex) {
            return null;
        }
    }

    public static String removeTrailingZero(double v) {
        return removeTrailingZero(v, false);
    }

    public static String removeTrailingZero(double v, boolean round) {
        String str;
        if (round) {
            v = Math.round(10000.0 * v) / 10000.0;
        }
        str = "" + v;
        if (str.endsWith(".0")) {
            return str.substring(0, str.length() - 2);
        }
        return str;
    }

    public static BigInteger parseBigInt(String word, int base) {
        try {
            if (word.endsWith("L")) {
                word = word.substring(0, word.length() - 1);
                return new BigInteger(word, base);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Long parseBinary(String in) {
        int sign = 1;
        if (in.charAt(0) == '-') {
            sign = -1;
            in = in.substring(1);
        }
        if (in.startsWith("0b")) {
            in = in.substring(2);
            try {
                return Long.parseLong(in, 2) * sign;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public static Long parseLong(String word, int base) {
        long mult = 1;
        if (word.endsWith("M")) {
            mult = 1000000;
            word = word.substring(0, word.length() - 1);
        } else if (word.endsWith("K")) {
            mult = 1000;
            word = word.substring(0, word.length() - 1);
        }
        try {
            return mult * Long.parseLong(word, base);
        } catch (NumberFormatException ignored) {
            try {
                return mult * Long.decode(word);
            } catch (NumberFormatException e) {
                Long ll = parseBinary(word);
                if (ll == null) {
                    return parseTimer(word);
                }
                return ll;
            }
        }
    }

    public static Double parseDouble(String word, int base) {
        if (base != 10) {
            return null;
        }
        try {
            return Double.parseDouble(word);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Double parseDouble(String word) throws NumberFormatException {
        if (word.equals("e"))
            return Math.E;
        if (word.equals("pi"))
            return Math.PI;
        return Double.parseDouble(word);
    }

    public static List<String> splitEqually(String text, int size) {
        try {
            List<String> ret = new ArrayList<>((text.length() + size - 1) / size);
            for (int start = 0; start < text.length(); start += size) {
                ret.add(text.substring(start, Math.min(text.length(), start + size)));
            }
            return ret;
        } catch (Exception unused) {
            return null;
        }
    }

    public static List<DoubleSequence> splitEqually(DoubleSequence text, int size) {
        try {
            List<DoubleSequence> ret = new ArrayList<>((text.length() + size - 1) / size);
            for (int start = 0; start < text.length(); start += size) {
                ret.add((DoubleSequence) text.subList(start, Math.min(text.length(), start + size)));
            }
            return ret;
        } catch (Exception unused) {
            return null;
        }
    }

    public static String parseString(String in) {
        if (in.length() < 2) {
            return null;
        }
        if (!in.startsWith("\"")) {
            return null;
        }
        if (!in.endsWith("\"")) {
            return null;
        }
        return in.substring(1, in.length() - 1);
    }


    public static String rotRight(String s, int a) {
        char[] array = s.toCharArray();
        try {
            char[] tmp = new char[a];
            System.arraycopy(array, array.length - a, tmp, 0, a);
            System.arraycopy(array, 0, array, a, array.length - a);
            System.arraycopy(tmp, 0, array, 0, a);
            return new String(array);
        } catch (Exception e) {
            return s;
        }
    }

    public static String rotLeft(String s, int a) {
        return rotRight(s, s.length() - a);
    }

    public static byte[] rotRight(byte[] array, int a) {
        try {
            byte[] ret = new byte[array.length];
            System.arraycopy(array, 0, ret, a, array.length - a);
            System.arraycopy(array, array.length - a, ret, 0, a);
            return ret;
        } catch (Exception e) {
            return array;
        }
    }

    public static byte[] rotLeft(byte[] array, int a) {
        return rotRight(array, array.length - a);
    }

    public static double readDouble(OStack dStack) throws Exception {
        Object o = dStack.pop();
        return getDouble(o);
    }


    static public Double getDouble(Object o1) throws Exception {
        if (o1 instanceof BigInteger) {
            return ((BigInteger) o1).doubleValue();
        }
        if (o1 instanceof Double) {
            return (Double) o1;
        }
        if (o1 instanceof Long) {
            return ((Long) o1).doubleValue();
        }
        if (o1 instanceof Fraction) {
            double denom = ((Fraction) o1).getDenominator();
            double nume = ((Fraction) o1).getNumerator();
            return nume / denom;
        }
        if (o1 instanceof Complex) {
            Complex c = (Complex) o1;
            if (c.getImaginary() == 0.0) {
                return c.getReal();
            }
        }
        throw new Exception("Wrong args");
    }

    public static long readLong(OStack dStack) throws Exception {
        return getLong(dStack.pop());
    }

    public static long peekLong(OStack dStack) throws Exception {
        return getLong(dStack.peek());
    }

    public static double[] read2(OStack dStack) throws Exception {
        DoubleSequence ds = readDoubleSequence(dStack);
        if (ds.length() != 2)
            throw new Exception("wrong Seq length");
        return ds.asPrimitiveArray();
    }

    public static Color readColor(OStack dStack) throws Exception {
        Object ox = dStack.pop();
        if (ox instanceof DoubleSequence) {
            DoubleSequence ds = (DoubleSequence) ox;
            if (ds.length() != 3)
                throw new Exception("wrong Seq length");
            return new Color(ds.pick(0).intValue(), ds.pick(1).intValue(), ds.pick(2).intValue());
        }
        else if (ox instanceof Color) {
            return (Color)ox;
        }
        int r = (int) getLong(ox);
        int g = (int) readLong(dStack);
        int b = (int) readLong(dStack);
        return new Color(r, g, b);
    }

    public static Point readPoint(OStack dStack) throws Exception {
        Object ox = dStack.pop();
        if (ox instanceof DoubleSequence) {
            DoubleSequence ds = (DoubleSequence) ox;
            if (ds.length() != 2)
                throw new RuntimeException("ds != 2");
            return new Point(ds.pick(0).intValue(), ds.pick(1).intValue());
        }
        int x = (int) getLong(ox);
        int y = (int) readLong(dStack);
        return new Point(x, y);
    }


    public static FileInputStream readFileInputStream(OStack dStack) {
        return (FileInputStream) dStack.pop();
    }


    public static long getLong(Object o) throws Exception {
        if (o instanceof BigInteger) {
            return ((BigInteger) o).longValue();
        }
        if (o instanceof Double) {
            return ((Double) o).longValue();
        }
        if (o instanceof Long) {
            return (Long) o;
        }
        if (o instanceof Integer) {
            return ((Integer) o).longValue();
        }
        if (o instanceof Fraction) {
            int denom = (int) ((Fraction) o).getDenominator();
            int nume = (int) ((Fraction) o).getNumerator();
            if (nume % denom == 0) {
                return nume / denom;
            }
        }
        if (o instanceof Complex) {
            Complex c = (Complex) o;
            return ((Double) c.getReal()).longValue();
        }
        throw new Exception("Wrong or no Type on Stack");
    }

    public static BigInteger readBig(OStack dStack) throws Exception {
        Object o = dStack.pop();
        return getBig(o);
    }

    public static FileBlob readBlob(OStack stack) throws Exception {
        return getBlob(stack.pop());

    }

    public static FileBlob getBlob(Object o) throws Exception {
        if (o instanceof FileBlob) {
            return (FileBlob) o;
        }
        throw new Exception("no blob");
    }

    public static BigInteger getBig(Object o1) throws Exception {
        if (o1 instanceof BigInteger) {
            return (BigInteger) o1;
        }
        if (o1 instanceof Long) {
            return BigInteger.valueOf((Long) o1);
        }
        if (o1 instanceof Double) {
            return BigInteger.valueOf(((Double) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    public static String readStringOrNull(OStack dstack) {
        try {
            return readString(dstack);
        } catch (Exception e) {
            return null;
        }
    }

    public static Complex readComplex(OStack dStack) throws Exception {
        return getComplex(dStack.pop());
    }

    private static Complex getComplex(Object o1) throws Exception {
        if (o1 instanceof Complex) {
            return (Complex) o1;
        }
        if (o1 instanceof Long) {
            return new Complex((Long) o1);
        } else if (o1 instanceof Double) {
            return new Complex((Double) o1);
        } else if (o1 instanceof Fraction) {
            Fraction fr = (Fraction) o1;
            return new Complex((double) fr.getNumerator() / (double) fr.getDenominator());
        } else if (o1 instanceof BigInteger) {
            return new Complex(((BigInteger) o1).longValue());
        }
        throw new Exception("Wrong args");
    }

    public static double[] parseCSVtoDoubleArray(String in) {
        try {
            String[] vals = in.split(",");
            double[] out = new double[vals.length];
            int s = 0;
            for (String x : vals) {
                out[s++] = Utilities.parseDouble(x);
            }
            return out;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static BigInteger[] parseCSVtoBigArray(String in) {
        try {
            String[] vals = in.split(",");
            BigInteger[] out = new BigInteger[vals.length];
            for (int s = 0; s < vals.length; s++) {
                if (!vals[s].endsWith("L"))
                    return null;
                out[s] = parseBigInt(vals[s], 10);
            }
            return out;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String removeBeginEnd(String in, char[] be, boolean mustHave) {
        if (in.charAt(0) == be[0] && in.charAt(in.length() - 1) == be[1]) {
            return (in.substring(1, in.length() - 1));
        }
        if (mustHave) {
            return null;
        }
        return in;
    }

    public static String extractSequence(String in) {
        return removeBeginEnd(in, new char[]{'{', '}'}, true);
    }

    public static String extractStringBody(String in) {
        return removeBeginEnd(in, new char[]{'\"', '\"'}, false);
    }

    public static Vector3D readVector3D(OStack dStack) throws Exception {
        DoubleSequence ds = readDoubleSequence(dStack);
        double i2;
        if (ds.length() > 3 || ds.length() < 2) {
            throw new Exception("wrong size");
        }
        if (ds.length() == 2) {
            i2 = 0;
        } else {
            i2 = ds.pick(2);
        }
        return new Vector3D(ds.pick(0), ds.pick(1), i2);
    }

    public static StringSequence readStringSequence(OStack dStack) {
        return getStringSequence(dStack.pop());
    }

    public static StringSequence getStringSequence(Object o) {
        if (o instanceof StringSequence) {
            return (StringSequence) o;
        }
        return null;
    }

    public static DoubleSequence readDoubleSequence(OStack dStack) {
        return getDoubleSequence(dStack.pop());
    }

    public static DoubleSequence getDoubleSequence(Object o) {
        if (o instanceof DoubleSequence) {
            return (DoubleSequence) o;
        }
        if (o instanceof String) {
            return new DoubleSequence((String) o);
        }
        return null;
    }

    public static Fraction pow(Fraction f, Long n) {
        int denom = (int) Math.pow(f.getDenominator(), n);
        int num = (int) Math.pow(f.getNumerator(), n);
        return Fraction.getReducedFraction(num, denom);
    }

    public static BigInteger pow(BigInteger a, BigInteger b) {
        return a.pow(b.intValue());
    }

    public static Double add(Double a, Double b) {
        return a + b;
    }

    public static DoubleMatrix add(DoubleMatrix a, DoubleMatrix b) {
        BlockRealMatrix res = a.add(b);
        return new DoubleMatrix(res);
    }

    public static DoubleMatrix sub(DoubleMatrix a, DoubleMatrix b) {
        BlockRealMatrix res = a.subtract(b);
        return new DoubleMatrix(res);
    }

    public static DoubleMatrix mult(DoubleMatrix a, DoubleMatrix b) {
        BlockRealMatrix res = a.multiply(b);
        return new DoubleMatrix(res);
    }

    public static DoubleMatrix div(DoubleMatrix a, DoubleMatrix b) {
        BlockRealMatrix res = a.multiply(MatrixUtils.inverse(b));
        return new DoubleMatrix(res);
    }

    public static Double sub(Double a, Double b) {
        return a - b;
    }

    public static Double mult(Double a, Double b) {
        return a * b;
    }

    public static Double div(Double a, Double b) {
        return a / b;
    }

    public static Double doCalcDouble(Object o1, Object o2, TwoFuncs<Double, Double, Double> func) throws
            Exception {
        if (o1 instanceof Double || o2 instanceof Double) {
            return func.apply(getDouble(o1), getDouble(o2));
        }
        throw new Exception("Wrong args");
    }

    public static DoubleSequence doCalcDoubleSeq(Object o1, Object o2,
                                                 TwoFuncs<DoubleSequence, DoubleSequence, DoubleSequence> func)
            throws Exception {
        if (o1 instanceof DoubleSequence || o2 instanceof DoubleSequence) {
            return func.apply((DoubleSequence) o1, (DoubleSequence) o2);
        }
        throw new Exception("Wrong args");
    }

    public static Complex doCalcComplex(Object o1, Object o2, TwoFuncs<Complex, Complex, Complex> func) throws
            Exception {
        if (areBothObjectsOfType(o1, o2, Complex.class)) {
            return func.apply(getComplex(o1), getComplex(o2));
        }
        throw new Exception("Wrong args");
    }

    private static boolean areBothObjectsOfType(Object o1, Object o2, Class c) {
        return (c.isInstance(o1) || c.isInstance(o2));
    }

    public static Fraction doCalcFraction(Object o1, Object o2, TwoFuncs<Fraction, Fraction, Fraction> func) throws
            Exception {
        if (areBothObjectsOfType(o1, o2, Fraction.class)) {
            return func.apply(getFrac(o1), getFrac(o2));
        }
        throw new Exception("Wrong args");
    }

    static public Fraction getFrac(Object o1) throws Exception {
        if (o1 instanceof Fraction) {
            return (Fraction) o1;
        }
        if (o1 instanceof Long) {
            return new Fraction((Long) o1);
        }
        if (o1 instanceof Double) {
            return new Fraction((Double) o1);
        }
        if (o1 instanceof BigInteger) {
            return new Fraction(((BigInteger) o1).longValue());
        }
        if (o1 instanceof DoubleSequence) {
            DoubleSequence t = (DoubleSequence) o1;
            return new Fraction(t.pick(0).intValue(), t.pick(1).intValue());
        }
        if (o1 instanceof Complex) {
            Complex c = (Complex) o1;
            if (c.getImaginary() == 0.0)
                return new Fraction(c.getReal());
        }
        throw new Exception("Wrong args");
    }

    public static BigInteger doCalcBigInt(Object o1,
                                          Object o2,
                                          TwoFuncs<BigInteger, BigInteger, BigInteger> func) throws Exception {
        if (areBothObjectsOfType(o1, o2, BigInteger.class)) {
            return func.apply(getBig(o1), getBig(o2));
        }
        throw new Exception("Wrong args");
    }

    public static DoubleMatrix doCalcMatrix(Object o1, Object
            o2, TwoFuncs<DoubleMatrix, DoubleMatrix, DoubleMatrix> func) throws Exception {
        if (areBothObjectsOfType(o1, o2, DoubleMatrix.class)) {
            return func.apply(getMatrix(o1), getMatrix(o2));
        }
        throw new Exception("Wrong args");
    }

    static private DoubleMatrix getMatrix(Object o1) throws Exception {
        if (o1 instanceof DoubleMatrix) {
            return (DoubleMatrix) o1;
        }
        throw new Exception("Wrong args");
    }

    static public boolean containsIgnoreCase(String original, String pattern) {
        if (original == null || pattern == null) {
            return true;
        }
        return original.toUpperCase().contains(pattern.toUpperCase());
    }


    static public ArrayList<Integer> makeCyclicGroup(int generator, int mod) throws Exception {
        BigInteger gen = BigInteger.valueOf(generator);
        ArrayList<Integer> list = new ArrayList<>();
        BigInteger p = BigInteger.valueOf(generator);
        for (; ; ) {
            BigInteger m2 = p.mod(BigInteger.valueOf(mod));
            p = p.multiply(gen);
            int m2i = m2.intValue();
            if (list.contains(m2i)) {
                break;
            }
            if (m2i == 0) {
                throw new Exception("Got a ZERO");
            }
            list.add(m2i);
        }
        if (!list.contains(1)) {
            throw new Exception("Missing 1-element");
        }
        return list;
    }

//    /**
//     * Multiplicative Inverses of group
//     *
//     * @param group The multiplicative group
//     * @param mod   the modulus
//     * @return Arraylist of inverses in the same order as input
//     */
/*
    static public ArrayList<Integer> groupInverses (int[] group, int mod)
    {
        ArrayList<Integer> inv = new ArrayList<> ();
        for (int i : group)
        {
            for (int v1 : group)
            {
                if ((v1 * i) % mod == 1)
                {
                    inv.add (v1);
                }
            }
        }
        return inv;
    }
*/

    /**
     * Multiplicative Inverses of group
     *
     * @param group The multiplicative group
     * @param mod   the modulus
     * @return Arraylist of inverses in the same order as input
     */
    static public ArrayList<Integer> groupInverses(int[] group, int mod) {
        ArrayList<Integer> inv = new ArrayList<>();
        for (int a : group) {
            long[] retvals = euclid(mod, a); //ExtendedGCD (mod, a);
            if (retvals[2] < 0) {
                retvals[2] = mod + retvals[2];
            }
            inv.add((int) retvals[2]);
        }
        return inv;
    }

//    /**
//     * Euclidian ext GCD
//     *
//     * @param a test candidate 1
//     * @param b test canditate 2
//     * @return three integers:  i[0] = a*i[1] + b*i[2]
//     */
//    public static long[] ExtendedGCD (long a, long b)
//    {
//        long[] retvals = {0, 0, 0};
//        long[] aa = {1, 0};
//        long[] bb = {0, 1};
//        long q;
//        while (true)
//        {
//            q = a / b;
//            a = a % b;
//            aa[0] = aa[0] - q * aa[1];
//            bb[0] = bb[0] - q * bb[1];
//            if (a == 0)
//            {
//                retvals[0] = b;
//                retvals[1] = aa[1];
//                retvals[2] = bb[1];
//                return retvals;
//            }
//            q = b / a;
//            b = b % a;
//            aa[1] = aa[1] - q * aa[0];
//            bb[1] = bb[1] - q * bb[0];
//            if (b == 0)
//            {
//                retvals[0] = a;
//                retvals[1] = aa[0];
//                retvals[2] = bb[0];
//                return retvals;
//            }
//        }
//    }


    public static String readString(OStack dStack) throws EmptyStackException {
        Object o = dStack.pop();
        return makePrintable(o, 10);
    }

    /**
     * generate textual representation of object
     *
     * @param o    the object
     * @param base current number base
     * @return the object as string
     */
    public static String makePrintable(Object o, int base) {
        if (o == null) {
            return "NULL";
        }
        if (base == -1) {
            base = 10;
        }
        if (o instanceof Long) {
            return Long.toString((Long) o, base).toUpperCase();
        } else if (o instanceof Double) {
            return Utilities.formatDouble((Double) o);
        } else if (o instanceof Complex) {
            return Utilities.formatComplex((Complex) o);
        } else if (o instanceof Fraction) {
            return Utilities.formatFraction((Fraction) o);
        } else if (o instanceof String) {
            return StringEscape.unescape((String) o);
        } else if (o instanceof PolynomialFunction) {
            return PolySupport.formatPoly((PolynomialFunction) o);
        } else if (o instanceof BigInteger) {
            return o.toString();
        } else if (o instanceof SerializableImage) {
            return "Image";
        } else {
            return StringEscape.unescape(o.toString());
        }
    }

    /**
     * shuffle a string
     *
     * @param string input
     * @return the shuffled string
     */
    public static String shuffle(String string) {
        List<Character> list = string.chars().mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        list.forEach(sb::append);
        return sb.toString();
    }

    /**
     * keep only unique chars in string
     *
     * @param string input
     * @return the new string
     */
    public static String unique(String string) {
        List<Character> list = string.chars().mapToObj(c -> (char) c)
                .distinct()
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        list.forEach(sb::append);
        return sb.toString();
    }

    /**
     * Sort a string
     *
     * @param string input
     * @return a new sorted string
     */
    public static String sort(String string) {
        List<Character> list = string.chars().mapToObj(c -> (char) c).sorted()
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        list.forEach(sb::append);
        return sb.toString();
    }

    /**
     * Reverse a byte array
     *
     * @param arr input array
     * @return a new reveresed array
     */
    public static byte[] reverse(byte[] arr) {
        byte[] res = new byte[arr.length];
        for (int s = 0; s < arr.length; s++)
            res[arr.length - 1 - s] = arr[s];
        return res;
    }

    public static byte grayByte(byte in) {
        return (byte) (in ^ (in & 0xff) >>> 1);
    }

    public static byte ungrayByte(byte in) {
        int r = 8;
        while (--r != 0) {
            in ^= (in & 0xff) >>> 1;
        }
        return in;
    }

    /**
     * Do the Backspace
     *
     * @param in String containig backspace chars
     * @return smaller string with BS and that char before rmoved
     */
    public static String translateBackspace(String in) {
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray()) {
            if (c == '\b') {
                if (sb.length() > 0)
                    sb.setLength(sb.length() - 1);
            } else
                sb.append(c);
        }
        return sb.toString();
    }

    public static String replaceUmlauts(String output) {
        return output.replace("\u00fc", "ue")
                .replace("\u00f6", "oe")
                .replace("\u00e4", "ae")
                .replace("\u00df", "ss")
                .replaceAll("\u00dc(?=[a-z\u00e4\u00f6\u00fc\u00df ])", "Ue")
                .replaceAll("\u00d6(?=[a-z\u00e4\u00f6\u00fc\u00df ])", "Oe")
                .replaceAll("\u00c4(?=[a-z\u00e4\u00f6\u00fc\u00df ])", "Ae")
                .replace("\u00dc", "UE")
                .replace("\u00d6", "OE")
                .replace("\u00c4", "AE");
    }

    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public static byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    /**
     * Apply function to a list of Doubles
     *
     * @param dStack Stack eith DoubleSequence on top
     * @param func   the function
     * @return the results
     */
    public static DoubleSequence functionOverDS(OStack dStack, UnivariateFunction func) {
        Object o = dStack.peek();
        if (o instanceof DoubleSequence) {
            DoubleSequence in = Utilities.readDoubleSequence(dStack);
            DoubleSequence out = new DoubleSequence();
            for (double d : in.asPrimitiveArray()) {
                out.get_list().add(func.value(d));
            }
            return out;
        }
        return null;
    }

    public static String currentLine(JTextPane textTx) {
        // Get section element
        Element section = textTx.getDocument().getDefaultRootElement();

        // Get number of paragraphs.
        // In a text pane, a span of characters terminated by single
        // newline is typically called a paragraph.
        int paraCount = section.getElementCount();

        int position = textTx.getCaret().getDot();

        // Get index ranges for each paragraph
        for (int i = 0; i < paraCount; i++) {
            Element e1 = section.getElement(i);

            int rangeStart = e1.getStartOffset();
            int rangeEnd = e1.getEndOffset();

            try {
                String para = textTx.getText(rangeStart, rangeEnd - rangeStart);

                if (position >= rangeStart && position <= rangeEnd)
                    return para;
            } catch (BadLocationException ex) {
                System.err.println("Get current line from editor error: " + ex.getMessage());
            }
        }
        return null;
    }

    public static byte[] compress(byte[] input, int compressionLevel,
                                  boolean GZIPFormat) throws IOException {
        Deflater compressor = new Deflater(compressionLevel, GZIPFormat);
        compressor.setInput(input);
        compressor.finish();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] readBuffer = new byte[1024];
        int readCount = 0;
        while (!compressor.finished()) {
            readCount = compressor.deflate(readBuffer);
            if (readCount > 0) {
                bao.write(readBuffer, 0, readCount);
            }
        }
        compressor.end();
        return bao.toByteArray();
    }
    public static byte[] decompress(byte[] input, boolean GZIPFormat)
            throws IOException, DataFormatException {
        Inflater decompressor = new Inflater(GZIPFormat);
        decompressor.setInput(input);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] readBuffer = new byte[1024];
        int readCount = 0;
        while (!decompressor.finished()) {
            readCount = decompressor.inflate(readBuffer);
            if (readCount > 0) {
                bao.write(readBuffer, 0, readCount);
            }
        }
        decompressor.end();
        return bao.toByteArray();
    }
}
