package spec.spectrum;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import spec.lib.graph.DatasetXtoY;
import spec.lib.graph.DatasetXtoYRenderer;
import spec.lib.graph.Graph;
import spec.lib.ui.ViewerDialog;
import spec.ui.SpecUI;
import spec.ui.SpectrumInternalFrame;
import spec.ui.SpectrumManipulator;

/**
 * class to manage overlaying spectra The OvelaySpectrum class has only one dialog and buttons there
 * is always a reference to the current overlay spectrum which ... the buttons act on
 */
public final class OverlaySpectrum {
  public static final JPanel contentPane = new JPanel();

  protected static JPanel buttonPane = new JPanel();

  protected static JButton addButton = new JButton("Add");
  protected static JButton deleteButton = new JButton("Delete");
  protected static JButton addMultipleButton = new JButton("Add (Multiple)");
  protected static JButton deleteMultipleButton = new JButton("Delete (Multiple)");
  protected static JButton infoButton = new JButton("Information");

  private static JScrollPane spectrumScroller = new JScrollPane();

  protected static OverlaySpectrum currentOverlaySpectrum;

  protected static ActionListener actionListener;

  /** vector of spectra */
  protected DefaultListModel spectra = new DefaultListModel();

  protected JList spectrumList = new JList(spectra);

  protected Map spectrumToDatasetMap = new HashMap();

  private DrawableSpectrum.OneDim drawSpectrum;
  private SpectrumManipulator spectrumManipulator;
  private SpecUI specUI;

  static {
    actionListener = new OverlayActor();

    addButton.setToolTipText("Get a spectrum to overlay");
    addButton.setActionCommand("addSpectrum");
    addButton.addActionListener(actionListener);

    deleteButton.setToolTipText("Delete the selected overlayed spectrum");
    deleteButton.setActionCommand("deleteSpectrum");
    deleteButton.addActionListener(actionListener);

    addMultipleButton.setToolTipText("Get some spectra to overlay (multiple selection)");
    addMultipleButton.setActionCommand("addSpectra");
    addMultipleButton.addActionListener(actionListener);

    deleteMultipleButton.setToolTipText(
        "Delete the selected overlayed spectra (multiple selection)");
    deleteMultipleButton.setActionCommand("deleteSpectra");
    deleteMultipleButton.addActionListener(actionListener);

    infoButton.setEnabled(false);

    // set the layout of the contentPane container
    updateLayout();
  }

  public OverlaySpectrum(DrawableSpectrum.OneDim drawSpectrum) {
    super();

    this.drawSpectrum = drawSpectrum;
    this.spectrumManipulator = drawSpectrum.getSpectrumManipulator();
    this.specUI = spectrumManipulator.getSpecUI();

    spectrumList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }

  /** get the content panel */
  public static Component getContentPanel() {
    return contentPane;
  }

  /** performs the layout for this component */
  private static void updateLayout() {
    buttonPane.setLayout(new GridLayout(0, 1));
    buttonPane.add(addButton);
    buttonPane.add(deleteButton);
    buttonPane.add(addMultipleButton);
    buttonPane.add(deleteMultipleButton);
    buttonPane.add(infoButton);

    contentPane.setLayout(new BorderLayout());
    contentPane.add(buttonPane, BorderLayout.EAST);
    contentPane.add(spectrumScroller, BorderLayout.CENTER);
  }

