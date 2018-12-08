package jforth;

import jforth.forthwords.PredefinedWords;

import java.util.ArrayList;

public class History
{
    private static final String HISTORYFILENAME = "history";
    private final int length;
    public ArrayList<String> history;
    private int nextIndex;

    History (int length)
    {
        this.length = length;
        nextIndex = 0;
        history = new ArrayList<>(length);
    }

    public void clear ()
    {
        history.clear();
    }

    public void save () throws Exception
    {
        Utilities.fileSave(history, HISTORYFILENAME);
    }

    public void load () throws Exception
    {
        history = Utilities.fileLoad(HISTORYFILENAME);
    }

    public void add (String s)
    {
        s = s.trim();
        if (s.isEmpty())
            return;
        if (Utilities.containsIgnoreCase(s, PredefinedWords.PLAYHIST))
            return;
        if (Utilities.containsIgnoreCase(s, PredefinedWords.SAVEHIST))
            return;
        if (history.size() == length)
        {
            history.remove(0);
        }
        history.add(s);
        nextIndex = history.size();
    }

    public String getNext ()
    {
        nextIndex--;
        if (nextIndex < 0)
        {
            nextIndex = history.size() - 1;
        }
        return history.get(nextIndex);
    }
}
