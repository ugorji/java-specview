package spec.spectrum;

import java.io.Serializable;
import spec.lib.MathPlus;
import spec.lib.Misc;

/** SpecChannel need not be abstract since, by default, it is similar to a SpecChannel.OneDim */
public abstract class SpecChannel implements Cloneable, Serializable {
  /** holds the maximum spectrum channel count */
  protected int maxCount = 0;
  /** array holding the position of the maximum count */
  protected int[] maxCountPosition;

  /** checks if two specChannels have the same shape */
  public abstract boolean isSameShape(SpecChannel a, SpecChannel b);

  protected static final int search(int[] array, int value) {
    int position = -1;

    for (int i = 0; i < array.length; i++) {
      if (array[i] == value) {
        position = i;
        break;
      }
    }

    return position;

    // int[] array2 = new int [array.length];
    // System.arraycopy (array, 0, array2, 0, array.length);
    // Arrays.sort ( array2 );
    // return ( Arrays.binarySearch (array2, value) );
  }

  /** returns the maximum channel count */
  public int getMaxCount() {
    return maxCount;
  }

  /** returns the maximum channel count */
  public int[] getMaxCountPosition() {
    return maxCountPosition;
  }

  public static class OneDim extends SpecChannel {
    private int specShape;
    /** array containing the counts of the spectrum channels */
    protected int[] specCount;
    /**
     * the uncertainty of each count in the spec count ... cannot be arbitrarily set since it
     * depends on the count
     */
    protected double[] specUncertainty;

    {
      maxCountPosition = new int[] {-1};
    }

    // Default Constructor
    public OneDim() {
      this(512);
    }

    // Constructor ... needs a specShape variable
    public OneDim(int aSpecShape) {
      super();
      specShape = aSpecShape;
      specCount = new int[specShape];
      specUncertainty = new double[specShape];
      maxCountPosition[0] = -1;
    }

    // Copy Constructor
    public OneDim(OneDim aOneDim) {
      super();
      specShape = aOneDim.specShape;
      specCount = new int[specShape];
      specUncertainty = new double[specShape];

      // copy the arrays locally ... instead of sharing the same array
      System.arraycopy(aOneDim.specCount, 0, specCount, 0, specShape);
      System.arraycopy(aOneDim.specUncertainty, 0, specUncertainty, 0, specShape);
      maxCount = aOneDim.getMaxCount();
      maxCountPosition = aOneDim.getMaxCountPosition();
    }

    /**
     * for spectrum read in from a file or sth ... where the error is a simple function of the
     * number of counts it shares the same array as the passes specCount array *** NOT A COPY ***
     * and then creates the uncertainty array as a square root of the count array
     *
     * @exception SpectrumException thrown if the specShape and specCount are not equal in length
     */
    public OneDim(int specShape, int[] aSpecCount) throws SpectrumException {
      super();
      if (specShape != aSpecCount.length)
        throw new SpectrumException(
            "The spec shape is not equal to the number of elements" + " in the passed array");

      this.specShape = specShape;
      specCount = aSpecCount;
      specUncertainty = new double[specShape];

      maxCount = Integer.MIN_VALUE;
      // sets the spec count to equal this array and resets the uncertainties
      for (int i = 0; i < aSpecCount.length; i++) {
        maxCount = Math.max(maxCount, specCount[i]);
        specUncertainty[i] = Math.sqrt((double) specCount[i]);
      }

      maxCountPosition[0] = this.search(specCount, maxCount);
      if (maxCountPosition[0] < 0) maxCountPosition[0] = -1;
    }

    /**
     * it shares the same array as the passes specCount array and also with the uncertainty array
     * passes *** NOT A COPY ***
     *
     * @exception SpectrumException thrown if the specShape and specCount and uncertainty are not
     *     equal in length
     */
    public OneDim(int aSpecShape, int[] aSpecCount, double[] aSpecUncertainty)
        throws SpectrumException {
      super();
      try {
        setSpecChannelAttributes(aSpecShape, aSpecCount, aSpecUncertainty);
      } catch (SpectrumException se) {
        throw (se);
      }
    }

    /*
     * THINK ABOUT THIS ...
     * No reason to arbitrarily set the spec counts for a spectrum ...
     * No reason to arbitrarily set the spec uncertainty for a spectrum ...
     */

    /**
     * sets the spec channel attributes to equal these arguments Preferred Way to set the spec
     * channel attributes THE ARRAYS ARE NOT COPIED LOCALLY
     */
    public void setSpecChannelAttributes(
        int aSpecShape, int[] aSpecCount, double[] aSpecUncertainty) throws SpectrumException {
      if ((aSpecCount.length != aSpecShape) || (aSpecUncertainty.length != aSpecShape))
        throw new SpectrumException("The number of elements in array must equal the specShape");

      specShape = aSpecShape;
      specCount = aSpecCount;
      specUncertainty = aSpecUncertainty;

      maxCount = MathPlus.getMaxValue(specCount);
      maxCountPosition[0] = this.search(specCount, maxCount);
      if (maxCountPosition[0] < 0) maxCountPosition[0] = -1;
    }

