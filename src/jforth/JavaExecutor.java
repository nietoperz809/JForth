package jforth;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.CharBuffer;
import java.util.*;

/**
 * Simple interface to Java compiler using JSR 199 Compiler API.
 */
public class JavaExecutor
{
    private javax.tools.JavaCompiler tool;
    private StandardJavaFileManager stdManager;

    public JavaExecutor ()
    {
        tool = ToolProvider.getSystemJavaCompiler();
        if (tool == null)
        {
            throw new RuntimeException("Could not get Java compiler. Please, ensure that JDK is used instead of JRE.");
        }
        stdManager = tool.getStandardFileManager(null, null, null);
    }

    /**
     * Compile a single static method.
     */
    public Method compileStaticMethod (final String methodName, final String className,
                                       final String source)
            throws ClassNotFoundException
    {
        final Map<String, byte[]> classBytes = compile(className + ".java", source);
        final MemoryClassLoader classLoader = new MemoryClassLoader(classBytes);
        final Class<?> clazz = classLoader.loadClass(className);
        final Method[] methods = clazz.getDeclaredMethods();
        for (final Method method : methods)
        {
            if (method.getName().equals(methodName))
            {
                if (!method.isAccessible())
                {
                    method.setAccessible(true);
                }
                return method;
            }
        }
        throw new NoSuchMethodError(methodName);
    }


    public Map<String, byte[]> compile (String fileName, String source)
    {
        return compile(fileName, source, new PrintWriter(System.err), null, null);
    }


    /**
     * compile given String source and return bytecodes as a Map.
     *
     * @param fileName   source fileName to be used for error messages etc.
     * @param source     Java source as String
     * @param err        error writer where diagnostic messages are written
     * @param sourcePath location of additional .java source files
     * @param classPath  location of additional .class files
     */
    private Map<String, byte[]> compile (String fileName, String source,
                                         Writer err, String sourcePath, String classPath)
    {
        // to collect errors, warnings etc.
        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<JavaFileObject>();

        // create a new memory JavaFileManager
        MemoryJavaFileManager fileManager = new MemoryJavaFileManager(stdManager);

        // prepare the compilation unit
        List<JavaFileObject> compUnits = new ArrayList<JavaFileObject>(1);
        compUnits.add(fileManager.makeStringSource(fileName, source));

        return compile(compUnits, fileManager, err, sourcePath, classPath);
    }

    private Map<String, byte[]> compile (final List<JavaFileObject> compUnits,
                                         final MemoryJavaFileManager fileManager,
                                         Writer err, String sourcePath, String classPath)
    {
        // to collect errors, warnings etc.
        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<JavaFileObject>();

        // javac options
        List<String> options = new ArrayList<String>();
        options.add("-Xlint:all");
        //       options.add("-g:none");
        options.add("-deprecation");
        if (sourcePath != null)
        {
            options.add("-sourcepath");
            options.add(sourcePath);
        }

        if (classPath != null)
        {
            options.add("-classpath");
            options.add(classPath);
        }

        // create a compilation task
        javax.tools.JavaCompiler.CompilationTask task =
                tool.getTask(err, fileManager, diagnostics,
                        options, null, compUnits);

        if (task.call() == false)
        {
            PrintWriter perr = new PrintWriter(err);
            for (Diagnostic diagnostic : diagnostics.getDiagnostics())
            {
                perr.println(diagnostic);
            }
            perr.flush();
            return null;
        }

        Map<String, byte[]> classBytes = fileManager.getClassBytes();
        try
        {
            fileManager.close();
        }
        catch (IOException exp)
        {
        }

        return classBytes;
    }
}


/**
 * JavaFileManager that keeps compiled .class bytes in memory.
 */
@SuppressWarnings("unchecked")
final class MemoryJavaFileManager extends ForwardingJavaFileManager
{

    /**
     * Java source file extension.
     */
    private final static String EXT = ".java";

    private Map<String, byte[]> classBytes;

    public MemoryJavaFileManager (JavaFileManager fileManager)
    {
        super(fileManager);
        classBytes = new HashMap<>();
    }

    static JavaFileObject makeStringSource (String fileName, String code)
    {
        return new StringInputBuffer(fileName, code);
    }

