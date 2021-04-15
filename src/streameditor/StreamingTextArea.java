package streameditor;

import jforth.JForth;
import jforth.RuntimeEnvironment;
import tools.Utilities;
import tools.ForthProperties;
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
        setBackground(ForthProperties.getBkColor());
        setFont(new java.awt.Font("Monospaced", Font.PLAIN, 16)); // NOI18N
        setCaretColor(new java.awt.Color(255, 102, 102));
        setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        runForthLoop();
    }

    /**
     * Handle text from clipboard
     */
    @Override
    public void paste ()
    {
        super.paste();
        String clip = Utilities.getClipBoardString();
        String[] split = clip.split("\\n");
        for (String s : split)
        {
            lineListener.fakeIn(s);
        }
    }

    /**
     * Add image to our TextPane
     * @param img the Image
     */
    public void addImage (Image img)
    {
        appendANSI("\n");
        addIcon(img);
        appendANSI("\n");
    }

    /**
     * Add Text and does text coloring
     * @param txt
     */
    private void appendText(String txt)
    {
        try {
            appendANSI(txt);
            setCaretPosition (getDocument().getLength());
        } catch (Exception e) {
            System.out.println("HUH?");;
        }
    }

    /**
     * Initialize and start Forth thread
     */
    private void runForthLoop()
    {
        StringStream _ss = new StringStream();
        _ss.getPrintStream().println(Utilities.buildInfo);
        JForth jForth = new JForth (_ss.getPrintStream(), RuntimeEnvironment.GUITERMINAL, this);
        try {
            jForth.executeFile("autoexec.4th");
        } catch (Exception e) {
            System.out.println("autoexec file not found");
        }
        jForth.singleShot("");
        appendText(_ss.toString());
        _ss.clear();

        Utilities.executeThread(() -> {
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

    /**
     * Get single char from input buffer
     * Blocks if there is none
     * @return the character
     */
    public char getKey() {
        return lineListener.getBufferedChar();
    }

    /**
     * Halt line input but keep single char input runing
     * @param lock true if line input is disabled
     */
    public void lockLineInput(boolean lock) {
        lineListener.lock (lock);
        lineListener.reset();
    }
}
