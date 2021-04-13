package streameditor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

public class LineListener implements KeyListener {

    private final ArrayBlockingQueue<String> lineBuffer = new ArrayBlockingQueue<>(128,true);
    private final ArrayBlockingQueue<Character> charBuffer = new ArrayBlockingQueue<>(1024,true);

    private volatile boolean locked = false;

    public void fakeIn (String s)
    {
        try {
            for (char c : s.toCharArray())
                charBuffer.put(c);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void keyTyped (KeyEvent e)
    {
        char c = e.getKeyChar();
        //System.out.println(0+c);
        charBuffer.offer(c);
    }

    @Override
    public void keyPressed (KeyEvent e)
    {
//        if ((e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
//            System.out.println("Pasted!");
//        }
    }

    @Override
    public void keyReleased (KeyEvent e)
    {
        if (locked)
            return;

        if (e.getKeyChar() == '\n')
        {
            try
            {
                String str =  charBuffer.stream().map(String::valueOf).collect(Collectors.joining());
                lineBuffer.put(str);
                charBuffer.clear();
            }
            catch (InterruptedException interruptedException)
            {
                interruptedException.printStackTrace();
            }
        }
    }

    public char getBufferedChar()
    {
        try {
            return charBuffer.take();
        } catch (InterruptedException e) {
            return 'X';
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

    public void reset() {
        lineBuffer.clear();
        charBuffer.clear();
    }

    public void lock(boolean lock) {
        locked = lock;
    }
}
