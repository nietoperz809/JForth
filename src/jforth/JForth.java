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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class JForth {
    public final RuntimeEnvironment CurrentEnvironment;

    public enum MODE {EDIT, DIRECT}

    public static final Charset ENCODING = StandardCharsets.ISO_8859_1;
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
    public final LSystem _lsys = new LSystem();
    private MultiDotStreamTokenizer st = null;

    public JForth(RuntimeEnvironment ri) {
        this(System.out, ri);
    }

    public JForth(PrintStream out, RuntimeEnvironment ri) {
        CurrentEnvironment = ri;
        compiling = false;
        base = 10;
        history = new History(HISTORY_LENGTH);
        _out = out;
        new PredefinedWords(this, dictionary);
        _lineEditor = new LineEdit(out, this);
    }

    /**
     * Call speech output
     *
     * @param txt Text to speak
     */
    public static void speak(String txt) {
        if (voice == null) {
            KevinVoiceDirectory dir = new KevinVoiceDirectory();
            //AlanVoiceDirectory dir = new AlanVoiceDirectory();
            voice = dir.getVoices()[0];
            voice.allocate();
        }
        voice.speak(txt);
    }

    /**
     * Starting point
     *
     * @param args not used
     */
    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        JForth jf;
        while (true) // Restart the interpreter on memory errors
        {
            jf = new JForth(AnsiConsole.out, RuntimeEnvironment.CONSOLE);
            try {
                jf.mainLoop();
                break;
            } catch (OutOfMemoryError ex) {
                jf._out.println(ANSI_ERROR + "Memory Error: " + ex.getMessage());
                jf._out.println(ANSI_ERROR + "RESET!");
            }
        }
    }

    /**
     * Execute one line and generate output
     *
     * @param input String containing forth commands
     */
    public void singleShot(String input) {
        input = StringEscape.escape(input);
        if (mode == MODE.DIRECT) {
            if (!interpretLine(input)) {
                if (_out == AnsiConsole.out) {
                    _out.print(input + " - " + ANSI_ERROR +
                            " word execution or stack error " +
                            ANSI_NORMAL);
                } else {
                    _out.print(input +
                            " word execution or stack error");
                }
                dStack.removeAllElements();
            } else {
                history.add(input);
                _out.print(OK);
            }
        } else // mode == EDIT
        {
            if (!_lineEditor.handleLine(input)) {
                mode = MODE.DIRECT;
            }
        }
        if (mode == MODE.DIRECT) {
            _out.print(FORTHPROMPT);
        } else {
            _out.print(EDITORPROMPT);
        }
        _out.flush();
    }

    /**
     * Main loop
     */
    private void mainLoop() {
        dStack.removeAllElements();
        Scanner scanner = new Scanner(System.in);
        _out.println(Utilities.buildInfo);
        singleShot("\n"); // to show prompt immediately
        try {
            executeFile("autoexec.4th");
        } catch (Exception unused) {
            // execution error, file not found
        }
        while (true) {
            String s = scanner.nextLine().trim();
            singleShot(s);
        }
    }

    /**
     * Make human-readable String from object
     *
     * @param o input object
     * @return String
     */
    public String makePrintable(Object o) {
        return Utilities.makePrintable(o, base);
    }

    public String ObjectToString(Object o) {
        String out = makePrintable(o);
        if (_out == AnsiConsole.out) {
            return ANSI_YELLOW + ANSI_BOLD + out + ANSI_NORMAL;
        }
        return out;
    }

    /**
     * Run the history
     */
    public void play() {
        for (String s : history.history) {
            if (!interpretLine(s)) {
                dStack.removeAllElements();
            }
            _out.print(FORTHPROMPT);
            _out.flush();
        }
    }

    /**
     * Run a single line of FORTH statements
     *
     * @param text The line
     * @return false if an error occured
     */
    public boolean interpretLine(String text) {
        try {
            StringReader sr = new StringReader(text);
            st = new MultiDotStreamTokenizer(sr);
            st.resetSyntax();
            st.wordChars('!', '~');
            //st.quoteChar('_');  // test
            st.whitespaceChars('\u0000', '\u0020');
            int ttype = st.nextToken();
            while (ttype != StreamTokenizer.TT_EOF) {
                String word = st.sval;
                if (word == null)
                    return true;
                if (word.equals("\\"))   // Comment until line end
                {
                    return true;
                }
                if (word.equals("(")) // filter out comments
                {
                    for (; ; ) {
                        st.nextToken();
                        String word2 = st.sval;
                        if (word2.endsWith(")")) {
                            break;
                        }
                    }
                    st.nextToken();
                    continue;
                }
                if (!compiling) {
                    if (!doInterpret(word)) {
                        return false;
                    }
                } else {
                    if (!doCompile(word)) {
                        return false;
                    }
                }
                ttype = st.nextToken();
            }
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

    private String handleDirectStringOut(String word, boolean compile) throws Exception {
        if (word.equals(".\"")) {
            StringBuilder sb = new StringBuilder();
            for (; ; ) {
                st.nextToken();
                String word2 = st.sval;
                if (word2.endsWith("\"")) {
                    sb.append(word2, 0, word2.length() - 1);
                    break;
                }
                sb.append(word2).append(' ');
            }
            if (compile) {
                wordBeingDefined.addWord(new Literal(sb.toString()));
            } else {
                dStack.push(sb.toString());
            }
            return ".";
        }
        return word;
    }

    /**
     * Interpret or compile known words
     *
     * @param word      the word
     * @param interpret true if direct mode, false if compile mode
     * @return true if word is known
     */
    private boolean doForKnownWords(String word, boolean interpret) {
        Long num = Utilities.parseLong(word, base);
        if (num != null) {
            if (interpret) {
                dStack.push(num);
            } else {
                wordBeingDefined.addWord(new Literal(num));
            }
            return true;
        }
        BigInteger big = Utilities.parseBigInt(word, base);
        if (big != null) {
            if (interpret) {
                dStack.push(big);
            } else {
                wordBeingDefined.addWord(new Literal(big));
            }
            return true;
        }
        Double dnum = Utilities.parseDouble(word, base);
        if (dnum != null) {
            if (interpret) {
                dStack.push(dnum);
            } else {
                wordBeingDefined.addWord(new Literal(dnum));
            }
            return true;
        }
        Complex co = Utilities.parseComplex(word, base);
        if (co != null) {
            if (interpret) {
                dStack.push(co);
            } else {
                wordBeingDefined.addWord(new Literal(co));
            }
            return true;
        }
        Tuple tp = Tuple.parseTuple(word);
        if (tp != null)  {
            if (interpret) {
                dStack.push(tp);
            } else {
                wordBeingDefined.addWord(new Literal(tp));
            }
            return true;
        }
        Fraction fr = Utilities.parseFraction(word, base);
        if (fr != null) {
            if (interpret) {
                dStack.push(fr);
            } else {
                wordBeingDefined.addWord(new Literal(fr));
            }
            return true;
        }
        DoubleMatrix ma = DoubleMatrix.parseMatrix(word, base);
        if (ma != null) {
            if (interpret) {
                dStack.push(ma);
            } else {
                wordBeingDefined.addWord(new Literal(ma));
            }
            return true;
        }
        DoubleSequence lo = DoubleSequence.parseSequence(word, base);
        if (lo != null) {
            if (interpret) {
                dStack.push(lo);
            } else {
                wordBeingDefined.addWord(new Literal(lo));
            }
            return true;
        }
        StringSequence ss = StringSequence.parseSequence(word);
        if (ss != null) {
            if (interpret) {
                dStack.push(ss);
            } else {
                wordBeingDefined.addWord(new Literal(ss));
            }
            return true;
        }
        String ws = Utilities.parseString(word);
        if (ws != null) {
            if (interpret) {
                dStack.push(ws);
            } else {
                wordBeingDefined.addWord(new Literal(ws));
            }
            return true;
        }
        double[] pd = PolynomialParser.parsePolynomial(word, base);
        if (pd != null) {
            if (interpret) {
                dStack.push(new PolynomialFunction(pd));
            } else {
                wordBeingDefined.addWord(
                        new Literal(
                                new PolynomialFunction(pd)));
            }
            return true;
        }
        return false;
    }

    private boolean doInterpret(String word) throws Exception {
        word = handleDirectStringOut(word, false);
        BaseWord bw = dictionary.search(word);
        if (bw != null) {
            if (bw instanceof NonPrimitiveWord) {
                currentWord = bw;  // Save for recursion
            }
            return bw.execute(dStack, vStack) != 0;
        }
        boolean ret = doForKnownWords(word, true);
        if (ret) {
            return true;
        }
        dStack.push(word); // as String if word isn't known
        return true;
    }

    private boolean doCompile(String word) throws Exception {
        word = handleDirectStringOut(word, true);
        BaseWord bw;
        if (currentWord != null && word.equalsIgnoreCase(currentWord.name)) {
            bw = dictionary.search("recurse");
        } else {
            bw = dictionary.search(word);
        }
        if (bw != null) {
            if (bw.immediate) {
                bw.execute(dStack, vStack);
            } else {
                wordBeingDefined.addWord(bw);
            }
            return true;
        }
        boolean ret = doForKnownWords(word, false);
        if (ret) {
            return true;
        }
        _out.print(word + " ?");
        compiling = false;
        return false;
    }

    public String getNextToken() {
        try {
            if (st.nextToken() != StreamTokenizer.TT_EOF) {
                return st.sval;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    public void executeFile(ArrayList<String> as, boolean crlf) {
        for (String s : as) {
            interpretLine(s);
            if (crlf) {
                _out.println();
            }
        }
    }

    public void executeFile(String fileName) throws Exception {
        executeFile(FileUtils.loadStrings(fileName), false);
    }
}
