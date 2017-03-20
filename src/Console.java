import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Console extends JPanel
{
  private static final int COLUMNS = 80;
  private static final int ROWS = 50;
  
  private JTextArea display;
  private JScrollPane scrollPane;
  private JTextField textInput;

  Console()
  {
    display = new JTextArea();
    display.setColumns(COLUMNS);
    display.setRows(ROWS);
    display.setBackground(Color.white);
    display.setForeground(Color.black);
    display.setLineWrap(true);
    display.setText("");
    display.setEditable(true);
    scrollPane = new JScrollPane(display);
    add(scrollPane);
    System.setOut(new PrintStream(new TextAreaOutputStream(display)));
    System.setErr(new PrintStream(new TextAreaOutputStream(display)));
  }

  public JTextArea getDisplay()
  {
    return display;
  }
}

