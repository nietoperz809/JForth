import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Administrator on 3/23/2017.
 */
public class LongSequence
{
    private final ArrayList<Long> mem = new ArrayList<>();

    public LongSequence (String[] values)
    {
        for (int s=0; s<values.length; s++)
        {
            mem.add (Long.parseLong(values[s]));
        }
    }

    public LongSequence (long ... vals)
    {
        for (int s=0; s<vals.length; s++)
        {
            mem.add (vals[s]);
        }
    }

    public LongSequence (LongSequence src)
    {
        for (int s=0; s<src.mem.size(); s++)
        {
            mem.add (src.mem.get(s));
        }
    }

    public LongSequence (LongSequence src1, LongSequence src2)
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

    public LongSequence reverse()
    {
        LongSequence ret = new LongSequence(this);
        Collections.reverse(ret.mem);
        return ret;
    }

    public Long pick (int i)
    {
        return mem.get(i);
    }

    public LongSequence shuffle()
    {
        LongSequence ret = new LongSequence(this);
        Collections.shuffle(ret.mem);
        return ret;
    }

    public LongSequence sort()
    {
        LongSequence ret = new LongSequence(this);
        Collections.sort (ret.mem);
        return ret;
    }

    public static LongSequence parseSequence (String in)
    {
        if (in.length() < 2)
            return null;
        if (in.charAt(0) == '{' && in.charAt(in.length()-1) == '}')
        {
            in = in.substring(1, in.length()-1);
            String vals[] = in.split(",");
            return new LongSequence(vals);
        }
        return null;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int s=0; s<mem.size()-1; s++)
        {
            sb.append(mem.get(s)).append(',');
        }
        sb.append(mem.get(mem.size()-1)).append('}');
        return sb.toString();
    }
}
