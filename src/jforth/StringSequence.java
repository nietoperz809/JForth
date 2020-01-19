
// not used

package jforth;


import java.util.*;

public class StringSequence
{
    private ArrayList<String> _list = new ArrayList<> ();

    public StringSequence (StringSequence src)
    {
        _list.addAll(src._list);
    }

    public StringSequence (List<String> list)
    {
        _list = new ArrayList<> (list);
    }


    public StringSequence (String csv)
    {
        this (csv.split(","));
    }

    public StringSequence (String[] in)
    {
        _list.addAll (Arrays.asList (in));
    }

    public StringSequence shuffle()
    {
        StringSequence ret = new StringSequence(this);
        Collections.shuffle(ret._list);
        return ret;
    }

    public StringSequence unique ()
    {
        ArrayList<String> nodupe = new ArrayList<>(new LinkedHashSet<> (_list));
        return new StringSequence(nodupe);
    }

    public StringSequence intersect (StringSequence other)
    {
        StringSequence ret = new StringSequence(this);
        ret._list.retainAll(other._list);
        return ret;
    }

    public void put (int x, String s)
    {
        _list.add (x, s);
    }

    public String pick (int x)
    {
        return _list.get(x);
    }

    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder ();
            sb.append ('{');
            for (int x = 0; x <_list.size(); x++)
            {
                sb.append (_list.get(x));
                if (x != _list.size()-1)
                    sb.append (",");
            }
            sb.append ('}');
        return sb.toString ();
    }

    public static StringSequence parseSequence (String in)
    {
        String seq = Utilities.extractSequence (in);
        if (seq == null)
            return null;
        return new StringSequence (seq);
    }


    public static void main (String[] args)
    {
        StringSequence sl = parseSequence ("{das,ist,toll}");
        System.out.println (sl);
    }
}
