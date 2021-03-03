package spec.lib.io;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * This class extends the OutputStream class to allow output to be printed to a Document ... This is
 * good for like TextArea's which use a Document to manage ... the text
 */
public class DocumentOutputStream extends OutputStream {
  private Document doc;
  private boolean closed = false;

  public DocumentOutputStream(Document doc) {
    super();
    this.doc = doc;
  }

  /** method to write a char */
  public void write(int i) throws IOException {
    if (closed) return;

    try {
      doc.insertString(doc.getLength() - 1, String.valueOf((char) i), null);
    } catch (BadLocationException e) {
    }
  }

  /** write an array of bytes */
  public void write(byte[] b, int offset, int length) throws IOException {
    if (closed) return;

    if (b == null) throw new NullPointerException("The byte array is null");
    if (offset < 0 || length < 0 || (offset + length) > b.length)
      throw new IndexOutOfBoundsException(
          "offset and length are negative or extend outside array bounds");

    String str = new String(b, offset, length);
    try {
      doc.insertString(doc.getLength() - 1, str, null);
    } catch (BadLocationException e) {
    }
  }

  public void close() {
    doc = null;
    closed = true;
  }
}
