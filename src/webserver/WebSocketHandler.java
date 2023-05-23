package webserver;

import jforth.JForth;
import jforth.RuntimeEnvironment;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import tools.StringStream;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Administrator on 7/3/2017.
 */
public class WebSocketHandler extends BaseWebSocketHandler
{
    private final ArrayBlockingQueue<String> receiveQ = new ArrayBlockingQueue<>(100);
    private WebSocketConnection conn;
    private final StringStream _ss = new StringStream();
    private final JForth forth = new JForth(_ss.getPrintStream(), RuntimeEnvironment.WEBSERVER);

    public String read()
    {
        try
        {
            return receiveQ.take();
        }
        catch (InterruptedException ignored)
        {
            //e.printStackTrace();
        }
        return null;
    }

    public boolean write (String s)
    {
        if (conn == null)
            return false;
        conn.send(s);
        return true;
    }

    @Override
    public void onOpen (WebSocketConnection connection)
    {
        conn = connection;
    }

    @Override
    public void onClose(WebSocketConnection connection)
    {
        conn = null;
    }

    @Override
    public void onMessage (WebSocketConnection connection, String message)
    {
        //System.out.println ("from websocket: "+ message);
        JForth.runCommands1By1 (message, (cmdArray, idx) -> {
            forth.singleShot(cmdArray[idx]);
            String erg = _ss.getAndClear();
            if (!(erg.startsWith(" OK\n") && idx != cmdArray.length - 1)) { // empty result
                erg = erg.replace("·", "•"); // replace morse dots
                write(erg);
            }
        });

        //write ("echo:" + message);
    }
}
