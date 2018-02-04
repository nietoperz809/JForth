package jforth;

import java.util.HashMap;

class AddMap
{
    private final HashMap<Integer, Double> ar = new HashMap<>();

    void add (double val, int pos)
    {
        Double d = ar.get(pos);
        if (d == null)
            d = 0.0;
        d += val;
        ar.put (pos, d);
    }

    double[] toArray()
    {
        int max = 0;
        for (Integer n : ar.keySet())
        {
            max = n;
            //System.out.println(n);
        }
        double[] da = new double[max+1];
        for (Integer n : ar.keySet())
        {
            da[n] = ar.get(n);
        }
        return da;
    }
}
