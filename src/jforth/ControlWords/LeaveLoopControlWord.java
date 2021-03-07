package jforth.ControlWords;

import jforth.BaseWord;
import jforth.OStack;

public final class LeaveLoopControlWord extends BaseWord
{
  public LeaveLoopControlWord()
  {
    super("", false, null);
  }

  public Integer apply(OStack dStack, OStack vStack)
  {
    if (vStack.size() < 2)
      return 0;
    vStack.pop();
    Object o2 = vStack.peek();
    vStack.push(o2);
    return 1;
  }
}