    /** sets the spec count to equal this array as much as possible ... a copy is made */
    protected void setSpecCount(int[] aSpecCount) {
      // actual number of elements to copy from the array
      int length = Math.min(specShape, aSpecCount.length);
      System.arraycopy(aSpecCount, 0, specCount, 0, length);
      maxCount = MathPlus.getMaxValue(specCount);
      maxCountPosition[0] = this.search(specCount, maxCount);
    }

    /** sets the spec Uncertainty to equal this array as much as possible ... a copy is made */
    protected void setSpecUncertainty(int[] aSpecUncertainty) {
      // actual number of elements to copy from the array
      int length = Math.min(specShape, aSpecUncertainty.length);
      System.arraycopy(aSpecUncertainty, 0, specUncertainty, 0, length);
    }

    public int[] getSpecCount() {
      return specCount;
    }

    public double[] getSpecUncertainty() {
      return specUncertainty;
    }

    public int getSpecShape() {
      return specShape;
    }

    public boolean isSameShape(SpecChannel a, SpecChannel b) {
      SpecChannel.OneDim a2;
      SpecChannel.OneDim b2;

      if (a instanceof SpecChannel.OneDim && b instanceof SpecChannel.OneDim) {
        a2 = (SpecChannel.OneDim) a;
        b2 = (SpecChannel.OneDim) b;
      } else return false;

      int shape1 = a2.getSpecShape();
      int shape2 = b2.getSpecShape();

      if (shape1 == shape2) return true;
      else return false;
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(specShape + " channels");
      sb.append(
          "\nMaximum channel count: " + maxCount + "\n\tat channel (" + maxCountPosition[0] + ")");

      return sb.toString();
    }
  }

  public static class TwoDim extends SpecChannel {

    /*
    the counts for the TwoDSpectrum in SPCLIB format are stored as {y,x} values ...
    i.e looping through every y value, ...
    all the x value subscripts in the rectangular array are stored ...
    */

    /** the width and height of the rectangular array representing the specChannel */
    private int specShape0;

    private int specShape1;
    /** array containing the counts of the spectrum channels */
    protected int[][] specCount;
    /**
     * the uncertainty of each count in the spec count ... cannot be arbitrarily set since it
     * depends on the count
     */
    protected double[][] specUncertainty;

    {
      maxCountPosition = new int[] {-1, -1};
    }

    // Default Constructor
    public TwoDim() {
      this(512, 512);
    }

    // Constructor ... needs values for the spec shape
    public TwoDim(int specShape0, int specShape1) {
      super();
      this.specShape0 = specShape0;
      this.specShape1 = specShape1;

      specCount = new int[specShape0][specShape1];
      specUncertainty = new double[specShape0][specShape1];
      maxCountPosition[0] = maxCountPosition[1] = -1;
    }

    /**
     * for spectrum read in from a file or sth ... where the error is a simple function of the
     * number of counts it shares the same array as the passes specCount array *** NOT A COPY ***
     * and then creates the uncertainty array as a square root of the count array
     *
     * @exception SpectrumException thrown if the specShapes and specCount are not equal in length
     */
    public TwoDim(int specShape0, int specShape1, int[][] aSpecCount) throws SpectrumException {
      this(specShape0, specShape1);

      int subLength;

      try {
        subLength = Misc.getRectangularArrayHeight(aSpecCount);
        if ((specShape0 != aSpecCount.length) || (specShape1 != subLength))
          throw new SpectrumException(
              "passed dimensions of rectangular array are not" + "equivalent to actual dimensions");
      } catch (Misc.NotRectangularArrayException nrae) {
        throw new SpectrumException("passed array is not rectangular");
      }

      specCount = aSpecCount;

      maxCount = Integer.MIN_VALUE;
      // loop through, since java implements these as arrays of arrays
      for (int i = 0; i < specShape0; i++) {
        for (int j = 0; j < specShape1; j++) {
          specUncertainty[i][j] = Math.sqrt(specCount[i][j]);
        }
        maxCount = Math.max(maxCount, MathPlus.getMaxValue(specCount[i]));
      }
      setMaxCountPosition();
    }

    /**
     * it shares the same array as the passes specCount array and also with the uncertainty array
     * passes *** NOT A COPY ***
     *
     * @exception SpectrumException thrown if the specShapes and specCount and uncertainty are not
     *     equal in length
     */
    public TwoDim(int specShape0, int specShape1, int[][] aSpecCount, double[][] aSpecUncertainty)
        throws SpectrumException {
      this(specShape0, specShape1);

      try {
        setSpecChannelAttributes(specShape0, specShape1, aSpecCount, aSpecUncertainty);
      } catch (Exception e) {
        throw new SpectrumException(e.getMessage(), e);
      }
    }

    // Copy Constructor
    public TwoDim(TwoDim aTwoDim) {
      super();
      this.specShape0 = aTwoDim.specShape0;
      this.specShape1 = aTwoDim.specShape1;

      specCount = new int[specShape0][specShape1];
      specUncertainty = new double[specShape0][specShape1];

      // loop through, since java implements these as arrays of arrays
      for (int i = 0; i < specShape0; i++) {
        System.arraycopy(aTwoDim.specCount[i], 0, specCount[i], 0, specShape1);
      }

      maxCount = aTwoDim.getMaxCount();
      maxCountPosition = aTwoDim.getMaxCountPosition();
    }

