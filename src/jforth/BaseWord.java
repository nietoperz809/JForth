package jforth;

import java.io.Serializable;

public abstract class BaseWord implements ExecuteIF, Comparable<BaseWord>, Serializable
{
    private static final long serialVersionUID = 7526471155622776148L;
    public final String name;
    public final boolean isPrimitive;
    private final String info;
    boolean immediate;

    public BaseWord (String name, boolean immediate, boolean isPrimitive, String inf)
    {
        this.name = name.toUpperCase();
        this.immediate = immediate;
        this.isPrimitive = isPrimitive;
        this.info = inf;
    }

    public String toString (boolean showDetail)
    {
        if (showDetail)
        //return "Name: \"" + name + "\", Primitive: " + isPrimitive + ", Immediate: " + immediate;
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
        return (name.equals(bw.name) && (isPrimitive == bw.isPrimitive));
    }

    @Override
    public int compareTo (BaseWord o)
    {
        return o.name.compareTo(this.name);
    }
}
