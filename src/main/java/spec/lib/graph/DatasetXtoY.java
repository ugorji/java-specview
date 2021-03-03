package spec.lib.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import spec.lib.MathPlus;

/*
 **************************************************************************
 **    Class  XtoY to handle X to Y data sets
 **************************************************************************
 **    Copyright Ugorji Nwoke
 ************************************************************************
 *
 * Need to put a method to set the renderer
 */
public abstract class DatasetXtoY extends Dataset {
  // property for the color attribure
  public static final String LINE_ATTRIBUTE_PROPERTY = "Line Attribute property";

  // keep track of the num of XtoY Datasets made
  protected static int numDatasets = 0;

  /** error in Y array has alternating element for up and down distance */
  public static final int ERROR_ALTERNATING = 111;
  /** error in Y array has each element = up and down distance */
  public static final int ERROR_DUPLICATE = 112;
  /** error in Y array has half of each element = up and down distance */
  public static final int ERROR_HALF = 113;

  /** the error in Y type */
  protected int errorInYType = this.ERROR_DUPLICATE;

  /*
   *********************
   ** Protected Variables
   **********************/

  /** The number of data points */
  protected int numDataPoints;

  /** Determines whether or not to show error bars in Vertical (Y axis) */
  protected boolean showErrorInY = false;

  /*
   ***********************
   ** Public Variables
   **********************/

  // The following variables are necessary for every dataset
  /** The line attribute, encapsulating the line color and line stroke style */
  protected DatasetXtoYLineAttribute lineAttribute = new DatasetXtoYLineAttribute();

  /**
   * optional arbitrary point to accomodate optionally drawing from an arbitrary first point to
   * first dataset point A line is drawn from this point to first dataset point if this point is
   * non-null
   */
  protected Point2D.Float optionalStartPoint;
  /**
   * optional arbitrary point to accomodate optionally drawing from an arbitrary end point to last
   * dataset point A line is drawn from last dataset point to this point if this point is non-null
   */
  // private Point2D.Float optionalEndPoint;

  /**
   * The array containing the data graphed in the y plane ... always a "double"array so that
   * manipulations, changing it to log or linear scale will not lose accuracy
   */
  public double[] ydata;

  /**
   * Array of the uncertainty in a dataset value Each dataset value has associated with it 2 error
   * values, upDistance and downDistance, representing the error above and below the measured value
   */
  protected double[] errorInY;
  /** Array containing the actual uncertainty in a dataset value */
  protected double[] errorInYValue;

  /**
   * the object that performs drawing operations for this dataset ... Also performs setting the data
   * and error that will be plotted, also sets if error will be shown or not also handles getting
   * the actual value given the x value
   */
  DatasetXtoYRenderer renderer;

  // instance initializer ... initializing these variables
  {
    /** The dataset name */
    name = "An XY Data Set";
    /** The dataset font */
    font = new Font("Helvetica", Font.BOLD, 18);
  }

  /*
   *********************
   ** Constructors
   ********************/

  /** get the type of dataset ... (Dataset.XTOY) */
  public int getDatasetType() {
    return Dataset.XTOY;
  }

  /**
   * sets the scale used to render the dataset, without firing the property change event This way,
   * subclasses can call this, do other manipulations, then fire the event themselves
   */
  protected void setScaleAndDoNotFirePropertyChange(int aScale) {
    super.setScaleAndDoNotFirePropertyChange(aScale);
    AxisX x = this.xAxis;
    AxisY y = this.yAxis;

    // cause a redraw
    // first detach yourself from your axes
    x.detachDataset(this);
    y.detachDataset(this);
    // adjust your y data
    this.setData();
    // Adjust the data range ... this allows the axis to be correctly adjusted
    range();
    // re-attach yourself to your axes
    this.setAxisX(x);
    this.setAxisY(y);
  }

  /** sets the scale used to render the dataset ... then fires a property change event */
  protected void setScale(int aScale) {
    int oldValue = scale;

    setScaleAndDoNotFirePropertyChange(aScale);

    // fire the property Change
    this.firePropertyChange(Dataset.SCALE_PROPERTY, oldValue, scale);
  }

  public void setRenderer(DatasetXtoYRenderer renderer) {
    if (renderer != null) this.renderer = renderer;
  }

