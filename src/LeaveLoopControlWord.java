public final class LeaveLoopControlWord extends BaseWord
{
  public LeaveLoopControlWord()
  {
    super("", false, false, null);
  }

  public int execute(OStack dStack, OStack vStack)
  {
    if (vStack.size() < 2)
      return 0;
    vStack.pop();
    Object o2 = vStack.peek();
    vStack.push(o2);
    return 1;
  }
}
