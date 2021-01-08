package jforth;

import tools.TwoFuncs;

import java.io.Serializable;

//public abstract class BaseWord implements ExecuteIF, Comparable<BaseWord>, Serializable
public abstract class BaseWord implements TwoFuncs<OStack, OStack, Integer>, Comparable<BaseWord>, Serializable
{
    private static final long serialVersionUID = 7526471155622776148L;
    public final String name;
    private final String info;
    boolean immediate;

    public BaseWord (String name, boolean immediate, String inf)
    {
        this.name = name.toUpperCase();
        this.immediate = immediate;
        this.info = inf;
    }

    public BaseWord ()
    {
        this.name = "";
        this.immediate = false;
        this.info = null;
    }

    public String toString (boolean showDetail)
    {
        if (showDetail && info != null)
        {
            return String.format("%-15s -- %s", name, info);
        }
        else
        {
            return name + " ";
        }
    }

    public String getInfo() throws Exception
    {
        if (info == null)
            throw new Exception ("Info no available");
        return info;
    }

    public boolean equals (Object o)
    {
        if (!(o instanceof BaseWord))
        {
            return false;
        }
        BaseWord bw = (BaseWord) o;
        return (name.equals(bw.name));
    }

    @Override
    public int compareTo (BaseWord o)
    {
        return o.name.compareTo(this.name);
    }
}