  /** set the render Method */
  public void setRenderMethod(int renderMethod, int errorInYType) {
    setErrorInYType(errorInYType);
    resetRenderer(renderMethod);
  }

  public void setErrorInYType(int errorInYType) {
    switch (errorInYType) {
      case ERROR_ALTERNATING:
        this.errorInYType = errorInYType;
        break;
      case ERROR_DUPLICATE:
        this.errorInYType = errorInYType;
        break;
      case ERROR_HALF:
        this.errorInYType = errorInYType;
        break;
      default:
        this.errorInYType = ERROR_DUPLICATE;
        break;
    }
  }

  /**
   * method to reset the object that handles drawing and painting of the dataset onto a graphics
   * context
   */
  public abstract void resetRenderer(int renderMethod);

  /**
   * sets and returns the number of data points in the X to Y Dataset
   *
   * @return number of (x,y) points.
   */
  public abstract int getNumDataPoint();

  public void setLineAttributes(Color aColor, BasicStroke aStroke) {
    DatasetXtoYLineAttribute oldValue = lineAttribute;
    lineAttribute = new DatasetXtoYLineAttribute(aColor, aStroke);

    // fire the property Change
    this.firePropertyChange(DatasetXtoY.LINE_ATTRIBUTE_PROPERTY, oldValue, lineAttribute);
  }

  public DatasetXtoYLineAttribute getLineAttributes() {
    return lineAttribute;
  }

  /** get the line color for plotting this dataset with */
  public Color getLineColor() {
    return this.lineAttribute.lineColor;
  }

  /** get the line style (a stroke object) for plotting the dataset with */
  public BasicStroke getLineStyle() {
    return this.lineAttribute.lineStyle;
  }

  /** Indicates whether or not we should show the error indicators ... as error bars or otherwise */
  public abstract void setShowError(boolean b);

  /** returns if error indication is being shown or not */
  public boolean isShowError() {
    return showErrorInY;
  }

  /** sets the data that will actually be plotted as y */
  public abstract void setData();

  /**
   * Calculate the range of the data. This modifies dxmin,dxmax,dymin,dymax Take the optional start
   * and/or end points into consideration also ... so the range includes the optional end and start
   * points This is because the values set here are very important ... they are used in the axes and
   * so on.
   */
  public abstract void range();

  /** sets the errorInY to this array and errorInYType to this */
  public void setErrorInY(double[] d, int errorInYType) {
    this.setErrorInYType(errorInYType);
    setErrorInY(d);
  }

  /** sets the errorInY to this array */
  public void setErrorInY(double[] d) {
    switch (errorInYType) {
      case ERROR_ALTERNATING:
        errorInYValue = new double[numDataPoints * 2];
        System.arraycopy(d, 0, errorInYValue, 0, Math.min(d.length, numDataPoints * 2));
        break;
      case ERROR_DUPLICATE:
      case ERROR_HALF:
        errorInYValue = new double[numDataPoints];
        System.arraycopy(d, 0, errorInYValue, 0, Math.min(d.length, numDataPoints));
        break;
      default:
        break;
    }

    // set errorInY to a default array with the right number of elements
    errorInY = new double[errorInYValue.length];
    // cause a call to setData so that errorInY will be initialized
    setData();
  }

  /** takes a graphics context and draws its plot onto it */
  public abstract void draw(Graphics2D g, Dimension dim);

  /**
   * **** ALWAYS USE DOUBLE FOR CONSISTENCY *** given an x value (on horizontal axis), get the
   * corresponding y value if no corresponding y value, 0.0 is returned ... for example, when the
   * first data point is to the right of the parsed x value for the step-like cases ...
   * VERTICAL_HORIZONTAL & HORIZONTAL_VERTICAL
   */
  protected abstract double getY(double x);

  /** Handles plotting of Integer X to Y datasets */
  public static class Integer extends DatasetXtoY {
    /** The array containing the actual y values */
    public int[] yValue;
    /** The array containing the data graphed in the x plane */
    public int[] xdata;

