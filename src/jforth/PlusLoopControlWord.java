package jforth;

public final class PlusLoopControlWord extends LoopControlWord
{
  public PlusLoopControlWord(Integer indexIncrement)
  {
    super(indexIncrement);
  }

  public Integer apply(OStack dStack, OStack vStack)
  {
    if ((vStack.size() < 2) || dStack.empty())
      return 0;
    Object o1 = vStack.pop();
    Object o2 = vStack.peek();
    Object o3 = dStack.pop();
    if ((o1 instanceof Long) && (o2 instanceof Long) && (o3 instanceof Long))
    {
      long index = (Long) o1;
      long limit = (Long) o2;
      long inc   = (Long) o3;
      index += inc;
      boolean condition;
      condition = (inc >= 0) ? (index >= limit) : (index <= limit);
      if (condition)
      {
        vStack.pop();
        return 1;
      }
      else
      {
        vStack.push(index);
        return indexIncrement;
      }
    }
    return 0;
  }
}
