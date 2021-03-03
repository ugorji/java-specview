package spec.lib;

import java.awt.Cursor;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/** This class just defines a bunch of constants that we can use in our program */
public interface LibConstants {
  /** A default icon that can be used for actions with no defined icons */
  public static final Icon DEFAULT_ICON =
      new ImageIcon(new BufferedImage(15, 15, BufferedImage.TYPE_INT_RGB));

  public static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  public static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
}