    /**
     * Instantiate a XtoY Dataset with the parsed data arrays for x and y values. Dataset uses the
     * array passed, not a copy ... the default mode of error is DatasetXtoY.ERROR_DUPLICATE Throws
     * a Dataset.DatasetException if the passed arrays have different number of elements No default
     * empty dataset can be created
     */
    public Integer(int x[], int y[], int aRenderMethod, Point2D.Float optionalStartPoint)
        throws Dataset.DatasetException {
      if (x.length != y.length)
        throw new Dataset.DatasetException("The parsed arrays have different number of elements");

      xdata = x;
      yValue = y;
      ydata = new double[y.length];

      this.setRenderMethod(aRenderMethod, DatasetXtoY.ERROR_DUPLICATE);

      this.optionalStartPoint = optionalStartPoint;

      numDataPoints = y.length;
      this.setData();
      range();
      numDatasets++;
    }

    /**
     * Instantiate a XtoY Dataset with the parsed data arrays for x and y values. No default empty
     * dataset can be created
     */
    public Integer(int x[], int y[], int aRenderMethod) throws Dataset.DatasetException {
      this(x, y, aRenderMethod, null);
    }

    public Integer(int x[], int y[]) throws Dataset.DatasetException {
      this(x, y, DatasetXtoYRenderer.DIAGONAL, null);
    }

    /**
     * Instantiate a X to Y Dataset with the parsed data. Default stride is 2. The int array
     * contains the data. The X data is expected in the even indices, the y data in the odd. The
     * integer n is the number of data Points. This means that the length of the data array is 2*n.
     *
     * @param d Array containing the (x,y) data pairs.
     * @param n Number of (x,y) data pairs in the array.
     * @exception Exception A Generic exception if it fails to load the parsed array into the class.
     */
    public Integer(int d[], int n) throws Dataset.DatasetException {
      this(d, n, 2, DatasetXtoYRenderer.DIAGONAL, null);
    }

    /**
     * Instantiate a X to Y Dataset with the parsed data. The int array contains the data. The X
     * data is expected to be in indices i*stride where i=0,1,... The Y data is expected to be found
     * in indices i*stride+1 where i=0,1,2... The integer n is the number of data Points. This means
     * that the length of the data array is 2*stride.
     *
     * @param d Array containing the (x,y) data pairs.
     * @param n Number of (x,y) data pairs in the array.
     * @param s The stride of the data.
     * @exception Exception A Generic exception if it fails to load the parsed array into the class.
     */
    public Integer(int d[], int n, int s, int aRenderMethod, Point2D.Float optionalStartPoint)
        throws Dataset.DatasetException {
      if (s < 2) throw new Dataset.DatasetException("Invalid stride parameter!");

      if (d == null || d.length == 0 || n <= 0 || d.length < (n * s)) {
        throw new Dataset.DatasetException("X to Y Dataset: Error in parsed data!");
      }

      numDatasets++;

      this.setRenderMethod(aRenderMethod, DatasetXtoY.ERROR_DUPLICATE);

      //     Copy the data locally.
      xdata = new int[n];
      yValue = new int[n];
      ydata = new double[n];

      for (int i = 0; i < n; i++) {
        xdata[i] = d[s * i];
        yValue[i] = d[s * i + 1];
      }

      this.optionalStartPoint = optionalStartPoint;

      numDataPoints = n;
      this.setData();
      range();
    }

    /** get the primitive way in which data values are represented (Dataset.INTEGER_DATA) */
    public int getDataPrimitive() {
      return Dataset.INTEGER_DATA;
    }

    /**
     * set the render Method ... and set the object that will perform rendering operations for this
     * dataset ... based on the renderMethod and errorInYType
     */
    public void resetRenderer(int renderMethod) {
      switch (renderMethod) {
        case DatasetXtoYRenderer.DIAGONAL:
          if (renderer == null || !(renderer instanceof DatasetXtoYRendererIntegerDiag))
            this.setRenderer(new DatasetXtoYRendererIntegerDiag(this));
          break;
        case DatasetXtoYRenderer.HORIZONTAL_VERTICAL:
          if (renderer == null || !(renderer instanceof DatasetXtoYRendererIntegerHV))
            this.setRenderer(new DatasetXtoYRendererIntegerHV(this));
          break;
        case DatasetXtoYRenderer.VERTICAL_HORIZONTAL:
          if (renderer == null || !(renderer instanceof DatasetXtoYRendererIntegerVH))
            this.setRenderer(new DatasetXtoYRendererIntegerVH(this));
          break;
        default:
          if (renderer == null || !(renderer instanceof DatasetXtoYRendererIntegerDiag))
            this.setRenderer(new DatasetXtoYRendererIntegerDiag(this));
          break;
      }
    }

