package spec.lib.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;

/*
 **************************************************************************
 **    Class  Axis
 **************************************************************************
 **    This class is designed to be used in conjunction with
 **    the Graph class and Dataset class for plotting 2D graphs.
 *************************************************************************/

/**
 * This class controls the look and feel of horizontal axes. It is designed to be used in
 * conjunction with the Graph class and Dataset class for plotting 2D graphs.
 *
 * <p>To work with the other classes a system of registration is used. The axes have to be attached
 * to the controlling Graph class and the Dataset's have to be attached to both the Graph class and
 * the Axis class.
 */
public final class AxisX extends Axis {
  public static final int orientation = Axis.HORIZONTAL;

  /** ******************** * Constructors ********************* */

  /**
   * Instantiate the class. The defalt type is a Horizontal axis positioned at the bottom of the
   * graph.
   */
  public AxisX() {
    this(false, Axis.BOTTOM);
  }

  /**
   * Instantiate the class. Setting the position.
   *
   * @param p Set the axis position. Must be one of Axis.BOTTOM, Axis.TOP, Axis.LEFT, Axis.RIGHT,
   *     Axis.HORIZONTAL or Axis.VERTICAL. If one of the latter two are used then Axis.BOTTOM or
   *     Axis.LEFT is assumed.
   */
  public AxisX(boolean axisRangeEqualsDataRange, int p) {
    super(axisRangeEqualsDataRange);
    setPosition(p);
    axisTitleString = "A Horizontal Axis";
    super.orientation = Axis.HORIZONTAL;
  }

  /**
   * Set the axis position.
   *
   * @param p Must be one of Axis.BOTTOM, Axis.TOP or Axis.HORIZONTAL
   */
  public void setPosition(int p) {

    switch (position) {
      case Axis.TOP:
      case Axis.BOTTOM:
        position = p;
        break;
      default:
        position = Axis.BOTTOM;
        break;
    }
  }

  public boolean isVertical() {
    return false;
  }

  /**
   * Attach a Dataset for the Horizontal Axis to manage.
   *
   * @param d dataset to attach
   * @see graph.Dataset
   */
  public void attachDataset(Dataset d) {
    if (dataset.contains(d)) return;

    dataset.addElement(d);
    d.xAxis = this;

    // set the range of the data
    this.setDataRange();
    // adjust the axis Limits
    this.setAxisMinLimit();
    this.setAxisMaxLimit();
    // set the range of the axis
    this.setAxisRange();
  }

  /**
   * Detach an attached Dataset
   *
   * @param d dataset to detach
   * @see graph.Dataset
   */
  public void detachDataset(Dataset d) {
    int i = 0;

    // if dataset is null, or this axis is not the axis of this dataset
    if (!(dataset.contains(d)) || (d == null) || (d.xAxis != this) || (dataset.isEmpty())) return;

    d.xAxis = null;
    dataset.removeElement(d);

    // set the range of the data
    this.setDataRange();
    // adjust the axis Limits
    this.setAxisMinLimit();
    this.setAxisMaxLimit();
    // set the range of the axis
    this.setAxisRange();
  }

  /** Detach All attached datasets. */
  public void detachAll() {
    int i;
    Dataset d;

    if (dataset.isEmpty()) return;

    for (i = 0; i < dataset.size(); i++) {
      d = (Dataset) (dataset.elementAt(i));
      d.xAxis = null;
    }

    dataset.removeAllElements();

    // set the range of the data
    this.setDataRange();
    // adjust the axis Limits
    this.setAxisMinLimit();
    this.setAxisMaxLimit();
    // set the range of the axis
    this.setAxisRange();
  }

  /**
   * Return the minimum value of All datasets attached to the axis.
   *
   * @return Data minimum
   */
  public double getDataMin() {
    double m;
    Enumeration e;
    Dataset d;

    if (dataset.isEmpty()) return 0.0;

    d = (Dataset) (dataset.firstElement());
    if (d == null) return 0.0;

    m = d.getXMin();
    for (e = dataset.elements(); e.hasMoreElements(); ) {
      d = (Dataset) e.nextElement();
      m = Math.min(d.getXMin(), m);
    }
    return m;
  }

  /**
   * Return the maximum value of All datasets attached to the axis.
   *
   * @return Data maximum
   */
  public double getDataMax() {
    double m;
    Enumeration e;
    Dataset d;

    if (dataset.isEmpty()) return 0.0;

    d = (Dataset) (dataset.firstElement());

    if (d == null) return 0.0;

    m = d.getXMax();
    for (e = dataset.elements(); e.hasMoreElements(); ) {
      d = (Dataset) e.nextElement();
      m = Math.max(d.getXMax(), m);
    }

    return m;
  }

