package webserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jforth.JForth;
import jforth.RuntimeEnvironment;
import tools.BuildInfo;
import tools.StringStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executors;

public class SimpleWebserver
{
    private static HttpServer server;

    public static void stop()
    {
        if (server != null)
        {
            server.stop (1);
            server = null;
        }
    }

    public static void start (int port)
    {
        try
        {
            new SimpleWebserver().startServer(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private final StringStream _ss = new StringStream();
    private final JForth forth = new JForth(_ss.getPrintStream(), RuntimeEnvironment.WEBSERVER);

    private final HttpHandler httpHandler = e ->
    {
        //System.out.println("HTTP: " + e.getRequestURI().toString());
        String txt = e.getRequestURI().toString();
        String[] split = txt.split("\\?");
        OutputStream os = e.getResponseBody();
        e.sendResponseHeaders(200, 0);
        if (txt.equals("/"))
        {
            SimpleWebserver.this.sendResource("page.html", os);
        }
        else if (split[0].equals("/forth"))
        {
            String cmd = URLDecoder.decode(split[1], "UTF-8");
            forth.singleShot(cmd);
            String erg = _ss.toString();
            erg.replace ("·","&#xb7;"); // replace morse dots
            byte[] arr = erg.getBytes (StandardCharsets.UTF_8);
            os.write(arr, 0, arr.length - 8);
            _ss.clear ();
        }
        else if (split[0].equals("/headline"))
        {
            os.write(BuildInfo.buildInfo.getBytes());
        }
        else if (txt.equals("/jquery.js")) //send jquery
        {
            SimpleWebserver.this.sendResource("jquery-1.11.3.min.js", os);
        }
        else if (txt.equals("/favicon.ico")) //send icon
        {
            SimpleWebserver.this.sendResource("tiger.png", os);
        }
        os.flush();
        os.close();
        //System.out.println("handler leave");
    };

    private void startServer (int port) throws IOException
    {
        server = HttpServer.create(new InetSocketAddress(port), 10);
        server.setExecutor(Executors.newCachedThreadPool()); // multiple Threads
        server.createContext("/", httpHandler);
        server.start();
    }

    private void sendResource (String name, OutputStream os) throws IOException
    {
        InputStream is = ClassLoader.getSystemResourceAsStream(name);
        BufferedInputStream bis = new BufferedInputStream (Objects.requireNonNull (is));
        byte[] buff = new byte[1024];
        for (; ; )
        {
            int r = bis.read(buff);
            if (r == -1)
            {
                break;
            }
            os.write (buff, 0, r);
        }
    }
}
