package spec.spectrum;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import spec.lib.graph.Axis;
import spec.lib.graph.AxisX;
import spec.lib.graph.AxisY;
import spec.lib.graph.Dataset;
import spec.lib.graph.DatasetXYtoZ;
import spec.lib.graph.DatasetXtoY;
import spec.lib.graph.DatasetXtoYRenderer;
import spec.lib.graph.Graph;
import spec.lib.ui.ViewerDialog;
import spec.ui.SpectrumInternalFrame;
import spec.ui.SpectrumManipulator;

/**
 * This encapsulates the drawing of a spectrum onto a graph It uses the Graph data plotting toolkit
 * Let it implement the SpectrumInterface so we can call spectrum methods
 */
public abstract class DrawableSpectrum implements SpectrumInterface, PropertyChangeListener {

  protected Spectrum spectrum;
  protected Dataset dataset;
  protected Graph graph;

  protected AxisX xAxis;
  protected AxisY yAxis;
  protected SpectrumManipulator spectrumManipulator;

  // holds a list of all the overlaid spectra
  public final java.util.List overlaidSpectraList = new ArrayList();

  /** nothing can be null */
  public DrawableSpectrum(
      Spectrum aSpectrum,
      Graph aGraph,
      AxisX aAxisX,
      AxisY aAxisY,
      SpectrumManipulator spectrumManipulator) {
    spectrum = aSpectrum;
    spectrum.addPropertyChangeListener(this);

    this.xAxis = aAxisX;
    this.yAxis = aAxisY;
    this.graph = aGraph;
    this.spectrumManipulator = spectrumManipulator;
  }

  /**
   * should perform the actual drawing on the graph. Takes care of creating a dataset and attaching
   * it to the graph
   */
  public abstract void draw();

  /** method to return the graph member */
  public Graph getGraph() {
    return graph;
  }

  /** method to return the spectrum member */
  public Spectrum getSpectrum() {
    return spectrum;
  }

  public SpectrumManipulator getSpectrumManipulator() {
    return spectrumManipulator;
  }

  public AxisX getAxisX() {
    return xAxis;
  }

  public AxisY getAxisY() {
    return yAxis;
  }

  // methods from SpectrumInterface
  public int getSpecDimension() {
    return spectrum.getSpecDimension();
  }

  public String getSpecName() {
    return spectrum.getSpecName();
  }

  public void setSpecName(String aSpecName) {
    spectrum.setSpecName(aSpecName);
  }

  public Date getSpecDate() {
    return spectrum.getSpecDate();
  }

  public void setSpecDate(Date aspecDate) {
    spectrum.setSpecDate(aspecDate);
  }

  public int getMaxCount() {
    return spectrum.getMaxCount();
  }

  public String toString() {
    return spectrum.toString();
  }

  public String oneLineSummary() {
    return spectrum.oneLineSummary();
  }

  public String details() {
    return spectrum.details();
  }

  public void editSpectrum() {
    spectrum.editSpectrum();
  }

  public void info() {
    InfoViewer.show(this);
  }

  /** No property being listened to here yet */
  public void propertyChange(PropertyChangeEvent e) {
    // redraw if the scale of the dataset is changed, so we can reflect the new scale
    if (e.getPropertyName().equals(Spectrum.SPECTRUM_NAME_PROPERTY)) {
      SpectrumInternalFrame frame = spectrumManipulator.getAssocFrame(this);
      if (frame != null) {
        frame.setTitle(spectrum.getSpecName());
        // frame.repaint ();
      }
    }
  }

  /** static inner class to handle drawing One Dimensional spectra */
  public static class OneDim extends DrawableSpectrum {

    // set the object managing overlaying of spectra
    protected OverlaySpectrum overlaySpectrum;

    public OneDim(
        Spectrum.OneDim aSpectrum,
        Graph.XtoY aGraph,
        AxisX aAxisX,
        AxisY aAxisY,
        SpectrumManipulator spectrumManipulator) {
      super(aSpectrum, aGraph, aAxisX, aAxisY, spectrumManipulator);
      overlaySpectrum = new OverlaySpectrum(this);
    }

    /**
     * Default constructor that takes just a spectrum and constructs new graph and axes to display
     * the spectrum
     */
    public OneDim(
        Spectrum.OneDim aSpectrum, Graph.XtoY graph, SpectrumManipulator spectrumManipulator) {
      this(aSpectrum, graph, new AxisX(true, Axis.BOTTOM), new AxisY(), spectrumManipulator);
      xAxis.setAxisTitleString("Channels of the spectrum");
      yAxis.setAxisTitleString("Counts for the spectrum channels");
    }

