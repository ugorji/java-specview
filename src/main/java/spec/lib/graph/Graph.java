package spec.lib.graph;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import spec.lib.ui.BackgroundPrint;
import spec.lib.ui.CancelPrint;
import spec.lib.ui.FontChooser;

/**
 * Graph is just a container that knows about the components below: Title, Key & Plot Area.
 *
 * <p>All attaching or detaching of datasets publicly is performed from here the corresponding
 * methods in GraphKey and GraphAxisData are protected
 *
 * <p>The Container of this Graph object must have a minimum size set to 30 x 30 to prevent a
 * sun.dc.pr.PRException
 *
 * <p>... Graph should have getPreferredSize() return getSize() ... ... ... so that the view port
 * will not change the size when scrolling ... ...(saw it in source code of ViewportLayout)
 */
public abstract class Graph extends JPanel implements Printable, Scrollable {
  protected static int NUM_GRAPH = 0;

  /** Properties object for this */
  protected static Properties properties = new Properties();

  /** flag to set if printing is enabled or not */
  protected static boolean enablePrinting = true;

  /** the font chooser */
  public static final FontChooser fontChooser = new FontChooser();

  /** the very minimum size that this graph should be */
  public static final Dimension preferredSize = new Dimension(670, 590);
  /** the very minimum size that this graph should be */
  public static final Dimension minimumSize = new Dimension(125, 125);

  /**
   * define whether the grah2d plots 2-dimensional (X to Y) datasets or 3-dimensional (XY to Z)
   * datasets
   */
  public static final int XTOY = 1;

  public static final int XYTOZ = 2;
  /** cause the graphics to be rendered with high quality and anti-aliased */
  public static RenderingHints qualityHints;
  /** flag to indicate if printing is being done or just on-screen rendering, when painting */
  protected boolean printFlag = false;
  /** int representing the type of the graph, whether XtoY or XYtoZ */
  public int graphType;

  /** the maximum unit increment, used for scrolling */
  protected int maxUnitIncrement = 1;

  static {
    qualityHints =
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    qualityHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
  }

  /** ****************** * Public Variables ******************* */

  // Name of the graph ... e.g placed as the title of the frame containing the graph
  protected String graphName;
  // represents the title of the graph
  protected GraphTitle graphTitle;
  // represents the key of the graph
  protected GraphKey graphKey;
  // represents the position of the graph
  protected GraphPosition graphPosition;
  // represents the axis /data plot of the graph
  protected GraphAxisData graphAxisData;

  // The rectangles that define the bounds of the various components
  protected Rectangle graphTitleRectangle = new Rectangle();
  protected Rectangle graphKeyRectangle = new Rectangle();
  protected Rectangle graphAxisDataRectangle = new Rectangle();
  protected Rectangle graphPositionRectangle = new Rectangle();

  // boolean to indicate that we are scrolling
  private boolean isScrolling = false;

  public static void loadIntoProperties(Properties prop) {
    for (Enumeration e = prop.propertyNames(); e.hasMoreElements(); ) {
      String key = (String) e.nextElement();
      properties.setProperty(key, prop.getProperty(key));
    }

    String eps = null;
    if (enablePrinting) eps = "true";
    else eps = "false";

    eps = properties.getProperty("enablePrinting", eps).trim();

    if (eps.regionMatches(true, 0, "true", 0, 4)) enablePrinting = true;
    else if (eps.regionMatches(true, 0, "false", 0, 5)) enablePrinting = false;
  }

  /* *******************
   * CONSTRUCTORS
   ****************** */
  /** no-one can instantiate this */
  private Graph() {}

  /** graphKey will be null, all others will be instantiated */
  public Graph(String graphName, GraphAxisData graphAxisData) {
    this(
        graphName,
        graphAxisData,
        null,
        new GraphTitle("Graph " + (NUM_GRAPH + 1)),
        new GraphPosition(graphAxisData));
  }

  /** graphKey will be null, all others will be instantiated */
  public Graph(String graphName, GraphAxisData graphAxisData, GraphTitle graphTitle) {
    this(graphName, graphAxisData, null, graphTitle, new GraphPosition(graphAxisData));
  }

