
// not used

package jforth;


public class StringSequence
{
    private final String[] _list;

    public StringSequence (int size)
    {
        _list = new String[size];
    }

    public StringSequence (String csv)
    {
        this (csv.split(","));
    }

    public StringSequence (String[] in)
    {
        _list = in;
    }

    public void put (int x, String s)
    {
        _list[x] = s;
    }

    public String get (int x)
    {
        return _list[x];
    }

    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder ();
            sb.append ('{');
            for (int x = 0; x <_list.length; x++)
            {
                sb.append (_list[x]);
                if (x != _list.length-1)
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
