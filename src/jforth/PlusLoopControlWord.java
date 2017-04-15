package jforth;

public final class PlusLoopControlWord extends BaseWord
{
  public PlusLoopControlWord(int indexIncrement)
  {
    super("", false, false, null);
    this.indexIncrement = indexIncrement;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    if ((vStack.size() < 2) || dStack.empty())
      return 0;
    Object o1 = vStack.pop();
    Object o2 = vStack.peek();
    Object o3 = dStack.pop();
    if ((o1 instanceof Long) && (o2 instanceof Long) && (o3 instanceof Long))
    {
      long index = ((Long) o1).longValue();
      long limit = ((Long) o2).longValue();
      long inc   = ((Long) o3).longValue();
      index += inc;
      boolean condition;
      if (inc >= 0)
        condition = index >= limit;
      else
        condition = index <= limit;
      if (condition)
      {
        vStack.pop();
        return 1;
      }
      else
      {
        vStack.push(new Long(index));
        return indexIncrement;
      }
    }
    else
      return 0;
  }

  private int indexIncrement;
}
