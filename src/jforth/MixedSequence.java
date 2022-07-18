package jforth;

// TODO: experimental class

public class MixedSequence extends SequenceBase<String> {

    public static MixedSequence parseSequence (String in) {
        if (in.startsWith("{") && in.endsWith("}")) {
            in = in.substring(1,in.length()-1);
            System.out.println(in);
            System.out.println(parse (in));
        }
        return null;
    }

    private static String parse (String in) {
        int state = 0;
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray()) {
            switch (state) {
                case 0:
                    if (c == '{') {
                        state = 1;
                        break;
                    }
                    if (c == '\"') {
                        state = 2;
                        break;
                    }
                    sb.append(c);
                case 1:
                    if (c == '}' && state == 1) {
                        state = 0;
                        break;
                    }
                case 2:
                    if (c == '\"' && state == 2) {
                        state = 0;
                        break;
                    }


            }
        }
        return sb.toString();
    }
}
