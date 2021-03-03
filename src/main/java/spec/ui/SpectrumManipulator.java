package spec.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import spec.io.SpecFormat;
import spec.io.SpectrumReader;
import spec.io.SpectrumWriter;
import spec.lib.Misc;
import spec.lib.graph.Graph;
import spec.lib.ui.ViewerDialog;
import spec.spectrum.DrawableSpectrum;
import spec.spectrum.Spectrum;
import spec.spectrum.SpectrumException;

/**
 * class to handle Choosing and manipulating spectra ... It references a JDialog which is non-modal
 * ... ... and has methods to show and hide the dialog It also has a panel in which it adds all the
 * stuff
 *
 * <p>A spectrum cannot be opened from the same file twice, ... except it is closed first
 */
public class SpectrumManipulator implements ActionListener {
  /** vector of spectra */
  protected DefaultListModel spectra = new DefaultListModel();

  protected JPanel contentPane = new JPanel();

  protected JPanel buttonPane = new JPanel();

  protected JList spectrumList = new JList(spectra);

  protected JButton openButton = new JButton("Read");
  protected JButton writeButton = new JButton("Write");
  protected JButton closeButton = new JButton("Close");
  protected JButton displayButton = new JButton("Display");
  protected JButton displayMultipleButton = new JButton("Display (multiple)");
  protected JButton editButton = new JButton("Edit");
  protected JButton infoButton = new JButton("Information");

  /**
   * holds a mapping of the drawableSpectrum to the SpectrumInternalFrames they are displayed in use
   * HashMap ... so we can map to null values
   */
  protected Map spectrumToFrameMap = new HashMap();

  /**
   * holds a mapping of the drawableSpectrum to the Files they were opened from use HashMap ... so
   * we can map to null values
   */
  protected Map spectrumToFilenameMap = new HashMap();

  // holds a reference to the plotPanel and FileChooser on which stuff is plotted & files are
  // selected
  private PlotPanel plotPanel;
  private SpectrumFileChooser fileChooser;

  private SpecUI.MDI specUI;

  // controls for the dialog displaying this chooser
  protected JDialog manipulateDialog;
  private String closeDialogButtonText = "CLOSE MANIPULATE DIALOG";
  private JButton closeDialogButton = new JButton(closeDialogButtonText);

  private SpectrumManipulator() {} // no-one can instantiate this

  public SpectrumManipulator(SpecUI.MDI specUI, PlotPanel plotPanel) {
    this(specUI, plotPanel, specUI.getSpectrumFileChooser());
  }

  public SpectrumManipulator(
      SpecUI.MDI specUI, PlotPanel plotPanel, SpectrumFileChooser fileChooser) {
    super();

    openButton.setToolTipText("Read Spectrum (from a file)");
    openButton.setActionCommand("readSpec");
    openButton.addActionListener(this);
    writeButton.setToolTipText("Write Spectrum (to a file)");
    writeButton.setActionCommand("writeSpec");
    writeButton.addActionListener(this);
    closeButton.setToolTipText("Close Spectrum (and its graphs)");
    closeButton.setActionCommand("closeSpec");
    closeButton.addActionListener(this);
    displayButton.setToolTipText("Display (graph) Spectrum");
    displayButton.setActionCommand("displaySpec");
    displayButton.addActionListener(this);
    displayMultipleButton.setToolTipText("Display (graph) multiple Spectra");
    displayMultipleButton.setActionCommand("displayMultipleSpec");
    displayMultipleButton.addActionListener(this);
    editButton.setToolTipText("View / Edit Spectrum Attributes");
    editButton.setActionCommand("editSpec");
    editButton.addActionListener(this);
    infoButton.setToolTipText("View Information on Spectrum & Graph");
    infoButton.setActionCommand("viewInfo");
    infoButton.addActionListener(this);

    spectrumList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    // set the layout of the contentPane container
    updateLayout();

    this.plotPanel = plotPanel;
    this.fileChooser = fileChooser;

    this.specUI = specUI;

    updateManipulateDialog();
  }

  /** performs the layout for this component */
  private void updateLayout() {
    buttonPane.setLayout(new GridLayout(0, 1));
    buttonPane.add(openButton);
    buttonPane.add(writeButton);
    buttonPane.add(closeButton);
    buttonPane.add(displayButton);
    buttonPane.add(displayMultipleButton);
    buttonPane.add(editButton);
    buttonPane.add(infoButton);

    contentPane.setLayout(new BorderLayout());
    contentPane.add(new JScrollPane(spectrumList), BorderLayout.WEST);
    contentPane.add(buttonPane, BorderLayout.EAST);
  }

