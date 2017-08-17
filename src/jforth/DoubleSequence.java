package jforth;

import jforth.scalacode.MyMath;
import jforth.scalacode.SieveOfEratosthenes;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;
import scala.math.BigInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.DoubleStream;


/**
 * Created by Administrator on 3/23/2017.
 */
public class DoubleSequence
{
    private ArrayList<Double> mem = new ArrayList<>();

    public DoubleSequence()
    {

    }

    public DoubleSequence (String s)
    {
        for(char c : s.toCharArray())
        {
            mem.add ((double)c);
        }
    }

    public DoubleSequence (String[] values)
    {
        for (String value : values)
        {
            try
            {
                mem.add(Double.parseDouble(value));
            }
            catch (Exception ignored)
            {

            }
        }
    }

    private DoubleSequence (List<Double> list)
    {
        mem = new ArrayList<>(list);
    }

    public DoubleSequence (double ... vals)
    {
        for (double val : vals)
        {
            mem.add(val);
        }
    }

    public DoubleSequence (int ... vals)
    {
        for (int val : vals)
        {
            mem.add((double) val);
        }
    }

    public DoubleSequence (DoubleSequence src)
    {
        mem.addAll(src.mem);
    }

    public DoubleSequence (DoubleSequence src1, DoubleSequence src2)
    {
        mem.addAll(src1.mem);
        mem.addAll(src2.mem);
    }

    public static DoubleSequence makeCounted (double start, long howmuch, double step)
    {
        DoubleStream ds = DoubleStream.iterate(start, n -> n + step).limit(howmuch);
        return new DoubleSequence (ds.toArray());
    }

    public static DoubleSequence makeBits (BigInt in)
    {
        DoubleSequence out = new DoubleSequence();
        BigInt two = BigInt.apply(2);
        BigInt zero = BigInt.apply(0);
        do
        {
            if (in.mod(two).equals(zero))
            {
                out.mem.add(0.0);
            }
            else
            {
                out.mem.add(1.0);
            }
            in = in.$div(two);
        } while (!in.equals(zero));
        return out.reverse();
    }

    public static DoubleSequence primes (long in)
    {
        return primes (BigInt.apply(in));
    }

    public static DoubleSequence primes (BigInt in)
    {
        DoubleSequence out = new DoubleSequence();
        List<BigInt> list = MyMath.toJList(SieveOfEratosthenes.factors(in));
        for (BigInt i : list)
        {
            out.mem.add (i.toDouble());
        }

        return out;
    }


    public static DoubleSequence parseSequence (String in)
    {
        if (in.charAt(0) == '{' && in.charAt(in.length()-1) == '}')
        {
            in = in.substring(1, in.length()-1);
            return new DoubleSequence(Utilities.parseCSVtoDoubleArray(in));
        }
        return null;
    }

    public double sum()
    {
        return new Sum().evaluate (this.asPrimitiveArray());
    }

    public double[] asPrimitiveArray ()
    {
        double[] out = new double[mem.size()];
        for (int s=0; s<mem.size(); s++)
            out[s] = mem.get(s);
        return out;
    }

    public int[] asIntArray ()
    {
        int[] out = new int[mem.size()];
        for (int s=0; s<mem.size(); s++)
            out[s] = mem.get(s).intValue();
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
        for (double d : mem)
        {
            ret.mem.add(p.value(d));
        }
        return ret;
    }

    public DoubleSequence add (DoubleSequence other)
    {
        DoubleSequence ds = new DoubleSequence(this);
        ds.mem.addAll(other.mem);
        return ds;
    }

    public boolean isEmpty()
    {
        return mem.isEmpty();
    }

    public DoubleSequence reverse()
    {
        DoubleSequence ret = new DoubleSequence(this);
        Collections.reverse(ret.mem);
        return ret;
    }

    public Double pick (int i)
    {
        return mem.get(i);
    }

    public DoubleSequence shuffle()
    {
        DoubleSequence ret = new DoubleSequence(this);
        Collections.shuffle(ret.mem);
        return ret;
    }

    public DoubleSequence sort()
    {
        DoubleSequence ret = new DoubleSequence(this);
        Collections.sort (ret.mem);
        return ret;
    }

    public DoubleSequence intersect (DoubleSequence other)
    {
        DoubleSequence ret = new DoubleSequence(this);
        ret.mem.retainAll(other.mem);
        return ret;
    }

    public DoubleSequence difference (DoubleSequence other)
    {
        DoubleSequence ret = new DoubleSequence(this);
        ret.mem.removeAll(other.mem);
        return ret;
    }

    public DoubleSequence rotateLeft (int n)
    {
        DoubleSequence ret = new DoubleSequence(this);
        while (n != 0)
        {
            ret.mem.add(ret.mem.size(), 0.0);
            ret.mem.remove(0);
            n--;
        }
        return ret;
    }

    public DoubleSequence rotateRight (int n)
    {
        DoubleSequence ret = new DoubleSequence(this);
        while (n != 0)
        {
            ret.mem.add(0, 0.0);
            ret.mem.remove(ret.mem.size()-1);
            n--;
        }
        return ret;
    }

    public int length()
    {
        return mem.size();
    }

    public DoubleSequence unique ()
    {
        ArrayList<Double> nodupe = new ArrayList<>(new LinkedHashSet<>(mem));
        return new DoubleSequence(nodupe);
    }

    public DoubleSequence subList (int from, int to)
    {
        return new DoubleSequence(this.mem.subList(from, to));
    }

    public DoubleSequence rearrange (int pos[])
    {
        DoubleSequence out = new DoubleSequence();
        for (int p : pos)
        {
            out = out.add(this.mem.get(p));
        }
        return out;
    }

    public DoubleSequence add (double d)
    {
        DoubleSequence ds = new DoubleSequence(this);
        ds.mem.add(d);
        return ds;
    }

    public String asString ()
    {
        StringBuilder sb = new StringBuilder();
        for (Double d : this.mem)
        {
            sb.append((char)d.intValue());
        }
        return sb.toString();
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
            WeightedObservedPoint p = new WeightedObservedPoint(1, mem.get(s), mem.get(s+1));
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
            xp[s] = mem.get(s*2);
            yp[s] = mem.get(s*2+1);
        }
        PolynomialFunctionLagrangeForm pfl = new PolynomialFunctionLagrangeForm(xp, yp);
        return new PolynomialFunction(pfl.getCoefficients());
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        String str;
        sb.append('{');
        if (mem.size() > 0)
        {
            for (int s = 0; s < mem.size() - 1; s++)
            {
                str = Utilities.removeTrailingZero(mem.get(s));
                sb.append(str).append(',');
            }
            str = Utilities.removeTrailingZero(mem.get(mem.size() - 1));
            sb.append(str);
        }
        sb.append('}');
        return sb.toString();
    }
}
