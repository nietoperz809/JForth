package jforth;

public final class ElseControlWord extends BaseWord
{
  public ElseControlWord(int indexFollowingElse)
  {
    super("", false, null);
    this.indexFollowingElse = indexFollowingElse;
  }

  public void setThenIndexIncrement(int thenIndexIncrement)
  {
    this.thenIndexIncrement = thenIndexIncrement;
  }

  public Integer apply(OStack dStack, OStack vStack)
  {
    return thenIndexIncrement - indexFollowingElse + 1;
  }

  private final int indexFollowingElse;
  private int thenIndexIncrement;
}
