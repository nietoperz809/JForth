import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

final public class AllWords
{
    private final JForth _jforth;
    public final BaseWord[] forthWords =
            {
                    new PrimitiveWord
                            (
                                    "(", true,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            String token = _jforth.getNextToken();
                                            while ((token != null) && (!token.equals(")")))
                                            {
                                                token = _jforth.getNextToken();
                                            }
                                            if (token != null)
                                            {
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "'", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "execute", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "if", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "then", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "else", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "do", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "i", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (vStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = vStack.peek();
                                            dStack.push(o);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "j", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "leave", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "loop", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "+loop", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "begin", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (!_jforth.compiling)
                                            {
                                                return 1;
                                            }
                                            int index = _jforth.wordBeingDefined.getNextWordIndex();
                                            vStack.push((long) index);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "until", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "dup", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = dStack.peek();
                                            if (o instanceof DoubleSequence)
                                            {
                                                DoubleSequence s2 = new DoubleSequence((DoubleSequence) o);
                                                dStack.push(s2);
                                            }
                                            else
                                            {
                                                dStack.push(o);
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "drop", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            dStack.pop();
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "swap", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "over", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "rot", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "pick", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Long)
                                            {
                                                long i1 = (Long) o1;
                                                if ((i1 < 0) || (i1 >= dStack.size()))
                                                {
                                                    return 0;
                                                }
                                                else
                                                {
                                                    dStack.push(dStack.get((int) i1));
                                                    return 1;
                                                }
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "depth", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            Long i = (long) dStack.size();
                                            dStack.push(i);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "<", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "=", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    ">", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "0<", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "0=", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "0>", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "not", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "true", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            dStack.push(JForth.TRUE);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "false", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            dStack.push(JForth.FALSE);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "+", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                                i2 += i1;
                                                dStack.push(i2);
                                            }
                                            else if ((o1 instanceof Double) && (o2 instanceof Double))
                                            {
                                                double d1 = (Double) o1;
                                                double d2 = (Double) o2;
                                                d2 += d1;
                                                dStack.push(d2);
                                            }
                                            else if ((o1 instanceof Complex) && (o2 instanceof Complex))
                                            {
                                                Complex d1 = (Complex) o1;
                                                Complex d2 = (Complex) o2;
                                                dStack.push(d2.add(d1));
                                            }
                                            else if ((o1 instanceof Fraction) && (o2 instanceof Fraction))
                                            {
                                                Fraction d1 = (Fraction) o1;
                                                Fraction d2 = (Fraction) o2;
                                                dStack.push(d2.add(d1));
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
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "-", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                                i2 -= i1;
                                                dStack.push(i2);
                                            }
                                            else if ((o1 instanceof Complex) && (o2 instanceof Complex))
                                            {
                                                Complex d1 = (Complex) o1;
                                                Complex d2 = (Complex) o2;
                                                dStack.push(d2.subtract(d1));
                                            }
                                            else if ((o1 instanceof Fraction) && (o2 instanceof Fraction))
                                            {
                                                Fraction d1 = (Fraction) o1;
                                                Fraction d2 = (Fraction) o2;
                                                dStack.push(d2.subtract(d1));
                                            }
                                            else if ((o1 instanceof Double) && (o2 instanceof Double))
                                            {
                                                double d1 = (Double) o1;
                                                double d2 = (Double) o2;
                                                d2 -= d1;
                                                dStack.push(d2);
                                            }
                                            else if ((o1 instanceof DoubleSequence) && (o2 instanceof DoubleSequence))
                                            {
                                                DoubleSequence d1 = (DoubleSequence) o1;
                                                DoubleSequence d2 = (DoubleSequence) o2;
                                                dStack.push(d2.difference(d1));
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "1+", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "1-", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "2+", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "2-", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "*", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                                i2 *= i1;
                                                dStack.push(i2);
                                            }
                                            else if ((o1 instanceof Complex) && (o2 instanceof Complex))
                                            {
                                                Complex d1 = (Complex) o1;
                                                Complex d2 = (Complex) o2;
                                                dStack.push(d2.multiply(d1));
                                            }
                                            else if ((o1 instanceof Fraction) && (o2 instanceof Fraction))
                                            {
                                                Fraction d1 = (Fraction) o1;
                                                Fraction d2 = (Fraction) o2;
                                                dStack.push(d2.multiply(d1));
                                            }
                                            else if ((o1 instanceof Double) && (o2 instanceof Double))
                                            {
                                                double d1 = (Double) o1;
                                                double d2 = (Double) o2;
                                                d2 *= d1;
                                                dStack.push(d2);
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "/", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                                i2 /= i1;
                                                dStack.push(i2);
                                            }
                                            else if ((o1 instanceof Complex) && (o2 instanceof Complex))
                                            {
                                                Complex d1 = (Complex) o1;
                                                Complex d2 = (Complex) o2;
                                                dStack.push(d2.divide(d1));
                                            }
                                            else if ((o1 instanceof Fraction) && (o2 instanceof Fraction))
                                            {
                                                Fraction d1 = (Fraction) o1;
                                                Fraction d2 = (Fraction) o2;
                                                dStack.push(d2.divide(d1));
                                            }
                                            else if ((o1 instanceof Double) && (o2 instanceof Double))
                                            {
                                                double d1 = (Double) o1;
                                                double d2 = (Double) o2;
                                                d2 /= d1;
                                                dStack.push(d2);
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "mod", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                                i2 %= i1;
                                                dStack.push(i2);
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "/mod", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                                long i3 = i1 % i2;
                                                long i4 = i1 / i2;
                                                dStack.push(i3);
                                                dStack.push(i4);
                                                return 1;
                                            }
                                            return 0;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "max", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "min", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "abs", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord   //
                            (
                                    "phi", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord   //
                            (
                                    "conj", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord   //
                            (
                                    "split", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                                dStack.push(d1.getNumerator());
                                                dStack.push(d1.getDenominator());
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "and", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "or", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "xor", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "<<", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                            return 0;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    ">>", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                            return 0;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    ".", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = dStack.pop();
                                            String outstr = JForth.stackElementToString(o, _jforth.base);
                                            if (outstr == null)
                                            {
                                                return 0;
                                            }
                                            _jforth._out.print(outstr);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    ".s", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            for (Object o : dStack)
                                            {
                                                _jforth._out.print(JForth.stackElementToString(o, _jforth.base) + " ");
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "cr", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            _jforth._out.println();
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "sp", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            _jforth._out.print(' ');
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "spaces", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "binary", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            _jforth.base = 2;
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "decimal", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            _jforth.base = 10;
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "hex", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            _jforth.base = 16;
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "setbase", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    ":", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    ";", true,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            _jforth.compiling = false;
                                            _jforth.dictionary.add(_jforth.wordBeingDefined);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "words", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            _jforth._out.print(_jforth.dictionary.toString(false));
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "wordsd", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            _jforth._out.println(_jforth.dictionary.toString(true));
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "forget", true,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "constant", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "variable", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    ">r", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = dStack.pop();
                                            vStack.push(o);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "r>", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (vStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = vStack.pop();
                                            dStack.push(o);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "r@", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (vStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = vStack.peek();
                                            dStack.push(o);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "!", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "+!", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "@", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "array", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "round", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "time", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "sleep", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (!(o1 instanceof Long))
                                            {
                                                return 0;
                                            }
                                            try
                                            {
                                                Thread.sleep((Long) o1);
                                            }
                                            catch (InterruptedException e)
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "emit", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "fraction", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            Object o1 = dStack.pop();
                                            Object o2 = dStack.pop();
                                            Fraction f;
                                            if (o1 instanceof Double && o2 instanceof Double)
                                            {
                                                f = new Fraction(((Double) o1).intValue(), ((Double) o2).intValue());
                                            }
                                            else if (o1 instanceof Long && o2 instanceof Long)
                                            {
                                                f = new Fraction(((Long) o1).intValue(), ((Long) o2).intValue());
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                            dStack.push(f);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "complex", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            Object o1 = dStack.pop();
                                            Object o2 = dStack.pop();
                                            Complex f;
                                            if (o1 instanceof Double && o2 instanceof Double)
                                            {
                                                f = new Complex((Double) o1, (Double) o2);
                                            }
                                            else if (o1 instanceof Long && o2 instanceof Long)
                                            {
                                                f = new Complex(((Long) o1).doubleValue(), ((Long) o2).doubleValue());
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                            dStack.push(f);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "toLong", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "toDouble", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "toFraction", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "toList", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Long)
                                            {
                                                dStack.push(new DoubleSequence((Long) o1));
                                                return 1;
                                            }
                                            else if (o1 instanceof Double)
                                            {
                                                dStack.push(new DoubleSequence(((Double) o1).longValue()));
                                                return 1;
                                            }
                                            return 0;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "toString", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                                dStack.push(((DoubleSequence) o1).toString());
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "length", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "subString", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "E", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            dStack.push(Math.E);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "PI", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            dStack.push(Math.PI);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "sqrt", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.sqrt((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                Complex oc = (Complex) o1;
                                                dStack.push(oc.sqrt());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "pow", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.size() < 2)
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            Object o2 = dStack.pop();
                                            if ((o1 instanceof Double) && (o2 instanceof Double))
                                            {
                                                double d1 = (Double) o1;
                                                double d2 = (Double) o2;
                                                dStack.push(Math.pow(d2, d1));
                                            }
                                            else if ((o1 instanceof Long) && (o2 instanceof Long))
                                            {
                                                Long d1 = (Long) o1;
                                                Long d2 = (Long) o2;
                                                dStack.push((long) Math.pow(d2, d1));
                                            }
                                            else if ((o1 instanceof Complex) && (o2 instanceof Complex))
                                            {
                                                Complex d1 = (Complex) o1;
                                                Complex d2 = (Complex) o2;
                                                dStack.push(d2.pow(d1));
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "ln", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.log((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                Complex oc = (Complex) o1;
                                                double re = oc.getReal() * oc.getReal() + oc.getImaginary() * oc.getImaginary();
                                                re = Math.log(re) / 2.0;
                                                double im = oc.getImaginary() / oc.getReal();
                                                im = Math.atan(im);
                                                dStack.push(new Complex(re, im));
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "factorial", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Long)
                                            {
                                                Long ol = (Long) o1;
                                                double fact = 1;
                                                for (long i = 1; i <= ol; i++)
                                                {
                                                    fact = fact * i;
                                                }
                                                dStack.push(fact);
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "log10", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "exp", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.exp((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                Complex oc = (Complex) o1;
                                                dStack.push(oc.exp());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "sin", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.sin((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                dStack.push(((Complex) o1).sin());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "cos", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.cos((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                dStack.push(((Complex) o1).cos());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "tan", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.tan((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                dStack.push(((Complex) o1).tan());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "asin", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.asin((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                dStack.push(((Complex) o1).asin());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "acos", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.acos((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                dStack.push(((Complex) o1).acos());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "atan", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.atan((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                Complex oc = (Complex) o1;
                                                dStack.push(oc.atan());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "atan2", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.size() < 2)
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            Object o2 = dStack.pop();
                                            if ((o1 instanceof Double) && (o2 instanceof Double))
                                            {
                                                double d1 = (Double) o1;
                                                double d2 = (Double) o2;
                                                dStack.push(Math.atan2(d2, d1));
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "sinh", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.sinh((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                dStack.push(((Complex) o1).sinh());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "cosh", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.cosh((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                dStack.push(((Complex) o1).cosh());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "tanh", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (o1 instanceof Double)
                                            {
                                                dStack.push(Math.tanh((Double) o1));
                                                return 1;
                                            }
                                            if (o1 instanceof Complex)
                                            {
                                                dStack.push(((Complex) o1).tanh());
                                                return 1;
                                            }
                                            else
                                            {
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "load", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "saveState", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            try
                                            {
                                                JForth.save("state", _jforth);
                                            }
                                            catch (Exception ex)
                                            {
                                                return 0;
                                            }
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "gaussian", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "random", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "openByteReader", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "readByte", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "dir", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "unlink", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = dStack.pop();
                                            if (!(o instanceof String))
                                            {
                                                return 0;
                                            }
                                            return Utilities.del((String) o) ? 1 : 0;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "key", true,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "accept", true,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            long l;
                                            if (dStack.empty())
                                            {
                                                l = -1;
                                            }
                                            else
                                            {
                                                Object o = dStack.pop();
                                                if (!(o instanceof Long))
                                                {
                                                    return 0;
                                                }
                                                l = (Long) o;
                                                if (l < 0)
                                                {
                                                    return 0;
                                                }
                                            }
                                            String s = "";
                                            try
                                            {
                                                if (l == -1)
                                                {
                                                    while (true)
                                                    {
                                                        char c = (char) RawConsoleInput.read(true);
                                                        if (c == '\r')
                                                        {
                                                            break;
                                                        }
                                                        s += c;
                                                        _jforth._out.print('-');
                                                        _jforth._out.flush();
                                                    }
                                                }
                                                else
                                                {
                                                    while (l-- != 0)
                                                    {
                                                        s += (char) RawConsoleInput.read(true);
                                                        _jforth._out.print('-');
                                                        _jforth._out.flush();
                                                    }
                                                }
                                                RawConsoleInput.resetConsoleMode();
                                                dStack.push(s);
                                                return 1;
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                                return 0;
                                            }
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "closeByteReader", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "openReader", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "readLine", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "closeReader", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "openWriter", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "writeString", false,
                                    new ExecuteIF()
                                    {
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "writeEol", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "writeByte", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "closeWriter", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
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
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "bye", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            System.exit(0);
                                            return 1;
                                        }
                                    }
                            ),
                    new PrimitiveWord
                            (
                                    "sort", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = dStack.pop();
                                            if (o instanceof DoubleSequence)
                                            {
                                                dStack.push(((DoubleSequence) o).sort());
                                                return 1;
                                            }
                                            return 0;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "rev", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = dStack.pop();
                                            if (o instanceof DoubleSequence)
                                            {
                                                DoubleSequence l = (DoubleSequence) o;
                                                dStack.push(l.reverse());
                                                return 1;
                                            }
                                            return 0;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "shuffle", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o = dStack.pop();
                                            if (o instanceof DoubleSequence)
                                            {
                                                DoubleSequence l = (DoubleSequence) o;
                                                dStack.push(l.shuffle());
                                                return 1;
                                            }
                                            return 0;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "intersect", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (!(o1 instanceof DoubleSequence))
                                            {
                                                return 0;
                                            }
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o2 = dStack.pop();
                                            if (!(o2 instanceof DoubleSequence))
                                            {
                                                return 0;
                                            }
                                            DoubleSequence l = ((DoubleSequence) o1).intersect((DoubleSequence) o2);
                                            dStack.push(l);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "unique", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (!(o1 instanceof DoubleSequence))
                                            {
                                                return 0;
                                            }
                                            DoubleSequence l = ((DoubleSequence) o1).unique();
                                            dStack.push(l);
                                            return 1;
                                        }
                                    }
                            ),

                    new PrimitiveWord
                            (
                                    "lpick", false,
                                    new ExecuteIF()
                                    {
                                        @Override
                                        public int execute (OStack dStack, OStack vStack)
                                        {
                                            if (dStack.empty())
                                            {
                                                return 0;
                                            }
                                            Object o1 = dStack.pop();
                                            if (!(o1 instanceof Long))
                                            {
                                                return 0;
                                            }
                                            Object o2 = dStack.pop();
                                            if (!(o2 instanceof DoubleSequence))
                                            {
                                                return 0;
                                            }
                                            dStack.push(((DoubleSequence) o2).pick(((Long) o1).intValue()));
                                            return 1;
                                        }
                                    }
                            ),
            };

    public AllWords (JForth jf)
    {
        this._jforth = jf;
    }
}
