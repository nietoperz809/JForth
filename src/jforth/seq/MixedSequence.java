package jforth.seq;

// TODO: experimental class

import jforth.JForth;
import tools.Utilities;

import java.util.ArrayList;

/*
 {{1,2,3};{peter,ist,lieb};"motha,fucka";"mothafucka";3+4i;1103;2/4;0.666;{1/2,1/6}}
 */

public class MixedSequence extends SequenceBase {

    public MixedSequence() {

    }

    public MixedSequence (ArrayList<?> ar) {
        this._list = ar;
    }

    public void addAnything (Object o) {
        _list.add(o);
    }

    public static MixedSequence parseSequence (String in) {
        if (in.startsWith("{") && in.endsWith("}")) {
            in = Utilities.extractSequence (in);
            String[] parts = split(in);
            if (parts == null)
                return null;
            MixedSequence ms = new MixedSequence();
            for (String s : parts) {
                boolean ret = JForth.doForKnownWordsUnmixed (s, ms._list::add, 10);
                if (!ret)
                    ms._list.add(s); // raw strings
            }
            return ms;
        }
        return null;
    }

    public String types() {
        StringBuilder sb = new StringBuilder ();
        sb.append ('{');
        for (int x = 0; x < _list.size (); x++) {
            Object o = _list.get(x);
            sb.append (o.getClass().getSimpleName());
            if (x < _list.size()-1)
                sb.append (",");
        }
        sb.append ('}');
        return sb.toString ();
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder ();
        sb.append ('{');
        for (int x = 0; x < _list.size (); x++) {
            Object o = _list.get(x);
            String s1 = Utilities.makePrintable(o, 10);
            if (o instanceof String)
                sb.append("\"");
            sb.append (s1);
            if (o instanceof String)
                sb.append("\"");
            if (x != _list.size () - 1) {
                sb.append (";");
            }
        }
        sb.append ('}');
        return sb.toString ();
    }

    private enum State {OUT, CURLY, QUOTE}

    /**
     * Split whole input into parts
     * @param in e.g.: {{1,2,3};{peter,ist,lieb};"motha,fucka";"mothafucka";3+4i;1103;2/4}
     *           or: {{1/2,3/4};hello}
     * @return Array of substrings
     */
    private static String[] split(String in) {
        State inQuote = State.OUT;
        int semiColons = 0;
        StringBuilder sb = new StringBuilder();
        ArrayList<String> arl = new ArrayList<>();
        for (char c : in.toCharArray()) {
            switch (inQuote) {
                case OUT:
                    if (c == ';') {
                        semiColons++;
                        if (sb.length() > 0) {
                            arl.add(sb.toString());
                            sb.setLength(0);
                        }
                        break;
                    }
                    sb.append(c);
                    if (c == '{') {
                        inQuote = State.CURLY;
                    }
                    if (c == '\"') {
                        inQuote = State.QUOTE;
                    }
                    break;
                case CURLY:
                    sb.append(c);
                    if (c == '}') {
                        inQuote = State.OUT;
                        arl.add(sb.toString());
                        sb.setLength(0);
                    }
                    break;
                case QUOTE:
                    sb.append(c);
                    if (c == '\"') {
                        inQuote = State.OUT;
                        arl.add(sb.toString());
                        sb.setLength(0);
                    }
                    break;
            }
        }
        if (semiColons == 0) // no mixedSeq
            return null;
        if (sb.length() > 0)
            arl.add(sb.toString());
        return arl.toArray(new String[0]);
    }

}
