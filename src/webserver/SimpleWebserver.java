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

public class SimpleWebserver
{
    static private final String form = "<form action=\"\" method=\"GET\">\n" +
            "<font color=\"gray\">Type commands here and press return</font> </br>" +
            "<input type=\"text\" id=\"InputID\"  name=\"msg\" value=\"\" size=\"50\">\n" +
            "</form>";
    static private final String pageStart = "<html>" +
            "<head>\n<meta charset=\"utf-8\"/>" +
            "<style>\n" +
            "\n" +
            "      p { margin-left: 3em; }\n" +
            "\n" +
            "</style>" +
            "<script type=\"text/javascript\">\n" +
            "function FocusOnInput()\n" +
            "{\n" +
            "     document.getElementById(\"InputID\").focus();\n" +
            "}\n" +
            "</script>" +
            "<script src=\"jquery.js\"></script>" +
            "</head><body bgcolor=\"#000000\" onload=\"FocusOnInput()\">" +
            "<font color=\"gray\">" + Utilities.buildInfo + "</font>" +
            "<hr><code>";
    static private final String pageEnd = "</code><hr>" + form + "</body></html>";

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

    private static void startServer (int port) throws IOException
    {
        StringStream _ss = new StringStream();
        JForth forth = new JForth(_ss.getPrintStream());

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 10);

        HttpHandler httpHandler = e ->
        {
            System.out.println("HTTP: " + e.getRequestURI().toString());
            String[] txt = e.getRequestURI().toString().split("=");
            OutputStream os = e.getResponseBody();
            e.sendResponseHeaders(200, 0);
            if (txt[0].equals("/?msg"))
            {
                String cmd = URLDecoder.decode(txt[1], "UTF-8");
                forth.singleShot(cmd);
                String erg = _ss.toString().replace("\n", "<br>");
                os.write(makePage(cmd + "<br>" + erg.substring(0, erg.length() - 2)));
                _ss.clear();
            }
            else if (txt[0].equals("/jquery.js"))
            {
                InputStream jqStream =
                        ClassLoader.getSystemResourceAsStream("jquery-3.2.1.min.js");
                byte[] buff = new byte[jqStream.available()];
                jqStream.read (buff);
                os.write(buff);
            }
            else
            {
                os.write(makePage("oops?"));
            }
            os.close();
        };

        server.createContext("/", httpHandler);
        server.start();
    }

    private static byte[] makePage (String content)
    {
        final String sb = pageStart +
                        "<p><font color=\"yellow\">" + content + "</font></p>" +
                        pageEnd;

        return sb.getBytes();
    }
}
