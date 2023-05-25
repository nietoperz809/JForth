package jforth;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory;
import guishell.JfTerminalPanel;
import jforth.forthwords.PredefinedWords;
import jforth.seq.*;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import tools.FileUtils;
import tools.Func;
import tools.Utilities;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.BiConsumer;

public class JForth {
    public final RuntimeEnvironment CurrentEnvironment;
    public enum MODE {EDIT, DIRECT}
    public long LastETime;
    public final long StartTime;
    public final Random random = new Random();
    public HashMap<String, Object> globalMap = new HashMap<>();
    public Exception LastError = null;
    public static final Charset ENCODING = StandardCharsets.ISO_8859_1;
    public static final Long TRUE = 1L;
    public static final Long FALSE = 0L;
    private static final String FORTHPROMPT = "\nJFORTH> ";
    private static final String EDITORPROMPT = "\nEdit> ";
    private static final String OK = " OK";
    private static Voice voice = null;
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
    private MultiDotStreamTokenizer tokenizer = null;
    public JfTerminalPanel guiTerminal;

    public JForth (RuntimeEnvironment ri) {
        this(System.out, ri);
    }

    public JForth (PrintStream out, RuntimeEnvironment ri, JfTerminalPanel ta) {
        this (out, ri);
        guiTerminal = ta;
    }

    public JForth (PrintStream out, RuntimeEnvironment ri) {
        StartTime = System.currentTimeMillis();
        CurrentEnvironment = ri;
        compiling = false;
        base = 10;
        _out = out;
        new PredefinedWords(this, dictionary);
        _lineEditor = new LineEdit(out, this);
    }

