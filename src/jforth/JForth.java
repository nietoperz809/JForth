package jforth;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.fusesource.jansi.AnsiConsole;
import scala.math.BigInt;

import java.io.*;
import java.util.ArrayList;
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
    private static final String PROMPT = "\nJFORTH> ";
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
    public BaseWord currentWord;

    public JForth ()
    {
        compiling = false;
        base = 10;
        random = new Random();
        history = new History(HISTORY_LENGTH);
        _out = System.out;
        new PredefinedWords(this, dictionary);
    }

    public JForth (PrintStream out)
    {
        compiling = false;
        base = 10;
        random = new Random();
        history = new History(HISTORY_LENGTH);
        _out = out;
        new PredefinedWords(this, dictionary);
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

    /**
     * Execute one line and generate output
     * @param input String containung forth commands
     */
    public void singleShot (String input)
    {
        history.add(input);
        if (interpretLine(input))
        {
            if (_out == AnsiConsole.out)
                _out.print(input + " - " + ANSI_ERROR +
                        " word execution or stack error " +
                        ANSI_NORMAL);
            else
                _out.print(input +
                        " word execution or stack error");
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
        _out.println(Utilities.buildInfo);
        singleShot ("\n"); // to show prompt immediately
        try
        {
            executeFile("autoexec.4th");
        }
        catch (Exception unused)
        {
            // execution error
        }
        while (true)
        {
            singleShot (scanner.nextLine().trim());
        }
    }

    public boolean interpretLine (String text)
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
                    return false;
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
                        return true;
                    }
                }
                else
                {
                    if (!doCompile(word, st))
                    {
                        return true;
                    }
                }
                ttype = st.nextToken();
            }
            return false;
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            return true;
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
            if (bw instanceof NonPrimitiveWord)
            {
                currentWord = bw;  // Save for recursion
            }
            if (bw.execute(dStack, vStack) == 0)
            {
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
        BigInt big = Utilities.parseBigInt(word, base);
        if (big != null)
        {
            dStack.push(big);
            return true;
        }
        Double dnum = Utilities.parseDouble(word, base);
        if (dnum != null)
        {
            dStack.push(dnum);
            return true;
        }
        Complex co = Utilities.parseComplex(word, base);
        if (co != null)
        {
            dStack.push(co);
            return true;
        }
        Fraction fr = Utilities.parseFraction(word, base);
        if (fr != null)
        {
            dStack.push(fr);
            return true;
        }
        DoubleMatrix ma = DoubleMatrix.parseMatrix(word, base);
        if (ma != null)
        {
            dStack.push(ma);
            return true;
        }
        DoubleSequence lo = DoubleSequence.parseSequence(word, base);
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
        double[] pd = PolynomialParser.parsePolynomial(word, base);
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
        BigInt big = Utilities.parseBigInt(word, base);
        if (big != null)
        {
            wordBeingDefined.addWord(new BigIntLiteral(big));
            return true;
        }
        Double dnum = Utilities.parseDouble(word, base);
        if (dnum != null)
        {
            wordBeingDefined.addWord(new DoubleLiteral(dnum));
            return true;
        }
        DoubleMatrix ma = DoubleMatrix.parseMatrix(word, base);
        if (ma != null)
        {
            wordBeingDefined.addWord(new DMatrixLiteral(ma));
            return true;
        }
        DoubleSequence ds = DoubleSequence.parseSequence(word, base);
        if (ds != null)
        {
            wordBeingDefined.addWord(new DListLiteral(ds));
            return true;
        }
        Fraction fr = Utilities.parseFraction(word, base);
        if (fr != null)
        {
            wordBeingDefined.addWord(new FractionLiteral(fr));
            return true;
        }
        Complex cpl = Utilities.parseComplex(word, base);
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
        double[] pd = PolynomialParser.parsePolynomial(word, base);
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
        if (o == null)
            return null;
        if (o instanceof Long)
        {
            outstr = Long.toString((Long) o, base).toUpperCase();
        }
//        else if (o instanceof DoubleMatrix)
//        {
//            outstr = o.toString();
//        }
//        else if (o instanceof DoubleSequence)
//        {
//            outstr = o.toString();
//        }
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
        else if (o instanceof PolynomialFunction)
        {
            outstr = PolySupport.formatPoly((PolynomialFunction) o);
        }
        else if (o instanceof BigInt)
        {
            outstr = o.toString();
        }
        else
        {
            outstr = o.toString();
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
            if (interpretLine(s))
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

    public void executeFile (String fileName) throws Exception
    {
        ArrayList<String> as = Utilities.fileLoad(fileName);
        for (String s : as)
        {
            interpretLine(s);
        }
    }
}
