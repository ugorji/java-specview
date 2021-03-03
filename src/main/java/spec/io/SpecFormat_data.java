package spec.io;

import java.io.File;
import java.io.Serializable;
import spec.main.SpecConstants;
import spec.spectrum.Spectrum;

public class SpecFormat_data extends SpecFormat implements Cloneable, Serializable {

  public SpecFormat_data() {
    super();
  }

  public String toString() {
    return "COLUMNAR FORMAT";
  }

  /** get the default extension for this spectrum format */
  public String getExtension() {
    return SpecConstants.RAW_DATA_SPC_FORMAT_EXTENSION;
  }

  /**
   * Returns the spectrum dimension if right format ... or throws an exception if the wrong format
   */
  public static int checkFormatAndDimension(File aFile) throws SpecFormat.IOException {
    throw new SpecFormat.IOException("This format is not yet supported");

    // return -1;
  }

  public int formatAndDimension(File aFile) throws SpecFormat.IOException {
    return (checkFormatAndDimension(aFile));
  }

  public Spectrum read(File aFile) throws SpecFormat.IOException {
    int specDimension = this.checkFormatAndDimension(aFile);

    return (read(aFile, specDimension));
  }

  public Spectrum read(File aFile, int specDimension) throws SpecFormat.IOException {
    throw new SpecFormat.IOException("This format is not yet supported");

    // return null;
  }

  public boolean write(File aFile, Spectrum aSpectrum) throws SpecFormat.IOException {
    throw new SpecFormat.IOException("This format is not yet supported");

    // return false;
  }
}
