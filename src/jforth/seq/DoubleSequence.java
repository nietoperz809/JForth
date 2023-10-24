package jforth.seq;

import jforth.JForth;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;
import tools.Func;
import tools.Utilities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 3/23/2017.
 */
public class DoubleSequence extends SequenceBase<Double> implements java.io.Serializable {
    public DoubleSequence() {

    }

    public DoubleSequence(StringSequence in) throws NumberFormatException {
        for (int s = 0; s < in.length(); s++)
            _list.add(Double.parseDouble(in.pick(s)));
    }

    public DoubleSequence(Vector3D v) {
        _list.add(v.getX());
        _list.add(v.getY());
        double z = v.getZ();
        if (z != 0.0)
            _list.add(z);
    }

    public DoubleSequence(byte[] b) {
        for (byte bb : b) {
            _list.add((double) (((int) bb) & 0x00ff));
        }
    }

    public DoubleSequence(String s) {
        for (char c : s.toCharArray()) {
            _list.add((double) c);
        }
    }

    public DoubleSequence(String[] values) {
        for (String value : values) {
            try {
                _list.add(Double.parseDouble(value));
            } catch (Exception ignored) {

            }
        }
    }

    public DoubleSequence(List<Double> list) {
        _list = new ArrayList<>(list);
    }

    public DoubleSequence(double... vals) {
        for (double val : vals) {
            _list.add(val);
        }
    }

    public DoubleSequence(long... vals) {
        for (double val : vals) {
            _list.add(val);
        }
    }

    public DoubleSequence(int... vals) {
        for (int val : vals) {
            _list.add((double) val);
        }
    }

    public DoubleSequence(ArrayList<Integer> vals) {
        for (int val : vals) {
            _list.add((double) val);
        }
    }

    public DoubleSequence(Integer[] vals) {
        for (int val : vals) {
            _list.add((double) val);
        }
    }


    public DoubleSequence(DoubleSequence src) {
        _list.addAll(src._list);
    }

    public DoubleSequence(DoubleSequence... src) {
        addAll(src);
    }

    public static DoubleSequence fromFracSeq(FracSequence fr) {
        DoubleSequence ds = new DoubleSequence();
        for (Fraction f : fr._list) {
            double d = f.doubleValue();
            ds._list.add(d);
        }
        return ds;
    }

    public static DoubleSequence fromNumberString(String str) {
        DoubleSequence ds = new DoubleSequence();
        for (char c : str.toCharArray()) {
            int c1 = c - '0';
            if (c1 < 0 || c1 > 9)
                c1 = 0;
            ds._list.add((double) c1);
        }
        return ds;
    }

    public static SummaryStatistics getStats(DoubleSequence in) {
        SummaryStatistics stat = new SummaryStatistics();
        for (double d : in._list)
            stat.addValue(d);
        return stat;
    }

    /**
     * Generate Sequence
     *
     * @param start   first value
     * @param howmuch number of value
     * @param step    distance from one value to another
     * @return the new Sequence
     */
    public static DoubleSequence makeCounted(double start, int howmuch, double step) {
        double[] arr = new double[howmuch];
        for (int s = 0; s < howmuch; s++) {
            arr[s] = start;
            start += step;
        }
        return new DoubleSequence(arr);
    }

    public static DoubleSequence makeBits(BigInteger in) {
        DoubleSequence out = new DoubleSequence();
        BigInteger two = BigInteger.valueOf(2);
        BigInteger zero = BigInteger.valueOf(0);
        do {
            if (in.mod(two).equals(zero)) {
                out._list.add(0.0);
            } else {
                out._list.add(1.0);
            }
            in = in.divide(two);
        } while (!in.equals(zero));
        return (DoubleSequence) out.reverse();
    }

    public static DoubleSequence primeFactors(long n) {
        List<Double> factors = new ArrayList<>();
        while (n % 2 == 0 && n > 0) {
            factors.add(2.0);
            n /= 2;
        }

        for (long i = 3; i * i <= n; i += 2) {
            while (n % i == 0) {
                factors.add((double) i);
                n /= i;
            }
        }
        if (n > 1)
            factors.add((double) n);

        return new DoubleSequence(factors);
    }

    public static DoubleSequence parseSequence(String in, int base) {
        if (base != 10)
            return null;
        if (in.equals("{}"))
            return new DoubleSequence();
        String seq = Utilities.extractSequence(in);
        if (seq == null)
            return null;
        double[] arr = Utilities.parseCSVtoDoubleArray(seq);
        if (arr == null)
            return null;
        return new DoubleSequence(arr);
    }

    public static DoubleSequence mult(DoubleSequence s1, DoubleSequence other) {
        int len = Math.max(s1.length(), other.length());
        DoubleSequence ds = new DoubleSequence();
        double a, b;
        for (int s = 0; s < len; s++) {
            try {
                a = s1.pick(s);
            } catch (IndexOutOfBoundsException e) {
                a = 0;
            }
            try {
                b = other.pick(s);
            } catch (IndexOutOfBoundsException e) {
                b = 0;
            }
            ds.add(a * b);
        }
        return ds;
    }

