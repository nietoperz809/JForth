package jforth;

import java.util.*;

public final class NonPrimitiveWord extends BaseWord
{
  public NonPrimitiveWord(String name)
  {
    super(name, false, false, null);
  }

  public void addWord(ExecuteIF eif)
  {
    words.add(eif);
  }

  public int getNextWordIndex()
  {
    return words.size() + 1;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    int index = 0;
    int size = words.size();
    while (index < size)
    {
      ExecuteIF eif = words.get(index);
      if (eif instanceof BreakLoopControlWord)
        return 1;
      int increment = eif.execute(dStack, vStack);
      if (increment == 0)
        return 0;
      index += increment;
    }
    return 1;
  }

  public void setImmediate()
  {
    immediate = true;
  }

  public ArrayList<ExecuteIF> getList()
  {
    return words;
  }

  private final ArrayList<ExecuteIF> words = new ArrayList<>();
}
