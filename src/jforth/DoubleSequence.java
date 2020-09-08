package jforth;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;


/**
 * Created by Administrator on 3/23/2017.
 */
public class DoubleSequence
{
    private ArrayList<Double> _list = new ArrayList<>();

    public ArrayList<Double> get_list ()
    {
        return _list;
    }

    public DoubleSequence()
    {

    }

    public DoubleSequence (StringSequence in) throws NumberFormatException
    {
        for (int s=0; s<in.length (); s++)
            _list.add (Double.parseDouble (in.pick(s)));
    }

    public DoubleSequence (Vector3D v)
    {
        _list.add (v.getX());
        _list.add (v.getY());
        double z = v.getZ();
        if (z != 0.0)
            _list.add (z);
    }

    public DoubleSequence (byte[] b)
    {
        for(byte bb : b)
        {
            _list.add ((double)(((int)bb)&0x00ff));
        }
    }

    public DoubleSequence (String s)
    {
        for(char c : s.toCharArray())
        {
            _list.add ((double)c);
        }
    }

    public DoubleSequence (String[] values)
    {
        for (String value : values)
        {
            try
            {
                _list.add(Double.parseDouble(value));
            }
            catch (Exception ignored)
            {

            }
        }
    }

    public DoubleSequence (List<Double> list)
    {
        _list = new ArrayList<> (list);
    }

    public DoubleSequence (double ... vals)
    {
        for (double val : vals)
        {
            _list.add(val);
        }
    }

    public DoubleSequence (int ... vals)
    {
        for (int val : vals)
        {
            _list.add((double) val);
        }
    }

    public DoubleSequence (ArrayList<Integer> vals)
    {
        for (int val : vals)
        {
            _list.add((double) val);
        }
    }


    public DoubleSequence (DoubleSequence src)
    {
        _list.addAll(src._list);
    }

    /**
     * Constructor to append 2 DS
     * @param src1 DS1
     * @param src2 DS2
     */
    public DoubleSequence (DoubleSequence src1, DoubleSequence src2)
    {
        _list.addAll(src1._list);
        _list.addAll(src2._list);
    }

    public static DoubleSequence fromNumberString (String str)
    {
        DoubleSequence ds = new DoubleSequence();
        for(char c : str.toCharArray())
        {
            int c1 = c-'0';
            if (c1 < 0 || c1 > 9)
                c1 = 0;
            ds._list.add ((double) c1);
        }
        return ds;
    }

    public static SummaryStatistics getStats(DoubleSequence in)
    {
        SummaryStatistics stat = new SummaryStatistics();
        for (double d : in._list)
            stat.addValue(d);
        return stat;
    }

    public static DoubleSequence mixin (DoubleSequence d1, DoubleSequence d2)
    {
        int len = Math.max (d1.length(),d2.length());
        ArrayList<Double> ar = new ArrayList<>();
        for (int s=0; s<len; s++)
        {
            if (s < d1.length())
                ar.add(d1._list.get(s));
            if (s < d2.length())
                ar.add (d2._list.get(s));
        }
        return new DoubleSequence(ar);
    }

    public static DoubleSequence makeCounted (double start, int howmuch, double step)
    {
        double[] arr = new double[howmuch];
        for (int s=0; s<howmuch; s++)
        {
            arr[s] = start;
            start += step;
        }
        return new DoubleSequence (arr);
    }

    public static DoubleSequence makeBits (BigInteger in)
    {
        DoubleSequence out = new DoubleSequence();
        BigInteger two = BigInteger.valueOf(2);
        BigInteger zero = BigInteger.valueOf(0);
        do
        {
            if (in.mod(two).equals(zero))
            {
                out._list.add(0.0);
            }
            else
            {
                out._list.add(1.0);
            }
            in = in.divide(two);
        } while (!in.equals(zero));
        return out.reverse();
    }

    public static DoubleSequence primeFactors(long n)
    {
        List<Double> factors = new ArrayList<Double>();
        while (n % 2 == 0 && n > 0)
        {
            factors.add(2.0);
            n /= 2;
        }

        for (long i = 3; i * i <= n; i+=2)
        {
            while (n % i == 0)
            {
                factors.add((double) i);
                n /= i;
            }
        }
        if (n > 1)
            factors.add((double) n);

        return new DoubleSequence(factors);
    }

    public static DoubleSequence parseSequence (String in, int base)
    {
        if (base != 10)
            return null;
        String seq = Utilities.extractSequence (in);
        if (seq == null)
            return null;
        double[] arr = Utilities.parseCSVtoDoubleArray(seq);
        if (arr == null)
            return null;
        return new DoubleSequence(arr);
    }

    public double sum()
    {
        return new Sum().evaluate (this.asPrimitiveArray());
    }

    public BigInteger fromBitList ()
    {
        int[] arr = asIntArray();
        BigInteger ret = BigInteger.valueOf(0);
        for (int i : arr)
        {
            ret = ret.shiftLeft(1).add(BigInteger.valueOf(i));
        }
        return ret;
    }

    public double altsum()
    {
        DoubleSequence rev = reverse();
        int sign = 1;
        double sum = 0.0;
        for (Double aMem : rev._list)
        {
            sum += (aMem*sign);
            sign = -sign;
        }
        return sum;
    }

    public double[] asPrimitiveArray ()
    {
        double[] out = new double[_list.size()];
        for (int s = 0; s< _list.size(); s++)
            out[s] = _list.get(s);
        return out;
    }

