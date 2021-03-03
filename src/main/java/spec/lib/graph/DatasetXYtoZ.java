package spec.lib.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import spec.lib.MathPlus;
import spec.lib.Misc;

/*
 **************************************************************************
 **    Class  XYtoZ to handle XY to Z data sets
 **************************************************************************
 **    Copyright Ugorji Nwoke based on work by ... Leigh Brookshaw
 *************************************************************************/
public abstract class DatasetXYtoZ extends Dataset {
  /** each element of data array is an array of x values for a particular Y value */
  public static final int Y_OF_X = 51;
  /** each element of data array is an array of x values for a particular Y value */
  public static final int X_OF_Y = 52;
  /** defines a color plot */
  public static final int COLORPLOT = DatasetXYtoZRenderer.COLORPLOT;
  // public static final int CONTOURPLOT = 62;

  /** the render method */
  protected int renderMethod = this.COLORPLOT;

  /**
   * the type of 2 dimensional data array tells whether each element of data array is an array of X
   * values for a Y value (Y_OF_X) or if each element of data array is an array of Y values for an X
   * value (X_OF_Y)
   */
  protected int dataType = this.Y_OF_X;

  /** the colorTable for this graph It maps a percentage value to a color */
  protected Map colorTable = GraphSupport.blackBackgroundColorMap;

  /** property for the color map */
  public static final String IS_INVERTED_COLOR_MAP_PROPERTY = "IS_INVERTED_COLOR_MAP";
  /** property for the color map */
  public static final String COLOR_MAP_PROPERTY = "COLOR_MAP_PROPERTY";

  /*
   *********************
   ** Protected Variables
   **********************/

  /** The array that will be graphed based on the scale used */
  protected double[][] data;

  /** Two dimensional array of the error in a dataset value */
  protected double[][] errorInZ;

  /** Holds the other dimension of this rectangular array */
  protected int subLength;

  /** Upper and Lower limits of data values that can be recognized by the color table */
  public double upperLimit = -1.0 * java.lang.Double.MAX_VALUE;

  public double lowerLimit = java.lang.Double.MAX_VALUE;

  /**
   * Absolute upper and lower limits of the data in this data set This is different from those
   * recognized by the color table
   */
  protected double absoluteUpperLimit = -1.0 * java.lang.Double.MAX_VALUE;

  protected double absoluteLowerLimit = java.lang.Double.MAX_VALUE;

  protected DatasetXYtoZRenderer renderer;

  // instance initializer ... initializing these variables
  {
    /** The dataset name */
    name = "An XYZ Data Set";
    /** The dataset font */
    font = new Font("Helvetica", Font.BOLD, 18);
  }

  /** get the type of dataset ... (Dataset.XYTOZ) */
  public int getDatasetType() {
    return Dataset.XYTOZ;
  }

  /**
   * sets the error in the Z values The errorInZ array is set to d (shares the same array with d)
   */
  public void setErrorInZ(double[][] d) throws Dataset.DatasetException {
    try {
      int subLength = Misc.getRectangularArrayHeight(d);
    } catch (Misc.NotRectangularArrayException nrae) {
      throw new Dataset.DatasetException("The passed array is not rectangular");
    }

    if (!((this.subLength == subLength) && (data.length == d.length))) {
      throw new Dataset.DatasetException("This array has different length from dataset array");
    }

    errorInZ = d;
  }

  /**
   * sets the scale used to render the dataset, without firing the property change event This way,
   * subclasses can call this, do other manipulations, then fire the event themselves
   */
  protected void setScaleAndDoNotFirePropertyChange(int aScale) {
    super.setScaleAndDoNotFirePropertyChange(aScale);
    // cause a redraw
    // adjust your z data
    this.setData();
    // cause a redraw of the datasetImage
  }

  /** sets the scale used to render the dataset */
  protected void setScale(int aScale) {
    int oldValue = scale;
    setScaleAndDoNotFirePropertyChange(aScale);

    // fire the property Change
    this.firePropertyChange(Dataset.SCALE_PROPERTY, oldValue, scale);
  }

  /**
   * given the x and y value, it returns the value at that index of the data being graphed (data,
   * which is always a double ... to accomodate log scales) ... based on the data array type ...
   * Y_OF_X or X_OF_Y
   */
  public double getValue(double x, double y) {
    double d = GraphAxisData.POSITION_VALUE_UNKNOWN;

    switch (dataType) {
      case Y_OF_X:
        d = data[(int) y][(int) x];
        break;
      case X_OF_Y:
        d = data[(int) x][(int) y];
        break;
    }

    return d;
  }

