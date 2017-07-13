package webserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jforth.JForth;
import jforth.Utilities;
import tools.StringStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.concurrent.Executors;

public class SimpleWebserver
{
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

    private StringStream _ss = new StringStream();
    private JForth forth = new JForth(_ss.getPrintStream());

    private HttpHandler httpHandler = e ->
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
            os.write(cmd.getBytes());
            os.write("\n".getBytes());
            os.write(erg.getBytes(), 0, erg.length() - 2);
            _ss.clear();
        }
        else if (split[0].equals("/headline"))
        {
            os.write(Utilities.buildInfo.getBytes());
        }
        else if (txt.equals("/jquery.js")) //send jquery
        {
            SimpleWebserver.this.sendResource("jquery-3.2.1.min.js", os);
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
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 10);
        server.setExecutor(Executors.newCachedThreadPool()); // multiple Threads
        server.createContext("/", httpHandler);
        server.start();
    }

    private void sendResource (String name, OutputStream os) throws IOException
    {
        InputStream jqStream = ClassLoader.getSystemResourceAsStream(name);
        byte[] buff = new byte[1024];
        for (; ; )
        {
            int r = jqStream.read(buff);
            if (r == -1)
            {
                break;
            }
            os.write (buff, 0, r);
        }
    }
}
