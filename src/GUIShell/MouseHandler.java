package GUIShell;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {
    private JfTerminalPanel textarea;

    public MouseHandler (JfTerminalPanel tp)
    {
        textarea = tp;
    }

    final JPopupMenu popup = new JPopupMenu();
    {
        JMenuItem menuItem = new JMenuItem("Copy");
        menuItem.addActionListener(e -> {
           textarea.copy();
        });
        popup.add(menuItem);

        menuItem = new JMenuItem("Paste");
        menuItem.addActionListener(e -> {
            textarea.paste();
        });
        popup.add(menuItem);

        menuItem = new JMenuItem("Select all");
        menuItem.addActionListener(e -> {
            textarea.selectAll();
        });
        popup.add(menuItem);

        menuItem = new JMenuItem("Clear");
        menuItem.addActionListener(e -> {
            textarea.singleShot("cls");
        });
        popup.add(menuItem);

    }

    @Override
    public void mousePressed(MouseEvent e) {
        showPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showPopup(e);
    }

    private void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }
}
