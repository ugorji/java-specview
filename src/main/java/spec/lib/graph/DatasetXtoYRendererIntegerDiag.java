package spec.lib.graph;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class DatasetXtoYRendererIntegerDiag extends DatasetXtoYRenderer.Integer {

  public DatasetXtoYRendererIntegerDiag(DatasetXtoY.Integer datasetI) {
    super(datasetI);
  }

  /**
   * **** ALWAYS USE DOUBLE FOR CONSISTENCY *** given an x value (on horizontal axis), get the
   * corresponding y value if no corresponding y value, 0.0 is returned ... for example, when the
   * first data point is to the right of the parsed x value for the step-like cases ...
   * VERTICAL_HORIZONTAL & HORIZONTAL_VERTICAL
   */
  protected double getY(double x) {
    int closestX = 0, closestY = 0;
    // first get int value, contained in data set, which is the closest to x
    int minimumDiff = java.lang.Integer.MAX_VALUE, diff = java.lang.Integer.MAX_VALUE;
    for (int i = 0; i < datasetI.numDataPoints; i++) {
      diff = datasetI.xdata[i] - (int) x;
      // if new diff is smaller, then this is the new minimum difference
      // and closestY is the new corresponding value
      diff = Math.abs(diff);
      if (diff < minimumDiff) {
        closestX = datasetI.xdata[i];
        closestY = (int) datasetI.ydata[i];
        minimumDiff = diff;
      }
    }
    return (double) closestY;
  }

  protected void drawAlt(Graphics2D g, Dimension dim) {
    this.initDraw(g, dim);

    for (int i = 0; i < datasetI.numDataPoints; i++) {
      // translate these points to conform to the java rendering model
      p1.x = (float) ((datasetI.xdata[i] - xAxis.axisMin) / xAxisRange * w);
      p1.y = (float) ((yAxis.axisMax - datasetI.ydata[i]) / yAxisRange * (h - 1));

      // this.lineDraw (g, p0, p1, renderMethod);
      g.draw(new Line2D.Float(p0.x, p0.y, p1.x, p1.y));
      if (showErrorInY && (datasetI.errorInY != null)) {
        upErrorDist = (float) (datasetI.errorInY[i * 2] / yAxisRange * h);
        downErrorDist = (float) (datasetI.errorInY[i * 2 + 1] / yAxisRange * h);
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

    if (datasetI.errorInYValue != null && datasetI.errorInY == null)
      datasetI.errorInY = new double[datasetI.errorInYValue.length];

    if (datasetI.errorInYValue == null || datasetI.errorInY == null) resetError = false;

    switch (datasetI.scale) {
      case Dataset.LOG_10:
        for (int i = 0; i < datasetI.yValue.length; i++) {
          datasetI.ydata[i] = datasetI._log10(datasetI.yValue[i]);
          if (resetError) {
            datasetI.errorInY[i * 2] = datasetI._log10(datasetI.errorInYValue[i * 2]);
            datasetI.errorInY[i * 2 + 1] = datasetI._log10(datasetI.errorInYValue[i * 2 + 1]);
          }
        }
        break;
      case Dataset.LOG_E:
        for (int i = 0; i < datasetI.yValue.length; i++) {
          datasetI.ydata[i] = datasetI._log(datasetI.yValue[i]);
          if (resetError) {
            datasetI.errorInY[i * 2] = datasetI._log(datasetI.errorInYValue[i * 2]);
            datasetI.errorInY[i * 2 + 1] = datasetI._log(datasetI.errorInYValue[i * 2 + 1]);
          }
        }
        break;
      case Dataset.LINEAR:
      default:
        for (int i = 0; i < datasetI.yValue.length; i++) {
          // for some reason, directly assigning one array to the other did
          // not work ... so I have to create a whole new array
          datasetI.ydata[i] = datasetI.yValue[i];
          if (resetError) {
            datasetI.errorInY[i * 2] = (datasetI.errorInYValue[i * 2]);
            datasetI.errorInY[i * 2 + 1] = (datasetI.errorInYValue[i * 2 + 1]);
          }
        }
        break;
    }
  }

  protected void drawDup(Graphics2D g, Dimension dim) {
    this.initDraw(g, dim);

    for (int i = 0; i < datasetI.numDataPoints; i++) {
      // translate these points to conform to the java rendering model
      p1.x = (float) ((datasetI.xdata[i] - xAxis.axisMin) / xAxisRange * w);
      p1.y = (float) ((yAxis.axisMax - datasetI.ydata[i]) / yAxisRange * (h - 1));

      // this.lineDraw (g, p0, p1, renderMethod);
      g.draw(new Line2D.Float(p0.x, p0.y, p1.x, p1.y));
      if (showErrorInY && (datasetI.errorInY != null)) {
        upErrorDist = downErrorDist = (float) (datasetI.errorInY[i] / yAxisRange * h);
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

    if (datasetI.errorInYValue != null && datasetI.errorInY == null)
      datasetI.errorInY = new double[datasetI.errorInYValue.length];

    if (datasetI.errorInYValue == null || datasetI.errorInY == null) resetError = false;

    switch (datasetI.scale) {
      case Dataset.LOG_10:
        for (int i = 0; i < datasetI.yValue.length; i++) {
          datasetI.ydata[i] = datasetI._log10(datasetI.yValue[i]);
          if (resetError) datasetI.errorInY[i] = datasetI._log10(datasetI.errorInYValue[i]);
        }
        break;
      case Dataset.LOG_E:
        for (int i = 0; i < datasetI.yValue.length; i++) {
          datasetI.ydata[i] = datasetI._log(datasetI.yValue[i]);
          if (resetError) datasetI.errorInY[i] = datasetI._log(datasetI.errorInYValue[i]);
        }
        break;
      case Dataset.LINEAR:
      default:
        for (int i = 0; i < datasetI.yValue.length; i++) {
          // for some reason, directly assigning one array to the other did
          // not work ... so I have to create a whole new array
          datasetI.ydata[i] = datasetI.yValue[i];
          if (resetError) datasetI.errorInY[i] = (datasetI.errorInYValue[i]);
        }
        break;
    }
  }

  protected void drawHalf(Graphics2D g, Dimension dim) {
    this.initDraw(g, dim);

    for (int i = 0; i < datasetI.numDataPoints; i++) {
      // translate these points to conform to the java rendering model
      p1.x = (float) ((datasetI.xdata[i] - xAxis.axisMin) / xAxisRange * w);
      p1.y = (float) ((yAxis.axisMax - datasetI.ydata[i]) / yAxisRange * (h - 1));

      // this.lineDraw (g, p0, p1, renderMethod);
      g.draw(new Line2D.Float(p0.x, p0.y, p1.x, p1.y));
      if (showErrorInY && (datasetI.errorInY != null)) {
        upErrorDist = downErrorDist = (float) (datasetI.errorInY[i] / yAxisRange * h / 2.0);
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

    if (datasetI.errorInYValue != null && datasetI.errorInY == null)
      datasetI.errorInY = new double[datasetI.errorInYValue.length];

    if (datasetI.errorInYValue == null || datasetI.errorInY == null) resetError = false;

    switch (datasetI.scale) {
      case Dataset.LOG_10:
        for (int i = 0; i < datasetI.yValue.length; i++) {
          datasetI.ydata[i] = datasetI._log10(datasetI.yValue[i]);
          if (resetError) datasetI.errorInY[i] = datasetI._log10(datasetI.errorInYValue[i]);
        }
        break;
      case Dataset.LOG_E:
        for (int i = 0; i < datasetI.yValue.length; i++) {
          datasetI.ydata[i] = datasetI._log(datasetI.yValue[i]);
          if (resetError) datasetI.errorInY[i] = datasetI._log(datasetI.errorInYValue[i]);
        }
        break;
      case Dataset.LINEAR:
      default:
        for (int i = 0; i < datasetI.yValue.length; i++) {
          // for some reason, directly assigning one array to the other did
          // not work ... so I have to create a whole new array
          datasetI.ydata[i] = datasetI.yValue[i];
          if (resetError) datasetI.errorInY[i] = (datasetI.errorInYValue[i]);
        }
        break;
    }
  }
}
