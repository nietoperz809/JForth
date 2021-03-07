package jforth;

import jforth.ControlWords.BreakLoopControlWord;
import tools.TwoFuncs;

import java.util.*;

public final class NonPrimitiveWord extends BaseWord
{
  public NonPrimitiveWord(String name)
  {
    super(name, false, null);
  }

  public Object addWord(TwoFuncs<OStack, OStack, Integer> eif)
  {
    words.add(eif);
    return null;
  }

  public int getNextWordIndex()
  {
    return words.size() + 1;
  }

  public Integer apply(OStack dStack, OStack vStack)
  {
    int index = 0;
    int size = words.size();
    while (index < size)
    {
      TwoFuncs<OStack, OStack, Integer> eif = words.get(index);
      if (eif instanceof BreakLoopControlWord)
        return 1;
      int increment = eif.apply(dStack, vStack);
      if (increment == 0)
        return 0;
      index += increment;
    }
    return 1;
  }

// --Commented out by Inspection START (11/30/2018 1:21 AM):
//  public void setImmediate()
//  {
//    immediate = true;
//  }
// --Commented out by Inspection STOP (11/30/2018 1:21 AM)

// --Commented out by Inspection START (11/30/2018 1:20 AM):
//  public ArrayList<ExecuteIF> getList()
//  {
//    return words;
//  }
// --Commented out by Inspection STOP (11/30/2018 1:20 AM)

  private final ArrayList<TwoFuncs<OStack, OStack, Integer>> words = new ArrayList<>();
}
