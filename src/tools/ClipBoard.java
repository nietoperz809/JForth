package tools;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class ClipBoard {

    public static void put(String str) throws Exception {
        Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
        board.setContents(new StringSelection(str), null);
    }

    public static String get() throws Exception {
        Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable data = board.getContents(null);
        return (String)data.getTransferData(DataFlavor.stringFlavor);
    }
}
