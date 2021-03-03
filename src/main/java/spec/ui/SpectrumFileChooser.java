package spec.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import spec.io.SpecFormat;
import spec.main.SpecConstants;

/**
 * An implementation of the JFileChooser that contains filters for the various spectra formats ...
 * This Chooser will be more involved in the future ... For now, much stuff has been commented out
 * ... ... Only the "originalSpecFormatFilter" is available for reading or writing spectra ...
 */
public class SpectrumFileChooser extends JFileChooser implements SpecConstants {

  // the filter for all the spec formats
  private Filter allSpecFormatsFilter;
  private Filter originalSpecFormatFilter;
  private Filter extendedSpecFormatFilter;
  private Filter asciiSpecFormatFilter;

  /** constructor, that adds the file filters based on strings defined in SpecConstants */
  public SpectrumFileChooser() {
    super();
    // sets the current directory of the file chooser
    setCurrentDirectory(new File(System.getProperty("user.dir")));
    setMultiSelectionEnabled(true);

    allSpecFormatsFilter =
        new Filter(
            SPEC_FORMAT,
            "Spectrum Files (*."
                + ORIGINAL_SPC_FORMAT_EXTENSION
                + ", *."
                + EXTENDED_SPC_FORMAT_EXTENSION
                + ", *."
                + RAW_DATA_SPC_FORMAT_EXTENSION
                + ") ");
    // addChoosableFileFilter (allSpecFormatsFilter);
    // setFileFilter (allSpecFormatsFilter);

    originalSpecFormatFilter =
        new Filter(new String[] {ORIGINAL_SPC_FORMAT_EXTENSION}, ORIGINAL_SPC_FORMAT_DESCRIPTION);
    addChoosableFileFilter(originalSpecFormatFilter);
    setFileFilter(originalSpecFormatFilter);

    extendedSpecFormatFilter =
        new Filter(new String[] {EXTENDED_SPC_FORMAT_EXTENSION}, EXTENDED_SPC_FORMAT_DESCRIPTION);
    // addChoosableFileFilter (extendedSpecFormatFilter );

    asciiSpecFormatFilter =
        new Filter(new String[] {RAW_DATA_SPC_FORMAT_EXTENSION}, RAW_DATA_SPC_FORMAT_DESCRIPTION);
    // addChoosableFileFilter ( asciiSpecFormatFilter );

    setFileSelectionMode(JFileChooser.FILES_ONLY);
    setDialogType(JFileChooser.CUSTOM_DIALOG);
  }

  /** override the default SaveDialog to remove some file filters when saving */
  public int showSaveDialog(Component parent) {
    // **** COULD HAVE USED THIS ... NO ...
    // this.setAccessory (accessory);
    // int i = super.showSaveDialog ();
    // this.setAccessory (null);
    // return i;

    // ******* USING THIS ... YEA ...
    setFileFilter(originalSpecFormatFilter);

    // removeChoosableFileFilter (acceptAllFilter);
    // removeChoosableFileFilter (allSpecFormatsFilter);
    // removeChoosableFileFilter (extendedSpecFormatFilter);
    // removeChoosableFileFilter (asciiSpecFormatFilter);

    int i = super.showSaveDialog(parent);

    // addChoosableFileFilter (allSpecFormatsFilter);
    // addChoosableFileFilter (extendedSpecFormatFilter);
    // addChoosableFileFilter (asciiSpecFormatFilter);

    // do not do this, since we need to get the file filter to select the format
    // setFileFilter (allSpecFormatsFilter);

    return i;
  }

  /** Filter class for the Spectrum file */
  public static class Filter extends javax.swing.filechooser.FileFilter {

    String[] extensions;
    String description;

    // SpecFileChooserAccesory accessory = new SpecFileChooserAccesory ();

    public Filter(String ext) {
      this(new String[] {ext}, null);
    }

    public Filter(String[] exts, String descr) {
      // clone and lowercase the extensions
      extensions = new String[exts.length];
      for (int i = exts.length - 1; i >= 0; i--) {
        extensions[i] = exts[i].toLowerCase();
      }
      // make sure we have a valid (if simplistic) description
      description = (descr == null ? exts[0] + " files" : descr);
    }

    public boolean accept(File f) {
      // we always allow directories, regardless of their extension
      if (f.isDirectory()) {
        return true;
      }

      // ok, it's a regular file so check the extension
      String name = f.getName().toLowerCase();
      for (int i = extensions.length - 1; i >= 0; i--) {
        if (name.endsWith(extensions[i])) {
          return true;
        }
      }
      return false;
    }

    public String getDescription() {
      return description;
    }

    /** gets the extensions in this filter */
    public String[] getExtensions() {
      if (extensions == null) return null;
      else return extensions;
    }
  }

  public static class SpecFileChooserAccesory extends JPanel {
    JComboBox formatList;
    JTextArea textArea;

    SpecFormat[] specFormats;

    protected SpecFileChooserAccesory() {
      specFormats = new SpecFormat[SpecConstants.SPEC_FORMAT.length];
      for (int i = 0; i < specFormats.length; i++) {
        specFormats[i] = SpecFormat.getInstance(SpecConstants.SPEC_FORMAT[i]);
      }

      formatList = new JComboBox(specFormats);
      if (!(getLayout() instanceof BorderLayout)) this.setLayout(new BorderLayout());

      String msg = "Select a \nspectrum \nformat";
      textArea = new JTextArea(msg);

      this.add(textArea, BorderLayout.NORTH);
      this.add(formatList, BorderLayout.CENTER);
      reset();
    }

    /** reset the combo box to have the selected item as the original spec format */
    protected void reset() {
      int i = 0;

      for (i = 0; i < specFormats.length; i++) {
        if ((specFormats[i].getExtension()).equals(SpecConstants.ORIGINAL_SPC_FORMAT_EXTENSION))
          break;
      }

      formatList.setSelectedItem(specFormats[i]);
    }
  }
}
