import java.util.Stack;

public class OStack extends Stack<Object>
{
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
}
