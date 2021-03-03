package spec.spectrum;

import java.util.Date;

/*
 * Defines static methods for manipulating spectrum
 * Thus, it needs to hava no Spectrum data member
 * ... NOT REALLY NECESSARY ... IT ONLY ADDS ANOTHER LEVEL OF ABSTRACTION
 * ... WITH NO BENEFITS ... JUST ADD TO SPECTRUM CLASS
 */
public abstract class ManipulateSpectrum {
  /** spectrum that manipulations are performed on */
  protected Spectrum spectrum;

  /** multiply the count for each channel by a double value */
  public abstract void multiply(double value);

  /** add an int value to the count for each channel */
  public abstract void add(int value);

  /** Static class managing One Dimensional specific manipulations */
  public static class OneDim extends ManipulateSpectrum {
    SpecChannel.OneDim specChannel;
    int specShape;
    int[] specCount;
    double[] specUncertainty;

    public OneDim(Spectrum.OneDim spectrum) {
      super();

      this.spectrum = spectrum;
      setChannelVariables();
    }

    /** convenience method to set or reset the channel variables */
    private void setChannelVariables() {
      specChannel = ((Spectrum.OneDim) spectrum).getSpecChannel();
      specShape = specChannel.getSpecShape();
      specCount = specChannel.getSpecCount();
      specUncertainty = specChannel.getSpecUncertainty();
    }

