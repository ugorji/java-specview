package spec.ui;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.Serializable;
import java.util.Properties;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.event.InternalFrameListener;
import spec.lib.ui.ComponentInfoManager;
import spec.lib.ui.FontChooser;
import spec.main.SpecConstants;

/* interface class that just serves as superclass to Spec UI main classes e.g DefaultSpecUI_MDI */
public interface SpecUI extends SpecConstants, Serializable, PropertyChangeListener {

  /** get the resource bundle used by this SpecUI */
  public Properties getProperties();

  /** set the resource bundle used by this SpecUI */
  public void setProperties(Properties properties);

  /** get the window displaying information about SpecView */
  public AboutSpecViewWindow getAboutSpecViewWindow();

  /** get the main window used by this SpecUI */
  public JFrame getMainWindow();

  /** get the file chooser used by this SpecUI */
  public SpectrumFileChooser getSpectrumFileChooser();

  /** get the font chooser used by this SpecUI */
  public FontChooser getFontChooser();

  /** get the spectrumManipulator used by this SpecUI */
  public SpectrumManipulator getSpectrumManipulator();

  /** get the ComponentInfoManager */
  public ComponentInfoManager getComponentInfoManager();

  /** select a file that we can write to */
  public File selectWritableFile();

  /** select a file that we can read */
  public File selectReadableFile();

  /** changes the cursor for the whole UI to a wait cursor */
  public void setWaitCursor();

  /** resets the cursor to the default UI cursor */
  public void setDefaultCursor();

  /** set an informational message that can be displayed in a status bar */
  public void setStatusText(String statusText);

  /** enable or disable the relevant actions of the SpecUI */
  public void enableActions();

  // interface for MDI (Internal frame containing) UI to spec program
  public static interface MDI extends SpecUI, InternalFrameListener {
    /** get the desktop Pane used by this SpecUI */
    public JDesktopPane getDesktopPane();

    /**
     * enables or disables the action components for internal frames ... based on if there is a
     * drawable spectrum contained, ... and the dimension of the spectrum it delegates to
     */
    public void enableSpecFrameActions();
  }
}
