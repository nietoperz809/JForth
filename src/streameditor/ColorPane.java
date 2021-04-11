package streameditor;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public abstract class ColorPane extends JTextPane {
    static final Color cReset    = Color.WHITE;
    static Color colorCurrent    = cReset;
    String remaining = "";

    public void append(Color c, String s) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        int len = getDocument().getLength(); // same value as getText().length();
        setCaretPosition(len);  // place caret at the end (with no selection)
        setCharacterAttributes(aset, false);
        replaceSelection(s); // there is no selection, so inserts at caret
    }

    /**
     * Experimental
     */
    public void addIcon (Image img) {
        Image newimg = img.getScaledInstance(120, 120,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        Icon icon = new ImageIcon(newimg);
        JLabel label = new JLabel(icon);

        StyledDocument document = (StyledDocument) getDocument();
        StyleContext context = new StyleContext();

        Style labelStyle = context.getStyle(StyleContext.DEFAULT_STYLE);

        StyleConstants.setComponent(labelStyle, label);

        setCaretPosition(document.getLength());  // place caret at the end (with no selection)

        try {
            document.insertString(document.getLength(), "I", labelStyle);
        } catch (BadLocationException badLocationException) {
            System.err.println("Oops");
        }
    }

    public void appendANSI(String s) { // convert ANSI color codes first
        int aPos = 0;   // current char position in addString
        int aIndex; // index of next Escape sequence
        int mIndex; // index of "m" terminating Escape sequence
        String tmpString;
        boolean stillSearching; // true until no more Escape sequences
        String addString = remaining + s;
        remaining = "";

        if (addString.length() > 0) {
            aIndex = addString.indexOf("\u001B"); // find first escape
            if (aIndex == -1) { // no escape/color change in this string, so just send it with current color
                append(colorCurrent,addString);
                return;
            }
// otherwise There is an escape character in the string, so we must process it

            if (aIndex > 0) { // Escape is not first char, so send text up to first escape
                tmpString = addString.substring(0,aIndex);
                append(colorCurrent, tmpString);
                aPos = aIndex;
            }
// aPos is now at the beginning of the first escape sequence

            stillSearching = true;
            while (stillSearching) {
                mIndex = addString.indexOf("m",aPos); // find the end of the escape sequence
                if (mIndex < 0) { // the buffer ends halfway through the ansi string!
                    remaining = addString.substring(aPos);
                    stillSearching = false;
                    continue;
                }
                else {
                    tmpString = addString.substring(aPos,mIndex+1);
                    colorCurrent = AnsiColors.getColor (tmpString, colorCurrent);
                }
                aPos = mIndex + 1;
// now we have the color, send text that is in that color (up to next escape)

                aIndex = addString.indexOf("\u001B", aPos);

                if (aIndex == -1) { // if that was the last sequence of the input, send remaining text
                    tmpString = addString.substring(aPos);
                    append(colorCurrent, tmpString);
                    stillSearching = false;
                    continue; // jump out of loop early, as the whole string has been sent now
                }

                // there is another escape sequence, so send part of the string and prepare for the next
                tmpString = addString.substring(aPos,aIndex);
                aPos = aIndex;
                append(colorCurrent, tmpString);

            } // while there's text in the input buffer
        }
    }
}