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
            return "WordList is empty\n";
        }
        //Collections.sort(wordsList, Collections.reverseOrder());
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, BaseWord> stringBaseWordEntry : wordsList.entrySet())
        {
            BaseWord bw = stringBaseWordEntry.getValue();
            String str = bw.toString(showDetail);
            if (Utilities.containsIgnoreCase (str, containing))
            {
                sb.append (str);
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
