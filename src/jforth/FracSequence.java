package jforth;

import org.apache.commons.math3.fraction.Fraction;
import tools.Utilities;

import java.util.ArrayList;
import java.util.List;

public class FracSequence extends SequenceBase<Fraction> implements java.io.Serializable {

    public FracSequence ()
    {
    }

    public FracSequence (String csv) throws Exception
    {
        this (csv.split (","));
    }

    public FracSequence (List<Fraction> list)
    {
        _list = new ArrayList<>(list);
    }

    public FracSequence (StringSequence in) throws NumberFormatException {
        for (int s=0; s<in.length (); s++)
        {
            String ss = in.pick(s);
            Fraction frac;
            try {
                int i = Integer.parseInt(ss);
                frac = new Fraction(i);
            } catch (NumberFormatException e) {
                frac = Utilities.parseFraction(ss,10);
            }
            if (frac == null)
                throw new NumberFormatException("not a frac");
            _list.add (frac);
        }
    }

    public FracSequence (String[] values) throws Exception
    {
        for (String value : values)
        {
            Fraction fr = Utilities.parseFraction(value,10);
            if (fr == null)
                throw new Exception ("frac format error");
            _list.add (fr);
        }
    }

    public static FracSequence parseSequence (String in)
    {
        try {
            String seq = Utilities.extractSequence (in);
            return new FracSequence (seq);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder ();
        sb.append ('{');
        for (int x = 0; x < _list.size (); x++)
        {
            String s1 = Utilities.formatFraction(_list.get(x));
            sb.append (s1);
            if (x != _list.size () - 1)
            {
                sb.append (",");
            }
        }
        sb.append ('}');
        return sb.toString ();
    }
}