    /**
     * sets the spec channel attributes to equal these arguments Preferred Way to set the spec
     * channel attributes THE ARRAYS ARE NOT COPIED LOCALLY
     */
    public void setSpecChannelAttributes(
        int aSpecShape0, int aSpecShape1, int[][] aSpecCount, double[][] aSpecUncertainty)
        throws Misc.NotRectangularArrayException, Exception {
      try {
        int countArrayHeight = Misc.getRectangularArrayHeight(aSpecCount);
        int uncertaintyArrayHeight = Misc.getRectangularArrayHeight(aSpecUncertainty);

        if (!((aSpecCount.length == aSpecShape0)
            && (aSpecUncertainty.length == aSpecShape0)
            && (countArrayHeight == aSpecShape1)
            && (uncertaintyArrayHeight == aSpecShape1)))
          throw new Exception("The number of elements in array must equal the specShape");

        specShape0 = aSpecShape0;
        specShape1 = aSpecShape1;
        specCount = aSpecCount;
        specUncertainty = aSpecUncertainty;

        // set the maximum channel count
        maxCount = 0;
        for (int i = 0; i < specCount.length; i++) {
          maxCount = Math.max(maxCount, MathPlus.getMaxValue(specCount[i]));
        }

        setMaxCountPosition();
      } catch (Exception e) {
        System.out.println(e.getMessage());
        // rethrow the exception ... in case it is a NotRectangularArrayException
        throw (e);
      }
    }

    /** sets the spec count to equal this array as much as possible ... a copy is made */
    protected void setSpecCount(int[][] aSpecCount) {
      // actual number of elements to copy from the array
      int length0 = Math.min(specShape0, aSpecCount.length);
      int length1;

      maxCount = 0;
      // loop through, since java implements these as arrays of arrays
      for (int i = 0; i < length0; i++) {
        length1 = Math.min(specShape1, aSpecCount[i].length);
        System.arraycopy(aSpecCount[i], 0, specCount[i], 0, length1);
        maxCount = Math.max(maxCount, MathPlus.getMaxValue(specCount[i]));
      }
      setMaxCountPosition();
    }

    /** sets the spec Uncertainty to equal this array as much as possible ... a copy is made */
    protected void setSpecUncertainty(int[][] aSpecUncertainty) {
      // actual number of elements to copy from the array
      int length0 = Math.min(specShape0, aSpecUncertainty.length);
      int length1;

      // loop through, since java implements these as arrays of arrays
      for (int i = 0; i < length0; i++) {
        length1 = Math.min(specShape1, aSpecUncertainty[i].length);
        System.arraycopy(aSpecUncertainty[i], 0, specUncertainty[i], 0, length1);
      }
    }

    private void setMaxCountPosition() {
      int a = -1;
      int b = -1;

      for (int i = 0; i < specCount.length; i++) {
        a = this.search(specCount[i], maxCount);
        if (a >= 0) {
          b = i;
          break;
        }
      }

      if (a < 0) maxCountPosition[0] = -1;
      else maxCountPosition[0] = a;
      if (b < 0) maxCountPosition[1] = -1;
      else maxCountPosition[1] = b;
    }

    public int[][] getSpecCount() {
      return specCount;
    }

    public double[][] getSpecUncertainty() {
      return specUncertainty;
    }

    public int getSpecShape0() {
      return specShape0;
    }

    public int getSpecShape1() {
      return specShape1;
    }

    public boolean isSameShape(SpecChannel a, SpecChannel b) {
      SpecChannel.TwoDim a2;
      SpecChannel.TwoDim b2;

      if (a instanceof SpecChannel.TwoDim && b instanceof SpecChannel.TwoDim) {
        a2 = (SpecChannel.TwoDim) a;
        b2 = (SpecChannel.TwoDim) b;
      } else return false;

      int shape10 = a2.getSpecShape0();
      int shape11 = a2.getSpecShape1();
      int shape20 = b2.getSpecShape0();
      int shape21 = b2.getSpecShape1();

      if ((shape10 == shape20) && (shape11 == shape21)) return true;
      else return false;
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(specShape0 + " X " + specShape1 + " channels");
      sb.append(
          "\nMaximum channel count: "
              + maxCount
              + "\n\tat channel ("
              + maxCountPosition[0]
              + ", "
              + maxCountPosition[1]
              + ")");

      return sb.toString();
    }
  }

  /**
   * convenience exception class that two spectra have different shapes Two spectra have the same
   * shape if they have the same dimension and have the same specShapes ... a SpectrumShapeException
   * is thrown when, for example, a certain spectrum shape is expected ... or 2 spectra are checked
   * for the same shape (and are not)
   */
  public static class ShapeException extends Exception {
    public ShapeException(String msg) {
      super(msg);
    }
  }
}
