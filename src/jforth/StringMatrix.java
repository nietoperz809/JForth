
// not used

package jforth;

import java.awt.*;

public class StringMatrix
{
    private final String[][] matrix;
    private final Dimension dim = new Dimension ();

    public StringMatrix (int dimx, int dimy)
    {
        matrix = new String[dimx][dimy];
        dim.width = dimx;
        dim.height = dimy;
    }

    public void put (int x, int y, String s)
    {
        matrix[x][y] = s;
    }

    public String get (int x, int y)
    {
        return matrix[x][y];
    }

    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder ();
        sb.append ("{\n");
        for (int y=0; y<dim.height; y++)
        {
            sb.append ('{');
            for (int x = 0; x < dim.width; x++)
            {
                sb.append (matrix[x][y]);
                if (x != dim.width-1)
                    sb.append (", ");
            }
            sb.append ('}');
            if (y != dim.height-1)
                sb.append (", ");
            sb.append ('\n');
        }
        sb.append ('}');
        return sb.toString ();
    }

    public static void main (String[] args)
    {
        StringMatrix sm = new StringMatrix (10,10);
        sm.put (2,4, "doof");
        System.out.println (sm);
    }
}
