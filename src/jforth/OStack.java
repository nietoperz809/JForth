package jforth;

import java.util.Stack;

public class OStack extends Stack<Object>
{
    SizedStack<Object> saveStack = new SizedStack<>(1000);

    @Override
    public synchronized void removeAllElements ()
    {
        while (!isEmpty())
        {
            Object o = super.pop();
            saveStack.push(o);
        }
        super.removeAllElements();
    }

    public Object get (int n)
    {
        if (isEmpty())
            return null;
        return super.get(n);
    }

    public Object pop()
    {
        Object o = super.pop();
        saveStack.push (o);
        return o;
    }

    public boolean unpop()
    {
        if (saveStack.isEmpty())
            return false;
        Object o = saveStack.pop();
        push(o);
        return true;
    }
}
