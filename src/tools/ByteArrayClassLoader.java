package tools;

public class ByteArrayClassLoader extends ClassLoader
{
    private final byte[] clazz;

    /**
     * Creates a new instance of ByteArrayClassLoader
     * @param clazz a class in a byte array
     */
    public ByteArrayClassLoader (byte[] clazz)
    {
        this.clazz = clazz;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        try
        {
            return super.loadClass(name);
        }
        catch (ClassNotFoundException e)
        {
            return defineClass (name, clazz, 0, clazz.length);
        }
    }
}

