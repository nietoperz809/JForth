package jforth;

import java.util.Objects;

public final class EndLoopControlWord extends BaseWord
{
  public EndLoopControlWord(int indexIncrement)
  {
    super("", false, false, null);
    this.indexIncrement = indexIncrement;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    if (dStack.empty())
      return 0;
    Object o = dStack.pop();
    if (!(o instanceof Long))
      return 0;
    if (Objects.equals(o, JForth.TRUE))
      return 1;
    else
      return indexIncrement;
  }

  private final int indexIncrement;
}