  /** Graph must be opaque */
  public Graph(
      String graphName,
      GraphAxisData graphAxisData,
      GraphKey graphKey,
      GraphTitle graphTitle,
      GraphPosition graphPosition) {
    super();

    Graph.NUM_GRAPH++;

    if (!(isOpaque())) setOpaque(true);

    // set the border for this component
    this.setBorder(new LineBorder(Color.black, 2));

    setMinimumSize(minimumSize);

    this.setVisible(true);
    // this.setBackground (Color.lightGray);
    // this.setForeground (Color.black);
    this.setLayout(this.new LayoutMgr());

    // specify the AWT events we are interested in
    this.enableEvents(AWTEvent.COMPONENT_EVENT_MASK);

    this.graphName = graphName;
    this.graphAxisData = graphAxisData;
    this.graphKey = graphKey;
    this.graphTitle = graphTitle;
    this.graphPosition = graphPosition;

    // ensure that graphPosition listens to graphAxisData for the mouse position
    if (graphPosition.getGraphAxisData() != graphAxisData)
      graphPosition.setGraphAxisData(graphAxisData);

    // add the various components into this graph container
    // do these first so that the components will be showing
    if (graphAxisData != null) add(graphAxisData);
    if (graphKey != null) add(graphKey);
    if (graphTitle != null) add(graphTitle);
    if (graphPosition != null) add(graphPosition);

    // call graphAxisData to set its "graph2D" member to me
    if (graphAxisData != null) graphAxisData.setGraph(this);
  }

  /** *************** * Public Methods ***************** */
  public void setGraphName(String aName) {
    graphName = aName;
  }

  public String getGraphName() {
    return graphName;
  }

  // sets the title of the graph ... what is drawn on the graph plot
  public void setGraphTitle(String str) {
    this.graphTitle.setGraphTitle(str);
  }

  public static final Graph.XtoY getXtoYInstance() {
    return new Graph.XtoY();
  }

  public static final Graph.XYtoZ getXYtoZInstance() {
    return new Graph.XYtoZ();
  }

  public static boolean isPrintingEnabled() {
    return enablePrinting;
  }

  /**
   * Process component events to cause a call to updateAll() ... when the component size changes
   * validate should be called, to cause immediate validation of the component
   */
  public void processComponentEvent(ComponentEvent e) {
    super.processComponentEvent(e);

    switch (e.getID()) {
      case ComponentEvent.COMPONENT_RESIZED:
        // do this so a call to dispose will not cause an exception, since
        // disposing a window sets the component sizes to zero
        if (this.getWidth() < 15 || this.getHeight() < 15) break;

        validate();
        updateAll();
        this.repaint();
        break;
      default:
        break;
    }
  }

  /**
   * Performs prelim draws to the components b4 repaint() is called called after adding the
   * components to the graph or whenever a redraw is required, e.g if a dataset was attached or
   * detached returns true if it updated, else returns false
   *
   * <p>NOTE: It does not cause a call to validate()
   */
  public boolean updateAll() {
    // set the parent's name to the graphName (e.g. the frame title)
    this.setName(this.graphName);

    graphTitle.draw();
    graphAxisData.draw();
    graphKey.draw();
    graphPosition.draw();

    return true;
  }

  /** get the component encapsulating the main graphing area and the axes of the whole graph */
  public GraphAxisData getGraphAxisData() {
    return graphAxisData;
  }

  /**
   * get the main graphing area of the whole graph If printing, do not set the rendering hints to
   * Graph.qualityHints
   */
  public GraphDataset getGraphDataset() {
    return graphAxisData.graphDataset;
  }

  /** detaches all datasets here and handles everything that goes with it */
  public void detachAllDatasets() {
    this.graphKey.detachAllDatasets();
    this.graphAxisData.detachAllDatasets();
    this.getGraphDataset().resetPrimaryDataset();
  }

  /** override this, to return the preferred size ... which is the current size */
  public Dimension getPreferredSize() {
    return getSize();
    // return preferredSize;
  }

  /** override this, to return the minimum size */
  public Dimension getMinimumSize() {
    return minimumSize;
  }

  /** (Scrollable Interface) return the preferred size for scrolling mode */
  public Dimension getPreferredScrollableViewportSize() {
    return (getPreferredSize());
  }