  /** method to reset the renderer based on some understood types */
  public abstract void resetRenderer(int type);

  /** set the renderer to a specific object */
  public abstract void setRenderer(DatasetXYtoZRenderer renderer);

  /**
   * This draws the color representation of the z data It uses the concept of a maximum and minimum
   * range. ... THERE IS A LITTLE HACK ... Every value is in the range 1 to 100 % except the data
   * value is exactly zero (then it is 0 %)
   */
  public abstract void draw(Graphics2D g, Color background);

  /**
   * draws a data set onto a graphics context also ... ... using the color used to represent 0% in
   * the color table
   */
  public void draw(Graphics2D g) {
    draw(g, (Color) colorTable.get(new Byte((byte) 0)));
  }

  /** get the graph image ... a subImage of the datasetImage representing this dataset */
  protected BufferedImage getGraphImage(BufferedImage datasetImage) {
    if (datasetImage == null) return null;

    int x = (int) ((xAxis.axisMin - dxmin) / (dxmax - dxmin) * datasetImage.getWidth());
    int y = (int) ((dymax - yAxis.axisMax) / (dymax - dymin) * datasetImage.getHeight());
    int w = (int) ((xAxis.axisMax - xAxis.axisMin) / (dxmax - dxmin) * datasetImage.getWidth());
    int h = (int) ((yAxis.axisMax - yAxis.axisMin) / (dymax - dymin) * datasetImage.getHeight());

    return (datasetImage.getSubimage(x, y, w, h));
  }

  /**
   * sets the color table to the passed map, ... later, implement a check to ensure that the map
   * passed has a key set of 1 to 100, and the values are all instances of java.awt.Color
   */
  protected void setColorMap(Map map) {
    if (colorTable == map) return;

    Map oldValue = colorTable;
    colorTable = map;
    this.firePropertyChange(DatasetXYtoZ.COLOR_MAP_PROPERTY, oldValue, map);
  }

  /** handles plotting a 3 dimensional array of integer values */
  public static class Integer extends DatasetXYtoZ {
    /**
     * The actual 2-dimensional array holding the data to be plotted Note that this 2-dim array must
     * be rectangular This data is an array of Y sub-array containing X elements
     */
    protected int[][] zValue;

    /**
     * Instantiate a XYtoZ Dataset with the parsed data 2-dimensional array. *** DOES NOT USE A COPY
     * *** No default empty dataset can be created Note that this 2-dim array must be rectangular
     * This data is an array of Y sub-array containing X elements Throws a Dataset.DatasetException
     * if the passed array is not rectangular
     */
    public Integer(int[][] aData, int type) throws Dataset.DatasetException {
      try {
        subLength = Misc.getRectangularArrayHeight(aData);
      } catch (Misc.NotRectangularArrayException nrae) {
        throw new Dataset.DatasetException("The passed array is not rectangular");
      }

      // set the data type
      switch (type) {
        case Y_OF_X:
        case X_OF_Y:
          dataType = type;
          break;
        default:
          dataType = Y_OF_X;
          break;
      }

      // set renderer here, since it performs setData
      resetRenderer(DatasetXYtoZRenderer.COLORPLOT);

      zValue = aData;
      absoluteUpperLimit = (double) MathPlus.getMaxValue(zValue);
      absoluteLowerLimit = (double) MathPlus.getMinValue(zValue);

      // initialize data (the array that will actually be graphed ... depending on scale
      data = new double[zValue.length][subLength];
      // set data based on the scale
      this.setData();

      // set the initial upper and lower limits recognized by the color table
      // to the absolute values
      upperLimit = absoluteUpperLimit;
      lowerLimit = absoluteLowerLimit;

      // set up data limits (range)
      dxmin = 0;
      dymin = 0;
      switch (dataType) {
        case Y_OF_X:
          dymax = zValue.length;
          dxmax = subLength;
          break;
        case X_OF_Y:
          dxmax = zValue.length;
          dymax = subLength;
          break;
        default:
          break;
      }
    }

    public void resetRenderer(int type) {
      switch (type) {
        case DatasetXYtoZRenderer.COLORPLOT:
        default:
          if (renderer == null) setRenderer(new DatasetXYtoZRendererIntegerColor(this));
          break;
      }
    }

