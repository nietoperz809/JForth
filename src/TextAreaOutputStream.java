import javax.swing.*;
import java.io.*;

  public class TextAreaOutputStream extends OutputStream
  {
    JTextArea textArea;

    public TextAreaOutputStream(JTextArea ta)
    {
      super();
      textArea = ta;
    }

    public void write(int i)
    {
      textArea.append(Character.toString((char)i));
    }

    public void write(char[] buffer, int offset, int length)
    {
      String s = new String(buffer, offset, length);
      textArea.append(s);
    }
  }
