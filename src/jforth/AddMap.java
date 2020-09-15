package jforth;

import java.util.HashMap;

/**
 * Implements a vector of Doubles
 */
class AddMap
{
    private final HashMap<Integer, Double> ar = new HashMap<>();

    public void add (double val, int pos)
    {
        Double d = ar.get(pos);
        if (d == null)
            d = 0.0;
        d += val;
        ar.put (pos, d);
    }

    public double[] toArray()
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
//        Double[] vals = ar.values().toArray(new Double[0]);
//        double[] valsd = Arrays.stream(vals).mapToDouble (Double::doubleValue).toArray();
        return da;
    }
}
