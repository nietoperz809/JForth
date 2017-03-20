public final class PrimitiveWord extends BaseWord
{
  public PrimitiveWord(String name, boolean isImmediate, ExecuteIF eif)
  {
    super(name, isImmediate, true);
    this.eif = eif;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    return eif.execute(dStack, vStack);
  }

  private ExecuteIF eif;
}
