package spec.lib.graph;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class DatasetXtoYRendererDoubleDiag extends DatasetXtoYRenderer.Double {

  public DatasetXtoYRendererDoubleDiag(DatasetXtoY.Double datasetD) {
    super(datasetD);
  }

  /**
   * **** ALWAYS USE DOUBLE FOR CONSISTENCY *** given an x value (on horizontal axis), get the
   * corresponding y value if no corresponding y value, 0.0 is returned ... for example, when the
   * first data point is to the right of the parsed x value for the step-like cases ...
   * VERTICAL_HORIZONTAL & HORIZONTAL_VERTICAL
   */
  protected double getY(double x) {
    double closestX = 0.0, closestY = 0.0;
    // first get double value, contained in data set, which is the closest to x
    double minimumDiff = java.lang.Double.MAX_VALUE, diff = java.lang.Double.MAX_VALUE;
    for (int i = 0; i < datasetD.numDataPoints; i++) {
      diff = datasetD.xdata[i] - x;
      // if new diff is smaller, then this is the new minimum difference
      // and closestY is the new corresponding value
      diff = Math.abs(diff);
      if (diff < minimumDiff) {
        closestX = datasetD.xdata[i];
        closestY = datasetD.ydata[i];
        minimumDiff = diff;
      }
    }
    return closestY;
  }

  protected void drawAlt(Graphics2D g, Dimension dim) {
    this.initDraw(g, dim);

    for (int i = 0; i < datasetD.numDataPoints; i++) {
      // translate these points to conform to the java rendering model
      p1.x = (float) ((datasetD.xdata[i] - xAxis.axisMin) / xAxisRange * w);
      p1.y = (float) ((yAxis.axisMax - datasetD.ydata[i]) / yAxisRange * (h - 1));

      // this.lineDraw (g, p0, p1, renderMethod);
      g.draw(new Line2D.Float(p0.x, p0.y, p1.x, p1.y));
      if (showErrorInY && (datasetD.errorInY != null)) {
        upErrorDist = (float) (datasetD.errorInY[i * 2] / yAxisRange * h);
        downErrorDist = (float) (datasetD.errorInY[i * 2 + 1] / yAxisRange * h);
        // this.errorDrawInY (g, p0, p1, upErrorDist, downErrorDist, renderMethod);
        g.draw(new Line2D.Float(p1.x, p1.y, p1.x, p1.y - upErrorDist));
        g.draw(new Line2D.Float(p1.x, p1.y, p1.x, p1.y + downErrorDist));
      }

      // substitute this point for p1 so we do not have to calculate it again
      p0.x = p1.x;
      p0.y = p1.y;
    }
  }

  /** sets the data that will actually be plotted as y ... and the errorInY */
  protected void setDataAlt() {
    boolean resetError = true;

    if (datasetD.errorInYValue != null && datasetD.errorInY == null)
      datasetD.errorInY = new double[datasetD.errorInYValue.length];

    if (datasetD.errorInYValue == null || datasetD.errorInY == null) resetError = false;

    switch (datasetD.scale) {
      case Dataset.LOG_10:
        for (int i = 0; i < datasetD.yValue.length; i++) {
          datasetD.ydata[i] = datasetD._log10(datasetD.yValue[i]);
          if (resetError) {
            datasetD.errorInY[i * 2] = datasetD._log10(datasetD.errorInYValue[i * 2]);
            datasetD.errorInY[i * 2 + 1] = datasetD._log10(datasetD.errorInYValue[i * 2 + 1]);
          }
        }
        break;
      case Dataset.LOG_E:
        for (int i = 0; i < datasetD.yValue.length; i++) {
          datasetD.ydata[i] = datasetD._log(datasetD.yValue[i]);
          if (resetError) {
            datasetD.errorInY[i * 2] = datasetD._log(datasetD.errorInYValue[i * 2]);
            datasetD.errorInY[i * 2 + 1] = datasetD._log(datasetD.errorInYValue[i * 2 + 1]);
          }
        }
        break;
      case Dataset.LINEAR:
      default:
        for (int i = 0; i < datasetD.yValue.length; i++) {
          // for some reason, directly assigning one array to the other did
          // not work ... so I have to create a whole new array
          datasetD.ydata[i] = datasetD.yValue[i];
          if (resetError) {
            datasetD.errorInY[i * 2] = (datasetD.errorInYValue[i * 2]);
            datasetD.errorInY[i * 2 + 1] = (datasetD.errorInYValue[i * 2 + 1]);
          }
        }
        break;
    }
  }

  protected void drawDup(Graphics2D g, Dimension dim) {
    this.initDraw(g, dim);

    for (int i = 0; i < datasetD.numDataPoints; i++) {
      // translate these points to conform to the java rendering model
      p1.x = (float) ((datasetD.xdata[i] - xAxis.axisMin) / xAxisRange * w);
      p1.y = (float) ((yAxis.axisMax - datasetD.ydata[i]) / yAxisRange * (h - 1));

      // this.lineDraw (g, p0, p1, renderMethod);
      g.draw(new Line2D.Float(p0.x, p0.y, p1.x, p1.y));
      if (showErrorInY && (datasetD.errorInY != null)) {
        upErrorDist = downErrorDist = (float) (datasetD.errorInY[i] / yAxisRange * h);
        // this.errorDrawInY (g, p0, p1, upErrorDist, downErrorDist, renderMethod);
        g.draw(new Line2D.Float(p1.x, p1.y, p1.x, p1.y - upErrorDist));
        g.draw(new Line2D.Float(p1.x, p1.y, p1.x, p1.y + downErrorDist));
      }

      // substitute this point for p1 so we do not have to calculate it again
      p0.x = p1.x;
      p0.y = p1.y;
    }
  }

  /** sets the data that will actually be plotted as y ... and the errorInY */
  protected void setDataDup() {
    boolean resetError = true;

    if (datasetD.errorInYValue != null && datasetD.errorInY == null)
      datasetD.errorInY = new double[datasetD.errorInYValue.length];

    if (datasetD.errorInYValue == null || datasetD.errorInY == null) resetError = false;

    switch (datasetD.scale) {
      case Dataset.LOG_10:
        for (int i = 0; i < datasetD.yValue.length; i++) {
          datasetD.ydata[i] = datasetD._log10(datasetD.yValue[i]);
          if (resetError) datasetD.errorInY[i] = datasetD._log10(datasetD.errorInYValue[i]);
        }
        break;
      case Dataset.LOG_E:
        for (int i = 0; i < datasetD.yValue.length; i++) {
          datasetD.ydata[i] = datasetD._log(datasetD.yValue[i]);
          if (resetError) datasetD.errorInY[i] = datasetD._log(datasetD.errorInYValue[i]);
        }
        break;
      case Dataset.LINEAR:
      default:
        for (int i = 0; i < datasetD.yValue.length; i++) {
          // for some reason, directly assigning one array to the other did
          // not work ... so I have to create a whole new array
          datasetD.ydata[i] = datasetD.yValue[i];
          if (resetError) datasetD.errorInY[i] = (datasetD.errorInYValue[i]);
        }
        break;
    }
  }

  protected void drawHalf(Graphics2D g, Dimension dim) {
    this.initDraw(g, dim);

    for (int i = 0; i < datasetD.numDataPoints; i++) {
      // translate these points to conform to the java rendering model
      p1.x = (float) ((datasetD.xdata[i] - xAxis.axisMin) / xAxisRange * w);
      p1.y = (float) ((yAxis.axisMax - datasetD.ydata[i]) / yAxisRange * (h - 1));

      // this.lineDraw (g, p0, p1, renderMethod);
      g.draw(new Line2D.Float(p0.x, p0.y, p1.x, p1.y));
      if (showErrorInY && (datasetD.errorInY != null)) {
        upErrorDist = downErrorDist = (float) (datasetD.errorInY[i] / yAxisRange * h / 2.0);
        // this.errorDrawInY (g, p0, p1, upErrorDist, downErrorDist, renderMethod);
        g.draw(new Line2D.Float(p1.x, p1.y, p1.x, p1.y - upErrorDist));
        g.draw(new Line2D.Float(p1.x, p1.y, p1.x, p1.y + downErrorDist));
      }

      // substitute this point for p1 so we do not have to calculate it again
      p0.x = p1.x;
      p0.y = p1.y;
    }
  }

  /** sets the data that will actually be plotted as y ... and the errorInY */
  protected void setDataHalf() {
    boolean resetError = true;

    if (datasetD.errorInYValue != null && datasetD.errorInY == null)
      datasetD.errorInY = new double[datasetD.errorInYValue.length];

    if (datasetD.errorInYValue == null || datasetD.errorInY == null) resetError = false;

    switch (datasetD.scale) {
      case Dataset.LOG_10:
        for (int i = 0; i < datasetD.yValue.length; i++) {
          datasetD.ydata[i] = datasetD._log10(datasetD.yValue[i]);
          if (resetError) datasetD.errorInY[i] = datasetD._log10(datasetD.errorInYValue[i]);
        }
        break;
      case Dataset.LOG_E:
        for (int i = 0; i < datasetD.yValue.length; i++) {
          datasetD.ydata[i] = datasetD._log(datasetD.yValue[i]);
          if (resetError) datasetD.errorInY[i] = datasetD._log(datasetD.errorInYValue[i]);
        }
        break;
      case Dataset.LINEAR:
      default:
        for (int i = 0; i < datasetD.yValue.length; i++) {
          // for some reason, directly assigning one array to the other did
          // not work ... so I have to create a whole new array
          datasetD.ydata[i] = datasetD.yValue[i];
          if (resetError) datasetD.errorInY[i] = (datasetD.errorInYValue[i]);
        }
        break;
    }
  }
}
