package spec.lib.graph;

/** interface identifying a dataset of integer values */
public interface DatasetInterface {
  /**
   * get the primitive way in which data values are represented in the dataset, whether as integers
   * (Dataset.INTEGER_DATA) or doubles (Dataset.DOUBLE_DATA)
   */
  public int getDataPrimitive();

  /** get the type of dataset ... whether XtoY (Dataset.XTOY) or XYtoZ (Dataset.XYTOZ) */
  public int getDatasetType();

  /**
   * sets the data that will actually be plotted as y (in XtoY Dataset) or as z (in XYtoZ Dataset)
   */
  public void setData();

  /** set the name of the dataset */
  public void setName(String name);

  /**
   * Indicate whether or not we should show the error indicators ... as error bars or otherwise
   * ...Does nothing now ... since we have no clean way of showing the error
   */
  public void setShowError(boolean b);

  /** returns if error indication is being shown or not ... defaults to returning false */
  public boolean isShowError();

  // set the axes ... and attach this dataset to the axis to handle
  public void setAxisX(AxisX aAxisX);

  public void setAxisY(AxisY aAxisY);

  /** returns the scale used to render the dataset */
  public int getScale();

  /** returns the scale used to render the dataset as a String */
  public String getScaleAsString();

  /**
   * sets the scale used to render the dataset, and then fire the propertyChange subclasses should
   * override this by calling setScaleAndDoNotFirePropertyChange
   */
  // protected void setScale (int aScale);

  /** return the data X maximum ... as a double always (for consistency). */
  public double getXMax();
  /** return the data X minimum ... as a double (for consistency). */
  public double getXMin();
  /** return the data Y maximum ... as a double (for consistency). */
  public double getYMax();
  /** return the data Y minimum ... as a double (for consistency). */
  public double getYMin();

  /** set the data X maximum. */
  public void setXMax(double d);
  /** set the data X minimum. */
  public void setXMin(double d);
  /** set the data Y maximum. */
  public void setYMax(double d);
  /** set the data Y minimum. */
  public void setYMin(double d);
}