    /** sets the data that will actually be plotted as y */
    public void setData() {
      renderer.setData();
    }

    /**
     * Indicates whether or not we should show the error indicators ... as error bars or otherwise
     */
    public void setShowError(boolean b) {
      showErrorInY = b;
      renderer.setShowError(b);
    }

    /**
     * sets and returns the number of data points in the X to Y Dataset
     *
     * @return number of (x,y) points.
     */
    public int getNumDataPoint() {
      numDataPoints = Math.min(xdata.length, yValue.length);
      return numDataPoints;
    }

    /**
     * **** ALWAYS USE DOUBLE FOR CONSISTENCY *** given an x value (on horizontal axis), get the
     * corresponding y value if no corresponding y value, 0.0 is returned ... for example, when the
     * first data point is to the right of the parsed x value for the step-like cases ...
     * VERTICAL_HORIZONTAL & HORIZONTAL_VERTICAL
     */
    protected double getY(double x) {
      return (renderer.getY(x));
    }

    /**
     * get the data point at the parsed index.
     *
     * @param index Data point index e.g xdata[index],yValue[index]
     * @return Point containing the (x,y) pair.
     */
    public Point getPoint(int index) {
      return new Point((int) xdata[index], (int) yValue[index]);
    }

    /**
     * Return the data point that is closest to the parsed (x,y) position
     *
     * @param x
     * @param y (x,y) position in data space.
     * @return the closest data point.
     */
    public Point2D.Double getClosestPoint(double x, double y) {

      Point2D.Double aPoint = new Point2D.Double();
      double xdiff = 0.0, ydiff = 0.0, diff = 0.0, newdiff = 0.0;

      for (int i = 0; i < numDataPoints; i++) {
        xdiff = (xdata[i] - x);
        ydiff = (ydata[i] - y);

        newdiff = xdiff * xdiff + ydiff * ydiff;

        if (newdiff < diff) {
          aPoint.x = xdata[i];
          aPoint.y = ydata[i];
          diff = newdiff;
        }
      }
      return aPoint;
    }

    /** takes a graphics context and draws its plot onto it */
    public void draw(Graphics2D g, Dimension dim) {
      renderer.draw(g, dim);
    }

    /**
     * Calculate the range of the data. This modifies dxmin,dxmax,dymin,dymax Take the optional
     * start and/or end points into consideration also ... so the range includes the optional end
     * and start points This is because the values set here are very important ... they are used in
     * the axes and so on.
     */
    public void range() {
      int optionalXMax = java.lang.Integer.MIN_VALUE;
      int optionalXMin = java.lang.Integer.MAX_VALUE;
      int optionalYMax = java.lang.Integer.MIN_VALUE;
      int optionalYMin = java.lang.Integer.MAX_VALUE;

      if (optionalStartPoint != null) {
        optionalXMin = optionalXMax = (int) optionalStartPoint.x;
        optionalYMin = optionalYMax = (int) optionalStartPoint.y;
      }

      setXMax(Math.max(optionalXMax, MathPlus.getMaxValue(xdata)));
      setXMin(Math.min(optionalXMin, MathPlus.getMinValue(xdata)));
      setYMax(Math.max(optionalYMax, MathPlus.getMaxValue(ydata)));
      setYMin(Math.min(optionalYMin, MathPlus.getMinValue(ydata)));
    }
  }

  /** Handles plotting of Double X to Y datasets */
  public static class Double extends DatasetXtoY {
    /** The array containing the actual y values */
    public double[] yValue;
    /** The array containing the data graphed in the x plane */
    public double[] xdata;

