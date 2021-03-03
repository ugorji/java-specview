package spec.lib.graph;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;

public abstract class Dataset implements DatasetInterface {

  /** property for the color map */
  public static final String SCALE_PROPERTY = "DATASET SCALE PROPERTY";

  /** DEFINE THE SCALE TYPES ... linear, log to base e (natural log), or log to base 10 */
  public static final int LINEAR = 100;

  public static final int LOG_E = 101;
  public static final int LOG_10 = 102;

  /** DEFINE the type of dataset, XtoY or XYtoZ */
  public static final int XTOY = 103;

  public static final int XYTOZ = 104;

  /** DEFINE the representation of data values in the dataset, as integers or doubles */
  public static final int INTEGER_DATA = 105;

  public static final int DOUBLE_DATA = 106;

  /** defines the scale type */
  protected int scale = Dataset.LINEAR;

  /* *** COMPULSORY variables in a dataset *** */
  protected double dxmax; // data X maximum.
  protected double dxmin; // data X minimum.
  protected double dymax; // data Y maximum.
  protected double dymin; // data Y minimum.

  /**
   * The Axis object the X data is attached to. From the Axis object, the scaling for the data can
   * be derived.
   */
  protected AxisX xAxis = new AxisX();
  /** The Axis object the Y data is attached to. */
  protected AxisY yAxis = new AxisY();
  /** name of the data set that will be used to identify itself in key, etc. */
  protected String name = "A Data Set";
  /** font to be used for dataset stuff */
  public Font font = new Font("Helvetica", Font.PLAIN, 18);

  /**
   * handles the property changes ... since Dataset has to fire property changes ... like when the
   * scale changes, or the colormapping for XYtoZ inverts
   */
  private SwingPropertyChangeSupport changeSupport;

  /*
   *******************
   ** Public Methods
   ******************/

  /**
   * get the primitive way in which data values are represented in the dataset, whether as integers
   * (Dataset.INTEGER_DATA) or doubles (Dataset.DOUBLE_DATA)
   */
  public abstract int getDataPrimitive();

  /** get the type of dataset ... whether XtoY (Dataset.XTOY) or XYtoZ (Dataset.XYTOZ) */
  public abstract int getDatasetType();

  /**
   * sets the data that will actually be plotted as y (in XtoY Dataset) or as z (in XYtoZ Dataset)
   */
  public abstract void setData();

  /**
   * Indicate whether or not we should show the error indicators ... as error bars or otherwise
   * ...Does nothing now ... since we have no clean way of showing the error
   */
  public void setShowError(boolean b) {}

  /** returns if error indication is being shown or not ... defaults to returning false */
  public boolean isShowError() {
    return false;
  }

  /** set the name of the dataset */
  public void setName(String name) {
    this.name = name;
  }

  // set the axes ... and attach this dataset to the axis to handle
  public void setAxisX(AxisX aAxisX) {
    xAxis = aAxisX;
    xAxis.attachDataset(this);
  }

  public void setAxisY(AxisY aAxisY) {
    yAxis = aAxisY;
    yAxis.attachDataset(this);
  }

  public AxisX getAxisX() {
    return xAxis;
  }

  public AxisY getAxisY() {
    return yAxis;
  }

  /** return the data X maximum. */
  public double getXMax() {
    return dxmax;
  }
  /** return the data X minimum. */
  public double getXMin() {
    return dxmin;
  }
  /** return the data Y maximum. */
  public double getYMax() {
    return dymax;
  }
  /** return the data Y minimum. */
  public double getYMin() {
    return dymin;
  }

  /** set the data X maximum. */
  public void setXMax(double d) {
    dxmax = d;
  }
  /** set the data X minimum. */
  public void setXMin(double d) {
    dxmin = d;
  }
  /** set the data Y maximum. */
  public void setYMax(double d) {
    dymax = d;
  }
  /** set the data Y minimum. */
  public void setYMin(double d) {
    dymin = d;
  }

  /** returns the scale used to render the dataset */
  public int getScale() {
    return scale;
  }

  /** returns the scale used to render the dataset as a String */
  public String getScaleAsString() {
    String scaleString = "";

    switch (scale) {
      case LINEAR:
        scaleString = "Linear Scale";
        break;
      case LOG_10:
        scaleString = "Log->base 10 Scale";
        break;
      case LOG_E:
        scaleString = "Log->base E Scale";
        break;
      default:
        scaleString = "Error in Scale";
        break;
    }

    return scaleString;
  }

