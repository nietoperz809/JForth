package jforth;

import java.util.EmptyStackException;
import java.util.Stack;

public class OStack extends Stack<Object>
{
    private final Stack<Object> saveStack = new SizedStack<>(10);

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

    public Object get2(int n)
    {
        if (isEmpty())
            return null;
        return super.get(n);
    }

    public Object pop() throws EmptyStackException
    {
        Object o = super.pop();
        saveStack.push (o);
        return o;
    }

    /**
     * Writes last popped object back on stack
     * @return true if done
     */
    public boolean unpop()
    {
        if (saveStack.isEmpty())
            return false;
        Object o = saveStack.pop();
        push(o);
        return true;
    }
}
