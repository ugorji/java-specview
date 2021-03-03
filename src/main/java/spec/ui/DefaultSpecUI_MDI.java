package spec.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.event.InternalFrameEvent;
import spec.lib.Misc;
import spec.lib.graph.Graph;
import spec.lib.io.IOUtilities;
import spec.lib.ui.ComponentInfoManager;
import spec.lib.ui.FontChooser;
import spec.lib.ui.InternalFrameAction.CascadeAction;
import spec.lib.ui.InternalFrameAction.MinimizeAction;
import spec.lib.ui.InternalFrameAction.RestoreAction;
import spec.lib.ui.InternalFrameAction.TileAction;
import spec.lib.ui.ViewerDialog;
import spec.main.SpecConstants;
import spec.spectrum.DrawableSpectrum;
import spec.spectrum.OverlaySpectrum;
import spec.spectrum.Spectrum;

/**
 * A SpecUI class implementation that uses internal frames to show plots for each graph and allows a
 * user dynamically change the Look and Feel (for item listener) ... expects internal frames to be
 * SpectrumInternalFrames
 *
 * <p>Let the specFrameMenu be the menu from the spec frames ... when a spec frame is activated, the
 * menu changes to that of the new spec frame when deactivated ... the menu is removed ... ... that
 * way, the menu is always that of the current window ...
 */
public class DefaultSpecUI_MDI implements SpecUI.MDI, SpecConstants, ActionListener {
  /** determines if the GUI has already been created */
  protected boolean isGUICreated = false;
  /** JFrame that contains the main window of the application */
  public final JFrame window;

  public final JPanel topPanel = new JPanel();
  public final JPanel bottomPanel = new JPanel();
  /** plotPanel is really a JDeskTopPane but with texture painting & event-handling added */
  public final PlotPanel plotPanel;
  // JPanel buttonBar = new JPanel ();
  public final JMenuBar menuBar = new JMenuBar();

  public final JPanel statusBar = new JPanel();
  public final JPanel statusPanel = new JPanel();
  /** default string that will be displayed in the status bar */
  public final String defaultStatusText = "Send bugs and comments to nwoke@nscl.msu.edu";
  /** label that holds the actual status string that will be displayed */
  public final JLabel statusLabel = new JLabel(defaultStatusText);

  public final JLabel specDisplayedLabel = new JLabel("Spectrum: ");

  public final ComponentInfoManager statusMgr =
      new ComponentInfoManager(statusLabel, statusPanel, statusBar);

  /** Window and panel that show help information */
  public static final AboutSpecViewWindow aboutSpecViewWindow = new AboutSpecViewWindow();

  public final JToolBar standardToolBar = new JToolBar();
  public final JToolBar graphToolBar = new JToolBar();

  // handles opening the files ...
  public final SpectrumFileChooser fileChooser;
  // the font chooser
  public final FontChooser fontChooser;

  // handles choosing and manipulating spectra
  protected final SpectrumManipulator spectrumManipulator;
  // handles interacting directly with the Java Virtual Machine ...
  protected final SpectrumTranscript spectrumTranscript;

  /** Resource bundle holding some stuff we need */
  private Properties properties;

  // Current ui
  private String currentUI = "Cross Platform";

  // L&F radio buttons
  protected JRadioButtonMenuItem macMenuItem;
  protected JRadioButtonMenuItem metalMenuItem;
  protected JRadioButtonMenuItem motifMenuItem;
  protected JRadioButtonMenuItem windowsMenuItem;

  /** can the look and feel be changed ? */
  protected boolean isChangableLookAndFeel = true;

  /** actions corresponding to their names */
  protected Action specWindowAction;

  protected Action openSpecAction;
  protected Action openMultipleSpecAction;
  protected Action specManipulatorAction;
  protected Action specTranscriptAction;
  protected Action errorAction;
  protected Action exitAction;
  protected Action tileAction;
  protected Action cascadeAction;
  protected Action minimizeAction;
  protected Action restoreAction;
  protected Action resizeAllGraphsAction;
  protected Action aboutSpecAction;
  protected Action helpContentsAction;

  /** the menu of the selected spec frame */
  protected JMenu specFrameMenu;

  /** actions corresponding to the spec frame */
  protected Action specFrameDisplayAction;

  protected Action specFramePrintAction;
  protected Action specFrameCaptureAction;
  protected Action specFrameOverlayAction;
  protected Action specFrameInfoAction;
  protected Action specFrameRefreshAction;
  protected Action specFrameRedrawAction;
  protected Action specFrameResizeGraphAction;
  protected Action specFrameClearAction;
  protected Action specFrameCloseAction;

  /** Holds a reference to the SpectrumInternalFrame selected */
  private SpectrumInternalFrame selectedFrame = null;

  /** Maps the actions to the Tooltips */
  protected Map actionToTooltipMap = new HashMap();
  /** Maps the actions to the shortcut keys */
  protected Map actionToShortcutMap = new HashMap();
  /** Maps the actions to the status messages */
  protected Map actionToInfoMsgMap = new HashMap();

  /** window menu showing all the open spec frames */
  protected JMenu windowMenu;
  /** help menu */
  protected JMenu helpMenu;

  /** maps specframes to actions that represent them on the windowMenu */
  protected Map frameToActionMap = new HashMap();
  /** group to hold all the windows */
  protected ButtonGroup windowGroup = new ButtonGroup();
  /** the default icon that all the actions will have if no icon is defined for them */
  Icon defaultImageIcon = DEFAULT_ICON; // from LibConstants

  // no-one can instantiate this
  private DefaultSpecUI_MDI() {
    this(null, true);
  }

  /**
   * constructor ... instantiates and creates and shows window using the resource bundle passed
   * assumes we should show the window
   */
  public DefaultSpecUI_MDI(Properties properties) {
    this(properties, true);
  }

  /**
   * constructor ... instantiates and creates and shows window (if arg is true) using the properties
   * passed boolean flags determine if show main window
   */
  public DefaultSpecUI_MDI(Properties properties, boolean isShowWindow) {
    super();

    // display the info screen
    aboutSpecViewWindow.setVisible(true);
    aboutSpecViewWindow.toFront();

    aboutSpecViewWindow.setStatusText("Setting properties");
    // set the properties, and the UI, then update the UI
    setProperties(properties);

    // perform other init operations
    aboutSpecViewWindow.setStatusText("creating main window");
    window =
        new JFrame("SpecView") {
          // instance initializer
          {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK);
            // this.enableEvent (AWTEvent.COMPONENT_EVENT_MASK);
            // I will handle the window closing myself ... through a window closing method
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setFont(new Font("Helvetica", Font.PLAIN, 12));
            setJMenuBar(menuBar);
          }

          /** request confirmation to close the application before you exit */
          public void processWindowEvent(WindowEvent e) {
            super.processWindowEvent(e);
            if (e.getID() == WindowEvent.WINDOW_CLOSING) {
              // request confirmation to close the application before you exit
              exitSpecView();
            }
          }
        };

