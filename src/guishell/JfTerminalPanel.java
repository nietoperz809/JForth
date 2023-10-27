package guishell;

import jforth.JForth;
import jforth.RuntimeEnvironment;
import tools.ForthProperties;
import tools.StringStream;
import tools.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * @author Administrator
 */
public class JfTerminalPanel extends ColorPane {
    private static final String AnsiDefaultOutput = AnsiColors.getCode(Color.yellow);
    private static final String AnsiError = AnsiColors.getCode(Color.RED);
    private static final String AnsiReset = AnsiColors.getCode(Color.GREEN);

    private final JComboBox<String> combo;
    // The forth and its output channel -----------------------------------------------------------
    public final StringStream _ss = new StringStream();
    public final JForth _jf = new JForth(_ss.getPrintStream(), RuntimeEnvironment.GUITERMINAL, this);
    private StringBuffer collector;
    //private Object waiter = new Object();
    private int ncoll;

    /**
     * Get some keystrokes (e.g. for forth accept word)
     * @param n Number of keys that must be hit leave the wait state
     * @return All keys as ons string
     * @throws Exception if smth. gone rong
     */
    public String collectKeys(int n) throws Exception{
        collector = new StringBuffer();
        ncoll = n;
        synchronized(this) {
            wait();
        }
        String str = collector.toString();
        collector = null;
        return str;
    }

    /**
     * Execute forth interpreter in separate thread
     */
    private void runForthThread() {
        (new Thread(() -> {
            String lineData = Utilities.currentLine(JfTerminalPanel.this);
            assert lineData != null;
            lineData = lineData.replace("JFORTH>", "");
            boolean ok = _jf.interpretLine(lineData);
            String response = _ss.getAndClear();
            if (ok) {
                combo.addItem(lineData.trim());
                response = AnsiDefaultOutput + response + " OK\n";
            }
            else
                response = AnsiError + _jf.LastError+"\n";
            response = response+ AnsiReset + "JFORTH> ";
            appendANSI(response);
        })).start();
    }

    /**
     * Key adapter for this GUI app
     * @return the KA itself
     */
    private KeyAdapter thisKA() {
        return new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (collector != null) {
                    collector.append(e.getKeyChar());
                    if (collector.length() >= ncoll) {
                        synchronized (JfTerminalPanel.this) {
                            JfTerminalPanel.this.notifyAll();
                        }
                    }
                }
                else if (e.getKeyChar() == '\n') {
                    runForthThread();
                }
            }
        };
    }

    public JfTerminalPanel(JComboBox<String> comboBox) {
        super();
        combo = comboBox;
        combo.addActionListener(e -> {
            if (e.getActionCommand().equals("comboBoxEdited")) {
                String item = combo.getEditor().getItem() + "\n";
                //lineListener.fakeIn(item);
                appendANSI(item);
            }
        });

        addMouseListener(new MouseHandler(this));

        setCaret(new BlockCaret());
        setBackground(ForthProperties.getBkColor());
        setFont(new java.awt.Font("Monospaced", Font.PLAIN, 16)); // NOI18N
        setCaretColor(new java.awt.Color(70, 116, 151, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        // run JForth
        addKeyListener(thisKA());

        // start ...
        try {
            _jf.executeFile("autoexec.4th");
        } catch (Exception e) {
            appendANSI("autoexec file not found");
        }
        _jf.singleShot("");
        appendANSI(_ss.getAndClear());
    }

    public String singleShot(String in) {
        _jf.singleShot(in);
        return _ss.getAndClear();
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

    //@Override
//    public void paste() {
//        super.paste();
//        System.out.println("paste");
//        String clip = Objects.requireNonNull(Utilities.getClipBoardString()).trim();
//        if (clip.startsWith("file:/")) {
//            clip = clip.substring(5);
//            try {
//                ArrayList<String> prog = loadStrings(clip);
//                for (String s : prog) {
//                    System.out.println(s);
//                }
//
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//        //appendANSI(clip);
//        //clip = clip.replaceAll("\\p{C}", " ");
//        //System.out.println(clip);
//        //lineListener.fakeIn(clip);
//    }

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
}
