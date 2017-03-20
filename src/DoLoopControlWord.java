public final class DoLoopControlWord extends BaseWord
{
  public DoLoopControlWord()
  {
    super("", false, false);
  }

  public int execute(OStack dStack, OStack vStack)
  {
    if (dStack.size() < 2)
      return 0;
    Object o1 = dStack.pop();
    Object o2 = dStack.pop();
    if ((o1 instanceof Long) && (o2 instanceof Long))
    {
      vStack.push(o2);
      vStack.push(o1);
      return 1;
    }
    else
      return 0;
  }
}
