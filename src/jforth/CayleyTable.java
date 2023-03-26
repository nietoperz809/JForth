package jforth;

import jforth.seq.DoubleMatrix;

public class CayleyTable {
    private final DoubleMatrix _m;

    public CayleyTable(int order) throws Exception {
        if (order < 2) {
            throw new Exception("order must be >= 2");
        }
        int dim = order - 1;
        _m = new DoubleMatrix(dim, dim);
        for (int s = 0; s < dim; s++) {
            for (int t = 0; t < dim; t++) {
                int val = ((s + 1) * (t + 1)) % order;
                if (val == 0)
                    throw new Exception("Zero generated!");
                _m.setEntry(s, t, val);
            }
        }
    }

    public DoubleMatrix getMatrix() {
        return _m;
    }

    // Test
    public static void main(String[] args) throws Exception {
        CayleyTable c = new CayleyTable(4);
        System.out.println(c._m);
    }
}