  /** update the dialog for the manipulation */
  private void updateManipulateDialog() {
    // make it non-modal ... let's see how it will be
    manipulateDialog =
        new JDialog(specUI.getMainWindow(), "Spectrum Manipulator Dialog", false) {
          // instance initializer
          {
            if (!(isResizable())) setResizable(true);
            if (isVisible()) setVisible(false);

            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel closeDialogPanel = new JPanel();
            JPanel southPanel = new JPanel();

            closeDialogPanel.add(closeDialogButton);
            southPanel.setLayout(new BorderLayout());
            southPanel.add(new JSeparator(), BorderLayout.NORTH);
            southPanel.add(closeDialogPanel, BorderLayout.CENTER);

            Container cont = getContentPane();
            if (!(cont.getLayout() instanceof BorderLayout)) cont.setLayout(new BorderLayout());
            cont.add(southPanel, BorderLayout.SOUTH);
          }

          public void processWindowEvent(WindowEvent e) {
            super.processWindowEvent(e);
            if (e.getID() == WindowEvent.WINDOW_CLOSING) {
              closeDialogButton.doClick();
            }
          }
        };

    closeDialogButton.setToolTipText("Close this dialog");
    closeDialogButton.addActionListener(this);
  }

  /** return the dialog that is used by the SpectrumManipulator */
  public JDialog getDialog() {
    return manipulateDialog;
  }

  /** get the spec UI */
  public SpecUI getSpecUI() {
    return specUI;
  }

  /** set the file chooser for this spectrum manipulator */
  public void setSpectrumFileChooser(SpectrumFileChooser fileChooser) {
    this.fileChooser = fileChooser;
  }

  /** method to add a drawSpectrum to the spectrum list */
  public void addDrawableSpectrum(DrawableSpectrum drawSpectrum) {
    // add this element if it is not already contained in this listModel
    if (!(spectra.contains(drawSpectrum))) spectra.addElement(drawSpectrum);
    spectrumList.setSelectedValue(drawSpectrum, true);
    spectrumList.repaint();
  }

  /**
   * maps a drawSpectrum to a frame that contains its graph ... *** ONLY IF THE FRAME CONTAINS ITS
   * GRAPH ***
   */
  protected void mapSpectrumToFrame(DrawableSpectrum drawSpectrum, SpectrumInternalFrame frame) {
    // check if this frame contains the graph
    if (frame.getContentPane().isAncestorOf(drawSpectrum.getGraph()))
      spectrumToFrameMap.put(drawSpectrum, frame);
  }

  /**
   * maps a drawSpectrum to a filename it was opened from ... *** ONLY IF THE FRAME CONTAINS ITS
   * GRAPH ***
   */
  protected void mapSpectrumToFilename(DrawableSpectrum drawSpectrum, File file) {
    spectrumToFilenameMap.put(drawSpectrum, file.getAbsolutePath());
  }

  /**
   * removes the mapping of a draw spectrum to the frame, ... *** ONLY IF THE FRAME IT WAS
   * PREVIOUSLY MAPPED TO NO LONGER CONTAINS IT ***
   */
  protected void unMapSpectrumToFrame(DrawableSpectrum drawSpectrum) {
    SpectrumInternalFrame frame = (SpectrumInternalFrame) spectrumToFrameMap.get(drawSpectrum);
    // put the mapping if the previous frame it was mapped to is no longer the frame
    // that contained it
    if (frame != null && !(frame.getContentPane().isAncestorOf(drawSpectrum.getGraph())))
      spectrumToFrameMap.remove(drawSpectrum);
  }

  /** removes the mapping of a draw spectrum to the filename, */
  protected void unMapSpectrumToFilename(DrawableSpectrum drawSpectrum) {
    String name = (String) spectrumToFilenameMap.get(drawSpectrum);
    // put the mapping if the previous frame it was mapped to is no longer the frame
    // that contained it
    if (name != null) spectrumToFilenameMap.remove(drawSpectrum);
  }

  /** get the SpectrumInternalFrame associated with this drawable spectrum */
  public SpectrumInternalFrame getAssocFrame(DrawableSpectrum drawSpectrum) {
    return ((SpectrumInternalFrame) spectrumToFrameMap.get(drawSpectrum));
  }

