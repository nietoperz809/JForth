package jforth;

import jforth.scalacode.MyMath;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.util.ArithmeticUtils;
import scala.math.BigInt;
import webserver.SimpleWebserver;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

final class PredefinedWords
{
    private final JForth _jforth;

    PredefinedWords (JForth jf, WordsList wl)
    {
        this._jforth = jf;
        fill(wl);
    }

    private void fill (WordsList _fw)
    {
        // do nothing. comments handled by tokenizer
        _fw.add(new PrimitiveWord   // dummy
                (
                        "(", true, "Begin comment",
                        (dStack, vStack) -> 1
                )
        );

        // do nothing. this handled by tokenizer
        _fw.add(new PrimitiveWord  // dummy
                (
                        ".\"", true, "String output",
                        (dStack, vStack) -> 1
                )
        );

        _fw.add(new PrimitiveWord
                (
                        "'", true, "Push word from dictionary on stack",
                        (dStack, vStack) ->
                        {
                            if (_jforth.compiling)
                            {
                                return 1;
                            }
                            String name = _jforth.getNextToken();
                            if (name == null)
                            {
                                return 0;
                            }
                            BaseWord bw = null;
                            try
                            {
                                bw = _jforth.dictionary.search(name);
                            }
                            catch (Exception ignore)
                            {
                            }
                            if (bw != null)
                            {
                                dStack.push(bw);
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "seq", false, "generate sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                double d3 = Utilities.readDouble(dStack);
                                long l2 = Utilities.readLong(dStack);
                                double d1 = Utilities.readDouble(dStack);
                                DoubleSequence ds = DoubleSequence.makeCounted(d1,l2,d3);
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
                        "execute", false, "executes word from stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
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
                            if (!_jforth.compiling)
                            {
                                return 1;
                            }
                            int currentIndex = _jforth.wordBeingDefined.getNextWordIndex();
                            IfControlWord ifcw = new IfControlWord(currentIndex);
                            _jforth.wordBeingDefined.addWord(ifcw);
                            vStack.push(ifcw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "then", true,
                        (dStack, vStack) ->
                        {
                            if (!_jforth.compiling)
                            {
                                return 1;
                            }
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.pop();
                            int thenIndex = _jforth.wordBeingDefined.getNextWordIndex();
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
                            if (!_jforth.compiling)
                            {
                                return 1;
                            }
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.peek();
                            if (o instanceof IfControlWord)
                            {
                                int elseIndex = _jforth.wordBeingDefined.getNextWordIndex() + 1;
                                ElseControlWord ecw = new ElseControlWord(elseIndex);
                                _jforth.wordBeingDefined.addWord(ecw);
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
                            if (!_jforth.compiling)
                            {
                                return 1;
                            }
                            DoLoopControlWord dlcw = new DoLoopControlWord();
                            _jforth.wordBeingDefined.addWord(dlcw);
                            int index = _jforth.wordBeingDefined.getNextWordIndex();
                            vStack.push((long) index);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "i", false,
                        (dStack, vStack) ->
                        {
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.peek();
                            dStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "j", false,
                        (dStack, vStack) ->
                        {
                            if (vStack.size() < 4)
                            {
                                return 0;
                            }
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
                            if (!_jforth.compiling)
                            {
                                return 1;
                            }
                            if (vStack.size() < 2)
                            {
                                return 0;
                            }
                            LeaveLoopControlWord llcw = new LeaveLoopControlWord();
                            _jforth.wordBeingDefined.addWord(llcw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "loop", true,
                        (dStack, vStack) ->
                        {
                            if (!_jforth.compiling)
                            {
                                return 1;
                            }
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.pop();
                            if (!(o instanceof Long))
                            {
                                return 0;
                            }
                            int beginIndex = ((Long) o).intValue();
                            int endIndex = _jforth.wordBeingDefined.getNextWordIndex();
                            int increment = beginIndex - endIndex;
                            LoopControlWord lcw = new LoopControlWord(increment);
                            _jforth.wordBeingDefined.addWord(lcw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "+loop", true,
                        (dStack, vStack) ->
                        {
                            if (!_jforth.compiling)
                            {
                                return 1;
                            }
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.pop();
                            if (!(o instanceof Long))
                            {
                                return 0;
                            }
                            int beginIndex = ((Long) o).intValue();
                            int endIndex = _jforth.wordBeingDefined.getNextWordIndex();
                            int increment = beginIndex - endIndex;
                            PlusLoopControlWord plcw = new PlusLoopControlWord(increment);
                            _jforth.wordBeingDefined.addWord(plcw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "begin", true,
                        (dStack, vStack) ->
                        {
                            if (!_jforth.compiling)
                            {
                                return 1;
                            }
                            int index = _jforth.wordBeingDefined.getNextWordIndex();
                            vStack.push((long) index);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "until", true,
                        (dStack, vStack) ->
                        {
                            if (!_jforth.compiling)
                            {
                                return 1;
                            }
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.pop();
                            if (!(o instanceof Long))
                            {
                                return 0;
                            }
                            int beginIndex = ((Long) o).intValue();
                            int endIndex = _jforth.wordBeingDefined.getNextWordIndex();
                            int increment = beginIndex - endIndex;
                            EndLoopControlWord ecw = new EndLoopControlWord(increment);
                            _jforth.wordBeingDefined.addWord(ecw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "dup", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.peek();
                            if (o instanceof DoubleSequence)
                            {
                                dStack.push(new DoubleSequence((DoubleSequence) o));
                            }
                            else
                            {
                                dStack.push(o);
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "?dup", false, "Duplicate TOS if not zero",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.peek();
                            if (o instanceof Long)
                            {
                                if (((Long) o) != 0)
                                {
                                    dStack.push(o);
                                }
                            }
                            else if (o instanceof Double)
                            {
                                if (((Double) o) != 0.0)
                                {
                                    dStack.push(o);
                                }
                            }
                            else if (o instanceof DoubleSequence)
                            {
                                if (!((DoubleSequence) o).isEmpty())
                                {
                                    dStack.push(new DoubleSequence((DoubleSequence) o));
                                }
                            }
                            else if (o instanceof String)
                            {
                                if (!((String) o).isEmpty())
                                {
                                    dStack.push(o);
                                }
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
                        "permute", false, "Generate permutation",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long l2 = Utilities.readLong(dStack);
                                DoubleSequence ds = Utilities.readDoubleSequence(dStack);
                                int[] arr = LehmerCode.perm(ds.length(), (int) l2);
                                DoubleSequence out = ds.rearrange(arr);
                                dStack.push(out);
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
                        "drop", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            dStack.pop();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "swap", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            dStack.push(o1);
                            dStack.push(o2);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "over", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            dStack.push(o2);
                            dStack.push(o1);
                            dStack.push(o2);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "rot", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 3)
                            {
                                return 0;
                            }
                            Object o3 = dStack.pop();
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            dStack.push(o2);
                            dStack.push(o3);
                            dStack.push(o1);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "depth", false,
                        (dStack, vStack) ->
                        {
                            Long i = (long) dStack.size();
                            dStack.push(i);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "<", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                if (i1 < i2)
                                {
                                    dStack.push(JForth.TRUE);
                                }
                                else
                                {
                                    dStack.push(JForth.FALSE);
                                }
                            }
                            else if ((o1 instanceof Double) && (o2 instanceof Double))
                            {
                                double d1 = (Double) o1;
                                double d2 = (Double) o2;
                                if (d1 < d2)
                                {
                                    dStack.push(JForth.TRUE);
                                }
                                else
                                {
                                    dStack.push(JForth.FALSE);
                                }
                            }
                            else if ((o1 instanceof String) && (o2 instanceof String))
                            {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                int result = s1.compareTo(s2);
                                if (result < 0)
                                {
                                    dStack.push(JForth.TRUE);
                                }
                                else
                                {
                                    dStack.push(JForth.FALSE);
                                }
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
                        "=", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                if (i1 == i2)
                                {
                                    dStack.push(JForth.TRUE);
                                }
                                else
                                {
                                    dStack.push(JForth.FALSE);
                                }
                            }
                            else if ((o1 instanceof Double) && (o2 instanceof Double))
                            {
                                double d1 = (Double) o1;
                                double d2 = (Double) o2;
                                if (d1 == d2)
                                {
                                    dStack.push(JForth.TRUE);
                                }
                                else
                                {
                                    dStack.push(JForth.FALSE);
                                }
                            }
                            else if ((o1 instanceof String) && (o2 instanceof String))
                            {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                int result = s1.compareTo(s2);
                                if (result == 0)
                                {
                                    dStack.push(JForth.TRUE);
                                }
                                else
                                {
                                    dStack.push(JForth.FALSE);
                                }
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
                        "<>", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                dStack.push(i1 != i2 ? JForth.TRUE : JForth.FALSE);
                            }
                            else if ((o1 instanceof Double) && (o2 instanceof Double))
                            {
                                double d1 = (Double) o1;
                                double d2 = (Double) o2;
                                dStack.push(d1 != d2 ? JForth.TRUE : JForth.FALSE);
                            }
                            else if ((o1 instanceof String) && (o2 instanceof String))
                            {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                dStack.push(s1.compareTo(s2) != 0 ? JForth.TRUE : JForth.FALSE);
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
                        ">", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                if (i1 > i2)
                                {
                                    dStack.push(JForth.TRUE);
                                }
                                else
                                {
                                    dStack.push(JForth.FALSE);
                                }
                            }
                            else if ((o1 instanceof Double) && (o2 instanceof Double))
                            {
                                double d1 = (Double) o1;
                                double d2 = (Double) o2;
                                if (d1 > d2)
                                {
                                    dStack.push(JForth.TRUE);
                                }
                                else
                                {
                                    dStack.push(JForth.FALSE);
                                }
                            }
                            else if ((o1 instanceof String) && (o2 instanceof String))
                            {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                int result = s1.compareTo(s2);
                                if (result > 0)
                                {
                                    dStack.push(JForth.TRUE);
                                }
                                else
                                {
                                    dStack.push(JForth.FALSE);
                                }
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
                        "0<", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                dStack.push((i1 < 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            }
                            else if (o1 instanceof Double)
                            {
                                double d1 = (Double) o1;
                                dStack.push((d1 < 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "0=", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                dStack.push((i1 == 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            }
                            else if (o1 instanceof Double)
                            {
                                double d1 = (Double) o1;
                                dStack.push((d1 == 0.0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "0>", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                dStack.push((i1 > 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            }
                            else if (o1 instanceof Double)
                            {
                                double d1 = (Double) o1;
                                dStack.push((d1 > 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "not", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                dStack.push((i1 == JForth.FALSE) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "true", false,
                        (dStack, vStack) ->
                        {
                            dStack.push(JForth.TRUE);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "false", false,
                        (dStack, vStack) ->
                        {
                            dStack.push(JForth.FALSE);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "+", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try
                            {
                                dStack.push (Calculator.doCalcBigInt(o2, o1, BigInt::$plus));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcComplex(o2, o1, Complex::add));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcFraction(o2, o1, Fraction::add));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcPoly(o2, o1, PolynomialFunction ::add));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcDouble(o2, o1, Calculator::add));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 += i1;
                                dStack.push(i2);
                            }
                            else if ((o1 instanceof String) && (o2 instanceof String))
                            {
                                String s = (String) o2 + (String) o1;
                                dStack.push(s);
                            }
                            else if ((o1 instanceof DoubleSequence) && (o2 instanceof DoubleSequence))
                            {
                                DoubleSequence s = new DoubleSequence((DoubleSequence) o2, (DoubleSequence) o1);
                                dStack.push(s);
                            }
                            else if ((o1 instanceof Double) && (o2 instanceof DoubleSequence))
                            {
                                Double d1 = (Double) o1;
                                DoubleSequence d2 = (DoubleSequence) o2;
                                dStack.push(d2.add(d1));
                            }
                            else if ((o1 instanceof Long) && (o2 instanceof DoubleSequence))
                            {
                                Long d1 = (Long) o1;
                                DoubleSequence d2 = (DoubleSequence) o2;
                                dStack.push(d2.add(d1.doubleValue()));
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
                        "-", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try
                            {
                                dStack.push (Calculator.doCalcBigInt(o2, o1, BigInt::$minus));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcComplex(o2, o1, Complex::subtract));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcFraction(o2, o1, Fraction::subtract));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcPoly(o2, o1, PolynomialFunction::subtract));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcDouble(o2, o1, Calculator::sub));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 -= i1;
                                dStack.push(i2);
                            }
                            else if ((o1 instanceof DoubleSequence) && (o2 instanceof DoubleSequence))
                            {
                                DoubleSequence d1 = (DoubleSequence) o1;
                                DoubleSequence d2 = (DoubleSequence) o2;
                                dStack.push(d2.difference(d1));
                            }
                            else if ((o1 instanceof Long) && (o2 instanceof DoubleSequence))
                            {
                                Long d1 = (Long) o1;
                                DoubleSequence d2 = (DoubleSequence) o2;
                                dStack.push(d2.subList(0, d2.length() - d1.intValue()));
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
                        "1+", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                dStack.push(i1 + 1);
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "1-", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                dStack.push(i1 - 1);
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2+", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                dStack.push(i1 + 2);
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2-", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                dStack.push(i1 - 2);
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "*", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try
                            {
                                dStack.push (Calculator.doCalcBigInt(o2, o1, BigInt::$times));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcComplex(o2, o1, Complex::multiply));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcFraction(o2, o1, Fraction::multiply));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcPoly(o2, o1, PolynomialFunction::multiply));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcDouble(o2, o1, Calculator::mult));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                if (o2 instanceof Long)
                                {
                                    long i2 = (Long) o2;
                                    dStack.push(i2*i1);
                                    return 1;
                                }
                                else if (o2 instanceof DoubleSequence)
                                {
                                    DoubleSequence d2 = (DoubleSequence) o2;
                                    DoubleSequence d3 = new DoubleSequence();  // empty
                                    while (i1-- != 0)
                                    {
                                        d3 = d3.add(d2);
                                    }
                                    dStack.push(d3);
                                    return 1;
                                }
                                else if (o2 instanceof String)
                                {
                                    String d2 = (String) o2;
                                    StringBuilder sb = new StringBuilder();  // empty
                                    while (i1-- != 0)
                                    {
                                        sb.append(d2);
                                    }
                                    dStack.push(sb.toString());
                                    return 1;
                                }
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "/", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try
                            {
                                dStack.push (Calculator.doCalcBigInt(o2, o1, BigInt::$div));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcComplex(o2, o1, Complex::divide));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcFraction(o2, o1, Fraction::divide));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcPoly(o2, o1, Utilities::polyDiv));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcDouble(o2, o1, Calculator::div));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 /= i1;
                                dStack.push(i2);
                            }
                            else if ((o1 instanceof Long) && (o2 instanceof String))
                            {
                                long d1 = (Long) o1;
                                String d2 = (String) o2;
                                List<String> ll = Utilities.splitEqually(d2, (int) d1);
                                if (ll == null)
                                {
                                    return 0;
                                }
                                for (String s : ll)
                                {
                                    dStack.push(s);
                                }
                            }
                            else if ((o1 instanceof Long) && (o2 instanceof DoubleSequence))
                            {
                                long d1 = (Long) o1;
                                DoubleSequence d2 = (DoubleSequence) o2;
                                List<DoubleSequence> ll = Utilities.splitEqually(d2, (int) d1);
                                if (ll == null)
                                {
                                    return 0;
                                }
                                for (DoubleSequence s : ll)
                                {
                                    dStack.push(s);
                                }
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
                        "mod", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try
                            {
                                dStack.push (Calculator.doCalcBigInt(o2, o1, BigInt::mod));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                dStack.push (Calculator.doCalcPoly(o2, o1, Utilities::polyMod));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 %= i1;
                                dStack.push(i2);
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
                        "/mod", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try
                            {
                                dStack.push (Calculator.doCalcPoly(o2, o1, Utilities::polyMod));
                                dStack.push (Calculator.doCalcPoly(o2, o1, Utilities::polyDiv));
                                return 1;
                            }
                            catch (Exception ignored) {}
                            try
                            {
                                long l1 = Utilities.getLong(o1);
                                long l2 = Utilities.getLong(o2);
                                dStack.push(l2%l1);
                                dStack.push(l2/l1);
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
                        "max", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 = Math.max(i1, i2);
                                dStack.push(i2);
                            }
                            else if ((o1 instanceof String) && (o2 instanceof String))
                            {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                s2 = (s1.compareTo(s2) > 0) ? s1 : s2;
                                dStack.push(s2);
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
                        "min", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 = Math.min(i1, i2);
                                dStack.push(i2);
                            }
                            else if ((o1 instanceof String) && (o2 instanceof String))
                            {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                s2 = (s1.compareTo(s2) < 0) ? s1 : s2;
                                dStack.push(s2);
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
                        "abs", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                i1 = Math.abs(i1);
                                dStack.push(i1);
                            }
                            else if (o1 instanceof Complex)
                            {
                                Complex d1 = (Complex) o1;
                                dStack.push(d1.abs());
                            }
                            else if (o1 instanceof Fraction)
                            {
                                Fraction d1 = (Fraction) o1;
                                dStack.push(d1.abs());
                            }
                            else
                            {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord   //
                (
                        "phi", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Complex)
                            {
                                Complex d1 = (Complex) o1;
                                dStack.push(Math.atan(d1.getImaginary() / d1.getReal()));
                            }
                            else
                            {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord   //
                (
                        "conj", false,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Complex)
                            {
                                Complex d1 = (Complex) o1;
                                dStack.push(d1.conjugate());
                            }
                            if (o1 instanceof Fraction)
                            {
                                Fraction d1 = (Fraction) o1;
                                dStack.push(d1.reciprocal());
                            }
                            else
                            {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord   //
                (
                        "split", false, "Split object into partitions",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Complex)
                            {
                                Complex d1 = (Complex) o1;
                                dStack.push(d1.getReal());
                                dStack.push(d1.getImaginary());
                                return 1;
                            }
                            if (o1 instanceof Fraction)
                            {
                                Fraction d1 = (Fraction) o1;
                                dStack.push((double) d1.getNumerator());
                                dStack.push((double) d1.getDenominator());
                                return 1;
                            }
                            if (o1 instanceof Double)
                            {
                                Double d1 = (Double) o1;
                                dStack.push(Math.floor(d1));
                                dStack.push(d1 - Math.floor(d1));
                                return 1;
                            }
                            if (o1 instanceof String)
                            {
                                String s = (String) o1;
                                if (dStack.empty())
                                {
                                    return 0;
                                }
                                Object o2 = dStack.pop();
                                if (!(o2 instanceof String))
                                {
                                    return 0;
                                }
                                String[] sp = s.split((String) o2);
                                for (String x : sp)
                                {
                                    dStack.push(x);
                                }
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "and", false, "Binary and of 2 values",
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 &= i1;
                                dStack.push(i2);
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
                        "or", false, "Binary or of 2 values",
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 |= i1;
                                dStack.push(i2);
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
                        "xor", false, "Xors two values",
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 ^= i1;
                                dStack.push(i2);
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
                        "<<", false, "Rotate left",
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i2 = (Long) o2;
                                int i1 = (int) ((Long) o1).longValue();
                                i2 = Long.rotateLeft(i2, i1);
                                dStack.push(i2);
                                return 1;
                            }
                            if ((o1 instanceof Long) && (o2 instanceof DoubleSequence))
                            {
                                DoubleSequence i2 = (DoubleSequence) o2;
                                int i1 = (int) ((Long) o1).longValue();
                                dStack.push(i2.rotateLeft(i1));
                                return 1;
                            }
                            if ((o1 instanceof Long) && (o2 instanceof String))
                            {
                                String i2 = (String) o2;
                                int i1 = ((Long) o1).intValue();
                                dStack.push(Utilities.rotLeft(i2, i1));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ">>", false, "Rotate right",
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long))
                            {
                                long i2 = (Long) o2;
                                int i1 = (int) ((Long) o1).longValue();
                                i2 = Long.rotateRight(i2, i1);
                                dStack.push(i2);
                                return 1;
                            }
                            if ((o1 instanceof Long) && (o2 instanceof DoubleSequence))
                            {
                                DoubleSequence i2 = (DoubleSequence) o2;
                                int i1 = (int) ((Long) o1).longValue();
                                dStack.push(i2.rotateRight(i1));
                                return 1;
                            }
                            if ((o1 instanceof Long) && (o2 instanceof String))
                            {
                                String i2 = (String) o2;
                                int i1 = ((Long) o1).intValue();
                                dStack.push(Utilities.rotRight(i2, i1));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ".", false, "Pop TOS and print it",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.pop();
                            String outstr = _jforth.stackElementToString(o, _jforth.base);
                            if (outstr == null)
                            {
                                return 0;
                            }
                            _jforth._out.print(outstr);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "<.", false, "Restore last stack object",
                        (dStack, vStack) ->
                        {
                            if (!dStack.unpop())
                            {
                                _jforth._out.print("Nothing to do ...");
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ".v", false, "Show whole variable stack",
                        (dStack, vStack) ->
                        {
                            _jforth._out.print(_jforth.dictionary.variableList());
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ".s", false, "Show whole data stack",
                        (dStack, vStack) ->
                        {
                            for (Object o : dStack)
                            {
                                _jforth._out.print(_jforth.stackElementToString(o, _jforth.base) + " ");
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "cr", false, "Emit carriage return",
                        (dStack, vStack) ->
                        {
                            _jforth._out.println();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sp", false, "Emit single space",
                        (dStack, vStack) ->
                        {
                            _jforth._out.print(' ');
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "spaces", false, "Emit multiple spaces",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                long i1 = (Long) o1;
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < i1; i++)
                                {
                                    sb.append(" ");
                                }
                                _jforth._out.print(sb.toString());
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "binary", false, "Set number base to 2",
                        (dStack, vStack) ->
                        {
                            _jforth.base = 2;
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "decimal", false, "Set number base to 10",
                        (dStack, vStack) ->
                        {
                            _jforth.base = 10;
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "hex", false, "Set number base to 16",
                        (dStack, vStack) ->
                        {
                            _jforth.base = 16;
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "setbase", false, "Set a new number base",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                _jforth.base = (int) ((Long) o1).longValue();
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ":", false, "Begin word definition",
                        (dStack, vStack) ->
                        {
                            _jforth.compiling = true;
                            String name = _jforth.getNextToken();
                            if (name == null)
                            {
                                return 0;
                            }
                            _jforth.wordBeingDefined = new NonPrimitiveWord(name);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ";", true, "End word definition",
                        (dStack, vStack) ->
                        {
                            _jforth.compiling = false;
                            _jforth.dictionary.add(_jforth.wordBeingDefined);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "words", false, "Show all words",
                        (dStack, vStack) ->
                        {
                            dStack.push(_jforth.dictionary.toString(false));
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "wordsd", false, "Show words and description",
                        (dStack, vStack) ->
                        {
                            dStack.push(_jforth.dictionary.toString(true));
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "forget", true, "Delete word from dictionary",
                        (dStack, vStack) ->
                        {
                            String name = _jforth.getNextToken();
                            if (name == null)
                            {
                                return 0;
                            }
                            BaseWord bw;
                            try
                            {
                                bw = _jforth.dictionary.search(name);
                            }
                            catch (Exception ignore)
                            {
                                return 0;
                            }
                            if (bw != null)
                            {
                                if (!bw.isPrimitive)
                                {
                                    //dictionary.truncateList(bw);
                                    _jforth.dictionary.remove(bw);
                                    return 1;
                                }
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "constant", false, "create new Constant",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            String name = _jforth.getNextToken();
                            if (name == null)
                            {
                                return 0;
                            }
                            NonPrimitiveWord constant = new NonPrimitiveWord(name);
                            _jforth.dictionary.add(constant);
                            Object o1 = dStack.pop();
                            if (o1 instanceof String)
                            {
                                String stringConstant = (String) o1;
                                constant.addWord(new StringLiteral(stringConstant));
                            }
                            else if (o1 instanceof Long)
                            {
                                Long numericConstant = (Long) o1;
                                constant.addWord(new LongLiteral(numericConstant));
                            }
                            else if (o1 instanceof Double)
                            {
                                Double floatingPointConstant = (Double) o1;
                                constant.addWord(new DoubleLiteral(floatingPointConstant));
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
                        "variable", true, "Create new variable",
                        (dStack, vStack) ->
                        {
                            String name = _jforth.getNextToken();
                            if (name == null)
                            {
                                return 0;
                            }
                            StorageWord sw = new StorageWord(name, 1, false);
                            _jforth.dictionary.add(sw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ">r", false, "Put TOS to variable stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.pop();
                            vStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "r>", false, "Put variable on data stack",
                        (dStack, vStack) ->
                        {
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.pop();
                            dStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "r@", false, "Put variable on data stack",
                        (dStack, vStack) ->
                        {
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.peek();
                            dStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "!", false, "Store value into varable or array",
                        (dStack, vStack) ->
                        {
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.pop();
                            if (!(o instanceof StorageWord))
                            {
                                return 0;
                            }
                            StorageWord sw = (StorageWord) o;
                            int offset = 0;
                            if (!sw.isArray())
                            {
                                if (dStack.empty())
                                {
                                    return 0;
                                }
                            }
                            else
                            {
                                if (dStack.size() < 2)
                                {
                                    return 0;
                                }
                                Object off = dStack.pop();
                                if (!(off instanceof Long))
                                {
                                    return 0;
                                }
                                offset = (int) ((Long) off).longValue();
                            }
                            return sw.store(dStack.pop(), offset);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "+!", false, "Add value to variable",
                        (dStack, vStack) ->
                        {
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.pop();
                            if (!(o instanceof StorageWord))
                            {
                                return 0;
                            }
                            StorageWord sw = (StorageWord) o;
                            int offset = 0;
                            if (!sw.isArray())
                            {
                                if (dStack.empty())
                                {
                                    return 0;
                                }
                            }
                            else
                            {
                                if (dStack.size() < 2)
                                {
                                    return 0;
                                }
                                Object off = dStack.pop();
                                if (!(off instanceof Long))
                                {
                                    return 0;
                                }
                                offset = (int) ((Long) off).longValue();
                            }
                            return sw.plusStore(dStack.pop(), offset);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "@", false, "Put variable value on stack",
                        (dStack, vStack) ->
                        {
                            if (vStack.empty())
                            {
                                return 0;
                            }
                            Object o = vStack.pop();
                            if (!(o instanceof StorageWord))
                            {
                                return 0;
                            }
                            StorageWord sw = (StorageWord) o;
                            Object data;
                            if (!sw.isArray())
                            {
                                data = sw.fetch(0);
                                if (data == null)
                                {
                                    return 0;
                                }
                            }
                            else
                            {
                                if (dStack.empty())
                                {
                                    return 0;
                                }
                                Object off = dStack.pop();
                                if (!(off instanceof Long))
                                {
                                    return 0;
                                }
                                int offset = (int) ((Long) off).longValue();
                                data = sw.fetch(offset);
                                if (data == null)
                                {
                                    return 0;
                                }
                            }
                            dStack.push(data);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "array", false, "Create array",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.pop();
                            if (!(o instanceof Long))
                            {
                                return 0;
                            }
                            int size = (int) ((Long) o).longValue();
                            String name = _jforth.getNextToken();
                            if (name == null)
                            {
                                return 0;
                            }
                            StorageWord sw = new StorageWord(name, size, true);
                            _jforth.dictionary.add(sw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "round", false, "Round double value",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Double)
                            {
                                dStack.push(Math.round((Double) o1));
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "time", false, "Get a time string",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (!(o1 instanceof String))
                            {
                                return 0;
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat((String) o1);
                            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                            dStack.push(System.currentTimeMillis());
                            dStack.push(sdf.format(timestamp));
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sleep", false, "Sleep some milliseconds",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long l1 = Utilities.readLong(dStack);
                                Thread.sleep(l1);
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
                        "emit", false, "Emit single char to console",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                Long l = (Long) o1;
                                _jforth._out.print((char) (long) l);
                                return 1;
                            }
                            if (o1 instanceof String)
                            {
                                String str = (String) o1;
                                for (int s = 0; s < str.length(); s++)
                                {
                                    _jforth._out.print(str.charAt(s));
                                }
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "fraction", false, "Create a fraction from 2 Numbers",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                int o1 = (int)Utilities.readLong(dStack);
                                int o2 = (int)Utilities.readLong(dStack);
                                dStack.push(new Fraction(o1, o2));
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
                        "complex", false, "Create a complex from 2 numbers",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                double o1 = Utilities.readDouble(dStack);
                                double o2 = Utilities.readDouble(dStack);
                                dStack.push(new Complex (o1, o2));
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
                        "toLong", false, "Make long values of what is on the stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Double)
                            {
                                dStack.push(((Double) o1).longValue());
                            }
                            else if (o1 instanceof DoubleSequence)
                            {
                                int[] arr = ((DoubleSequence)o1).asIntArray();
                                long l = MyMath.fromBinaryListLong(arr);
                                dStack.push(l);
                            }
                            else if (o1 instanceof String)
                            {
                                dStack.push(Long.parseLong((String) o1));
                            }
                            else if (o1 instanceof Complex)
                            {
                                Complex oc = (Complex) o1;
                                dStack.push((long) oc.getReal());
                                dStack.push((long) oc.getImaginary());
                            }
                            else if (o1 instanceof Fraction)
                            {
                                Fraction oc = (Fraction) o1;
                                dStack.push((long) oc.getNumerator() / (long) oc.getDenominator());
                            }
                            else if (o1 instanceof BigInt)
                            {
                                BigInt oc = (BigInt) o1;
                                dStack.push(oc.longValue());
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
                        "toBig", false, "Make BigInt values of what is on the stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Double)
                            {
                                dStack.push(BigInt.apply (((Double) o1).longValue()));
                            }
                            else if (o1 instanceof DoubleSequence)
                            {
                                int[] arr = ((DoubleSequence)o1).asIntArray();
                                BigInt l = MyMath.fromBinaryListBig(arr);
                                dStack.push(l);
                            }
                            else if (o1 instanceof String)
                            {
                                dStack.push(BigInt.apply (((String) o1)));
                            }
                            else if (o1 instanceof Complex)
                            {
                                Complex oc = (Complex) o1;
                                dStack.push(BigInt.apply((long) oc.getReal()));
                                dStack.push(BigInt.apply((long) oc.getImaginary()));
                            }
                            else if (o1 instanceof Fraction)
                            {
                                Fraction oc = (Fraction) o1;
                                dStack.push(BigInt.apply((long) oc.getNumerator() / (long) oc.getDenominator()));
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
                        "toBits", false, "Make bit sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                BigInt l = Utilities.readBig(dStack);
                                dStack.push (DoubleSequence.makeBits(l));
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
                        "toDouble", false, "Make double value of what is on the stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                dStack.push((double) (Long) o1);
                            }
                            else if (o1 instanceof String)
                            {
                                dStack.push(Double.parseDouble((String) o1));
                            }
                            else if (o1 instanceof Complex)
                            {
                                Complex oc = (Complex) o1;
                                dStack.push(oc.getReal());
                                dStack.push(oc.getImaginary());
                            }
                            else if (o1 instanceof Fraction)
                            {
                                Fraction oc = (Fraction) o1;
                                dStack.push((double) oc.getNumerator() / (double) oc.getDenominator());
                            }
                            else if (o1 instanceof BigInt)
                            {
                                BigInt oc = (BigInt) o1;
                                dStack.push(oc.doubleValue());
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
                        "type", false, "Get type of TOS as string",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.peek();
                            dStack.push (o1.getClass().getSimpleName());
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toFraction", false, "Make fraction from value on the stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                dStack.push(new Fraction((double) (Long) o1));
                                return 1;
                            }
                            else if (o1 instanceof Double)
                            {
                                dStack.push(new Fraction((Double) o1));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toList", false, "Make list of what is on the stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof String)
                            {
                                String str = (String) o1;
                                DoubleSequence ds = new DoubleSequence();
                                for (int s = 0; s < str.length(); s++)
                                {
                                    ds = ds.add(str.charAt(s));
                                }
                                dStack.push(ds);
                                return 1;
                            }
                            if (!(o1 instanceof Long))
                            {
                                return 0;
                            }
                            long cnt = (Long) o1;
                            DoubleSequence seq = new DoubleSequence();
                            for (long n = 0; n < cnt; n++)
                            {
                                if (dStack.empty())
                                {
                                    break;
                                }
                                Object o2 = dStack.pop();
                                if (o2 instanceof Double)
                                {
                                    seq = seq.add((Double) o2);
                                }
                                else if (o2 instanceof Long)
                                {
                                    seq = seq.add((Long) o2);
                                }
                                else if (o2 instanceof DoubleSequence)
                                {
                                    seq = seq.add((DoubleSequence) o2);
                                }
                            }
                            dStack.push(seq);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toPoly", false, "Make polynomial of what is on the stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.pop();
                            if (!(o instanceof DoubleSequence))
                            {
                                return 0;
                            }
                            PolynomialFunction p =
                                    new PolynomialFunction(((DoubleSequence) o).asPrimitiveArray());
                            dStack.push(p);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "f'=", false, "Derive a polynomial",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.pop();
                            if (!(o instanceof PolynomialFunction))
                            {
                                return 0;
                            }
                            PolynomialFunction p = (PolynomialFunction) o;
                            dStack.push(p.polynomialDerivative());
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "Sf=", false, "Antiderive of a polynomial",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            Object o2;
                            Object o3;
                            PolynomialFunction p;
                            if (!dStack.isEmpty())
                            {
                                o2 = dStack.pop();
                                o3 = dStack.pop();
                                p = (PolynomialFunction) o3;
                            }
                            else
                            {
                                p = (PolynomialFunction) o;
                                dStack.push(Utilities.antiDerive(p));
                                return 1;
                            }
                            if (o2 instanceof Long)
                            {
                                o2 = ((Long) o2).doubleValue();
                            }
                            if (o instanceof Long)
                            {
                                o = ((Long) o).doubleValue();
                            }
                            if (!(o2 instanceof Double && o instanceof Double))
                            {
                                return 0;
                            }
                            SimpsonIntegrator si = new SimpsonIntegrator();
                            double d = si.integrate(1000, p,
                                    (Double) o2, (Double) o);
                            dStack.push(d);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "x=", false, "Solve a polynomial",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                double d1 = Utilities.readDouble(dStack);
                                PolynomialFunction p1 = Utilities.readPoly(dStack);
                                dStack.push(p1.value(d1));
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
                        "apply", false, "Apply polynomial to sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence s1 = Utilities.readDoubleSequence(dStack);
                                PolynomialFunction p1 = Utilities.readPoly(dStack);
                                dStack.push(s1.apply(p1));
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
                        "toString", false, "Make string of what is on the stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                dStack.push(Long.toString((Long) o1, _jforth.base).toUpperCase());
                            }
                            else if (o1 instanceof Double)
                            {
                                dStack.push(Double.toString((Double) o1));
                            }
                            else if (o1 instanceof Fraction)
                            {
                                dStack.push(Utilities.formatFraction((Fraction) o1));
                            }
                            else if (o1 instanceof Complex)
                            {
                                dStack.push(Utilities.formatComplex((Complex) o1));
                            }
                            else if (o1 instanceof DoubleSequence)
                            {
                                dStack.push(((DoubleSequence) o1).asString());
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
                        "length", false, "Get length of what is on the stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof String)
                            {
                                dStack.push((long) ((String) o1).length());
                                return 1;
                            }
                            if (o1 instanceof DoubleSequence)
                            {
                                dStack.push((long) ((DoubleSequence) o1).length());
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "subSeq", false, "Subsequence of string or list",
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 3)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            Object o3 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long) && (o3 instanceof String))
                            {
                                int i1 = (int) ((Long) o1).longValue();
                                int i2 = (int) ((Long) o2).longValue();
                                dStack.push(((String) o3).substring(i2, i1));
                                return 1;
                            }
                            if ((o1 instanceof Long) && (o2 instanceof Long) && (o3 instanceof DoubleSequence))
                            {
                                int i1 = (int) ((Long) o1).longValue();
                                int i2 = (int) ((Long) o2).longValue();
                                dStack.push(((DoubleSequence) o3).subList(i2, i1));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "E", false, "Natural logarithm base",
                        (dStack, vStack) ->
                        {
                            dStack.push(Math.E);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "PI", false, "Circle constant PI",
                        (dStack, vStack) ->
                        {
                            dStack.push(Math.PI);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sqrt", false, "Square root",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.sqrt());
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
                        "gcd", false, "Greates common divisor",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Long o1 = Utilities.readLong(dStack);
                                Long o2 = Utilities.readLong(dStack);
                                dStack.push(ArithmeticUtils.gcd(o1, o2));
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
                        "lcm", false, "Least common multiple",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Long o1 = Utilities.readLong(dStack);
                                Long o2 = Utilities.readLong(dStack);
                                dStack.push(ArithmeticUtils.lcm(o1, o2));
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
                        "factor", false, "Prime factorisation",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                BigInt o1 = Utilities.readBig(dStack);
                                dStack.push (DoubleSequence.primes(o1));
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
                        "pow", false, "Exponentation",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Object o1 = dStack.pop();
                                Object o2 = dStack.pop();
                                try
                                {
                                    dStack.push (Calculator.doCalcBigInt(o2, o1, Calculator::pow));
                                    return 1;
                                }
                                catch (Exception unused)
                                {
                                    //e.printStackTrace();
                                }
                                try
                                {
                                    dStack.push (Calculator.doCalcComplex(o2, o1, Complex::pow));
                                    return 1;
                                }
                                catch (Exception unused)
                                {
                                    //e.printStackTrace();
                                }
                                if (o1 instanceof Long && o2 instanceof Long)
                                {
                                    Long l1 = (Long)o1;
                                    Long l2 = (Long)o2;
                                    dStack.push (MyMath.bigPow(l2, l1.intValue()));
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
                        "fib", false, "Fibonacci number",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long l = Utilities.readLong(dStack);
                                dStack.push (MyMath.fibonacci(l));
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
                        "ln", false, "Natural logarithm",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex oc = Utilities.readComplex(dStack);
                                double re = oc.getReal() * oc.getReal() + oc.getImaginary() * oc.getImaginary();
                                re = Math.log(re) / 2.0;
                                double im = oc.getImaginary() / oc.getReal();
                                im = Math.atan(im);
                                dStack.push(new Complex(re, im));
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
                        "fact", false, "Factorial",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long)
                            {
                                Long ol = (Long) o1;
                                dStack.push (MyMath.factorial(ol));
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "log10", false, "Logarithm to base 10",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof Double)
                            {
                                dStack.push(Math.log10((Double) o1));
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "exp", false, "E^x",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.exp());
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
                        "sin", false, "Sine",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.sin());
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
                        "cos", false, "Cosine",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.cos());
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
                        "tan", false, "Tangent",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push (o1.tan());
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
                        "asin", false, "Inverse sine",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.asin());
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
                        "acos", false, "Inverse cosine",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.acos());
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
                        "atan", false, "Inverse tangent",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.atan());
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
                        "atan2", false, "Second arctan, see: https://de.wikipedia.org/wiki/Arctan2",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Double o1 = Utilities.readDouble(dStack);
                                Double o2 = Utilities.readDouble(dStack);
                                dStack.push(Math.atan2(o2, o1));
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
                        "sinh", false, "Sinus hyperbolicus",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.sinh());
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
                        "cosh", false, "Cosinus hyperbolicus",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.cosh());
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
                        "tanh", false, "Tangent hyperbolicus",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.tanh());
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
                        "load", false, "load program file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof String)
                            {
                                String fileName = (String) o1;
                                return _jforth.fileLoad(fileName);
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "saveHist", false, "Save history",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                _jforth.history.save();
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "loadHist", false, "Load history",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                _jforth.history.load();
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "playHist", false, "Execute History",
                        (dStack, vStack) ->
                        {
                            _jforth.play();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "clearHist", false, "Clear History",
                        (dStack, vStack) ->
                        {
                            _jforth.history.clear();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gaussian", false, "Gaussian random number",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.pop();
                            if (o instanceof Long)
                            {
                                long mult = (Long) o;
                                double number = _jforth.random.nextGaussian() * mult;
                                dStack.push((long) number);
                            }
                            else if (o instanceof Double)
                            {
                                double mult = (Double) o;
                                double number = _jforth.random.nextGaussian() * mult;
                                dStack.push(number);
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
                        "random", false, "Pseudo random number",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.pop();
                            if (o instanceof Long)
                            {
                                long mult = (Long) o;
                                double number = _jforth.random.nextDouble() * mult;
                                dStack.push((long) number);
                            }
                            else if (o instanceof Double)
                            {
                                double mult = (Double) o;
                                double number = _jforth.random.nextDouble() * mult;
                                dStack.push(number);
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
                        "openByteReader", false, "Open file for reading",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof String)
                            {
                                try
                                {
                                    File f = new File((String) o1);
                                    dStack.push(new FileInputStream(f));
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                    return 0;
                                }
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "readByte", false, "Read byte from file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof FileInputStream)
                            {
                                try
                                {
                                    dStack.push((long) (((FileInputStream) o1).read()));
                                    return 1;
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                }
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "dir", false, "Get directory",
                        (dStack, vStack) ->
                        {
                            String path = ".";
                            if (!dStack.empty())
                            {
                                Object o = dStack.pop();
                                if (o instanceof String)
                                {
                                    path = (String) o;
                                }
                                else
                                {
                                    dStack.push(o);
                                }
                            }
                            String s = Utilities.dir(path);
                            dStack.push(s);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unlink", false, "Delete file",
                        (dStack, vStack) ->
                        {
                            String o = null;
                            try
                            {
                                o = Utilities.readString(dStack);
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                            return Utilities.del (o) ? 1 : 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "key", true, "Get key from keyboard",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                int c = RawConsoleInput.read(true);
                                RawConsoleInput.resetConsoleMode();
                                dStack.push((long) c);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "clear", true, "Clear the stack",
                        (dStack, vStack) ->
                        {
                            dStack.clear();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "pick", true, "Get value from arbitrary Positon and place it on TOS",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.pop();
                            if (!(o instanceof Long))
                            {
                                return 0;
                            }
                            Object n = dStack.get(dStack.size() - ((Long) o).intValue() - 1);
                            if (n == null)
                            {
                                return 0;
                            }
                            dStack.push(n);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "roll", true,
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o = dStack.pop();
                            if (!(o instanceof Long))
                            {
                                return 0;
                            }
                            Object n = dStack.remove(dStack.size() - ((Long) o).intValue() - 1);
                            if (n == null)
                            {
                                return 0;
                            }
                            dStack.push(n);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "accept", true, "Read string from keyboard",
                        (dStack, vStack) ->
                        {
                            long l;
                            if (dStack.empty())
                            {
                                l = -1;
                            }
                            else
                            {
                                try
                                {
                                    l = Utilities.readLong(dStack);
                                }
                                catch (Exception e)
                                {
                                    return 0;
                                }
                                if (l < 0)
                                {
                                    return 0;
                                }
                            }
                            StringBuilder s = new StringBuilder();
                            try
                            {
                                while (true)
                                {
                                    char c = (char) RawConsoleInput.read(true);
                                    if (l > 0)
                                    {
                                        l--;
                                    }
                                    if (c == '\r' || l == 0)
                                    {
                                        break;
                                    }
                                    s.append(c);
                                    _jforth._out.print('-');
                                    _jforth._out.flush();
                                }
                                RawConsoleInput.resetConsoleMode();
                                dStack.push(s.toString());
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
                        "closeByteReader", false, "Close file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof FileInputStream)
                            {
                                try
                                {
                                    ((FileInputStream) o1).close();
                                    return 1;
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                    return 0;
                                }
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "openReader", false, "Open file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof String)
                            {
                                try
                                {
                                    File f = new File((String) o1);
                                    dStack.push(new BufferedReader(new FileReader(f)));
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                    return 0;
                                }
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "readLine", false, "Read line from file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof BufferedReader)
                            {
                                try
                                {
                                    String s = ((BufferedReader) o1).readLine();
                                    if (s != null)
                                    {
                                        dStack.push(s);
                                        dStack.push("");
                                    }
                                    else
                                    {
                                        dStack.push("EOF");
                                    }
                                    return 1;
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                    return 0;
                                }
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "closeReader", false, "Close file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof BufferedReader)
                            {
                                try
                                {
                                    ((BufferedReader) o1).close();
                                    return 1;
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                    return 0;
                                }
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "openWriter", false, "Open file for Writing",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof String)
                            {
                                try
                                {
                                    File f = new File((String) o1);
                                    dStack.push(new PrintStream(f));
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                    return 0;
                                }
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "writeString", false, "Write string to file",
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 2)
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if (o1 instanceof PrintStream)
                            {
                                if (o2 instanceof String)
                                {
                                    ((PrintStream) o1).print((String) o2);
                                }
                                else if (o2 instanceof Long)
                                {
                                    ((PrintStream) o1).print(Long.toString((Long) o2, _jforth.base).toUpperCase());
                                }
                                else if (o2 instanceof Double)
                                {
                                    ((PrintStream) o1).print(Double.toString((Double) o2));
                                }
                                else
                                {
                                    return 0;
                                }
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "writeEol", false, "Write string end into file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof PrintStream)
                            {
                                ((PrintStream) o1).println();
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "writeByte", false, "Write byte into file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof PrintStream) && (o2 instanceof Long))
                            {
                                ((PrintStream) o1).write((byte) (((Long) o2).longValue()));
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "closeWriter", false, "Close file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty())
                            {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof PrintStream)
                            {
                                ((PrintStream) o1).close();
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "bye", false, "End the Forth interpreter",
                        (dStack, vStack) ->
                        {
                            System.exit(0);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sort", false, "Sort a Sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                dStack.push(o.sort());
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
                        "rev", false, "Reverse a sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                dStack.push(o.reverse());
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
                        "shuffle", false, "Random shuffles a sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                dStack.push(o.shuffle());
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
                        "sum", false, "Add all elements together",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                dStack.push(o.sum());
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
                        "sumq", false, "Make sum of squares",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                dStack.push(o.sumQ());
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
                        "prod", false, "Product of all values",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                dStack.push(o.prod());
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
                        "intersect", false, "Make intersection of 2 sequences",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence o1 = Utilities.readDoubleSequence(dStack);
                                DoubleSequence o2 = Utilities.readDoubleSequence(dStack);
                                dStack.push(o1.intersect(o2));
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
                        "unique", false, "Only keep unique elements of sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence o1 = Utilities.readDoubleSequence(dStack);
                                dStack.push(o1.unique());
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
                        "lpick", false, "Get one Element from sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long l1 = Utilities.readLong(dStack);
                                DoubleSequence ds = Utilities.readDoubleSequence(dStack);
                                dStack.push(ds.pick((int) l1));
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
                        "http", false, "run web server",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long o = Utilities.readLong(dStack);
                                SimpleWebserver.start((int)o);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));


    }
}
