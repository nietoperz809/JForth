public final class StorageWord extends BaseWord
{
  public StorageWord(String name, int size, boolean isArray)
  {
    super(name, false, false);
    this.size = size;
    array = new Object [size];
    this.isArray = isArray;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    vStack.push(this);
    return 1;
  }

  public boolean isArray()
  {
    return isArray;
  }

  public Object fetch(int offset)
  {
    if ((offset < 0) || (offset >= size))
      return null;
    return array[offset];
  }

  public int store(Object data, int offset)
  {
    if ((offset < 0) || (offset >= size))
      return 0;
    array[offset] = data;
    return 1;
  }

  public int plusStore(Object data, int offset)
  {
    if ((offset < 0) || (offset >= size))
      return 0;
    Object o1 = array[offset];
    Object o2 = data;
    if ((o1 instanceof Long) && (o2 instanceof Long))
    {
      long i1 = ((Long) o1).longValue();
      long i2 = ((Long) o2).longValue();
      array[offset] = new Long(i1 + i2);
    }
    else if ((o1 instanceof String) && (o2 instanceof String))
    {
      String s1 = (String) o1;
      String s2 = (String) o2;
      array[offset] = s1 + s2;
    }
    else
      return 0;
    return 1;
  }

  private int size;
  private Object [] array = null;
  private boolean isArray;
}
