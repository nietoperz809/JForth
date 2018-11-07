package jforth;

public class StringEscape
{
    enum State {IN, OUT};
    private static char repl = 9999;

    public static String unescape (String in)
    {
        return in.replace(repl, ' ');
    }

    public static String escape (String in)
    {
        StringBuilder coll = new StringBuilder();
        State state = State.OUT;
        for (char c : in.toCharArray())
        {
            switch (state)
            {
                case OUT:
                    if (c == '\"')
                        state = State.IN;
                    coll.append(c);
                    break;

                case IN:
                    if (c == '\"')
                        state = State.OUT;
                    if (Character.isSpaceChar(c))
                        coll.append(repl);
                    else
                        coll.append(c);
                    break;
            }
        }
        return coll.toString();
    }

}
