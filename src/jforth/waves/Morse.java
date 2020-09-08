package jforth.waves;

import java.util.HashMap;

public class Morse
{
    static private final HashMap<Character, CharSequence> dict = new HashMap<>();

    static
    {
        dict.put('A', "\u00b7-");
        dict.put('B', "-\u00b7\u00b7\u00b7");
        dict.put('C', "-\u00b7-\u00b7");
        dict.put('D', "-\u00b7\u00b7");
        dict.put('E', "\u00b7");
        dict.put('F', "\u00b7\u00b7-\u00b7");
        dict.put('G', "--\u00b7");
        dict.put('H', "\u00b7\u00b7\u00b7\u00b7");
        dict.put('I', "\u00b7\u00b7");
        dict.put('J', "\u00b7---");
        dict.put('K', "-\u00b7-");
        dict.put('L', "\u00b7-\u00b7\u00b7");
        dict.put('M', "--");
        dict.put('N', "-\u00b7");
        dict.put('O', "---");
        dict.put('P', "\u00b7--\u00b7");
        dict.put('Q', "--\u00b7-");
        dict.put('R', "\u00b7-\u00b7");
        dict.put('S', "\u00b7\u00b7\u00b7");
        dict.put('T', "-");
        dict.put('U', "\u00b7\u00b7-");
        dict.put('V', "\u00b7\u00b7\u00b7-");
        dict.put('W', "\u00b7--");
        dict.put('X', "-\u00b7\u00b7-");
        dict.put('Y', "-\u00b7--");
        dict.put('Z', "--\u00b7\u00b7");

        dict.put('0', "-----");
        dict.put('1', "\u00b7----");
        dict.put('2', "\u00b7\u00b7---");
        dict.put('3', "\u00b7\u00b7\u00b7--");
        dict.put('4', "\u00b7\u00b7\u00b7\u00b7-");
        dict.put('5', "\u00b7\u00b7\u00b7\u00b7\u00b7");
        dict.put('6', "-\u00b7\u00b7\u00b7\u00b7");
        dict.put('7', "--\u00b7\u00b7\u00b7");
        dict.put('8', "---\u00b7\u00b7");
        dict.put('9', "----\u00b7");

        dict.put('.', "\u00b7-\u00b7-\u00b7-");
        dict.put(',', "--\u00b7\u00b7--");
        dict.put('?', "\u00b7\u00b7--\u00b7\u00b7");
        dict.put(':', "---\u00b7\u00b7\u00b7");
        dict.put('-', "-\u00b7\u00b7\u00b7\u00b7-");
        dict.put('@', "\u00b7--\u00b7-\u00b7");
    }

    private static boolean isAlreadyMorse (String in)
    {
        for (char c : in.toCharArray ())
        {
            if (c != '-' && c != '\u00b7' && c != ' ')
                return false;
        }
        return true;
    }

    /**
     * Converts text into morse representation
     * @param in Clear Text
     * @return Morse out
     */
    public static String text2Morse (String in)
    {
        if (isAlreadyMorse (in))
            return in;
        StringBuilder out = new StringBuilder();
        in = in.toUpperCase();
        for (int s=0; s<in.length(); s++)
        {
            CharSequence s1 = dict.get(in.charAt(s));
            if (s1 != null)
                out.append(s1);
            if (s < in.length ()-1)
                out.append(' ');
        }
        return out.toString();
    }
}
