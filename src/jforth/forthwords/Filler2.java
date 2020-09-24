package jforth.forthwords;

import jforth.*;
import jforth.audio.DtmfMorsePlayer;
import jforth.audio.Morse;
import jforth.audio.MusicTones;
import jforth.audio.WaveTools;
import org.fusesource.jansi.AnsiConsole;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.TimerTask;
import java.util.zip.CRC32;

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
                                b = ((String) o1).getBytes(JForth.ENCODING);
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
                        "binStr", "Make binary String",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long ll = Utilities.readLong (dStack);
                                String ss = Long.toBinaryString (ll);
                                dStack.push (ss);
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
                                String hash = Utilities.readString(dStack).toLowerCase ();
                                String input = Utilities.readString(dStack);
                                byte[] inbytes = input.getBytes (JForth.ENCODING);
                                if (hash.equals ("crc32"))
                                {
                                    CRC32 crc = new CRC32 ();
                                    crc.update (inbytes, 0, inbytes.length);
                                    dStack.push (crc.getValue ());
                                }
                                else if (hash.equals ("crc16"))
                                {
                                    CRC16 crc = new CRC16();
                                    crc.calc (inbytes);
                                    dStack.push ((long)crc.getCRC());
                                }
                                else
                                {
                                    MessageDigest md = MessageDigest.getInstance (hash);
                                    dStack.push (new DoubleSequence (md.digest (inbytes)));
                                }
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
                                String encoded = URLEncoder.encode (ss, JForth.ENCODING.name ());
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
                                String encoded = URLDecoder.decode(ss,JForth.ENCODING.name ());
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
                        "collect", "collects all numbers or strings from stack into sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                StringSequence sq = new StringSequence();
                                while (!dStack.isEmpty())
                                {
                                    sq.add (Utilities.makePrintable (dStack.pop (), 10));
                                }
                                try
                                {
                                    DoubleSequence ds = new DoubleSequence (sq);
                                    dStack.push (ds.reverse());
                                }
                                catch (NumberFormatException e)
                                {
                                    dStack.push (sq.reverse());
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
                                MusicTones.playSong (s1);
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
                                DtmfMorsePlayer.playDtmfString (s1);
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
                        "morse", "play Morse code",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String s1 = Utilities.readString(dStack);
                                DtmfMorsePlayer.playMorseString (Morse.text2Morse(s1));
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

        _fw.add(new PrimitiveWord
                (
                        "beep", "play single tone",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long l1 = Utilities.readLong (dStack);
                                long l2 = Utilities.readLong (dStack);
                                MusicTones.playSingleTone ((int)l1, (int)l2);
                                return 1;
                            }
                            catch (Exception ignored)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "bf", "execute brainfuck code",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String s = Utilities.readString (dStack);
                                String ret = new Brainfuck().interpret (s);
                                if (ret == null)
                                    return 0;
                                dStack.push (ret);
                                return 1;
                            }
                            catch (Exception ignored)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "pwd", "get working directory",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String s = Paths.get("").toAbsolutePath().toString();
                                dStack.push (s);
                                return 1;
                            }
                            catch (Exception ignored)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "getblob", "read blob into memory",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String s = Utilities.readString (dStack);
                                FileBlob bl = new FileBlob(s);
                                dStack.push (bl);
                                return 1;
                            }
                            catch (Exception ignored)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "mkblob", "make blob from String",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String s = Utilities.readString (dStack);
                                FileBlob bl = FileBlob.fromStringData (s);
                                dStack.push (bl);
                                return 1;
                            }
                            catch (Exception ignored)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "putblob", "write blob to disk",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String newpath = Utilities.readString (dStack);
                                FileBlob bl = Utilities.readBlob (dStack);
                                bl.put (newpath);
                                return 1;
                            }
                            catch (Exception ignored)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "blobname", "puts blob's path name on stack",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                FileBlob bl = Utilities.readBlob (dStack);
                                dStack.push (bl.getPath ());
                                return 1;
                            }
                            catch (Exception ignored)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "playWav", "play Wave file",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Object o = dStack.pop ();
                                Clip cl = null;
                                if (o instanceof String)
                                {
                                    File f = new File((String)o);
                                    cl = WaveTools.playWave (f, true);
                                }
                                else if (o instanceof FileBlob)
                                {
                                    byte[] dat = ((FileBlob)o).get_content ();
                                    cl = WaveTools.playWave (dat, true);
                                }
                                if (cl == null)
                                   return 0;
                                dStack.push (cl);
                                return 1;
                            }
                            catch (Exception ignored)
                            {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "stopWav", "stop playing wave clip",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Clip s = (Clip)dStack.pop();
                                WaveTools.stopWave (s);
                                return 1;
                            }
                            catch (Exception ignored)
                            {
                            }
                            return 0;
                        }
                ));

    }
}
