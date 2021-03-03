package spec.lib.graph;

import spec.lib.MathPlus;

public abstract class DatasetXYtoZRendererDoubleColor extends DatasetXYtoZRenderer.Double {
  public DatasetXYtoZRendererDoubleColor(DatasetXYtoZ.Double datasetD) {
    super(datasetD);
  }

  /** sets the data that will actually be plotted as z */
  public void setData() {
    // manipulate the limits also so that a new scale will be used
    // and the graph Key will be updated for this
    datasetD.upperLimit = datasetD.absoluteUpperLimit = -1.0 * java.lang.Double.MAX_VALUE;
    datasetD.lowerLimit = datasetD.absoluteLowerLimit = java.lang.Double.MAX_VALUE;

    switch (datasetD.scale) {
      case Dataset.LOG_10:
        for (int i = 0; i < datasetD.zValue.length; i++) {
          for (int j = 0; j < datasetD.subLength; j++) {
            datasetD.data[i][j] = datasetD._log10(datasetD.zValue[i][j]);
          }
        }
        break;
      case Dataset.LOG_E:
        for (int i = 0; i < datasetD.zValue.length; i++) {
          for (int j = 0; j < datasetD.subLength; j++) {
            datasetD.data[i][j] = datasetD._log(datasetD.zValue[i][j]);
          }
        }
        break;
      case Dataset.LINEAR:
      default:
        for (int i = 0; i < datasetD.zValue.length; i++) {
          for (int j = 0; j < datasetD.subLength; j++) {
            datasetD.data[i][j] = datasetD.zValue[i][j];
          }
        }
        break;
    }

    datasetD.upperLimit = datasetD.absoluteUpperLimit = MathPlus.getMaxValue(datasetD.data);
    datasetD.lowerLimit = datasetD.absoluteLowerLimit = MathPlus.getMinValue(datasetD.data);
  }
}
