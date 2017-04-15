package jforth;

public final class LoopControlWord extends BaseWord
{
  public LoopControlWord(int indexIncrement)
  {
    super("", false, false, null);
    this.indexIncrement = indexIncrement;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    if (vStack.size() < 2)
      return 0;
    Object o1 = vStack.pop();
    Object o2 = vStack.peek();
    if ((o1 instanceof Long) && (o2 instanceof Long))
    {
      long index = ((Long) o1).longValue();
      long limit = ((Long) o2).longValue();
      index += 1;
      if (index >= limit)
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