    ViewerDialog.recreateBackUpDialogs(window);

    aboutSpecViewWindow.setStatusText("creating file chooser dialog");
    fileChooser = new SpectrumFileChooser();

    aboutSpecViewWindow.setStatusText("creating font chooser dialog");
    fontChooser = new FontChooser();

    aboutSpecViewWindow.setStatusText("creating desktop for graph windows");
    plotPanel = new PlotPanel(this);

    // give a border (thicker) around the plotPanel area
    plotPanel.setBorder(new LineBorder(Color.black, 2));

    aboutSpecViewWindow.setStatusText("creating dialog for manipulating spectra");
    // set the plotPanel & fileChooser for the spectrumManipulator
    spectrumManipulator = new SpectrumManipulator(this, plotPanel);
    aboutSpecViewWindow.setStatusText("creating transcript window");
    // set the spectrum transcript
    spectrumTranscript = SpectrumTranscript.createSpectrumTranscript(this);

    // create and show window, if requested
    if (isShowWindow) showWindow();

    aboutSpecViewWindow.toFront();
    aboutSpecViewWindow.setStatusText("Initialization completed");

    // Wait a little while, maybe while loading properties
    try {
      Thread.sleep(2000);
    } catch (Exception e) {
    }

    aboutSpecViewWindow.setDefaultStatusText();
    aboutSpecViewWindow.setVisible(false);
  }

  /**
   * sets the UI based on the parsed string if updateUI==true, update the UI also, else don't update
   * the UI check done in case the UI will still be updated later
   */
  protected void setSpecViewUI(String UI, boolean updateUI) {
    // try using the native system look and feel ... should be called from a main method
    // use CrossPlatform for now ... due to bug with windows L&F
    try {
      // String UI = resources.getProperty ("UI").trim().toLowerCase();
      if (UI == null || UI.equals("system") || UI.equals("") || UI == null)
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      else if (UI.equals("motif"))
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
      else if (UI.equals("mac"))
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.mac.MacLookAndFeel");
      else if (UI.equals("java") || UI.equals("cross platform"))
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      else if (UI.equals("windows") || UI.equals("window"))
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
      else
        // maybe a customized look and feel is used
        UIManager.setLookAndFeel(UI);
    } catch (Exception e) {
      System.err.println("Problems setting the look and feel: " + e);
      try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception e2) {
        System.err.println("Still couldn't set the Cross platform look and feel: " + e);
      }
    }

    // update the UI if requested
    if (updateUI) {
      updateSpecUI();
    }
  }

  /**
   * sets the Resource Bundle that this program will use , if null, set to an empty Properties
   * object, and then update the UI
   */
  public void setProperties(Properties properties) {
    if (properties != null) this.properties = properties;
    if (this.properties == null) this.properties = new Properties();

    String ui = this.properties.getProperty("UI");

    if (ui != null) ui = ui.trim().toLowerCase();
    // System.out.println ("UI: " + ui);
    setSpecViewUI(ui, true);
  }

  /** gets the Resource Bundle that this program will use */
  public Properties getProperties() {
    return (this.properties);
  }

  /** get the window displaying information about SpecView */
  public AboutSpecViewWindow getAboutSpecViewWindow() {
    return aboutSpecViewWindow;
  }

  /** get the file chooser used by this SpecUI */
  public SpectrumFileChooser getSpectrumFileChooser() {
    return fileChooser;
  }

  /** get the font chooser used by this SpecUI */
  public FontChooser getFontChooser() {
    return fontChooser;
  }

  /** get the main window used by this SpecUI */
  public JFrame getMainWindow() {
    return window;
  }

  /** get the spectrumManipulator used by this SpecUI */
  public SpectrumManipulator getSpectrumManipulator() {
    return spectrumManipulator;
  }

  /** get the ComponentInfoManager */
  public ComponentInfoManager getComponentInfoManager() {
    return statusMgr;
  }

  /** get the spectrumTranscript used by this SpecUI */
  public SpectrumTranscript getSpectrumTranscript() {
    return spectrumTranscript;
  }

  /** get the desktop Pane used by this SpecUI */
  public JDesktopPane getDesktopPane() {
    return plotPanel;
  }

  /** changes the cursor to a wait cursor for the UI of whole the program */
  public void setWaitCursor() {
    window.setCursor(WAIT_CURSOR);
  }

  /** resets the cursor to the default cursor for the UI of whole the program */
  public void setDefaultCursor() {
    window.setCursor(DEFAULT_CURSOR);
  }

  /** set an informational message that can be displayed in a status bar */
  public void setStatusText(String statusText) {
    statusLabel.setText(statusText);
  }

  /** enable or disable the relevant actions of the SpecUI */
  public void enableActions() {
    enablePlotPanelActions();
    enableSpecFrameActions();
  }

  /** select a file that we can write to */
  public File selectWritableFile() {
    return (IOUtilities.selectWritableFile(fileChooser));
  }

  /** select a file that we can read */
  public File selectReadableFile() {
    return (IOUtilities.selectReadableFile(fileChooser));
  }

  /**
   * Creates the window, its menubars and all ... or just shows the UI if the window was hidden or
   * is invisible
   */
  public void showWindow() {
    // if GUI has been created, just make sure it is visible
    if (isGUICreated) {
      if (!(window.isVisible())) {
        window.setVisible(true);
        this.enableActions();
      }
      return;
    }

    aboutSpecViewWindow.setStatusText("setting up main window");

    // let window be indented 1/6 of screen size from all corners (to occupy 2/3 screen size)
    Misc.proportionOnScreen(window, 0.67, 0.67);

    aboutSpecViewWindow.setStatusText("creating UI action components");
    createActionComponents();
    mapActionsToTooltips();
    mapActionsToShortcuts();
    mapActionsToInfoMsgs();

    aboutSpecViewWindow.setStatusText("creating menu bar");
    createMenuBar();
    aboutSpecViewWindow.setStatusText("creating tool bar and status bar");
    createToolBar();
    createStatusBar();

    // put only 2 components in this panel, so the orientation of the
    // toolbar can easily be changed ... to vertical or horizontal
    aboutSpecViewWindow.setStatusText("laying out main window");
    topPanel.setLayout(new BorderLayout());
    topPanel.add(standardToolBar, BorderLayout.NORTH);
    topPanel.add(plotPanel, BorderLayout.CENTER);

    bottomPanel.setLayout(new BorderLayout());
    bottomPanel.add(statusBar, BorderLayout.CENTER);
    bottomPanel.add(graphToolBar, BorderLayout.EAST);

    Container contentPane = window.getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(topPanel, BorderLayout.CENTER);
    contentPane.add(bottomPanel, BorderLayout.SOUTH);

    // update the UI ...
    updateSpecUI();
    // show the window ...
    window.setVisible(true);

    // enable the relevant actions
    enableActions();
    // set the flag indicating that the GUI has been created
    isGUICreated = true;
  }

  /** create the action components */
  private void createActionComponents() {
    Icon icon = null;
    Icon specWindowActionIcon = defaultImageIcon;
    Icon openSpecActionIcon = defaultImageIcon;
    Icon openMultipleSpecActionIcon = defaultImageIcon;
    Icon specManipulatorActionIcon = defaultImageIcon;
    Icon specTranscriptActionIcon = defaultImageIcon;
    Icon errorActionIcon = defaultImageIcon;
    Icon exitActionIcon = defaultImageIcon;
    Icon tileActionIcon = defaultImageIcon;
    Icon cascadeActionIcon = defaultImageIcon;
    Icon minimizeActionIcon = defaultImageIcon;
    Icon restoreActionIcon = defaultImageIcon;
    Icon resizeAllGraphsActionIcon = defaultImageIcon;
    Icon aboutSpecActionIcon = defaultImageIcon;
    Icon helpContentsActionIcon = defaultImageIcon;

    Icon specFrameDisplayActionIcon = defaultImageIcon;
    Icon specFramePrintActionIcon = defaultImageIcon;
    Icon specFrameCaptureActionIcon = defaultImageIcon;
    Icon specFrameOverlayActionIcon = defaultImageIcon;
    Icon specFrameInfoActionIcon = defaultImageIcon;
    Icon specFrameRefreshActionIcon = defaultImageIcon;
    Icon specFrameRedrawActionIcon = defaultImageIcon;
    Icon specFrameResizeGraphActionIcon = defaultImageIcon;
    Icon specFrameClearActionIcon = defaultImageIcon;
    Icon specFrameCloseActionIcon = defaultImageIcon;

    String iconLocation = null;

    iconLocation = properties.getProperty("images." + "specWindow");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specWindowActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "openSpec");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      openSpecActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "openMultipleSpec");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      openMultipleSpecActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "specManipulator");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specManipulatorActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "specTranscript");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specTranscriptActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "error");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      errorActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "exit");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      exitActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "tile");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      tileActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "cascade");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      cascadeActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "minimize");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      minimizeActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "restore");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      restoreActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "resizeAllGraphs");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      resizeAllGraphsActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "aboutSpec");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      aboutSpecActionIcon = icon;
    }

    iconLocation = properties.getProperty("images." + "help");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      helpContentsActionIcon = icon;
    }

    // now ... get those for the spec frame actions

    iconLocation = properties.getProperty("images.specFrame." + "display");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFrameDisplayActionIcon = icon;
    }

    iconLocation = properties.getProperty("images.specFrame." + "print");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFramePrintActionIcon = icon;
    }

    iconLocation = properties.getProperty("images.specFrame." + "capture");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFrameCaptureActionIcon = icon;
    }

    iconLocation = properties.getProperty("images.specFrame." + "overlay");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFrameOverlayActionIcon = icon;
    }

    iconLocation = properties.getProperty("images.specFrame." + "info");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFrameInfoActionIcon = icon;
    }

    iconLocation = properties.getProperty("images.specFrame." + "refresh");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFrameRefreshActionIcon = icon;
    }

    iconLocation = properties.getProperty("images.specFrame." + "redraw");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFrameRedrawActionIcon = icon;
    }

    iconLocation = properties.getProperty("images.specFrame." + "resizeGraph");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFrameResizeGraphActionIcon = icon;
    }

    iconLocation = properties.getProperty("images.specFrame." + "clear");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFrameClearActionIcon = icon;
    }

    iconLocation = properties.getProperty("images.specFrame." + "close");
    if (iconLocation != null) {
      icon =
          new ImageIcon(
              getClass()
                  .getClassLoader()
                  .getResource("spec/resources/images/" + iconLocation.trim()));
      specFrameCloseActionIcon = icon;
    }

    specWindowAction =
        new AbstractAction("New Spectrum Window", specWindowActionIcon) {
          public void actionPerformed(ActionEvent e) {
            try {
              PlotPanel.newSpecFrame(DefaultSpecUI_MDI.this, plotPanel, null, spectrumManipulator);
            } catch (PlotPanel.PlotPanelException ppe) {
              JOptionPane.showMessageDialog(
                  window, ppe.getMessage(), "Error displaying spectrum", JOptionPane.ERROR_MESSAGE);
            }
          }
        };

    openSpecAction =
        new AbstractAction("Read in / Display Spectrum", openSpecActionIcon) {
          public void actionPerformed(ActionEvent e) {
            try {
              setWaitCursor();
              DrawableSpectrum aDrawSpectrum = spectrumManipulator.openSpectrum();

              // create an internal frame for the spectrum, add it to a desktop and
              // ... add a spectrum to it, and passes it a reference to the SpectrumManipulator
              if (aDrawSpectrum == null) return;
              try {
                PlotPanel.newSpecFrame(
                    DefaultSpecUI_MDI.this, plotPanel, aDrawSpectrum, spectrumManipulator);
              } catch (PlotPanel.PlotPanelException ppe) {
                JOptionPane.showMessageDialog(
                    window,
                    ppe.getMessage(),
                    "Error displaying spectrum",
                    JOptionPane.ERROR_MESSAGE);
              }
            } finally {
              setDefaultCursor();
            }
          }
        };

    openMultipleSpecAction =
        new AbstractAction("Read in / Display Multiple Spectra", openMultipleSpecActionIcon) {
          // disable this for now, since multiple files selection is not yet supported
          {
            setEnabled(false);
          }

          public void actionPerformed(ActionEvent e) {
            try {
              setWaitCursor();
              DrawableSpectrum[] drawSpectra = spectrumManipulator.openSpectra();
              DrawableSpectrum aDrawSpectrum;

              // create an internal frame for the spectrum, add it to a desktop and
              // ... add a spectrum to it, and passes it a reference to the SpectrumManipulator
              for (int i = 0; i < drawSpectra.length; i++) {
                aDrawSpectrum = drawSpectra[i];
                if (aDrawSpectrum == null) continue;
                try {
                  PlotPanel.newSpecFrame(
                      DefaultSpecUI_MDI.this, plotPanel, aDrawSpectrum, spectrumManipulator);
                } catch (PlotPanel.PlotPanelException ppe) {
                  JOptionPane.showMessageDialog(
                      window,
                      ppe.getMessage(),
                      "Error displaying spectrum",
                      JOptionPane.ERROR_MESSAGE);
                }
              }
            } finally {
              setDefaultCursor();
            }
          }
        };

    specManipulatorAction =
        new AbstractAction("Spectrum Manipulator", specManipulatorActionIcon) {
          public void actionPerformed(ActionEvent e) {
            spectrumManipulator.manipulateSpectrum();
          }
        };

    specTranscriptAction =
        new AbstractAction("Transcript", specTranscriptActionIcon) {
          // { setEnabled (false); } // do not enable this component for now
          public void actionPerformed(ActionEvent e) {
            boolean visibility = spectrumTranscript.isVisible();

            spectrumTranscript.setVisible(!visibility);
            visibility = !visibility;

            // if (visibility && spectrumTranscript.getState() == Frame.ICONIFIED)
            // spectrumTranscript.setState (Frame.NORMAL);

          }
        };

    errorAction =
        new AbstractAction("View error", errorActionIcon) {
          public void actionPerformed(ActionEvent e) {
            spectrumTranscript.errorDialog.show();
          }
        };

    exitAction =
        new AbstractAction("Exit", exitActionIcon) {
          public void actionPerformed(ActionEvent e) {
            exitSpecView();
          }
        };

    tileAction = new TileAction("Tile Frames", tileActionIcon, plotPanel);

    cascadeAction = new CascadeAction("Cascade Frames", cascadeActionIcon, plotPanel);

    minimizeAction = new MinimizeAction("Minimize Frames", minimizeActionIcon, plotPanel);

    restoreAction = new RestoreAction("Restore Frames", restoreActionIcon, plotPanel);

    resizeAllGraphsAction =
        new AbstractAction("Resize all graphs to fill their windows", resizeAllGraphsActionIcon) {
          public void actionPerformed(ActionEvent e) {
            try {
              setWaitCursor();
              resizeAllGraphs();
            } finally {
              setDefaultCursor();
            }
          }
        };

    aboutSpecAction =
        new AbstractAction("About SpecView", aboutSpecActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // System.out.println ("display information about the application called");
            if (aboutSpecViewWindow != null) {
              aboutSpecViewWindow.setVisible(true);
              aboutSpecViewWindow.toFront();
            } else {
              String msg =
                  "SpecView helps enables you "
                      + "\nview and manipulateSpectrum Files "
                      + "\n\tby Ugorji Nwoke"
                      + "\n\tVersion 1";
              JOptionPane.showMessageDialog(
                  window, msg, "About SpecView", JOptionPane.INFORMATION_MESSAGE);
            }
          }
        };

    helpContentsAction =
        new AbstractAction("Help Contents", helpContentsActionIcon) {
          {
            setEnabled(false);
          } // do not enable this action for now

          public void actionPerformed(ActionEvent e) {
            String msg = "There is no online help available at this time";
            JOptionPane.showMessageDialog(window, msg, "Help Contents", JOptionPane.ERROR_MESSAGE);
          }
        };

    // make the actions for the internal frames
    specFrameDisplayAction =
        new AbstractAction("Display Spectrum", specFrameDisplayActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              selectedFrame.display();
            }
          }
        };

    specFramePrintAction =
        new AbstractAction("Print Graph", specFramePrintActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              selectedFrame.print();
            }
          }
        };

    specFrameCaptureAction =
        new AbstractAction("Screen Capture", specFrameCaptureActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              selectedFrame.capture();
            }
          }
        };

    specFrameOverlayAction =
        new AbstractAction("Overlay Spectrum", specFrameOverlayActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              selectedFrame.overlay();
            }
          }
        };

    specFrameInfoAction =
        new AbstractAction("Info", specFrameInfoActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              DrawableSpectrum drawSpectrum = selectedFrame.getDrawableSpectrum();
              if (drawSpectrum != null) drawSpectrum.info();
            }
          }
        };

    specFrameRefreshAction =
        new AbstractAction("Refresh", specFrameRefreshActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              selectedFrame.repaint();
            }
          }
        };

    specFrameRedrawAction =
        new AbstractAction("Redraw plot", specFrameRedrawActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              selectedFrame.redraw();
            }
          }
        };

    specFrameResizeGraphAction =
        new AbstractAction("Resize graph", specFrameResizeGraphActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              selectedFrame.resizeGraph();
            }
          }
        };

    specFrameClearAction =
        new AbstractAction("Clear graph", specFrameClearActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              selectedFrame.clear();
            }
          }
        };

    specFrameCloseAction =
        new AbstractAction("Close Window", specFrameCloseActionIcon) {
          public void actionPerformed(ActionEvent e) {
            // SpectrumInternalFrame specFrame = (SpectrumInternalFrame)
            // plotPanel.getSelectedFrame() ;
            if (selectedFrame != null) {
              selectedFrame.close();
            }
          }
        };
  }

  /** put the mapping of actions to Tooltips */
  private void mapActionsToTooltips() {
    actionToTooltipMap.put(specWindowAction, "New");
    actionToTooltipMap.put(openSpecAction, "Open");
    actionToTooltipMap.put(openMultipleSpecAction, "Open multiple");
    actionToTooltipMap.put(specManipulatorAction, "Manipulator");
    actionToTooltipMap.put(specTranscriptAction, "Transcript");
    actionToTooltipMap.put(errorAction, "Error");
    actionToTooltipMap.put(exitAction, "Exit");
    actionToTooltipMap.put(tileAction, "Tile");
    actionToTooltipMap.put(cascadeAction, "Cascade");
    actionToTooltipMap.put(minimizeAction, "Minimize");
    actionToTooltipMap.put(restoreAction, "Restore All");
    actionToTooltipMap.put(resizeAllGraphsAction, "Stretch All");
    actionToTooltipMap.put(aboutSpecAction, "About");
    actionToTooltipMap.put(helpContentsAction, "Help");
    actionToTooltipMap.put(specFrameDisplayAction, "Display");
    actionToTooltipMap.put(specFramePrintAction, "Print");
    actionToTooltipMap.put(specFrameCaptureAction, "Screen capture");
    actionToTooltipMap.put(specFrameOverlayAction, "Overlay");
    actionToTooltipMap.put(specFrameInfoAction, "Info");
    actionToTooltipMap.put(specFrameRefreshAction, "Refresh");
    actionToTooltipMap.put(specFrameRedrawAction, "Redraw");
    actionToTooltipMap.put(specFrameResizeGraphAction, "Stretch");
    actionToTooltipMap.put(specFrameClearAction, "Clear");
    actionToTooltipMap.put(specFrameCloseAction, "Close");
  }

  /** put the mapping of actions to status msgs */
  private void mapActionsToInfoMsgs() {
    actionToInfoMsgMap.put(specWindowAction, "Open Empty Spectrum Window");
    actionToInfoMsgMap.put(openSpecAction, "Read in and Display from a Spectrum File");
    actionToInfoMsgMap.put(
        openMultipleSpecAction, "Read in and Display from multiple Spectrum Files");
    actionToInfoMsgMap.put(specManipulatorAction, "Show/Hide dialog for manipulating spectra");
    actionToInfoMsgMap.put(specTranscriptAction, "Show/Hide Transcript Window");
    actionToInfoMsgMap.put(errorAction, "View error");
    actionToInfoMsgMap.put(exitAction, "Exit SpecView");
    actionToInfoMsgMap.put(tileAction, "Tile all the open internal frames");
    actionToInfoMsgMap.put(cascadeAction, "Cascade all the open internal frames");
    actionToInfoMsgMap.put(minimizeAction, "Minimize all the open internal frames");
    actionToInfoMsgMap.put(
        restoreAction, "Restore all the open internal frames to their non-maximized sizes");
    actionToInfoMsgMap.put(
        resizeAllGraphsAction, "Resize all graphs to fill their internal frames");
    actionToInfoMsgMap.put(aboutSpecAction, "Information about Spec Program");
    actionToInfoMsgMap.put(helpContentsAction, "View Help Contents");
    actionToInfoMsgMap.put(specFrameDisplayAction, "Display Spectrum in selected graph");
    actionToInfoMsgMap.put(specFramePrintAction, "Print Graph");
    actionToInfoMsgMap.put(
        specFrameCaptureAction, "Screen capture for graph ... alternative printing method");
    actionToInfoMsgMap.put(specFrameOverlayAction, "Overlay Spectrum in selected graph");
    actionToInfoMsgMap.put(specFrameInfoAction, "View Information on selected graph");
    actionToInfoMsgMap.put(specFrameRefreshAction, "Refresh selected graph");
    actionToInfoMsgMap.put(specFrameRedrawAction, "Redraw plot in selected graph");
    actionToInfoMsgMap.put(specFrameResizeGraphAction, "Resize graph to fill selected window");
    actionToInfoMsgMap.put(specFrameClearAction, "Clear graph in selected window");
    actionToInfoMsgMap.put(specFrameCloseAction, "Close selected window");
  }

  /** put the mapping of actions to Shortcuts */
  private void mapActionsToShortcuts() {
    actionToShortcutMap.put(
        specWindowAction, KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
    actionToShortcutMap.put(openSpecAction, KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
    actionToShortcutMap.put(
        specManipulatorAction, KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK));
    actionToShortcutMap.put(
        specTranscriptAction, KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK));
    actionToShortcutMap.put(exitAction, KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
    actionToShortcutMap.put(
        specFrameDisplayAction, KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
    actionToShortcutMap.put(
        specFramePrintAction, KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
    actionToShortcutMap.put(
        specFrameOverlayAction, KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK));
    actionToShortcutMap.put(
        specFrameInfoAction, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));
    actionToShortcutMap.put(
        specFrameRefreshAction, KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));
    actionToShortcutMap.put(
        specFrameRedrawAction, KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK));
    actionToShortcutMap.put(
        specFrameCloseAction, KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));
  }

  /** creates the menu bar */
  private void createMenuBar() {
    JMenuItem menuItem;
    Insets marginInsets = new Insets(1, 1, 1, 1);

    // (no longer necessary) ... Action commands were:
    // [specWindow, openSpec, specManipulator, specTranscript, exit]

    Action[] fileActions =
        new Action[] {specWindowAction, openSpecAction, openMultipleSpecAction, exitAction};
    // char[] fileMnemonics = new char [] {'n', 'o', '\0', 'q'};

    // number of menu items / commands
    int fileMenuLength = fileActions.length;

    JMenu fileMenu = (JMenu) menuBar.add(new JMenu("File"));
    fileMenu.setMnemonic('F');

    for (int i = 0; i < fileMenuLength; i++) {
      menuItem = (JMenuItem) fileMenu.add(fileActions[i]);
      menuItem.setMargin(marginInsets);
      menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
      menuItem.setAccelerator((KeyStroke) actionToShortcutMap.get(fileActions[i]));
      // menuItem.setMnemonic ( fileMnemonics [i] );
      menuItem.setToolTipText((String) actionToTooltipMap.get(fileActions[i]));

      menuItem.putClientProperty("component.info", (String) actionToInfoMsgMap.get(fileActions[i]));
      menuItem.addMouseListener(statusMgr);
    }

    Action[] viewActions = new Action[] {specManipulatorAction, specTranscriptAction, errorAction};
    // char[] viewMnemonics = new char [] {'c', 't'};

    // number of menu items / commands
    int viewMenuLength = viewActions.length;

    JMenu viewMenu = (JMenu) menuBar.add(new JMenu("View"));
    viewMenu.setMnemonic('V');

    for (int i = 0; i < viewMenuLength; i++) {
      menuItem = (JMenuItem) viewMenu.add(viewActions[i]);
      menuItem.setMargin(marginInsets);
      menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
      menuItem.setAccelerator((KeyStroke) actionToShortcutMap.get(viewActions[i]));
      // menuItem.setMnemonic ( viewMnemonics [i] );
      menuItem.setToolTipText((String) actionToTooltipMap.get(viewActions[i]));

      menuItem.putClientProperty("component.info", (String) actionToInfoMsgMap.get(viewActions[i]));
      menuItem.addMouseListener(statusMgr);
    }

    viewMenu.addSeparator();

    // make Look and Feel menu ... in order crossPlatform, windows, motif
    JMenu lookAndFeel = (JMenu) viewMenu.add(new JMenu("Change Look and Feel"));

    // Look and Feel Radio control
    ButtonGroup lfGroup = new ButtonGroup();

    metalMenuItem =
        (JRadioButtonMenuItem) lookAndFeel.add(new JRadioButtonMenuItem("Cross Platform"));
    lfGroup.add(metalMenuItem);
    metalMenuItem.setEnabled(isChangableLookAndFeel);
    metalMenuItem.setSelected(UIManager.getLookAndFeel().getName().equals("Metal"));
    metalMenuItem.addActionListener(this);
    metalMenuItem.setToolTipText("Cross Platform Look and Feel");
    metalMenuItem.putClientProperty("component.info", "Set to the Cross-Platform Look and Feel");
    metalMenuItem.addMouseListener(statusMgr);

    windowsMenuItem = (JRadioButtonMenuItem) lookAndFeel.add(new JRadioButtonMenuItem("Windows"));
    lfGroup.add(windowsMenuItem);
    windowsMenuItem.setEnabled(isChangableLookAndFeel);
    windowsMenuItem.setSelected(UIManager.getLookAndFeel().getName().equals("Windows"));
    windowsMenuItem.addActionListener(this);
    windowsMenuItem.setToolTipText("Windows look and Feel");
    windowsMenuItem.putClientProperty("component.info", "Set to the Windows Look and Feel");
    windowsMenuItem.addMouseListener(statusMgr);

    motifMenuItem = (JRadioButtonMenuItem) lookAndFeel.add(new JRadioButtonMenuItem("CDE/Motif"));
    lfGroup.add(motifMenuItem);
    motifMenuItem.setEnabled(isChangableLookAndFeel);
    motifMenuItem.setSelected(UIManager.getLookAndFeel().getName().equals("CDE/Motif"));
    motifMenuItem.addActionListener(this);
    motifMenuItem.setToolTipText("CDE/Motif look and Feel");
    motifMenuItem.putClientProperty("component.info", "Set to the CDE/Motif Look and Feel");
    motifMenuItem.addMouseListener(statusMgr);

    macMenuItem = (JRadioButtonMenuItem) lookAndFeel.add(new JRadioButtonMenuItem("Macintosh"));
    lfGroup.add(macMenuItem);
    macMenuItem.setEnabled(false);
    macMenuItem.setSelected(UIManager.getLookAndFeel().getName().equals("Macintosh"));
    macMenuItem.addActionListener(this);
    macMenuItem.setToolTipText("Macintosh Look and Feel");
    macMenuItem.putClientProperty("component.info", "Set to the Macintosh Look and Feel");
    macMenuItem.addMouseListener(statusMgr);

    viewMenu.addSeparator();

    windowMenu = (JMenu) menuBar.add(new JMenu("Window"));
    windowMenu.setMnemonic('W');

    Action[] windowActions =
        new Action[] {
          specWindowAction,
          tileAction,
          cascadeAction,
          minimizeAction,
          restoreAction,
          resizeAllGraphsAction
        };

    // number of menu items / commands
    int windowMenuLength = windowActions.length;

    for (int i = 0; i < windowMenuLength; i++) {
      menuItem = (JMenuItem) windowMenu.add(windowActions[i]);
      menuItem.setMargin(marginInsets);
      menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
      menuItem.setToolTipText((String) actionToTooltipMap.get(windowActions[i]));

      menuItem.putClientProperty(
          "component.info", (String) actionToInfoMsgMap.get(windowActions[i]));
      menuItem.addMouseListener(statusMgr);

      if (windowActions[i] == specWindowAction) windowMenu.addSeparator();
    }

    windowMenu.addSeparator();

    Action[] specFrameActions =
        new Action[] {
          specFrameDisplayAction,
          specFramePrintAction,
          specFrameCaptureAction,
          specFrameOverlayAction,
          specFrameInfoAction,
          specFrameRefreshAction,
          specFrameRedrawAction,
          specFrameResizeGraphAction,
          specFrameClearAction,
          specFrameCloseAction
        };

    int specFrameMenuLength = specFrameActions.length;

    specFrameMenu = (JMenu) menuBar.add(new JMenu("Graph"));
    specFrameMenu.setMnemonic('G');

    for (int i = 0; i < specFrameMenuLength; i++) {
      menuItem = (JMenuItem) specFrameMenu.add(specFrameActions[i]);
      menuItem.setMargin(marginInsets);
      menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
      menuItem.setToolTipText((String) actionToTooltipMap.get(specFrameActions[i]));
      menuItem.setAccelerator((KeyStroke) actionToShortcutMap.get(specFrameActions[i]));

      menuItem.putClientProperty(
          "component.info", (String) actionToInfoMsgMap.get(specFrameActions[i]));
      menuItem.addMouseListener(statusMgr);
    }

    specFrameMenu.addSeparator();
    menuItem = new JMenuItem("About Spec Graph");
    menuItem.setMargin(marginInsets);
    menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
    menuItem.setActionCommand("aboutSpecGraph");
    menuItem.addActionListener(this);

    menuItem.putClientProperty("component.info", "About Spec Graph");
    menuItem.addMouseListener(statusMgr);

    specFrameMenu.add(menuItem);

    Action[] helpActions = new Action[] {helpContentsAction, aboutSpecAction};
    // number of menu items / commands
    int helpMenuLength = helpActions.length;

    helpMenu = (JMenu) menuBar.add(new JMenu("Help"));
    helpMenu.setMnemonic('H');

    for (int i = 0; i < helpMenuLength; i++) {
      if (helpActions[i] == aboutSpecAction) helpMenu.addSeparator();

      menuItem = (JMenuItem) helpMenu.add(helpActions[i]);
      menuItem.setMargin(marginInsets);
      menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
      menuItem.setToolTipText((String) actionToTooltipMap.get(helpActions[i]));

      menuItem.putClientProperty("component.info", (String) actionToInfoMsgMap.get(helpActions[i]));
      menuItem.addMouseListener(statusMgr);
    }
  }

  /** creates the tool bar */
  private void createToolBar() {
    JButton button;
    Insets marginInsets = new Insets(1, 1, 1, 1);

    // standardToolBar is not floatable ... to prevent repainting problems sometimes
    // standardToolBar.setFloatable (false);
    Action[] standardToolBarActions =
        new Action[] {
          specWindowAction,
          openSpecAction,
          openMultipleSpecAction,
          specManipulatorAction,
          specTranscriptAction,
          errorAction,
          tileAction,
          cascadeAction,
          minimizeAction,
          restoreAction,
          resizeAllGraphsAction,
          helpContentsAction,
          exitAction,
          aboutSpecAction
        };

    int standardToolBarLength = standardToolBarActions.length;

    String[] standardShortLabels =
        new String[] {
          "New", "Read in", "Multi Read", "Manipulator", "Transcript",
          "Tile", "Cascade", "Minimize", "Restore", "Resize All",
          "Help", "Exit", "About"
        };

    for (int i = 0; i < standardToolBarLength; i++) {
      // add an invisible component at the beginning of the standardToolBar
      if (i == 0) {
        standardToolBar.addSeparator();
      }
      // let aboutSpecAction be at the end
      else if (standardToolBarActions[i] == aboutSpecAction) {
        standardToolBar.add(Box.createGlue());
      }

      button = (JButton) standardToolBar.add(standardToolBarActions[i]);
      button.setMargin(marginInsets);
      // if (button.getIcon () == defaultImageIcon || button.getIcon () == null)
      // button.setText ( standardShortLabels [i] );
      // else
      button.setText(null);

      button.setHorizontalTextPosition(JButton.RIGHT);
      button.setToolTipText((String) actionToTooltipMap.get(standardToolBarActions[i]));

      button.putClientProperty(
          "component.info", (String) actionToInfoMsgMap.get(standardToolBarActions[i]));
      button.addMouseListener(statusMgr);

      // add separators
      if (standardToolBarActions[i] == openMultipleSpecAction
          || standardToolBarActions[i] == specTranscriptAction
          || standardToolBarActions[i] == errorAction
          || standardToolBarActions[i] == resizeAllGraphsAction
          || standardToolBarActions[i] == helpContentsAction
          || standardToolBarActions[i] == exitAction
          || standardToolBarActions[i] == aboutSpecAction) standardToolBar.addSeparator();
    }

    // graphToolBar is not floatable
    graphToolBar.setFloatable(false);
    Action[] graphToolBarActions =
        new Action[] {
          specFrameDisplayAction,
          specFramePrintAction,
          specFrameCaptureAction,
          specFrameOverlayAction,
          specFrameInfoAction,
          specFrameRefreshAction,
          specFrameRedrawAction,
          specFrameResizeGraphAction,
          specFrameClearAction,
          specFrameCloseAction
        };

    int graphToolBarLength = graphToolBarActions.length;

    String[] graphShortLabels =
        new String[] {
          "display", "print", "capture", "overlay", "info", "refresh", "redraw", "resize", "clear",
          "close"
        };

    for (int i = 0; i < graphToolBarLength; i++) {
      // add an invisible component at the beginning of the graphToolBar
      if (i == 0) {
        graphToolBar.addSeparator();
      }

      button = (JButton) graphToolBar.add(graphToolBarActions[i]);
      button.setMargin(marginInsets);
      // if (button.getIcon () == defaultImageIcon || button.getIcon () == null)
      // button.setText ( graphShortLabels [i] );
      // else
      button.setText(null);

      button.setHorizontalTextPosition(JButton.RIGHT);
      button.setToolTipText((String) actionToTooltipMap.get(graphToolBarActions[i]));

      button.putClientProperty(
          "component.info", (String) actionToInfoMsgMap.get(graphToolBarActions[i]));
      button.addMouseListener(statusMgr);

      // add a small component also at the end of the graphToolBar
      if (i == (graphToolBarLength - 1)) {
        graphToolBar.addSeparator();
      }
    }

    standardToolBar.setMaximumSize(standardToolBar.getSize());
    graphToolBar.setMaximumSize(graphToolBar.getSize());
  }

  /** creates the status bar */
  private void createStatusBar() {

    statusBar.setLayout(new BorderLayout());
    statusBar.add(statusPanel);

    statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    statusPanel.add(statusLabel);
    statusPanel.add(specDisplayedLabel);
    statusPanel.add(new JLabel("..."));
  }

  /**
   * enables or disables the action components for the desktop, based on if any spectrum frames are
   * still open, ... and the dimension of the spectrum it delegates to
   */
  public void enablePlotPanelActions() {
    int num = plotPanel.getAllFrames().length;
    if (num == 0) {
      tileAction.setEnabled(false);
      cascadeAction.setEnabled(false);
      minimizeAction.setEnabled(false);
      restoreAction.setEnabled(false);
      resizeAllGraphsAction.setEnabled(false);
    } else {
      tileAction.setEnabled(true);
      cascadeAction.setEnabled(true);
      minimizeAction.setEnabled(true);
      restoreAction.setEnabled(true);
      resizeAllGraphsAction.setEnabled(true);
    }
  }

  /**
   * enables or disables the action components for internal frames ... based on if there is a
   * drawable spectrum contained, ... and the dimension of the spectrum it delegates to
   */
  public void enableSpecFrameActions() {
    // int num = plotPanel.getAllFrames().length;
    // if (num == 0)
    //   selectedFrame = null;
    JInternalFrame internalFrame = plotPanel.getSelectedFrame();
    if (internalFrame instanceof SpectrumInternalFrame)
      selectedFrame = (SpectrumInternalFrame) internalFrame;
    else selectedFrame = null;

    if (selectedFrame == null) {
      specFrameDisplayAction.setEnabled(false);
      specFramePrintAction.setEnabled(false);
      specFrameCaptureAction.setEnabled(false);
      specFrameOverlayAction.setEnabled(false);
      specFrameInfoAction.setEnabled(false);
      specFrameRefreshAction.setEnabled(false);
      specFrameRedrawAction.setEnabled(false);
      specFrameResizeGraphAction.setEnabled(false);
      specFrameClearAction.setEnabled(false);
      specFrameCloseAction.setEnabled(false);
      return;
    }

    DrawableSpectrum drawSpectrum = selectedFrame.getDrawableSpectrum();

    if (drawSpectrum == null) {
      specFrameDisplayAction.setEnabled(true);
      specFramePrintAction.setEnabled(false);
      specFrameCaptureAction.setEnabled(false);
      specFrameOverlayAction.setEnabled(false);
      specFrameInfoAction.setEnabled(false);
      specFrameRefreshAction.setEnabled(true);
      specFrameRedrawAction.setEnabled(false);
      specFrameResizeGraphAction.setEnabled(false);
      specFrameClearAction.setEnabled(false);
      specFrameCloseAction.setEnabled(true);
    } else {
      specFrameDisplayAction.setEnabled(false);
      specFramePrintAction.setEnabled(Graph.isPrintingEnabled());
      specFrameCaptureAction.setEnabled(true);
      if (drawSpectrum.getSpectrum().getSpecDimension() == 1)
        specFrameOverlayAction.setEnabled(true);
      else specFrameOverlayAction.setEnabled(false);
      specFrameInfoAction.setEnabled(true);
      specFrameRefreshAction.setEnabled(true);
      specFrameRedrawAction.setEnabled(true);
      specFrameResizeGraphAction.setEnabled(true);
      specFrameClearAction.setEnabled(true);
      specFrameCloseAction.setEnabled(true);
    }
  }

  /** update the User interface when the Look and Feel is changed */
  private void updateSpecUI() {
    if (aboutSpecViewWindow != null) SwingUtilities.updateComponentTreeUI(aboutSpecViewWindow);
    if (window != null) SwingUtilities.updateComponentTreeUI(window);
    if (fileChooser != null) SwingUtilities.updateComponentTreeUI(fileChooser);
    if (spectrumManipulator != null)
      SwingUtilities.updateComponentTreeUI(spectrumManipulator.getDialog());
    if (spectrumTranscript != null) {
      SwingUtilities.updateComponentTreeUI(spectrumTranscript);
      SwingUtilities.updateComponentTreeUI(spectrumTranscript.errorDialog);
    }

    ViewerDialog.updateReusableDialogUIs();

    SwingUtilities.updateComponentTreeUI(DrawableSpectrum.InfoViewer.getContentPanel());
    SwingUtilities.updateComponentTreeUI(Spectrum.getSpecModifier().getContentPanel());
    SwingUtilities.updateComponentTreeUI(OverlaySpectrum.getContentPanel());
  }

  /** called to confirm if the application should be exited */
  protected void exitSpecView() {
    String msg = "Exit SpecView completely?";
    int value =
        JOptionPane.showConfirmDialog(window, msg, "Exit SpecView?", JOptionPane.YES_NO_OPTION);

    if (value == JOptionPane.YES_OPTION) System.exit(0);
  }

  /** called to resizeGraph the graphs in all the open frames */
  public void resizeAllGraphs() {
    try {
      setWaitCursor();
      JInternalFrame[] allFrames = plotPanel.getAllFrames();

      SpectrumInternalFrame specFrame;
      for (int i = 0; i < allFrames.length; i++) {
        if (!(allFrames[i] instanceof SpectrumInternalFrame)) continue;

        specFrame = (SpectrumInternalFrame) allFrames[i];
        specFrame.updateGraph();
      }

    } finally {
      setDefaultCursor();
    }
  }

  /** handles when the Look and Feel is changed */
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    String actionCommand = e.getActionCommand();

    // give information about the spec graphs
    if (actionCommand != null && actionCommand.equals("aboutSpecGraph")) {
      SpectrumInternalFrame.aboutSpecGraph();
      return;
    }

    try {
      setWaitCursor();

      if (source == metalMenuItem && metalMenuItem.isSelected()) {
        currentUI = "Cross Platform";
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        updateSpecUI();
      } else if (source == windowsMenuItem && windowsMenuItem.isSelected()) {
        currentUI = "Windows";
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        updateSpecUI();
      } else if (source == motifMenuItem && motifMenuItem.isSelected()) {
        currentUI = "CDE/Motif";
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        updateSpecUI();
      } else if (source == macMenuItem && macMenuItem.isSelected()) {
        currentUI = "Macintosh";
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.mac.MacLookAndFeel");
        updateSpecUI();
      }
    } catch (UnsupportedLookAndFeelException exc) {
      // it had to be one of the menuItem buttons
      JMenuItem menuItem = (JMenuItem) source;
      // Error - unsupported L&F
      System.err.println("Unsupported LookAndFeel: " + menuItem.getText());
      menuItem.setEnabled(false);
      // Set L&F to Metal
      try {
        currentUI = "Cross Platform";
        metalMenuItem.setSelected(true);
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        updateSpecUI();
      } catch (Exception exc2) {
        System.err.println("Could not load Cross Platform LookAndFeel: " + exc2);
      }
    } catch (Exception exc3) {
      System.err.println(exc3.getMessage());
    } finally {
      setDefaultCursor();
      standardToolBar.setMaximumSize(standardToolBar.getSize());
      graphToolBar.setMaximumSize(graphToolBar.getSize());
      window.repaint();
    }
  }

  /** methods from the internal frame listener class ... these manage the internalFrameMenu */
  public void internalFrameOpened(InternalFrameEvent e) {
    // System.out.println ("internal frame open event called ");
    SpectrumInternalFrame frame = (SpectrumInternalFrame) e.getSource();

    // do nothing if this frame has already been opened ...
    // and so the frame has a mapping already
    if (frameToActionMap.containsKey(frame)) return;

    // put a menu Item representing this frame on the menuBar
    this.new SpecFrameRadioButtonMenuItem(frame);

    // enable the relevant actions
    enablePlotPanelActions();
  }

  /** This does nothing for now ... work is handled in internalFrameClosed() */
  public void internalFrameClosing(InternalFrameEvent e) {}

  /**
   * This removes the menuItem assoc. with this frame, we also have to set the selectedFrame to null
   * since it was selected before it was closed, ... then if another frame is selected later, it
   * will be set to the selected frame
   */
  public void internalFrameClosed(InternalFrameEvent e) {
    // System.out.println ("internal frame closing  event called ");
    SpectrumInternalFrame frame = (SpectrumInternalFrame) e.getSource();

    JMenuItem menuItem = (JMenuItem) frameToActionMap.get(frame);

    if (windowMenu.getPopupMenu().isAncestorOf(menuItem) && menuItem != null)
      windowMenu.getPopupMenu().remove(menuItem);

    windowGroup.remove(menuItem);
    frameToActionMap.remove(frame);

    // the closed frame was selected first, so set selected to null
    selectedFrame = null;

    // plotPanel.unMaximizeAllFrames ();

  }

  /**
   * selects the menuitem associated with this frame on the menubar ... then enables the relevant
   * spectrumFrame Actions
   */
  public void internalFrameActivated(InternalFrameEvent e) {
    // System.out.println ("internal frame activated event called ");
    SpectrumInternalFrame frame = (SpectrumInternalFrame) e.getSource();
    JMenuItem menuItem = (JMenuItem) frameToActionMap.get(frame);

    if (menuItem != null && (!(menuItem.isSelected()))) menuItem.setSelected(true);

    selectedFrame = frame;

    // enable the relevant actions
    enableSpecFrameActions();
  }

  /**
   * deselects the menuitem associated with this frame on the menubar ... then enables the relevant
   * spectrumFrame Actions
   */
  public void internalFrameDeactivated(InternalFrameEvent e) {
    // System.out.println ("internal frame activated event called ");
    SpectrumInternalFrame frame = (SpectrumInternalFrame) e.getSource();
    JMenuItem menuItem = (JMenuItem) frameToActionMap.get(frame);

    if (menuItem != null) menuItem.setSelected(false);

    selectedFrame = null;

    // enable the relevant actions
    enableSpecFrameActions();
  }

  public void internalFrameIconified(InternalFrameEvent e) {}

  public void internalFrameDeiconified(InternalFrameEvent e) {}

  /**
   * method from the PropertyChangeListener ... resets the spec frame name in the menu if the
   * spectrum frame has been closed fully, enable or disable the actions on this specUI ...
   * (especially for plot panel and spectrum frames ) ****[[ cannot do this, because if the spectrum
   * is disposed, all its listeners are purged ]]
   */
  public void propertyChange(PropertyChangeEvent e) {
    Object source = e.getSource();
    String propertyName = e.getPropertyName();
    Object oldValue = e.getOldValue();
    Object newValue = e.getNewValue();

    if (propertyName.equals(JInternalFrame.TITLE_PROPERTY)) {
      SpectrumInternalFrame frame = (SpectrumInternalFrame) source;
      JMenuItem menuItem = (JMenuItem) frameToActionMap.get(frame);
      if (menuItem != null) menuItem.setText((String) newValue);
    }
    // else if (propertyName.equals (SpectrumInternalFrame.SPECFRAME_CLOSED_FULLY_PROPERTY) ) {
    // if the spectrum frame has been closed fully, enable all the actions
    // boolean b = ((Boolean) newValue).booleanValue ();
    // if ( b )
    // enableActions ();
    // }

  }

  /** class manages the actions for when the menuItem is selected */
  private class SpecFrameRadioButtonMenuItem extends JRadioButtonMenuItem
      implements ActionListener {
    public final SpectrumInternalFrame frame;

    private SpecFrameRadioButtonMenuItem() {
      this(null);
    } // cannot be instantiated this way

    protected SpecFrameRadioButtonMenuItem(SpectrumInternalFrame aFrame) {
      super(aFrame.getTitle().trim());

      this.frame = aFrame;

      frameToActionMap.put(frame, this);
      addActionListener(this);
      windowMenu.add(this);
      windowGroup.add(this);

      this.setSelected(frame.isSelected());
    }

    /** just select the frame when this menu item is selected */
    public void actionPerformed(ActionEvent evt) {
      plotPanel.getDesktopManager().activateFrame(frame);
    }
  }
}
