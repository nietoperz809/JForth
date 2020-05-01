package jforth.forthwords;

import jforth.*;
import jforth.waves.*;
import org.fusesource.jansi.AnsiConsole;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.TimerTask;

class Filler2
{
    static void fill (WordsList _fw, PredefinedWords predefinedWords)
    {
        LSystem lSys = predefinedWords._jforth._lsys;

        _fw.add(new PrimitiveWord
                (
                        "LsGet", "get LSystem result",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = lSys.doIt ();
                                dStack.push(ss);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsRep", "run LSystem on previous result ",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = lSys.doNext ();
                                dStack.push(ss);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsPut", "Set Matrial for LSystem",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                lSys.setMaterial (ss);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsRule", "Set Rule for LSystem",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                lSys.putRule (ss);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "LsClr", "Remove all rules",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                lSys.clrRules();
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "LsInfo", "Get complete LSystem as String",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String s = lSys.toString ();
                                dStack.push (s);
                                return 1;
                            }
                            catch (Exception e)
                            {
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
                            if (o1 instanceof String)
                            {
                                b = ((String) o1).getBytes(StandardCharsets.ISO_8859_1);
                            }
                            else if (o1 instanceof DoubleSequence)
                            {
                                b = ((DoubleSequence) o1).asBytes();
                            }
                            if (b != null)
                            {
                                dStack.push(Utilities.printHexBinary(b));
                                return 1;
                            }
                            if (o1 instanceof Long)
                            {
                                dStack.push(Long.toHexString((Long)o1));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unhexStr", "Make Hexstr to Bytes",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                byte[] b = Utilities.parseHexBinary(ss);
                                dStack.push(new DoubleSequence(b));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "seq", "generate sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                double d3 = Utilities.readDouble(dStack);
                                int l2 = (int)Utilities.readLong(dStack);
                                double d1 = Utilities.readDouble(dStack);
                                DoubleSequence ds = DoubleSequence.makeCounted(d1, l2, d3);
                                dStack.push(ds);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "hash", "generate hash string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String hash = Utilities.readString(dStack);
                                String input = Utilities.readString(dStack);
                                MessageDigest md = MessageDigest.getInstance(hash);
                                dStack.push(new DoubleSequence(md.digest(input.getBytes(StandardCharsets.ISO_8859_1))));
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "b64", "make Base64 from String",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                byte[] bytes = ss.getBytes(JForth.ENCODING);
                                String encoded = Base64.getEncoder().encodeToString(bytes);
                                dStack.push(encoded);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unb64", "make String from Base64",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                byte[] decoded = Base64.getDecoder().decode(ss);
                                String b2 = new String(decoded, JForth.ENCODING);
                                dStack.push(b2);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "urlEnc", "URL encode a string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                String encoded = URLEncoder.encode(ss,JForth.ENCODING);
                                dStack.push(encoded);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "urlDec", "Decode URL encoded string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                String encoded = URLDecoder.decode(ss,JForth.ENCODING);
                                dStack.push(encoded);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "psp", "Push space on stack",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                dStack.push(" ");
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "say", "speak a string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                JForth.speak(ss);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "js", "evaluate js expression string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
                                Object o = engine.eval(ss);
                                dStack.push(o);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "java", "compile and run java class",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String source = Utilities.readString(dStack);
                                String classname = "Solution";
                                source = "public class " + classname + " {" + source + "}";
                                Object arg = dStack.pop();
                                JavaExecutor compiler = new JavaExecutor();
                                final Method greeting = compiler.compileStaticMethod("main", classname, source);
                                final Object result = greeting.invoke(null, arg);
                                dStack.push(result);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "forth", "execute forth line asynchronously",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                final String ss = Utilities.readString(dStack);
                                new Thread(() ->
                                {
                                    JForth f = new JForth (AnsiConsole.out,
                                            RuntimeEnvironment.CONSOLE);
                                    f.interpretLine(ss);
                                    AnsiConsole.out.flush();
                                }).start();
                                return 1;
                            }
                            catch (Exception ex)
                            {
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
                            if (o instanceof BaseWord)
                            {
                                BaseWord bw = (BaseWord) o;
                                return bw.execute(dStack, vStack);
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "if", true,
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling)
                            {
                                return 1;
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
                            if (!predefinedWords._jforth.compiling)
                            {
                                return 1;
                            }
                            Object o = vStack.pop();
                            int thenIndex = predefinedWords._jforth.wordBeingDefined.getNextWordIndex();
                            if (o instanceof ElseControlWord)
                            {
                                ((ElseControlWord) o).setThenIndexIncrement(thenIndex);
                                o = vStack.pop();
                            }
                            if (o instanceof IfControlWord)
                            {
                                ((IfControlWord) o).setThenIndex(thenIndex);
                            }
                            else
                            {
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
                            if (!predefinedWords._jforth.compiling)
                            {
                                return 1;
                            }
                            Object o = vStack.peek();
                            if (o instanceof IfControlWord)
                            {
                                int elseIndex = predefinedWords._jforth.wordBeingDefined.getNextWordIndex() + 1;
                                ElseControlWord ecw = new ElseControlWord(elseIndex);
                                predefinedWords._jforth.wordBeingDefined.addWord(ecw);
                                vStack.push(ecw);
                                ((IfControlWord) o).setElseIndex(elseIndex);
                            }
                            else
                            {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "do", true,
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
                        "leave", true,
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling)
                            {
                                return 1;
                            }
                            LeaveLoopControlWord llcw = new LeaveLoopControlWord();
                            predefinedWords._jforth.wordBeingDefined.addWord(llcw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "loop", true,  "repeat loop",
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
                        "begin", true,
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
                        "until", true,
                        (dStack, vStack) ->
                                WordHelpers.addLoopWord(vStack, predefinedWords, EndLoopControlWord.class)
                ));

        _fw.add(new PrimitiveWord
                (
                        "again", true,
                        (dStack, vStack) ->
                        {
                            try
                            {
                                predefinedWords._jforth.wordBeingDefined.addWord(predefinedWords._wl.search("false"));
                            }
                            catch (Exception e)
                            {
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
                            if (!predefinedWords._jforth.compiling)
                            {
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
                            try
                            {
                                long n = Utilities.readLong(dStack);
                                ArrayList<Double> ar = new ArrayList<>();
                                ar.add((double)n);
                                for (;n!=1;)
                                {
                                    if (n%2 == 0)
                                        n=n/2;
                                    else
                                        n=3*n+1;
                                    ar.add((double)n);
                                }
                                dStack.push (new DoubleSequence(ar));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ping", "Check a host",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String host  = Utilities.readString (dStack);
                                InetAddress inet = InetAddress.getByName(host);
                                if (inet.isReachable(5000))
                                    dStack.push(JForth.TRUE);
                                else
                                    dStack.push(JForth.FALSE);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "msg", "Show message box",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String txt = Utilities.readString (dStack);
                                JOptionPane.showMessageDialog(null,
                                        txt,
                                        "JForth",
                                        JOptionPane.PLAIN_MESSAGE);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ask", "Show yes/no box",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String txt = Utilities.readString (dStack);
                                JForth f = predefinedWords._jforth;
                                if (f.CurrentEnvironment == RuntimeEnvironment.WEBSERVER)
                                {
                                    f._out.print ("alertbox%%~"+txt);
                                    return 1;
                                }
                                int dialogResult = JOptionPane.showConfirmDialog(null,
                                        txt+"?",
                                        "Jforth", JOptionPane.YES_NO_OPTION);
                                if (dialogResult == 0)
                                    dStack.push(JForth.TRUE);
                                else
                                    dStack.push(JForth.FALSE);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "asyncmsg", "Show asynchronous message box",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String txt = Utilities.readString (dStack);
                                new Thread(() ->
                                {
                                    JOptionPane.showMessageDialog(null,
                                            txt,
                                            "JForth",
                                            JOptionPane.PLAIN_MESSAGE);
                                }).start();
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "what", "Show description about a word",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String wordname = Utilities.readString (dStack);
                                BaseWord bw = _fw.search(wordname); //dictionary.search(word);
                                if (bw == null)
                                    return 0;
                                String info = bw.getInfo();
                                dStack.push(info);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "collect", "collects all numbers from stack into sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                StringSequence sq = new StringSequence();
                                while (!dStack.isEmpty())
                                {
                                    sq.add (Utilities.makePrintable (dStack.pop (), 10));
                                }
                                dStack.push (sq.reverse());
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "scatter", "desintegrate sequence onto stack",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence ds = Utilities.readDoubleSequence(dStack);
                                for (Double d : ds.asPrimitiveArray())
                                {
                                    dStack.push(d);
                                }
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toTime", "make time string from TOS",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Long v = Utilities.readLong(dStack);
                                dStack.push(Utilities.toTimeView(v));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "soon", "run deferred word",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                final long delay = Utilities.readLong(dStack);
                                final Object o = dStack.pop();
                                if (o instanceof BaseWord)
                                {
                                    final BaseWord bw = (BaseWord) o;

                                    new java.util.Timer().schedule(new TimerTask()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            bw.execute(dStack, vStack);
                                        }
                                    },delay);
                                    return 1;
                                }
                            }
                            catch (Exception unused)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "cls", "clear screen",
                        (dStack, vStack) ->
                        {
                            //predefinedWords._jforth._out.print('\u000C');
                            predefinedWords._jforth._out.print("\u001b[2J");
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
                            try
                            {
                                String s1 = Utilities.readString(dStack);
                                MusicTones mt = new MusicTones();
                                Wave16 wv = mt.makeSong(44100, s1);
                                byte[] bts = Wave16.makeHeader(wv.toByteArray(), 44100);
                                dStack.push(Base64.getEncoder().encodeToString(bts));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "DTMF", "create DTMF sound",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String s1 = Utilities.readString(dStack);
                                DTMF dt = new DTMF(44100, 1500*4);
                                byte[] bt = dt.dtmfFromString(s1).toByteArray();
                                byte[] combined = Wave16.makeHeader(bt, 44100);
                                dStack.push(Base64.getEncoder().encodeToString(combined));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "morse", "Morse signal from string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String s1 = Utilities.readString(dStack);
                                Morse mors = new Morse (44100);
                                byte[] morse = mors.text2Wave(s1);
                                byte[] combined = Wave16.makeHeader(morse, 44100);
                                dStack.push(Base64.getEncoder().encodeToString(combined));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "morseTxt", "translate to Morse alphabet",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String s1 = Utilities.readString(dStack);
                                dStack.push(Morse.text2Morse(s1));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sinWav", "Make sinus wave",
                        (dStack2, vStack2) -> executeSine(dStack2)
                ));

        _fw.add(new PrimitiveWord
                (
                        "rectWav", "Make rectangle wave",
                        (dStack2, vStack2) -> executeRect(dStack2)
                ));

        _fw.add(new PrimitiveWord
                (
                        "sawWav", "Make sawrooth wave",
                        (dStack2, vStack2) -> executeSaw(dStack2)
                ));

        _fw.add(new PrimitiveWord
                (
                        "triWav", "Make triangle wave",
                        (dStack1, vStack1) -> executeTri(dStack1)
                ));

        _fw.add(new PrimitiveWord
                (
                        "cTab", "Makes multiplicative group Cayley table matrix of order n",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long l = Utilities.readLong(dStack);
                                CayleyTable cl = new CayleyTable ((int)l);
                                dStack.push (cl.getMatrix ());
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "cGroup", "Make cyclic group from generator and mod value",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long mod = Utilities.readLong(dStack);
                                long gen = Utilities.readLong(dStack);
                                ArrayList<Integer> list = Utilities.makeCyclicGroup ((int)gen, (int)mod);
                                dStack.push (new DoubleSequence (list));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "iGroup", "calculate inverses of a group",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long mod = Utilities.readLong(dStack);
                                DoubleSequence ds = Utilities.readDoubleSequence (dStack);
                                ArrayList<Integer> list = Utilities.groupInverses (ds.asIntArray (), (int)mod);
                                dStack.push (new DoubleSequence (list));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "lswap", "swap 2 list members",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                int o1 = (int)Utilities.readLong (dStack);
                                int o2 = (int)Utilities.readLong (dStack);
                                Object o3 = dStack.pop ();
                                if (o3 instanceof StringSequence)
                                {
                                    StringSequence ss = ((StringSequence)o3).swap (o1, o2);
                                    dStack.push(ss);
                                    return 1;
                                }
                                if (o3 instanceof DoubleSequence)
                                {
                                    DoubleSequence ss = ((DoubleSequence)o3).swap (o1, o2);
                                    dStack.push(ss);
                                    return 1;
                                }
                            }
                            catch (Exception unused)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "percent", "calculate x percent of y",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                double o1 = Utilities.readDouble (dStack);
                                double o2 = Utilities.readDouble (dStack);
                                double res = o1/100.0 * o2;
                                dStack.push (res);
                                return 1;
                            }
                            catch (Exception unused)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "whatperc", "calculate x is what percent of y",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                double o1 = Utilities.readDouble (dStack);
                                double o2 = Utilities.readDouble (dStack);
                                double res = o1/o2 * 100.0;
                                dStack.push (res);
                                return 1;
                            }
                            catch (Exception unused)
                            {
                            }
                            return 0;
                        }
                ));
    }

    private static int executeSine (OStack dStack)
    {
        return genWave(dStack, WaveForms::curveSine);
    }

    private static int executeRect (OStack dStack)
    {
        return genWave(dStack, WaveForms::curveRect);
    }

    private static int executeSaw (OStack dStack)
    {
        return genWave(dStack, WaveForms::curveSawTooth);
    }

    private static int executeTri (OStack dStack)
    {
        return genWave(dStack, WaveForms::curveTriangle);
    }

    interface WaveGen
    {
        Wave16 gen (int rate, int len, double freq, int startval);
    }

    private static int genWave (OStack dStack, WaveGen wvg)
    {
        try
        {
            double freq = Utilities.readDouble(dStack); // Hz
            int len = (int)Utilities.readLong(dStack);  // milliseconds
            Wave16 wv = wvg.gen(11025,11*len,freq, 0);

            byte[] combined = Wave16.makeHeader(wv.toByteArray(), 11025);
            dStack.push(Base64.getEncoder().encodeToString(combined));
            return 1;
        }
        catch (Exception unused)
        {
            return 0;
        }
    }

}
