package jforth;

// TODO: experimental class

import tools.Utilities;

import java.util.ArrayList;

/*
 {{1,2,3};{peter,ist,lieb};"motha,fucka";"mothafucka";3+4i;1103;2/4}
 */

public class MixedSequence extends SequenceBase {

    public static MixedSequence parseSequence (String in) {
        if (in.startsWith("{") && in.endsWith("}")) {
            MixedSequence ms = new MixedSequence();
            in = Utilities.extractSequence (in);
            String[] parts = split(in);
            if (parts == null)
                return null;
            for (String s : parts) {
                JForth.doForKnownWords (s, ms._list::add, 10);
            }
            return ms;
        }
        return null;
    }

    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder ();
        sb.append ('{');
        for (int x = 0; x < _list.size (); x++)
        {
            Object o = _list.get(x);
            String s1 = Utilities.makePrintable(o, 10);
            if (o instanceof String)
                sb.append("\"");
            sb.append (s1);
            if (o instanceof String)
                sb.append("\"");
            if (x != _list.size () - 1)
            {
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