  /** (Scrollable Interface) let unit scrolling return 1/4 of the block increment */
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return ((getScrollableBlockIncrement(visibleRect, orientation, direction)) / 4);
  }

  /**
   * (Scrollable Interface) block scrolling scrolls down by the area which is visible ... like going
   * down by one page
   */
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return (orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
  }

  /** (Scrollable Interface) do we resize when viewport width changes??? */
  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  /** (Scrollable Interface) do we resize when viewport height changes??? */
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  /**
   * set the print flag to true or false, so components will know to paint in the preferred way for
   * printing or normal on-screen rendering
   *
   * <p>When printing, make the graph transparent when not printing, make the graph opaque
   */
  public void setPrintFlag(boolean b) {
    boolean origOpaque = isOpaque();
    if (b) {
      if (origOpaque) setOpaque(false);
    } else {
      if (!(origOpaque)) setOpaque(true);
    }

    graphTitle.setPrintFlag(b);
    graphKey.setPrintFlag(b);
    graphPosition.setPrintFlag(b);
    graphAxisData.setPrintFlag(b);
  }

  /** if dimensions are less than the minimum dimensions, do not paint the graph */
  protected void paintComponent(Graphics g) {
    // if dimensions are less than the minimum dimensions, do nothing
    // this prevents the sun.raster exceptions we got at times
    if (this.getWidth() < getMinimumSize().width || this.getHeight() < getMinimumSize().height)
      return;

    if (!(printFlag)) {
      // cause the graphics to be rendered with high quality and anti-aliased
      ((Graphics2D) g).setRenderingHints(Graph.qualityHints);
    }

    super.paintComponent(g);
  }

  /**
   * performs the main printing uses 2 threads to print in the background and also to allow the
   * printing job be cancelled ... check that this graph might not be in a frame ...
   *
   * <p>ensure that this graph is not opaque ... to minimize printing time
   */
  public void printGraph() {
    printGraph1();
  }

  /**
   * background printing ... still has a lot of problems ... not yet figured it out this is because
   * changes in the graph will cause problems while printing ... for now, it just returns without
   * doing anything
   */
  public void printGraphBg() {
    if (true) return;

    // if printing is not enabled, return
    if (!(enablePrinting)) {
      String msg = "Printing has been temporarily disabled due to ... problems";
      JOptionPane.showMessageDialog(
          this, msg, "Printing disabled", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    Color origBg = this.getBackground();
    this.setBackground(Color.white);

    // set the print flag for components to TRUE ...
    this.setPrintFlag(true);

    BackgroundPrint bgPrint =
        new BackgroundPrint(
            this,
            (Frame) this.getTopLevelAncestor(),
            "SpecView: " + graphName == null ? graphName : "");
    bgPrint.start();

    // reset the print flag for components to FALSE ...
    this.setPrintFlag(false);

    this.setBackground(origBg);
  }

  /**
   * performs the main printing ensure that this graph is not opaque ... to minimize printing time
   * (performed by setPrintFlag() )
   */
  private void printGraph1() {
    // if printing is not enabled, return
    if (!(enablePrinting)) {
      String msg = "Printing has been temporarily disabled due to ... problems";
      JOptionPane.showMessageDialog(
          this, msg, "Printing disabled", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    // set the print flag for components to TRUE ...
    this.setPrintFlag(true);

    PrinterJob printJob = PrinterJob.getPrinterJob();

    // Ask user for page format (e.g., portrait/landscape)
    PageFormat pf = printJob.pageDialog(printJob.defaultPage());

    printJob.setJobName("SpecView: " + graphName == null ? graphName : "");

    printJob.setPrintable(this, pf);

    if (printJob.printDialog()) {
      try {
        printJob.print();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // reset the print flag for components to FALSE ...
    this.setPrintFlag(false);
  }

  /**
   * performs the main printing uses a thread to allow the printing job be cancelled ensure that
   * this graph is not opaque ... to minimize printing time (performed by setPrintFlag() )
   *
   * <p>currently, problems exist with using a thread to allow the printing be cancelled ... ... so
   * just return
   */
  private void printGraph2() {
    if (true) return; // just do nothing, due to problems below

    // if printing is not enabled, return
    if (!(enablePrinting)) {
      String msg = "Printing has been temporarily disabled due to ... problems";
      JOptionPane.showMessageDialog(
          this, msg, "Printing disabled", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    Color origBg = this.getBackground();
    boolean sameBackground = origBg.equals(Color.white);

    if (!(sameBackground)) this.setBackground(Color.white);

    // set the print flag for components to TRUE ...
    this.setPrintFlag(true);

    PrinterJob printJob = PrinterJob.getPrinterJob();

    Frame aFrame = null;
    if (this.getTopLevelAncestor() instanceof Frame) aFrame = (Frame) this.getTopLevelAncestor();
    CancelPrint cancelPrint = new CancelPrint(printJob, aFrame);
    cancelPrint.start();

    // Ask user for page format (e.g., portrait/landscape)
    PageFormat pf = printJob.pageDialog(printJob.defaultPage());

    printJob.setJobName("SpecView: " + graphName == null ? graphName : "");

    printJob.setPrintable(this, pf);

    if (printJob.printDialog()) {
      JDialog dialog = cancelPrint.getDialog();
      try {
        dialog.toFront();
        printJob.print();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        cancelPrint.setPrintingDone(true);
        if (dialog != null) dialog.dispose();
      }
    }

    // reset the print flag for components to FALSE ...
    this.setPrintFlag(false);

    if (!(sameBackground)) this.setBackground(origBg);
  }

  /**
   * performs the main printing ... no more works since the print method from Printable interface
   * has been changed ... so now just return
   */
  /*
  *********************************************************
  private void printGraph3 ()
  {
    if (true)
      return; // due to problems, just return

    // if printing is not enabled, return
    if ( !(enablePrinting) ) {
      String msg = "Printing has been temporarily disabled due to ... problems" ;
      JOptionPane.showMessageDialog (this, msg, "Printing disabled", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    PrinterJob printJob = PrinterJob.getPrinterJob();
    // Ask user for page format (e.g., portrait/landscape)
    PageFormat pf = printJob.pageDialog (printJob.defaultPage());

    double x, y, width, ht;
    x = pf.getImageableX ();
    y = pf.getImageableY ();
    width = pf.getImageableWidth ();
    ht = pf.getImageableHeight ();

    Rectangle origBounds = this.getBounds ();

    Insets in = this.getInsets ();
    this.setBounds (in.left, in.right, (int) width, (int) ht);
    // this.updateAll ();

    // set the print flag for components to TRUE ...
    this.setPrintFlag (true);

    printJob.setPrintable (this, pf);

    if (printJob.printDialog()) {
      try { printJob.print(); }
      catch (Exception e) { e.printStackTrace(); }
    }

    this.setBounds (origBounds);
    // this.updateAll ();

    // paint again directly on the screen before setting the printFlag back to false
    // this recreates the font render context, graphics contexts and stuff ...
    // for normal on-screen rendering ... preventing the bad fonts we had on axes
    // *** ... BUG HERE in JDK1.2rc-1 ***
    // this.paint ( this.getGraphics () );

    // reset the print flag for components to FALSE ...
    this.setPrintFlag (false);

  }
  *********************************************************
  */

  /** Handles printing ... from Printable Interface */
  public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
    return (print1(g, pf, pi));
  }

  /**
   * Handles printing ... from Printable Interface ->> METHOD 1 <<- Uses a transform scale on the
   * Graphics context ... to fit the graph onto the imageable area ... just paints directly into
   * that transformed graphics context
   */
  private int print1(Graphics g, PageFormat pf, int pi) throws PrinterException {
    // only print one page
    if (pi >= 1) return Printable.NO_SUCH_PAGE;

    RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
    System.setProperty("java2d.font.usePlatformFont", "true");

    double x, y, width, height;
    x = pf.getImageableX();
    y = pf.getImageableY();
    width = pf.getImageableWidth();
    height = pf.getImageableHeight();

    Graphics2D g2 = (Graphics2D) g;

    // // g.translate ( (int) x, (int) y);
    AffineTransform oldTransform = new AffineTransform(g2.getTransform());

    // Define the rendering transform
    AffineTransform at = new AffineTransform();
    // Apply a translation transform to make room for the rotated text.
    at.setToTranslation(x, y);
    g2.transform(at);
    // scale it
    at.setToScale(width / getWidth(), height / getHeight());
    g2.transform(at);

    // use print to print components ...
    this.print(g2);

    g2.setTransform(oldTransform);

    RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
    return Printable.PAGE_EXISTS;
  }

  /**
   * Handles printing ... from Printable Interface ->> METHOD 2 <<- ... paints Graph onto an image
   * and just prints the image out ... ... like double buffering
   */
  private int print2(Graphics g, PageFormat pf, int pi) throws PrinterException {
    // only print one page
    if (pi >= 1) return Printable.NO_SUCH_PAGE;

    double x, y, width, height;
    x = pf.getImageableX();
    y = pf.getImageableY();
    width = pf.getImageableWidth();
    height = pf.getImageableHeight();

    int graphWidth = getWidth();
    int graphHeight = getHeight();

    BufferedImage graphImage = (BufferedImage) this.createImage(graphWidth, graphHeight);
    Graphics2D graphics = graphImage.createGraphics();
    graphics.setColor(g.getColor());
    graphics.setFont(g.getFont());
    graphics.setClip(g.getClipBounds());

    paint(graphics);

    AffineTransform at =
        AffineTransform.getScaleInstance(
            width / (double) graphWidth, height / (double) graphHeight);

    // draw the image using this scaling
    ((Graphics2D) g).drawImage(graphImage, at, this);

    return Printable.PAGE_EXISTS;
  }

  /** returns a one line summary of this graph */
  public String toString() {
    return (oneLineSummary());
  }

  /**
   * returns the spectrum as a one-line short string ... this can be used in a List Box to represent
   * the spectrum
   */
  public String oneLineSummary() {
    StringBuffer sb = new StringBuffer();
    sb.append(graphTitle.getGraphTitle());
    sb.append("; ");

    return sb.toString();
  }

  /** returns the spectrum as a long string (with all the details) ... */
  public String details() {
    StringBuffer sb = new StringBuffer();
    sb.append("GraphTitle : ");
    sb.append(graphTitle.getGraphTitle());
    sb.append("\nPlotting Area: ");

    return sb.toString();
  }

  public void updateUI() {
    super.updateUI();
    SwingUtilities.updateComponentTreeUI(fontChooser);
  }

  /** Inner static member class for Graph to oversee X to Y Data Sets */
  public static class XtoY extends Graph {

    public XtoY() {
      this("X to Y Graph " + Graph.NUM_GRAPH);
    }

    public XtoY(String graphName) {
      super(graphName, new GraphAxisData.XtoY());

      this.graphKey = new GraphKey.XtoY((GraphAxisData.XtoY) graphAxisData);
      add(graphKey);

      graphType = Graph.XTOY;
    }

    public XtoY(String graphName, GraphKey.XtoY graphKey, GraphAxisData.XtoY graphAxisData) {
      super(
          graphName,
          graphAxisData,
          graphKey,
          new GraphTitle("Graph " + (NUM_GRAPH + 1)),
          new GraphPosition(graphAxisData));

      graphType = Graph.XTOY;
    }

    /** attaches a data set here and handles everything that goes with it */
    public void attachDataset(DatasetXtoY d) {
      if (d == null) return;

      ((GraphAxisData.XtoY) this.graphAxisData).attachDataset(d);
      ((GraphKey.XtoY) this.graphKey).attachDataset(d);
      this.getGraphDataset().resetPrimaryDataset();
    }

    /** detaches a data set here and handles everything that goes with it */
    public void detachDataset(DatasetXtoY d) {
      if (d == null) return;

      ((GraphAxisData.XtoY) this.graphAxisData).detachDataset(d);
      ((GraphKey.XtoY) this.graphKey).detachDataset(d);
      this.getGraphDataset().resetPrimaryDataset();
    }
  }

  /** Inner static member class for Graph to oversee XY to Z Data Sets */
  public static class XYtoZ extends Graph {

    public XYtoZ() {
      this("XY to Z Graph " + Graph.NUM_GRAPH);
    }

    public XYtoZ(String graphName) {
      super(graphName, new GraphAxisData.XYtoZ());

      this.graphKey = new GraphKey.XYtoZ((GraphAxisData.XYtoZ) graphAxisData);
      add(graphKey);

      graphType = Graph.XYTOZ;
    }

    public XYtoZ(String graphName, GraphKey.XYtoZ graphKey, GraphAxisData.XYtoZ graphAxisData) {
      super(
          graphName,
          graphAxisData,
          graphKey,
          new GraphTitle("Graph " + (NUM_GRAPH + 1)),
          new GraphPosition(graphAxisData));

      graphType = Graph.XYTOZ;
    }

    /** attaches a data set here and handles everything that goes with it */
    public void attachDataset(DatasetXYtoZ d) {
      if (d == null) return;

      ((GraphKey.XYtoZ) this.graphKey).attachDataset(d);
      ((GraphAxisData.XYtoZ) this.graphAxisData).attachDataset(d);
      this.getGraphDataset().resetPrimaryDataset();
    }

    /** detaches a data set here and handles everything that goes with it */
    public void detachDataset(DatasetXYtoZ d) {
      if (d == null) return;

      ((GraphKey.XYtoZ) this.graphKey).detachDataset(d);
      ((GraphAxisData.XYtoZ) this.graphAxisData).detachDataset(d);
      this.getGraphDataset().resetPrimaryDataset();
    }
  }

  /** layout manager for this graph */
  public class LayoutMgr implements LayoutManager2, Serializable {

    protected Dimension preferredDim = preferredSize;
    protected Dimension minDim = new Dimension(400, 300);

    /** The method that actually performs the layout */
    public void layoutContainer(Container target) {
      if (graphAxisData == null || graphKey == null || graphTitle == null || graphPosition == null)
        return;

      // set the bounding rectangles which the components occupy
      setComponentBounds(target);

      // set the bounds of the various comonents
      graphAxisData.setBounds(graphAxisDataRectangle);
      graphTitle.setBounds(graphTitleRectangle);
      graphKey.setBounds(graphKeyRectangle);
      graphPosition.setBounds(graphPositionRectangle);
    }

    /**
     * Set the bounds that the various components occupy using this algorithm: A preferred Dimension
     * above which title and key component have defined dimensions and axis/data take all the space
     * left A minimum dimension above which axis and key components are scaled to use a certain
     * percentage of the container space below which axis and key components are not drawn and
     * axis/data take all the extra space
     */
    public void setComponentBounds(Container target) {
      Insets in = target.getInsets();
      int w = target.getWidth();
      int h = target.getHeight();

      int title_h = 40;
      int key_w = 120;
      int position_h = 120;

      if (h >= preferredDim.height) {
        if (w >= preferredDim.width) {
          graphTitleRectangle.setBounds(
              0 + in.left, 0 + in.top, w - (in.left + in.right), title_h - in.top);
          graphKeyRectangle.setBounds(
              w - key_w, title_h, key_w - in.right, h - title_h - position_h);
          graphPositionRectangle.setBounds(
              w - key_w, h - position_h, key_w - in.right, position_h - in.bottom);
          graphAxisDataRectangle.setBounds(
              0 + in.left, title_h, w - key_w - in.left, h - title_h - in.bottom);
        } else if (w >= minDim.width) {
          graphTitleRectangle.setBounds(
              0 + in.left, 0 + in.top, w - (in.left + in.right), title_h - in.top);
          graphKeyRectangle.setBounds(
              5 * w / 6, title_h, w / 6 - in.right, h - title_h - position_h);
          graphPositionRectangle.setBounds(
              5 * w / 6, h - position_h, w / 6 - in.right, position_h - in.bottom);
          graphAxisDataRectangle.setBounds(
              0 + in.left, title_h, 5 * w / 6 - in.left, h - title_h - in.bottom);
        } else {
          graphTitleRectangle.setBounds(
              0 + in.left, 0 + in.top, w - (in.left + in.right), title_h - in.top);
          graphKeyRectangle.setBounds(0, 0, 0, 0);
          graphPositionRectangle.setBounds(0, 0, 0, 0);
          graphAxisDataRectangle.setBounds(
              0 + in.left, title_h, w - in.left, h - title_h - in.bottom);
        }
      } else if (h >= minDim.height) {
        if (w >= preferredDim.width) {
          graphTitleRectangle.setBounds(
              0 + in.left, 0 + in.top, w - (in.left + in.right), h / 14 - in.top);
          graphKeyRectangle.setBounds(
              w - key_w, h / 14, key_w - in.right, 13 * h / 14 - position_h);
          graphPositionRectangle.setBounds(
              w - key_w, h - position_h, key_w - in.right, position_h - in.bottom);
          graphAxisDataRectangle.setBounds(
              0 + in.left, h / 14, w - key_w - in.left, 13 * h / 14 - in.bottom);
        } else if (w >= minDim.width) {
          graphTitleRectangle.setBounds(
              0 + in.left, 0 + in.top, w - (in.left + in.right), h / 14 - in.top);
          graphKeyRectangle.setBounds(
              5 * w / 6, h / 14, w / 6 - in.right, 13 * h / 14 - position_h);
          graphPositionRectangle.setBounds(
              5 * w / 6, h - position_h, w / 6 - in.right, position_h - in.bottom);
          graphAxisDataRectangle.setBounds(
              0 + in.left, h / 14, 5 * w / 6 - in.left, 13 * h / 14 - in.bottom);
        } else {
          graphTitleRectangle.setBounds(
              0 + in.left, 0 + in.top, w - (in.left + in.right), h / 14 - in.top);
          graphKeyRectangle.setBounds(0, 0, 0, 0);
          graphPositionRectangle.setBounds(0, 0, 0, 0);
          graphAxisDataRectangle.setBounds(
              0 + in.left, h / 14, w - in.left, 13 * h / 14 - in.bottom);
        }
      } else {
        if (w >= preferredDim.width) {
          graphTitleRectangle.setBounds(0, 0, 0, 0);
          graphKeyRectangle.setBounds(
              w - key_w, 0 + in.top, key_w - in.right, h - in.top - in.bottom);
          graphPositionRectangle.setBounds(0, 0, 0, 0);
          graphAxisDataRectangle.setBounds(
              0 + in.left, 0 + in.top, w - key_w - in.left, h - in.top - in.bottom);
        } else if (w >= minDim.width) {
          graphTitleRectangle.setBounds(0, 0, 0, 0);
          graphKeyRectangle.setBounds(
              5 * w / 6, 0 + in.top, w / 6 - in.right, h - in.top - in.bottom);
          graphPositionRectangle.setBounds(0, 0, 0, 0);
          graphAxisDataRectangle.setBounds(
              0 + in.left, 0 + in.top, 5 * w / 6 - in.left, h - in.top - in.bottom);
        } else {
          graphTitleRectangle.setBounds(0, 0, 0, 0);
          graphKeyRectangle.setBounds(0, 0, 0, 0);
          graphPositionRectangle.setBounds(0, 0, 0, 0);
          graphAxisDataRectangle.setBounds(
              0 + in.left, 0 + in.top, w - in.right - in.left, h - in.top - in.bottom);
        }
      }
    }

    public Dimension preferredLayoutSize(Container parent) {
      // return preferredDim;
      Dimension dim = parent.getPreferredSize();
      Insets in = parent.getInsets();
      dim.width = dim.width + in.left + in.right;
      dim.height = dim.height + in.top + in.bottom;
      return dim;
    }

    public Dimension maximumLayoutSize(Container target) {
      return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public Dimension minimumLayoutSize(Container parent) {
      // return Graph.minimumSize;
      Dimension dim = parent.getMinimumSize();
      Insets in = parent.getInsets();
      dim.width = dim.width + in.left + in.right;
      dim.height = dim.height + in.top + in.bottom;
      return dim;
    }

    /** Other methods from LayoutManager Interface */
    public void addLayoutComponent(String name, Component comp) {}

    public void removeLayoutComponent(Component comp) {}

    /** Other methods from LayoutManager2 Interface */
    public void addLayoutComponent(Component comp, Object constraints) {}

    public void invalidateLayout(Container target) {}

    public float getLayoutAlignmentX(Container target) {
      return 0.5f;
    }

    public float getLayoutAlignmentY(Container target) {
      return 0.5f;
    }
  }
}
