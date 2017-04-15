package jforth;

public final class PrimitiveWord extends BaseWord
{
  public PrimitiveWord(String name, boolean isImmediate, ExecuteIF eif)
  {
    this(name, isImmediate, null, eif);
  }

  public PrimitiveWord(String name, boolean isImmediate, String info, ExecuteIF eif)
  {
    super(name, isImmediate, true, info);
    this.eif = eif;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    return eif.execute(dStack, vStack);
  }

  private ExecuteIF eif;
}
