package spec.main;

import spec.io.SpecFormat;
import spec.io.SpecFormat_data;
import spec.io.SpecFormat_spc;
import spec.io.SpecFormat_spc2;
import spec.spectrum.Spectrum;

public class SpecFactory {

  /** factory methods for creating a specFormat */
  public static SpecFormat createSpecFormat(String specFormatString) {
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

  /** create a spectrum ... why??? */
  public static Spectrum createSpectrum(int specDimension) {
    if (specDimension == 1) return (new Spectrum.OneDim());
    else if (specDimension == 2) return (new Spectrum.TwoDim());
    else return null;
  }
}
