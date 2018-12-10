package jforth.waves;

import java.util.HashMap;

public class MorsePlayer
{
    static private HashMap<Character, String> dict = new HashMap<>();

    static
    {
        dict.put('A', ".-");
        dict.put('B', "-...");
        dict.put('C', "-.-.");
        dict.put('D', "-..");
        dict.put('E', ".");
        dict.put('F', "..-.");
        dict.put('G', "--.");
        dict.put('H', "....");
        dict.put('I', "..");
        dict.put('J', ".---");
        dict.put('K', "-.-");
        dict.put('L', ".-..");
        dict.put('M', "--");
        dict.put('N', "-.");
        dict.put('O', "---");
        dict.put('P', ".--.");
        dict.put('Q', "--.-");
        dict.put('R', ".-.");
        dict.put('S', "...");
        dict.put('T', "-");
        dict.put('U', "..-");
        dict.put('V', "...-");
        dict.put('W', ".--");
        dict.put('X', "-..-");
        dict.put('Y', "-.--");
        dict.put('Z', "--..");

        dict.put('0', "-----");
        dict.put('1', ".----");
        dict.put('2', "..---");
        dict.put('3', "...--");
        dict.put('4', "....-");
        dict.put('5', ".....");
        dict.put('6', "-....");
        dict.put('7', "--...");
        dict.put('8', "---..");
        dict.put('9', "----.");

        dict.put('.', ".-.-.-");
        dict.put(',', "--..--");
        dict.put('?', "..--..");
        dict.put(':', "---...");
        dict.put('-', "-....-");
        dict.put('@', ".--.-.");
        dict.put((char)0xffff, "........");    // Error
    }

    private static Wave16 dot = WaveForms.curveSine(11025, 1500, 1000,0);
    private static Wave16 dash = WaveForms.curveSine(11025, 3000, 1000,0);
    private static Wave16 pause = WaveForms.curveSine(11025, 5000, 0,0);
    private static Wave16 delay = WaveForms.curveSine(11025, 1000, 0,0);

    public static String text2Morse (String in)
    {
        StringBuilder out = new StringBuilder();
        in = in.toUpperCase();
        for (int s=0; s<in.length(); s++)
        {
            String s1 = dict.get(in.charAt(s));
            if (s1 != null)
                out.append(s1).append(' ');
        }
        return out.toString();
    }

    public static byte[] morse2Audio (String morse)
    {
        int totallen = 0;
        for(int s=0; s<morse.length(); s++)
        {
            switch(morse.charAt(s))
            {
                case '.':
                    totallen += dot.data.length;
                    break;
                case '-':
                    totallen += dash.data.length;
                    break;
                case ' ':
                    totallen += pause.data.length;
                    break;
            }
            totallen += delay.data.length;
        }
        byte[] out = new byte[totallen];
        int pos = 0;
        for(int s=0; s<morse.length(); s++)
        {
            switch(morse.charAt(s))
            {
                case '.':
                    System.arraycopy(dot.toByteArray(),
                            0, out, pos, dot.data.length);
                    pos += dot.data.length;
                    break;
                case '-':
                    System.arraycopy(dash.toByteArray(),
                            0, out, pos, dash.data.length);
                    pos += dash.data.length;
                    break;
                case ' ':
                    System.arraycopy(pause.toByteArray(),
                            0, out, pos, pause.data.length);
                    pos += pause.data.length;
                    break;
            }
            System.arraycopy(delay.toByteArray(),
                    0, out, pos, delay.data.length);
            pos += delay.data.length;
        }
        return out;
    }

    public static byte[] text2Wave (String in)
    {
        String morse = text2Morse(in);
        return morse2Audio(morse);
    }
}