    public String getMapContent () {
        StringBuilder sb = new StringBuilder();
        @SuppressWarnings("unchecked")
        HashMap<String, Object> map = (HashMap<String, Object>) globalMap.clone();
        map.forEach((key, value) -> sb.append(key)
                .append(" --> ")
                .append(makePrintable(value))
                .append('\n'));
        return sb.toString();
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
     * Helper function to split a line in single forth statements
     * @param lineData  The line
     * @param con Function to handle the statements, will be called for each statement
     */
    public static void runCommands1By1 (String lineData, BiConsumer<String[], Integer> con) {
        if (lineData.isEmpty())
            return;
        if (lineData.endsWith("\"\n") && lineData.startsWith("\"")) // quoted string
        {
            con.accept (new String[]{lineData.substring(0, lineData.length()-1)}, 0);
            return;
        }
        // Generate multiple inputs from single line
        String[] arr = lineData.split("\\s+");
        if (arr.length == 0) {
            arr = new String[]{"\n"};
        } else if (arr[0].equals(":") && arr[arr.length - 1].equals(";")) {
            arr = new String[]{lineData};
        }
        for (int n = 0; n < arr.length; n++) {
            con.accept(arr, n);
        }
    }

    /**
     * Execute one line and generate output
     *
     * @param input String containing forth commands
     */
    public boolean singleShot(String input) {
        boolean res = true;
        input = Utilities.replaceUmlauts(input);
        input = StringEscape.escape(input);
        if (mode == MODE.DIRECT) {
            if (!interpretLine(input)) {
                _out.print(input + " word execution or stack error");
                dStack.removeAllElements();
                res = false;
            } else {
                //if (this.CurrentEnvironment != RuntimeEnvironment.GUITERMINAL)
                   _out.print(OK);
            }
        } else { // mode == EDIT
            try {
                if (!_lineEditor.handleLine(input)) {
                    mode = MODE.DIRECT;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (mode == MODE.DIRECT) {
            _out.print(FORTHPROMPT);
        } else {
            _out.print(EDITORPROMPT);
        }
        _out.flush();
        return res;
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

    /**
     * Run a single line of FORTH statements
     *
     * @param text The line
     * @return false if an error occured
     */
    public boolean interpretLine(String text) {
        try {
            StringReader sr = new StringReader(text);
            tokenizer = new MultiDotStreamTokenizer(sr);
            tokenizer.resetSyntax();
            tokenizer.wordChars('!', '~');
            //st.quoteChar('_');  // test
            tokenizer.whitespaceChars('\u0000', '\u0020');
            //tokenizer.whitespaceChars('\u0020', '\u0020');
            int ttype = tokenizer.nextToken();
            while (ttype != StreamTokenizer.TT_EOF) {
                String word = tokenizer.sval;
                if (word == null)
                    return true;
                if (word.equals("//"))   // Comment until line end
                {
                    return true;
                }
                if (word.equals("(")) // filter out comments
                {
                    for (; ; ) {
                        tokenizer.nextToken();
                        String word2 = tokenizer.sval;
                        if (word2.endsWith(")")) {
                            break;
                        }
                    }
                    tokenizer.nextToken();
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
                ttype = tokenizer.nextToken();
            }
            return true;
        } catch (Exception e) {
            setLastError(e);
            return false;
        }
    }

    private String handleDirectStringOut(String word, boolean compile) throws Exception {
        if (word.equals(".\"")) {
            StringBuilder sb = new StringBuilder();
            for (; ; ) {
                tokenizer.nextToken();
                String word2 = tokenizer.sval;
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

    public static boolean doForKnownWordsUnmixed(String input, Func<Object, Object> action, int base) {
        Long num = Utilities.parseLong(input, base);
        if (num != null) {
            action.apply(num);
            return true;
        }
        BigInteger big = Utilities.parseBigInt(input, base);
        if (big != null) {
            action.apply(big);
            return true;
        }
        Double dnum = Utilities.parseDouble(input, base);
        if (dnum != null) {
            action.apply(dnum);
            return true;
        }
        Complex co = Utilities.parseComplex(input, base);
        if (co != null) {
            action.apply(co);
            return true;
        }
        Fraction fr = Utilities.parseFraction(input, base);
        if (fr != null) {
            action.apply(fr);
            return true;
        }
        DoubleMatrix ma = DoubleMatrix.parseMatrix(input, base);
        if (ma != null) {
            action.apply(ma);
            return true;
        }
        DoubleSequence lo = DoubleSequence.parseSequence(input, base);
        if (lo != null) {
            action.apply(lo);
            return true;
        }
        FracSequence fs = FracSequence.parseSequence(input);
        if (fs != null) {
            action.apply(fs);
            return true;
        }
        StringSequence ss = StringSequence.parseSequence(input);
        if (ss != null) {
            action.apply(ss);
            return true;
        }
        String ws = Utilities.parseString(input);
        if (ws != null) {
            action.apply(ws);
            return true;
        }
        double[] pd = PolynomialParser.parsePolynomial(input, base);
        if (pd != null) {
            PolynomialFunction plf = new PolynomialFunction(pd);
            action.apply(plf);
            return true;
        }
        return false;
    }
    /**
     * Interpret or compile known words
     *
     * @param input      the word
     * @param action function to be applied
     * @return true if word is known
     *
     * Made static so it can be used frome elsewhere
     */


    public static boolean doForKnownWords(String input, Func<Object, Object> action, int base) {
        // should be last?
        MixedSequence mx = MixedSequence.parseSequence (input);
        if (mx != null) {
            action.apply(mx);
            return true;
        }
        return doForKnownWordsUnmixed(input, action, base);
    }

    private void setLastError(Exception e)
    {
        LastError = e;
        LastETime = System.currentTimeMillis();
    }

    private boolean doInterpret(String word) throws Exception {
        word = handleDirectStringOut(word, false);
        BaseWord bw = dictionary.search(word);
        if (bw != null) {
            if (bw instanceof NonPrimitiveWord) {
                currentWord = bw;  // Save for recursion
            }
            boolean ret = bw.apply(dStack, vStack) != 0;
            if (!ret)
                setLastError(new Exception("failed execution of '"+word+"'"));
            return ret;
        }
        boolean ret = doForKnownWords(word, dStack::push, base);
        if (ret) {
            return true;
        }
        dStack.push(word); // as String if word isn't known
        return true;
    }

    boolean recflag = false;

    private boolean doCompile(String word) throws Exception {
        word = handleDirectStringOut(word, true);
        BaseWord bw;
        if (word.equalsIgnoreCase("recursive"))
            recflag = true;
        if (recflag && currentWord != null && word.equalsIgnoreCase(currentWord.name)) {
            bw = dictionary.search("recurse");
            recflag = false;
        } else {
            bw = dictionary.search(word);
        }
        if (bw != null) {
            if (bw.immediate) {
                bw.apply(dStack, vStack);
            } else {
                wordBeingDefined.addWord(bw);
            }
            return true;
        }
        boolean ret = doForKnownWords(word,
                o -> wordBeingDefined.addWord(new Literal(o)),
                base);
        if (ret) {
            return true;
        }
        _out.print(word + " ?");
        compiling = false;
        return false;
    }

    public String getNextToken() {
        try {
            if (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                return tokenizer.sval;
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
