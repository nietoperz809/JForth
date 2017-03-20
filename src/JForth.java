import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class JForth
{
  private static final String PROMPT = "\n> ";
  private static final String OK = " OK";
  public static final Long TRUE  = new Long(1);
  public static final Long FALSE = new Long(0);
  private static final int HISTORY_LENGTH = 25;

  private OStack dStack = new OStack();
  private OStack vStack = new OStack();
  private WordsList dictionary = new WordsList();
  private boolean compiling;
  private int base;
  private StreamTokenizer st = null;
  private NonPrimitiveWord wordBeingDefined = null;
  private Random random;
  private History history;

  private BaseWord [] forthWords =
  {
    new PrimitiveWord
    (
      "(", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          String token = getNextToken();
          while ((token != null) && (!token.equals(")")))
            token = getNextToken();
          if (token != null)
            return 1;
          else
            return 0;
        }
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
       new ExecuteIF()
       {
         public int execute(OStack dStack, OStack vStack)
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
          vStack.push(new Long(index));
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "i", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (vStack.empty())
            return 0;
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
        public int execute(OStack dStack, OStack vStack)
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
          vStack.push(new Long(index));
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
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o = dStack.peek();
          dStack.push(o);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "drop", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
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
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
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
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "rot", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "pick", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
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
      }
    ),

    new PrimitiveWord
    (
      "depth", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          Long i = new Long(dStack.size());
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
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o2 = dStack.pop();
          Object o1 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            if (i1 < i2)
              dStack.push(TRUE);
            else
              dStack.push(FALSE);
          }
          else if ((o1 instanceof Double) && (o2 instanceof Double))
          {
            double d1 = ((Double) o1).doubleValue();
            double d2 = ((Double) o2).doubleValue();
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
      }
    ),

    new PrimitiveWord
    (
      "=", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o2 = dStack.pop();
          Object o1 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            if (i1 == i2)
              dStack.push(TRUE);
            else
              dStack.push(FALSE);
          }
          else if ((o1 instanceof Double) && (o2 instanceof Double))
          {
            double d1 = ((Double) o1).doubleValue();
            double d2 = ((Double) o2).doubleValue();
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
      }
    ),

    new PrimitiveWord
    (
      ">", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o2 = dStack.pop();
          Object o1 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            if (i1 > i2)
              dStack.push(TRUE);
            else
              dStack.push(FALSE);
          }
          else if ((o1 instanceof Double) && (o2 instanceof Double))
          {
            double d1 = ((Double) o1).doubleValue();
            double d2 = ((Double) o2).doubleValue();
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
      }
    ),

    new PrimitiveWord
    (
      "0<", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            dStack.push((i1 < 0) ? TRUE : FALSE);
            return 1;
          }
          else if (o1 instanceof Double)
          {
            double d1 = ((Double) o1).doubleValue();
            dStack.push((d1 < 0) ? TRUE : FALSE);
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "0=", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            dStack.push((i1 == 0) ? TRUE : FALSE);
            return 1;
          }
          else if (o1 instanceof Double)
          {
            double d1 = ((Double) o1).doubleValue();
            dStack.push((d1 == 0.0) ? TRUE : FALSE);
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "0>", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            dStack.push((i1 > 0) ? TRUE : FALSE);
            return 1;
          }
          else if (o1 instanceof Double)
          {
            double d1 = ((Double) o1).doubleValue();
            dStack.push((d1 > 0) ? TRUE : FALSE);
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "not", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            dStack.push((i1 == FALSE) ? TRUE : FALSE);
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "true", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          dStack.push(TRUE);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "false", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          dStack.push(FALSE);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "+", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 += i1;
            dStack.push(new Long(i2));
          }
          else if ((o1 instanceof Double) && (o2 instanceof Double))
          {
            double d1 = ((Double) o1).doubleValue();
            double d2 = ((Double) o2).doubleValue();
            d2 += d1;
            dStack.push(new Double(d2));
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
      }
    ),

    new PrimitiveWord
    (
      "-", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 -= i1;
            dStack.push(new Long(i2));
          }
          else if ((o1 instanceof Double) && (o2 instanceof Double))
          {
            double d1 = ((Double) o1).doubleValue();
            double d2 = ((Double) o2).doubleValue();
            d2 -= d1;
            dStack.push(new Double(d2));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "1+", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            dStack.push(new Long(i1 + 1));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "1-", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            dStack.push(new Long(i1 - 1));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "2+", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            dStack.push(new Long(i1 + 2));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "2-", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            dStack.push(new Long(i1 - 2));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "*", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 *= i1;
            dStack.push(new Long(i2));
          }
          else if ((o1 instanceof Double) && (o2 instanceof Double))
          {
            double d1 = ((Double) o1).doubleValue();
            double d2 = ((Double) o2).doubleValue();
            d2 *= d1;
            dStack.push(new Double(d2));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "/", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 /= i1;
            dStack.push(new Long(i2));
          }
          else if ((o1 instanceof Double) && (o2 instanceof Double))
          {
            double d1 = ((Double) o1).doubleValue();
            double d2 = ((Double) o2).doubleValue();
            d2 /= d1;
            dStack.push(new Double(d2));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "mod", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 %= i1;
            dStack.push(new Long(i2));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "max", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 = Math.max(i1, i2);
            dStack.push(new Long(i2));
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
      }
    ),

    new PrimitiveWord
    (
      "min", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 = Math.min(i1, i2);
            dStack.push(new Long(i2));
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
      }
    ),

    new PrimitiveWord
    (
      "abs", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            i1 = Math.abs(i1);
            dStack.push(new Long(i1));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "and", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 &= i1;
            dStack.push(new Long(i2));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "or", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 |= i1;
            dStack.push(new Long(i2));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "xor", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i1 = ((Long) o1).longValue();
            long i2 = ((Long) o2).longValue();
            i2 ^= i1;
            dStack.push(new Long(i2));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "<<", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i2 = ((Long) o2).longValue();
            int i1 = (int)((Long) o1).longValue();
            i2 = Long.rotateLeft(i2, i1);
            dStack.push(new Long(i2));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      ">>", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Long) && (o2 instanceof Long))
          {
            long i2 = ((Long) o2).longValue();
            int i1 = (int)((Long) o1).longValue();
            i2 = Long.rotateRight(i2, i1);
            dStack.push(new Long(i2));
          }
          else
            return 0;
          return 1;
        }
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
            outstr = Long.toString(((Long) o).longValue(), base).toUpperCase();
          else if (o instanceof Double)
            outstr = Double.toString(((Double) o).doubleValue());
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
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          System.out.println();
          return 1;
        }
      }
    ),

          new PrimitiveWord
                  (
                          "sp", false,
                          new ExecuteIF()
                          {
                              public int execute(OStack dStack, OStack vStack)
                              {
                                  System.out.print(' ');
                                  return 1;
                              }
                          }
                  ),

    new PrimitiveWord
    (
      "spaces", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
          {
            long i1 = ((Long) o1).longValue();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < i1; i++)
              sb.append(" ");
            System.out.print(sb.toString());
            return 1;
          }
          else
            return 0;
        }
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
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          System.out.println(dictionary.toString(false));
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "wordsd", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          System.out.println(dictionary.toString(true));
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "forget", true,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "constant", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "variable", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          String name = getNextToken();
          if (name == null)
            return 0;
          StorageWord sw = new StorageWord(name, 1, false);
          dictionary.add(sw);
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      ">r", false,
       new ExecuteIF()
       {
         public int execute(OStack dStack, OStack vStack)
         {
           if (dStack.empty())
             return 0;
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
        public int execute(OStack dStack, OStack vStack)
        {
          if (vStack.empty())
            return 0;
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
        public int execute(OStack dStack, OStack vStack)
        {
          if (vStack.empty())
            return 0;
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
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "+!", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "@", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (vStack.empty())
            return 0;
          Object o = vStack.pop();
          if (!(o instanceof StorageWord))
            return 0;
          StorageWord sw = (StorageWord) o;
          Object data = null;
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
      }
    ),

    new PrimitiveWord
    (
      "array", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "round", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Long(Math.round(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "toLong", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
            dStack.push(new Long(((Double)o1).longValue()));
          else if(o1 instanceof String) 
            dStack.push(Long.parseLong((String)o1));
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "toDouble", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Long)
            dStack.push(new Double(((Long)o1).longValue()));
          else if(o1 instanceof String) 
            dStack.push(Double.parseDouble((String)o1));
          else
            return 0;
          return 1;
        }
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
            dStack.push(Long.toString(((Long) o1).longValue(), base).toUpperCase());
          else if (o1 instanceof Double)
            dStack.push(Double.toString(((Double) o1).doubleValue()));
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "length", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if(o1 instanceof String)
          { 
            dStack.push(new Long(((String)o1).length()));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "subString", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "E", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          dStack.push(new Double(Math.E));
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "PI", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          dStack.push(new Double(Math.PI));
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "sqrt", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.sqrt(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "pow", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Double) && (o2 instanceof Double))
          {
            double d1 = ((Double) o1).doubleValue();
            double d2 = ((Double) o2).doubleValue();
            dStack.push(new Double(Math.pow(d2, d1)));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "log", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.log(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "log10", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.log10(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "exp", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.exp(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "sin", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.sin(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "cos", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.cos(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "tan", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.tan(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "asin", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.asin(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "acos", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.acos(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "atan", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.atan(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "atan2", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.size() < 2)
            return 0;
          Object o1 = dStack.pop();
          Object o2 = dStack.pop();
          if ((o1 instanceof Double) && (o2 instanceof Double))
          {
            double d1 = ((Double) o1).doubleValue();
            double d2 = ((Double) o2).doubleValue();
            dStack.push(new Double(Math.atan2(d2, d1)));
          }
          else
            return 0;
          return 1;
        }
      }
    ),

    new PrimitiveWord
    (
      "sinh", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.sinh(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "cosh", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.cosh(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "tanh", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof Double)
          {
            dStack.push(new Double(Math.tanh(((Double) o1).doubleValue())));
            return 1;
          }
          else
            return 0;
        }
      }
    ),

    new PrimitiveWord
    (
      "load", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
            long mult = ((Long) o).longValue();
            double number = random.nextGaussian() * mult;
            dStack.push(new Long((long) number));
          }
          else if (o instanceof Double)
          {
            double mult = ((Double) o).doubleValue();
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
            long mult = ((Long) o).longValue();
            double number = random.nextDouble() * mult;
            dStack.push(new Long((long) number));
          }
          else if (o instanceof Double)
          {
            double mult = ((Double) o).doubleValue();
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
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "readByte", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          if (dStack.empty())
            return 0;
          Object o1 = dStack.pop();
          if (o1 instanceof FileInputStream)
          {
            try
            {
              dStack.push(new Long((long)(((FileInputStream) o1).read())));
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
      }
    ),

    new PrimitiveWord
    (
      "closeByteReader", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "openReader", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "readLine", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "closeReader", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "openWriter", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
              ((PrintStream) o1).print(Long.toString(((Long) o2).longValue(), base).toUpperCase());
            else if (o2 instanceof Double)
              ((PrintStream) o1).print(Double.toString(((Double) o2).doubleValue()));
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
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "writeByte", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "closeWriter", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
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
      }
    ),

    new PrimitiveWord
    (
      "bye", false,
      new ExecuteIF()
      {
        public int execute(OStack dStack, OStack vStack)
        {
          System.exit(0);
          return 1;
        }
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
    catch(NumberFormatException nfe) {}
    if (isNumber)
      return new Long(number);
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
    catch(NumberFormatException nfe) {}
    if (isDouble)
      return new Double(number);
    else
      return null;
  }

  public JForth()
  {
    for (int i = 0; i < forthWords.length; i++)
      dictionary.add(forthWords[i]);
    compiling = false;
    base = 10;
    random = new Random();
    history = new History(HISTORY_LENGTH);
  }

  public boolean interpretLine(String text)
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

  public int fileLoad(String fileName)
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
        file.close();
      }
      catch(Exception ex) {}
    }
  }

  public void outerInterpreter()
  {
    dStack.removeAllElements();
    Scanner scanner = new Scanner(System.in);
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
