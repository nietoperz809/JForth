import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class JForth
{
    public static final Long TRUE = 1L;
    public static final Long FALSE = 0L;
    private static final String ANSI_CLS = "\u001b[2J";
    private static final String ANSI_BOLD = "\u001b[1m";
    private static final String ANSI_YELLOW = "\u001b[33m";
    private static final String ANSI_NORMAL = "\u001b[0m";
    private static final String ANSI_WHITEONBLUE = "\u001b[37;44m";
    private static final String ANSI_ERROR = "\u001b[93;41m";
    private static final String PROMPT = "\n> ";
    private static final String OK = " OK";
    private static final int HISTORY_LENGTH = 1000;
    public final Random random;
    public WordsList dictionary = new WordsList();
    public OStack dStack = new OStack();
    public OStack vStack = new OStack();
    public final History history;
    public transient PrintStream _out; // output channel
    public boolean compiling;
    public int base;
    public NonPrimitiveWord wordBeingDefined = null;
    private StreamTokenizer st = null;


    private JForth (PrintStream out)
    {
        _out = out;
        compiling = false;
        base = 10;
        random = new Random();
        history = new History(HISTORY_LENGTH);
        new DictionaryFiller(this, dictionary);
    }

    public static void main (String[] args) throws IOException, ClassNotFoundException
    {
        AnsiConsole.systemInstall();
        JForth forth = new JForth(AnsiConsole.out);
        forth.setPrintStream (AnsiConsole.out());
        forth.outerInterpreter();
    }

    private void setPrintStream (PrintStream printStream)
    {
        _out = printStream;
    }

    public void play()
    {
        for (String s : history.history)
        {
            if (s.equals ("playHist"))
                continue;
            _out.println(s);
            if (!interpretLine(s))
            {
                dStack.removeAllElements();
            }
            _out.flush();
        }
    }

    private void outerInterpreter ()
    {
        dStack.removeAllElements();
        Scanner scanner = new Scanner(System.in);
        _out.println("JForth, Build: " + Utilities.BUILD_NUMBER + ", " + Utilities.BUILD_DATE);
        while (true)
        {
            _out.print(PROMPT);
            _out.flush();
            String input = scanner.nextLine().trim();
            history.add(input);
            if (!interpretLine(input))
            {
                dStack.removeAllElements();
            }
            else
            {
                _out.print(OK);
            }
            _out.flush();
        }
    }


    public static String stackElementToString (Object o, int base)
    {
        String outstr;
        if (o instanceof Long)
        {
            outstr = Long.toString((Long) o, base).toUpperCase();
        }
        else if (o instanceof DoubleSequence)
        {
            outstr = o.toString();
        }
        else if (o instanceof Double)
        {
            outstr = Double.toString((Double) o);
        }
        else if (o instanceof Complex)
        {
            outstr = Utilities.formatComplex((Complex) o);
        }
        else if (o instanceof Fraction)
        {
            outstr = Utilities.formatFraction((Fraction) o);
        }
        else if (o instanceof String)
        {
            outstr = (String) o;
        }
        else if (o instanceof BaseWord)
        {
            outstr = "BaseWord address on stack";
        }
        else if (o instanceof FileInputStream)
        {
            outstr = "FileInputStream address on stack";
        }
        else if (o instanceof BufferedReader)
        {
            outstr = "BufferedReader address on stack";
        }
        else if (o instanceof PrintStream)
        {
            outstr = "PrintStream address on stack";
        }
        else
        {
            return null;
        }
        return ANSI_YELLOW + ANSI_BOLD + outstr + ANSI_NORMAL;
    }

    public String getNextToken ()
    {
        try
        {
            if (st.nextToken() != StreamTokenizer.TT_EOF)
            {
                return st.sval;
            }
            else
            {
                return null;
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return null;
        }
    }

    public int fileLoad (String fileName)
    {
        File f = new File(fileName);
        if (!f.exists())
        {
            return 0;
        }
        BufferedReader file = null;
        try
        {
            FileReader fr = new FileReader(fileName);
            file = new BufferedReader(fr);
            String text = file.readLine();
            while (text != null)
            {
                if (!interpretLine(text))
                {
                    return 0;
                }
                text = file.readLine();
            }
            return 1;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
        finally
        {
            try
            {
                assert file != null;
                file.close();
            }
            catch (Exception ignored)
            {
            }
        }
    }

    private boolean interpretLine (String text)
    {
        try
        {
            StringReader sr = new StringReader(text);
            st = new StreamTokenizer(sr);
            st.resetSyntax();
            st.wordChars('!', '~');
            //st.quoteChar('"');
            st.whitespaceChars('\u0000', '\u0020');
            int ttype = st.nextToken();
            while (ttype != StreamTokenizer.TT_EOF)
            {
                String word = st.sval;
                if (word.equals("(")) // filter out comments
                {
                    for (;;)
                    {
                        st.nextToken();
                        String word2 = st.sval;
                        if (word2.endsWith(")"))
                            break;
                    }
                    st.nextToken();
                    continue;
                }
                if (!compiling)
                {
                    if (word.equals(".\""))
                    {
                        StringBuilder sb = new StringBuilder();
                        for(;;)
                        {
                            st.nextToken();
                            String word2 = st.sval;
                            if (word2.endsWith("\""))
                            {
                                sb.append(word2.substring(0, word2.length()-1));
                                break;
                            }
                            sb.append(word2).append(' ');
                        }
                        dStack.push (sb.toString());
                        word = ".";
                    }
                    else
                    {
                        String ws = Utilities.parseString(word);
                        if (ws != null)
                        {
                            dStack.push(ws);
                            ttype = st.nextToken();
                            continue;
                        }
                    }
                    BaseWord bw = dictionary.search(word);
                    if (bw != null)
                    {
                        if (bw.execute(dStack, vStack) == 0)
                        {
                            _out.print(word + " - " + ANSI_ERROR +
                                    " word execution or stack error "+
                                    ANSI_NORMAL);
                            history.removeLast();
                            return false;
                        }
                    }
                    else
                    {
                        Long num = Utilities.parseLong (word, base);
                        if (num != null)
                        {
                            dStack.push(num);
                        }
                        else
                        {
                            Double dnum = Utilities.parseDouble(word);
                            if (dnum != null)
                            {
                                dStack.push(dnum);
                            }
                            else
                            {
                                Complex co = Utilities.parseComplex(word);
                                if (co != null)
                                {
                                    dStack.push(co);
                                }
                                else
                                {
                                    Fraction fr = Utilities.parseFraction(word);
                                    if (fr != null)
                                    {
                                        dStack.push(fr);
                                    }
                                    else
                                    {
                                        DoubleSequence lo = DoubleSequence.parseSequence(word);
                                        if (lo != null)
                                        {
                                            dStack.push(lo);
                                        }
                                        else
                                        {
                                            _out.print(word + " ?");
                                            history.removeLast();
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    if (word.equals(".\""))
                    {
                        StringBuilder sb = new StringBuilder();
                        for(;;)
                        {
                            st.nextToken();
                            String word2 = st.sval;
                            if (word2.endsWith("\""))
                            {
                                sb.append(word2.substring(0, word2.length()-1));
                                break;
                            }
                            sb.append(word2).append(' ');
                        }
                        wordBeingDefined.addWord(new StringLiteral(sb.toString()));
                        word = ".";
                    }
                    else
                    {
                        String ws = Utilities.parseString(word);
                        if (ws != null)
                        {
                            wordBeingDefined.addWord(new StringLiteral(word));
                            ttype = st.nextToken();
                            continue;
                        }
                    }
                    BaseWord bw = dictionary.search(word);
                    if (bw != null)
                    {
                        if (bw.immediate)
                        {
                            bw.execute(dStack, vStack);
                        }
                        else
                        {
                            wordBeingDefined.addWord(bw);
                        }
                    }
                    else
                    {
                        Long num = Utilities.parseLong (word, base);
                        if (num != null)
                        {
                            wordBeingDefined.addWord(new LongLiteral(num));
                        }
                        else
                        {
                            Double dnum = Utilities.parseDouble(word);
                            if (dnum != null)
                            {
                                wordBeingDefined.addWord(new DoubleLiteral(dnum));
                            }
                            else
                            {
                                _out.print(word + " ?");
                                compiling = false;
                                return false;
                            }
                        }
                    }
                }
                ttype = st.nextToken();
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

}
