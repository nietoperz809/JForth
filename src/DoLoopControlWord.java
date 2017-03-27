public final class DoLoopControlWord extends BaseWord
{
    public DoLoopControlWord ()
    {
        super("", false, false, null);
    }

    public int execute (OStack dStack, OStack vStack)
    {
        if (dStack.size() < 1)
        {
            return 0;
        }
        Object o1 = dStack.pop();
        if (o1 instanceof Double)
        {
            o1 = ((Double) o1).longValue();
        }
        Object o2;
        if (dStack.isEmpty())
        {
            o2 = Long.MAX_VALUE;
        }
        else
        {
            o2 = dStack.pop();
            if (o2 instanceof Double)
            {
                o2 = ((Double) o2).longValue();
            }
        }
        if ((o1 instanceof Long) && (o2 instanceof Long))
        {
            vStack.push(o2);
            vStack.push(o1);
            return 1;
        }
        return 0;
    }
}
