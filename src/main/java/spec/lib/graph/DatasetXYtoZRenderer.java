package spec.lib.graph;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class DatasetXYtoZRenderer {
  protected DatasetXYtoZ dataset;

  /** defines a color plot */
  public static final int COLORPLOT = 61;

  public DatasetXYtoZRenderer(DatasetXYtoZ dataset) {
    this.dataset = dataset;
  }

  /** sets the data that will actually be plotted as z */
  public abstract void setData();

  /**
   * This draws the color representation of the z data It uses the concept of a maximum and minimum
   * range. ... THERE IS A LITTLE HACK ... Every value is in the range 1 to 100 % except the data
   * value is exactly zero (then it is 0 %)
   *
   * @param the background color of the dataset
   */
  public abstract void draw(Graphics2D g, Color background);

  public abstract static class Integer extends DatasetXYtoZRenderer {
    protected DatasetXYtoZ.Integer datasetI;

    public Integer(DatasetXYtoZ.Integer datasetI) {
      super(datasetI);
      this.datasetI = datasetI;
    }
  }

  public abstract static class Double extends DatasetXYtoZRenderer {
    protected DatasetXYtoZ.Double datasetD;

    public Double(DatasetXYtoZ.Double datasetD) {
      super(datasetD);
      this.datasetD = datasetD;
    }
  }
}
