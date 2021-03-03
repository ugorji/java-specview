package spec.lib;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public final class Misc {
  private static Calendar cal = Calendar.getInstance();

  /**
   * Round up the passed value to a NICE value ... which is (1, 2, 5, or 10) multiplied by a
   * multiple of 10
   */
  public static final double RoundUp(double val) {
    int exponent;
    int i;

    exponent = (int) (Math.floor(MathPlus.log10(val)));

    val = val * Math.pow(10.0, -1.0 * exponent);

    if (val > 5.0) val = 10.0;
    else if (val > 2.0) val = 5.0;
    else if (val > 1.0) val = 2.0;
    else val = 1.0;

    val = val * Math.pow(10.0, (double) exponent);

    return val;
  }

  /**
   * returns the height of the rectangular array i.e the constant length of each subarray, or throws
   * an exception if array is not rectangular
   */
  public static final int getRectangularArrayHeight(int[][] array)
      throws Misc.NotRectangularArrayException {
    boolean isRectangular;
    int oldSubLength, subLength;
    oldSubLength = subLength = array[0].length;

    for (int i = 0; i < array.length; i++) {
      subLength = array[i].length;
      isRectangular = (subLength == oldSubLength) ? true : false;
      if (!(isRectangular))
        throw new NotRectangularArrayException("Array sub lengths are not equal");
      oldSubLength = subLength;
    }

    return subLength;
  }

  /**
   * returns the height of the rectangular array i.e the constant length of each subarray, or
   * returns -1 if array is not rectangular
   */
  public static final int getRectangularArrayHeight(double[][] array)
      throws NotRectangularArrayException {
    boolean isRectangular = true;
    int oldSubLength, subLength;
    oldSubLength = subLength = array[0].length;

    for (int i = 0; i < array.length; i++) {
      subLength = array[i].length;
      isRectangular = (subLength == oldSubLength) ? true : false;
      if (!(isRectangular))
        throw new NotRectangularArrayException(
            "The array argument is not rectangular \n"
                + "\tThelengths of the subarrays are not equal");
      oldSubLength = subLength;
    }

    return subLength;
  }

  /** given a map and a value, it tries to return an array of keys that maps to that value */
  public static final Object[] getKeysForValue(Map map, Object value) {
    Object obj;
    ArrayList arrayList = new ArrayList();

    for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
      obj = i.next();
      if (map.get(obj).equals(value)) {
        arrayList.add(obj);
      }
    }

    return arrayList.toArray();
  }

  /**
   * This routine sets the Cursor crsr on Component cmp and all of its children. If cmp is the top
   * level Component, the effect is setting the busy cursor on the entire application.
   */
  public static final void setRecursiveCursor(Cursor crsr, Component cmp) {

    cmp.setCursor(crsr);
    if (cmp instanceof Container) {
      Component[] kids = ((Container) cmp).getComponents();
      for (int i = 0; i < kids.length; i++) {
        setRecursiveCursor(crsr, kids[i]);
      }
    }
  }

  /**
   * This routine sets the Cursor crsr on Component cmp and all of its children. If cmp is the top
   * level Component, the effect is setting the busy cursor on the entire application. Also, all the
   * components are disabled
   */
  public static final void setRecursiveCursorAndDisable(Cursor crsr, Component cmp) {

    cmp.setCursor(crsr);
    cmp.setEnabled(false);
    if (cmp instanceof Container) {
      Component[] kids = ((Container) cmp).getComponents();
      for (int i = 0; i < kids.length; i++) {
        setRecursiveCursorAndDisable(crsr, kids[i]);
      }
    }
  }

  /**
   * This routine sets the Cursor crsr on Component cmp and all of its children. If cmp is the top
   * level Component, the effect is setting the busy cursor on the entire application. Also, all the
   * components are enabled
   */
  public static final void setRecursiveCursorAndEnable(Cursor crsr, Component cmp) {

    cmp.setCursor(crsr);
    cmp.setEnabled(true);
    if (cmp instanceof Container) {
      Component[] kids = ((Container) cmp).getComponents();
      for (int i = 0; i < kids.length; i++) {
        setRecursiveCursorAndEnable(crsr, kids[i]);
      }
    }
  }

  /**
   * This copies all the key/value pairs ... from a ResourceBundle object into a Properties object
   */
  public static final void copyResourcesIntoProperties(
      ResourceBundle resources, Properties properties) {
    if (resources == null || properties == null) return;

    Enumeration keys = resources.getKeys();

    String aKey;
    while (keys.hasMoreElements()) {
      aKey = (String) keys.nextElement();
      properties.setProperty(aKey, resources.getString(aKey));
    }
  }

  /** center a window on the screen */
  public static final void centerOnScreen(Window window) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (screen.width - window.getWidth()) / 2;
    int y = (screen.height - window.getHeight()) / 2;

    window.setLocation(x, y);
  }

  /** give a window certain proportions on the screen and then center it */
  public static final void proportionOnScreen(
      Window window, double widthProportion, double heightProportion) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    int windowWidth = (int) (screenSize.width * widthProportion);
    int windowHt = (int) (screenSize.height * heightProportion);

    int insetHor = (screenSize.width - windowWidth) / 2;
    int insetVer = (screenSize.height - windowHt) / 2;

    window.setBounds(insetHor, insetVer, windowWidth, windowHt);
  }

  /** get the current time as "hr:min:sec am/pm" */
  public static String getCurrentTime() {
    // long time = System.currentTimeMillis ();
    // cal.setTimeInMillis (time);
    // timeString.append ();
    cal = Calendar.getInstance();

    int hr = cal.get(Calendar.HOUR);
    int min = cal.get(Calendar.MINUTE);
    int amOrPm = cal.get(Calendar.AM_PM);
    int sec = cal.get(Calendar.SECOND);

    StringBuffer timeString = new StringBuffer(32);
    timeString.append(hr);
    timeString.append(":");
    if (min < 10) timeString.append("0");
    timeString.append(min);
    timeString.append(":");
    if (sec < 10) timeString.append("0");
    timeString.append(sec);

    timeString.append(" ");
    if (amOrPm == Calendar.AM) timeString.append("am");
    else if (amOrPm == Calendar.PM) timeString.append("pm");

    return timeString.toString();
  }

  /* CONVENIENCE CLASSES BELOW */

  /** convenience exception class to signal a non-rectangular array */
  public static final class NotRectangularArrayException extends Exception {
    public NotRectangularArrayException(String msg) {
      super(msg);
    }
  }
}
