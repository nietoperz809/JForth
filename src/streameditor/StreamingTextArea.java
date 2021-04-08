package streameditor;

import jforth.JForth;
import jforth.RuntimeEnvironment;
import jforth.Utilities;
import tools.StringStream;

import javax.swing.*;
import java.awt.*;

/**
 * @author Administrator
 */
public class StreamingTextArea extends JTextArea //implements Runnable
{
    private final LineListener lineListener = new LineListener();

    public StreamingTextArea ()
    {
        super();
        setCaret(new BlockCaret());
        addKeyListener (lineListener);
        init();
        runForth();
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
                String lineData = Utilities.performBackspace (lineListener.getBufferedLine());
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

//    @Override
//    public void paste ()
//    {
//        super.paste();
//        String clip = Utilities.getClipBoardString();
//        if (clip == null)
//            return;
//
//        String[] split = clip.split("\\n");
//        for (String s : split)
//        {
//            try
//            {
//                lineBuffer.put(s);
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void fakeIn (String s)
//    {
//        try {
//            lineBuffer.put(s);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

}
