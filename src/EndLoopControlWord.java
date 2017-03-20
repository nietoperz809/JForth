public final class EndLoopControlWord extends BaseWord
{
  public EndLoopControlWord(int indexIncrement)
  {
    super("", false, false);
    this.indexIncrement = indexIncrement;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    if (dStack.empty())
      return 0;
    Object o = dStack.pop();
    if (!(o instanceof Long))
      return 0;
    if (((Long) o) == JForth.TRUE)
      return 1;
    else
      return indexIncrement;
  }

  private int indexIncrement;
}
