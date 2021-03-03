package spec.ui;

import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;

public class ExtendedPrintstream extends PrintStream {
  private JTextArea textArea;
  private OutputStream out;

  private StringBuffer strBuffer = new StringBuffer();

  /**
   * This takes each String that should be printed to the output stream and appends it to the text
   * area
   *
   * <p>It thus only overrides the print (String) and println (String) methods
   */
  public ExtendedPrintstream(OutputStream out, JTextArea textArea) {
    super(out);
    this.textArea = textArea;
    this.out = out;
  }

  public void write(int i) {
    super.write(i);

    strBuffer.append((char) i);

    if (i == '\n') transferToTextArea();
  }

  public void print(String s) {
    // super.print (s);
    strBuffer.append(s);

    if (s.indexOf('\n') >= 0) transferToTextArea();
  }

  public void println(String s) {
    // super.println (s); // cannot use this ... since it will call out print (String)
    // super.print (s);
    // super.print ('\n');
    strBuffer.append(s);
    strBuffer.append('\n');

    transferToTextArea();
  }

  protected void transferToTextArea() {
    textArea.append(strBuffer.toString());
    int length = strBuffer.length();
    strBuffer.delete(0, length);
  }
}
