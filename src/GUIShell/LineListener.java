package GUIShell;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

public class LineListener implements KeyListener {
    Robot robot;

    public LineListener() {
        super();
        try {
            robot = new Robot();
        } catch (AWTException e) {
            robot = null;
            e.printStackTrace();
        }
    }

    private final ArrayBlockingQueue<String> lineBuffer = new ArrayBlockingQueue<>(128, true);
    private ArrayBlockingQueue<Character> charBuffer = new ArrayBlockingQueue<>(1024, true);
    String lastStr;

    private volatile boolean locked = false;

    public void fakeIn(String s) {
        try {
            for (char c : s.toCharArray())
                charBuffer.put(c);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {
        if (e.isControlDown())
            return;
        char c = e.getKeyChar();
        if (c == '\u0008') {  // handle Backspace
            ArrayBlockingQueue<Character> cb2 = new ArrayBlockingQueue<>(1024, true);
            while (charBuffer.size() > 1) {
                cb2.add(charBuffer.poll());
            }
            charBuffer = cb2;
        } else
            charBuffer.offer(c);  // insert at tail
    }

//    public void writeKeyboard(Robot bot, String st) throws Exception {
//        String upperCase = st.toUpperCase();
//
//        for(int i = 0; i < upperCase.length(); i++) {
//            String letter = Character.toString(upperCase.charAt(i));
//            String code = "VK_" + letter;
//
//            Field f = KeyEvent.class.getField(code);
//            int keyEvent = f.getInt(null);
//
//            bot.keyPress(keyEvent);
//            bot.keyRelease(keyEvent);
//        }
//    }

    @Override
    public void keyPressed(KeyEvent e) {
//        if (e.getKeyCode() == 35) {
//            try {
//                writeKeyboard(robot, lastStr);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (locked)
            return;

        if (e.getKeyChar() == '\n') {
            try {
                lastStr = charBuffer.stream().map(String::valueOf).collect(Collectors.joining());
                lineBuffer.put(lastStr);
                charBuffer.clear();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    public char getBufferedChar() {
        try {
            return charBuffer.take();
        } catch (InterruptedException e) {
            return 'X';
        }
    }

    public String getBufferedLine() {
        try {
            return lineBuffer.take();
        } catch (InterruptedException e) {
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
