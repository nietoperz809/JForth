package jforth;

public class LoopControlWord extends BaseWord
{
    final int indexIncrement;

    public LoopControlWord (Integer indexIncrement)
    {
        super("", false, null);
        this.indexIncrement = indexIncrement;
    }

    public Integer apply(OStack dStack, OStack vStack)
    {
        if (vStack.size() < 2)
        {
            return 0;
        }
        Object o1 = vStack.pop();
        Object o2 = vStack.peek();
        if ((o1 instanceof Long) && (o2 instanceof Long))
        {
            long index = (Long) o1;
            long limit = (Long) o2;
            index += 1;
            if (index >= limit)
            {
                vStack.pop();
                return 1;
            }
            else
            {
                vStack.push(index);
                return indexIncrement;
            }
        }
        else
        {
            return 0;
        }
    }
}
