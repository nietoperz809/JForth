package jforth;

import java.io.IOException;

public class Brainfuck
{
    private byte[] mem = new byte[30000];
    private int dptr;

    public String interpret (String code)
    {
        StringBuilder sb = new StringBuilder ();
        int l = 0;
        for (int i = 0; i < code.length (); i++)
        {
            if (code.charAt (i) == '>')
            {
                dptr = (dptr == mem.length - 1) ? 0 : dptr + 1;
            }
            else if (code.charAt (i) == '<')
            {
                dptr = (dptr == 0) ? mem.length - 1 : dptr - 1;
            }
            else if (code.charAt (i) == '+')
            {
                mem[dptr]++;
            }
            else if (code.charAt (i) == '-')
            {
                mem[dptr]--;
            }
            else if (code.charAt (i) == '.')
            {
               sb.append ((char) mem[dptr]);
            }
            else if (code.charAt (i) == ',')
            {
                try
                {
                    mem[dptr] = (byte) RawConsoleInput.read (true);
                }
                catch (IOException e)
                {
                    e.printStackTrace ();
                }
            }
            else if (code.charAt (i) == '[')
            {
                if (mem[dptr] == 0)
                {
                    i++;
                    while (l > 0 || code.charAt (i) != ']')
                    {
                        if (code.charAt (i) == '[')
                        {
                            l++;
                        }
                        if (code.charAt (i) == ']')
                        {
                            l--;
                        }
                        i++;
                    }
                }
            }
            else if (code.charAt (i) == ']')
            {
                if (mem[dptr] != 0)
                {
                    i--;
                    while (l > 0 || code.charAt (i) != '[')
                    {
                        if (code.charAt (i) == ']')
                        {
                            l++;
                        }
                        if (code.charAt (i) == '[')
                        {
                            l--;
                        }
                        i--;
                    }
                    i--;
                }
            }
        }
        return sb.toString ();
    }

    public static void main (String[] args)
    {
        String test = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.";
        String out = new Brainfuck ().interpret (test);
        System.out.println (out);
    }
}