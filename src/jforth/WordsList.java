package jforth;

import java.util.*;

public class WordsList
{
    private HashMap<String, BaseWord> wordsList;
    private HashMap<String, BaseWord> saveList;

    WordsList ()
    {
        wordsList = new HashMap<>();
        saveList = new HashMap<>();
    }

    void add (BaseWord bw)
    {
        BaseWord old = wordsList.get(bw.name);
        if (old != null)
        {
            saveList.put (old.name, old);
            System.out.println("Overwrite: "+bw.name);
        }
        wordsList.put(bw.name, bw);
    }

    String toString (boolean showDetail)
    {
        if (isEmpty())
        {
            return "WordsList is empty\n";
        }
        //Collections.sort(wordsList, Collections.reverseOrder());
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, BaseWord>> i1 = wordsList.entrySet().iterator();
        while (i1.hasNext())
        {
            BaseWord bw = i1.next().getValue();
            sb.append(bw.toString(showDetail));
            if (showDetail)
            {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String variableList ()
    {
        if (isEmpty())
        {
            return null;
        }
        //Collections.sort(wordsList, Collections.reverseOrder());
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, BaseWord>> i1 = wordsList.entrySet().iterator();
        while (i1.hasNext())
        {
            BaseWord bw = i1.next().getValue();
            if (bw instanceof StorageWord)
                sb.append(bw.toString(false)).append(' ');
        }
        return sb.toString();
    }

    private boolean isEmpty ()
    {
        return wordsList.size() == 0;
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

    BaseWord search (String wordName) throws Exception
    {
        if (isEmpty())
        {
            throw new Exception("WordsList is empty");
        }
        return wordsList.get(wordName);
//        Iterator<BaseWord> i1 = wordsList.listIterator(0);
//        while (i1.hasNext())
//        {
//            BaseWord bw = i1.next();
//            if (bw.name.equals(wordName))
//            {
//                return bw;
//            }
//        }
//        return null;
    }

    void remove (BaseWord bw)
    {
        BaseWord old = saveList.get (bw.name);
        wordsList.remove(bw.name);
        if (old != null)
        {
            wordsList.put(old.name, old);
            saveList.remove(old.name);
        }
    }
}
  