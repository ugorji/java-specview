package spec.lib.graph;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

/**
 * *****************************************************************************************
 * Ideally, this should be a member class of GraphAxisData since a lot of its functionality calls
 * methods in GraphAxisData However, on its own, it is a large file and will be cumbersome as a
 * member class Workaround: Explicitly pass a reference to the GraphAxisData object when it is
 * instantiated
 * *****************************************************************************************
 *
 * <p>Add a concept of a primary dataset, which cannot be detached ... except there is no other
 * dataset contained on the graph
 * ***************************************************************************************** Jan 28,
 * 99: disabled the log e radio button calls
 * *****************************************************************************************
 */
public abstract class GraphDataset extends JComponent
    implements MouseListener, ActionListener, PropertyChangeListener {
  // this is the primary dataset, which cannot be detached except no others
  // are attached, and which is used to determine the grid and stuff
  protected Dataset primaryDataset;

  // the scale of the datasets on this graph
  protected int scale = Dataset.LINEAR;

  /** property that the scale has changed */
  public static final String SCALE_PROPERTY = "Scale Property of all datasets on Graph";
  /** property that the primary dataset has changed */
  public static final String PRIMARY_DATASET_PROPERTY = "Primary Dataset Property";

  protected BufferedImage graphImage;

  public boolean showGridLines = true;
  protected boolean showUncertainty = false;

  protected static Color gridLineColor = Color.gray;
  protected static BasicStroke gridLineStyle =
      new BasicStroke(
          1.0f,
          BasicStroke.CAP_ROUND,
          BasicStroke.JOIN_ROUND,
          10.0f,
          new float[] {4.0f, 4.0f},
          0.0f);
  /** flag to indicate if printing is being done or just on-screen rendering, when painting */
  protected boolean printFlag = false;
  /** the graphics context to be used here ... transient so it will not be serialized */
  protected transient Graphics2D datasetGraphics;

  /** The starting and ending points for the zoom rectangle */
  protected Point zoomStart = new Point(-1, -1);

  protected Point zoomEnd = new Point(-1, -1);

  /**
   * hold a reference to the GraphAxisData that this is contained in ... MUST be "protected" 'cos it
   * might be directly accessed by the GraphAxisData object
   */
  protected GraphAxisData graphAxisData;

  /** The popup menu */
  protected JPopupMenu popup = new JPopupMenu();

  /** commands that can be used to fire action commands */
  public static final String RESET_AXES_COMMAND = "resetAxes";

  public static final String REPAINT_COMMAND = "repaint";
  public static final String REDRAW_COMMAND = "redraw";
  public static final String GRIDLINES_COMMAND = "gridlines";
  public static final String UNCERTAINTY_COMMAND = "uncertainty";
  public static final String LINEAR_COMMAND = "linear";
  public static final String LOG_10_COMMAND = "log10";
  // public static final String LOG_E_COMMAND = "logE";

  protected JCheckBoxMenuItem uncertaintyChkBox;
  protected JCheckBoxMenuItem gridLinesChkBox;

  // Data Scale buttons
  protected JRadioButtonMenuItem linearRadioButton;
  protected JRadioButtonMenuItem log10RadioButton;
  // protected JRadioButtonMenuItem logERadioButton;

  /** do not allow anyone instantiate this class without passing the GraphAxisData reference */
  private GraphDataset() {}

  /** CONSTRUCTOR */
  public GraphDataset(GraphAxisData graphAxisData) {
    super();
    this.graphAxisData = graphAxisData;

    this.setOpaque(true);

    // make it visible so it can be seen and a graphics context can be got
    this.setVisible(true);
    // set the border for this component
    this.setBorder(new LineBorder(Color.black, 1));
    // set the default background to white ... (so there would be no problem with XOR mode)
    this.setBackground(Color.white);

    addEventHandler();
  }

  /**
   * **************************************************************************** passed a component
   * and draws the datasets onto that component paint is called for general repainting while draws
   * are called for updating the drawings
   * ***************************************************************************
   */
  public abstract void draw();

  /** detaches all the datasets that are attached to this graphDataset */
  protected abstract void detachAllDatasets();

  /** uses an algorithm to check if all the data sets share only one set of x and y axes */
  protected abstract boolean isSingleAxes();

  /** uses an algorithm to check if there is only one dataset plotted on this graph */
  protected abstract boolean isSingleDataset();

  /** To update the graph image */
  protected abstract void updateGraphImage();

  /** no property being listened to here yet */
  public void propertyChange(PropertyChangeEvent e) {}

  /** set the scale of the datasets on this graph */
  public abstract void setScale(int scale);

  /** set the primary dataset to this */
  public abstract void setPrimaryDataset(Dataset d);

  /** get the primary dataset */
  public abstract Dataset getPrimaryDataset();

  /** resets the primary dataset */
  public abstract void resetPrimaryDataset();

  /** adds all the event listeners and recreates the popup menu */
  private void addEventHandler() {
    // allow mouse events to be trapped ...
    // so we can double-click on it to change the title string
    this.enableEvents(AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);

    this.addMouseMotionListener(graphAxisData);
    this.addMouseListener(this);
    // request keyboard focus so we can trap key events
    this.requestFocus();

    // register the popup with this graphDataset
    this.add(popup);

    JMenuItem menuItem;

    // create a sub-menu for Scale changing
    JMenu Scale = (JMenu) popup.add(new JMenu("Data Scale"));

    ButtonGroup scaleGroup = new ButtonGroup();
    linearRadioButton = (JRadioButtonMenuItem) Scale.add(new JRadioButtonMenuItem("Linear"));
    scaleGroup.add(linearRadioButton);
    linearRadioButton.addActionListener(this);
    linearRadioButton.setToolTipText("Linear Scale");

    log10RadioButton = (JRadioButtonMenuItem) Scale.add(new JRadioButtonMenuItem("Log to Base 10"));
    scaleGroup.add(log10RadioButton);
    log10RadioButton.addActionListener(this);
    log10RadioButton.setToolTipText("Log to Base 10 Scale");

    // logERadioButton = (JRadioButtonMenuItem) Scale.add (new JRadioButtonMenuItem ("Log to Base
    // e") );
    // scaleGroup.add (logERadioButton);
    // logERadioButton.addActionListener(this);
    // logERadioButton.setToolTipText ("Log to Base E (Natural Log) Scale");

    popup.addSeparator();

    // linear scale is always the first scale, so set it selected first
    linearRadioButton.setSelected(true);

    // Create the popup menu using a loop.
    // "Action commands" are left distinct from the menu "labels"
    String[] labels = new String[] {"Reset Axes Range", "Refresh plot", "Redraw plot"};

    String[] commands = new String[] {RESET_AXES_COMMAND, REPAINT_COMMAND, REDRAW_COMMAND};

    for (int i = 0; i < labels.length; i++) {
      menuItem = (JMenuItem) popup.add(new JMenuItem(labels[i]));
      menuItem.setActionCommand(commands[i]);
      menuItem.addActionListener(this);
    }

    popup.addSeparator();

    gridLinesChkBox =
        (JCheckBoxMenuItem) popup.add(new JCheckBoxMenuItem("Show/Hide gridlines", showGridLines));
    gridLinesChkBox.addActionListener(this);
  }

  /** draws grid lines if only one set of x and y axes and showGridLines = true */
  public void drawGrid(Graphics2D g, int numYTics, int numXTics) {
    /*
     ********************************************************************************
     * check if there are multiple axes, ... if so, set showGridLines to false
     * grid lines should be drawn only if there is one set of x and y axes
     * this is because we need to get the numMajorTics to use in drawing the gridlines
     * and multiple axes give inconsistent major tic positions
     ********************************************************************************
     */
    if (!(isSingleAxes())) {
      showGridLines = false;
    }

    // do not draw grid lines if not requested
    if (!(showGridLines)) return;

    // Store the original Stroke and Color here so they can be restored later on
    Stroke origStroke = g.getStroke();
    Color origColor = g.getColor();

    /*
     ***************************************
     * NOTE: floats are used instead of ints for drawing lines and getting more
     * precise positions. This prevents the offsets that were experienced before,
     * when the axis lines and grids did not match with the graphDataset bounds
     * due to precision lost in int manipulations
     ***************************************
     */
    float h = (float) this.getHeight();
    float w = (float) this.getWidth();

    // the vertical and horizontal spaces between grid lines
    float vIncr = w / (numXTics - 1);
    float hIncr = h / (numYTics - 1);

    g.setColor(gridLineColor);
    g.setStroke(gridLineStyle);

    // draw Vertical Grid lines
    for (int i = 0; i < numXTics; i++) {
      g.draw(new Line2D.Float(i * vIncr, 0.0f, i * vIncr, h));
    }

    // draw Horizontal  Grid lines ... do this way so we start drawing grids
    // from the bottom up ... conforming to the graph plotting space
    for (int i = numYTics - 1; i >= 0; i--) {
      g.draw(new Line2D.Float(0.0f, i * hIncr, w, i * hIncr));
    }

    g.setStroke(origStroke);
    g.setColor(origColor);
  }

  /** overrides updateUI () so that it updates the UI's of popup menu also */
  public void updateUI() {
    super.updateUI();
    if (popup != null) {
      SwingUtilities.updateComponentTreeUI(popup);
    }
  }

  /**
   * set the print flag to true or false, so components will know to paint in the preferred way for
   * printing or normal on-screen rendering
   */
  protected void setPrintFlag(boolean b) {
    printFlag = b;
  }

  /**
   * initialize the datasetGraphics everytime the component is affected so the rectangle for zooming
   * will be drawn correctly
   */
  public void processComponentEvent(ComponentEvent e) {
    super.processComponentEvent(e);
    datasetGraphics = (Graphics2D) this.getGraphics();
  }

  /** handle the popup trigger here */
  public void processMouseEvent(MouseEvent e) {
    // show pupup if popup-trigger is pressed (right mouse button, etc.)
    if (e.isPopupTrigger()) {
      popup.show(this, e.getX(), e.getY());
      e.consume();
    } else {
      super.processMouseEvent(e);
    }
  }

  /** Process mouse drag events to allow zooming */
  public void processMouseMotionEvent(MouseEvent e) {
    switch (e.getID()) {
      case MouseEvent.MOUSE_DRAGGED:
        // return if environment is not set for a zoom event
        if (e.isPopupTrigger()
            || this == null
            || datasetGraphics == null
            || zoomStart.x == -1
            || zoomStart.y == -1) break;

        // if button 1 (left mouse button) is not used to drag, do not do anything (return)
        // int mod = e.getModifiers();
        // if ( !( (mod & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK || mod == 0) )
        if (!(SwingUtilities.isLeftMouseButton(e))) break;

        int minx, maxx, miny, maxy;

        datasetGraphics.setXORMode(this.getBackground());
        // erase previous rectangle, if there was one.
        minx = Math.min(zoomStart.x, zoomEnd.x);
        maxx = Math.max(zoomStart.x, zoomEnd.x);
        miny = Math.min(zoomStart.y, zoomEnd.y);
        maxy = Math.max(zoomStart.y, zoomEnd.y);
        datasetGraphics.drawRect(minx, miny, maxx - minx, maxy - miny);

        // draw a new rectangle
        zoomEnd.x = e.getX();
        zoomEnd.y = e.getY();
        // ensure that zoom bounds does not extend outside this component
        if (zoomEnd.x < 0) zoomEnd.x = 0;
        else if (zoomEnd.x >= this.getWidth() - 1) zoomEnd.x = this.getWidth();

        if (zoomEnd.y < 0) zoomEnd.y = 0;
        else if (zoomEnd.y > this.getHeight() - 1) zoomEnd.y = this.getHeight();

        minx = Math.min(zoomStart.x, zoomEnd.x);
        maxx = Math.max(zoomStart.x, zoomEnd.x);
        miny = Math.min(zoomStart.y, zoomEnd.y);
        maxy = Math.max(zoomStart.y, zoomEnd.y);
        datasetGraphics.drawRect(minx, miny, maxx - minx, maxy - miny);
        datasetGraphics.setPaintMode();
        break;
      default:
        super.processMouseMotionEvent(e);
        break;
    }
  }

  /* METHODS FROM THE MouseListener INTERFACE */

  /** Process mouse-pressed events to assist mouse motion in zooming */
  public void mousePressed(MouseEvent e) {
    // return if null
    if (this == null || datasetGraphics == null) return;

    // request keyboard focus so we can also trap key events
    this.requestFocus();
    zoomStart.x = e.getX();
    zoomStart.y = e.getY();
    zoomEnd.x = e.getX();
    zoomEnd.y = e.getY();

    // draw starting point ... so it can be erased when mouse is dragged
    datasetGraphics.setXORMode(this.getBackground());
    datasetGraphics.drawRect(zoomStart.x, zoomStart.y, 0, 0);
    datasetGraphics.setPaintMode();
  }

  /** Process mouse-released events to assist mouse motion in zooming */
  public void mouseReleased(MouseEvent e) {
    if (this == null || datasetGraphics == null) return;

    if (zoomStart.x == -1 || zoomStart.y == -1) return;

    zoomEnd.x = e.getX();
    zoomEnd.y = e.getY();

    // ensure that zoom bounds does not extend outside this component
    if (zoomEnd.x < 0) zoomEnd.x = 0;
    else if (zoomEnd.x >= this.getWidth()) zoomEnd.x = this.getWidth() - 1;

    if (zoomEnd.y < 0) zoomEnd.y = 0;
    else if (zoomEnd.y >= this.getHeight()) zoomEnd.y = this.getHeight() - 1;

    // erase previous rectangle, if there was one.
    int minx = Math.min(zoomStart.x, zoomEnd.x);
    int maxx = Math.max(zoomStart.x, zoomEnd.x);
    int miny = Math.min(zoomStart.y, zoomEnd.y);
    int maxy = Math.max(zoomStart.y, zoomEnd.y);

    Rectangle zoomRect = new Rectangle(minx, miny, maxx - minx, maxy - miny);
    datasetGraphics.setXORMode(this.getBackground());
    datasetGraphics.draw(zoomRect);
    datasetGraphics.setPaintMode();

    // zoom only if rectangle is more than 5 pixels
    if ((zoomRect.width > 5) && (zoomRect.height > 5)) {
      // pass the rectangle to zoom method of GraphAxisData (its parent container)
      graphAxisData.zoom(zoomRect);
    }

    // set the points to the flag values of -1
    zoomStart.x = zoomStart.y = zoomEnd.x = zoomEnd.y = -1;
  }

  /** mouse exitting should set the position value to -1, -1 */
  public void mouseExited(MouseEvent e) {
    if (this == null || datasetGraphics == null) return;

    // if not single axes, do nothing
    if (!(isSingleAxes())) return;
    // cause the position to be displayed in the position component
    // -1 means dislay empty strings ""
    graphAxisData.setPositionValue(-1, -1);
  }

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  /* METHODS FROM THE ActionListener INTERFACE */

  /**
   * Handles the popup menu and action commands for those meunItems, we handle them here ... if
   * menuItems have mapped action commands, we just programatically cause a ... doClick() on that
   * menuItem NOTE: that we check if the source is predefined menuItem first ... before checking if
   * it is an action command (The order is extremely important)
   */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    Object source = e.getSource();

    if (source == gridLinesChkBox) {
      this.showGridLines = gridLinesChkBox.isSelected();
      this.repaint();
    } else if (source == linearRadioButton) {
      if (linearRadioButton.isSelected()) {
        this.setScale(Dataset.LINEAR);
      }
    } else if (source == log10RadioButton) {
      if (log10RadioButton.isSelected()) this.setScale(Dataset.LOG_10);
    }
    // else if (source == logERadioButton ) {
    // if (logERadioButton.isSelected() )
    // this.setScale ( Dataset.LOG_E);
    // }
    else if (command.equals(RESET_AXES_COMMAND)) {
      graphAxisData.resetAxesRange();
    } else if (command.equals(REPAINT_COMMAND)) {
      this.repaint();
    } else if (command.equals(REDRAW_COMMAND)) {
      graphAxisData.draw();
      graphAxisData.repaint();
    } else if (command.equals(GRIDLINES_COMMAND)) {
      gridLinesChkBox.doClick();
    } else if (command.equals(LINEAR_COMMAND)) {
      linearRadioButton.doClick();
    } else if (command.equals(LOG_10_COMMAND)) {
      log10RadioButton.doClick();
    }
    // else if (command.equals (LOG_E_COMMAND) ) {
    // logERadioButton.doClick ();
    // }

  }

  /** Inner class to handle plotting X to Y data sets */
  public static final class XtoY extends GraphDataset {
    // Use an array of linetypes
    // make class constants that will be used to draw the lines for these line plots
    public static final float[][] dashStyle = new float[10][];
    public static final BasicStroke[] lineStyle = new BasicStroke[10];
    public static final Color[] lineColor = new Color[10];

    // indicate whether Color plots or B/W
    private boolean isColorPlot = true;

    /** Vector containing a list of attached Datasets */
    protected Vector dataset = new Vector();

    protected JCheckBoxMenuItem isColorPlottingChkBox;

    // initialize the dash styles for this class, and then the BasicStrokes
    static {
      dashStyle[0] = null;
      dashStyle[1] = new float[] {10.0f, 4.0f};
      dashStyle[2] = new float[] {6.0f, 4.0f, 2.0f, 4.0f, 2.0f, 4.0f};
      dashStyle[3] = new float[] {4.0f, 4.0f};
      dashStyle[4] = new float[] {4.0f, 4.0f, 6.0f, 4.0f, 8.0f, 4.0f};
      // make last 4 copies of the ones above, for now
      dashStyle[5] = new float[] {6.0f, 4.0f, 2.0f, 4.0f, 2.0f, 4.0f};
      dashStyle[6] = new float[] {4.0f, 4.0f};
      dashStyle[7] = new float[] {2.0f, 2.0f};
      dashStyle[8] = new float[] {4.0f, 4.0f, 6.0f, 4.0f, 8.0f, 4.0f};
      dashStyle[9] = new float[] {10.0f, 4.0f};

      lineStyle[0] = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f);
      lineStyle[1] =
          new BasicStroke(
              2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f, dashStyle[1], 0.0f);
      lineStyle[2] =
          new BasicStroke(
              2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f, dashStyle[2], 0.0f);
      lineStyle[3] =
          new BasicStroke(
              2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f, dashStyle[3], 0.0f);
      lineStyle[4] =
          new BasicStroke(
              2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f, dashStyle[4], 0.0f);
      lineStyle[5] =
          new BasicStroke(
              2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f, dashStyle[5], 0.0f);
      lineStyle[6] =
          new BasicStroke(
              2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f, dashStyle[6], 0.0f);
      lineStyle[7] =
          new BasicStroke(
              2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f, dashStyle[7], 0.0f);
      lineStyle[8] =
          new BasicStroke(
              2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f, dashStyle[8], 0.0f);
      lineStyle[9] =
          new BasicStroke(
              2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 30.0f, dashStyle[9], 0.0f);

      lineColor[0] = Color.black;
      lineColor[1] = Color.red;
      lineColor[2] = Color.blue;
      lineColor[3] = Color.green;
      lineColor[4] = Color.darkGray;
      lineColor[5] = Color.gray;
      lineColor[6] = Color.pink;
      lineColor[7] = Color.orange;
      lineColor[8] = Color.magenta;
      lineColor[9] = Color.cyan;
    }

    /** do not allow anyone instantiate this class without passing the GraphAxisData reference */
    private XtoY() {}

    public XtoY(GraphAxisData graphAxisData) {
      super(graphAxisData);
      // add the menuItem for uncertainty
      popup.addSeparator();

      uncertaintyChkBox =
          (JCheckBoxMenuItem)
              popup.add(new JCheckBoxMenuItem("Show/Hide Uncertainty", showUncertainty));
      uncertaintyChkBox.addActionListener(this);

      isColorPlottingChkBox =
          (JCheckBoxMenuItem) popup.add(new JCheckBoxMenuItem("Color Plotting"));
      isColorPlottingChkBox.addActionListener(this);
      isColorPlottingChkBox.setSelected(isColorPlot);
    }

    // check if a data set is attached
    public boolean isAttached(DatasetXtoY d) {
      if (dataset.contains(d)) return true;
      else return false;
    }

    protected void attachDataset(DatasetXtoY d) {
      if (d == null) return;
      if (!(dataset.contains(d))) {
        dataset.add(d);
        d.setShowError(showUncertainty);
        d.addPropertyChangeListener(this);
        // if (primaryDataset == null)
        // setPrimaryDataset ( (DatasetXtoY) dataset.get (0) );
      }
    }

    protected void detachDataset(DatasetXtoY d) {
      if (d == null) return;
      if (dataset.contains(d)) {
        d.removePropertyChangeListener(this);
        dataset.remove(d);
        // if (d == primaryDataset)
        // setPrimaryDataset ( (DatasetXtoY) dataset.get (0) );
      }
    }

    protected void detachAllDatasets() {
      if (dataset.isEmpty()) return;
      DatasetXtoY d;
      for (int i = 0; i < dataset.size(); i++) {
        d = (DatasetXtoY) dataset.get(i);
        d.removePropertyChangeListener(this);
        dataset.remove(d);
      }

      // setPrimaryDataset (null);
    }

    /** set the primary dataset to this */
    public void setPrimaryDataset(Dataset d) {
      Dataset oldValue = primaryDataset;

      if (dataset.contains(d)) primaryDataset = d;

      if (primaryDataset != oldValue)
        firePropertyChange(PRIMARY_DATASET_PROPERTY, oldValue, primaryDataset);
    }

    /** get the primary dataset */
    public Dataset getPrimaryDataset() {
      return primaryDataset;
    }

    /** resets the primary dataset */
    public void resetPrimaryDataset() {
      if (primaryDataset == null || !(dataset.contains(primaryDataset))) {
        if (dataset.isEmpty()) setPrimaryDataset(null);
        else setPrimaryDataset((DatasetXtoY) dataset.get(0));
      }
    }

    /** sets the color and style to use for rendering the lines for these graphs */
    public void setLineAttributes() {
      DatasetXtoY d;

      for (int i = 0; i < dataset.size(); i++) {
        d = (DatasetXtoY) (dataset.get(i));
        if (isColorPlot) {
          // for all datasets, set a different line color, use a straight unbroken line
          d.setLineAttributes(lineColor[i], lineStyle[0]);
        } else {
          // for all datasets, set a different line style and set line color to black
          d.setLineAttributes(lineColor[0], lineStyle[i]);
        }
      }
    }

    public boolean isColorPlotting() {
      return isColorPlot;
    }

    /** set whether to use color or dash style, then set the line Attributes */
    public void setColorPlotting(boolean b) {
      if (isColorPlot == b) return;

      isColorPlot = b;
      setLineAttributes();
      draw();
      repaint();
    }

    // draws the datasets onto that image itself ...
    // paint is called for general repainting while draws are called for updating the graphImage
    public void draw() {
      // re-initialize the transient graphics context
      datasetGraphics = (Graphics2D) this.getGraphics();

      int w = this.getWidth();
      int h = this.getHeight();

      if (w < 15 || h < 15) return;

      graphImage = (BufferedImage) this.createImage(w, h);

      Graphics2D g = graphImage.createGraphics();

      if (!(printFlag)) {
        // cause the graphics to be rendered with high quality and anti-aliased
        g.setRenderingHints(Graph.qualityHints);
      } else {
      }

      draw(g, false);
    }

    /**
     * if directly on the component, then there is no need to set and fill the background if drawing
     * onto a buffered image, then you need to fill the background ... with the background color
     * first
     */
    protected void draw(Graphics2D g, boolean directlyOnComponent) {
      if (!(directlyOnComponent)) {
        g.setBackground(this.getBackground());
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(this.getForeground());
      }

      DatasetXtoY aDataset;
      // recursively draw the lines for each dataset onto this image
      for (int i = 0; i < dataset.size(); i++) {
        aDataset = (DatasetXtoY) dataset.get(i);
        // graphImage = aDataset.draw (graphImage);
        aDataset.draw(g, this.getSize());
      }
    }

    protected void draw(Graphics2D g) {
      draw(g, true);
    }

    /** To update the graph image ... just calls draw to get a full repaint */
    protected void updateGraphImage() {
      draw();
    }

    /** set the scale of the datasets on this graph, to linear, log10 or loge */
    public void setScale(int scale) {
      DatasetXtoY d;
      int oldValue = this.scale;

      switch (scale) {
        case Dataset.LINEAR:
        case Dataset.LOG_10:
        case Dataset.LOG_E:
          this.scale = scale;
          for (int i = 0; i < dataset.size(); i++) {
            d = (DatasetXtoY) dataset.get(i);
            d.setScaleAndDoNotFirePropertyChange(scale);
          }
          break;
      }

      firePropertyChange(GraphDataset.SCALE_PROPERTY, oldValue, this.scale);
    }

    /**
     * paints the off-screen image (graphImage) into the component paint is called for general
     * repainting while draws are called for updating the drawings
     */
    protected void paintComponent(Graphics g_orig) {
      super.paintComponent(g_orig);

      Graphics2D g = (Graphics2D) g_orig;

      if (printFlag) {
        draw(g, true);
        if (primaryDataset != null) {
          DatasetXtoY d = (DatasetXtoY) primaryDataset;
          this.drawGrid(g, d.yAxis.numMajorTics, d.xAxis.numMajorTics);
        }
      } else {
        if (graphImage != null && this.isShowing()) {
          g.drawImage(graphImage, 0, 0, this);
          if (primaryDataset != null) {
            DatasetXtoY d = (DatasetXtoY) primaryDataset;
            this.drawGrid(g, d.yAxis.numMajorTics, d.xAxis.numMajorTics);
          }
        }
      }
    }

    /** given a double x value, returns the corresponding ydata value in the dataset */
    public double getValue(double x) {
      if (isSingleDataset()) return ((DatasetXtoY) primaryDataset).getY(x);
      else return GraphAxisData.POSITION_VALUE_UNKNOWN;
    }

    // uses an algorithm to check if all the data sets share only one set of x and y axes
    protected boolean isSingleAxes() {
      DatasetXtoY d0;
      DatasetXtoY d1;

      d0 = (DatasetXtoY) primaryDataset;
      // check the x axis of all the datasets and return false if any differ
      for (int i = 1; i < dataset.size(); i++) {
        d1 = (DatasetXtoY) (dataset.get(i));
        if (d1.xAxis != d0.xAxis) return false;
        d0 = d1;
      }

      // check the y axis of all the datasets and return false if any differ
      for (int i = 1; i < dataset.size(); i++) {
        d1 = (DatasetXtoY) (dataset.get(i));
        if (d1.yAxis != d0.yAxis) return false;
        d0 = d1;
      }
      return true;
    }

    /*
     * uses an algorithm to check if only one dataset is plotted on this graph
     * returns true always since only one data set is allowed for this XY to Z
     */
    protected boolean isSingleDataset() {
      if (dataset.size() <= 1) return true;
      else return false;
    }

    /**
     * Handles the popup menu and action commands for those meunItems, we handle them here ... if
     * menuItems have mapped action commands, we just programatically cause a ... doClick() on that
     * menuItem NOTE: that we check if the source is predefined menuItem first ... before checking
     * if it is an action command *** IF WE CANNOT HANDLE THE ACTION HERE, WE PASS IT TO THE
     * actionPerformed() of superclass *** ... we also handle the uncertainty here ...
     */
    public void actionPerformed(ActionEvent e) {
      String command = e.getActionCommand();
      Object source = e.getSource();

      DatasetXtoY d;

      if (source == uncertaintyChkBox) {
        showUncertainty = uncertaintyChkBox.isSelected();

        for (int i = 0; i < dataset.size(); i++) {
          d = (DatasetXtoY) dataset.get(i);
          d.setShowError(showUncertainty);
        }
        this.updateGraphImage();
        this.repaint();
      } else if (source == isColorPlottingChkBox) {
        setColorPlotting(isColorPlottingChkBox.isSelected());
      } else if (command.equals(UNCERTAINTY_COMMAND)) {
        uncertaintyChkBox.doClick();
      } else {
        // allow superclass to handle the other commands
        super.actionPerformed(e);
      }
    }
  }

  /** Inner class to handle plotting XY to Z data sets */
  public static final class XYtoZ extends GraphDataset {
    /** constraint here: there can only be one Data Set */
    protected DatasetXYtoZ dataset;

    /** buffered Image of the whole dataset */
    protected BufferedImage datasetImage;

    public final String BLACK_BACKGROUND_COLORMAP_COMMAND = "blackBgColorMap";
    public final String WHITE_BACKGROUND_COLORMAP_COMMAND = "whiteBgColorMap";

    // Background buttons
    protected JRadioButtonMenuItem blackBgRadioButton;
    protected JRadioButtonMenuItem whiteBgRadioButton;

    /** do not allow anyone instantiate this class without passing the GraphAxisData reference */
    private XYtoZ() {}

    public XYtoZ(GraphAxisData graphAxisData) {
      super(graphAxisData);

      // add the menuItem for changing the color map
      popup.addSeparator();

      JMenu colorMapMenu = new JMenu("Color Map");

      ButtonGroup colorMapGroup = new ButtonGroup();
      blackBgRadioButton =
          (JRadioButtonMenuItem) colorMapMenu.add(new JRadioButtonMenuItem("Black Background"));
      colorMapGroup.add(blackBgRadioButton);
      blackBgRadioButton.addActionListener(this);

      whiteBgRadioButton =
          (JRadioButtonMenuItem) colorMapMenu.add(new JRadioButtonMenuItem("White Background"));
      colorMapGroup.add(whiteBgRadioButton);
      whiteBgRadioButton.addActionListener(this);

      // black background is always the first color map, so set it selected first
      blackBgRadioButton.setSelected(true);

      popup.add(colorMapMenu);
    }

    /** check if a data set is attached */
    public boolean isAttached(DatasetXYtoZ d) {
      if (dataset == d) return true;
      else return false;
    }

    /**
     * Attach a dataset which will be the only dataset in here let this graphDataset listen to the
     * dataset for changes in its color map
     */
    protected void attachDataset(DatasetXYtoZ d) {
      if (dataset != null) return;

      dataset = d;
      dataset.addPropertyChangeListener(this);
      // setPrimaryDataset (dataset);
    }

    /** detach the dataset contained in the graph */
    protected void detachAllDatasets() {
      dataset.removePropertyChangeListener(this);
      dataset = null;
      // setPrimaryDataset (null);
    }

    /** detach this dataset if this is the one contained in the graph */
    protected void detachDataset(DatasetXYtoZ d) {
      if (dataset == d) detachAllDatasets();
    }

    /** set the primary dataset to this */
    public void setPrimaryDataset(Dataset d) {
      Dataset oldValue = primaryDataset;

      if (dataset == d) primaryDataset = d;

      if (primaryDataset != oldValue)
        firePropertyChange(PRIMARY_DATASET_PROPERTY, oldValue, primaryDataset);
    }

    /** get the primary dataset */
    public Dataset getPrimaryDataset() {
      return (dataset);
    }

    /** resets the primary dataset */
    public void resetPrimaryDataset() {
      setPrimaryDataset(dataset);
    }

    /**
     * ************************ draws the dataset onto that itself ... (which is a component) paint
     * is called for general repainting while draws are called for updating the graphImage
     * ************************ NOW: draw just updates the datasetImage. datasetImage is contained
     * in the dataset class. Depending on the axis range and the axis range upon which the
     * datasetImage was created, a graphImage is created for this component In paint, this
     * graphImage is then scaled to fill the whole component ************************** Basically,
     * it works like this: the datasetImage has its number of pixels equal to the axisRange limits
     * This method will allow easy scaling and refreshing ***************************
     */
    public void draw() {
      // initialize the transient graphics context
      datasetGraphics = (Graphics2D) this.getGraphics();

      datasetImage = null;

      int datasetImageWidth = (int) (dataset.getXMax() - dataset.getXMin());
      int datasetImageHeight = (int) (dataset.getYMax() - dataset.getYMin());

      datasetImage = (BufferedImage) this.createImage(datasetImageWidth, datasetImageHeight);

      Graphics2D g = datasetImage.createGraphics();

      if (!(printFlag)) {
        // cause the graphics to be rendered with high quality and anti-aliased
        g.setRenderingHints(Graph.qualityHints);
      } else {
      }

      g.setColor(Color.black);
      g.fillRect(0, 0, datasetImageWidth, datasetImageHeight);
      g.setColor(this.getForeground());

      // draw the main dataset on this datasetImage buffered image
      dataset.draw(g);

      // update the graph image
      updateGraphImage();
    }

    /** To update the graph image ... gets the subimage based on the current axis limits */
    protected void updateGraphImage() {
      graphImage = dataset.getGraphImage(datasetImage);
    }

    /**
     * paints the off-screen image (graphImage) into the component paint is called for general
     * repainting while draws are called for updating the drawings
     */
    protected void paintComponent(Graphics g_orig) {
      super.paintComponent(g_orig);

      // if image is null, get the graph Image
      // this way, the graph Image can just be set to null when a zoom is done to force an update
      if (graphImage == null) updateGraphImage(); // update the graph image

      if (graphImage == null) return;

      Graphics2D g = (Graphics2D) g_orig;

      AffineTransform oldTransform = g.getTransform();

      AffineTransform at =
          AffineTransform.getScaleInstance(
              (double) getWidth() / graphImage.getWidth(),
              (double) getHeight() / graphImage.getHeight());

      // draw the image using this scaling
      g.drawImage(graphImage, at, this);

      g.setTransform(oldTransform);

      if (dataset != null) this.drawGrid(g, dataset.yAxis.numMajorTics, dataset.xAxis.numMajorTics);
    }

    /** given a double x value, returns the corresponding ydata value in the dataset */
    public double getValue(double x, double y) {
      // due to the way the axes are labelled, there is a possibility that the axes
      // extend beyond the allowed dataset range
      // check for this ...
      if (isSingleDataset()) {
        if (x >= dataset.getXMax() || y >= dataset.getYMax())
          return GraphAxisData.POSITION_VALUE_UNKNOWN;
        else return dataset.getValue(x, y);
      } else return GraphAxisData.POSITION_VALUE_UNKNOWN;
    }

    /** returns true always since only one data set is allowed for this XY to Z */
    protected boolean isSingleAxes() {
      return true;
    }

    /** returns true always since only one data set is allowed for this XY to Z */
    protected boolean isSingleDataset() {
      return true;
    }

    /** if the dataset color map changed, redraw the graphDataset */
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(DatasetXYtoZ.COLOR_MAP_PROPERTY)) {
        draw();
        repaint();
      }
    }

    /** set the scale of the datasets on this graph, to linear, log10 or loge */
    public void setScale(int scale) {
      if (dataset == null) return;

      int oldValue = this.scale;

      switch (scale) {
        case Dataset.LINEAR:
        case Dataset.LOG_10:
        case Dataset.LOG_E:
          this.scale = scale;
          dataset.setScale(scale);
          break;
      }

      firePropertyChange(GraphDataset.SCALE_PROPERTY, oldValue, this.scale);
    }

    /**
     * Handles the popup menu and action commands for those meunItems, we handle them here ... if
     * menuItems have mapped action commands, we just programatically cause a ... doClick() on that
     * menuItem NOTE: that we check if the source is predefined menuItem first ... before checking
     * if it is an action command *** IF WE CANNOT HANDLE THE ACTION HERE, WE PASS IT TO THE
     * actionPerformed() of superclass *** ... we also handle the color maps here ...
     */
    public void actionPerformed(ActionEvent e) {
      String command = e.getActionCommand();
      Object source = e.getSource();

      if (source == blackBgRadioButton) {
        if (blackBgRadioButton.isSelected())
          dataset.setColorMap(GraphSupport.blackBackgroundColorMap);
      } else if (source == whiteBgRadioButton) {
        if (whiteBgRadioButton.isSelected())
          dataset.setColorMap(GraphSupport.whiteBackgroundColorMap);
      } else if (command.equals(BLACK_BACKGROUND_COLORMAP_COMMAND)) {
        blackBgRadioButton.doClick();
      } else if (command.equals(WHITE_BACKGROUND_COLORMAP_COMMAND)) {
        whiteBgRadioButton.doClick();
      } else {
        // allow superclass to handle the other commands
        super.actionPerformed(e);
      }
    }
  }
}
