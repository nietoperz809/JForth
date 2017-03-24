import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class WordsList
{
  WordsList()
  {
    wordsList = new LinkedList<>();
  }

  private boolean isEmpty ()
  {
    return wordsList.size() == 0;
  }

  void add(BaseWord bw)
  {
    //System.out.println(bw.name);
    wordsList.addFirst(bw);
  }

  String toString(boolean showDetail)
  {
    if (isEmpty())
    {
      return "WordsList is empty\n";
    }
    Collections.sort(wordsList, Collections.reverseOrder());
    StringBuilder sb = new StringBuilder();
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

  BaseWord search(String wordName) throws Exception
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

//  public void truncateList(BaseWord bw)
//  {
//    int size = wordsList.size();
//    int index = wordsList.indexOf(bw);
//    if (index != -1)
//    {
//      for (int i = 0; i <= index; i++)
//      {
//        wordsList.remove(0);
//      }
//    }
//  }

  void remove(BaseWord bw)
  {
    wordsList.remove(bw);
  }

  private final LinkedList<BaseWord> wordsList;
}
  