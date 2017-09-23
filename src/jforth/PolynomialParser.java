package jforth;

import java.util.ArrayList;
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

public class PolynomialParser
{
    private static String adjustX (String in)
    {
        if (in.matches("[+-]?\\d*(\\.\\d+)?")) // numeric
        {
            in = in+"x^0";
        }
        if (in.startsWith("-x"))
            in = in.replace("-x", "-1x");
        else if (in.startsWith("x"))
            in = in.replace("x", "+1x");
        else if (in.startsWith("+x"))
            in = in.replace("+x", "+1x");
        if (Character.isDigit(in.charAt(0)))
            in = "+"+in;
        if (in.endsWith("x"))
            return in+"^1";
        return in;
    }

    public static double[] parsePolynomial (String poly, int base)
    {
        if (base != 10)
            return null;
        try
        {
            AddMap adm = new AddMap();
            ArrayList<String> arl = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (int s = 0; s < poly.length(); s++)
            {
                char c = poly.charAt(s);
                if (Character.isWhitespace(c))
                    continue;
                if (c == '+' || c == '-')
                {
                    arl.add(adjustX(sb.toString()));
                    sb = new StringBuilder();
                }
                sb.append(c);
            }
            arl.add(adjustX(sb.toString()));

            for (String v : arl)
            {
                int sign = v.charAt(0) == '-' ? -1 : 1;
                v = v.substring(1);
                int xindex = v.indexOf("x^");
                String val = v.substring(0, xindex);
                String exp = v.substring(xindex + 2, v.length());
                adm.add(Double.parseDouble(val) * sign, Integer.parseInt(exp));
            }
            return adm.toArray();
        }
        catch (Exception unused)
        {
            return null;
        }
    }
}
