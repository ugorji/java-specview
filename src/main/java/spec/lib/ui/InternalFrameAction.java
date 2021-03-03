package spec.lib.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import javax.swing.AbstractAction;
import javax.swing.DesktopManager;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import spec.lib.LibConstants;

/** abstract class containing actions that act on internal frames in a desktop */
public abstract class InternalFrameAction extends AbstractAction implements LibConstants {
  protected JDesktopPane desk; // the desktop to work with

  public InternalFrameAction(String label, Icon icon) {
    super(label, icon);
  }

  /** An action that tiles all internal frames when requested. */
  public static final class TileAction extends InternalFrameAction {

    /** Constructor that takes a label, icon and the desktop to work on */
    public TileAction(String label, Icon icon, JDesktopPane desk) {
      super(label, icon);
      this.desk = desk;
    }

    /** Constructor that uses a default icon */
    public TileAction(String label, JDesktopPane desk) {
      this(label, DEFAULT_ICON, desk);
    }

    public TileAction(JDesktopPane desk) {
      this("Tile Frames", DEFAULT_ICON, desk);
    }

    /** does not tile windows if smaller than 75x75 in dimensions */
    public void actionPerformed(ActionEvent ev) {

      // How many frames do we have?
      JInternalFrame[] allframes = desk.getAllFrames();
      int count = allframes.length;
      if (count == 0) return;

      // Determine the necessary grid size
      int sqrt = (int) Math.sqrt(count);
      int rows = sqrt;
      int cols = sqrt;
      if (rows * cols < count) {
        cols++;
        if (rows * cols < count) {
          rows++;
        }
      }

      // Define some initial values for size & location
      Dimension size = desk.getSize();

      int w = size.width / cols;
      int h = size.height / rows;
      int x = 0;
      int y = 0;

      if (w <= 75 || h <= 75) {
        JOptionPane.showMessageDialog(
            desk,
            "Cannot tile windows due to \n\ttoo many windows open \n\tor main window is too small.\n "
                + "Try closing some windows.",
            "Tile windows disallowed",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      DesktopManager manager = desk.getDesktopManager();

      // Iterate over the frames, deiconifying any iconified frames and then
      // relocating & resizing each
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols && ((i * cols) + j < count); j++) {
          JInternalFrame f = allframes[(i * cols) + j];

          if (!(f.isClosed())) {
            try {
              if (f.isIcon()) f.setIcon(false);
              if (f.isMaximum()) f.setMaximum(false);
            } catch (PropertyVetoException ex) {
            }

            manager.resizeFrame(f, x, y, w, h);
            x += w;
          }
        }
        y += h; // start the next row
        x = 0;
      }
    }
  }

  /** An action that tiles all internal frames when requested. */
  public static final class CascadeAction extends InternalFrameAction {

    public CascadeAction(String label, Icon icon, JDesktopPane desk) {
      super(label, icon);
      this.desk = desk;
    }

    public CascadeAction(String label, JDesktopPane desk) {
      this(label, DEFAULT_ICON, desk);
    }

    public CascadeAction(JDesktopPane desk) {
      this("Cascade Frames", DEFAULT_ICON, desk);
    }

    /** does not cascade windows if smaller than 75x75 in dimensions */
    public void actionPerformed(ActionEvent ev) {

      // How many frames do we have?
      JInternalFrame[] allframes = desk.getAllFrames();
      int count = allframes.length;
      if (count == 0) return;

      // Determine the necessary grid size
      int verticalBridge = 25;
      int horizontalBridge = 25;

      // x, y, w, h are dimensions for the
      int x = 0, y = 0, w = 0, h = 0, count2 = 0, numIteration = 0;
      int minHt = 300, minWidth = 300;

      // Define some initial values for size & location
      Dimension size = desk.getSize();

      w = size.width - horizontalBridge * (count + 2);
      h = size.height - verticalBridge * (count + 2);

      if (w < minWidth) w = minWidth;

      if (h < minHt) h = minHt;

      count2 = (size.height - h) / verticalBridge;

      numIteration = count / count2;
      if ((count % count2) > 0) numIteration++;

      DesktopManager manager = desk.getDesktopManager();

      // Iterate over the frames, deiconifying any iconified frames and then
      // relocating & resizing each
      for (int i = 0; i < count2; i++) {
        for (int j = 0; j < numIteration; j++) {
          int k = i + (j * count2);
          if (k >= count) continue;

          JInternalFrame f = allframes[k];
          if (!(f.isClosed())) {
            try {
              if (f.isIcon()) f.setIcon(false);
              if (f.isMaximum()) f.setMaximum(false);
            } catch (PropertyVetoException ex) {
            }

            manager.resizeFrame(f, x + (j * horizontalBridge), y, w, h);
          }
        }
        x += horizontalBridge;
        y += verticalBridge;
      }
    }
  }

  /** An action that minimizes all internal frames on a desktop. */
  public static final class MinimizeAction extends InternalFrameAction {

    public MinimizeAction(String label, Icon icon, JDesktopPane desk) {
      super(label, icon);
      this.desk = desk;
    }

    public MinimizeAction(String label, JDesktopPane desk) {
      this(label, DEFAULT_ICON, desk);
    }

    public MinimizeAction(JDesktopPane desk) {
      this("Minimize Frames", DEFAULT_ICON, desk);
    }

    public void actionPerformed(ActionEvent ev) {
      // How many frames do we have?
      JInternalFrame[] allframes = desk.getAllFrames();
      int count = allframes.length;
      if (count == 0) return;

      // Iterate over the frames, deiconifying any iconified frames and then
      // relocating & resizing each
      JInternalFrame f;
      for (int i = 0; i < count; i++) {
        try {
          f = allframes[i];
          if (!(f.isClosed())) {
            if (!(f.isIcon())) {
              f.setIcon(true);
            }
          }
        } catch (PropertyVetoException ex) {
        }
      }
    }
  }

  /** An action that restores all internal frames on a desktop. */
  public static final class RestoreAction extends InternalFrameAction {

    public RestoreAction(String label, Icon icon, JDesktopPane desk) {
      super(label, icon);
      this.desk = desk;
    }

    public RestoreAction(String label, JDesktopPane desk) {
      this(label, DEFAULT_ICON, desk);
    }

    public RestoreAction(JDesktopPane desk) {
      this("Restore Frames", DEFAULT_ICON, desk);
    }

    public void actionPerformed(ActionEvent ev) {
      // How many frames do we have?
      JInternalFrame[] allframes = desk.getAllFrames();
      int count = allframes.length;
      if (count == 0) return;

      // Iterate over the frames, deiconifying any iconified frames and then
      // relocating & resizing each
      JInternalFrame f;
      for (int i = 0; i < count; i++) {
        try {
          f = allframes[i];
          if (!(f.isClosed())) {
            if (f.isMaximum()) f.setMaximum(false);
            if (f.isIcon()) f.setIcon(false);
          }
        } catch (PropertyVetoException ex) {
        }
      }
    }
  }
}
