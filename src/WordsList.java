import java.util.Iterator;
import java.util.LinkedList;

public class WordsList
{
  public WordsList()
  {
    wordsList = new LinkedList<>();
  }

  private boolean isEmpty ()
  {
    return wordsList.size() == 0;
  }

  public void clear()
  {
    wordsList.clear();
  }

  public void add(BaseWord bw)
  { 
    wordsList.addFirst(bw);
  }

  public String toString(boolean showDetail)
  {
    if (isEmpty())
    {
      return "WordsList is empty\n";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Words:\n");
    Iterator<BaseWord> i1 = wordsList.listIterator(0);
    while (i1.hasNext())
    {
      BaseWord bw = i1.next();
      sb.append(bw.toString(showDetail));
      if (showDetail)
      {
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  public BaseWord search(String wordName) throws Exception
  {
    if (isEmpty())
    {
      throw new Exception("WordsList is empty");
    }
    Iterator<BaseWord> i1 = wordsList.listIterator(0);
    while (i1.hasNext())
    {
      BaseWord bw = i1.next();
      if (bw.name.equals(wordName))
      {
        return bw;
      }
    }
    return null;
  }

  public void truncateList(BaseWord bw)
  { 
    int size = wordsList.size();
    int index = wordsList.indexOf(bw);
    if (index != -1)
    {
      for (int i = 0; i <= index; i++)
      {
        wordsList.remove(0);
      }
    }
  }

  private final LinkedList<BaseWord> wordsList;
}
  