  /** method to overlay a spectrum on another spectrum */
  private void addSpectrum() {
    SpectrumInternalFrame frame = spectrumManipulator.getAssocFrame(drawSpectrum);

    // only one dimensional spectra can be overlayed
    if (drawSpectrum.getSpectrum().getSpecDimension() != 1) {
      JOptionPane.showMessageDialog(
          contentPane,
          "Only one dimensional spectra can be overlaid",
          "Error overlaying spectrum",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      specUI.setWaitCursor();
      // ensure that the frame is not iconified
      try {
        if (frame.isIcon()) frame.setIcon(false);
      } catch (java.beans.PropertyVetoException e) {
      }

      DrawableSpectrum aDrawSpectrum = spectrumManipulator.chooseSpectrum();
      if (aDrawSpectrum == null) return;
      // you cannot overlay a spectrum onto itself
      if (aDrawSpectrum == drawSpectrum) {
        JOptionPane.showMessageDialog(
            frame,
            "A spectrum cannot be overlaid on itself",
            "Error overlaying spectrum",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      // overlayed spectrum must have the same dimension as the spectrum it is overlayed on
      // only one dimensional spectra can be overlaid
      int dimension = aDrawSpectrum.getSpectrum().getSpecDimension();
      if (dimension != 1) {
        JOptionPane.showMessageDialog(
            frame,
            "Only one dimensional spectra can be overlaid",
            "Error overlaying spectrum",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      Spectrum.OneDim spectrum1 = (Spectrum.OneDim) aDrawSpectrum.getSpectrum();

      // if the spectrum is already overlaid, return
      if (spectrumToDatasetMap.containsValue(spectrum1) || spectra.contains(spectrum1)) {
        JOptionPane.showMessageDialog(
            frame,
            "The selected spectrum is already overlaid here",
            "Error overlaying spectrum",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      int specShape = spectrum1.getSpecChannel().getSpecShape();

      int[] y = spectrum1.getSpecChannel().getSpecCount();
      double[] yError = spectrum1.getSpecChannel().getSpecUncertainty();
      int[] x = new int[specShape];
      // for X axis, remember: first value in specCounts refers to the first channel and so on
      for (int i = 0; i < specShape; i++) {
        x[i] = i + 1;
      }

      // set up dataset and graph
      Point2D.Float optionalStartPoint = new Point2D.Float(0.0f, 0.0f);
      DatasetXtoY.Integer dataset1 =
          new DatasetXtoY.Integer(
              x, y, DatasetXtoYRenderer.VERTICAL_HORIZONTAL, optionalStartPoint);
      dataset1.setErrorInY(yError, DatasetXtoY.ERROR_DUPLICATE);
      dataset1.setShowError(false);
      dataset1.setName(spectrum1.getSpecName());
      dataset1.setAxisX(drawSpectrum.getAxisX());
      dataset1.setAxisY(drawSpectrum.getAxisY());

      Graph.XtoY aGraph = (Graph.XtoY) drawSpectrum.getGraph();
      aGraph.attachDataset(dataset1);
      aGraph.updateAll();
      aGraph.repaint();

      // add the spectrum to the JList and to the spectrumToDatasetMap
      drawSpectrum.overlaidSpectraList.add(spectrum1);
      spectra.addElement(spectrum1);
      spectrumList.setSelectedValue(spectrum1, true);
      spectrumList.repaint();
      spectrumToDatasetMap.put(spectrum1, dataset1);

    } catch (Exception exc) {
      JOptionPane.showMessageDialog(
          frame, exc.getMessage(), "Error overlaying spectrum", JOptionPane.ERROR_MESSAGE);
    } finally {
      specUI.setDefaultCursor();
    }
  }

  /** delete an overlaid spectrum */
  private void deleteSpectrum() {
    Spectrum.OneDim spectrum = (Spectrum.OneDim) spectrumList.getSelectedValue();

    if (spectrum == null) return;

    DatasetXtoY.Integer dataset = (DatasetXtoY.Integer) spectrumToDatasetMap.get(spectrum);
    Graph.XtoY graph = (Graph.XtoY) drawSpectrum.getGraph();
    graph.detachDataset(dataset);
    graph.updateAll();
    graph.repaint();

    drawSpectrum.overlaidSpectraList.remove(spectrum);
    spectra.removeElement(spectrum);
    spectrumList.repaint();
    spectrumToDatasetMap.remove(spectrum);
  }

  /** method to overlay a spectrum on another spectrum ... not yet implemented */
  private void addSpectra() {
    SpectrumInternalFrame frame = spectrumManipulator.getAssocFrame(drawSpectrum);

    // only one dimensional spectra can be overlayed
    if (drawSpectrum.getSpectrum().getSpecDimension() != 1) {
      JOptionPane.showMessageDialog(
          contentPane,
          "Only one dimensional spectra can be overlaid",
          "Error overlaying spectrum",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    Graph.XtoY theGraph = (Graph.XtoY) drawSpectrum.getGraph();
    try {
      specUI.setWaitCursor();
      // ensure that the frame is not iconified
      try {
        if (frame.isIcon()) frame.setIcon(false);
      } catch (java.beans.PropertyVetoException e) {
      }

      DrawableSpectrum[] drawSpectra = spectrumManipulator.chooseSpectra();
      if (drawSpectra == null) return;

      DatasetXtoY.Integer dataset1;
      Point2D.Float optionalStartPoint = new Point2D.Float(0.0f, 0.0f);

      for (int i = 0; i < drawSpectra.length; i++) {
        try {
          // you cannot overlay a spectrum onto itself
          if (drawSpectra[i] == drawSpectrum) {
            JOptionPane.showMessageDialog(
                frame,
                "A spectrum " + drawSpectra[i].getSpecName() + " cannot be overlaid on itself",
                "Error overlaying spectrum",
                JOptionPane.ERROR_MESSAGE);
            continue;
          }

          // overlayed spectrum must have the same dimension as the spectrum it is overlayed on
          // only one dimensional spectra can be overlaid
          int dimension = drawSpectra[i].getSpectrum().getSpecDimension();
          if (dimension != 1) {
            JOptionPane.showMessageDialog(
                frame,
                "Only one dimensional spectra can be overlaid",
                "Error overlaying spectrum",
                JOptionPane.ERROR_MESSAGE);
            continue;
          }

          Spectrum.OneDim spectrum1 = (Spectrum.OneDim) drawSpectra[i].getSpectrum();

          // if the spectrum is already overlaid, return
          if (spectrumToDatasetMap.containsValue(spectrum1) || spectra.contains(spectrum1)) {
            JOptionPane.showMessageDialog(
                frame,
                "The selected spectrum is already overlaid here",
                "Error overlaying spectrum",
                JOptionPane.ERROR_MESSAGE);
            continue;
          }

          int specShape = spectrum1.getSpecChannel().getSpecShape();

          int[] y = spectrum1.getSpecChannel().getSpecCount();
          double[] yError = spectrum1.getSpecChannel().getSpecUncertainty();
          int[] x = new int[specShape];
          // for X axis, remember: first value in specCounts refers to the first channel and so on
          for (int j = 0; j < specShape; j++) {
            x[j] = j + 1;
          }

          // set up dataset and graph
          dataset1 =
              new DatasetXtoY.Integer(
                  x, y, DatasetXtoYRenderer.VERTICAL_HORIZONTAL, optionalStartPoint);
          dataset1.setErrorInY(yError, DatasetXtoY.ERROR_DUPLICATE);
          dataset1.setShowError(false);
          dataset1.setName(spectrum1.getSpecName());
          dataset1.setAxisX(drawSpectrum.getAxisX());
          dataset1.setAxisY(drawSpectrum.getAxisY());

          theGraph.attachDataset(dataset1);

          // add the spectrum to the JList and to the spectrumToDatasetMap
          drawSpectrum.overlaidSpectraList.add(spectrum1);
          spectra.addElement(spectrum1);
          spectrumList.setSelectedValue(spectrum1, true);
          spectrumList.repaint();
          spectrumToDatasetMap.put(spectrum1, dataset1);

        } catch (Exception exc) {
          JOptionPane.showMessageDialog(
              frame, exc.getMessage(), "Error overlaying spectrum", JOptionPane.ERROR_MESSAGE);
        }
      }
    } catch (Exception exc) {
      JOptionPane.showMessageDialog(
          frame, exc.getMessage(), "Error overlaying spectrum", JOptionPane.ERROR_MESSAGE);
    } finally {
      theGraph.updateAll();
      theGraph.repaint();
      specUI.setDefaultCursor();
    }
  }

  /** delete an overlaid spectrum */
  private void deleteSpectra() {
    Object[] objs = spectrumList.getSelectedValues();
    if (objs == null) return;

    Spectrum.OneDim spectrum;
    DatasetXtoY.Integer dataset;
    Graph.XtoY graph = (Graph.XtoY) drawSpectrum.getGraph();

    for (int i = 0; i < objs.length; i++) {
      spectrum = (Spectrum.OneDim) objs[i];

      if (spectrum == null) continue;

      dataset = (DatasetXtoY.Integer) spectrumToDatasetMap.get(spectrum);
      graph.detachDataset(dataset);

      drawSpectrum.overlaidSpectraList.remove(spectrum);
      spectra.removeElement(spectrum);
      spectrumToDatasetMap.remove(spectrum);
    }

    spectrumList.repaint();
    graph.updateAll();
    graph.repaint();
  }

  /** shows the dialog for overlaying spectra */
  public void overlay() {
    overlay(this);
  }

  /** Sets the argument as the current overlay spectrum ... and then overlay spectra on it */
  public static void overlay(OverlaySpectrum overlaySpectrum) {
    if (overlaySpectrum == null) return;

    setCurrentOverlaySpectrum(overlaySpectrum);

    ViewerDialog.showCloseDialog(contentPane, "Overlay Spectrum Dialog");

    setCurrentOverlaySpectrum(null);
  }

  /**
   * set as current overlay spectrum ... argument can be null if argument is null, then set the
   * current overlay spectrum to null
   */
  private static void setCurrentOverlaySpectrum(OverlaySpectrum overlaySpectrum) {
    if (overlaySpectrum == null) {
      currentOverlaySpectrum = null;
      // spectrumScroller.getViewport().remove ();
      spectrumScroller.setViewportView(null);
      spectrumScroller.setViewport(null);
    } else {
      currentOverlaySpectrum = overlaySpectrum;
      spectrumScroller.setViewportView(overlaySpectrum.spectrumList);
    }
  }

  /** handles the actions for this OverlaySpectrum class */
  protected static final class OverlayActor implements ActionListener {
    /** Handles the actions ... from ActionListener Interface */
    public void actionPerformed(ActionEvent e) {
      String command = e.getActionCommand();

      if (command.equals("addSpectrum")) {
        if (OverlaySpectrum.currentOverlaySpectrum != null) currentOverlaySpectrum.addSpectrum();
      } else if (command.equals("deleteSpectrum")) {
        if (OverlaySpectrum.currentOverlaySpectrum != null) currentOverlaySpectrum.deleteSpectrum();
      } else if (command.equals("addSpectra")) {
        if (OverlaySpectrum.currentOverlaySpectrum != null) currentOverlaySpectrum.addSpectra();
      } else if (command.equals("deleteSpectra")) {
        if (OverlaySpectrum.currentOverlaySpectrum != null) currentOverlaySpectrum.deleteSpectra();
      }
    }
  }
}
