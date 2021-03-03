package spec.lib.io;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.text.AbstractDocument;

/**
 * This class extends the OutputStream class to allow output to be printed to a
 * AbstractDocument.Content ... This is good for like TextArea's which use a
 * AbstractDocument.Content to manage ... the text
 */
public class ContentOutputStream extends OutputStream {
  private AbstractDocument.Content content;
  private boolean closed = false;

  public ContentOutputStream(AbstractDocument.Content content) {
    super();
    this.content = content;
  }

  /** method to write a char */
  public void write(int i) throws IOException {
    if (closed) return;
    try {
      content.insertString(content.length() - 1, String.valueOf((char) i));
      // content.append ( (char) i);
    } catch (RuntimeException exc) {
      throw exc;
    } catch (Exception exc2) {
      throw new IOException("Exception inserting string: " + exc2.toString());
    }
  }

  /** write an array of bytes */
  public void write(byte[] b, int offset, int length) throws IOException {
    if (closed) return;

    if (b == null) throw new NullPointerException("The byte array is null");
    if (offset < 0 || length < 0 || (offset + length) > b.length)
      throw new IndexOutOfBoundsException(
          "offset and length are negative or extend outside array bounds");

    try {
      String str = new String(b, offset, length);
      content.insertString(content.length() - 1, str);
      // content.append (str);
    } catch (RuntimeException exc) {
      throw exc;
    } catch (Exception exc2) {
      throw new IOException("Exception inserting string: " + exc2.toString());
    }
  }

  public void close() {
    content = null;
    closed = true;
  }
}