  /**
   * sets the scale used to render the dataset, without firing the property change event This way,
   * subclasses can call this, do other manipulations, then fire the event themselves
   */
  protected void setScaleAndDoNotFirePropertyChange(int aScale) {
    // if scale is the same as the new scale, return without doing anything
    if (scale == aScale) return;

    int oldValue = scale;

    switch (aScale) {
      case LOG_10:
      case LOG_E:
        scale = aScale;
        // do not show error indicators for log
        // setShowError (false);
        break;
      case LINEAR:
      default:
        scale = LINEAR;
        break;
    }

    // cause a redraw
  }

  /**
   * sets the scale used to render the dataset, and then fire the propertyChange subclasses should
   * override this by calling setScaleAndDoNotFirePropertyChange
   */
  protected void setScale(int aScale) {
    int oldValue = scale;
    setScaleAndDoNotFirePropertyChange(aScale);

    // fire the property Change
    this.firePropertyChange(Dataset.SCALE_PROPERTY, oldValue, scale);
  }

  /**
   * returns the adjusted log value to base 10 for any values given to it this allows numbers less
   * than 1 have positive values
   */
  protected double _log10(double x) {
    if (x <= 0.0) return 0.0;
    else if (x <= 1.0) return 0.15; // 1/2 log 2 (base 10)
    else return Math.log(x) / 2.30258509299404568401;
  }

  /**
   * returns the adjusted log value for any values given to it this allows numbers less than 1 have
   * positive values
   */
  protected double _log(double x) {
    if (x <= 0.0) return 0.0;
    else if (x <= 1.0) return 0.35; // 1/2 log 2 (base e)
    else return Math.log(x);
  }

  /**
   * Support for reporting bound property changes. replica of what is in JComponent ... overloaded
   * also
   */
  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (changeSupport != null) {
      changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  /*
   * PENDING(hmuller) in JDK1.2 the following firePropertyChange overloads
   * should additional check for a non-empty listener list with
   * changeSupport.hasListeners(propertyName) before calling firePropertyChange.
   */

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Byte(oldValue), new Byte(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, char oldValue, char newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(
          propertyName, new Character(oldValue), new Character(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, short oldValue, short newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Short(oldValue), new Short(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(
          propertyName, new java.lang.Integer(oldValue), new java.lang.Integer(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, long oldValue, long newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Long(oldValue), new Long(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, float oldValue, float newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Float(oldValue), new Float(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, double oldValue, double newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(
          propertyName, new java.lang.Double(oldValue), new java.lang.Double(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Boolean(oldValue), new Boolean(newValue));
    }
  }

  /**
   * Add a PropertyChangeListener to the listener list. The listener is registered for all
   * properties.
   *
   * <p>A PropertyChangeEvent will get fired in response to setting a bound property, e.g. setFont,
   * setBackground, or setForeground. Note that if the current component is inheriting its
   * foreground, background, or font from its container, then no event will be fired in response to
   * a change in the inherited property.
   *
   * <p>This method will migrate to java.awt.Component in the next major JDK release
   *
   * @param listener The PropertyChangeListener to be added
   */
  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport == null) {
      changeSupport = new SwingPropertyChangeSupport(this);
    }
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Remove a PropertyChangeListener from the listener list. This removes a PropertyChangeListener
   * that was registered for all properties.
   *
   * <p>This method will migrate to java.awt.Component in the next major JDK release
   *
   * @param listener The PropertyChangeListener to be removed
   */
  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport != null) {
      changeSupport.removePropertyChangeListener(listener);
    }
  }

  /**
   * Takes a graphic context and the dimensions and draws onto it ... using the dimension dim to
   * know where to draw
   */
  // public abstract void draw (Graphics2D g, Dimension dim);

  //   /**
  //    * get the data point at the parsed index. The first (x,y) pair
  //    * is at index 0.
  //    * @param index Data point index
  //    * @return array containing the (x,y) pair.
  //    */
  //   public abstract double[] getPoint(int index);

  //   /**
  //    * Return the data point that is closest to the parsed (x,y) position
  //    * @param x
  //    * @param y (x,y) position in data space.
  //    * @return array containing the closest data point.
  //    */
  //   public abstract double[] getClosestPoint(double x, double y);

  /** Calculate the range of the data. This modifies dxmin,dxmax,dymin,dymax */

  // public abstract void range ();

  /*
  ***************************************************************
  // interface identifying a dataset of integer values
  public static interface Integer extends DatasetInterface
  {
  }

  // interface identifying a dataset of double values
  public static interface Double  extends DatasetInterface
  {
  }
  ***************************************************************
  */

  public static class DatasetException extends java.lang.Exception {
    public DatasetException(String msg) {
      super(msg);
    }
  }
}
