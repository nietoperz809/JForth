package jforth;

import java.util.*;

public class WordsList
{
    private final TreeMap<String, BaseWord> wordsList;
    private final TreeMap<String, BaseWord> saveList;

    WordsList ()
    {
        wordsList = new TreeMap<>();
        saveList = new TreeMap<>();
    }

    public void add (BaseWord bw)
    {
        BaseWord old = wordsList.get(bw.name);
        if (old != null)
        {
            saveList.put (old.name, old);
            System.out.println("Overwrite: "+bw.name);
        }
        wordsList.put(bw.name, bw);
    }

    public String toString (boolean showDetail, String containing)
    {
        if (isEmpty())
        {
            return "WordsList is empty\n";
        }
        //Collections.sort(wordsList, Collections.reverseOrder());
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, BaseWord> stringBaseWordEntry : wordsList.entrySet())
        {
            BaseWord bw = stringBaseWordEntry.getValue();
            if (Utilities.containsIgnoreCase(bw.toString(false), containing))
            {
                sb.append(bw.toString(showDetail));
                if (showDetail)
                {
                    sb.append("\n");
                }
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
        for (Map.Entry<String, BaseWord> stringBaseWordEntry : wordsList.entrySet())
        {
            BaseWord bw = stringBaseWordEntry.getValue();
            if (bw instanceof StorageWord)
            {
                sb.append(bw.toString(false)).append(' ');
            }
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

    public BaseWord search (String wordName) throws Exception
    {
        if (isEmpty())
        {
            throw new Exception("WordsList is empty");
        }
        return wordsList.get(wordName.toUpperCase());
    }

    public void remove (BaseWord bw)
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
