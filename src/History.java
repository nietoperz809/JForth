import java.io.IOException;
import java.util.*;

public class History
{
  public ArrayList<String> history;
  private final int length;
  private int nextIndex;

  History(int length)
  {
    this.length = length;
    nextIndex = 0;
    history = new ArrayList<>(length);
  }

  public void removeLast()
  {
    history.remove(history.size()-1);
  }

  public void clear()
  {
    history.clear();
  }

  public void save() throws IOException
  {
    Utilities.saveObject("history", history);
  }

  public void load() throws IOException
  {
    history = (ArrayList<String>) Utilities.loadObject("history");
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
