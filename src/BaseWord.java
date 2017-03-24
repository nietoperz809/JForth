import java.io.Serializable;

public abstract class BaseWord implements ExecuteIF, Comparable<BaseWord>, Serializable
{
  private static final long serialVersionUID = 7526471155622776148L;

  public BaseWord(String name, boolean immediate, boolean isPrimitive, String inf)
  {
    this.name = name;
    this.immediate = immediate;
    this.isPrimitive = isPrimitive;
    this.info = inf;
  }

  public String toString(boolean showDetail)
  {
    if (showDetail)
      return "Name: \"" + name + "\", Primitive: " + isPrimitive + ", Immediate: " + immediate;
    else
      return name + " ";
  }
  
  public boolean equals(Object o)
  {
    if (!(o instanceof BaseWord))
    {
      return false;
    }
    BaseWord bw = (BaseWord) o;
    return (name.equals(bw.name) && (isPrimitive == bw.isPrimitive));
  }
  
  public final String name;
  public boolean immediate;
  public final boolean isPrimitive;
  public final String info;

    @Override
    public int compareTo (BaseWord o)
    {
        return o.name.compareTo(this.name);
    }
}
