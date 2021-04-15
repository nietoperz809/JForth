package tools;

public class SpecialChars {
    private static final char ESC = '\\';
    private StringBuilder coll = new StringBuilder();
    private StringBuilder out = new StringBuilder();
    private int state = 0;

    private void finish()
    {
        if (state == 1) {
            if (coll.length() == 0)
                out.append(ESC);
            else {
                int x = Integer.parseInt(coll.toString(), 8);
                out.append((char) x);
            }
            coll.setLength(0);
            state = 0;
        }
    }

    public String convertSC(String in)
    {
        for (char c : in.toCharArray())
        {
            switch (state)
            {
                case 0:
                    if (c == ESC)
                        state = 1;
                    else
                        out.append(c);
                    break;
                case 1:
                    if (c <= '7' && c >= '0') // octal digits
                        coll.append(c);
                    else
                    {
                        finish();
                        out.append(c);
                    }
                    break;
            }
        }
        finish();
        return out.toString();
    }

    public static void main(String[] args) {
        System.out.println(new SpecialChars().convertSC("peter|123 ist lieb|"));
        System.out.println(new SpecialChars().convertSC("peter| ist lieb|122"));
    }
}
