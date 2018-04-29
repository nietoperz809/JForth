package jforth.forthwords;

import jforth.*;


public class PredefinedWords
{
    private static final String IMMEDIATE = "__immediate";
    final JForth _jforth;
    final WordsList _wl;

    public static final String SAVEHIST = "saveHist";
    public static final String PLAYHIST = "playHist";

    public PredefinedWords (JForth jf, WordsList wl)
    {
        this._wl = wl;
        this._jforth = jf;
        Filler1.fill(wl, this);
        Filler2.fill(wl, this);
    }

    /**
     * Create an immediate word immediately execution of new words
     */
    public void createTemporaryImmediateWord ()
    {
        if (_jforth.wordBeingDefined == null) // Loop in direct mode
        {
            try
            {
                BaseWord bw = _jforth.dictionary.search(IMMEDIATE);
                _jforth.dictionary.remove(bw);
            }
            catch (Exception unused)
            {
                //e.printStackTrace();
            }
            _jforth.compiling = true;
            _jforth.wordBeingDefined = new NonPrimitiveWord(IMMEDIATE);
        }
    }

    /**
     * Call temporary immediate word
     */
    public void executeTemporaryImmediateWord ()
    {
        if (_jforth.wordBeingDefined.name.equals(IMMEDIATE))
        {
            _jforth.interpretLine("; " + IMMEDIATE);
        }
    }

}
