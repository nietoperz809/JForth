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
    private final JComboBox<String> combo;

    public StreamingTextArea (JComboBox<String> combo)
    {
        super();

        this.combo = combo;
        //combo.setEditable(true);
        combo.addActionListener(e -> {
            if (e.getActionCommand().equals("comboBoxEdited"))
            {
                String item = (String) combo.getEditor().getItem();
                lineListener.fakeIn(item);
                appendText(item);
            }
        });

        setCaret(new BlockCaret());
        addKeyListener (lineListener);
        init();
        runForth();
    }

    private void appendText(StringStream ss)
    {
        appendText(ss.toString());
        ss.clear();
    }

    private void appendText(String txt)
    {
        int cp = getCaretPosition();
        insert(txt, cp);
        setCaretPosition(cp+txt.length());
    }

    private void runForth()
    {
        StringStream _ss = new StringStream();
        JForth jForth = new JForth (_ss.getPrintStream(), RuntimeEnvironment.GUITERMINAL, this);
        jForth.singleShot("");
        appendText(_ss);
        Utilities.execute(() -> {
            for(;;)
            {
                String lineData = Utilities.performBackspace (lineListener.getBufferedLine());
                if (lineData.isEmpty())
                    continue;
                combo.insertItemAt(lineData, 0);
                jForth.singleShot(lineData);
                appendText(_ss);
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

    public char getKey() {
        return lineListener.getBufferedChar();
    }

    public void lockLineInput(boolean lock) {
        lineListener.lock (lock);
        lineListener.reset();
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

}
