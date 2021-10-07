package gUIShell;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {
    private final JfTerminalPanel textarea;
    private final JPopupMenu popup = new JPopupMenu();

    public MouseHandler (JfTerminalPanel tp) {
        textarea = tp;
        addItem("Copy", e -> textarea.copy());
        addItem("Paste", e -> textarea.paste());
        addItem("Select all", e -> textarea.selectAll());
        popup.addSeparator();
        addItem("Clear", e -> textarea.singleShot("cls"));
        addItem("Stack", e -> textarea.appendANSI(textarea.singleShot(".s")));
        addItem("Words", e -> textarea.appendANSI("\r\n"+textarea.singleShot("wordsd .")));
    }

    private void addItem (String text, ActionListener acl) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(acl);
        popup.add(menuItem);
    }

    private void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()) popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        showPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showPopup(e);
    }
}
