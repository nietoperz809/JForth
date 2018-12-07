package jforth;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory;
import jforth.forthwords.PredefinedWords;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.fusesource.jansi.AnsiConsole;
//import scala.math.BigInt;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;


public class JForth
{
    public enum MODE {EDIT, DIRECT}

    public static final String ENCODING = "ISO_8859_1";
    public static final Long TRUE = 1L;
    public static final Long FALSE = 0L;
    // --Commented out by Inspection (3/25/2017 10:54 AM):private static final String ANSI_CLS = "\u001b[2J";
    private static final String ANSI_BOLD = "\u001b[1m";
    private static final String ANSI_YELLOW = "\u001b[33m";
    private static final String ANSI_NORMAL = "\u001b[0m";
    // --Commented out by Inspection (3/25/2017 10:54 AM):private static final String ANSI_WHITEONBLUE = "\u001b[37;44m";
    private static final String ANSI_ERROR = "\u001b[93;41m";
    private static final String FORTHPROMPT = "\nJFORTH> ";
    private static final String EDITORPROMPT = "\nEdit> ";
    private static final String OK = " OK";
    private static final int HISTORY_LENGTH = 1000;
    private static Voice voice = null;
    public final History history;
    public final WordsList dictionary = new WordsList();
    private final OStack dStack = new OStack();
    private final OStack vStack = new OStack();
    public MODE mode = MODE.DIRECT;
    public final transient PrintStream _out; // output channel
    public boolean compiling;
    public int base;
    public NonPrimitiveWord wordBeingDefined = null;
    public BaseWord currentWord;
    public final LineEdit _lineEditor;
    private MultiDotStreamTokenizer st = null;

    public JForth ()
    {
        this (System.out);
    }

    public JForth (PrintStream out)
    {
        compiling = false;
        base = 10;
        history = new History(HISTORY_LENGTH);
        _out = out;
        new PredefinedWords(this, dictionary);
        _lineEditor = new LineEdit(out);
    }

    /**
     * Call speech output
     * @param txt Text to speak
     */
    public static void speak(String txt)
    {
        if (voice == null)
        {
            KevinVoiceDirectory dir = new KevinVoiceDirectory();
            //AlanVoiceDirectory dir = new AlanVoiceDirectory();
            voice = dir.getVoices()[0];
            voice.allocate();
        }
        voice.speak(txt);
    }

    /**
     * Starting point
     * @param args not used
     */
    public static void main (String[] args)
    {
        AnsiConsole.systemInstall();
        JForth forth = new JForth(AnsiConsole.out);
        forth.mainLoop();
    }

