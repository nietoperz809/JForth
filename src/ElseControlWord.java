public final class ElseControlWord extends BaseWord
{
  public ElseControlWord(int indexFollowingElse)
  {
    super("", false, false);
    this.indexFollowingElse = indexFollowingElse;
  }

  public void setThenIndexIncrement(int thenIndexIncrement)
  {
    this.thenIndexIncrement = thenIndexIncrement;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    return thenIndexIncrement - indexFollowingElse + 1;
  }

  private int indexFollowingElse;
  private int thenIndexIncrement;
}
