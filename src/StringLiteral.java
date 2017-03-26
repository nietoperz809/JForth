

public final class StringLiteral extends BaseWord
{
  public StringLiteral(String str)
  {
    super("", false, false, null);
    this.str = str;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    dStack.push(str);
    return 1;
  }

  private String str;
}
