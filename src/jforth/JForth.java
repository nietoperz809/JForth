package jforth;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.fusesource.jansi.AnsiConsole;
import scala.math.BigInt;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class JForth
{
    public static final Long TRUE = 1L;
    public static final Long FALSE = 0L;
    // --Commented out by Inspection (3/25/2017 10:54 AM):private static final String ANSI_CLS = "\u001b[2J";
    private static final String ANSI_BOLD = "\u001b[1m";
    private static final String ANSI_YELLOW = "\u001b[33m";
    private static final String ANSI_NORMAL = "\u001b[0m";
    // --Commented out by Inspection (3/25/2017 10:54 AM):private static final String ANSI_WHITEONBLUE = "\u001b[37;44m";
    private static final String ANSI_ERROR = "\u001b[93;41m";
    private static final String PROMPT = "\n> ";
    private static final String OK = " OK";
    private static final int HISTORY_LENGTH = 1000;
    public final Random random;
    public final History history;
    public final WordsList dictionary = new WordsList();
    private final OStack dStack = new OStack();
    private final OStack vStack = new OStack();
    public transient PrintStream _out; // output channel
    public boolean compiling;
    public int base;
    public NonPrimitiveWord wordBeingDefined = null;
    private StreamTokenizer st = null;

    public JForth ()
    {
        compiling = false;
        base = 10;
        random = new Random();
        history = new History(HISTORY_LENGTH);
        new PredefinedWords(this, dictionary);
    }

    public JForth (PrintStream out)
    {
        this();
        _out = out;
    }

    public static void main (String[] args) throws IOException, ClassNotFoundException
    {
        AnsiConsole.systemInstall();
        JForth forth = new JForth(AnsiConsole.out);
        forth.outerInterpreter();
    }

    public void setPrintStream (PrintStream printStream)
    {
        _out = printStream;
    }

    public void singleShot (String input)
    {
        history.add(input);
        if (!interpretLine(input))
        {
            dStack.removeAllElements();
        }
        else
        {
            _out.print(OK);
        }
        _out.print(PROMPT);
        _out.flush();
    }

    private void outerInterpreter ()
    {
        dStack.removeAllElements();
        Scanner scanner = new Scanner(System.in);
        _out.println("JForth, Build: " + Utilities.BUILD_NUMBER + ", " + Utilities.BUILD_DATE);
        singleShot ("\n"); // to show prompt immediately
        while (true)
        {
            singleShot (scanner.nextLine().trim());
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
                if (word.equals("\\"))   // Comment until line end
                {
                    return true;
                }
                if (word.equals("(")) // filter out comments
                {
                    for (; ; )
                    {
                        st.nextToken();
                        String word2 = st.sval;
                        if (word2.endsWith(")"))
                        {
                            break;
                        }
                    }
                    st.nextToken();
                    continue;
                }
                if (!compiling)
                {
                    if (!doInterpret(word, st))
                    {
                        return false;
                    }
                }
                else
                {
                    if (!doCompile(word, st))
                    {
                        return false;
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

    private boolean doInterpret (String word, StreamTokenizer st) throws Exception
    {
        if (word.equals(".\""))
        {
            StringBuilder sb = new StringBuilder();
            for (; ; )
            {
                st.nextToken();
                String word2 = st.sval;
                if (word2.endsWith("\""))
                {
                    sb.append(word2.substring(0, word2.length() - 1));
                    break;
                }
                sb.append(word2).append(' ');
            }
            dStack.push(sb.toString());
            word = ".";
        }
        BaseWord bw = dictionary.search(word);
        if (bw != null)
        {
            if (bw.execute(dStack, vStack) == 0)
            {
                if (_out == AnsiConsole.out)
                    _out.print(word + " - " + ANSI_ERROR +
                            " word execution or stack error " +
                            ANSI_NORMAL);
                else
                    _out.print(word +
                            " word execution or stack error");
                history.removeLast();
                return false;
            }
            return true;
        }
        Long num = Utilities.parseLong(word, base);
        if (num != null)
        {
            dStack.push(num);
            return true;
        }
        Double dnum = Utilities.parseDouble(word);
        if (dnum != null)
        {
            dStack.push(dnum);
            return true;
        }
        Complex co = Utilities.parseComplex(word);
        if (co != null)
        {
            dStack.push(co);
            return true;
        }
        Fraction fr = Utilities.parseFraction(word);
        if (fr != null)
        {
            dStack.push(fr);
            return true;
        }
        DoubleSequence lo = DoubleSequence.parseSequence(word);
        if (lo != null)
        {
            dStack.push(lo);
            return true;
        }
        String ws = Utilities.parseString(word);
        if (ws != null)
        {
            dStack.push(ws);
            return true;
        }
        double[] pd = PolynomParser.parsePolynom(word);
        if (pd != null)
        {
            dStack.push(new PolynomialFunction(pd));
            return true;
        }
        _out.print(word + " ?");
        history.removeLast();
        return false;
    }

    private boolean doCompile (String word, StreamTokenizer st) throws Exception
    {
        if (word.equals(".\""))
        {
            StringBuilder sb = new StringBuilder();
            for (; ; )
            {
                st.nextToken();
                String word2 = st.sval;
                if (word2.endsWith("\""))
                {
                    sb.append(word2.substring(0, word2.length() - 1));
                    break;
                }
                sb.append(word2).append(' ');
            }
            wordBeingDefined.addWord(new StringLiteral(sb.toString()));
            word = ".";
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
            return true;
        }
        Long num = Utilities.parseLong(word, base);
        if (num != null)
        {
            wordBeingDefined.addWord(new LongLiteral(num));
            return true;
        }
        Double dnum = Utilities.parseDouble(word);
        if (dnum != null)
        {
            wordBeingDefined.addWord(new DoubleLiteral(dnum));
            return true;
        }
        DoubleSequence ds = DoubleSequence.parseSequence(word);
        if (ds != null)
        {
            wordBeingDefined.addWord(new DListLiteral(ds));
            return true;
        }
        Fraction fr = Utilities.parseFraction(word);
        if (fr != null)
        {
            wordBeingDefined.addWord(new FractionLiteral(fr));
            return true;
        }
        Complex cpl = Utilities.parseComplex(word);
        if (cpl != null)
        {
            wordBeingDefined.addWord(new ComplexLiteral(cpl));
            return true;
        }
        String ws = Utilities.parseString(word);
        if (ws != null)
        {
            wordBeingDefined.addWord(new StringLiteral(ws));
            return true;
        }
        double[] pd = PolynomParser.parsePolynom(word);
        if (pd != null)
        {
            wordBeingDefined.addWord(
                    new PolynomLiteral(
                            new PolynomialFunction(pd)));
            return true;
        }
        _out.print(word + " ?");
        compiling = false;
        return false;
    }

    public String stackElementToString (Object o, int base)
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
        else if (o instanceof PolynomialFunction)
        {
            outstr = ((PolynomialFunction) o).toString().replaceAll("\\s", "");
        }
        else if (o instanceof BigInt)
        {
            outstr = ((BigInt)o).toString();
        }
        else
        {
            return null;
        }
        if (_out != AnsiConsole.out)
            return outstr;
        return ANSI_YELLOW + ANSI_BOLD + outstr + ANSI_NORMAL;
    }

    public void play ()
    {
        for (String s : history.history)
        {
            if (s.equals("playHist"))
            {
                continue;
            }
            _out.println(s);
            if (!interpretLine(s))
            {
                dStack.removeAllElements();
            }
            _out.flush();
        }
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
}