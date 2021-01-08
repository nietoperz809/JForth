package jforth;

import tools.TwoFuncs;

public class PrimitiveWord extends BaseWord
{
  public PrimitiveWord(String name, boolean isImmediate, TwoFuncs<OStack, OStack, Integer> eif)
  {
    this(name, isImmediate, null, eif);
  }

  public PrimitiveWord(String name, boolean isImmediate, String info, TwoFuncs<OStack, OStack, Integer> eif)
  {
    super(name, isImmediate, info);
    this.eif = eif;
  }

  public PrimitiveWord(String name, String info, TwoFuncs<OStack, OStack, Integer> eif)
  {
    super(name, false, info);
    this.eif = eif;
  }

  public Integer apply(OStack dStack, OStack vStack)
  {
    return eif.apply(dStack, vStack);
  }

  private final TwoFuncs<OStack, OStack, Integer> eif;
}