  /** get the filename associated with this drawable spectrum */
  public String getAssocFilename(DrawableSpectrum drawSpectrum) {
    return ((String) spectrumToFilenameMap.get(drawSpectrum));
  }

  /**
   * given a drawable spectrum, it checks for all the SpectrumInternalFrames contained in the
   * PlotPanel that have a reference to the drawable spectrum, and closes them
   */
  public void closeDrawableSpectrum(DrawableSpectrum drawSpec) {
    if (drawSpec == null || plotPanel == null) return;

    // restore all frames to non-maximized size
    // plotPanel.unMaximizeAllFrames ();

    JInternalFrame[] allFrames = plotPanel.getAllFrames();
    SpectrumInternalFrame specFrame;

    for (int i = 0; i < allFrames.length; i++) {
      specFrame = (SpectrumInternalFrame) allFrames[i];
      if (specFrame.getDrawableSpectrum() != null
          && specFrame.getDrawableSpectrum().equals(drawSpec)) {
        specFrame.clear(false);
        specFrame.close();
      }
    }
  }

  /** method to remove a drawSpectrum from the spectrum list */
  public void removeDrawableSpectrum(DrawableSpectrum drawSpectrum) {
    // remove drawSpectrum if it is contained in this listModel
    if (spectra.contains(drawSpectrum)) spectra.removeElement(drawSpectrum);
    spectrumList.repaint();

    unMapSpectrumToFrame(drawSpectrum);
    unMapSpectrumToFilename(drawSpectrum);
  }

  /** methods to show and hide the manipulate dialog */
  public void manipulateSpectrum() {
    if (!(manipulateDialog.isVisible())) {
      spectrumList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      manipulateDialog.setLocationRelativeTo(specUI.getMainWindow());
      Container c = manipulateDialog.getContentPane();
      if (!(c.isAncestorOf(contentPane))) c.add(contentPane, BorderLayout.CENTER);

      manipulateDialog.pack();
      manipulateDialog.show();
    } else {
      manipulateDialog.setVisible(false);
    }
  }

  /** choose a spectrum from the list */
  public DrawableSpectrum chooseSpectrum() {
    DrawableSpectrum[] array = chooseSpectra(false);
    if (array == null || array.length < 1) return null;
    else return (array[0]);
  }

  /** select multiple spectra from the list */
  public DrawableSpectrum[] chooseSpectra() {
    return (chooseSpectra(true));
  }

  /**
   * select an array of spectra from the list ... argument dictates if multiple or single selection
   */
  public DrawableSpectrum[] chooseSpectra(boolean isMultipleSelection) {
    try {
      if (isMultipleSelection)
        spectrumList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      else spectrumList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      // disable the buttons below, for writing, closing or displaying spectra
      writeButton.setEnabled(false);
      closeButton.setEnabled(false);
      displayButton.setEnabled(false);
      displayMultipleButton.setEnabled(false);

      int selection = ViewerDialog.showOkCancelDialog(contentPane, "Choose a spectrum");

      switch (selection) {
        case ViewerDialog.OK_OPTION:
          Object[] array = spectrumList.getSelectedValues();
          DrawableSpectrum[] drawSpectra = new DrawableSpectrum[array.length];
          for (int i = 0; i < drawSpectra.length; i++) {
            drawSpectra[i] = (DrawableSpectrum) array[i];
          }
          return drawSpectra;
        case ViewerDialog.CANCEL_OPTION:
        default:
          break;
      }

      return null;
    } finally {
      // enable the buttons below, for writing, closing or displaying spectra
      writeButton.setEnabled(true);
      closeButton.setEnabled(true);
      displayButton.setEnabled(true);
      displayMultipleButton.setEnabled(true);

      Container c = manipulateDialog.getContentPane();
      c.add(contentPane, BorderLayout.CENTER);
      manipulateDialog.pack();
      manipulateDialog.repaint();
    }
  }

  /** method to open a spectrum */
  public DrawableSpectrum openSpectrum() {
    return (openSpectrum(fileChooser));
  }

