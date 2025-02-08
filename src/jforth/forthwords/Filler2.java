package jforth.forthwords;

import jforth.*;
import jforth.ControlWords.*;
import jforth.audio.DtmfMorsePlayer;
import jforth.audio.MusicTones;
import jforth.audio.SynthToneBase;
import jforth.audio.WaveTools;
import jforth.seq.*;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.mathIT.util.FunctionParser;
import tools.*;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.zip.CRC32;

import static java.lang.System.currentTimeMillis;
import static jforth.audio.DtmfMorsePlayer.*;
import static jforth.audio.MusicTones.putSongIntoMemory;
import static org.apache.commons.math3.complex.ComplexUtils.polar2Complex;
import static org.mathIT.numbers.Numbers.exactBinomial;
import static tools.Utilities.humanReadableByteCountBin;
import static tools.Utilities.humanReadableByteCountSI;

final class Filler2 {
    static void fill(WordsList _fw, PredefinedWords predefinedWords) {
        LSystem lSys = predefinedWords._jforth._lsys;

        _fw.add(new PrimitiveWord
                (
                        "f2=", "evaluate term with 2 arguments",
                        (dStack, vStack) ->
                        {
                            try {
                                double y = Utilities.readDouble(dStack);
                                double x = Utilities.readDouble(dStack);
                                String f = Utilities.readString(dStack);
                                FunctionParser fp = new FunctionParser(f);
                                dStack.push(fp.evaluate(0, x, y));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "f=", "evaluate term",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();   // Argument
                            String f = Utilities.readString(dStack); // function
                            FunctionParser fp = new FunctionParser(f);
                            try {
                                double d = Utilities.getDouble(o);
                                dStack.push(fp.evaluate(0, d));
                                return 1;
                            } catch (Exception ex) {
                                if (o instanceof DoubleSequence) {
                                    DoubleSequence ds = (DoubleSequence) o;
                                    ArrayList<Double> list = new ArrayList<>();
                                    for (double d : ds.asPrimitiveArray()) {
                                        list.add(fp.evaluate(0, d));
                                    }
                                    dStack.push(new DoubleSequence(list));
                                    return 1;
                                }
                                if (o instanceof FracSequence) {
                                    FracSequence fr = (FracSequence) o;
                                    FracSequence res = new FracSequence();
                                    for (Fraction fra : fr.get_list()) {
                                        res.add(new Fraction(fp.evaluate(0, fra.doubleValue())));
                                    }
                                    dStack.push(res);
                                    return 1;
                                }
                                if (o instanceof DoubleMatrix) {
                                    DoubleMatrix m = (DoubleMatrix) o;
                                    if (m.getColumnDimension() != 2)
                                        return 0;
                                    ArrayList<Double> list = new ArrayList<>();
                                    for (int s = 0; s < m.getRowDimension(); s++) {
                                        double[] c = m.getRow(s);
                                        list.add(fp.evaluate(0, c[0], c[1]));
                                    }
                                    if (list.size() == 1)
                                        dStack.push(list.get(0));
                                    else
                                        dStack.push(new DoubleSequence(list));
                                    return 1;
                                }
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsGet", "get LSystem result",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = lSys.doIt();
                                dStack.push(ss);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsRep", "run LSystem on previous result ",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = lSys.doNext();
                                dStack.push(ss);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsPut", "Set Matrial for LSystem",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = Utilities.readString(dStack);
                                lSys.setMaterial(ss);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsSys", "Set and run complete LSystem",
                        (dStack, vStack) ->
                        {
                            try {
                                StringSequence ss = Utilities.readStringSequence(dStack);
                                String res = lSys.setAndRunFullSystem(ss.get_list());
                                dStack.push(res);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsSys2", "Set and run complete LSystem",
                        (dStack, vStack) ->
                        {
                            try {
                                StringSequence ss = Utilities.readStringSequence(dStack);
                                String res = lSys.setAndRunFullSystem2(ss.get_list());
                                dStack.push(res);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsRule", "Set Rule for LSystem",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = Utilities.readString(dStack);
                                lSys.putRule(ss);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsClr", "Remove all rules",
                        (dStack, vStack) ->
                        {
                            try {
                                lSys.clrRules();
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "LsInfo", "Get complete LSystem as String",
                        (dStack, vStack) ->
                        {
                            try {
                                String s = lSys.toString();
                                dStack.push(s);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "hexStr", "Make hex string",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            byte[] b = null;
                            if (o1 instanceof String) {
                                b = ((String) o1).getBytes(JForth.ENCODING);
                            } else if (o1 instanceof DoubleSequence) {
                                b = ((DoubleSequence) o1).asBytes();
                            }
                            if (b != null) {
                                dStack.push(Utilities.printHexBinary(b));
                                return 1;
                            }
                            if (o1 instanceof Long) {
                                dStack.push(Long.toHexString((Long) o1));
                                return 1;
                            }
                            if (o1 instanceof BigInteger) {
                                BigInteger bi = (BigInteger) o1;
                                dStack.push(bi.toString(16));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "binStr", "Make binary String",
                        (dStack, vStack) ->
                        {
                            try {
                                long ll = Utilities.readLong(dStack);
                                String ss = Long.toBinaryString(ll);
                                dStack.push(ss);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unhexStr", "Make Hexstr to Bytes",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = Utilities.readString(dStack);
                                byte[] b = Utilities.parseHexBinary(ss);
                                dStack.push(new DoubleSequence(b));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "seq", "generate sequence",
                        (dStack, vStack) ->
                        {
                            try {
                                double step, start;
                                int howmuch;
                                Object o = dStack.pop();
                                if (o instanceof String) {
                                    StringSequence seq = new StringSequence(((String)o).toCharArray());
                                    dStack.push(seq);
                                    return 1;
                                }
                                if (o instanceof DoubleSequence) {
                                    DoubleSequence ds = (DoubleSequence)o;
                                    start = ds.pick(0);
                                    howmuch = (int)(double)(ds.pick(1));
                                    step = ds.pick(2);
                                } else {
                                    step = Utilities.getDouble(o);
                                    howmuch = (int) Utilities.readLong(dStack);
                                    start = Utilities.readDouble(dStack);
                                }
                                DoubleSequence ds = DoubleSequence.makeCounted(start, howmuch, step);
                                dStack.push(ds);
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "leven", "levenshtein distance of 2 strings",
                        (dStack, vStack) ->
                        {
                            try {
                                String s1 = Utilities.readString(dStack);
                                String s2 = Utilities.readString(dStack);
                                dStack.push(Utilities.levenshteinDistance(s1, s2));
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gray", "make gray code",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (o instanceof Long) {
                                byte b = (byte) (long) (Long) o;
                                dStack.push((long) Utilities.grayByte(b));
                                return 1;
                            }
                            if (o instanceof String) {
                                String input = (String) o;
                                byte[] inbytes = input.getBytes(JForth.ENCODING);
                                for (int s = 0; s < inbytes.length; s++)
                                    inbytes[s] = Utilities.grayByte(inbytes[s]);
                                dStack.push(new String(inbytes, JForth.ENCODING));
                                return 1;
                            }
                            if (o instanceof DoubleSequence) {
                                DoubleSequence input = (DoubleSequence) o;
                                byte[] inbytes = input.asBytes();
                                for (int s = 0; s < inbytes.length; s++)
                                    inbytes[s] = Utilities.grayByte(inbytes[s]);
                                dStack.push(new DoubleSequence(inbytes));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ungray", "reverse gray code",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (o instanceof Long) {
                                byte b = (byte) (long) (Long) o;
                                dStack.push((long) Utilities.ungrayByte(b));
                                return 1;
                            }
                            if (o instanceof String) {
                                String input = (String) o;
                                byte[] inbytes = input.getBytes(JForth.ENCODING);
                                for (int s = 0; s < inbytes.length; s++)
                                    inbytes[s] = Utilities.ungrayByte(inbytes[s]);
                                dStack.push(new String(inbytes, JForth.ENCODING));
                                return 1;
                            }
                            if (o instanceof DoubleSequence) {
                                DoubleSequence input = (DoubleSequence) o;
                                byte[] inbytes = input.asBytes();
                                for (int s = 0; s < inbytes.length; s++)
                                    inbytes[s] = Utilities.ungrayByte(inbytes[s]);
                                dStack.push(new DoubleSequence(inbytes));
                                return 1;
                            }
                            return 0;
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "hash", "generate hash string",
                        (dStack, vStack) ->
                        {
                            try {
                                String hashtype = Utilities.readString(dStack).toLowerCase();
                                Object o1 = dStack.pop();
                                byte[] inbytes;
                                if (o1 instanceof String)
                                    inbytes = ((String) o1).getBytes(JForth.ENCODING);
                                else
                                    inbytes = Utilities.convertToBytes(o1);
                                if (hashtype.equals("crc32")) {
                                    CRC32 crc = new CRC32();
                                    crc.update(inbytes, 0, inbytes.length);
                                    dStack.push(crc.getValue());
                                } else if (hashtype.equals("crc16")) {
                                    CRC16 crc = new CRC16();
                                    crc.calc(inbytes);
                                    dStack.push((long) crc.getCRC());
                                } else {
                                    MessageDigest md = MessageDigest.getInstance(hashtype);
                                    dStack.push(new DoubleSequence(md.digest(inbytes)));
                                }
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "b64", "make Base64 from String",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = Utilities.readString(dStack);
                                byte[] bytes = ss.getBytes(JForth.ENCODING);
                                String encoded = Base64.getEncoder().encodeToString(bytes);
                                dStack.push(encoded);
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unb64", "make String from Base64",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = Utilities.readString(dStack);
                                byte[] decoded = Base64.getDecoder().decode(ss);
                                String b2 = new String(decoded, JForth.ENCODING);
                                dStack.push(b2);
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "urlEnc", "URL encode a string",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = Utilities.readString(dStack);
                                String encoded = URLEncoder.encode(ss, JForth.ENCODING.name());
                                dStack.push(encoded);
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "urlDec", "Decode URL encoded string",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = Utilities.readString(dStack);
                                String encoded = URLDecoder.decode(ss, JForth.ENCODING.name());
                                dStack.push(encoded);
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "psp", "Push space on stack",
                        (dStack, vStack) ->
                        {
                            try {
                                dStack.push(" ");
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "say", "speak a string",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = Utilities.readString(dStack);
                                JForth.speak(ss);
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "js", "evaluate js expression string",
                        (dStack, vStack) ->
                        {
                            try {
                                String ss = Utilities.readString(dStack);
                                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
                                Object o = engine.eval(ss);
                                dStack.push(o);
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "java", "compile and run java class",
                        (dStack, vStack) ->
                        {
                            try {
                                String source = Utilities.readString(dStack);
                                String classname = "Solution";
                                source = "public class " + classname + " {" + source + "}";
                                Object arg = dStack.pop();
                                JavaExecutor compiler = new JavaExecutor();
                                final Method greeting = compiler.compileStaticMethod("main", classname, source);
                                final Object result = greeting.invoke(null, arg);
                                dStack.push(result);
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "forth", "execute forth line asynchronously",
                        (dStack, vStack) ->
                        {
                            try {
                                final String ss = Utilities.readString(dStack);
                                new Thread(() ->
                                {
                                    StringStream _stream = new StringStream();
                                    JForth f = new JForth(_stream.getPrintStream(),
                                            RuntimeEnvironment.EMBEDDED);
                                    f.interpretLine(ss);
                                    dStack.push(_stream.toString());
                                }).start();
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "execute", "executes word from stack",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (o instanceof BaseWord) {
                                BaseWord bw = (BaseWord) o;
                                return bw.apply(dStack, vStack);
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "if", true,
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling) {
                                //return 1;
                                predefinedWords._jforth.wordBeingDefined = new NonPrimitiveWord("directfor");
                            }
                            int currentIndex = predefinedWords._jforth.wordBeingDefined.getNextWordIndex();
                            IfControlWord ifcw = new IfControlWord(currentIndex);
                            predefinedWords._jforth.wordBeingDefined.addWord(ifcw);
                            vStack.push(ifcw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "then", true,
                        (dStack, vStack) ->
                        {
//                            if (!predefinedWords._jforth.compiling) {
//                                return 1;
//                            }
                            Object o = vStack.pop();
                            int thenIndex = predefinedWords._jforth.wordBeingDefined.getNextWordIndex();
                            if (o instanceof ElseControlWord) {
                                ((ElseControlWord) o).setThenIndexIncrement(thenIndex);
                                o = vStack.pop();
                            }
                            if (o instanceof IfControlWord) {
                                ((IfControlWord) o).setThenIndex(thenIndex);
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "else", true,
                        (dStack, vStack) ->
                        {
//                            if (!predefinedWords._jforth.compiling) {
//                                return 1;
//                            }
                            Object o = vStack.peek();
                            if (o instanceof IfControlWord) {
                                int elseIndex = predefinedWords._jforth.wordBeingDefined.getNextWordIndex() + 1;
                                ElseControlWord ecw = new ElseControlWord(elseIndex);
                                predefinedWords._jforth.wordBeingDefined.addWord(ecw);
                                vStack.push(ecw);
                                ((IfControlWord) o).setElseIndex(elseIndex);
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "do", true, "Sets up a finite loop, given index and limit.",
                        (dStack, vStack) ->
                        {
                            predefinedWords.createTemporaryImmediateWord();
                            DoLoopControlWord dlcw = new DoLoopControlWord();
                            predefinedWords._jforth.wordBeingDefined.addWord(dlcw);
                            int index = predefinedWords._jforth.wordBeingDefined.getNextWordIndex();
                            vStack.push((long) index);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "i", "put loop variable i on stack",
                        (dStack, vStack) ->
                        {
                            Object o = vStack.peek();
                            dStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "j", "put loop variable j on stack",
                        (dStack, vStack) ->
                        {
                            Object o1 = vStack.pop();
                            Object o2 = vStack.pop();
                            Object o3 = vStack.peek();
                            dStack.push(o3);
                            vStack.push(o2);
                            vStack.push(o1);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "leave", true, "Terminate the loop immediately",
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling) {
                                return 1;
                            }
                            LeaveLoopControlWord llcw = new LeaveLoopControlWord();
                            predefinedWords._jforth.wordBeingDefined.addWord(llcw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "loop", true, "repeat loop",
                        (dStack, vStack) ->
                                WordHelpers.addLoopWord(vStack, predefinedWords, LoopControlWord.class)
                ));

        _fw.add(new PrimitiveWord
                (
                        "+loop", true, "adds value to loop counter i",
                        (dStack, vStack) ->
                                WordHelpers.addLoopWord(vStack, predefinedWords, PlusLoopControlWord.class)
                ));

        _fw.add(new PrimitiveWord
                (
                        "begin", true, "Marks the start of an indefinite loop.",
                        (dStack, vStack) ->
                        {
                            predefinedWords.createTemporaryImmediateWord();
                            int index = predefinedWords._jforth.wordBeingDefined.getNextWordIndex();
                            vStack.push((long) index);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "until", true, "If false, go back to BEGIN. If true, terminate the loop",
                        (dStack, vStack) ->
                                WordHelpers.addLoopWord(vStack, predefinedWords, EndLoopControlWord.class)
                ));

        _fw.add(new PrimitiveWord
                (
                        "again", true, "Go back to BEGIN (infinite loop)",
                        (dStack, vStack) ->
                        {
                            try {
                                predefinedWords._jforth.wordBeingDefined.addWord(predefinedWords._wl.search("false"));
                            } catch (Exception e) {
                                return 0;
                            }
                            return WordHelpers.addLoopWord(vStack, predefinedWords, EndLoopControlWord.class);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "break", true, "Breaks out of the forth word",
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling) {
                                return 1;
                            }
                            BreakLoopControlWord ecw = new BreakLoopControlWord();
                            predefinedWords._jforth.wordBeingDefined.addWord(ecw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "clltz", "Get collatz sequence",
                        (dStack, vStack) ->
                        {
                            try {
                                long n = Utilities.readLong(dStack);
                                ArrayList<Double> ar = new ArrayList<>();
                                ar.add((double) n);
                                while (n != 1) {
                                    if (n % 2 == 0)
                                        n = n / 2;
                                    else
                                        n = 3 * n + 1;
                                    ar.add((double) n);
                                }
                                dStack.push(new DoubleSequence(ar));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ping", "Check a host",
                        (dStack, vStack) ->
                        {
                            try {
                                String host = Utilities.readString(dStack);
                                InetAddress inet = InetAddress.getByName(host);
                                if (inet.isReachable(5000))
                                    dStack.push(JForth.TRUE);
                                else
                                    dStack.push(JForth.FALSE);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "msg", "Show message box",
                        (dStack, vStack) ->
                        {
                            try {
                                String txt = Utilities.readString(dStack);
                                JOptionPane.showMessageDialog(null,
                                        txt,
                                        "JForth",
                                        JOptionPane.PLAIN_MESSAGE);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ask", "Show yes/no box",
                        (dStack, vStack) ->
                        {
                            try {
                                String txt = Utilities.readString(dStack);
                                JForth f = predefinedWords._jforth;
                                if (f.CurrentEnvironment == RuntimeEnvironment.WEBSERVER) {
                                    f._out.print("alertbox%%~" + txt);
                                    return 1;
                                }
                                int dialogResult = JOptionPane.showConfirmDialog(null,
                                        txt + "?",
                                        "Jforth", JOptionPane.YES_NO_OPTION);
                                if (dialogResult == 0)
                                    dStack.push(JForth.TRUE);
                                else
                                    dStack.push(JForth.FALSE);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "asyncmsg", "Show asynchronous message box",
                        (dStack, vStack) ->
                        {
                            try {
                                String txt = Utilities.readString(dStack);
                                new Thread(() ->
                                        JOptionPane.showMessageDialog(null,
                                                txt,
                                                "JForth",
                                                JOptionPane.PLAIN_MESSAGE)).start();
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "what", "Show description about a word",
                        (dStack, vStack) ->
                        {
                            try {
                                String wordname = Utilities.readString(dStack);
                                BaseWord bw = _fw.search(wordname); //dictionary.search(word);
                                if (bw == null)
                                    return 0;
                                String info = bw.getInfo();
                                dStack.push(info);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "collect", "collects all numbers or strings from stack into sequence",
                        (dStack, vStack) ->
                        {
                            try {
                                StringSequence sq = new StringSequence();
                                while (!dStack.isEmpty()) {
                                    sq.add(Utilities.makePrintable(dStack.pop(), 10));
                                }
                                try {
                                    DoubleSequence ds = new DoubleSequence(sq);
                                    dStack.push(ds.reverse());
                                    return 1;
                                } catch (NumberFormatException e) {
                                    try {
                                        FracSequence fs = new FracSequence(sq);
                                        dStack.push(fs.reverse());
                                        return 1;
                                    } catch (Exception exception) {
                                        //
                                    }
                                }
                                dStack.push(sq.reverse());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "scat", "desintegrate sequence onto stack",
                        (dStack, vStack) ->
                        {
                            try {
                                SequenceBase<?> sb = (SequenceBase<?>) dStack.pop();
                                for (Object d : sb.get_list()) {
                                    dStack.push(d);
                                }
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toTime", "make HH:MM:SS from TOS",
                        (dStack, vStack) ->
                        {
                            try {
                                Long v = Utilities.readLong(dStack);
                                dStack.push(Utilities.toTimeView(v));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toDate", "make Date from seconds on TOS",
                        (dStack, vStack) ->
                        {
                            try {
                                Long v = Utilities.readLong(dStack);
                                dStack.push(Utilities.toDateView(v));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toDateSS", "make Date from seconds on TOS as Stringsequence",
                        (dStack, vStack) ->
                        {
                            try {
                                Long v = Utilities.readLong(dStack);
                                String date = Utilities.toDateView(v);
                                String[] parts = date.split("[ ,]+");
                                StringSequence ret = new StringSequence(parts);
                                dStack.push(ret);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "soon", "run deferred word",
                        (dStack, vStack) ->
                        {
                            try {
                                final long delay = Utilities.readLong(dStack);
                                final Object o = dStack.pop();
                                if (o instanceof BaseWord) {
                                    final BaseWord bw = (BaseWord) o;

                                    new java.util.Timer().schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            bw.apply(dStack, vStack);
                                        }
                                    }, delay);
                                    return 1;
                                }
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "cls", "clear screen",
                        (dStack, vStack) ->
                        {
                            if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.GUITERMINAL) {
                                predefinedWords._jforth.guiTerminal.setText("");
                            } else {
                                predefinedWords._jforth._out.print("\u001b[2J");
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "nip", "same as swap+drop",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            dStack.pop();
                            dStack.push(o1);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "tune", "create music",
                        (dStack, vStack) ->
                        {
                            try {
                                String s1 = Utilities.readString(dStack);
                                if (s1.startsWith("+")) {
                                    s1 = s1.substring(1);
                                    byte[] wav = putSongIntoMemory(s1);
                                    dStack.push(new FileBlob(wav, "tune.wav"));
                                    return 1;
                                }
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.WEBSERVER) {
                                    MusicTones.sendSongtoBrowser(s1, predefinedWords);
                                    return 1;
                                }
                                MusicTones.playSong(s1);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "DTMF", "create DTMF sound",
                        (dStack, vStack) ->
                        {
                            try {
                                String s1 = Utilities.readString(dStack).toUpperCase();
                                if (s1.startsWith("+")) {
                                    s1 = s1.substring(1);
                                    byte[] wav = createContiguousDTMF(s1);
                                    dStack.push(new FileBlob(wav, "DTMF.wav"));
                                    return 1;
                                }
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.WEBSERVER) {
                                    sendDtmftoBrowser(s1, predefinedWords);
                                    return 1;
                                }
                                DtmfMorsePlayer.playDtmfString(s1);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "morse", "play Morse code",
                        (dStack, vStack) ->
                        {
                            try {
                                String s1 = Utilities.readString(dStack);
                                if (s1.startsWith("+")) {
                                    s1 = s1.substring(1);
                                    byte[] wav = createContiguousMorse(Morse.text2Morse(s1));
                                    dStack.push(new FileBlob(wav, "DTMF.wav"));
                                    return 1;
                                }
                                String s2 = Morse.text2Morse(s1);
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.WEBSERVER) {
                                    sendMorsetoBrowser(s2, predefinedWords);
                                    return 1;
                                }
                                DtmfMorsePlayer.playMorseString(s2);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "morseTxt", "translate to Morse alphabet",
                        (dStack, vStack) ->
                        {
                            try {
                                String s1 = Utilities.readString(dStack);
                                dStack.push(Morse.text2Morse(s1));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "cTab", "Makes multiplicative group Cayley table matrix of order n",
                        (dStack, vStack) ->
                        {
                            try {
                                long l = Utilities.readLong(dStack);
                                CayleyTable cl = new CayleyTable((int) l);
                                dStack.push(cl.getMatrix());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "cGroup", "Make cyclic group from generator and mod value",
                        (dStack, vStack) ->
                        {
                            try {
                                long mod = Utilities.readLong(dStack);
                                long gen = Utilities.readLong(dStack);
                                ArrayList<Integer> list = Utilities.makeCyclicGroup((int) gen, (int) mod);
                                dStack.push(new DoubleSequence(list));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "iGroup", "calculate inverses of a group",
                        (dStack, vStack) ->
                        {
                            try {
                                long mod = Utilities.readLong(dStack);
                                DoubleSequence ds = Utilities.readDoubleSequence(dStack);
                                ArrayList<Integer> list = Utilities.groupInverses(ds.asIntArray(), (int) mod);
                                dStack.push(new DoubleSequence(list));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "lswap", "swap 2 list or string members",
                        (dStack, vStack) ->
                        {
                            try {
                                int o1 = (int) Utilities.readLong(dStack);
                                int o2 = (int) Utilities.readLong(dStack);
                                Object o3 = dStack.pop();
                                if (o3 instanceof SequenceBase) {
                                    SequenceBase<?> as = ((SequenceBase<?>) o3).swap(o1, o2);
                                    dStack.push(as);
                                } else if (o3 instanceof String) {
                                    char[] arr = ((String) o3).toCharArray();
                                    char tmp = arr[o1];
                                    arr[o1] = arr[o2];
                                    arr[o2] = tmp;
                                    dStack.push(new String(arr));
                                }
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "pdiff", "calculate percentual difference",
                        (dStack, vStack) ->
                        {
                            try {
                                double v1 = Utilities.readDouble(dStack);
                                double v2 = Utilities.readDouble(dStack);
                                double res = 100.0*Math.abs((v1-v2)/((v1+v2)/2));
                                dStack.push(res);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "percent", "calculate x percent of y",
                        (dStack, vStack) ->
                        {
                            try {
                                double o1 = Utilities.readDouble(dStack);
                                double o2 = Utilities.readDouble(dStack);
                                double res = o1 / 100.0 * o2;
                                dStack.push(res);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "whatperc", "calculate x is what percent of y",
                        (dStack, vStack) ->
                        {
                            try {
                                double o1 = Utilities.readDouble(dStack);
                                double o2 = Utilities.readDouble(dStack);
                                double res = o1 / o2 * 100.0;
                                dStack.push(res);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "beep", "play single tone",
                        (dStack, vStack) ->
                        {
                            try {
                                long l1 = Utilities.readLong(dStack); // freq
                                long l2 = Utilities.readLong(dStack); // millisecs
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.WEBSERVER) {
                                    byte[] tone = createSingleToneWav ((int) l1, (int) l2);
                                    AudiotoBrowser(tone, predefinedWords);
                                    return 1;
                                }
                                SynthToneBase.playSingleTone((int) l1, (int) l2);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sbeep", "put single tone on stack",
                        (dStack, vStack) ->
                        {
                            try {
                                long l1 = Utilities.readLong(dStack); // freq
                                long l2 = Utilities.readLong(dStack); // millisecs
                                byte[] tone = createSingleToneWav ((int) l1, (int) l2);
                                dStack.push (new FileBlob (tone,"tone.wav"));
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "bf", "execute brainfuck code",
                        (dStack, vStack) ->
                        {
                            try {
                                String s = Utilities.readString(dStack);
                                String ret = new Brainfuck(predefinedWords._jforth.guiTerminal).interpret(s);
                                if (ret == null)
                                    return 0;
                                dStack.push(ret);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "f2l", "frequency to wave length & vice versa",
                        (dStack, vStack) ->
                        {
                            try {
                                double d = Utilities.readDouble(dStack);
                                d = 299792458.0 / d;
                                dStack.push(d);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "pwd", "get working directory",
                        (dStack, vStack) ->
                        {
                            try {
                                String s = Paths.get("").toAbsolutePath().toString();
                                dStack.push(s);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "getblob", "read blob into memory",
                        (dStack, vStack) ->
                        {
                            try {
                                String s = Utilities.readString(dStack);
                                FileBlob bl = new FileBlob(s);
                                dStack.push(bl);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "mkblob", "make blob from String",
                        (dStack, vStack) ->
                        {
                            try {
                                String s = Utilities.readString(dStack);
                                FileBlob bl = FileBlob.fromStringData(s);
                                dStack.push(bl);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "putblob", "write blob to disk",
                        (dStack, vStack) ->
                        {
                            try {
                                String newpath = Utilities.readString(dStack);
                                FileBlob bl = Utilities.readBlob(dStack);
                                bl.put(newpath);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "blobname", "puts blob's path name on stack",
                        (dStack, vStack) ->
                        {
                            try {
                                FileBlob bl = Utilities.readBlob(dStack);
                                dStack.push(bl.getPath());
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "nameblob", "rename existing blob",
                        (dStack, vStack) ->
                        {
                            try {
                                String newpath = Utilities.readString(dStack);
                                FileBlob bl = Utilities.readBlob(dStack);
                                FileBlob nbl = new FileBlob(bl.get_content(), newpath);
                                dStack.push(nbl);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "playWav", "play Wave file",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                Clip cl = null;
                                if (o instanceof String) {
                                    File f = new File((String) o);
                                    cl = WaveTools.playWave(f, true);
                                } else if (o instanceof FileBlob) {
                                    byte[] dat = ((FileBlob) o).get_content();
                                    cl = WaveTools.playWave(dat, true);
                                }
                                if (cl == null)
                                    return 0;
                                dStack.push(cl);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "stopWav", "stop playing wave clip",
                        (dStack, vStack) ->
                        {
                            try {
                                Clip s = (Clip) dStack.pop();
                                WaveTools.stopWave(s);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "err", "Put last error message onto stack",
                        (dStack, vStack) ->
                        {
                            if (predefinedWords._jforth.LastError == null)
                                dStack.push("Everything's fine ...");
                            else {
                                String sb = predefinedWords._jforth.LastError +
                                        ", " +
                                        (currentTimeMillis() - predefinedWords._jforth.LastETime) / 1000 +
                                        " secs ago\n";
                                dStack.push(sb);
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "plot", "Plot x/y data",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence s1 = Utilities.readDoubleSequence(dStack);
                                DoubleSequence s2 = Utilities.readDoubleSequence(dStack);
                                Plot2D plotter = new Plot2D(s1.asPrimitiveArray(), s2.asPrimitiveArray());
                                SerializableImage img = plotter.plot0();
                                dStack.push(img);
                            } catch (Exception e) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "qr", "Print QR code",
                        (dStack, vStack) ->
                        {
                            try {
                                QRTextWriter ap = new QRTextWriter();
                                String s = Utilities.readString(dStack);
                                dStack.push(ap.render(s));
                            } catch (Exception e) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "binomial", "n choose k",
                        (dStack, vStack) ->
                        {
                            try {
                                double[] dat = Utilities.read2(dStack);
                                dStack.push(exactBinomial((int)dat[0], (int)dat[1]));
                            } catch (Exception e) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "m+", "put any obj into global array",
                        (dStack, vStack) ->
                        {
                            try {
                                String name = Utilities.readString(dStack);
                                Object obj = dStack.pop();
                                predefinedWords._jforth.globalMap.put(name, obj);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "m-", "get obj from global array",
                        (dStack, vStack) ->
                        {
                            try {
                                String name = Utilities.readString(dStack);
                                Object word = predefinedWords._jforth.globalMap.get(name);
                                if (word == null)
                                    return 0;
                                dStack.push(word);
                                predefinedWords._jforth.globalMap.remove(name);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "mlist", "list elements of global array",
                        (dStack, vStack) ->
                        {
                            String list = predefinedWords._jforth.getMapContent();
                            dStack.push(list);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "mclr", "empties the global array",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth.globalMap.clear();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "msave", "save global array to file",
                        (dStack, vStack) ->
                        {
                            String name = Utilities.readString(dStack);
                            try {
                                FileUtils.deepSave(name, predefinedWords._jforth.globalMap);
                            } catch (Exception e) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "osave", "save TOS to file",
                        (dStack, vStack) ->
                        {
                            String name = Utilities.readString(dStack);
                            Object o1 = dStack.pop();
                            try {
                                FileUtils.deepSave(name, o1);
                            } catch (Exception e) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "mload", "load global array from file",
                        (dStack, vStack) ->
                        {
                            String name = Utilities.readString(dStack);
                            try {
                                predefinedWords._jforth.globalMap
                                        = (HashMap<String, Object>) FileUtils.deepLoad(name);
                            } catch (Exception e) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "oload", "load object from file to TOS",
                        (dStack, vStack) ->
                        {
                            String name = Utilities.readString(dStack);
                            try {
                                Object o1 = FileUtils.deepLoad(name);
                                dStack.push(o1);
                            } catch (Exception e) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "wpin", "pin window to top",
                        (dStack, vStack) ->
                        {
                            try {
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.GUITERMINAL) {
                                    JFrame frame = (JFrame) SwingUtilities.getRoot(predefinedWords._jforth.guiTerminal);
                                    frame.setAlwaysOnTop(true);
                                }
//                                else
//                                    MyWinApi.SetConsoleToFG();
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "wfull", "set window to fullscreen",
                        (dStack, vStack) ->
                        {
                            try {
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.GUITERMINAL) {
                                    JFrame frame = (JFrame) SwingUtilities.getRoot(predefinedWords._jforth.guiTerminal);
                                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                                }
//                                else
//                                    MyWinApi.SetConsoleFullScreen();
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "wshow", "show or hide the console",
                        (dStack, vStack) ->
                        {
                            try {
                                long l = Utilities.readLong(dStack);
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.GUITERMINAL) {
                                    JFrame frame = (JFrame) SwingUtilities.getRoot(predefinedWords._jforth.guiTerminal);
                                    frame.setVisible(l == JForth.TRUE);
                                }
//                                else
//                                    MyWinApi.showWnd(l);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "wdat", "put window position data onto stack",
                        (dStack, vStack) ->
                        {
                            if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.GUITERMINAL) {
                                JFrame frame = (JFrame) SwingUtilities.getRoot(predefinedWords._jforth.guiTerminal);
                                DoubleSequence ds = new DoubleSequence();
                                ds.add((double) frame.getX());
                                ds.add((double) frame.getY());
                                Dimension d = frame.getSize();
                                ds.add((double) d.width);
                                ds.add((double) d.height);
                                dStack.push(ds);
                                return 1;
                            }
                            return 0;
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "wpos", "set console position",
                        (dStack, vStack) ->
                        {
                            try {
                                Point p = Utilities.readPoint(dStack);
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.GUITERMINAL) {
                                    JFrame frame = (JFrame) SwingUtilities.getRoot(predefinedWords._jforth.guiTerminal);
                                    frame.setLocation(p);
                                }
//                                else
//                                    MyWinApi.SetConsolePos(p);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "wsize", "set console size",
                        (dStack, vStack) ->
                        {
                            try {
                                Point p = Utilities.readPoint(dStack);
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.GUITERMINAL) {
                                    JFrame frame = (JFrame) SwingUtilities.getRoot(predefinedWords._jforth.guiTerminal);
                                    frame.setSize(p.x, p.y);
                                }
//                                else
//                                   MyWinApi.SetConsoleSize(p);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "replace", "regex transform a string",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                StringSequence ss = Utilities.getStringSequence(o);
                                if (ss != null) {
                                    ArrayList<String> sl = ss.get_list();
                                    String in = Utilities.readString(dStack);
                                    String out = in.replaceAll(sl.get(0), sl.get(1));
                                    dStack.push(out);
                                } else {
                                    DoubleSequence ds = Utilities.getDoubleSequence(o);
                                    ArrayList<Double> sl = ds.get_list();
                                    DoubleSequence src = Utilities.readDoubleSequence(dStack);
                                    DoubleSequence dst = DoubleSequence.replace (src, sl.get(0), sl.get(1));
                                    dStack.push(dst);
                                }
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "roman", "make roman number from integer",
                        (dStack, vStack) ->
                        {
                            try {
                                long ll = Utilities.readLong(dStack);
                                String s = Roman.toRoman((int) ll);
                                dStack.push(s);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "arab", "make arab number from roman",
                        (dStack, vStack) ->
                        {
                            try {
                                String s1 = Utilities.readString(dStack);
                                int i = Roman.toArab(s1);
                                dStack.push(i);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "bkg", "set background color",
                        (dStack, vStack) ->
                        {
                            try {
                                Color col = Utilities.readColor(dStack);
                                if (predefinedWords._jforth.CurrentEnvironment == RuntimeEnvironment.GUITERMINAL) {
                                    predefinedWords._jforth.guiTerminal.setBackground(col);
                                }
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "tochar", "make character from numeric value",
                        (dStack, vStack) ->
                        {
                            try {
                                long ll = Utilities.readLong(dStack);
                                dStack.push(Character.toString((char) ll));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "loadIMG", "load Image from Disk",
                        (dStack, vStack) ->
                        {
                            try {
                                String path = Utilities.readString(dStack);
                                BufferedImage img = ImageIO.read(new File(path));
                                dStack.push(new SerializableImage(img));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "saveIMG", "save Image to Disk",
                        (dStack, vStack) ->
                        {
                            try {
                                String imgformat = ForthProperties.getImgFormat();
                                String fileEnd = "." + imgformat;
                                String path = Utilities.readString(dStack);
                                if (!path.endsWith(fileEnd))
                                    path = path + fileEnd;
                                BufferedImage img;
                                if (dStack.isEmpty())
                                    img = predefinedWords._jforth.guiTerminal.getScreenShot();
                                else
                                    img = ((SerializableImage) dStack.pop()).getImage();
                                ImageIO.write(img, imgformat, new File(path));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ImgScale", "set Image Scale",
                        (dStack, vStack) ->
                        {
                            try {
                                Point pt = Utilities.readPoint(dStack);
                                ForthProperties.putImgScale(pt);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "Ver", "Get version information",
                        (dStack, vStack) ->
                        {
                            dStack.push(BuildInfo.buildInfo);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "lfonts", "Show all system fonts",
                        (dStack, vStack) ->
                        {
                            try {
                                String[] fonts =
                                        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                                StringBuilder sb = new StringBuilder();
                                for (int s=0; s<fonts.length; s++)
                                {
                                    sb.append('@').append(s).append(" : ").append(fonts[s]).append("\r\n");
                                }
                                dStack.push (sb.toString());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "font", "set font",
                        (dStack, vStack) ->
                        {
                            try {
                                String[] fonts =
                                        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                                int fidx = -1;
                                float size = 16;
                                Font font;
                                String name = Utilities.readString(dStack);
                                if (name.startsWith("@"))
                                    fidx = Integer.parseInt(name.substring(1));
                                if (!dStack.empty())
                                    size = (float) Utilities.readDouble(dStack);
                                if (fidx != -1) {
                                    font = new Font (fonts[fidx], Font.PLAIN, (int)size);
                                }
                                else {
                                    font = Font.createFont(Font.TRUETYPE_FONT, new File(name)).deriveFont(size);
                                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                                    ge.registerFont(font);
                                }
                                predefinedWords._jforth.guiTerminal.setFont(font);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "HumBin", "Shows large byte amaounts human readable based on 1024",
                        (dStack, vStack) ->
                        {
                            try {
                                long l = Utilities.readLong(dStack);
                                String si = humanReadableByteCountBin(l);
                                dStack.push(si);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "Free", "Get anount of free heap mmeory",
                        (dStack, vStack) ->
                        {
                            try {
                                long heapFreeSize = Runtime.getRuntime().freeMemory();
                                dStack.push(heapFreeSize);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "HumSi", "Shows large byte amounts human readable based on 1000",
                        (dStack, vStack) ->
                        {
                            try {
                                long l = Utilities.readLong(dStack);
                                String si = humanReadableByteCountSI(l);
                                dStack.push(si);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "FracTran", "Run Fractran language",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence ret = new DoubleSequence();
                                int noint = 0;
                                long arg = Utilities.readLong(dStack);
                                FracSequence fr = (FracSequence) dStack.pop();
                                ArrayList<Fraction> list = fr.get_list();
                                int idx = 0;
                                ret.add((double) arg);
                                while (true) {
                                    double d = arg * list.get(idx).doubleValue();
                                    idx++;
                                    if (idx == list.size())
                                        idx = 0;
                                    if ((d % 1) == 0) {
                                        noint = 0;
                                        ret.add(d);
                                        if (ret.length() == 15) {  /* max length */
                                            break;
                                        }
                                        arg = (long) d;
                                        idx = 0;
                                    } else {
                                        noint++;
                                        if (noint == list.size()) /* no int result */
                                            break;
                                    }
                                }
                                dStack.push(ret);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "c2p", "get polar representaton of complex number",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex c = Utilities.readComplex(dStack);
                                double r = Math.sqrt(c.getReal()*c.getReal()+c.getImaginary()+c.getImaginary());
                                double a = Math.atan(c.getImaginary()/c.getReal());
                                DoubleSequence res = new DoubleSequence(r,a);
                                dStack.push(res);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "p2c", "convert polar representation to complex number",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence ds = Utilities.readDoubleSequence(dStack);
                                if (ds.length() != 2) {
                                    throw new RuntimeException("ds length != 2");
                                }
                                Complex c = polar2Complex (ds.pick(0), ds.pick(1));
                                dStack.push(c);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));
    }
}
