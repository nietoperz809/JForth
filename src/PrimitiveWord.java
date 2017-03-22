import java.io.Serializable;

public final class PrimitiveWord extends BaseWord implements Serializable
{
  private static final long serialVersionUID = 7526471155622776149L;

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
