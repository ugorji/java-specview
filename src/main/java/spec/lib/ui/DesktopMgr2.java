package spec.lib.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

public class DesktopMgr2 extends DefaultDesktopManager {

  // We'll tag internal frames that are being resized using a client
  // property with the name RESIZING.  Used in setBoundsForFrame().
  protected static final String RESIZING = "RESIZING";

  /** for using a dragPane for resizing */
  protected JComponent dragPane;

  protected boolean usingDragPane;

  /**
   * This is called any time a frame is moved or resized. This implementation keeps the frame from
   * leaving the desktop.
   */
  public void setBoundsForFrame(JComponent f, int x, int y, int w, int h) {
    if (f instanceof JInternalFrame) { // only deal w/internal frames
      JInternalFrame frame = (JInternalFrame) f;

      // Figure out if we are being resized (otherwise it's just a move)
      boolean resizing = false;
      Object r = frame.getClientProperty(RESIZING);
      if (r != null && r instanceof Boolean) {
        resizing = ((Boolean) r).booleanValue();
      }

      JDesktopPane desk = frame.getDesktopPane();
      Dimension d = desk.getSize();

      // Nothing all that fancy below, just figuring out how to adjust
      // to keep the frame on the desktop.
      if (x < 0) { // too far left?
        if (resizing) w += x; // don't get wider!
        x = 0; // flush against the left side
      } else {
        if (x + w > d.width) { // too far right?
          if (resizing) w = d.width - x; // don't get wider!
          else x = d.width - w; // flush against the right side
        }
      }
      if (y < 0) { // too high?
        if (resizing) h += y; // don't get taller!
        y = 0; // flush against the top
      } else {
        if (y + h > d.height) { // too low?
          if (resizing) h = d.height - y; // don't get taller!
          else y = d.height - h; // flush against the bottom
        }
      }

      if (!usingDragPane) {
        boolean didResize;
        didResize = (f.getWidth() != w || f.getHeight() != h);
        Rectangle rect = f.getBounds();
        f.setBounds(x, y, w, h);
        SwingUtilities.computeUnion(x, y, w, h, rect);
        f.getParent().repaint(rect.x, rect.y, rect.width, rect.height);
        if (didResize) {
          f.validate();
        }
      } else {
        Rectangle rect = dragPane.getBounds();
        dragPane.setBounds(x, y, w, h);
        SwingUtilities.computeUnion(x, y, w, h, rect);
        dragPane.getParent().repaint(rect.x, rect.y, rect.width, rect.height);
      }
    } else {
      // Set 'em the way we like 'em
      super.setBoundsForFrame(f, x, y, w, h);
    }
  }

  public void beginResizingFrame(JComponent f, int dir) {
    f.putClientProperty(RESIZING, Boolean.TRUE);
    usingDragPane = false;
    if (f.getParent() instanceof JLayeredPane) {
      if (dragPane == null) dragPane = new DragPane();
      JLayeredPane p = (JLayeredPane) f.getParent();
      p.setLayer(dragPane, Integer.MAX_VALUE);
      dragPane.setBounds(f.getX(), f.getY(), f.getWidth(), f.getHeight());
      p.add(dragPane);
      usingDragPane = true;
    }
  }

  public void resizeFrame(JComponent f, int x, int y, int w, int h) {
    setBoundsForFrame(f, x, y, w, h);
  }

  public void endResizingFrame(JComponent f) {
    f.putClientProperty(RESIZING, Boolean.FALSE);
    if (usingDragPane) {
      JLayeredPane p = (JLayeredPane) f.getParent();
      p.remove(dragPane);
      usingDragPane = false;
      setBoundsForFrame(
          f, dragPane.getX(), dragPane.getY(), dragPane.getWidth(), dragPane.getHeight());
    }
  }

  public void beginDraggingFrame(JComponent f) {
    usingDragPane = false;
    if (f.getParent() instanceof JLayeredPane) {
      if (dragPane == null) dragPane = new DragPane();
      JLayeredPane p = (JLayeredPane) f.getParent();
      p.setLayer(dragPane, Integer.MAX_VALUE);
      dragPane.setBounds(f.getX(), f.getY(), f.getWidth(), f.getHeight());
      p.add(dragPane);
      usingDragPane = true;
    }
  }

  public void dragFrame(JComponent f, int x, int y) {
    setBoundsForFrame(f, x, y, f.getWidth(), f.getHeight());
  }

  public void endDraggingFrame(JComponent f) {
    if (usingDragPane) {
      JLayeredPane p = (JLayeredPane) f.getParent();
      p.remove(dragPane);
      usingDragPane = false;
      setBoundsForFrame(
          f, dragPane.getX(), dragPane.getY(), dragPane.getWidth(), dragPane.getHeight());
    }
  }

  // DragPane class
  private class DragPane extends JComponent {
    public void paint(Graphics g) {
      g.setColor(Color.darkGray);
      g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
  };
}