    static URI toURI (String name)
    {
        File file = new File(name);
        if (file.exists())
        {
            return file.toURI();
        }
        else
        {
            try
            {
                final StringBuilder newUri = new StringBuilder();
                newUri.append("mfm:///");
                newUri.append(name.replace('.', '/'));
                if (name.endsWith(EXT))
                {
                    newUri.replace(newUri.length() - EXT.length(), newUri.length(), EXT);
                }
                return URI.create(newUri.toString());
            }
            catch (Exception exp)
            {
                return URI.create("mfm:///com/sun/script/java/java_source");
            }
        }
    }

    public Map<String, byte[]> getClassBytes ()
    {
        return classBytes;
    }

    public JavaFileObject getJavaFileForOutput (JavaFileManager.Location location,
                                                String className,
                                                Kind kind,
                                                FileObject sibling) throws IOException
    {
        if (kind == Kind.CLASS)
        {
            return new ClassOutputBuffer(className);
        }
        else
        {
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
    }

    public void flush () throws IOException
    {
    }

    public void close () throws IOException
    {
        classBytes = null;
    }

    /**
     * A file object used to represent Java source coming from a string.
     */
    private static class StringInputBuffer extends SimpleJavaFileObject
    {
        final String code;

        StringInputBuffer (String fileName, String code)
        {
            super(toURI(fileName), Kind.SOURCE);
            this.code = code;
        }

        public CharBuffer getCharContent (boolean ignoreEncodingErrors)
        {
            return CharBuffer.wrap(code);
        }
    }

    /**
     * A file object that stores Java bytecode into the classBytes map.
     */
    private class ClassOutputBuffer extends SimpleJavaFileObject
    {
        private String name;

        ClassOutputBuffer (String name)
        {
            super(toURI(name), Kind.CLASS);
            this.name = name;
        }

        public OutputStream openOutputStream ()
        {
            return new FilterOutputStream(new ByteArrayOutputStream())
            {
                public void close () throws IOException
                {
                    out.close();
                    ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                    classBytes.put(name, bos.toByteArray());
                }
            };
        }
    }
}


/**
 * ClassLoader that loads .class bytes from memory.
 */
final class MemoryClassLoader extends URLClassLoader
{
    private Map<String, byte[]> classBytes;

    public MemoryClassLoader (Map<String, byte[]> classBytes, String classPath)
    {
        this(classBytes, classPath, ClassLoader.getSystemClassLoader());
    }

    public MemoryClassLoader (Map<String, byte[]> classBytes,
                              String classPath, ClassLoader parent)
    {
        super(toURLs(classPath), parent);
        this.classBytes = classBytes;
    }

    private static URL[] toURLs (String classPath)
    {
        if (classPath == null)
        {
            return new URL[0];
        }

        List<URL> list = new ArrayList<URL>();
        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while (st.hasMoreTokens())
        {
            String token = st.nextToken();
            File file = new File(token);
            if (file.exists())
            {
                try
                {
                    list.add(file.toURI().toURL());
                }
                catch (MalformedURLException mue)
                {
                }
            }
            else
            {
                try
                {
                    list.add(new URL(token));
                }
                catch (MalformedURLException mue)
                {
                }
            }
        }
        URL[] res = new URL[list.size()];
        list.toArray(res);
        return res;
    }

    public MemoryClassLoader (Map<String, byte[]> classBytes)
    {
        this(classBytes, null, ClassLoader.getSystemClassLoader());
    }

    public Class load (String className) throws ClassNotFoundException
    {
        return loadClass(className);
    }

    public Iterable<Class> loadAll () throws ClassNotFoundException
    {
        List<Class> classes = new ArrayList<Class>(classBytes.size());
        for (String name : classBytes.keySet())
        {
            classes.add(loadClass(name));
        }
        return classes;
    }

    protected Class findClass (String className) throws ClassNotFoundException
    {
        byte[] buf = classBytes.get(className);
        if (buf != null)
        {
            // clear the bytes in map -- we don't need it anymore
            classBytes.put(className, null);
            return defineClass(className, buf, 0, buf.length);
        }
        else
        {
            return super.findClass(className);
        }
    }
}