  /**
   * read a drawable spectrum from a file, puts it into spectrumList ... and returns the read
   * drawable spectrum preserve the selection mode of the file chooser after using it, since the
   * selection mode must be single selection for this method
   */
  public DrawableSpectrum openSpectrum(JFileChooser fileChooser) {
    // first set the selected file to null ...
    boolean selectionMode = fileChooser.isMultiSelectionEnabled();
    if (selectionMode) fileChooser.setMultiSelectionEnabled(false);

    fileChooser.setSelectedFile(null);

    File file = specUI.selectReadableFile();
    // return if no file was selected
    if (file == null) return null;

    // get the spectrum
    Spectrum tempSpectrum;
    DrawableSpectrum drawSpec;

    try {
      String filePath = file.getAbsolutePath();

      if (spectrumToFilenameMap.containsValue(filePath)) {
        Object[] aSpecArray = (Misc.getKeysForValue(spectrumToFilenameMap, filePath));
        String aSpecName = ((DrawableSpectrum) aSpecArray[0]).getSpectrum().getSpecName();

        throw new Exception(
            "A spectrum: "
                + aSpecName
                + "\n    has already been opened from this file"
                + file.getName());
      }

      tempSpectrum = SpectrumReader.read(file);
      drawSpec = getDrawableSpectrum(tempSpectrum);
      addDrawableSpectrum(drawSpec);

      mapSpectrumToFilename(drawSpec, file);
    }
    // catch SpecFormat.IOException from reading the file,
    // IOException from when dimension is trying to be got
    // or any other exception that is thrown ... and return
    catch (Exception e) {
      drawSpec = null;
      // e.printStackTrace () ;
      JOptionPane.showMessageDialog(
          fileChooser,
          e.getMessage(),
          "Error reading or displaying spectrum",
          JOptionPane.ERROR_MESSAGE);
    }

    if (selectionMode) fileChooser.setMultiSelectionEnabled(selectionMode);

    return (drawSpec);
  }

  /** static method to open multiple spectra */
  public DrawableSpectrum[] openSpectra() {
    return (openSpectra(fileChooser));
  }

