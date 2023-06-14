package jforth.seq;

import java.math.BigInteger;
import java.util.ArrayList;

public class BigSequence extends SequenceBase<BigInteger> implements java.io.Serializable {
    public BigSequence() {

    }

    public BigSequence(BigInteger[] vals) {
        for (BigInteger val : vals) {
            _list.add(val);
        }
    }

    public BigSequence(ArrayList<BigInteger> vals) {
        for (BigInteger val : vals) {
            _list.add(val);
        }
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


}