    /*
     *************************************************************************
     * STATIC CLASS METHODS FOR MANIPULATIONS BETWEEN TWO ONE DIMENSIONAL SPECTRA
     *************************************************************************
     */
    /** add the first spectrum to the second spectrum channel by channel */
    public static Spectrum.OneDim add(Spectrum.OneDim spectrum1, Spectrum.OneDim spectrum2)
        throws SpecChannel.ShapeException, Exception {
      SpecChannel.OneDim specChannel;

      SpecChannel.OneDim specChannel1 = spectrum1.getSpecChannel();
      int specShape1 = specChannel1.getSpecShape();

      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      // throw exception if not the same shape
      if (specShape1 != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount1 = specChannel1.getSpecCount();
      double[] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = new int[specShape1];
      double[] specUncertainty = new double[specShape1];

      for (int i = 0; i < specShape1; i++) {
        specUncertainty[i] =
            Math.sqrt(
                (specUncertainty1[i] * specUncertainty1[i])
                    + (specUncertainty2[i] * specUncertainty2[i]));
        specCount[i] = specCount1[i] + specCount2[i];
      }

      String specName = spectrum1.getSpecName() + "_+_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.OneDim(specShape1, specCount, specUncertainty);

      return new Spectrum.OneDim(specName, new Date(), specChannel);
    }

    /** subtract the first spectrum from the second spectrum channel by channel */
    public static Spectrum.OneDim subtract(Spectrum.OneDim spectrum1, Spectrum.OneDim spectrum2)
        throws SpecChannel.ShapeException, Exception {
      SpecChannel.OneDim specChannel;

      SpecChannel.OneDim specChannel1 = spectrum1.getSpecChannel();
      int specShape1 = specChannel1.getSpecShape();

      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      // throw exception if not the same shape
      if (specShape1 != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount1 = specChannel1.getSpecCount();
      double[] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = new int[specShape1];
      double[] specUncertainty = new double[specShape1];

      for (int i = 0; i < specShape1; i++) {
        specUncertainty[i] =
            Math.sqrt(
                (specUncertainty1[i] * specUncertainty1[i])
                    + (specUncertainty2[i] * specUncertainty2[i]));
        specCount[i] = specCount1[i] - specCount2[i];
      }

      String specName = spectrum1.getSpecName() + "_-_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.OneDim(specShape1, specCount, specUncertainty);

      return new Spectrum.OneDim(specName, new Date(), specChannel);
    }

    /** multiply the first spectrum by the second spectrum channel by channel */
    public static Spectrum.OneDim multiply(Spectrum.OneDim spectrum1, Spectrum.OneDim spectrum2)
        throws SpecChannel.ShapeException, Exception {
      SpecChannel.OneDim specChannel;

      SpecChannel.OneDim specChannel1 = spectrum1.getSpecChannel();
      int specShape1 = specChannel1.getSpecShape();

      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      // throw exception if not the same shape
      if (specShape1 != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount1 = specChannel1.getSpecCount();
      double[] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = new int[specShape1];
      double[] specUncertainty = new double[specShape1];

      for (int i = 0; i < specShape1; i++) {
        specUncertainty[i] =
            Math.sqrt(
                Math.pow(specCount2[i] * specUncertainty1[i], 2)
                    + Math.pow(specCount1[i] * specUncertainty2[i], 2));
        specCount[i] = specCount1[i] * specCount2[i];
      }

      String specName = spectrum1.getSpecName() + "_*_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.OneDim(specShape1, specCount, specUncertainty);

      return new Spectrum.OneDim(specName, new Date(), specChannel);
    }

    /** divide the first spectrum by the second spectrum channel by channel */
    public static Spectrum.OneDim divide(Spectrum.OneDim spectrum1, Spectrum.OneDim spectrum2)
        throws SpecChannel.ShapeException, Exception {
      SpecChannel.OneDim specChannel;

      SpecChannel.OneDim specChannel1 = spectrum1.getSpecChannel();
      int specShape1 = specChannel1.getSpecShape();

      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      // throw exception if not the same shape
      if (specShape1 != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount1 = specChannel1.getSpecCount();
      double[] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = new int[specShape1];
      double[] specUncertainty = new double[specShape1];

      for (int i = 0; i < specShape1; i++) {
        specUncertainty[i] =
            (specCount1[i] / specCount2[i])
                * Math.sqrt(
                    (specUncertainty1[i] * specUncertainty1[i]) / (specCount1[i] * specCount1[i])
                        + (specUncertainty2[i] * specUncertainty2[i])
                            / (specCount2[i] * specCount2[i]));
        specCount[i] = specCount[i] / specCount2[i];
      }

      String specName = spectrum1.getSpecName() + "_/_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.OneDim(specShape1, specCount, specUncertainty);

      return new Spectrum.OneDim(specName, new Date(), specChannel);
    }

    /** add this spectrum to the parsed spectrum channel by channel */
    public void add(Spectrum.OneDim spectrum2) throws SpecChannel.ShapeException, Exception {
      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();
      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      // throw exception if not the same shape
      if (specShape != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      for (int i = 0; i < specShape; i++) {
        specUncertainty[i] =
            Math.sqrt(
                (specUncertainty[i] * specUncertainty[i])
                    + (specUncertainty2[i] * specUncertainty2[i]));
        specCount[i] += specCount2[i];
      }
    }

    /** subtract this spectrum to the parsed spectrum channel by channel */
    public void subtract(Spectrum.OneDim spectrum2) throws SpecChannel.ShapeException, Exception {
      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();
      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      // throw exception if not the same shape
      if (specShape != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      for (int i = 0; i < specShape; i++) {
        specUncertainty[i] =
            Math.sqrt(
                (specUncertainty[i] * specUncertainty[i])
                    + (specUncertainty2[i] * specUncertainty2[i]));
        specCount[i] -= specCount2[i];
      }
    }

    /** multiply this spectrum by the parsed spectrum channel by channel */
    public void multiply(Spectrum.OneDim spectrum2) throws SpecChannel.ShapeException, Exception {
      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();
      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      // throw exception if not the same shape
      if (specShape != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      for (int i = 0; i < specShape; i++) {
        specUncertainty[i] =
            Math.sqrt(
                Math.pow(specCount2[i] * specUncertainty[i], 2)
                    + Math.pow(specCount[i] * specUncertainty2[i], 2));
        specCount[i] *= specCount2[i];
      }
    }

    /** divide this spectrum by the parsed spectrum channel by channel */
    public void divide(Spectrum.OneDim spectrum2) throws SpecChannel.ShapeException, Exception {
      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();
      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      // throw exception if not the same shape
      if (specShape != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      for (int i = 0; i < specShape; i++) {
        specUncertainty[i] =
            (specCount[i] / specCount2[i])
                * Math.sqrt(
                    (specUncertainty[i] * specUncertainty[i]) / (specCount[i] * specCount[i])
                        + (specUncertainty2[i] * specUncertainty2[i])
                            / (specCount2[i] * specCount2[i]));
        specCount[i] /= specCount2[i];
      }
    }

    /**
     * compress a spectrum by a power of two ... e.g compressing a 256 channel by exponent of 3 ...
     * means making it to 256/(2*2*2) = 32 channels
     */
    public void compress(int exponent) throws Exception {
      // maximum allowed compression exponent
      int maxExponent = (int) Math.round(Math.log((double) specShape) / Math.log(2.0));

      if (exponent >= maxExponent)
        throw new Exception(
            "Invalid exponent ... this reduces the number" + "of channels to a negative number");

      // get the new specShapes
      int numInGroup = (int) Math.pow(2.0, (double) exponent);
      int newSpecShape = specShape / numInGroup;
      // calculate the new spec counts and uncertainties
      int[] newSpecCount = new int[newSpecShape];
      double[] newSpecUncertainty = new double[newSpecShape];
      // holds the count and uncertainty sum before the cound is set
      int countSum = 0;
      double uncertaintySum = 0.0;

      for (int i = 0; i < newSpecShape; i++) {
        countSum = 0;
        uncertaintySum = 0.0;
        for (int j = numInGroup * i + 1; j <= numInGroup * i + numInGroup; j++) {
          countSum = countSum + specCount[i];
          uncertaintySum = uncertaintySum + (specUncertainty[i] * specUncertainty[i]);
        }
        // newSpecCount is the total area (sum of counts) over that region
        newSpecCount[i] = countSum;
        newSpecUncertainty[i] = Math.sqrt(uncertaintySum);
      }

      try {
        specChannel.setSpecChannelAttributes(newSpecShape, newSpecCount, newSpecUncertainty);
        setChannelVariables();
      } catch (Exception e) {
        System.out.println("Error in compressing the spectrum\n" + e.getMessage());
      }
    }

    /** multiply the count for each channel by a double value */
    public void multiply(double value) {
      for (int i = 0; i < specShape; i++) {
        specCount[i] = (int) (specCount[i] * value);
        specUncertainty[i] = specUncertainty[i] * value;
      }
    }

    /** add an int value to the count for each channel */
    public void add(int value) {
      for (int i = 0; i < specShape; i++) {
        specCount[i] += value;
        // uncertainty does not change
      }
    }

    /**
     * find the area between an interval of channels (inclusive) ... basically add the channel
     * counts in the range Convention is that channels are labelled from 1 to specShape
     */
    public int area(int lowChannel, int highChannel) throws Exception {
      if ((lowChannel >= highChannel) || (highChannel > specShape))
        throw new Exception("Invalid range of values");

      int area = 0;
      for (int i = lowChannel - 1; i < highChannel; i++) {
        area += specCount[i];
      }

      return area;
    }

    /**
     * find the average channel count between an interval of channels ... basically divide the area
     * by the range
     */
    public int average(int lowChannel, int highChannel) throws Exception {
      int area = area(lowChannel, highChannel);
      int average = area / (highChannel - lowChannel + 1);

      return average;
    }
  }

  /** Static class managing Two Dimensional specific manipulations */
  public static class TwoDim extends ManipulateSpectrum {
    // initialize these values from the spectrum since they are used so many times
    SpecChannel.TwoDim specChannel;
    int specShape0;
    int specShape1;
    int[][] specCount;
    double[][] specUncertainty;

    public TwoDim(Spectrum.TwoDim spectrum) {
      super();

      this.spectrum = spectrum;
      setChannelVariables();
    }

    // private method to set the channel variables
    private void setChannelVariables() {
      specChannel = ((Spectrum.TwoDim) spectrum).getSpecChannel();
      specShape0 = specChannel.getSpecShape0();
      specShape1 = specChannel.getSpecShape1();
      specCount = specChannel.getSpecCount();
      specUncertainty = specChannel.getSpecUncertainty();
    }

    /*
     *************************************************************************
     * STATIC CLASS METHODS FOR MANIPULATIONS BETWEEN TWO ONE DIMENSIONAL SPECTRA
     *************************************************************************
     */
    /** add the first spectrum to the second spectrum channel by channel */
    public static Spectrum.TwoDim add(Spectrum.TwoDim spectrum1, Spectrum.TwoDim spectrum2)
        throws SpecChannel.ShapeException, Exception {
      SpecChannel.TwoDim specChannel1 = spectrum1.getSpecChannel();
      int specShape01 = specChannel1.getSpecShape0();
      int specShape11 = specChannel1.getSpecShape1();

      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape01 == specShape02) && (specShape11 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount1 = specChannel1.getSpecCount();
      double[][] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      SpecChannel.TwoDim specChannel;
      int[][] specCount = new int[specShape01][specShape11];
      double[][] specUncertainty = new double[specShape01][specShape11];

      for (int i = 0; i < specShape01; i++) {
        for (int j = 0; j < specShape11; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  (specUncertainty1[i][j] * specUncertainty1[i][j])
                      + (specUncertainty2[i][j] * specUncertainty2[i][j]));
          specCount[i][j] = specCount1[i][j] + specCount2[i][j];
        }
      }

      String specName = spectrum1.getSpecName() + "_+_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.TwoDim(specShape01, specShape11, specCount, specUncertainty);

      return new Spectrum.TwoDim(specName, new Date(), specChannel);
    }

    /** subtract the first spectrum from the second spectrum channel by channel */
    public static Spectrum.TwoDim subtract(Spectrum.TwoDim spectrum1, Spectrum.TwoDim spectrum2)
        throws SpecChannel.ShapeException, Exception {
      SpecChannel.TwoDim specChannel1 = spectrum1.getSpecChannel();
      int specShape01 = specChannel1.getSpecShape0();
      int specShape11 = specChannel1.getSpecShape1();

      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape01 == specShape02) && (specShape11 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount1 = specChannel1.getSpecCount();
      double[][] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      SpecChannel.TwoDim specChannel;
      int[][] specCount = new int[specShape01][specShape11];
      double[][] specUncertainty = new double[specShape01][specShape11];

      for (int i = 0; i < specShape01; i++) {
        for (int j = 0; j < specShape11; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  (specUncertainty1[i][j] * specUncertainty1[i][j])
                      + (specUncertainty2[i][j] * specUncertainty2[i][j]));
          specCount[i][j] = specCount1[i][j] - specCount2[i][j];
        }
      }

      String specName = spectrum1.getSpecName() + "_-_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.TwoDim(specShape01, specShape11, specCount, specUncertainty);

      return new Spectrum.TwoDim(specName, new Date(), specChannel);
    }

    /** multiply the first spectrum by the second spectrum channel by channel */
    public static Spectrum.TwoDim multiply(Spectrum.TwoDim spectrum1, Spectrum.TwoDim spectrum2)
        throws SpecChannel.ShapeException, Exception {
      SpecChannel.TwoDim specChannel1 = spectrum1.getSpecChannel();
      int specShape01 = specChannel1.getSpecShape0();
      int specShape11 = specChannel1.getSpecShape1();

      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape01 == specShape02) && (specShape11 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount1 = specChannel1.getSpecCount();
      double[][] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      SpecChannel.TwoDim specChannel;
      int[][] specCount = new int[specShape01][specShape11];
      double[][] specUncertainty = new double[specShape01][specShape11];

      for (int i = 0; i < specShape01; i++) {
        for (int j = 0; j < specShape11; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  Math.pow(specCount2[i][j] * specUncertainty1[i][j], 2)
                      + Math.pow(specCount1[i][j] * specUncertainty2[i][j], 2));
          specCount[i][j] = specCount1[i][j] * specCount2[i][j];
        }
      }

      String specName = spectrum1.getSpecName() + "_*_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.TwoDim(specShape01, specShape11, specCount, specUncertainty);

      return new Spectrum.TwoDim(specName, new Date(), specChannel);
    }

    /** divide the first spectrum by the second spectrum channel by channel */
    public static Spectrum.TwoDim divide(Spectrum.TwoDim spectrum1, Spectrum.TwoDim spectrum2)
        throws SpecChannel.ShapeException, Exception {
      SpecChannel.TwoDim specChannel1 = spectrum1.getSpecChannel();
      int specShape01 = specChannel1.getSpecShape0();
      int specShape11 = specChannel1.getSpecShape1();

      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape01 == specShape02) && (specShape11 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount1 = specChannel1.getSpecCount();
      double[][] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      SpecChannel.TwoDim specChannel;
      int[][] specCount = new int[specShape01][specShape11];
      double[][] specUncertainty = new double[specShape01][specShape11];

      for (int i = 0; i < specShape01; i++) {
        for (int j = 0; j < specShape11; j++) {
          specUncertainty[i][j] =
              (specCount1[i][j] / specCount2[i][j])
                  * Math.sqrt(
                      (specUncertainty1[i][j] * specUncertainty1[i][j])
                              / (specCount1[i][j] * specCount1[i][j])
                          + (specUncertainty2[i][j] * specUncertainty2[i][j])
                              / (specCount2[i][j] * specCount2[i][j]));
          specCount[i][j] = specCount1[i][j] + specCount2[i][j];
        }
      }

      String specName = spectrum1.getSpecName() + "_/_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.TwoDim(specShape01, specShape11, specCount, specUncertainty);

      return new Spectrum.TwoDim(specName, new Date(), specChannel);
    }

    /** add this spectrum to the parsed spectrum channel by channel */
    public void add(Spectrum.TwoDim spectrum2) throws SpecChannel.ShapeException, Exception {
      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();
      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      // throw exception if not the same shape
      if (!((specShape0 == specShape02) && (specShape1 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      for (int i = 0; i < specShape02; i++) {
        for (int j = 0; j < specShape12; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  (specUncertainty[i][j] * specUncertainty[i][j])
                      + (specUncertainty2[i][j] * specUncertainty2[i][j]));
          specCount[i][j] += specCount2[i][j];
        }
      }
    }

    /** subtract this spectrum to the parsed spectrum channel by channel */
    public void subtract(Spectrum.TwoDim spectrum2) throws SpecChannel.ShapeException, Exception {
      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();
      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      // throw exception if not the same shape
      if (!((specShape0 == specShape02) && (specShape1 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      for (int i = 0; i < specShape02; i++) {
        for (int j = 0; j < specShape12; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  (specUncertainty[i][j] * specUncertainty[i][j])
                      + (specUncertainty2[i][j] * specUncertainty2[i][j]));
          specCount[i][j] -= specCount2[i][j];
        }
      }
    }

    /** multiply this spectrum by the parsed spectrum channel by channel */
    public void multiply(Spectrum.TwoDim spectrum2) throws SpecChannel.ShapeException, Exception {
      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();
      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      // throw exception if not the same shape
      if (!((specShape0 == specShape02) && (specShape1 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      for (int i = 0; i < specShape02; i++) {
        for (int j = 0; j < specShape12; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  Math.pow(specCount2[i][j] * specUncertainty[i][j], 2)
                      + Math.pow(specCount[i][j] * specUncertainty2[i][j], 2));
          specCount[i][j] *= specCount2[i][j];
        }
      }
    }

    /** divide this spectrum by the parsed spectrum channel by channel */
    public void divide(Spectrum.TwoDim spectrum2) throws SpecChannel.ShapeException, Exception {
      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();
      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      // throw exception if not the same shape
      if (!((specShape0 == specShape02) && (specShape1 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      for (int i = 0; i < specShape02; i++) {
        for (int j = 0; j < specShape12; j++) {
          specUncertainty[i][j] =
              (specCount[i][j] / specCount2[i][j])
                  * Math.sqrt(
                      (specUncertainty[i][j] * specUncertainty[i][j])
                              / (specCount[i][j] * specCount[i][j])
                          + (specUncertainty2[i][j] * specUncertainty2[i][j])
                              / (specCount2[i][j] * specCount2[i][j]));
          specCount[i][j] /= specCount2[i][j];
        }
      }
    }

    /**
     * compress a spectrum by a power of two on the y and x scale ... e.g compressing a 256 channel
     * by exponent of 3 ... means making it to 256/(2*2*2) = 32 channels
     */
    public void compress(int yExponent, int xExponent) throws Exception {
      // maximum allowed compression exponent
      int maxYExponent = (int) Math.round(Math.log((double) specShape0) / Math.log(2.0));
      int maxXExponent = (int) Math.round(Math.log((double) specShape1) / Math.log(2.0));

      if ((yExponent >= maxYExponent) || (xExponent >= maxXExponent))
        throw new Exception(
            "Invalid exponent ... this reduces the number" + "of channels to a negative number");

      // get the number in Y & X direction that will be grouped together
      int numYInGroup = (int) Math.pow(2.0, (double) yExponent);
      int numXInGroup = (int) Math.pow(2.0, (double) xExponent);

      // get the new specShapes
      int newSpecShape0 = specShape0 / numYInGroup;
      int newSpecShape1 = specShape1 / numXInGroup;
      // calculate the new spec counts and uncertainties
      int[][] newSpecCount = new int[newSpecShape0][newSpecShape1];
      double[][] newSpecUncertainty = new double[newSpecShape0][newSpecShape1];
      // holds the count and uncertainty sum before the cound is set
      int countSum = 0;
      double uncertaintySum = 0.0;

      // loop through to set the newSpecCount for each channel in the new SpecChannel
      for (int i = 0; i < newSpecShape0; i++) {
        for (int j = 0; j < newSpecShape1; j++) {
          // get the average over the rectangular region that is made into one channel
          countSum = 0;
          for (int a = i * numYInGroup; a < (i + 1) * numYInGroup; a++) {
            for (int b = j * numXInGroup; b < (j + 1) * numXInGroup; b++) {
              countSum = countSum + specCount[a][b];
              uncertaintySum = uncertaintySum + (specUncertainty[a][b] * specUncertainty[a][b]);
            }
          }
          // newSpecCount is the total area (sum of counts) over that rectangular region
          newSpecCount[i][j] = countSum;
          newSpecUncertainty[i][j] = Math.sqrt(uncertaintySum);
        }
      }

      try {
        specChannel.setSpecChannelAttributes(
            newSpecShape0, newSpecShape1, newSpecCount, newSpecUncertainty);
        setChannelVariables();
      } catch (Exception e) {
        System.out.println("Error in compressing the spectrum\n" + e.getMessage());
      }
    }

    /** multiply the count for each channel by a double value */
    public void multiply(double value) {
      for (int i = 0; i < specShape0; i++) {
        for (int j = 0; j < specShape1; j++) {
          specCount[i][j] = (int) (specCount[i][j] * value);
          specUncertainty[i][j] = specUncertainty[i][j] * value;
        }
      }
    }

    /** add an int value to the count for each channel */
    public void add(int value) {
      for (int i = 0; i < specShape0; i++) {
        for (int j = 0; j < specShape1; j++) {
          specCount[i][j] += value;
        }
      }
    }
  }
}
