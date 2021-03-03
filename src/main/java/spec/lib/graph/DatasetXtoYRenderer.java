package spec.lib.graph;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public abstract class DatasetXtoYRenderer {

  /** points are directly connected to each other */
  public static final int DIAGONAL = 11;
  /** points are connected by drawing a horizontal line then a vertical */
  public static final int HORIZONTAL_VERTICAL = 12;
  /** points are connected by drawing a vertical line then a horizontal */
  public static final int VERTICAL_HORIZONTAL = 13;

  protected DatasetXtoY dataset;
  protected boolean showErrorInY = false;

  /** necessary for the drawing */
  protected Point2D.Float p0;

  protected Point2D.Float p1;

  protected int h;
  protected int w;

  protected float upErrorDist;
  protected float downErrorDist;

  protected double xAxisRange;
  protected double yAxisRange;

  protected AxisX xAxis;
  protected AxisY yAxis;

  public DatasetXtoYRenderer(DatasetXtoY dataset) {
    this.dataset = dataset;
    xAxis = dataset.getAxisX();
    yAxis = dataset.getAxisY();
  }

  public void setShowError(boolean b) {
    showErrorInY = b;
  }

  /** subclasses should always call this to perform default stuff, in their draw method */
  protected void initDraw(Graphics2D g, Dimension dim) {
    g.setStroke(dataset.lineAttribute.lineStyle);
    g.setColor(dataset.lineAttribute.lineColor);

    xAxis = dataset.getAxisX();
    yAxis = dataset.getAxisY();

    p0 = new Point2D.Float();
    p1 = new Point2D.Float();

    h = dim.height;
    w = dim.width;

    xAxisRange = xAxis.axisMax - xAxis.axisMin;
    yAxisRange = yAxis.axisMax - yAxis.axisMin;

    upErrorDist = downErrorDist = 0.0f;
  }

  /** render the dataset on the graphics context */
  public void draw(Graphics2D g, Dimension dim) {
    switch (dataset.errorInYType) {
      case DatasetXtoY.ERROR_ALTERNATING:
        drawAlt(g, dim);
        break;
      case DatasetXtoY.ERROR_DUPLICATE:
        drawDup(g, dim);
        break;
      case DatasetXtoY.ERROR_HALF:
        drawHalf(g, dim);
        break;
    }
  }

  protected abstract void drawAlt(Graphics2D g, Dimension dim);

  protected abstract void drawDup(Graphics2D g, Dimension dim);

  protected abstract void drawHalf(Graphics2D g, Dimension dim);

  /**
   * **** ALWAYS USE DOUBLE FOR CONSISTENCY *** given an x value (on horizontal axis), get the
   * corresponding y value if no corresponding y value, 0.0 is returned ... for example, when the
   * first data point is to the right of the parsed x value for the step-like cases ...
   * VERTICAL_HORIZONTAL & HORIZONTAL_VERTICAL
   */
  protected abstract double getY(double x);

  /** sets the data that will actually be plotted as y and the error also */
  public void setData() {
    switch (dataset.errorInYType) {
      case DatasetXtoY.ERROR_ALTERNATING:
        setDataAlt();
        break;
      case DatasetXtoY.ERROR_DUPLICATE:
        setDataDup();
        break;
      case DatasetXtoY.ERROR_HALF:
        setDataHalf();
        break;
    }
  }

  protected abstract void setDataAlt();

  protected abstract void setDataDup();

  protected abstract void setDataHalf();

  public abstract static class Integer extends DatasetXtoYRenderer {
    protected DatasetXtoY.Integer datasetI;

    public Integer(DatasetXtoY.Integer datasetI) {
      super(datasetI);
      this.datasetI = datasetI;
    }

    /** subclasses should always call this to perform default stuff, in their draw method */
    protected void initDraw(Graphics2D g, Dimension dim) {
      super.initDraw(g, dim);
      // translate these points to conform to the java rendering model
      if (datasetI.optionalStartPoint != null) {
        p0.x = datasetI.optionalStartPoint.x;
        p0.y = datasetI.optionalStartPoint.y;
      } else {
        p0.x = (float) ((datasetI.xdata[0] - xAxis.axisMin) / xAxisRange * w);
        // use (h-1) to accomodate the (0,0) starting position
        p0.y = (float) ((yAxis.axisMax - datasetI.ydata[0]) / yAxisRange * (h - 1));
      }
    }
  }

  public abstract static class Double extends DatasetXtoYRenderer {
    protected DatasetXtoY.Double datasetD;

    public Double(DatasetXtoY.Double datasetD) {
      super(datasetD);
      this.datasetD = datasetD;
    }

    /** subclasses should always call this to perform default stuff, in their draw method */
    protected void initDraw(Graphics2D g, Dimension dim) {
      super.initDraw(g, dim);
      // translate these points to conform to the java rendering model
      if (datasetD.optionalStartPoint != null) {
        p0.x = datasetD.optionalStartPoint.x;
        p0.y = datasetD.optionalStartPoint.y;
      } else {
        p0.x = (float) ((datasetD.xdata[0] - xAxis.axisMin) / xAxisRange * w);
        // use (h-1) to accomodate the (0,0) starting position
        p0.y = (float) ((yAxis.axisMax - datasetD.ydata[0]) / yAxisRange * (h - 1));
      }
    }
  }
}
