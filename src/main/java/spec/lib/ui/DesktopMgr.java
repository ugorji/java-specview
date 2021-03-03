package spec.lib.ui;

import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * This desktop manager prevents any frames from extending beyond the desktop.
 *
 * <p>This also more closely follows the MDI model than the DefaultDesktopManager. ... which
 * requires that the selected and activated child frames are the same, and that that frame always be
 * the top-most window.
 */
public class DesktopMgr extends DefaultDesktopManager {

  // We'll tag internal frames that are being resized using a client
  // property with the name RESIZING.  Used in setBoundsForFrame().
  protected static final String RESIZING = "RESIZING";

  /* The frame which is currently selected/activated.
   * We store this value to enforce MDI's single-selection model.
   */
  JInternalFrame currentFrame;

  /* The list of frames, sorted by order of creation.
   * This list is necessary because by default the order of
   * child frames in the JDesktopPane changes during frame
   * activation (the activated frame is moved to index 0).
   * We preserve the creation order so that "next" and "previous"
   * frame actions make sense.
   */
  java.util.List childFrames = new ArrayList(1);

  /** put the client property when you want to begin resizing the frame */
  public void beginResizingFrame(JComponent f, int dir) {
    f.putClientProperty(RESIZING, Boolean.TRUE);
  }

  /** put the client property when you want to end resizing the frame */
  public void endResizingFrame(JComponent f) {
    f.putClientProperty(RESIZING, Boolean.FALSE);
  }

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
    }

    // Set 'em the way we like 'em
    super.setBoundsForFrame(f, x, y, w, h);
  }

  public void closeFrame(JInternalFrame f) {
    JInternalFrame nextFrame = getFrame(true);
    super.closeFrame(f);
    childFrames.remove(f);

    if (nextFrame != null) {
      activateFrame(nextFrame);
      currentFrame = nextFrame;
    }
  }

  public void activateFrame(JInternalFrame f) {
    try {
      super.activateFrame(f);

      // If this is the first activation, add to child list.
      if (childFrames.indexOf(f) == -1) {
        childFrames.add(f);
      }

      if (currentFrame != null && f != currentFrame) {
        // If currentFrame is maximized, minimize it and make new activated frame maximized
        if (currentFrame.isMaximum()) {
          currentFrame.setMaximum(false);
          f.setMaximum(true);
        }
        if (currentFrame.isSelected()) {
          currentFrame.setSelected(false);
        }
      }

      if (!f.isSelected()) {
        f.setSelected(true);
      }
      currentFrame = f;
    } catch (PropertyVetoException e) {
    }
  }

  /**
   * Activate the next child JInternalFrame, as determined by the frames' Z-order. If there is only
   * one child frame, it remains activated. If there are no child frames, nothing happens.
   */
  public void activateNextFrame() {
    JInternalFrame f = getFrame(true);
    if (f != null) {
      activateFrame(f);
      currentFrame = f;
    }
  }

  /**
   * Activate the previous child JInternalFrame, as determined by the frames' Z-order. If there is
   * only one child frame, it remains activated. If there are no child frames, nothing happens.
   */
  public void activatePreviousFrame() {
    JInternalFrame f = getFrame(false);
    if (f != null) {
      activateFrame(f);
      currentFrame = f;
    }
  }

  /** true means get next frame false means get previous frame */
  private JInternalFrame getFrame(boolean next) {
    int count = childFrames.size();
    if (count <= 1) {
      // No other child frames.
      return null;
    }

    int currentIndex = childFrames.indexOf(currentFrame);
    if (currentIndex == -1) {
      // should never happen...
      return null;
    }
    int nextIndex;
    if (next) {
      nextIndex = currentIndex + 1;
      if (nextIndex == count) {
        nextIndex = 0;
      }
    } else {
      nextIndex = currentIndex - 1;
      if (nextIndex == -1) {
        nextIndex = count - 1;
      }
    }
    JInternalFrame f = (JInternalFrame) childFrames.get(nextIndex);
    return f;
  }
}
