package jforth;

public class StringEscape
{
    enum State {IN, OUT, DOT, DOTQUOTE}
    private static final char repl = 9999;

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
                    {
                        state = State.IN;
                    }
                    else if (c == '.')
                    {
                        state = State.DOT;
                    }
                    coll.append(c);
                    break;

                case DOT:
                    if (c == '\"')
                    {
                        state = State.DOTQUOTE;
                    }
                    else
                    {
                        state = State.OUT;
                    }
                    coll.append(c);
                    break;

                case DOTQUOTE:
                    if (c == '\"')
                    {
                        state = State.OUT;
                    }
                    coll.append(c);
                    break;

                case IN:
                    if (c == '\"')
                    {
                        state = State.OUT;
                    }
                    if (Character.isSpaceChar(c))
                    {
                        coll.append(repl);
                    }
                    else
                    {
                        coll.append(c);
                    }
                    break;
            }
        }
        return coll.toString();
    }

}