    public void setRenderer(DatasetXYtoZRenderer renderer) {
      if (renderer != null && renderer instanceof DatasetXYtoZRenderer.Integer)
        this.renderer = renderer;
    }

    /** get the primitive way in which data values are represented, (Dataset.INTEGER_DATA) */
    public int getDataPrimitive() {
      return Dataset.INTEGER_DATA;
    }

    /** sets the data that will actually be plotted as z */
    public void setData() {
      renderer.setData();

      /*
      ************ NOW HANDLED BY THE RENDERER *******************************
      // manipulate the limits also so that a new scale will be used
      // and the graph Key will be updated for this
      upperLimit = absoluteUpperLimit = (double) java.lang.Integer.MIN_VALUE;
      lowerLimit = absoluteLowerLimit = (double) java.lang.Integer.MAX_VALUE;

      switch (scale) {
      case LOG_10:
      for (int i = 0; i < zValue.length; i++) {
      for (int j = 0; j < subLength; j++) {
      data [i][j] = this._log10 (zValue [i][j]);
      }
      }
      break;
      case LOG_E:
      for (int i = 0; i < zValue.length; i++) {
      for (int j = 0; j < subLength; j++) {
      data [i][j] = this._log (zValue [i][j]);
      }
      }
      break;
      case  LINEAR:
      default:
      for (int i = 0; i < zValue.length; i++) {
      for (int j = 0; j < subLength; j++) {
      data [i][j] = zValue [i][j];
      }
      }
      break;
      }

      upperLimit = absoluteUpperLimit = MathPlus.getMaxValue (data);
      lowerLimit = absoluteLowerLimit = MathPlus.getMinValue (data);
      ************ NOW HANDLED BY THE RENDERER *******************************
      */

    }

    /**
     * This draws the color representation of the z data It uses the concept of a maximum and
     * minimum range. ... THERE IS A LITTLE HACK ... Every value is in the range 1 to 100 % except
     * the data value is exactly zero (then it is 0 %)
     *
     * @param the background color of the dataset
     */
    public void draw(Graphics2D g, Color background) {
      renderer.draw(g, background);

      /*
      ************ NOW HANDLED BY THE RENDERER *******************************
      // System.out.println ("Draw Method of XY to Z Data Set called");
      Rectangle aRect = new Rectangle ();
      int limitRange = (int) (upperLimit - lowerLimit);
      Color theColor;

      switch (dataType) {
      case Y_OF_X:
      // fill with the background color first
      aRect.setRect (0, 0, dymax, dxmax );
      g.setColor (background);
      g.fill (aRect);

      for (int i = 0; i < dymax; i++) {
      for (int j = 0; j < dxmax; j++) {
      // get the percentage that this value is of the whole range
      int b = (int) ( (data [i][j] - lowerLimit) / limitRange * 100 );
      if (b < 1)
      b = 1;
      else if (b > 100)
      b = 100;
      // if the data value is exactly zero, set the color to pure black
      if ( (int) data [i][j] == 0)
      b = 0;

      theColor = (Color) colorTable.get (new Byte ( (byte) b) );

      if ( !(theColor.equals (background) ) ) {
      // set color to color corresponding to this value (get from color table)
      aRect.setRect ( j, ((dymax-1-i)),  1,  1);
      g.setColor (theColor);
      g.fill (aRect);
      }

      }
      }
      break;
      case X_OF_Y:
      // fill with the background color first
      aRect.setRect (0, 0, dxmax, dymax );
      g.setColor (background);
      g.fill (aRect);
      for (int i = 0; i < dxmax; i++ ) {
      for (int j = 0; j < dymax; j++) {
      // get the percentage that this value is of the whole range
      int b = (int) ( (data [i][j] - lowerLimit) / limitRange * 100 );
      if (b < 1)
      b = 1;
      else if (b > 100)
      b = 100;
      // if the data value is exactly zero, set the color to pure black
      if ( (int) data [i][j] == 0)
      b = 0;

      theColor = (Color) colorTable.get (new Byte ( (byte) b) );
      if ( !(theColor.equals (background) ) ) {
      // set color to color corresponding to this value
      aRect.setRect (  i,  (dymax-1-j), 1, 1 );
      g.setColor (theColor);
      g.fill (aRect);
      }

      }
      }
      break;
      default:
      break;
      }
      ************ NOW HANDLED BY THE RENDERER *******************************
      */

    }
  }