  /**
   * Return the pixel equivalent of the parsed, passed data value relative to "rect" Using the
   * position of the axis and the maximum and minimum values, convert the data value position into a
   * pixel position The value taken must be a point on the actual plotting graph ... not on the
   * rectangle occupied by the axis
   *
   * @param v data value to convert
   * @return equivalent pixel value
   * @see graph.Axis#getDouble( )
   */
  protected int getPixel(double v) {
    // increment = (axisMax - axisMin) / (rect.width-1)
    // value = (v - axisMin) * increment
    return (int) ((v - axisMin) / (axisMax - axisMin) * (rect.width - 1));
  }

  /**
   * Return the data value equivalent of the parsed, passed point relative to "rect" Using the
   * position of the axis and the maximum and minimum values, convert the pixel position into a data
   * value The value taken must be a point on the actual plotting graph ... not on the rectangle
   * occupied by the axis
   *
   * @param i pixel value
   * @return equivalent data value
   * @see graph.Axis#getPixel( )
   */
  protected double getDouble(int i) throws Exception {
    // increment = (axisMax - axisMin) / (rect.width-1)
    // value = axisMin + (i / increment)
    double d = (axisMin + ((double) i / (rect.width - 1) * (axisMax - axisMin)));
    if (d > axisMax || d < axisMin)
      throw new Exception("computed mouse position is outside axis range");

    return d;
  }

  /**
   * Draw the axis ... just set its attributes in the passes rectangle, which will be set to the
   * private "rect" of this axis
   */
  public void draw(Rectangle2D aRect) {
    if (!redraw) return;

    isAttributeSet = false;

    this.rect.setRect(aRect);

    setAxisAttributes();
    // paint (g_orig);

  }

  public void paint(Graphics g_orig) {
    // if attributes have not been set, return
    if (!(isAttributeSet)) return;

    Graphics2D g = (Graphics2D) g_orig;
    g.setStroke(Axis.TIC_STROKE);

    // store the old color to be restored after drawing this axis
    Color oldColor = g.getColor();
    // set the graphics color to the color for this axis ...
    g.setColor(axisColor);

    // g.setClip (rect);

    /* DRAW AXIS TITLE */
    Point2D.Float p = new Point2D.Float();

    // draw the axis Title if requested (if showAxisTitle is true)
    if (showAxisTitle && (attributedAxisTitle != null)) {
      axisTitle = new TextLayout(attributedAxisTitle.getIterator(), g.getFontRenderContext());
      // try to center the axisTitle
      if (axisTitle.getAdvance() > rect.width) p.x = rect.x;
      else p.x = rect.x + (rect.width - axisTitle.getAdvance()) / 2;

      p.y = rect.y + 20 + (axisTitle.getAscent() + axisTitle.getLeading());
      axisTitle.draw(g, p.x, p.y);

      /*
       **********************************************
       System.out.println ("rectangle for X axis: " + rect.toString() + " and ...");
       System.out.println ("axisTitle.getDescent() + axisTitle.getLeading() = " +
      axisTitle.getDescent() + "   " + axisTitle.getLeading() );
       **********************************************
       */

    }

    /*
     ***************************************
     * NOTE: floats are used instead of ints for drawing lines and getting more
     * precise positions. This prevents the offsets that were experienced before,
     * when the axis lines and grids did not match with the graphDataset bounds
     * due to precision lost in int manipulations
     */
    // draw line for axis
    g.draw(new Line2D.Float(rect.x, rect.y, rect.x + rect.width, rect.y));

    float majorIncr = 0.0f;
    float newXMajor = 0.0f;
    float minorIncr = 0.0f;
    float newXMinor = 0.0f;

    // draw major tics, minor tics and labels together in a loop
    for (int i = 0; i < numMajorTics; i++) {
      // draw major tics
      majorIncr = rect.width / (numMajorTics - 1);
      newXMajor = rect.x + (float) i * majorIncr;
      g.draw(new Line2D.Float(newXMajor, rect.y, newXMajor, rect.y + majorTicSize));

      // draw minor tics if requested (if showMinorTics is true)
      if (showMinorTics) {
        minorIncr = majorIncr / (numMinorTics + 1);
        for (int j = 1; j <= numMinorTics; j++) {
          newXMinor = newXMajor + (j * minorIncr);
          g.draw(new Line2D.Float(newXMinor, rect.y, newXMinor, rect.y + minorTicSize));
        }
      }

      // draw the axis labels if requested (if showAxisLabels is true)
      if (showAxisLabels) {
        g.setFont(labelFont);
        g.drawString(labelString[i], newXMajor - 10, rect.y + 20);
      }
    }

    // set the color back to the old graphics context color
    g.setColor(oldColor);
  }

  /**
   * Set Label Attributes including: labelString[], labelValue[], numMajorTics, numMinorTics,
   * showMinorTics showAxisLabels, showAxisTitle, labelFont Uses (70, 60, 45, 30) or (70, 55, 40,
   * 20) algorithm for vertical or horizontal axes i.e. ...
   */
  public void setAxisAttributes() {
    setAxisAttributes(rect.height, rect.width);

    // DO THE SWITCHES
    labelFont = new Font("LucidaTypeWriter", Font.PLAIN, 12);
  }
}
