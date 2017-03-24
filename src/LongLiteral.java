public final class LongLiteral extends BaseWord
{
  public LongLiteral(Long number)
  {
    super("", false, false, null);
    this.number = number;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    dStack.push(number);
    return 1;
  }

  private Long number;
}