    /**
     * should perform the actual drawing on the graph. Takes care of creating a dataset and
     * attaching it to the graph
     */
    public void draw() {
      /*
       * make the number of elements in the data set be = specShape
       * We then define the optional start point as (0, 0)
       */
      int specShape = ((Spectrum.OneDim) spectrum).getSpecChannel().getSpecShape();

      int[] y = ((Spectrum.OneDim) spectrum).getSpecChannel().getSpecCount();
      double[] yError = ((Spectrum.OneDim) spectrum).getSpecChannel().getSpecUncertainty();
      int[] x = new int[specShape];
      // for X axis, remember: first value in specCounts refers to the first channel and so on
      for (int i = 0; i < specShape; i++) {
        x[i] = i + 1;
      }

      // set up dataset and graph
      Point2D.Float optionalStartPoint = new Point2D.Float(0.0f, 0.0f);
      try {
        dataset =
            new DatasetXtoY.Integer(
                x, y, DatasetXtoYRenderer.VERTICAL_HORIZONTAL, optionalStartPoint);
        ((DatasetXtoY.Integer) dataset).setErrorInY(yError, DatasetXtoY.ERROR_DUPLICATE);
        ((DatasetXtoY.Integer) dataset).setShowError(false);
        dataset.setName(this.getSpecName());

        dataset.setAxisX(xAxis);
        dataset.setAxisY(yAxis);

        // graph.setGraphTitle ( this.getSpectrum().getSpecName() );
        ((Graph.XtoY) graph).attachDataset((DatasetXtoY.Integer) dataset);
        graph.getGraphAxisData().setPrimaryDataset(dataset);
      } catch (Dataset.DatasetException exc) {
        System.out.println(exc.getMessage());
      }
    }

    public void overlay() {
      overlaySpectrum.overlay();
    }
  }

  /** static inner class to handle drawing Two Dimensional spectra */
  public static class TwoDim extends DrawableSpectrum {

    /**
     * Remember, only one data set can be graphed on a Graph2d.XYtoZ so it makes no sense to pass it
     * an already created graph or axes... except no data sets have been attached to them
     */
    public TwoDim(
        Spectrum.TwoDim aSpectrum,
        Graph.XYtoZ aGraph,
        AxisX aAxisX,
        AxisY aAxisY,
        SpectrumManipulator spectrumManipulator) {
      super(aSpectrum, aGraph, aAxisX, aAxisY, spectrumManipulator);
    }

    public TwoDim(
        Spectrum.TwoDim aSpectrum, Graph.XYtoZ aGraph, SpectrumManipulator spectrumManipulator) {
      this(
          aSpectrum,
          aGraph,
          new AxisX(true, Axis.BOTTOM),
          new AxisY(true, Axis.LEFT),
          spectrumManipulator);
      xAxis.setAxisTitleString("Channels of the spectrum");
      yAxis.setAxisTitleString("Channels of the spectrum");
    }

