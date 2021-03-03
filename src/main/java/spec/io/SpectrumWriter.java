package spec.io;

import java.io.File;
import java.util.Date;
import spec.spectrum.Spectrum;

/** has methods for writing spectra to files in specific formats */
public class SpectrumWriter {
  /**
   * try to write a spectrum and return true or false if successfully written the date on the
   * spectrum is always set to the current date when a spectrum is being written
   */
  public static boolean write(Spectrum spectrum, File specFile, SpecFormat specFormat)
      throws SpecFormat.IOException {
    // the spectrum date must be set to a new date when a spectrum is written
    spectrum.setSpecDate(new Date());
    return (specFormat.write(specFile, spectrum));
  }
}
