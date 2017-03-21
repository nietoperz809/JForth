import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class JForth
{
  private static final String PROMPT = "\n> ";
  private static final String OK = " OK";
  static final Long TRUE  = 1L;
  private static final Long FALSE = 0L;
  private static final int HISTORY_LENGTH = 25;

  private final OStack dStack = new OStack();
  private final OStack vStack = new OStack();
  private final WordsList dictionary = new WordsList();
  private boolean compiling;
  private int base;
  private StreamTokenizer st = null;
  private NonPrimitiveWord wordBeingDefined = null;
  private final Random random;
  private final History history;

  private final BaseWord [] forthWords =
  {
    new PrimitiveWord
    (
      "(", true,
            (dStack, vStack) ->
            {
              String token = getNextToken();
              while ((token != null) && (!token.equals(")")))
                token = getNextToken();
              if (token != null)
                return 1;
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "'", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (compiling)
            return 1;
          String name = getNextToken();
          if (name == null)
            return 0;
          BaseWord bw = null;
          try
          {
            bw = dictionary.search(name);
          }
          catch (Exception ignore) {}
          if (bw != null)
          {
            dStack.push(bw);
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "execute", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o = dStack.pop();
              if (o instanceof BaseWord)
              {
                BaseWord bw = (BaseWord) o;
                return bw.execute(dStack, vStack);
              }
              else
               return 0;
           }
    ),

    new PrimitiveWord
    (
      "if", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (!compiling)
            return 1;
          int currentIndex = wordBeingDefined.getNextWordIndex();
          IfControlWord ifcw = new IfControlWord(currentIndex);
          wordBeingDefined.addWord(ifcw);
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
        public int execute(OStack dStack, OStack vStack)
        {
          if (!compiling)
            return 1;
          if (vStack.empty())
            return 0;
          Object o = vStack.pop();
          int thenIndex = wordBeingDefined.getNextWordIndex();
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
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "else", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (!compiling)
            return 1;
          if (vStack.empty())
            return 0;
          Object o = vStack.peek();
          if (o instanceof IfControlWord)
          {
            int elseIndex = wordBeingDefined.getNextWordIndex() + 1;
            ElseControlWord ecw = new ElseControlWord(elseIndex);
            wordBeingDefined.addWord(ecw);
            vStack.push(ecw);
            ((IfControlWord) o).setElseIndex(elseIndex);
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "do", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (!compiling)
            return 1;
          DoLoopControlWord dlcw = new DoLoopControlWord();
          wordBeingDefined.addWord(dlcw);
          int index = wordBeingDefined.getNextWordIndex();
          vStack.push((long) index);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "i", false,
            (dStack, vStack) ->
            {
              if (vStack.empty())
                return 0;
              Object o = vStack.peek();
              dStack.push(o);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "j", false,
            (dStack, vStack) ->
            {
              if (vStack.size() < 4)
                return 0;
              Object o1 = vStack.pop();
              Object o2 = vStack.pop();
              Object o3 = vStack.peek();
              dStack.push(o3);
              vStack.push(o2);
              vStack.push(o1);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "leave", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (!compiling)
            return 1;
          if (vStack.size() < 2)
            return 0;
          LeaveLoopControlWord llcw = new LeaveLoopControlWord();
          wordBeingDefined.addWord(llcw);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "loop", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (!compiling)
            return 1;
          if (vStack.empty())
            return 0;
          Object o = vStack.pop();
          if (!(o instanceof Long))
            return 0;
          int beginIndex = ((Long) o).intValue();
          int endIndex = wordBeingDefined.getNextWordIndex();
          int increment = beginIndex - endIndex;
          LoopControlWord lcw = new LoopControlWord(increment);
          wordBeingDefined.addWord(lcw);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "+loop", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (!compiling)
            return 1;
          if (vStack.empty())
            return 0;
          Object o = vStack.pop();
          if (!(o instanceof Long))
            return 0;
          int beginIndex = ((Long) o).intValue();
          int endIndex = wordBeingDefined.getNextWordIndex();
          int increment = beginIndex - endIndex;
          PlusLoopControlWord plcw = new PlusLoopControlWord(increment);
          wordBeingDefined.addWord(plcw);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "begin", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (!compiling)
            return 1;
          int index = wordBeingDefined.getNextWordIndex();
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
        public int execute(OStack dStack, OStack vStack)
        {
          if (!compiling)
            return 1;
          if (vStack.empty())
            return 0;
          Object o = vStack.pop();
          if (!(o instanceof Long))
            return 0;
          int beginIndex = ((Long) o).intValue();
          int endIndex = wordBeingDefined.getNextWordIndex();
          int increment = beginIndex - endIndex;
          EndLoopControlWord ecw = new EndLoopControlWord(increment);
          wordBeingDefined.addWord(ecw);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "dup", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o = dStack.peek();
              dStack.push(o);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "drop", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              dStack.pop();
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "swap", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              dStack.push(o1);
              dStack.push(o2);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "over", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              dStack.push(o2);
              dStack.push(o1);
              dStack.push(o2);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "rot", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 3)
                return 0;
              Object o3 = dStack.pop();
              Object o2 = dStack.pop();
              Object o1 = dStack.pop();
              dStack.push(o2);
              dStack.push(o3);
              dStack.push(o1);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "pick", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                if ((i1 < 0) || (i1 >= dStack.size()))
                  return 0;
                else
                {
                  dStack.push(dStack.get((int)i1));
                  return 1;
                }
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "depth", false,
            (dStack, vStack) ->
            {
              Long i = (long) dStack.size();
              dStack.push(i);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "<", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o2 = dStack.pop();
              Object o1 = dStack.pop();
              if ((o1 instanceof Long) && (o2 instanceof Long))
              {
                long i1 = (Long) o1;
                long i2 = (Long) o2;
                if (i1 < i2)
                  dStack.push(TRUE);
                else
                  dStack.push(FALSE);
              }
              else if ((o1 instanceof Double) && (o2 instanceof Double))
              {
                double d1 = (Double) o1;
                double d2 = (Double) o2;
                if (d1 < d2)
                  dStack.push(TRUE);
                else
                  dStack.push(FALSE);
              }
              else if ((o1 instanceof String) && (o2 instanceof String))
              {
                String s1 = (String) o1;
                String s2 = (String) o2;
                int result = s1.compareTo(s2);
                if (result < 0)
                  dStack.push(TRUE);
                else
                  dStack.push(FALSE);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "=", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o2 = dStack.pop();
              Object o1 = dStack.pop();
              if ((o1 instanceof Long) && (o2 instanceof Long))
              {
                long i1 = (Long) o1;
                long i2 = (Long) o2;
                if (i1 == i2)
                  dStack.push(TRUE);
                else
                  dStack.push(FALSE);
              }
              else if ((o1 instanceof Double) && (o2 instanceof Double))
              {
                double d1 = (Double) o1;
                double d2 = (Double) o2;
                if (d1 == d2)
                  dStack.push(TRUE);
                else
                  dStack.push(FALSE);
              }
              else if ((o1 instanceof String) && (o2 instanceof String))
              {
                String s1 = (String) o1;
                String s2 = (String) o2;
                int result = s1.compareTo(s2);
                if (result == 0)
                  dStack.push(TRUE);
                else
                  dStack.push(FALSE);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      ">", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o2 = dStack.pop();
              Object o1 = dStack.pop();
              if ((o1 instanceof Long) && (o2 instanceof Long))
              {
                long i1 = (Long) o1;
                long i2 = (Long) o2;
                if (i1 > i2)
                  dStack.push(TRUE);
                else
                  dStack.push(FALSE);
              }
              else if ((o1 instanceof Double) && (o2 instanceof Double))
              {
                double d1 = (Double) o1;
                double d2 = (Double) o2;
                if (d1 > d2)
                  dStack.push(TRUE);
                else
                  dStack.push(FALSE);
              }
              else if ((o1 instanceof String) && (o2 instanceof String))
              {
                String s1 = (String) o1;
                String s2 = (String) o2;
                int result = s1.compareTo(s2);
                if (result > 0)
                  dStack.push(TRUE);
                else
                  dStack.push(FALSE);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "0<", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                dStack.push((i1 < 0) ? TRUE : FALSE);
                return 1;
              }
              else if (o1 instanceof Double)
              {
                double d1 = (Double) o1;
                dStack.push((d1 < 0) ? TRUE : FALSE);
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "0=", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                dStack.push((i1 == 0) ? TRUE : FALSE);
                return 1;
              }
              else if (o1 instanceof Double)
              {
                double d1 = (Double) o1;
                dStack.push((d1 == 0.0) ? TRUE : FALSE);
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "0>", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                dStack.push((i1 > 0) ? TRUE : FALSE);
                return 1;
              }
              else if (o1 instanceof Double)
              {
                double d1 = (Double) o1;
                dStack.push((d1 > 0) ? TRUE : FALSE);
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "not", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                dStack.push((i1 == FALSE) ? TRUE : FALSE);
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "true", false,
            (dStack, vStack) ->
            {
              dStack.push(TRUE);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "false", false,
            (dStack, vStack) ->
            {
              dStack.push(FALSE);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "+", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
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
              else if ((o1 instanceof String) && (o2 instanceof String))
              {
                String s = (String) o2 + (String) o1;
                dStack.push(s);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "-", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              if ((o1 instanceof Long) && (o2 instanceof Long))
              {
                long i1 = (Long) o1;
                long i2 = (Long) o2;
                i2 -= i1;
                dStack.push(i2);
              }
              else if ((o1 instanceof Double) && (o2 instanceof Double))
              {
                double d1 = (Double) o1;
                double d2 = (Double) o2;
                d2 -= d1;
                dStack.push(d2);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "1+", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                dStack.push(i1 + 1);
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "1-", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                dStack.push(i1 - 1);
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "2+", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                dStack.push(i1 + 2);
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "2-", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                dStack.push(i1 - 2);
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "*", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              if ((o1 instanceof Long) && (o2 instanceof Long))
              {
                long i1 = (Long) o1;
                long i2 = (Long) o2;
                i2 *= i1;
                dStack.push(i2);
              }
              else if ((o1 instanceof Double) && (o2 instanceof Double))
              {
                double d1 = (Double) o1;
                double d2 = (Double) o2;
                d2 *= d1;
                dStack.push(d2);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "/", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              if ((o1 instanceof Long) && (o2 instanceof Long))
              {
                long i1 = (Long) o1;
                long i2 = (Long) o2;
                i2 /= i1;
                dStack.push(i2);
              }
              else if ((o1 instanceof Double) && (o2 instanceof Double))
              {
                double d1 = (Double) o1;
                double d2 = (Double) o2;
                d2 /= d1;
                dStack.push(d2);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "mod", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
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
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "max", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
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
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "min", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
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
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "abs", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                i1 = Math.abs(i1);
                dStack.push(i1);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "and", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
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
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "or", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
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
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "xor", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
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
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "<<", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              if ((o1 instanceof Long) && (o2 instanceof Long))
              {
                long i2 = (Long) o2;
                int i1 = (int)((Long) o1).longValue();
                i2 = Long.rotateLeft(i2, i1);
                dStack.push(i2);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      ">>", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              if ((o1 instanceof Long) && (o2 instanceof Long))
              {
                long i2 = (Long) o2;
                int i1 = (int)((Long) o1).longValue();
                i2 = Long.rotateRight(i2, i1);
                dStack.push(i2);
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      ".", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          String outstr = "";
          Object o = dStack.pop();
          if (o instanceof Long)
            outstr = Long.toString((Long) o, base).toUpperCase();
          else if (o instanceof Double)
            outstr = Double.toString((Double) o);
          else if (o instanceof String)
            outstr = (String) o;
          else if (o instanceof BaseWord)
            outstr = "BaseWord address on stack";
          else if (o instanceof FileInputStream)
            outstr = "FileInputStream address on stack";
          else if (o instanceof BufferedReader)
            outstr = "BufferedReader address on stack";
          else if (o instanceof PrintStream)
            outstr = "PrintStream address on stack";
          System.out.print(outstr);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "cr", false,
            (dStack, vStack) ->
            {
              System.out.println();
              return 1;
            }
    ),

          new PrimitiveWord
                  (
                          "sp", false,
                          (dStack, vStack) ->
                          {
                              System.out.print(' ');
                              return 1;
                          }
                  ),

    new PrimitiveWord
    (
      "spaces", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
              {
                long i1 = (Long) o1;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < i1; i++)
                  sb.append(" ");
                System.out.print(sb.toString());
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "binary", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          base = 2;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "decimal", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          base = 10;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "hex", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          base = 16;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "setbase", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            base = (int)((Long) o1).longValue();
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      ":", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          compiling = true;
          String name = getNextToken();
          if (name == null)
            return 0;
          wordBeingDefined = new NonPrimitiveWord(name);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      ";", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          compiling = false;
          dictionary.add(wordBeingDefined);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "words", false,
            (dStack, vStack) ->
            {
              System.out.println(dictionary.toString(false));
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "wordsd", false,
            (dStack, vStack) ->
            {
              System.out.println(dictionary.toString(true));
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "forget", true,
            (dStack, vStack) ->
            {
              String name = getNextToken();
              if (name == null)
                return 0;
              BaseWord bw = null;
              try
              {
                bw = dictionary.search(name);
              }
              catch (Exception ignore) {}
              if (bw != null)
              {
                if (!bw.isPrimitive)
                {
                  dictionary.truncateList(bw);
                }
                else
                  return 0;
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "constant", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              String name = getNextToken();
              if (name == null)
                return 0;
              NonPrimitiveWord constant = new NonPrimitiveWord(name);
              dictionary.add(constant);
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
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "variable", false,
            (dStack, vStack) ->
            {
              String name = getNextToken();
              if (name == null)
                return 0;
              StorageWord sw = new StorageWord(name, 1, false);
              dictionary.add(sw);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      ">r", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o = dStack.pop();
              vStack.push(o);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "r>", false,
            (dStack, vStack) ->
            {
              if (vStack.empty())
                return 0;
              Object o = vStack.pop();
              dStack.push(o);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "r@", false,
            (dStack, vStack) ->
            {
              if (vStack.empty())
                return 0;
              Object o = vStack.peek();
              dStack.push(o);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "!", false,
            (dStack, vStack) ->
            {
              if (vStack.empty())
                return 0;
              Object o = vStack.pop();
              if (!(o instanceof StorageWord))
                return 0;
              StorageWord sw = (StorageWord) o;
              int offset = 0;
              if (!sw.isArray())
              {
                if (dStack.empty())
                  return 0;
              }
              else
              {
                if (dStack.size() < 2)
                  return 0;
                Object off = dStack.pop();
                if (!(off instanceof Long))
                  return 0;
                offset = (int)((Long) off).longValue();
              }
              return sw.store(dStack.pop(), offset);
            }
    ),

    new PrimitiveWord
    (
      "+!", false,
            (dStack, vStack) ->
            {
              if (vStack.empty())
                return 0;
              Object o = vStack.pop();
              if (!(o instanceof StorageWord))
                return 0;
              StorageWord sw = (StorageWord) o;
              int offset = 0;
              if (!sw.isArray())
              {
                if (dStack.empty())
                  return 0;
              }
              else
              {
                if (dStack.size() < 2)
                  return 0;
                Object off = dStack.pop();
                if (!(off instanceof Long))
                  return 0;
                offset = (int)((Long) off).longValue();
              }
              return sw.plusStore(dStack.pop(), offset);
            }
    ),

    new PrimitiveWord
    (
      "@", false,
            (dStack, vStack) ->
            {
              if (vStack.empty())
                return 0;
              Object o = vStack.pop();
              if (!(o instanceof StorageWord))
                return 0;
              StorageWord sw = (StorageWord) o;
              Object data;
              if (!sw.isArray())
              {
                data = sw.fetch(0);
                if (data == null)
                  return 0;
              }
              else
              {
                if (dStack.empty())
                  return 0;
                Object off = dStack.pop();
                if (!(off instanceof Long))
                  return 0;
                int offset = (int)((Long) off).longValue();
                data = sw.fetch(offset);
                if (data == null)
                  return 0;
              }
              dStack.push(data);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "array", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o = dStack.pop();
              if (!(o instanceof Long))
                return 0;
              int size = (int)((Long) o).longValue();
              String name = getNextToken();
              if (name == null)
                return 0;
              StorageWord sw = new StorageWord(name, size, true);
              dictionary.add(sw);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "round", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.round((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "toLong", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
                dStack.push(((Double) o1).longValue());
              else if(o1 instanceof String)
                dStack.push(Long.parseLong((String)o1));
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "toDouble", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Long)
                dStack.push((double) (Long) o1);
              else if(o1 instanceof String)
                dStack.push(Double.parseDouble((String)o1));
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "toString", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
            dStack.push(Long.toString((Long) o1, base).toUpperCase());
          else if (o1 instanceof Double)
            dStack.push(Double.toString((Double) o1));
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "length", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if(o1 instanceof String)
              {
                dStack.push((long) ((String) o1).length());
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "subString", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 3)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              Object o3 = dStack.pop();
              if ((o1 instanceof Long) && (o2 instanceof Long) && (o3 instanceof String))
              {
                int i1 = (int)((Long)o1).longValue();
                int i2 = (int)((Long)o2).longValue();
                dStack.push(((String)o3).substring(i2, i1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "E", false,
            (dStack, vStack) ->
            {
              dStack.push(Math.E);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "PI", false,
            (dStack, vStack) ->
            {
              dStack.push(Math.PI);
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "sqrt", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.sqrt((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "pow", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              if ((o1 instanceof Double) && (o2 instanceof Double))
              {
                double d1 = (Double) o1;
                double d2 = (Double) o2;
                dStack.push(Math.pow(d2, d1));
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "log", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.log((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "log10", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.log10((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "exp", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.exp((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "sin", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.sin((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "cos", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.cos((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "tan", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.tan((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "asin", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.asin((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "acos", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.acos((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "atan", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.atan((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "atan2", false,
            (dStack, vStack) ->
            {
              if (dStack.size() < 2)
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              if ((o1 instanceof Double) && (o2 instanceof Double))
              {
                double d1 = (Double) o1;
                double d2 = (Double) o2;
                dStack.push(Math.atan2(d2, d1));
              }
              else
                return 0;
              return 1;
            }
    ),

    new PrimitiveWord
    (
      "sinh", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.sinh((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "cosh", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.cosh((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "tanh", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof Double)
              {
                dStack.push(Math.tanh((Double) o1));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "load", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof String)
              {
                String fileName = (String) o1;
                return fileLoad(fileName);
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "gaussian", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o = dStack.pop();
          if (o instanceof Long)
          {
            long mult = (Long) o;
            double number = random.nextGaussian() * mult;
            dStack.push((long) number);
          }
          else if (o instanceof Double)
          {
            double mult = (Double) o;
            double number = random.nextGaussian() * mult;
            dStack.push(number);
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "random", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o = dStack.pop();
          if (o instanceof Long)
          {
            long mult = (Long) o;
            double number = random.nextDouble() * mult;
            dStack.push((long) number);
          }
          else if (o instanceof Double)
          {
            double mult = (Double) o;
            double number = random.nextDouble() * mult;
            dStack.push(number);
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "openByteReader", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof String)
              {
                try
                {
                  File f = new File((String) o1);
                  dStack.push(new FileInputStream(f));
                }
                catch(IOException ioe)
                {
                  ioe.printStackTrace();
                  return 0;
                }
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "readByte", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof FileInputStream)
              {
                try
                {
                  dStack.push((long) (((FileInputStream) o1).read()));
                  return 1;
                }
                catch(IOException ioe)
                {
                  ioe.printStackTrace();
                  return 0;
                }
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "closeByteReader", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof FileInputStream)
              {
                try
                {
                  ((FileInputStream) o1).close();
                  return 1;
                }
                catch(IOException ioe)
                {
                  ioe.printStackTrace();
                  return 0;
                }
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "openReader", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof String)
              {
                try
                {
                  File f = new File((String) o1);
                  dStack.push(new BufferedReader(new FileReader(f)));
                }
                catch(IOException ioe)
                {
                  ioe.printStackTrace();
                  return 0;
                }
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "readLine", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
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
                catch(IOException ioe)
                {
                  ioe.printStackTrace();
                  return 0;
                }
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "closeReader", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof BufferedReader)
              {
                try
                {
                  ((BufferedReader) o1).close();
                  return 1;
                }
                catch(IOException ioe)
                {
                  ioe.printStackTrace();
                  return 0;
                }
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "openWriter", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof String)
              {
                try
                {
                  File f = new File((String) o1);
                  dStack.push(new PrintStream(f));
                }
                catch(IOException ioe)
                {
                  ioe.printStackTrace();
                  return 0;
                }
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "writeString", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if (o1 instanceof PrintStream)
          {
            if (o2 instanceof String)
              ((PrintStream) o1).print((String) o2);
            else if (o2 instanceof Long)
              ((PrintStream) o1).print(Long.toString((Long) o2, base).toUpperCase());
            else if (o2 instanceof Double)
              ((PrintStream) o1).print(Double.toString((Double) o2));
            else
              return 0;
            return 1; 
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "writeEol", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof PrintStream)
              {
                ((PrintStream) o1).println();
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "writeByte", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              Object o2 = dStack.pop();
              if ((o1 instanceof PrintStream) && (o2 instanceof Long))
              {
                ((PrintStream) o1).write((byte)(((Long) o2).longValue()));
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "closeWriter", false,
            (dStack, vStack) ->
            {
              if (dStack.empty())
                return 0;
              Object o1 = dStack.pop();
              if (o1 instanceof PrintStream)
              {
                ((PrintStream) o1).close();
                return 1;
              }
              else
                return 0;
            }
    ),

    new PrimitiveWord
    (
      "bye", false,
            (dStack, vStack) ->
            {
              System.exit(0);
              return 1;
            }
    ),
  };

  private String getNextToken()
  {
    try
    {
      if (st.nextToken() != StreamTokenizer.TT_EOF)
        return st.sval;
      else
        return null;
    }
    catch(IOException ioe)
    {
      ioe.printStackTrace();
      return null;
    }
  }

  private Long parseLong(String word)
  {
    boolean isNumber = false;
    long number = 0;
    try
    {
      number = Long.parseLong(word, base);
      isNumber = true;
    }
    catch(NumberFormatException ignored) {}
    if (isNumber)
      return number;
    else
      return null;
  }

  private Double parseDouble(String word)
  {
    boolean isDouble = false;
    double number = 0.0;
    try
    {
      number = Double.parseDouble(word);
      isDouble = true;
    }
    catch(NumberFormatException ignored) {}
    if (isDouble)
      return number;
    else
      return null;
  }

  private JForth ()
  {
    for (BaseWord forthWord : forthWords)
    {
      dictionary.add(forthWord);
    }
    compiling = false;
    base = 10;
    random = new Random();
    history = new History(HISTORY_LENGTH);
  }

  private boolean interpretLine (String text)
  {
    try
    {
      StringReader sr = new StringReader(text);
      st = new StreamTokenizer(sr);
      st.resetSyntax();
      st.wordChars('!', 'z');
      st.quoteChar('"');
      st.whitespaceChars('\u0000', '\u0020');
      int ttype = st.nextToken();
      while (ttype != StreamTokenizer.TT_EOF)
      {
        String word = st.sval;
        if (!compiling)
        {
          if (ttype == '"')
          {
            dStack.push(word);
            ttype = st.nextToken();
            continue;
          }
          BaseWord bw = dictionary.search(word);
          if (bw != null)
          {
            if (bw.execute(dStack, vStack) == 0)
            {
              System.out.println(word + " ?  word execution or stack error");
              return false;
            }
          }
          else
          {
            Long num = parseLong(word);
            if (num != null)
            {
              dStack.push(num);
            }
            else
            {
              Double dnum = parseDouble(word);
              if (dnum != null)
              {
                dStack.push(dnum);
              }
              else
              {
                System.out.println(word + " ?");
                return false;
              }
            }
          }
        }
        else
        {
          if (ttype == '"')
          {
            wordBeingDefined.addWord(new StringLiteral(word));
            ttype = st.nextToken();
            continue;
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
          }
          else
          {
            Long num = parseLong(word);
            if (num != null)
            {
              wordBeingDefined.addWord(new LongLiteral(num));
            }
            else
            {
              Double dnum = parseDouble(word);
              if (dnum != null)
              {
                wordBeingDefined.addWord(new DoubleLiteral(dnum));
              }
              else
              {
                System.out.println(word + " ?");
                compiling = false;
                return false;
              }
            }
          }
        }
        ttype = st.nextToken();
      }
      return true;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  private int fileLoad (String fileName)
  {
    File f = new File(fileName);
    if (!f.exists())
      return 0;
    BufferedReader file = null;
    try
    {
      FileReader fr = new FileReader(fileName);
      file = new BufferedReader(fr);
      String text = file.readLine();
      while (text != null)
      {
        if (!interpretLine(text))
          return 0;
        text = file.readLine();
      }
      return 1;
    }
    catch(Exception e)
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
      catch(Exception ignored) {}
    }
  }

  private void outerInterpreter ()
  {
    dStack.removeAllElements();
    Scanner scanner = new Scanner(System.in);
    System.out.println("JForth from (http://linuxenvy.com/bprentice/JForth/");
    while (true)
    {
      System.out.print(PROMPT);
      String input = scanner.nextLine();
      history.add(input);
      if (!interpretLine(input))
      {
        dStack.removeAllElements();
      }
      else
      {
        System.out.print(OK);
      }
    }
  }

  public static void main(String [] args)
  {
    JForth forth = new JForth();
    forth.outerInterpreter();
  }
}