  /** abstract for now ... since we do not really know how to implement this one yet */
  public abstract static class Double extends DatasetXYtoZ {
    /**
     * The actual 2-dimensional array holding the data to be plotted Note that this 2-dim array must
     * be rectangular This data is an array of Y sub-array containing X elements
     */
    protected double[][] zValue;

    /**
     * Instantiate a XYtoZ Dataset with the parsed data 2-dimensional array. *** DOES NOT USE A COPY
     * *** No default empty dataset can be created Note that this 2-dim array must be rectangular
     * This data is an array of Y sub-array containing X elements Throws a Dataset.DatasetException
     * if the passed array is not rectangular
     */
    public Double(double[][] aData, int type) throws Dataset.DatasetException {
      try {
        subLength = Misc.getRectangularArrayHeight(aData);
      } catch (Misc.NotRectangularArrayException nrae) {
        throw new Dataset.DatasetException("The passed array is not rectangular");
      }

      // set the data type
      switch (type) {
        case Y_OF_X:
        case X_OF_Y:
          dataType = type;
          break;
        default:
          dataType = Y_OF_X;
          break;
      }

      // set renderer here, since it performs setData
      resetRenderer(DatasetXYtoZRenderer.COLORPLOT);

      zValue = aData;
      absoluteUpperLimit = (double) MathPlus.getMaxValue(zValue);
      absoluteLowerLimit = (double) MathPlus.getMinValue(zValue);

      // initialize data (the array that will actually be graphed ... depending on scale
      data = new double[zValue.length][subLength];
      // set data based on the scale
      this.setData();

      // set the initial upper and lower limits recognized by the color table
      // to the absolute values
      upperLimit = absoluteUpperLimit;
      lowerLimit = absoluteLowerLimit;

      // set up data limits (range)
      dxmin = 0;
      dymin = 0;
      switch (dataType) {
        case Y_OF_X:
          dymax = zValue.length;
          dxmax = subLength;
          break;
        case X_OF_Y:
          dxmax = zValue.length;
          dymax = subLength;
          break;
        default:
          break;
      }
    }

    public void resetRenderer(int type) {
      switch (type) {
        case DatasetXYtoZRenderer.COLORPLOT:
        default:
          // if (renderer == null || !(renderer instanceof DatasetXYtoZRendererDoubleColor) )
          // setRenderer (new DatasetXYtoZRendererDoubleColor (this) );
          break;
      }
    }

    public void setRenderer(DatasetXYtoZRenderer renderer) {
      if (renderer != null && renderer instanceof DatasetXYtoZRenderer.Double)
        this.renderer = renderer;
    }

    /** get the primitive way in which data values are represented (Dataset.DOUBLE_DATA) */
    public int getDataPrimitive() {
      return Dataset.DOUBLE_DATA;
    }

    /** sets the data that will actually be plotted as z */
    public void setData() {
      renderer.setData();

      //       // manipulate the limits also so that a new scale will be used
      //       // and the graph Key will be updated for this
      //       upperLimit = absoluteUpperLimit = -1.0 * java.lang.Double.MAX_VALUE;
      //       lowerLimit = absoluteLowerLimit = java.lang.Double.MAX_VALUE;

      //       switch (scale) {
      //       case LOG_10:
      // 	for (int i = 0; i < zValue.length; i++) {
      // 	  for (int j = 0; j < subLength; j++) {
      // 	    data [i][j] = this._log10 (zValue [i][j]);
      // 	  }
      // 	}
      // 	break;
      //       case LOG_E:
      // 	for (int i = 0; i < zValue.length; i++) {
      // 	  for (int j = 0; j < subLength; j++) {
      // 	    data [i][j] = this._log (zValue [i][j]);
      // 	  }
      // 	}
      // 	break;
      //       case  LINEAR:
      //       default:
      // 	for (int i = 0; i < zValue.length; i++) {
      // 	  for (int j = 0; j < subLength; j++) {
      // 	    data [i][j] = zValue [i][j];
      // 	  }
      // 	}
      // 	break;
      //       }

      //       upperLimit = absoluteUpperLimit = MathPlus.getMaxValue (data);
      //       lowerLimit = absoluteLowerLimit = MathPlus.getMinValue (data);

    }
  }
}