  /**
   * reads some drawable spectrum from files, puts them into spectrumList and returns an array of
   * the read drawable spectra preserve the selection mode of the file chooser after using it, since
   * the selection mode must be multi-selection for this method
   */
  public DrawableSpectrum[] openSpectra(JFileChooser fileChooser) {
    // first set the selected file to null ...
    boolean selectionMode = fileChooser.isMultiSelectionEnabled();
    if (!(selectionMode)) fileChooser.setMultiSelectionEnabled(true);

    fileChooser.setSelectedFile(null);
    int returnVal = fileChooser.showOpenDialog(fileChooser);
    if (returnVal != JFileChooser.APPROVE_OPTION) return null;

    // return if no file was selected
    File[] files = fileChooser.getSelectedFiles();
    if (files == null) {
      JOptionPane.showMessageDialog(
          fileChooser, "No file was selected", "Error reading spectrum", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    // get the spectrum
    Spectrum tempSpectrum;
    DrawableSpectrum drawSpec;
    java.util.List drawSpecs = new ArrayList();

    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      try {
        String filePath = file.getAbsolutePath();

        if (spectrumToFilenameMap.containsValue(filePath)) {
          Object[] aSpecArray = (Misc.getKeysForValue(spectrumToFilenameMap, filePath));
          String aSpecName = ((DrawableSpectrum) aSpecArray[0]).getSpectrum().getSpecName();

          throw new Exception(
              "A spectrum: "
                  + aSpecName
                  + "\n    has already been opened from this file"
                  + file.getName());
        }

        tempSpectrum = SpectrumReader.read(file);
        drawSpec = getDrawableSpectrum(tempSpectrum);
        addDrawableSpectrum(drawSpec);

        mapSpectrumToFilename(drawSpec, file);
        drawSpecs.add(drawSpec);
      }
      // catch SpecFormat.IOException from reading the file,
      // IOException from when dimension is trying to be got
      // or any other exception that is thrown ... and return
      catch (Exception e) {
        JOptionPane.showMessageDialog(
            fileChooser,
            e.getMessage(),
            "Error reading or displaying spectrum",
            JOptionPane.ERROR_MESSAGE);
      }
    }

    // cast them back to drawable spectra
    Object[] array = drawSpecs.toArray();
    DrawableSpectrum[] drawSpectra = new DrawableSpectrum[array.length];
    for (int i = 0; i < drawSpectra.length; i++) {
      drawSpectra[i] = (DrawableSpectrum) array[i];
    }

    if (!(selectionMode)) fileChooser.setMultiSelectionEnabled(selectionMode);

    return (drawSpectra);
  }

  /**
   * writes a selected spectrum to a file
   *
   * @Deprecated Jan 6, 1999 ... new method uses the file filter to decide the format
   */
  private void writeSpectrum_1_6_99(Spectrum spectrum) {
    if (spectrum == null) return;
    SpecFormat specFormat = SpecFormat.chooseFormat();
    if (specFormat == null) return;

    // first set the selected file to null ...
    fileChooser.setSelectedFile(null);
    int returnVal = fileChooser.showSaveDialog(fileChooser);
    if (returnVal != JFileChooser.APPROVE_OPTION) return;

    // return if no file was selected
    File file = fileChooser.getSelectedFile();
    if (file == null) return;

    if (file.exists()) {
      // if file exists, get confirmation first
      int value =
          JOptionPane.showConfirmDialog(
              contentPane,
              "The file, " + file.getName() + ", exists, ... overwrite it???",
              "Overwrite file?",
              JOptionPane.OK_CANCEL_OPTION);

      if (value != JOptionPane.OK_OPTION) return;
    }

    try {
      boolean wasWritten = SpectrumWriter.write(spectrum, file, specFormat);
      // boolean wasWritten = specFormat.write (file, spectrum);
      if (!(wasWritten))
        throw new SpecFormat.IOException("The spectrum was not successfully written");
    } catch (SpecFormat.IOException ex) {
      JOptionPane.showMessageDialog(
          contentPane, ex.getMessage(), "Error writing spectrum", JOptionPane.ERROR_MESSAGE);
      return;
    }
  }

  /** writes a selected spectrum to a file */
  public void writeSpectrum(Spectrum spectrum) {
    if (spectrum == null) return;

    // first set the selected file to null ...
    fileChooser.setSelectedFile(null);
    int returnVal = fileChooser.showSaveDialog(fileChooser);
    if (returnVal != JFileChooser.APPROVE_OPTION) return;

    SpecFormat specFormat = null;

    javax.swing.filechooser.FileFilter filter = fileChooser.getFileFilter();
    if (filter instanceof SpectrumFileChooser.Filter) {
      String[] ext = ((SpectrumFileChooser.Filter) filter).getExtensions();
      if (ext != null && ext[0] != null) specFormat = SpecFormat.getFormatFromExtension(ext[0]);
    }

    // return if no file was selected
    File file = fileChooser.getSelectedFile();

    if (specFormat == null) {
      JOptionPane.showMessageDialog(
          contentPane,
          "A Spectrum Format was not selected",
          "No spectrum Format selected",
          JOptionPane.ERROR_MESSAGE);
      return;
    } else if (file == null) {
      JOptionPane.showMessageDialog(
          contentPane, "No file was selected", "No file selected", JOptionPane.ERROR_MESSAGE);
      return;
    }
    // if file exists, get confirmation first
    else if (file.exists()) {
      int value =
          JOptionPane.showConfirmDialog(
              contentPane,
              "The file, " + file.getName() + ", exists, ... overwrite it???",
              "Overwrite file?",
              JOptionPane.OK_CANCEL_OPTION);

      if (value != JOptionPane.OK_OPTION) return;
    }

    try {
      boolean wasWritten = SpectrumWriter.write(spectrum, file, specFormat);
      // boolean wasWritten = specFormat.write (file, spectrum);
      if (!(wasWritten))
        throw new SpecFormat.IOException("The spectrum was not successfully written");
    } catch (SpecFormat.IOException ex) {
      JOptionPane.showMessageDialog(
          contentPane, ex.getMessage(), "Error writing spectrum", JOptionPane.ERROR_MESSAGE);
      return;
    }
  }

  /** given a spectrum, it constructs a drawable spectrum for it and returns it */
  public DrawableSpectrum getDrawableSpectrum(Spectrum aSpectrum) throws SpectrumException {
    Graph graph;
    DrawableSpectrum drawSpectrum;

    int dimension = aSpectrum.getSpecDimension();

    // display the spectrum
    switch (dimension) {
      case 1:
        graph = new Graph.XtoY();

        Spectrum.OneDim _spectrum = (Spectrum.OneDim) aSpectrum;
        Graph.XtoY _graph = (Graph.XtoY) graph;

        drawSpectrum = new DrawableSpectrum.OneDim(_spectrum, _graph, this);
        drawSpectrum.draw();
        break;
      case 2:
        graph = new Graph.XYtoZ();

        Spectrum.TwoDim __spectrum = (Spectrum.TwoDim) aSpectrum;
        Graph.XYtoZ __graph = (Graph.XYtoZ) graph;

        drawSpectrum = new DrawableSpectrum.TwoDim(__spectrum, __graph, this);
        drawSpectrum.draw();
        break;
      default:
        throw new SpectrumException("The spectrum dimension is not recognized");
    }

    return drawSpectrum;
  }

  protected void displaySpec() {
    DrawableSpectrum drawSpectrum = (DrawableSpectrum) spectrumList.getSelectedValue();
    if (drawSpectrum == null) return;
    // display this spectrum in a new frame if not already displayed
    // ... or just maximize the frame it is mapped to
    // this is because the graph member of a DrawableSpectrum can only be displayed in one comtainer
    SpectrumInternalFrame specFrame = (SpectrumInternalFrame) spectrumToFrameMap.get(drawSpectrum);
    if (specFrame == null) {
      // create an internal frame for the drawable spectrum, add it to a desktop and
      // ... passes it a reference to the SpectrumManipulator, and map it to this frame
      try {
        specFrame = PlotPanel.newSpecFrame(specUI, plotPanel, drawSpectrum, this);
      } catch (PlotPanel.PlotPanelException ppe) {
        JOptionPane.showMessageDialog(
            contentPane, ppe.getMessage(), "Error displaying spectrum", JOptionPane.ERROR_MESSAGE);
      }
    } else {
      try {
        plotPanel.unMaximizeAllFrames();
        if (specFrame.isIcon()) specFrame.setIcon(false);
        specFrame.toFront();
        specFrame.setSelected(true);
      } catch (java.beans.PropertyVetoException ex) {
      }
    }
  }

  protected void displayMultipleSpec() {
    DrawableSpectrum[] drawSpectra = chooseSpectra();
    if (drawSpectra == null) return;

    DrawableSpectrum drawSpectrum;
    SpectrumInternalFrame specFrame;
    for (int i = 0; i < drawSpectra.length; i++) {
      drawSpectrum = drawSpectra[i];
      // display this spectrum in a new frame if not already displayed
      // ... or just maximize the frame it is mapped to
      // this is because the graph member of a DrawableSpectrum can only be displayed in one
      // comtainer
      specFrame = (SpectrumInternalFrame) spectrumToFrameMap.get(drawSpectrum);
      if (specFrame == null) {
        // create an internal frame for the drawable spectrum, add it to a desktop and
        // ... passes it a reference to the SpectrumManipulator, and map it to this frame
        try {
          specFrame = PlotPanel.newSpecFrame(specUI, plotPanel, drawSpectrum, this);
        } catch (PlotPanel.PlotPanelException ppe) {
          JOptionPane.showMessageDialog(
              contentPane,
              ppe.getMessage(),
              "Error displaying spectrum",
              JOptionPane.ERROR_MESSAGE);
        }
      } else {
        try {
          plotPanel.unMaximizeAllFrames();
          if (specFrame.isIcon()) specFrame.setIcon(false);
          specFrame.toFront();
          specFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException ex) {
        }
      }
    }
  }

  /** Handles the actions ... from ActionListener Interface */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();

    if (e.getSource() == closeDialogButton) {
      manipulateDialog.setVisible(false);
    } else if (command.equals("readSpec")) {
      this.openSpectrum();
    } else if (command.equals("writeSpec")) {
      DrawableSpectrum drawSpectrum = (DrawableSpectrum) spectrumList.getSelectedValue();
      if (drawSpectrum == null) return;

      this.writeSpectrum(drawSpectrum.getSpectrum());
    } else if (command.equals("closeSpec")) {
      DrawableSpectrum drawSpectrum = (DrawableSpectrum) spectrumList.getSelectedValue();
      if (drawSpectrum == null) return;
      closeDrawableSpectrum(drawSpectrum);
      removeDrawableSpectrum(drawSpectrum);
    } else if (command.equals("displaySpec")) {
      displaySpec();
    } else if (command.equals("displayMultipleSpec")) {
      displayMultipleSpec();
    } else if (command.equals("editSpec")) {
      DrawableSpectrum drawSpectrum = (DrawableSpectrum) spectrumList.getSelectedValue();
      if (drawSpectrum == null) return;
      String oldName = drawSpectrum.getSpectrum().getSpecName().trim();

      drawSpectrum.getSpectrum().editSpectrum();
      spectrumList.repaint();
    } else if (command.equals("viewInfo")) {
      DrawableSpectrum drawSpectrum = (DrawableSpectrum) spectrumList.getSelectedValue();
      if (drawSpectrum == null) return;

      drawSpectrum.info();
    }
  }
}