    /**
     * should perform the actual drawing on the graph. Takes care of creating a dataset and
     * attaching it to the graph
     */
    public void draw() {
      int specShape0 = ((Spectrum.TwoDim) spectrum).getSpecChannel().getSpecShape0();
      int specShape1 = ((Spectrum.TwoDim) spectrum).getSpecChannel().getSpecShape1();

      int[][] data = ((Spectrum.TwoDim) spectrum).getSpecChannel().getSpecCount();
      double[][] dataError = ((Spectrum.TwoDim) spectrum).getSpecChannel().getSpecUncertainty();

      // set up dataset and graph
      try {
        dataset = new DatasetXYtoZ.Integer(data, DatasetXYtoZ.Y_OF_X);
        ((DatasetXYtoZ.Integer) dataset).setErrorInZ(dataError);
        dataset.setName(this.getSpectrum().getSpecName());

        dataset.setAxisX(xAxis);
        dataset.setAxisY(yAxis);

        // graph.setGraphTitle ( this.getSpectrum().getSpecName() );
        ((Graph.XYtoZ) graph).attachDataset((DatasetXYtoZ.Integer) dataset);
        graph.getGraphAxisData().setPrimaryDataset(dataset);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }

  /** class to manage displaying information about this drawable spectrum */
  public static class InfoViewer {
    public static final JPanel contentPane = new JPanel();

    protected static JTextField fileField = new JTextField();
    protected static JTextField frameField = new JTextField();
    protected static JTextArea specArea = new JTextArea(7, 50);
    protected static JTextArea graphArea = new JTextArea(3, 50);

    // protected JScrollPane specScroller = new JScrollPane();
    // protected JScrollPane graphScroller = new JScrollPane();

    private static DrawableSpectrum drawSpec = null;

    protected static ActionListener actionListener;

    protected static JScrollPane overlayScroller = new JScrollPane();

    static {
      fileField.setEditable(false);
      frameField.setEditable(false);
      specArea.setEditable(false);
      // specScroller.getViewport().setView (specArea);
      graphArea.setEditable(false);
      // graphScroller.getViewport().setView (graphArea);
      // set the layout of the contentPane container
      updateLayout();
    }

    private InfoViewer() {} // no-one should instantiate

    public static void show(DrawableSpectrum drawSpectrum) {
      if (drawSpectrum == null) return;

      setCurrentDrawSpectrum(drawSpectrum);

      // get the values for this axis
      getValues();

      ViewerDialog.showCloseDialog(contentPane, "Spectrum & Graph Info");

      setCurrentDrawSpectrum(null);
    }

    /** get the content panel */
    public static Component getContentPanel() {
      return contentPane;
    }

    /**
     * set as current draw spectrum ... argument can be null if argument is null, then set the
     * current draw spectrum to null
     */
    private static void setCurrentDrawSpectrum(DrawableSpectrum drawSpectrum) {
      if (drawSpectrum == null) {
        drawSpec = null;
        overlayScroller.setViewportView(null);
        overlayScroller.setViewport(null);
      } else {
        drawSpec = drawSpectrum;
      }
    }

    private static void getValues() {
      String fileName = drawSpec.spectrumManipulator.getAssocFilename(drawSpec);
      if (fileName != null) fileField.setText(fileName);
      else fileField.setText("");

      SpectrumInternalFrame specFrame = drawSpec.spectrumManipulator.getAssocFrame(drawSpec);
      if (specFrame != null) frameField.setText(specFrame.getTitle());
      else frameField.setText("");

      specArea.setText(drawSpec.spectrum.details());
      graphArea.setText(drawSpec.graph.details());

      Spectrum spec = drawSpec.getSpectrum();
      if (drawSpec instanceof DrawableSpectrum.OneDim) {
        DrawableSpectrum.OneDim aOneDimDrawSpec = (DrawableSpectrum.OneDim) drawSpec;
        overlayScroller.setViewportView(aOneDimDrawSpec.overlaySpectrum.spectrumList);
      }
    }

    /** handles updating the layout for the contentPane */
    private static void updateLayout() {
      JScrollPane scrollPane;

      contentPane.setBorder(
          BorderFactory.createTitledBorder(
              BorderFactory.createEtchedBorder(),
              "Information on Spectrum and Graph",
              TitledBorder.LEFT,
              TitledBorder.TOP));

      contentPane.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(5, 5, 5, 5);

      c.gridwidth = 1;
      c.gridy = 0;
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      contentPane.add(new JLabel("Associated File"), c);
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(fileField, c);

      scrollPane = new JScrollPane(specArea);

      scrollPane.setBorder(
          BorderFactory.createTitledBorder(
              BorderFactory.createEtchedBorder(),
              "Spectrum Information",
              TitledBorder.LEFT,
              TitledBorder.TOP));

      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 2;
      c.fill = GridBagConstraints.CENTER;
      contentPane.add(scrollPane, c);

      c.gridwidth = 1;
      c.gridy = 2;
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      contentPane.add(new JLabel("Associated Graph Window Title"), c);
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(frameField, c);

      scrollPane = new JScrollPane(graphArea);

      scrollPane.setBorder(
          BorderFactory.createTitledBorder(
              BorderFactory.createEtchedBorder(),
              "Graph Information",
              TitledBorder.LEFT,
              TitledBorder.TOP));

      c.gridx = 0;
      c.gridy = 3;
      c.gridwidth = 2;
      c.fill = GridBagConstraints.CENTER;
      contentPane.add(scrollPane, c);

      overlayScroller.setBorder(
          BorderFactory.createTitledBorder(
              BorderFactory.createEtchedBorder(),
              "Overlaid Spectra (for one-dimensional spectra only)",
              TitledBorder.LEFT,
              TitledBorder.TOP));

      c.gridx = 0;
      c.gridy = 4;
      c.gridwidth = 2;
      c.fill = GridBagConstraints.CENTER;
      contentPane.add(overlayScroller, c);
    }

    /** handles updating the layout for the contentPane */
    private static void updateLayout2() {
      contentPane.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(5, 5, 5, 5);

      // add the components
      c.gridx = c.gridy = 0;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(new JLabel("Information on Spectrum and Graph"), c);

      c.gridwidth = 1;
      c.gridy = 1;
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      contentPane.add(new JLabel("Associated File"), c);
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(fileField, c);

      c.gridwidth = 2;
      c.gridx = 0;
      c.gridy = 2;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(new JLabel("Spectrum Information"), c);

      c.gridx = 0;
      c.gridy = 3;
      c.fill = GridBagConstraints.CENTER;
      contentPane.add(new JScrollPane(specArea), c);

      c.gridwidth = 1;
      c.gridy = 4;
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      contentPane.add(new JLabel("Associated Graph Window Title"), c);
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(frameField, c);

      c.gridwidth = 2;
      c.gridx = 0;
      c.gridy = 5;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(new JLabel("Graph Information"), c);

      c.gridx = 0;
      c.gridy = 6;
      c.fill = GridBagConstraints.CENTER;
      contentPane.add(new JScrollPane(graphArea), c);
    }
  }
}
