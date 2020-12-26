
// not used

package jforth;


import java.util.*;

public class StringSequence extends SequenceBase<String> implements java.io.Serializable
{
    public StringSequence ()
    {

    }

    public StringSequence (SequenceBase<String> src)
    {
        _list.addAll (src._list);
    }

    public StringSequence (StringSequence src)
    {
        _list.addAll (src._list);
    }

    public StringSequence (DoubleSequence in)
    {
        for (Double d : in.get_list ())
        {
            _list.add (Utilities.formatDouble (d));
        }
    }

    public StringSequence (char[] chars)
    {
        for (char c : chars)
        {
            _list.add ("" + c);
        }
    }

    public StringSequence (List<String> list)
    {
        _list = new ArrayList<> (list);
    }

    public StringSequence (String csv)
    {
        this (csv.split (","));
    }

    public StringSequence (String[] in)
    {
        String[] out = new String[in.length];
        for (int s=0; s<in.length; s++)
            out[s] = Utilities.extractStringBody (in[s]);
        _list.addAll (Arrays.asList (out));
    }

    public String asString ()
    {
        StringBuilder sb = new StringBuilder ();
        for (String d : this._list)
        {
            sb.append (d);
        }
        return sb.toString ();
    }

    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder ();
        sb.append ('{');
        for (int x = 0; x < _list.size (); x++)
        {
            String s1 = '\"' + StringEscape.unescape (_list.get(x)) + '\"';
            sb.append (s1);
            if (x != _list.size () - 1)
            {
                sb.append (",");
            }
        }
        sb.append ('}');
        return sb.toString ();
    }

    public static StringSequence parseSequence (String in)
    {
        String seq = Utilities.extractSequence (in);
        if (seq == null)
        {
            return null;
        }
        return new StringSequence (seq);
    }

//////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        StringSequence sl = parseSequence ("{das,ist,toll}");
        System.out.println (sl);
    }
}
