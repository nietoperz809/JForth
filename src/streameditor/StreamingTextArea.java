package streameditor;

import jforth.JForth;
import jforth.RuntimeEnvironment;
import jforth.Utilities;
import tools.StringStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Administrator
 */
public class StreamingTextArea extends JTextArea //implements Runnable
{
    public final ArrayBlockingQueue<String> lineBuffer = new ArrayBlockingQueue<>(128,true);

    public StreamingTextArea ()
    {
        super();
        setCaret(new BlockCaret());
        setKeyListener();
        init();
        runForth();
    }

    private String handleBackspace (String in)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray())
        {
            if (c == '\b')
            {
                if (sb.length() > 0)
                    sb.setLength(sb.length()-1);
            }
            else
                sb.append(c);
        }
        return sb.toString();
    }

    private void addText(StringStream ss)
    {
        int cp = getCaretPosition();
        String txt = ss.toString();
        ss.clear();
        insert(txt, cp);
        setCaretPosition(cp+txt.length());
    }

    private void runForth()
    {
        StringStream _ss = new StringStream();
        JForth jForth = new JForth (_ss.getPrintStream(), RuntimeEnvironment.GUITERMINAL, this);
        jForth.singleShot("");
        addText(_ss);
        Utilities.execute(() -> {
            for(;;)
            {
                String lineData = handleBackspace(getBufferedLine());
                System.out.println(lineData);
                jForth.singleShot(lineData);
                addText(_ss);
            }
        });
    }

    private void init()
    {
        setBackground(new java.awt.Color(0, 0, 153));
        setForeground(new java.awt.Color(255, 255, 102));
        setColumns(80);
        setFont(new java.awt.Font("Monospaced", Font.PLAIN, 16)); // NOI18N
        setLineWrap(true);
        setRows(20);
        setCaretColor(new java.awt.Color(255, 102, 102));
        setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
    }

    private void setKeyListener()
    {
        this.addKeyListener(new KeyListener()
        {
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
        });
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

    @Override
    public void paste ()
    {
        super.paste();
        String clip = Utilities.getClipBoardString();
        if (clip == null)
            return;

        String[] split = clip.split("\\n");
        for (String s : split)
        {
            try
            {
                lineBuffer.put(s);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void fakeIn (String s)
    {
        try {
            lineBuffer.put(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
