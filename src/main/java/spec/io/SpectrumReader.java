package spec.io;

import java.io.File;
import spec.spectrum.Spectrum;

/** has static methods for reading a spectrum from a file */
public class SpectrumReader {
  // protected File specFile;
  // protected int specDimension;
  // protected SpecFormat specFormat;

  /*
  * ***********************************************
  private static Spectrum read_Old (File specFile)
  throws SpecFormat.IOException
  {
  int specDimension = -1;
  SpecFormat specFormat = null;

  // for each spec formats, call its checkFormat method with a File argument.
  // If SpecFormat.IOException is thrown, keep looping. Else, specDimension ...
  int tempSpecDimension = -1;
  SpecFormat aSpecFormat = null;
  for (int i = 0; i < SpecConstants.SPEC_FORMAT.length; i++) {
  try  {
  aSpecFormat = SpecFormat.getInstance ( SpecConstants.SPEC_FORMAT [i] );
  tempSpecDimension = aSpecFormat.checkFormatAndDimension (specFile);

  if (tempSpecDimension != -1) {
  specDimension = tempSpecDimension;
  specFormat = aSpecFormat;
  break;
  }
  }
  catch (SpecFormat.IOException e)  {
  if (specFormat != null)
  break;
  }
  catch (Exception e2)  {
  break;
  }
  }

  if (specFormat == null)
  throw new SpecFormat.IOException
  ("The file: " + specFile.getName () + " did not fit a spectrum format" );

  return (specFormat.read (specFile, specDimension) );

  }
  * ***********************************************
  */

  public static Spectrum read(File specFile) throws SpecFormat.IOException {
    SpecFormat.SpecFormatAndDim specFormatAndDim = SpecFormat.getSuitableFormat(specFile);

    if (specFormatAndDim == null)
      throw new SpecFormat.IOException(
          "The file: " + specFile.getName() + " did not fit a spectrum format");

    return (specFormatAndDim.specFormat.read(specFile, specFormatAndDim.dim));
  }

  /** given the name of a spec file, return a spectrum from it */
  public static Spectrum read(String aSpecFileName) throws SpecFormat.IOException {
    return (read(new File(aSpecFileName)));
  }
}