    /**
     * Execute one line and generate output
     * @param input String containing forth commands
     */
    public void singleShot (String input)
    {
        input = StringEscape.escape (input);
        if (mode == MODE.DIRECT)
        {
            if (!interpretLine(input))
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
                history.add(input);
                _out.print(OK);
            }
        }
        else // mode == EDIT
        {
            if (!_lineEditor.handleLine(input))
            {
                mode = MODE.DIRECT;
            }
        }
        if (mode == MODE.DIRECT)
        {
            _out.print (FORTHPROMPT);
        }
        else
        {
            _out.print (EDITORPROMPT);
        }
        _out.flush();
    }

    /**
     * Main loop
     */
    private void mainLoop ()
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
            // execution error, file not found
        }
        while (true)
        {
            String s = scanner.nextLine().trim();
            singleShot (s);
        }
    }

    /**
     * Make human-readable String from object
     * @param o input object
     * @return String
     */
    public String makePrintable (Object o)
    {
        if (o == null)
            return null;
        if (o instanceof Long)
        {
            return Long.toString((Long) o, base).toUpperCase();
        }
        else if (o instanceof Double)
        {
            return Utilities.formatDouble((Double) o);
        }
        else if (o instanceof Complex)
        {
            return Utilities.formatComplex((Complex) o);
        }
        else if (o instanceof Fraction)
        {
            return Utilities.formatFraction((Fraction) o);
        }
        else if (o instanceof String)
        {
            return StringEscape.unescape((String) o);
        }
        else if (o instanceof PolynomialFunction)
        {
            return PolySupport.formatPoly((PolynomialFunction) o);
        }
        else if (o instanceof BigInteger)
        {
            return o.toString();
        }
        else
        {
            return o.toString();
        }
    }


    public String ObjectToString (Object o)
    {
        String out = makePrintable(o);
        if (_out == AnsiConsole.out)
            return ANSI_YELLOW + ANSI_BOLD + out + ANSI_NORMAL;
        return out;
    }

    /**
     * Run the history
     */
    public void play ()
    {
        for (String s : history.history)
        {
            if (!interpretLine(s))
            {
                dStack.removeAllElements();
            }
            _out.print (FORTHPROMPT);
            _out.flush();
        }
    }

    /**
     * Run a single line of FORTH statements
     * @param text The line
     * @return false if an error occured
     */
    public boolean interpretLine (String text)
    {
        try
        {
            StringReader sr = new StringReader(text);
            st = new MultiDotStreamTokenizer(sr);
            st.resetSyntax();
            st.wordChars('!', '~');
            //st.quoteChar('_');  // test
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
                    if (!doInterpret(word))
                    {
                        return false;
                    }
                }
                else
                {
                    if (!doCompile(word))
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
            //e.printStackTrace();
            return false;
        }
    }

    private boolean doInterpret (String word) throws Exception
    {
        word = handleDirectStringOut(word, false);
        BaseWord bw = dictionary.search(word);
        if (bw != null)
        {
            if (bw instanceof NonPrimitiveWord)
            {
                currentWord = bw;  // Save for recursion
            }
            return bw.execute(dStack, vStack) != 0;
        }
        Long num = Utilities.parseLong(word, base);
        if (num != null)
        {
            dStack.push(num);
            return true;
        }
        BigInteger big = Utilities.parseBigInt(word, base);
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
        dStack.push (word); // as String
        return true;
        //_out.print(word + " ?");
        //return false;
    }

    private String handleDirectStringOut (String word, boolean compile) throws Exception
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
                    sb.append(word2, 0, word2.length() - 1);
                    break;
                }
                sb.append(word2).append(' ');
            }
            if (compile)
                wordBeingDefined.addWord(new Literal(sb.toString()));
            else
                dStack.push(sb.toString());
            return ".";
        }
        return word;
    }

    private boolean doCompile (String word) throws Exception
    {
        word = handleDirectStringOut(word, true);
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
            wordBeingDefined.addWord(new Literal(num));
            return true;
        }
        BigInteger big = Utilities.parseBigInt(word, base);
        if (big != null)
        {
            wordBeingDefined.addWord(new Literal(big));
            return true;
        }
        Double dnum = Utilities.parseDouble(word, base);
        if (dnum != null)
        {
            wordBeingDefined.addWord(new Literal(dnum));
            return true;
        }
        DoubleMatrix ma = DoubleMatrix.parseMatrix(word, base);
        if (ma != null)
        {
            wordBeingDefined.addWord(new Literal(ma));
            return true;
        }
        DoubleSequence ds = DoubleSequence.parseSequence(word, base);
        if (ds != null)
        {
            wordBeingDefined.addWord(new Literal(ds));
            return true;
        }
        Fraction fr = Utilities.parseFraction(word, base);
        if (fr != null)
        {
            wordBeingDefined.addWord(new Literal(fr));
            return true;
        }
        Complex cpl = Utilities.parseComplex(word, base);
        if (cpl != null)
        {
            wordBeingDefined.addWord(new Literal(cpl));
            return true;
        }
        String ws = Utilities.parseString(word);
        if (ws != null)
        {
            wordBeingDefined.addWord(new Literal(ws));
            return true;
        }
        double[] pd = PolynomialParser.parsePolynomial(word, base);
        if (pd != null)
        {
            wordBeingDefined.addWord(
                    new Literal(
                            new PolynomialFunction(pd)));
            return true;
        }
        _out.print(word + " ?");
        compiling = false;
        return false;
    }

    public String getNextToken ()
    {
        try
        {
            if (st.nextToken() != StreamTokenizer.TT_EOF)
            {
                return st.sval;
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return null;
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
