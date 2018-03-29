package jforth.forthwords;

import jforth.*;


public class PredefinedWords
{
    private static final String IMMEDIATE = "__immediate";
    public final JForth _jforth;
    public final WordsList _wl;
    public final WordHelpers _help = new WordHelpers();

    public static final String SAVEHIST = "saveHist";
    public static final String PLAYHIST = "playHist";

    public PredefinedWords (JForth jf, WordsList wl)
    {
        this._wl = wl;
        this._jforth = jf;
        Filler1 _f1 = new Filler1();
        _f1.fill(wl, this);
        Filler2 _f2 = new Filler2();
        _f2.fill(wl, this);
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
