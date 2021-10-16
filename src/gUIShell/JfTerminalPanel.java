package gUIShell;

import jforth.JForth;
import jforth.RuntimeEnvironment;
import tools.ForthProperties;
import tools.StringStream;
import tools.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Administrator
 */
public class JfTerminalPanel extends ColorPane {
    private static final String AnsiDefaultOutput = AnsiColors.getCode(Color.yellow);
    private static final String AnsiError = AnsiColors.getCode(Color.RED);
    private static final String AnsiReset = AnsiColors.getCode(Color.white);
    // --------------------------------------------------------------------------------------------
    private final LineListener lineListener = new LineListener();
    private final JComboBox<String> combo;
    // The forth and its output channel -----------------------------------------------------------
    public StringStream _ss = new StringStream();
    public JForth _jf = new JForth(_ss.getPrintStream(), RuntimeEnvironment.GUITERMINAL, this);

    public JfTerminalPanel(JComboBox<String> combo) {
        super();
        this.combo = combo;
        combo.addActionListener(e -> {
            if (e.getActionCommand().equals("comboBoxEdited")) {
                String item = (String) combo.getEditor().getItem() + "\n";
                lineListener.fakeIn(item);
                appendANSI(item);
            }
        });

        addMouseListener(new MouseHandler(this));

        setCaret(new BlockCaret());
        addKeyListener(lineListener);
        setBackground(ForthProperties.getBkColor());
        setFont(new java.awt.Font("Monospaced", Font.PLAIN, 16)); // NOI18N
        setCaretColor(new java.awt.Color(255, 102, 102));
        setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        runForthLoop();
    }

    public String singleShot(String in) {
        _jf.singleShot(in);
        String ret = _ss.toString();
        _ss.clear();
        return ret;
    }

    /**
     * Make image from TextArea
     *
     * @return The Image
     */
    public BufferedImage getScreenShot() {
        Rectangle r = this.getVisibleRect();
        int w = r.width;
        int h = r.height;
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage image = new BufferedImage(w, h, type);
        Graphics2D g2 = image.createGraphics();
        // Translate g2 to clipping rectangle of textArea.
        g2.translate(-r.x, -r.y);
        this.paint(g2);
        g2.dispose();
        return image;
    }

    /**
     * Handle text from clipboard
     * "laladumm"
     */
    @Override
    public void paste() {
        super.paste();
        String clip = Utilities.getClipBoardString().trim();
        clip = clip.replaceAll("[\\p{C}]", " ");
        lineListener.fakeIn(clip);
    }

    /**
     * Add image to our TextPane
     *
     * @param img the Image
     */
    public void addImage(Image img) {
        appendANSI("\n");
        addIcon(img);
        appendANSI("\n");
    }

//    public static  void runCommands1By1 (String lineData, BiConsumer<String[], Integer> con) {
//        if (lineData.isEmpty())
//            return;
//        // Generate multiple inputs from single line
//        String[] arr = lineData.split("\\s+");
//        if (arr.length == 0) {
//            arr = new String[]{"\n"};
//        } else if (arr[0].equals(":") && arr[arr.length - 1].equals(";")) {
//            arr = new String[]{lineData};
//        }
//        for (int n = 0; n < arr.length; n++) {
//            con.accept(arr, n);
//        }
//    }

    /**
     * Initialize and start Forth thread
     */
    private void runForthLoop() {
        try {
            _jf.executeFile("autoexec.4th");
        } catch (Exception e) {
            System.out.println("autoexec file not found");
        }
        _jf.singleShot("");
        appendANSI(_ss.toString());
        _ss.clear();

        Utilities.executeThread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                String lineData = Utilities.translateBackspace(lineListener.getBufferedLine());
                if (!lineData.equals("\n"))
                    combo.insertItemAt (lineData, 0);
                JForth.runCommands1By1 (lineData, (arr, idx) -> {
                    boolean res = _jf.singleShot(arr[idx]);
                    String txt = _ss.toString();
                    _ss.clear();
                    if (!(txt.startsWith(" OK\n") && idx != arr.length - 1)) { // empty result
                        if (res) {
                            txt = AnsiDefaultOutput + txt;
                        } else {
                            txt = AnsiError + txt;
                        }
                        txt = txt.replace("JFORTH", AnsiReset + "JFORTH");
                        appendANSI(txt);
                    }
                });
            }
        });
    }

    /**
     * Get single char from input buffer
     * Blocks if there is none
     *
     * @return the character
     */
    public char getKey() {
        return lineListener.getBufferedChar();
    }

    /**
     * Halt line input but keep single char input runing
     *
     * @param lock true if line input is disabled
     */
    public void lockLineInput(boolean lock) {
        lineListener.lock(lock);
        lineListener.reset();
    }
}
