package jforth.seq;

import tools.Utilities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class BigSequence extends SequenceBase<BigInteger> implements java.io.Serializable {
    public BigSequence() {

    }

    public BigSequence(BigInteger[] vals) {
        _list.addAll(Arrays.asList(vals));
    }

    public BigSequence(ArrayList<BigInteger> vals) {
        _list.addAll(vals);
    }

    public BigSequence(DoubleSequence vals) {
        _list.addAll(Arrays.asList(vals.asBigIntArray()));
    }


    public static BigSequence parseSequence(String in, int base) {
        if (base != 10)
            return null;
        if (in.equals("{}"))
            return new BigSequence();
        String seq = Utilities.extractSequence(in);
        if (seq == null)
            return null;
        BigInteger[] arr = Utilities.parseCSVtoBigArray(seq);
        if (arr == null)
            return null;
        return new BigSequence(arr);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String str;
        sb.append('{');
        if (_list.size() > 0) {
            for (int s = 0; s < _list.size() - 1; s++) {
                str = _list.get(s).toString();
                sb.append(str).append(',');
            }
            str = str = _list.get(_list.size()-1).toString();
            sb.append(str);
        }
        sb.append('}');
        return sb.toString();
    }

    public BigInteger prod() {
        BigInteger ret = BigInteger.ONE;
        ArrayList<BigInteger> arr = this.get_list();
        for (BigInteger bi : arr) {
            ret = ret.multiply(bi);
        }
        return ret;
    }

    public BigInteger sum() {
        BigInteger ret = BigInteger.ZERO;
        ArrayList<BigInteger> arr = this.get_list();
        for (BigInteger bi : arr) {
            ret = ret.add(bi);
        }
        return ret;
    }
}