    /**
     * Instantiate a XtoY Dataset with the parsed data arrays for x and y values. Dataset uses the
     * array passed, not a copy Throws a Dataset.DatasetException if the passed arrays have
     * different number of elements No default empty dataset can be created
     */
    public Double(double x[], double y[], int aRenderMethod, Point2D.Float optionalStartPoint)
        throws Dataset.DatasetException {
      if (x.length != y.length)
        throw new Dataset.DatasetException("The parsed arrays have different number of elements");

      xdata = x;
      yValue = y;
      ydata = new double[y.length];

      this.setRenderMethod(aRenderMethod, DatasetXtoY.ERROR_DUPLICATE);

      this.optionalStartPoint = optionalStartPoint;

      numDataPoints = y.length;
      this.setData();
      range();
      numDatasets++;
    }

    /**
     * Instantiate a XtoY Dataset with the parsed data arrays for x and y values. No default empty
     * dataset can be created
     */
    public Double(double x[], double y[], int aRenderMethod) throws Dataset.DatasetException {
      this(x, y, aRenderMethod, null);
    }

    public Double(double x[], double y[]) throws Dataset.DatasetException {
      this(x, y, DatasetXtoYRenderer.DIAGONAL, null);
    }

    /**
     * Instantiate a X to Y Dataset with the parsed data. Default stride is 2. The double array
     * contains the data. The X data is expected in the even indices, the y data in the odd. The
     * integer n is the number of data Points. This means that the length of the data array is 2*n.
     *
     * @param d Array containing the (x,y) data pairs.
     * @param n Number of (x,y) data pairs in the array.
     * @exception Exception A Generic exception if it fails to load the parsed array into the class.
     */
    public Double(double d[], int n) throws Dataset.DatasetException {
      this(d, n, 2, DatasetXtoYRenderer.DIAGONAL, null);
    }

    /**
     * Instantiate a X to Y Dataset with the parsed data. The double array contains the data. The X
     * data is expected to be in indices i*stride where i=0,1,... The Y data is expected to be found
     * in indices i*stride+1 where i=0,1,2... The integer n is the number of data Points. This means
     * that the length of the data array is 2*stride.
     *
     * @param d Array containing the (x,y) data pairs.
     * @param n Number of (x,y) data pairs in the array.
     * @param s The stride of the data.
     * @exception Exception A Generic exception if it fails to load the parsed array into the class.
     */
    public Double(double d[], int n, int s, int aRenderMethod, Point2D.Float optionalStartPoint)
        throws Dataset.DatasetException {
      if (s < 2) throw new Dataset.DatasetException("Invalid stride parameter!");

      if (d == null || d.length == 0 || n <= 0 || d.length < (n * s)) {
        throw new Dataset.DatasetException("X to Y Dataset: Error in parsed data!");
      }

      numDatasets++;

      this.setRenderMethod(aRenderMethod, DatasetXtoY.ERROR_DUPLICATE);

      //     Copy the data locally.
      xdata = new double[n];
      yValue = new double[n];
      ydata = new double[n];

      for (int i = 0; i < n; i++) {
        xdata[i] = d[s * i];
        yValue[i] = d[s * i + 1];
      }

      this.optionalStartPoint = optionalStartPoint;

      numDataPoints = n;
      this.setData();
      range();
    }

    /*
     *******************
     ** Public Methods
     *****************
     */

    /** get the primitive way in which data values are represented (Dataset.DOUBLE_DATA) */
    public int getDataPrimitive() {
      return Dataset.DOUBLE_DATA;
    }

    /**
     * set the render Method ... and set the object that will perform rendering operations for this
     * dataset ... based on the renderMethod and errorInYType
     */
    public void resetRenderer(int renderMethod) {
      switch (renderMethod) {
        case DatasetXtoYRenderer.DIAGONAL:
          if (renderer == null || !(renderer instanceof DatasetXtoYRendererDoubleDiag))
            this.setRenderer(new DatasetXtoYRendererDoubleDiag(this));
          break;
        case DatasetXtoYRenderer.HORIZONTAL_VERTICAL:
          if (renderer == null || !(renderer instanceof DatasetXtoYRendererDoubleHV))
            this.setRenderer(new DatasetXtoYRendererDoubleHV(this));
          break;
        case DatasetXtoYRenderer.VERTICAL_HORIZONTAL:
          if (renderer == null || !(renderer instanceof DatasetXtoYRendererDoubleVH))
            this.setRenderer(new DatasetXtoYRendererDoubleVH(this));
          break;
        default:
          if (renderer == null || !(renderer instanceof DatasetXtoYRendererDoubleDiag))
            this.setRenderer(new DatasetXtoYRendererDoubleDiag(this));
          break;
      }
    }