    public int[] asIntArray ()
    {
        int[] out = new int[_list.size()];
        for (int s = 0; s< _list.size(); s++)
            out[s] = _list.get(s).intValue();
        return out;
    }

    public double sumQ()
    {
        return new SumOfSquares().evaluate (this.asPrimitiveArray());
    }

    public double prod()
    {
        return new Product().evaluate (this.asPrimitiveArray());
    }

    public DoubleSequence apply (PolynomialFunction p)
    {
        DoubleSequence ret = new DoubleSequence();
        for (double d : _list)
        {
            ret._list.add(p.value(d));
        }
        return ret;
    }

    public DoubleSequence add (DoubleSequence other)
    {
        DoubleSequence ds = new DoubleSequence(this);
        ds._list.addAll(other._list);
        return ds;
    }

    public boolean isEmpty()
    {
        return _list.isEmpty();
    }

    public DoubleSequence reverse()
    {
        DoubleSequence ret = new DoubleSequence(this);
        Collections.reverse(ret._list);
        return ret;
    }

    public Double pick (int i)
    {
        return _list.get(i);
    }

    public DoubleSequence shuffle()
    {
        DoubleSequence ret = new DoubleSequence(this);
        Collections.shuffle(ret._list);
        return ret;
    }

    public DoubleSequence sort()
    {
        DoubleSequence ret = new DoubleSequence(this);
        Collections.sort (ret._list);
        return ret;
    }

    public DoubleSequence intersect (DoubleSequence other)
    {
        DoubleSequence ret = new DoubleSequence(this);
        ret._list.retainAll(other._list);
        return ret;
    }

    public DoubleSequence difference (DoubleSequence other)
    {
        DoubleSequence ret = new DoubleSequence(this);
        ret._list.removeAll(other._list);
        return ret;
    }

    public boolean sameContents (DoubleSequence other)
    {
        if (this._list.size () != other._list.size ())
            return false;
        ArrayList<Double> test = (ArrayList<Double>) this._list.clone ();
        test.removeAll (other._list);
        return test.size() == 0;
    }

    public DoubleSequence rotateLeft (int n)
    {
        DoubleSequence ret = new DoubleSequence(this);
        ret._list = Utilities.rotateLeft (ret._list, n);
        return ret;
    }

    public DoubleSequence rotateRight (int n)
    {
        DoubleSequence ret = new DoubleSequence(this);
        ret._list = Utilities.rotateRight (ret._list, n);
        return ret;
    }

    public int length()
    {
        return _list.size();
    }

    public DoubleSequence unique ()
    {
        ArrayList<Double> nodupe = new ArrayList<>(new LinkedHashSet<>(_list));
        return new DoubleSequence(nodupe);
    }

    public DoubleSequence subList (int from, int to)
    {
        return new DoubleSequence(this._list.subList(from, to));
    }

//    public DoubleSequence rearrange (int pos[])
//    {
//        DoubleSequence out = new DoubleSequence();
//        for (int p : pos)
//        {
//            out = out.add(this._list.get(p));
//        }
//        return out;
//    }

    public DoubleSequence rearrange (int pos[])
    {
        ArrayList<Double> ret = Utilities.rearrange (pos, _list);
        return new DoubleSequence (ret);
    }

    public DoubleSequence add (double d)
    {
        //DoubleSequence ds = new DoubleSequence(this);
        _list.add(d);
        return this;
    }

    public String asString ()
    {
        StringBuilder sb = new StringBuilder();
        for (Double d : this._list)
        {
            sb.append((char)d.intValue());
        }
        return sb.toString();
    }

    public byte[] asBytes()
    {
        try
        {
            return asString().getBytes(JForth.ENCODING);
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }

    /**
     * Uses polynomial fitter to fit a sequence of points
     * This DoubleSequence must be even size.
     * Every pair is x/y coords of a point
     * @return a new PolynomialFunction
     */
    public PolynomialFunction polyFit() throws Exception
    {
        if (this.length()%2 == 1)
            throw new Exception("DoubleSeq must be even size");
        int numPoints = this.length()/2;
        PolynomialCurveFitter fitter= PolynomialCurveFitter.create(numPoints-1);
        List<WeightedObservedPoint> points = new ArrayList<>();
        for (int s=0; s<this.length(); s+=2)
        {
            WeightedObservedPoint p = new WeightedObservedPoint(1, _list.get(s), _list.get(s+1));
            points.add(p);
        }
        return new PolynomialFunction(fitter.fit(points));
    }

    public PolynomialFunction lagFit() throws Exception
    {
        if (this.length()%2 == 1)
            throw new Exception("DoubleSeq must be even size");
        int len = this.length()/2;
        double[] xp = new double[len];
        double[] yp = new double[len];
        for (int s=0; s<len; s++)
        {
            xp[s] = _list.get(s*2);
            yp[s] = _list.get(s*2+1);
        }
        PolynomialFunctionLagrangeForm pfl = new PolynomialFunctionLagrangeForm(xp, yp);
        return new PolynomialFunction(pfl.getCoefficients());
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        String str;
        sb.append('{');
        if (_list.size() > 0)
        {
            for (int s = 0; s < _list.size() - 1; s++)
            {
                str = Utilities.removeTrailingZero(_list.get(s));
                sb.append(str).append(',');
            }
            str = Utilities.removeTrailingZero(_list.get(_list.size() - 1));
            sb.append(str);
        }
        sb.append('}');
        return sb.toString();
    }

    public DoubleSequence swap (int a, int b)
    {
        ArrayList<?> al = Utilities.swap (_list, a, b);
        return new DoubleSequence ((ArrayList<Double>) al);
    }

}
