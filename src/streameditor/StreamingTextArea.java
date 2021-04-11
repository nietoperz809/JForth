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
public class StreamingTextArea extends ColorPane
{
    private final LineListener lineListener = new LineListener();
    private final JComboBox<String> combo;
    private static final String AnsiDefaultOutput = AnsiColors.getCode(Color.yellow);
    private static final String AnsiError = AnsiColors.getCode (Color.RED);
    private static final String AnsiReset = AnsiColors.getCode (Color.white);

    public StreamingTextArea (JComboBox<String> combo)
    {
        super();
        this.combo = combo;
        combo.addActionListener(e -> {
            if (e.getActionCommand().equals("comboBoxEdited"))
            {
                String item = (String) combo.getEditor().getItem() +"\n";
                lineListener.fakeIn(item);
                appendText(item);
            }
        });

        setCaret(new BlockCaret());
        addKeyListener (lineListener);
        init();
        runForth();
    }

    public void addImage (Image img)
    {
        appendANSI("\n");
        addIcon(img);
        appendANSI("\n");
    }

    private void appendText(String txt)
    {
        try {
            appendANSI(txt);
            setCaretPosition (getDocument().getLength());
        } catch (Exception e) {
            System.out.println("HUH?");;
        }
    }

    private void runForth()
    {
        StringStream _ss = new StringStream();
        _ss.getPrintStream().println(Utilities.buildInfo);
        JForth jForth = new JForth (_ss.getPrintStream(), RuntimeEnvironment.GUITERMINAL, this);
        jForth.singleShot("");
        appendText(_ss.toString());
        _ss.clear();

        Utilities.execute(() -> {
            for(;;)
            {
                String lineData = Utilities.performBackspace (lineListener.getBufferedLine());
                if (lineData.isEmpty())
                    continue;
                if (!lineData.equals("\n"))
                    combo.insertItemAt(lineData, 0);
                boolean res = jForth.singleShot(lineData);
                String txt = _ss.toString();
                _ss.clear();

                if (res)
                {
                    txt = AnsiDefaultOutput+txt;
                }
                else
                {
                    txt = AnsiError+txt;
                }
                txt = txt.replace("JFORTH", AnsiReset+"JFORTH");
                appendText(txt);
            }
        });
    }

    private void init()
    {
        setBackground(new java.awt.Color(0, 0, 0));
        setFont(new java.awt.Font("Monospaced", Font.PLAIN, 16)); // NOI18N
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
