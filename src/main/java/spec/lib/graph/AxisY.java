package spec.lib.graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;

/*
 **************************************************************************
 **    Class  AxisY
 **************************************************************************
 **    This class is designed to be used in conjunction with
 **    the Graph class and Dataset class for plotting 2D graphs.
 *************************************************************************/

/**
 * This class controls the look and feel of vertical axes. It is designed to be used in conjunction
 * with the Graph class and Dataset class for plotting 2D graphs.
 *
 * <p>To work with the other classes a system of registration is used. The axes have to be attached
 * to the controlling Graph class and the Dataset's have to be attached to both the Graph class and
 * the Axis class.
 */
public final class AxisY extends Axis {
  public static final int orientation = Axis.VERTICAL;
  // private static final AffineTransform rotate90 = AffineTransform.getRotateInstance
  // (Math.PI/-2.0);

  /** ******************** * Constructors ********************* */

  /**
   * Instantiate the class. The defalt type is a Horizontal axis positioned at the bottom of the
   * graph.
   */
  public AxisY() {
    this(false, Axis.LEFT);
  }

  /**
   * Instantiate the class. Setting the position.
   *
   * @param p Set the axis position. Must be one of Axis.BOTTOM, Axis.TOP, Axis.LEFT, Axis.RIGHT,
   *     Axis.HORIZONTAL or Axis.VERTICAL. If one of the latter two are used then Axis.BOTTOM or
   *     Axis.LEFT is assumed.
   */
  public AxisY(boolean axisRangeEqualsDataRange, int p) {
    super(axisRangeEqualsDataRange);
    setPosition(p);
    axisTitleString = "A Vertical Axis";
    super.orientation = Axis.VERTICAL;
  }

  /**
   * Set the vertical axis position.
   *
   * @param p Must be one of Axis.LEFT, Axis.RIGHT or Axis.VERTICAL.
   */
  public void setPosition(int p) {

    switch (position) {
      case Axis.LEFT:
      case Axis.RIGHT:
        position = p;
        break;
      default:
        position = Axis.LEFT;
        break;
    }
  }

  public boolean isVertical() {
    return true;
  }

  /**
   * Attach a Dataset for the Vertical Axis to manage.
   *
   * @param d dataset to attach
   * @see graph.Dataset
   */
  public void attachDataset(Dataset d) {
    if (dataset.contains(d)) return;

    dataset.add(d);
    d.yAxis = this;

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
    if (!(dataset.contains(d)) || (d == null) || (d.yAxis != this) || (dataset.isEmpty())) return;

    d.yAxis = null;
    dataset.remove(d);

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
      d.yAxis = null;
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

    m = d.getYMin();
    for (e = dataset.elements(); e.hasMoreElements(); ) {
      d = (Dataset) e.nextElement();
      m = Math.min(d.getYMin(), m);
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

    m = d.getYMax();
    for (e = dataset.elements(); e.hasMoreElements(); ) {
      d = (Dataset) e.nextElement();
      m = Math.max(d.getYMax(), m);
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
    // increment = (rect.height - 1 ) / (axisMax - axisMin)
    // value = (axisMax - v) * increment
    return (int) ((axisMax - v) / (axisMax - axisMin) * (rect.height - 1));
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
  /*
   *(i+1) since first pixel position is at 0
   *(so first pixel position of 0 corresponds to offset height of 1 pixel)
   */
  protected double getDouble(int i) throws Exception {
    // increment = (rect.height - 1 ) / (axisMax - axisMin)
    // value = axisMax - (i / increment)
    double d = (axisMax - ((double) i / (rect.height - 1) * (axisMax - axisMin)));
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

    // isAttributeSet will be set back to true by setAxisAttributes
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
      // try to center the axisTitle ... need to rotate it first
      if (axisTitle.getAdvance() > rect.height) {
        p.y = rect.y + rect.height;
      } else {
        p.y = rect.y + rect.height - ((rect.height - axisTitle.getAdvance()) / 2.0f);
      }
      p.x = rect.x + rect.width - 40.0f;

      AffineTransform oldTransform = new AffineTransform(g.getTransform());

      // get the affine transform to rotate and render this TextLayout at this point
      AffineTransform at = new AffineTransform();
      // translate the point
      at.setToTranslation((double) p.x, (double) p.y);
      g.transform(at);
      // rotate the transformation by 90 degrees
      at.setToRotation(Math.PI / -2.0);
      g.transform(at);
      // render the Text Layout
      axisTitle.draw(g, 0, 0);

      g.setTransform(oldTransform);
    }

    // NOTE: floats are used instead of ints for drawing lines and getting more
    // precise positions. This prevents the offsets that were experienced before,
    // when the axis lines and grids did not match with the graphDataset bounds
    // due to precision lost in int manipulations

    // draw line for axis
    g.draw(
        new Line2D.Float(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height));

    float majorIncr = 0.0f;
    float newYMajor = 0.0f;
    float minorIncr = 0.0f;
    float newYMinor = 0.0f;

    // draw major tics, minor tics and labels together in a loop
    for (int i = 0; i < numMajorTics; i++) {
      // draw major tics ... (rect.height-1) accomodates for the (0,0) starting pixel
      majorIncr = (rect.height - 1) / (numMajorTics - 1);
      newYMajor = rect.y + (rect.height - 1) - ((float) i * majorIncr);
      g.draw(
          new Line2D.Float(
              rect.x + rect.width, newYMajor, rect.x + rect.width - majorTicSize, newYMajor));

      // draw minor tics if requested (if showMinorTics is true)
      if (showMinorTics) {
        minorIncr = majorIncr / (numMinorTics + 1);
        for (int j = 1; j <= numMinorTics; j++) {
          newYMinor = newYMajor - (j * minorIncr);
          g.draw(
              new Line2D.Float(
                  rect.x + rect.width, newYMinor, rect.x + rect.width - minorTicSize, newYMinor));
        }
      }

      // draw the axis labels if requested (if showAxisLabels is true)
      if (showAxisLabels) {
        g.setFont(labelFont);
        g.drawString(labelString[i], rect.x + rect.width - 32, newYMajor + 10);
      }
    }

    // set the color back to the old graphics context color
    g.setColor(oldColor);
  }

  /**
   * Set Label Attributes including: showAxisLabels, labelString[], labelValues[], showMinorTics,
   * numMinorTics, showAxisTitle, axisTitle. Uses (70, 60, 45, 30) or (70, 55, 40, 20) algorithm for
   * vertical or horizontal axes i.e. ...
   */
  public void setAxisAttributes() {
    setAxisAttributes(rect.width, rect.height);
  }
}
