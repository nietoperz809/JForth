package webserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jforth.JForth;
import tools.StringStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;

public class SimpleWebserver
{
    public static void start (int port)
    {
        new Thread(() ->
        {
            try
            {
                startServer(port);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }).start();
    }

    private static void sendContent (String name, OutputStream os) throws IOException
    {
        InputStream jqStream = ClassLoader.getSystemResourceAsStream(name);
        byte[] buff = new byte[jqStream.available()];
        jqStream.read (buff);
        os.write(buff);
    }

    private static void startServer (int port) throws IOException
    {
        StringStream _ss = new StringStream();
        JForth forth = new JForth(_ss.getPrintStream());

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 10);

        HttpHandler httpHandler = e ->
        {
            //System.out.println("HTTP: " + e.getRequestURI().toString());
            String txt = e.getRequestURI().toString();
            String[] split = txt.split("\\?");
            OutputStream os = e.getResponseBody();
            e.sendResponseHeaders(200, 0);
            if (txt.equals("/"))
            {
                sendContent("page.html", os);
            }
            else if (split[0].equals("/forth"))
            {
                String cmd = URLDecoder.decode(split[1],"UTF-8");
                forth.singleShot(cmd);
                String erg = _ss.toString();
                os.write (cmd.getBytes());
                os.write ("\n".getBytes());
                os.write(erg.getBytes(), 0, erg.length()-2);
                _ss.clear();
            }
            else if (txt.equals("/jquery.js")) //send jquery
            {
                sendContent("jquery-3.2.1.min.js", os);
            }
            else if (txt.equals("/favicon.ico")) //send jquery
            {
                sendContent("tiger.png", os);
            }
            os.close();
        };
        server.createContext("/", httpHandler);
        server.start();
    }
}
