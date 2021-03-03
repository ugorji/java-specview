package spec.io;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Hashtable;
import javax.swing.JList;
import javax.swing.JOptionPane;
import spec.main.SpecConstants;
import spec.spectrum.Spectrum;

public abstract class SpecFormat {
  public boolean supported = false;

  public String toString() {
    return "A SPECTRUM FORMAT";
  }

  public boolean isSupported() {
    return supported;
  }

  /** factory methods for creating a specFormat */
  public static SpecFormat getInstance(String specFormatString) {
    if (specFormatString.equals(SpecConstants.ORIGINAL_SPC_FORMAT_EXTENSION))
      // for trtaditional spc file format
      return (new SpecFormat_spc());
    else if (specFormatString.equals(SpecConstants.EXTENDED_SPC_FORMAT_EXTENSION))
      // for extended spc files
      return (new SpecFormat_spc2());
    else if (specFormatString.equals(SpecConstants.RAW_DATA_SPC_FORMAT_EXTENSION))
      // for raw data spectrum files
      return (new SpecFormat_data());
    else return null;
  }

  /** Tries to get the appropriate format ... or null if it couln't get one */
  public static SpecFormatAndDim getSuitableFormat(File file) {
    String supportedExtension = null;
    int tempSpecDimension = -1;
    SpecFormat[] formats = new SpecFormat[SpecConstants.SPEC_FORMAT.length];

    for (int i = 0; i < formats.length; i++) {
      formats[i] = getInstance(SpecConstants.SPEC_FORMAT[i]);
    }

    for (int i = 0; i < formats.length; i++) {
      if (file.getName().endsWith(SpecConstants.SPEC_FORMAT[i])) {
        try {
          supportedExtension = SpecConstants.SPEC_FORMAT[i];
          tempSpecDimension = formats[i].formatAndDimension(file);
          return new SpecFormatAndDim(formats[i], tempSpecDimension);
        } catch (Exception e) {
          System.out.println(e.getMessage());
          e.printStackTrace();
        }
      }
      break;
    }

    for (int i = 0; i < formats.length; i++) {
      if (supportedExtension != null && supportedExtension == SpecConstants.SPEC_FORMAT[i])
        continue;

      try {
        tempSpecDimension = formats[i].formatAndDimension(file);
        return new SpecFormatAndDim(formats[i], tempSpecDimension);
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }

    return null;
  }

  /**
   * Tries to get the appropriate format ... or null if it couln't get one.
   *
   * <p>Uses the reflection API to call checkFormatAndDimension (File) on all the SpecFormat classes
   *
   * <p>Made private ... only stays here for informational purposes
   */
  private static SpecFormatAndDim getSuitableFormatUsingReflection(File file) {
    int tempSpecDimension = -1;
    SpecFormat specFormat = null;
    String supportedExtension = null;
    Class theClass = null;
    Method chkMethod = null;
    Integer dimI = null;

    Class[] param = new Class[] {File.class};
    Object[] arg = new Object[] {file};

    for (int i = 0; i < SpecConstants.SPEC_FORMAT.length; i++) {
      if (file.getName().endsWith(SpecConstants.SPEC_FORMAT[i])) {
        try {
          supportedExtension = SpecConstants.SPEC_FORMAT[i];
          theClass = Class.forName(SpecConstants.SPEC_CLASS[i]);
          chkMethod = theClass.getMethod("checkFormatAndDimension", param);
          dimI = (Integer) chkMethod.invoke(theClass, arg);
          tempSpecDimension = dimI.intValue();
          // tempSpecDimension = theClass.checkFormatAndDimension (file);
          specFormat = getInstance(supportedExtension);
          return new SpecFormatAndDim(specFormat, tempSpecDimension);
        } catch (Exception e) {
          System.out.println(e.getMessage());
          e.printStackTrace();
        }
        // catch (SpecFormat.IOException sioe) {}
        // catch (ClassNotFoundException cnfe) {}
        // catch (NoSuchMethodException nsme) {}
        break;
      }
    }

    for (int i = 0; i < SpecConstants.SPEC_CLASS.length; i++) {
      if (supportedExtension != null && supportedExtension == SpecConstants.SPEC_FORMAT[i])
        continue;

      try {
        theClass = Class.forName(SpecConstants.SPEC_CLASS[i]);
        chkMethod = theClass.getMethod("checkFormatAndDimension", param);
        dimI = (Integer) chkMethod.invoke(theClass, arg);
        tempSpecDimension = dimI.intValue();
        // tempSpecDimension = theClass.checkFormatAndDimension (file);
        specFormat = getInstance(SpecConstants.SPEC_FORMAT[i]);
        return new SpecFormatAndDim(specFormat, tempSpecDimension);
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
      // catch (SpecFormat.IOException sioe) {}
      // catch (ClassNotFoundException cnfe) {}
      // catch (NoSuchMethodException nsme) {}
    }

    return null;
  }

  /**
   * Checks the file arguments and determines if it fits its format If true, returns the
   * specDimension If false, throws a "SpecFormat.IOException"
   */
  public abstract int formatAndDimension(File aFile) throws SpecFormat.IOException;

  /** reads a file and returns a spectrum */
  public abstract Spectrum read(File aFile) throws SpecFormat.IOException;

  /** reads a file (given the spec dimension) and returns a spectrum */
  public abstract Spectrum read(File aFile, int aSpecDimension) throws SpecFormat.IOException;

  /** writes a spectrum to a file and returns true or false if successful or not */
  public abstract boolean write(File aFile, Spectrum aSpectrum) throws SpecFormat.IOException;

  /** get the default extension for this spectrum format */
  public abstract String getExtension();

  /** allow a user choose a spectrum format */
  public static SpecFormat chooseFormat() {
    SpecFormat[] specFormats = new SpecFormat[SpecConstants.SPEC_FORMAT.length];
    for (int i = 0; i < specFormats.length; i++) {
      specFormats[i] = getInstance(SpecConstants.SPEC_FORMAT[i]);
    }

    JList formatList = new JList(specFormats);
    int value =
        JOptionPane.showConfirmDialog(
            formatList, formatList, "Choose a spectrum format", JOptionPane.OK_CANCEL_OPTION);

    if (value != JOptionPane.OK_OPTION) return null;
    else return ((SpecFormat) (formatList.getSelectedValue()));
  }

  public static SpecFormat getFormatFromExtension(String ext) {
    if (ext == null) return null;

    return (getInstance(ext));
  }

  /**
   * returns array with each element corresponding to position of a non-zero channel number. Do a
   * bit inspection on each byte in an array and set the value of the current element in the array
   * (that is returned) to the position of this set bit ... Let this return an integer array since
   * we can never tell how big these values can get ... like in the error I got
   */
  public static int[] getArrayOfSetBit(byte[] arrayOfBitmaskRecord, int numNonZeroElements) {

    int[] arrayOfSetBit = new int[numNonZeroElements];

    int k = 0;

    for (int i = 0; i < arrayOfBitmaskRecord.length; i++) {
      byte[] temp = (byte[]) setBitTable.get(new Byte(arrayOfBitmaskRecord[i]));
      for (int j = 0; j < temp.length; j++) {
        arrayOfSetBit[k] = ((i * 8) + temp[j]);
        k++;
      }
    }

    return (arrayOfSetBit);
  }

  /*
  * *******************************************
  * ALTERNATIVE WAY TO CHECK FOR SET BITS
  * *******************************************
  private final int[] getSet (byte bits)
  {
  int[] setbit = new int [8];
  int setbitOffset = 0;

  for (int bitnum = 0; bitnum < 7; bitnum++) {
  if ( (bits & (1 << bitnum)) != 0) {
  setbit [setbitOffset] = bitnum;
  setbitOffset++;
  }
  }

  int returnBit = new int [setbitOffset];
  for (int i = 0; i < setbitOffset; i++) {
  returnBit [i] = setbit [i];
  }

  return returnBit;
  }
  * *******************************************
  */

  public static class IOException extends java.lang.Exception {
    public IOException(String msg) {
      super(msg);
    }

    public IOException(String msg, Throwable thr) {
      super(msg, thr);
    }
  }

  /**
   * Encapsulates the specFormat and the dimension ... ... returned by calls to getSuitableFormat
   * (File)
   */
  public static final class SpecFormatAndDim {
    public SpecFormat specFormat;
    public int dim;

    public SpecFormatAndDim(SpecFormat specFormat, int dim) {
      this.specFormat = specFormat;
      this.dim = dim;
    }
  }

  /*
    Lookup table that finds which bits are set in a particular byte
    static initializer for it at bottom of file
  */
  public static Hashtable setBitTable = new Hashtable(270, 0.97f);

  // static initializer so this setBitTable is only loaded once
  static {
    setBitTable.put(new Byte((byte) -128), new byte[] {7});
    setBitTable.put(new Byte((byte) -127), new byte[] {7, 0});
    setBitTable.put(new Byte((byte) -126), new byte[] {7, 1});
    setBitTable.put(new Byte((byte) -125), new byte[] {7, 1, 0});
    setBitTable.put(new Byte((byte) -124), new byte[] {7, 2});
    setBitTable.put(new Byte((byte) -123), new byte[] {7, 2, 0});
    setBitTable.put(new Byte((byte) -122), new byte[] {7, 2, 1});
    setBitTable.put(new Byte((byte) -121), new byte[] {7, 2, 1, 0});
    setBitTable.put(new Byte((byte) -120), new byte[] {7, 3});
    setBitTable.put(new Byte((byte) -119), new byte[] {7, 3, 0});
    setBitTable.put(new Byte((byte) -118), new byte[] {7, 3, 1});
    setBitTable.put(new Byte((byte) -117), new byte[] {7, 3, 1, 0});
    setBitTable.put(new Byte((byte) -116), new byte[] {7, 3, 2});
    setBitTable.put(new Byte((byte) -115), new byte[] {7, 3, 2, 0});
    setBitTable.put(new Byte((byte) -114), new byte[] {7, 3, 2, 1});
    setBitTable.put(new Byte((byte) -113), new byte[] {7, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) -112), new byte[] {7, 4});
    setBitTable.put(new Byte((byte) -111), new byte[] {7, 4, 0});
    setBitTable.put(new Byte((byte) -110), new byte[] {7, 4, 1});
    setBitTable.put(new Byte((byte) -109), new byte[] {7, 4, 1, 0});
    setBitTable.put(new Byte((byte) -108), new byte[] {7, 4, 2});
    setBitTable.put(new Byte((byte) -107), new byte[] {7, 4, 2, 0});
    setBitTable.put(new Byte((byte) -106), new byte[] {7, 4, 2, 1});
    setBitTable.put(new Byte((byte) -105), new byte[] {7, 4, 2, 1, 0});
    setBitTable.put(new Byte((byte) -104), new byte[] {7, 4, 3});
    setBitTable.put(new Byte((byte) -103), new byte[] {7, 4, 3, 0});
    setBitTable.put(new Byte((byte) -102), new byte[] {7, 4, 3, 1});
    setBitTable.put(new Byte((byte) -101), new byte[] {7, 4, 3, 1, 0});
    setBitTable.put(new Byte((byte) -100), new byte[] {7, 4, 3, 2});
    setBitTable.put(new Byte((byte) -99), new byte[] {7, 4, 3, 2, 0});
    setBitTable.put(new Byte((byte) -98), new byte[] {7, 4, 3, 2, 1});
    setBitTable.put(new Byte((byte) -97), new byte[] {7, 4, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) -96), new byte[] {7, 5});
    setBitTable.put(new Byte((byte) -95), new byte[] {7, 5, 0});
    setBitTable.put(new Byte((byte) -94), new byte[] {7, 5, 1});
    setBitTable.put(new Byte((byte) -93), new byte[] {7, 5, 1, 0});
    setBitTable.put(new Byte((byte) -92), new byte[] {7, 5, 2});
    setBitTable.put(new Byte((byte) -91), new byte[] {7, 5, 2, 0});
    setBitTable.put(new Byte((byte) -90), new byte[] {7, 5, 2, 1});
    setBitTable.put(new Byte((byte) -89), new byte[] {7, 5, 2, 1, 0});
    setBitTable.put(new Byte((byte) -88), new byte[] {7, 5, 3});
    setBitTable.put(new Byte((byte) -87), new byte[] {7, 5, 3, 0});
    setBitTable.put(new Byte((byte) -86), new byte[] {7, 5, 3, 1});
    setBitTable.put(new Byte((byte) -85), new byte[] {7, 5, 3, 1, 0});
    setBitTable.put(new Byte((byte) -84), new byte[] {7, 5, 3, 2});
    setBitTable.put(new Byte((byte) -83), new byte[] {7, 5, 3, 2, 0});
    setBitTable.put(new Byte((byte) -82), new byte[] {7, 5, 3, 2, 1});
    setBitTable.put(new Byte((byte) -81), new byte[] {7, 5, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) -80), new byte[] {7, 5, 4});
    setBitTable.put(new Byte((byte) -79), new byte[] {7, 5, 4, 0});
    setBitTable.put(new Byte((byte) -78), new byte[] {7, 5, 4, 1});
    setBitTable.put(new Byte((byte) -77), new byte[] {7, 5, 4, 1, 0});
    setBitTable.put(new Byte((byte) -76), new byte[] {7, 5, 4, 2});
    setBitTable.put(new Byte((byte) -75), new byte[] {7, 5, 4, 2, 0});
    setBitTable.put(new Byte((byte) -74), new byte[] {7, 5, 4, 2, 1});
    setBitTable.put(new Byte((byte) -73), new byte[] {7, 5, 4, 2, 1, 0});
    setBitTable.put(new Byte((byte) -72), new byte[] {7, 5, 4, 3});
    setBitTable.put(new Byte((byte) -71), new byte[] {7, 5, 4, 3, 0});
    setBitTable.put(new Byte((byte) -70), new byte[] {7, 5, 4, 3, 1});
    setBitTable.put(new Byte((byte) -69), new byte[] {7, 5, 4, 3, 1, 0});
    setBitTable.put(new Byte((byte) -68), new byte[] {7, 5, 4, 3, 2});
    setBitTable.put(new Byte((byte) -67), new byte[] {7, 5, 4, 3, 2, 0});
    setBitTable.put(new Byte((byte) -66), new byte[] {7, 5, 4, 3, 2, 1});
    setBitTable.put(new Byte((byte) -65), new byte[] {7, 5, 4, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) -64), new byte[] {7, 6});
    setBitTable.put(new Byte((byte) -63), new byte[] {7, 6, 0});
    setBitTable.put(new Byte((byte) -62), new byte[] {7, 6, 1});
    setBitTable.put(new Byte((byte) -61), new byte[] {7, 6, 1, 0});
    setBitTable.put(new Byte((byte) -60), new byte[] {7, 6, 2});
    setBitTable.put(new Byte((byte) -59), new byte[] {7, 6, 2, 0});
    setBitTable.put(new Byte((byte) -58), new byte[] {7, 6, 2, 1});
    setBitTable.put(new Byte((byte) -57), new byte[] {7, 6, 2, 1, 0});
    setBitTable.put(new Byte((byte) -56), new byte[] {7, 6, 3});
    setBitTable.put(new Byte((byte) -55), new byte[] {7, 6, 3, 0});
    setBitTable.put(new Byte((byte) -54), new byte[] {7, 6, 3, 1});
    setBitTable.put(new Byte((byte) -53), new byte[] {7, 6, 3, 1, 0});
    setBitTable.put(new Byte((byte) -52), new byte[] {7, 6, 3, 2});
    setBitTable.put(new Byte((byte) -51), new byte[] {7, 6, 3, 2, 0});
    setBitTable.put(new Byte((byte) -50), new byte[] {7, 6, 3, 2, 1});
    setBitTable.put(new Byte((byte) -49), new byte[] {7, 6, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) -48), new byte[] {7, 6, 4});
    setBitTable.put(new Byte((byte) -47), new byte[] {7, 6, 4, 0});
    setBitTable.put(new Byte((byte) -46), new byte[] {7, 6, 4, 1});
    setBitTable.put(new Byte((byte) -45), new byte[] {7, 6, 4, 1, 0});
    setBitTable.put(new Byte((byte) -44), new byte[] {7, 6, 4, 2});
    setBitTable.put(new Byte((byte) -43), new byte[] {7, 6, 4, 2, 0});
    setBitTable.put(new Byte((byte) -42), new byte[] {7, 6, 4, 2, 1});
    setBitTable.put(new Byte((byte) -41), new byte[] {7, 6, 4, 2, 1, 0});
    setBitTable.put(new Byte((byte) -40), new byte[] {7, 6, 4, 3});
    setBitTable.put(new Byte((byte) -39), new byte[] {7, 6, 4, 3, 0});
    setBitTable.put(new Byte((byte) -38), new byte[] {7, 6, 4, 3, 1});
    setBitTable.put(new Byte((byte) -37), new byte[] {7, 6, 4, 3, 1, 0});
    setBitTable.put(new Byte((byte) -36), new byte[] {7, 6, 4, 3, 2});
    setBitTable.put(new Byte((byte) -35), new byte[] {7, 6, 4, 3, 2, 0});
    setBitTable.put(new Byte((byte) -34), new byte[] {7, 6, 4, 3, 2, 1});
    setBitTable.put(new Byte((byte) -33), new byte[] {7, 6, 4, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) -32), new byte[] {7, 6, 5});
    setBitTable.put(new Byte((byte) -31), new byte[] {7, 6, 5, 0});
    setBitTable.put(new Byte((byte) -30), new byte[] {7, 6, 5, 1});
    setBitTable.put(new Byte((byte) -29), new byte[] {7, 6, 5, 1, 0});
    setBitTable.put(new Byte((byte) -28), new byte[] {7, 6, 5, 2});
    setBitTable.put(new Byte((byte) -27), new byte[] {7, 6, 5, 2, 0});
    setBitTable.put(new Byte((byte) -26), new byte[] {7, 6, 5, 2, 1});
    setBitTable.put(new Byte((byte) -25), new byte[] {7, 6, 5, 2, 1, 0});
    setBitTable.put(new Byte((byte) -24), new byte[] {7, 6, 5, 3});
    setBitTable.put(new Byte((byte) -23), new byte[] {7, 6, 5, 3, 0});
    setBitTable.put(new Byte((byte) -22), new byte[] {7, 6, 5, 3, 1});
    setBitTable.put(new Byte((byte) -21), new byte[] {7, 6, 5, 3, 1, 0});
    setBitTable.put(new Byte((byte) -20), new byte[] {7, 6, 5, 3, 2});
    setBitTable.put(new Byte((byte) -19), new byte[] {7, 6, 5, 3, 2, 0});
    setBitTable.put(new Byte((byte) -18), new byte[] {7, 6, 5, 3, 2, 1});
    setBitTable.put(new Byte((byte) -17), new byte[] {7, 6, 5, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) -16), new byte[] {7, 6, 5, 4});
    setBitTable.put(new Byte((byte) -15), new byte[] {7, 6, 5, 4, 0});
    setBitTable.put(new Byte((byte) -14), new byte[] {7, 6, 5, 4, 1});
    setBitTable.put(new Byte((byte) -13), new byte[] {7, 6, 5, 4, 1, 0});
    setBitTable.put(new Byte((byte) -12), new byte[] {7, 6, 5, 4, 2});
    setBitTable.put(new Byte((byte) -11), new byte[] {7, 6, 5, 4, 2, 0});
    setBitTable.put(new Byte((byte) -10), new byte[] {7, 6, 5, 4, 2, 1});
    setBitTable.put(new Byte((byte) -9), new byte[] {7, 6, 5, 4, 2, 1, 0});
    setBitTable.put(new Byte((byte) -8), new byte[] {7, 6, 5, 4, 3});
    setBitTable.put(new Byte((byte) -7), new byte[] {7, 6, 5, 4, 3, 0});
    setBitTable.put(new Byte((byte) -6), new byte[] {7, 6, 5, 4, 3, 1});
    setBitTable.put(new Byte((byte) -5), new byte[] {7, 6, 5, 4, 3, 1, 0});
    setBitTable.put(new Byte((byte) -4), new byte[] {7, 6, 5, 4, 3, 2});
    setBitTable.put(new Byte((byte) -3), new byte[] {7, 6, 5, 4, 3, 2, 0});
    setBitTable.put(new Byte((byte) -2), new byte[] {7, 6, 5, 4, 3, 2, 1});
    setBitTable.put(new Byte((byte) -1), new byte[] {7, 6, 5, 4, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) 0), new byte[] {});
    setBitTable.put(new Byte((byte) 1), new byte[] {0});
    setBitTable.put(new Byte((byte) 2), new byte[] {1});
    setBitTable.put(new Byte((byte) 3), new byte[] {1, 0});
    setBitTable.put(new Byte((byte) 4), new byte[] {2});
    setBitTable.put(new Byte((byte) 5), new byte[] {2, 0});
    setBitTable.put(new Byte((byte) 6), new byte[] {2, 1});
    setBitTable.put(new Byte((byte) 7), new byte[] {2, 1, 0});
    setBitTable.put(new Byte((byte) 8), new byte[] {3});
    setBitTable.put(new Byte((byte) 9), new byte[] {3, 0});
    setBitTable.put(new Byte((byte) 10), new byte[] {3, 1});
    setBitTable.put(new Byte((byte) 11), new byte[] {3, 1, 0});
    setBitTable.put(new Byte((byte) 12), new byte[] {3, 2});
    setBitTable.put(new Byte((byte) 13), new byte[] {3, 2, 0});
    setBitTable.put(new Byte((byte) 14), new byte[] {3, 2, 1});
    setBitTable.put(new Byte((byte) 15), new byte[] {3, 2, 1, 0});
    setBitTable.put(new Byte((byte) 16), new byte[] {4});
    setBitTable.put(new Byte((byte) 17), new byte[] {4, 0});
    setBitTable.put(new Byte((byte) 18), new byte[] {4, 1});
    setBitTable.put(new Byte((byte) 19), new byte[] {4, 1, 0});
    setBitTable.put(new Byte((byte) 20), new byte[] {4, 2});
    setBitTable.put(new Byte((byte) 21), new byte[] {4, 2, 0});
    setBitTable.put(new Byte((byte) 22), new byte[] {4, 2, 1});
    setBitTable.put(new Byte((byte) 23), new byte[] {4, 2, 1, 0});
    setBitTable.put(new Byte((byte) 24), new byte[] {4, 3});
    setBitTable.put(new Byte((byte) 25), new byte[] {4, 3, 0});
    setBitTable.put(new Byte((byte) 26), new byte[] {4, 3, 1});
    setBitTable.put(new Byte((byte) 27), new byte[] {4, 3, 1, 0});
    setBitTable.put(new Byte((byte) 28), new byte[] {4, 3, 2});
    setBitTable.put(new Byte((byte) 29), new byte[] {4, 3, 2, 0});
    setBitTable.put(new Byte((byte) 30), new byte[] {4, 3, 2, 1});
    setBitTable.put(new Byte((byte) 31), new byte[] {4, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) 32), new byte[] {5});
    setBitTable.put(new Byte((byte) 33), new byte[] {5, 0});
    setBitTable.put(new Byte((byte) 34), new byte[] {5, 1});
    setBitTable.put(new Byte((byte) 35), new byte[] {5, 1, 0});
    setBitTable.put(new Byte((byte) 36), new byte[] {5, 2});
    setBitTable.put(new Byte((byte) 37), new byte[] {5, 2, 0});
    setBitTable.put(new Byte((byte) 38), new byte[] {5, 2, 1});
    setBitTable.put(new Byte((byte) 39), new byte[] {5, 2, 1, 0});
    setBitTable.put(new Byte((byte) 40), new byte[] {5, 3});
    setBitTable.put(new Byte((byte) 41), new byte[] {5, 3, 0});
    setBitTable.put(new Byte((byte) 42), new byte[] {5, 3, 1});
    setBitTable.put(new Byte((byte) 43), new byte[] {5, 3, 1, 0});
    setBitTable.put(new Byte((byte) 44), new byte[] {5, 3, 2});
    setBitTable.put(new Byte((byte) 45), new byte[] {5, 3, 2, 0});
    setBitTable.put(new Byte((byte) 46), new byte[] {5, 3, 2, 1});
    setBitTable.put(new Byte((byte) 47), new byte[] {5, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) 48), new byte[] {5, 4});
    setBitTable.put(new Byte((byte) 49), new byte[] {5, 4, 0});
    setBitTable.put(new Byte((byte) 50), new byte[] {5, 4, 1});
    setBitTable.put(new Byte((byte) 51), new byte[] {5, 4, 1, 0});
    setBitTable.put(new Byte((byte) 52), new byte[] {5, 4, 2});
    setBitTable.put(new Byte((byte) 53), new byte[] {5, 4, 2, 0});
    setBitTable.put(new Byte((byte) 54), new byte[] {5, 4, 2, 1});
    setBitTable.put(new Byte((byte) 55), new byte[] {5, 4, 2, 1, 0});
    setBitTable.put(new Byte((byte) 56), new byte[] {5, 4, 3});
    setBitTable.put(new Byte((byte) 57), new byte[] {5, 4, 3, 0});
    setBitTable.put(new Byte((byte) 58), new byte[] {5, 4, 3, 1});
    setBitTable.put(new Byte((byte) 59), new byte[] {5, 4, 3, 1, 0});
    setBitTable.put(new Byte((byte) 60), new byte[] {5, 4, 3, 2});
    setBitTable.put(new Byte((byte) 61), new byte[] {5, 4, 3, 2, 0});
    setBitTable.put(new Byte((byte) 62), new byte[] {5, 4, 3, 2, 1});
    setBitTable.put(new Byte((byte) 63), new byte[] {5, 4, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) 64), new byte[] {6});
    setBitTable.put(new Byte((byte) 65), new byte[] {6, 0});
    setBitTable.put(new Byte((byte) 66), new byte[] {6, 1});
    setBitTable.put(new Byte((byte) 67), new byte[] {6, 1, 0});
    setBitTable.put(new Byte((byte) 68), new byte[] {6, 2});
    setBitTable.put(new Byte((byte) 69), new byte[] {6, 2, 0});
    setBitTable.put(new Byte((byte) 70), new byte[] {6, 2, 1});
    setBitTable.put(new Byte((byte) 71), new byte[] {6, 2, 1, 0});
    setBitTable.put(new Byte((byte) 72), new byte[] {6, 3});
    setBitTable.put(new Byte((byte) 73), new byte[] {6, 3, 0});
    setBitTable.put(new Byte((byte) 74), new byte[] {6, 3, 1});
    setBitTable.put(new Byte((byte) 75), new byte[] {6, 3, 1, 0});
    setBitTable.put(new Byte((byte) 76), new byte[] {6, 3, 2});
    setBitTable.put(new Byte((byte) 77), new byte[] {6, 3, 2, 0});
    setBitTable.put(new Byte((byte) 78), new byte[] {6, 3, 2, 1});
    setBitTable.put(new Byte((byte) 79), new byte[] {6, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) 80), new byte[] {6, 4});
    setBitTable.put(new Byte((byte) 81), new byte[] {6, 4, 0});
    setBitTable.put(new Byte((byte) 82), new byte[] {6, 4, 1});
    setBitTable.put(new Byte((byte) 83), new byte[] {6, 4, 1, 0});
    setBitTable.put(new Byte((byte) 84), new byte[] {6, 4, 2});
    setBitTable.put(new Byte((byte) 85), new byte[] {6, 4, 2, 0});
    setBitTable.put(new Byte((byte) 86), new byte[] {6, 4, 2, 1});
    setBitTable.put(new Byte((byte) 87), new byte[] {6, 4, 2, 1, 0});
    setBitTable.put(new Byte((byte) 88), new byte[] {6, 4, 3});
    setBitTable.put(new Byte((byte) 89), new byte[] {6, 4, 3, 0});
    setBitTable.put(new Byte((byte) 90), new byte[] {6, 4, 3, 1});
    setBitTable.put(new Byte((byte) 91), new byte[] {6, 4, 3, 1, 0});
    setBitTable.put(new Byte((byte) 92), new byte[] {6, 4, 3, 2});
    setBitTable.put(new Byte((byte) 93), new byte[] {6, 4, 3, 2, 0});
    setBitTable.put(new Byte((byte) 94), new byte[] {6, 4, 3, 2, 1});
    setBitTable.put(new Byte((byte) 95), new byte[] {6, 4, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) 96), new byte[] {6, 5});
    setBitTable.put(new Byte((byte) 97), new byte[] {6, 5, 0});
    setBitTable.put(new Byte((byte) 98), new byte[] {6, 5, 1});
    setBitTable.put(new Byte((byte) 99), new byte[] {6, 5, 1, 0});
    setBitTable.put(new Byte((byte) 100), new byte[] {6, 5, 2});
    setBitTable.put(new Byte((byte) 101), new byte[] {6, 5, 2, 0});
    setBitTable.put(new Byte((byte) 102), new byte[] {6, 5, 2, 1});
    setBitTable.put(new Byte((byte) 103), new byte[] {6, 5, 2, 1, 0});
    setBitTable.put(new Byte((byte) 104), new byte[] {6, 5, 3});
    setBitTable.put(new Byte((byte) 105), new byte[] {6, 5, 3, 0});
    setBitTable.put(new Byte((byte) 106), new byte[] {6, 5, 3, 1});
    setBitTable.put(new Byte((byte) 107), new byte[] {6, 5, 3, 1, 0});
    setBitTable.put(new Byte((byte) 108), new byte[] {6, 5, 3, 2});
    setBitTable.put(new Byte((byte) 109), new byte[] {6, 5, 3, 2, 0});
    setBitTable.put(new Byte((byte) 110), new byte[] {6, 5, 3, 2, 1});
    setBitTable.put(new Byte((byte) 111), new byte[] {6, 5, 3, 2, 1, 0});
    setBitTable.put(new Byte((byte) 112), new byte[] {6, 5, 4});
    setBitTable.put(new Byte((byte) 113), new byte[] {6, 5, 4, 0});
    setBitTable.put(new Byte((byte) 114), new byte[] {6, 5, 4, 1});
    setBitTable.put(new Byte((byte) 115), new byte[] {6, 5, 4, 1, 0});
    setBitTable.put(new Byte((byte) 116), new byte[] {6, 5, 4, 2});
    setBitTable.put(new Byte((byte) 117), new byte[] {6, 5, 4, 2, 0});
    setBitTable.put(new Byte((byte) 118), new byte[] {6, 5, 4, 2, 1});
    setBitTable.put(new Byte((byte) 119), new byte[] {6, 5, 4, 2, 1, 0});
    setBitTable.put(new Byte((byte) 120), new byte[] {6, 5, 4, 3});
    setBitTable.put(new Byte((byte) 121), new byte[] {6, 5, 4, 3, 0});
    setBitTable.put(new Byte((byte) 122), new byte[] {6, 5, 4, 3, 1});
    setBitTable.put(new Byte((byte) 123), new byte[] {6, 5, 4, 3, 1, 0});
    setBitTable.put(new Byte((byte) 124), new byte[] {6, 5, 4, 3, 2});
    setBitTable.put(new Byte((byte) 125), new byte[] {6, 5, 4, 3, 2, 0});
    setBitTable.put(new Byte((byte) 126), new byte[] {6, 5, 4, 3, 2, 1});
    setBitTable.put(new Byte((byte) 127), new byte[] {6, 5, 4, 3, 2, 1, 0});
  }
}
