package streameditor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ArrayBlockingQueue;

public class LineListener implements KeyListener {

    public final ArrayBlockingQueue<String> lineBuffer = new ArrayBlockingQueue<>(128,true);
    StringBuilder sb = new StringBuilder();

    @Override
    public void keyTyped (KeyEvent e)
    {
        char c = e.getKeyChar();
        sb.append(c);
    }

    @Override
    public void keyPressed (KeyEvent e)
    {
    }

    @Override
    public void keyReleased (KeyEvent e)
    {
        if (e.getKeyChar() == '\n')
        {
            try
            {
                lineBuffer.put(sb.toString());
                sb.setLength(0);
            }
            catch (InterruptedException interruptedException)
            {
                interruptedException.printStackTrace();
            }
        }
    }

    public String getBufferedLine()
    {
        try
        {
            return lineBuffer.take();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return "";
        }
    }
}