    public static DoubleSequence replace(DoubleSequence in, double old, double nev) {
        ArrayList<Double> ar = in._list;
        ArrayList ar2 = new ArrayList<Double>();
        for (Double aDouble : ar) {
            if (aDouble == old)
                ar2.add(nev);
            else
                ar2.add(aDouble);
        }
        DoubleSequence ret = new DoubleSequence();
        ret._list.addAll(ar2);
        return ret;
    }

    public double sum() {
        return new Sum().evaluate(this.asPrimitiveArray());
    }

    public BigInteger fromBitList() {
        int[] arr = asIntArray();
        BigInteger ret = BigInteger.valueOf(0);
        for (int i : arr) {
            ret = ret.shiftLeft(1).add(BigInteger.valueOf(i));
        }
        return ret;
    }

    public double altsum() {
        DoubleSequence rev = (DoubleSequence) reverse();
        int sign = 1;
        double sum = 0.0;
        for (Double aMem : rev._list) {
            sum += (aMem * sign);
            sign = -sign;
        }
        return sum;
    }

    public double[] asPrimitiveArray() {
        double[] out = new double[_list.size()];
        for (int s = 0; s < _list.size(); s++)
            out[s] = _list.get(s);
        return out;
    }

    public BigInteger[] asBigIntArray() {
        BigInteger[] out = new BigInteger[_list.size()];
        for (int s = 0; s < _list.size(); s++)
            out[s] = BigInteger.valueOf((long)(double)_list.get(s));
        return out;
    }


    public int[] asIntArray() {
        int[] out = new int[_list.size()];
        for (int s = 0; s < _list.size(); s++)
            out[s] = _list.get(s).intValue();
        return out;
    }

    public double sumQ() {
        return new SumOfSquares().evaluate(this.asPrimitiveArray());
    }

    public double prod() {
        return new Product().evaluate(this.asPrimitiveArray());
    }

    public BigInteger prodN() {
        double[] arr = this.asPrimitiveArray();
        BigInteger ret = new BigInteger(String.valueOf((long) arr[0]));
        for (int s = 1; s < arr.length; s++)
            ret = ret.multiply(new BigInteger(String.valueOf((long) arr[s])));
        return ret;
    }

    public double variance() {
        return StatUtils.variance(this.asPrimitiveArray());
    }

    public DoubleSequence reciprocals() {
        DoubleSequence ret = new DoubleSequence();
        for (double d : _list) {
            ret._list.add(1.0 / d);
        }
        return ret;
    }

    public DoubleSequence neg() {
        DoubleSequence ret = new DoubleSequence();
        for (double d : _list) {
            ret._list.add(-d);
        }
        return ret;
    }

    public DoubleSequence apply(PolynomialFunction p) {
        DoubleSequence ret = new DoubleSequence();
        for (double d : _list) {
            ret._list.add(p.value(d));
        }
        return ret;
    }

    public DoubleSequence add(DoubleSequence other) {
        DoubleSequence ds = new DoubleSequence(this);
        ds._list.addAll(other._list);
        return ds;
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();
        for (Double d : this._list) {
            sb.append((char) d.intValue());
        }
        return sb.toString();
    }

    public byte[] asBytes() {
        return asString().getBytes(JForth.ENCODING);
    }

    /**
     * Uses polynomial fitter to fit a sequence of points
     * This DoubleSequence must be even size.
     * Every pair is x/y coords of a point
     *
     * @return a new PolynomialFunction
     */
    public PolynomialFunction polyFit() throws Exception {
        if (this.length() % 2 == 1)
            throw new Exception("DoubleSeq must be even size");
        int numPoints = this.length() / 2;
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(numPoints - 1);
        List<WeightedObservedPoint> points = new ArrayList<>();
        for (int s = 0; s < this.length(); s += 2) {
            WeightedObservedPoint p = new WeightedObservedPoint(1, _list.get(s), _list.get(s + 1));
            points.add(p);
        }
        return new PolynomialFunction(fitter.fit(points));
    }

    public PolynomialFunction lagFit() throws Exception {
        if (this.length() % 2 == 1)
            throw new Exception("DoubleSeq must be even size");
        int len = this.length() / 2;
        double[] xp = new double[len];
        double[] yp = new double[len];
        for (int s = 0; s < len; s++) {
            xp[s] = _list.get(s * 2);
            yp[s] = _list.get(s * 2 + 1);
        }
        PolynomialFunctionLagrangeForm pfl = new PolynomialFunctionLagrangeForm(xp, yp);
        return new PolynomialFunction(pfl.getCoefficients());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String str;
        sb.append('{');
        if (_list.size() > 0) {
            for (int s = 0; s < _list.size() - 1; s++) {
                str = Utilities.removeTrailingZero(_list.get(s));
                sb.append(str).append(',');
            }
            str = Utilities.removeTrailingZero(_list.get(_list.size() - 1));
            sb.append(str);
        }
        sb.append('}');
        return sb.toString();
    }

    public DoubleSequence normalize() {
        return transform(StatUtils::normalize);
    }

    public DoubleSequence mode() {
        return transform(StatUtils::mode);
    }

    public DoubleSequence transform(Func<double[], double[]> action) {
        return new DoubleSequence(transformInnerArray(action));
    }

    public double[] transformInnerArray(Func<double[], double[]> action) {
        return action.apply(asPrimitiveArray());
    }
}
