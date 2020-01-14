package jforth;

public class CayleyTable
{
    private final DoubleMatrix _m;

    public CayleyTable (int order) throws Exception
    {
        if (order < 2)
        {
            throw new Exception ("order must be >= 2");
        }
        double[] arr = new double[order];
        _m = new DoubleMatrix (order, order);
        for (int row = 0; row < order; row++)
        {
            for (int s = 0; s < order; s++)
            {
                arr[s] = 1 + (s + row) % order;
            }
            _m.setRow (row, arr);
        }
    }

    // Test
    public static void main (String[] args) throws Exception
    {
        CayleyTable c = new CayleyTable (4);
        System.out.println (c._m);
    }
}
