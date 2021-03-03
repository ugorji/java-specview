package spec.lib.graph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import spec.lib.MathPlus;

public class DatasetXYtoZRendererIntegerColor extends DatasetXYtoZRenderer.Integer {
  public DatasetXYtoZRendererIntegerColor(DatasetXYtoZ.Integer datasetI) {
    super(datasetI);
  }

  /** sets the data that will actually be plotted as z */
  public void setData() {
    // manipulate the limits also so that a new scale will be used
    // and the graph Key will be updated for this
    datasetI.upperLimit = datasetI.absoluteUpperLimit = (double) java.lang.Integer.MIN_VALUE;
    datasetI.lowerLimit = datasetI.absoluteLowerLimit = (double) java.lang.Integer.MAX_VALUE;

    switch (datasetI.scale) {
      case Dataset.LOG_10:
        for (int i = 0; i < datasetI.zValue.length; i++) {
          for (int j = 0; j < datasetI.subLength; j++) {
            datasetI.data[i][j] = datasetI._log10(datasetI.zValue[i][j]);
          }
        }
        break;
      case Dataset.LOG_E:
        for (int i = 0; i < datasetI.zValue.length; i++) {
          for (int j = 0; j < datasetI.subLength; j++) {
            datasetI.data[i][j] = datasetI._log(datasetI.zValue[i][j]);
          }
        }
        break;
      case Dataset.LINEAR:
      default:
        for (int i = 0; i < datasetI.zValue.length; i++) {
          for (int j = 0; j < datasetI.subLength; j++) {
            datasetI.data[i][j] = datasetI.zValue[i][j];
          }
        }
        break;
    }

    datasetI.upperLimit = datasetI.absoluteUpperLimit = MathPlus.getMaxValue(datasetI.data);
    datasetI.lowerLimit = datasetI.absoluteLowerLimit = MathPlus.getMinValue(datasetI.data);
  }

  /**
   * This draws the color representation of the z data It uses the concept of a maximum and minimum
   * range. ... THERE IS A LITTLE HACK ... Every value is in the range 1 to 100 % except the data
   * value is exactly zero (then it is 0 %)
   *
   * @param the background color of the dataset
   */
  public void draw(Graphics2D g, Color background) {
    // System.out.println ("Draw Method of XY to Z Data Set called");
    Rectangle aRect = new Rectangle();
    int limitRange = (int) (datasetI.upperLimit - datasetI.lowerLimit);
    Color theColor;

    switch (datasetI.dataType) {
      case DatasetXYtoZ.Y_OF_X:
        // fill with the background color first
        aRect.setRect(0, 0, datasetI.dymax, datasetI.dxmax);
        g.setColor(background);
        g.fill(aRect);

        for (int i = 0; i < datasetI.dymax; i++) {
          for (int j = 0; j < datasetI.dxmax; j++) {
            // get the percentage that this value is of the whole range
            int b = (int) ((datasetI.data[i][j] - datasetI.lowerLimit) / limitRange * 100);
            if (b < 1) b = 1;
            else if (b > 100) b = 100;

            // if the data value is exactly zero, set the color to pure black
            if ((int) datasetI.data[i][j] == 0) b = 0;

            theColor = (Color) datasetI.colorTable.get(new Byte((byte) b));

            if (!(theColor.equals(background))) {
              // set color to color corresponding to this value (get from color table)
              aRect.setRect(j, ((datasetI.dymax - 1 - i)), 1, 1);
              g.setColor(theColor);
              g.fill(aRect);
            }
          }
        }
        break;
      case DatasetXYtoZ.X_OF_Y:
        // fill with the background color first
        aRect.setRect(0, 0, datasetI.dxmax, datasetI.dymax);
        g.setColor(background);
        g.fill(aRect);

        for (int i = 0; i < datasetI.dxmax; i++) {
          for (int j = 0; j < datasetI.dymax; j++) {
            // get the percentage that this value is of the whole range
            int b = (int) ((datasetI.data[i][j] - datasetI.lowerLimit) / limitRange * 100);
            if (b < 1) b = 1;
            else if (b > 100) b = 100;

            // if the data value is exactly zero, set the color to pure black
            if ((int) datasetI.data[i][j] == 0) b = 0;

            theColor = (Color) datasetI.colorTable.get(new Byte((byte) b));

            if (!(theColor.equals(background))) {
              // set color to color corresponding to this value
              aRect.setRect(i, (datasetI.dymax - 1 - j), 1, 1);
              g.setColor(theColor);
              g.fill(aRect);
            }
          }
        }
        break;
      default:
        break;
    }
  }
}
