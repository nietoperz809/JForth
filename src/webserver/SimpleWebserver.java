package webserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jforth.JForth;
import tools.StringStream;

import java.io.IOException;
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

    private static void startServer (int port) throws IOException
    {
        StringStream _ss = new StringStream();
        JForth forth = new JForth(_ss.getPrintStream());

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 10);

        HttpHandler httpHandler = e ->
        {
            String[] txt = e.getRequestURI().toString().split("=");
            OutputStream os = e.getResponseBody();
            e.sendResponseHeaders(200, 0);
            if (txt.length == 2)
            {
                String cmd = URLDecoder.decode(txt[1], "UTF-8");
                forth.singleShot(cmd);
                String erg = _ss.toString().replace("\n", "<br>");
                os.write(makePage(cmd + "<br>" + erg));
                _ss.clear();
            }
            else
            {
                os.write(makePage(">"));
            }
            os.close();
        };

        server.createContext("/", httpHandler);
        server.start();
    }

    private static byte[] makePage (String content)
    {
        String form = "<form action=\"\" method=\"GET\">\n" +
                "<input type=\"text\" name=\"msg\" value=\"\" size=\"50\">\n" +
                "</form>";
        String sb = "<html>" +
                "<head>\n<meta charset=\"utf-8\"/>" +
                "</head><body><hr>" +
                content +
                "<hr>" +
                form +
                "</body></html>";
        return sb.getBytes();
    }
}
