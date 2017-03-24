import java.util.Stack;

public class OStack extends Stack<Object>
{
    SizedStack<Object> saveStack = new SizedStack<>(1000);

    public Object get (int n)
    {
        try
        {
            return super.get(n);
        }
        catch (Exception unused)
        {
            return null;
        }
    }

    public Object pop()
    {
        Object o = super.pop();
        saveStack.push (o);
        return o;
    }

    public void unpop()
    {
        if (saveStack.isEmpty())
            return;
        Object o = saveStack.pop();
        push(o);
    }
}