    /** sets the data that will actually be plotted as y */
    public void setData() {
      renderer.setData();
    }

    /**
     * Indicates whether or not we should show the error indicators ... as error bars or otherwise
     */
    public void setShowError(boolean b) {
      showErrorInY = b;
      renderer.setShowError(b);
    }

    /**
     * sets and returns the number of data points in the X to Y Dataset
     *
     * @return number of (x,y) points.
     */
    public int getNumDataPoint() {
      numDataPoints = Math.min(xdata.length, yValue.length);
      return numDataPoints;
    }

    /**
     * **** ALWAYS USE DOUBLE FOR CONSISTENCY *** given an x value (on horizontal axis), get the
     * corresponding y value if no corresponding y value, 0.0 is returned ... for example, when the
     * first data point is to the right of the parsed x value for the step-like cases ...
     * VERTICAL_HORIZONTAL & HORIZONTAL_VERTICAL
     */
    protected double getY(double x) {
      return (renderer.getY(x));
    }

    /**
     * get the data point at the parsed index.
     *
     * @param index Data point index e.g xdata[index],yValue[index]
     * @return Point containing the (x,y) pair.
     */
    public Point getPoint(int index) {
      return new Point((int) xdata[index], (int) yValue[index]);
    }

    /**
     * Return the data point that is closest to the parsed (x,y) position
     *
     * @param x
     * @param y (x,y) position in data space.
     * @return the closest data point.
     */
    public Point2D.Double getClosestPoint(double x, double y) {

      Point2D.Double aPoint = new Point2D.Double();
      double xdiff = 0.0, ydiff = 0.0, diff = 0.0, newdiff = 0.0;

      for (int i = 0; i < numDataPoints; i++) {
        xdiff = (xdata[i] - x);
        ydiff = (ydata[i] - y);

        newdiff = xdiff * xdiff + ydiff * ydiff;

        if (newdiff < diff) {
          aPoint.x = xdata[i];
          aPoint.y = ydata[i];
          diff = newdiff;
        }
      }
      return aPoint;
    }

    /** takes a graphics context and draws its plot onto it */
    public void draw(Graphics2D g, Dimension dim) {
      renderer.draw(g, dim);
    }

    /**
     * Calculate the range of the data. This modifies dxmin,dxmax,dymin,dymax Take the optional
     * start and/or end points into consideration also ... so the range includes the optional end
     * and start points This is because the values set here are very important ... they are used in
     * the axes and so on.
     */
    public void range() {
      double optionalXMax = -1.0 * java.lang.Double.MAX_VALUE;
      double optionalXMin = java.lang.Double.MAX_VALUE;
      double optionalYMax = -1.0 * java.lang.Double.MAX_VALUE;
      double optionalYMin = java.lang.Double.MAX_VALUE;

      if (optionalStartPoint != null) {
        optionalXMin = optionalXMax = (double) optionalStartPoint.x;
        optionalYMin = optionalYMax = (double) optionalStartPoint.y;
      }

      setXMax(Math.max(optionalXMax, MathPlus.getMaxValue(xdata)));
      setXMin(Math.min(optionalXMin, MathPlus.getMinValue(xdata)));
      setYMax(Math.max(optionalYMax, MathPlus.getMaxValue(ydata)));
      setYMin(Math.min(optionalYMin, MathPlus.getMinValue(ydata)));
    }
  }

  public static class DatasetXtoYLineAttribute {
    protected Color lineColor;
    protected BasicStroke lineStyle;

    public DatasetXtoYLineAttribute() {
      lineStyle = new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f);
      lineColor = Color.black;
    }

    public DatasetXtoYLineAttribute(Color lineColor, BasicStroke lineStyle) {
      setLineAttributes(lineColor, lineStyle);
    }

    public void setLineAttributes(Color lineColor, BasicStroke lineStyle) {
      this.lineStyle = lineStyle;
      this.lineColor = lineColor;
    }

    public Color getLineColor() {
      return lineColor;
    }

    public BasicStroke getLineStyle() {
      return lineStyle;
    }
  }
}
