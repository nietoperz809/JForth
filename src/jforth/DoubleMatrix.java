package jforth;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import tools.Utilities;

import java.util.ArrayList;

import static tools.Utilities.parseCSVtoDoubleArray;

public class DoubleMatrix extends BlockRealMatrix implements java.io.Serializable
{
    public DoubleMatrix (RealMatrix src)
    {
        super(src.getData());
    }

    DoubleMatrix (int rows, int cols)
    {
        super(rows, cols);
    }

    public DoubleSequence[] toSequence()
    {
        DoubleSequence[] rows = new DoubleSequence[getRowDimension()];
        for (int s=0; s<rows.length; s++)
        {
            rows[s] = new DoubleSequence(getRow(s));
        }
        return rows;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        String str;
        sb.append("{");
        for (int s=0; s<getRowDimension(); s++)
        {
            sb.append("{");
            double[] d = getRow(s);
            for (int i=0; i<d.length-1; i++)
            {
                str = Utilities.removeTrailingZero(d[i], true);
                sb.append(str).append(',');
            }
            str = Utilities.removeTrailingZero(d[d.length - 1], true);
            sb.append(str);
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    static public DoubleMatrix identity (int dimension)
    {
        RealMatrix r = MatrixUtils.createRealIdentityMatrix(dimension);
        return new DoubleMatrix(r);
    }

    static public DoubleMatrix diagonal (DoubleSequence ds)
    {
        RealMatrix r = MatrixUtils.createRealDiagonalMatrix(ds.asPrimitiveArray());
        return new DoubleMatrix(r);
    }

    public static DoubleMatrix fromSequenceArray (ArrayList<DoubleSequence> arr)
    {
        int max = 0;
        for (DoubleSequence d : arr)
        {
            if (d.length() > max)
                max = d.length();
        }
        DoubleMatrix res = new DoubleMatrix(arr.size(), max);
        for (int s=0; s<arr.size(); s++)
        {
            double[] pa = arr.get(s).asPrimitiveArray();
            if (pa.length < max)
            {
                double[] d2 = new double[max];
                System.arraycopy(pa,0,d2,0, pa.length);
                res.setRow(s, d2);
            }
            else
            {
                res.setRow(s, pa);
            }
        }
        return res;
    }

    static DoubleMatrix parseMatrix (String in, int base)
    {
        if (base != 10)
            return null;
        if (in.startsWith("{{") && in.endsWith("}}"))
        {
            in = in.substring(2, in.length()-2);
            String[] rows = in.split("}\\{");
            double[][] mtd = new double[rows.length][];
            int max = 0;
            for (int s=0; s<rows.length; s++)
            {
                mtd[s] = parseCSVtoDoubleArray(rows[s]);
                if (mtd[s].length>max)
                    max = mtd[s].length;
            }
            DoubleMatrix m = new DoubleMatrix(rows.length, max);
            for (int s=0; s<rows.length; s++)
            {
                if (mtd[s].length < max)
                {
                    double[] d2 = new double[max];
                    System.arraycopy(mtd[s],0,d2,0,mtd[s].length);
                    m.setRow(s, d2);
                }
                else
                {
                    m.setRow(s, mtd[s]);
                }
            }
            return m;
        }
        return null;
    }
}
