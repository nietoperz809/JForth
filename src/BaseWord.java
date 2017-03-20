public abstract class BaseWord implements ExecuteIF
{
  public BaseWord(String name, boolean immediate, boolean isPrimitive)
  {
    this.name = name;
    this.immediate = immediate;
    this.isPrimitive = isPrimitive;
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
  
  public String name;
  public boolean immediate;
  public boolean isPrimitive;
}
