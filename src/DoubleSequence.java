import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Administrator on 3/23/2017.
 */
public class DoubleSequence
{
    private ArrayList<Double> mem = new ArrayList<>();

    public DoubleSequence()
    {

    }

    public DoubleSequence add (double d)
    {
        DoubleSequence ds = new DoubleSequence(this);
        ds.mem.add(d);
        return ds;
    }

    public DoubleSequence add (DoubleSequence other)
    {
        DoubleSequence ds = new DoubleSequence(this);
        ds.mem.addAll(other.mem);
        return ds;
    }

    public DoubleSequence (String[] values)
    {
        for (int s=0; s<values.length; s++)
        {
            try
            {
                mem.add(Double.parseDouble(values[s]));
            }
            catch (Exception unused)
            {
                
            }
        }
    }

    public DoubleSequence (List<Double> list)
    {
        mem = new ArrayList<Double>(list);
    }

    public DoubleSequence (double ... vals)
    {
        for (int s=0; s<vals.length; s++)
        {
            mem.add (vals[s]);
        }
    }

    public DoubleSequence (int ... vals)
    {
        for (int s=0; s<vals.length; s++)
        {
            mem.add ((double)vals[s]);
        }
    }

    public DoubleSequence (DoubleSequence src)
    {
        for (int s=0; s<src.mem.size(); s++)
        {
            mem.add (src.mem.get(s));
        }
    }

    public DoubleSequence (DoubleSequence src1, DoubleSequence src2)
    {
        for (int s=0; s<src1.mem.size(); s++)
        {
            mem.add (src1.mem.get(s));
        }
        for (int s=0; s<src2.mem.size(); s++)
        {
            mem.add (src2.mem.get(s));
        }
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


    public static DoubleSequence parseSequence (String in)
    {
        if (in.length() < 2)
            return null;
        if (in.charAt(0) == '{' && in.charAt(in.length()-1) == '}')
        {
            in = in.substring(1, in.length()-1);
            String vals[] = in.split(",");
            return new DoubleSequence(vals);
        }
        return null;
    }

    public DoubleSequence subList (int from, int to)
    {
        return new DoubleSequence(this.mem.subList(from, to));
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

    public double[] asPrimitiveArray ()
    {
        double[] out = new double[mem.size()];
        for (int s=0; s<mem.size(); s++)
            out[s] = mem.get(s);
        return out;
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
