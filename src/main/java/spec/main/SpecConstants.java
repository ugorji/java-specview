package spec.main;

import spec.lib.LibConstants;

/** This class just defines a bunch of constants that we can use in our program */
public interface SpecConstants extends LibConstants {

  // define the constants in this interface
  public static final String ORIGINAL_SPC_FORMAT_EXTENSION = "spc";
  public static final String EXTENDED_SPC_FORMAT_EXTENSION = "spx";
  public static final String RAW_DATA_SPC_FORMAT_EXTENSION = "spt";

  public static final String ORIGINAL_SPC_FORMAT_CLASS = "spec.io.SpecFormat_spc";
  public static final String EXTENDED_SPC_FORMAT_CLASS = "spec.io.SpecFormat_spc2";
  public static final String RAW_DATA_SPC_FORMAT_CLASS = "spec.io.SpecFormat_data";

  public static final String ORIGINAL_SPC_FORMAT_DESCRIPTION = "Original Spec File format (*.spc)";
  public static final String EXTENDED_SPC_FORMAT_DESCRIPTION = "Extended Spec File format (*.spx)";
  public static final String RAW_DATA_SPC_FORMAT_DESCRIPTION = "Ascii Spec File format (*.spt)";

  public static final String[] SPEC_FORMAT =
      new String[] {
        ORIGINAL_SPC_FORMAT_EXTENSION, EXTENDED_SPC_FORMAT_EXTENSION, RAW_DATA_SPC_FORMAT_EXTENSION
      };

  public static final String[] SPEC_CLASS =
      new String[] {
        ORIGINAL_SPC_FORMAT_CLASS, EXTENDED_SPC_FORMAT_CLASS, RAW_DATA_SPC_FORMAT_CLASS
      };

  public static final String[] APPLICATION_STATE = new String[] {"ser"};
}
