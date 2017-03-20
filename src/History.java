import java.util.*;

public class History
{
  private ArrayList<String> history;
  private int length;
  private int nextIndex;

  History(int length)
  {
    this.length = length;
    nextIndex = 0;
    history = new ArrayList<String>(length);
  }
  
  public void add(String s)
  {
    if (history.size() == length)
      history.remove(0);
    history.add(s);
    nextIndex = history.size();
  }

  public String getNext()
  {
    nextIndex--;
    if (nextIndex < 0)
      nextIndex = history.size() - 1;
    return history.get(nextIndex); 
  }
  
  public void resetIndex()
  {
    nextIndex = length;
  }
}
