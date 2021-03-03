package spec.lib.graph;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public abstract class GraphAxisData extends JComponent
    implements MouseMotionListener, PropertyChangeListener {
  /** flag to show that we can not determine the value of the mouse position */
  public static final int POSITION_VALUE_UNKNOWN = 198461;

  /** the preferred dimension of this component ... used by the layout manager */
  protected static Dimension preferredDim;
  /** below this dimension, axes are not shown ... used by the layout manager */
  protected static Dimension minDim;
  /** the maximum width of the whole set of y axes, or maximum height of the whole set of x axes */
  protected static int maxAxisWidth;
  /** the maximum width of a single y axis, or maximum height of a single X axis */
  protected static int maxSingleAxisWidth;

  static {
    preferredDim = new Dimension(550, 550);
    minDim = new Dimension(320, 320);
    maxAxisWidth = 150;
    maxSingleAxisWidth = 60;
  }

  /** The Graph component that handles the whole graph */
  protected Graph graph;
  /** flag to indicate if printing is being done or just on-screen rendering, when painting */
  protected boolean printFlag = false;

  /** the width of a single y axis, or height of a single X axis */
  protected int singleAxisXWidth = 0;

  protected int singleAxisYWidth = 0;

  /** rectangles defining the spaces that the X and Y axis will be drawn into */
  protected Rectangle xAxisRect = new Rectangle();

  protected Rectangle yAxisRect = new Rectangle();
  protected Rectangle graphDatasetRect = new Rectangle();

  /** delegates everything affecting Datasets to this member ... overshadowed by subclasses */
  protected GraphDataset graphDataset;

  /** array of 3 values holding the position of the mouse in graphDataset */
  protected double[] mousePosition = new double[3];

  public static final String POSITION_PROPERTY = "position";

  /** CONSTRUCTOR */
  public GraphAxisData() {
    this(null);
  }

  public GraphAxisData(GraphDataset graphDataset) {
    super();
    // do these so the graphDataset will be showing initially
    // Layout Manager is an inner member class so it can directly access the individual components
    this.setLayout(this.new LayoutMgr());
    // make it visible so it can be seen and a graphics context can be got
    this.setVisible(true);
    // specify the AWT events we are interested in
    this.enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
    // attach graphDataset if not-null
    if (graphDataset != null) {
      this.graphDataset = graphDataset;
      graphDataset.addPropertyChangeListener(this);
      // this graphAxisData MUST be the graphAxisData of this class
      graphDataset.graphAxisData = this;
      this.add(graphDataset);
    }
  }

  public GraphAxisData(GraphDataset graphDataset, Graph graph) {
    this(graphDataset);
    setGraph(graph);
  }

  /** Dataset.SCALE_PROPERTY property being listened to here */
  public void propertyChange(PropertyChangeEvent e) {
    // redraw if the scale of the dataset is changed, so we can reflect the new scale
    if (e.getPropertyName().equals(Dataset.SCALE_PROPERTY)) {
      draw();
      repaint();
    } else if (e.getPropertyName().equals(GraphDataset.SCALE_PROPERTY)) {
      draw();
      repaint();
    }
  }

  /** set its "graph" member to the parsed Graph */
  protected void setGraph(Graph graph) {
    this.graph = graph;
  }

  protected GraphDataset getGraphDataset() {
    return graphDataset;
  }

  /** abstract methods ... defined in methods below */
  protected abstract void detachAllDatasets();

  public abstract int getNumYAxes();

  public abstract int getNumXAxes();

  protected abstract void drawAxisX();

  protected abstract void drawAxisY();

  protected abstract void paintAxes(Graphics2D g);

  /**
   * takes a component on which the axis and datasets should be drawn and figures out how much space
   * to allocate for the axes and datasets to draw themselves ... It kinda directly draws the axes
   * (calling the individual axes to draw themselves) ... giving them a Rectangle argument within
   * which they can draw themselves However, it delegates handling the datasets to "graphDataset"
   */
  public void draw() {
    validate();

    // call the axes to set their respective rectangles and other attributes
    drawAxisY();
    drawAxisX();

    // plot the datasets on the graphDataset
    graphDataset.draw();
  }

  /** handles zooming, whether interactively or non-interactively */
  protected abstract void zoom(Rectangle zoomRect);

  /** given the x and y c0-ordinate points, it calculates the actual x, y, and value */
  protected abstract void setPositionValue(int x, int y);

  protected void paintComponent(Graphics g_orig) {
    // Graphics2D g = (Graphics2D) g_orig;

    // call the axes to paint themselves on their respective rectangles ...
    // update the axes first. should not be necessary since they are updated when there
    // is a change to the component. Bug with maximizing internal frame requires this though
    // axesUpdate ();
    // paintAxes (g);

    // super.paintComponent (g_orig);

    // axesUpdate ();
    super.paintComponent(g_orig);
    paintAxes((Graphics2D) g_orig);
  }

  /**
   * set the print flag to true or false, so components will know to paint in the preferred way for
   * printing or normal on-screen rendering
   */
  protected void setPrintFlag(boolean b) {
    printFlag = b;
    graphDataset.setPrintFlag(b);
  }

  /** set the primary dataset to this ... delegates to graphDataset */
  public void setPrimaryDataset(Dataset d) {
    graphDataset.setPrimaryDataset(d);
  }

  /**
   * handles updating the graph after the axes range have been altered, due to a zoom event or
   * otherwise ... ... Just update the zoomed region
   */
  protected void zoomUpdate() {
    // update axes after their attributes have been altered
    boolean wasUpdated = axesUpdate();
    if (wasUpdated) graphDataset.updateGraphImage();
  }

  /**
   * handles updating the axes after their attributes have been altered, returns true or false to
   * indicate that the axes were updated or not
   */
  public boolean axesUpdate() {
    // do nothing if component is too small
    if (this.getHeight() < 15 || this.getWidth() < 15) return false;

    // call the axes to set their respective rectangles and other attributes
    drawAxisY();
    drawAxisX();

    return true;
  }

  /** causes all the axes on this graph to reset their axes ranges */
  public abstract void resetAxesRange();

  /**
   * Process component events to cause a redraw when the component size changes validate should be
   * called, to cause immediate validation of the component
   */
  public void processComponentEvent(ComponentEvent e) {
    super.processComponentEvent(e);

    switch (e.getID()) {
      case ComponentEvent.COMPONENT_RESIZED:
        // do this so a call to dispose will not cause an exception, since
        // disposing a window sets the component sizes to zero
        if (this.getWidth() < 15 || this.getHeight() < 15) break;
        // this redraws the GraphAxisData for XtoY or gets the appropriate subImage for XYtoZ
        // this is better than calling draw which redraws for both of them
        validate();

        // cause a redraw of the set of axes and graphDataset
        this.zoomUpdate();
        this.repaint();
        break;
      default:
        break;
    }
  }

  /** Process mouse move events to allow the mouse position to be set */
  public void mouseMoved(MouseEvent e) {
    if (graphDataset == null || !(graphDataset.isSingleAxes())) return;

    // cause the position to be displayed in the position component
    setPositionValue(e.getX(), e.getY());
  }

  public void mouseDragged(MouseEvent e) {}

  /**
   * overrides updateUI () so that it updates the UI's of all the axis modifier components with it
   */
  public void updateUI() {
    super.updateUI();

    Component contentPane = Axis.getAxisModifier().getContentPanel();
    SwingUtilities.updateComponentTreeUI(contentPane);
  }

  /** Inner static class to handle X to Y plotting */
  public static class XtoY extends GraphAxisData {
    /** Vector containing a list of attached horizontal (X) Axes */
    protected Vector xAxes = new Vector();

    /** Vector containing a list of attached vertical (Y) Axes */
    protected Vector yAxes = new Vector();

    public XtoY() {
      super();
      graphDataset = new GraphDataset.XtoY(this);
      graphDataset.addPropertyChangeListener(this);
      this.add(graphDataset);
    }

    /**
     * This constructor should be used with caution The only reason this constructor exists is to
     * allow the graphDataset to be subclassed by ... other classes to include more functionality
     * this is because the graphDataset must have a reference to this graphAxisData for this to work
     * THE PROGRAMMER SHOULD MAKE SURE OF THIS
     */
    public XtoY(GraphDataset.XtoY graphDataset) {
      super(graphDataset);
    }

    public XtoY(GraphDataset.XtoY graphDataset, Graph.XtoY graph) {
      super(graphDataset, graph);
    }

    /** preferred way to attach a data set to graphDataset */
    protected void attachDataset(DatasetXtoY d) {
      if (!((GraphDataset.XtoY) graphDataset).isAttached(d)) {
        ((GraphDataset.XtoY) graphDataset).attachDataset(d);
        // attachAxisX (d.xAxis);
        if (!(xAxes.contains(d.xAxis))) xAxes.addElement(d.xAxis);
        // attachAxisY (d.yAxis);
        if (!(yAxes.contains(d.yAxis))) yAxes.addElement(d.yAxis);

        d.addPropertyChangeListener(this);
      }
    }

    /** preferred way to detach a data set to graphDataset */
    protected void detachDataset(DatasetXtoY d) {
      if (((GraphDataset.XtoY) graphDataset).isAttached(d)) {
        d.removePropertyChangeListener(this);
        // detach the data set
        ((GraphDataset.XtoY) graphDataset).detachDataset(d);
        // detach the axes from this graphAxisData if they do not contain
        // any other data sets
        if (d.xAxis.dataset.isEmpty()) xAxes.removeElement(d.xAxis);
        if (d.yAxis.dataset.isEmpty()) yAxes.removeElement(d.yAxis);
        // detach the dataset from the axes
        d.xAxis.detachDataset(d);
        d.yAxis.detachDataset(d);
      }
    }

    /**
     * you should not be able to arbitrarily remove an axis ... what about datasets that might be
     * attached to them ...
     */
    protected void detachAllDatasets() {
      DatasetXtoY d;
      java.util.List l = ((GraphDataset.XtoY) graphDataset).dataset;

      if (!(yAxes.isEmpty())) yAxes.removeAllElements();
      if (!(xAxes.isEmpty())) xAxes.removeAllElements();

      // remove the property listeners
      for (int i = 0; i < l.size(); i++) {
        d = (DatasetXtoY) l.get(i);
        d.removePropertyChangeListener(this);
      }

      graphDataset.detachAllDatasets();
    }

    /** delegates the setting of line attributes of datasets to its private member (graphDataset) */
    public void setLineAttributes() {
      ((GraphDataset.XtoY) graphDataset).setLineAttributes();
    }

    /** get numXAxes and numYAxes ... */
    public int getNumYAxes() {
      return (yAxes.size());
    }

    /** get numYAxes ... */
    public int getNumXAxes() {
      return (xAxes.size());
    }

    /**
     * override draw, to set the line attributes first. set the line Attributes (color and stroke
     * style) for all datasets so the Key can draw itself using information from the datasets
     * without waiting for the datasets to draw themselves first
     */
    public void draw() {
      setLineAttributes();
      super.draw();
    }

    /** make it private since it needs some information from other methods here to work */
    protected void drawAxisY() {
      AxisY a;
      Point p = new Point(yAxisRect.x, yAxisRect.y);
      Rectangle tempRect = new Rectangle();

      for (int i = 0; i < yAxes.size(); i++) {
        a = (AxisY) (yAxes.elementAt(i));
        // get the rectangle in which this axes should be drawn and draw into it
        tempRect.setRect(p.x, p.y, singleAxisYWidth, yAxisRect.height);
        a.draw(tempRect);
        p.x += singleAxisYWidth;
      }
    }

    /** make it private since it needs some information from other methods here to work */
    protected void drawAxisX() {
      AxisX a;
      Point p = new Point(xAxisRect.x, xAxisRect.y);
      Rectangle tempRect = new Rectangle();

      for (int i = 0; i < xAxes.size(); i++) {
        a = (AxisX) (xAxes.elementAt(i));
        // get the rectangle in which this axes should be drawn and draw into it
        tempRect.setBounds(p.x, p.y, xAxisRect.width, singleAxisXWidth);
        a.draw(tempRect);
        p.y += singleAxisXWidth;
      }
    }

    /** make it protected since it needs some information from other methods here to work */
    protected void paintAxes(Graphics2D g) {
      AxisY y;
      for (int i = 0; i < yAxes.size(); i++) {
        y = (AxisY) (yAxes.elementAt(i));
        y.paint(g);
      }

      AxisX x;
      for (int i = 0; i < xAxes.size(); i++) {
        x = (AxisX) (xAxes.elementAt(i));
        x.paint(g);
      }
    }

    /** performs zooming operations by calling setting the axis range and calling draw */
    protected void zoom(Rectangle zoomRect) {
      try {
        // temp values ... so that we can get the limits b4 setting them
        double max = 0.0;
        double min = 0.0;

        // reset the axis ranges based on the zoom rectangle
        AxisY y;
        for (int i = 0; i < yAxes.size(); i++) {
          y = (AxisY) (yAxes.elementAt(i));
          max = y.getDouble(zoomRect.y);
          min = y.getDouble(zoomRect.y + zoomRect.height);
          y.setAxisMax(max);
          y.setAxisMin(min);
        }

        AxisX x;
        for (int i = 0; i < xAxes.size(); i++) {
          x = (AxisX) (xAxes.elementAt(i));
          max = x.getDouble(zoomRect.x + zoomRect.width);
          min = x.getDouble(zoomRect.x);
          x.setAxisMax(max);
          x.setAxisMin(min);
        }

      } catch (Exception exc) {
      } finally {
        // draw everything again
        this.zoomUpdate();
        // schedule component for a repainting
        repaint();
      }
    }

    /**
     * given the x and y co-ordinate points, it calculates the actual x, y, and value and calls
     * setPositionValue on Graph with these values
     *
     * <p>if these values are -1, then we know that the mouse exited and we report no values in the
     * position component
     *
     * <p>if we catch an error, just cause a zoomUpdate and repaint, this redraws the axes, gets the
     * graphImage and repaints
     */
    protected void setPositionValue(int x, int y) {
      try {
        double[] oldPosition = new double[] {mousePosition[0], mousePosition[1], mousePosition[2]};

        if (x == -1 || y == -1) {
          mousePosition[0] = mousePosition[1] = mousePosition[2] = -1;
        } else {
          mousePosition[0] = ((AxisX) xAxes.elementAt(0)).getDouble(x);
          mousePosition[1] = ((AxisY) yAxes.elementAt(0)).getDouble(y);

          // have to figure out how to get this value
          mousePosition[2] = ((GraphDataset.XtoY) graphDataset).getValue(mousePosition[0]);
        }

        firePropertyChange(GraphAxisData.POSITION_PROPERTY, oldPosition, mousePosition);

        // call setPositionValue on Graph with these values
        // graph.setPositionValue (xValue, yValue, value);
      } catch (Exception exc) {
        // draw everything again
        this.zoomUpdate();
        // schedule component for a repainting
        repaint();
      }
    }

    /** causes all the axes on this graph to reset their axes ranges */
    public void resetAxesRange() {
      AxisX x;
      for (int i = 0; i < xAxes.size(); i++) {
        x = (AxisX) (xAxes.elementAt(i));
        // adjust the axis Limits
        x.setAxisMinLimit();
        x.setAxisMaxLimit();
        // set the range of the axis
        x.setAxisRange();
      }

      AxisY y;
      for (int i = 0; i < yAxes.size(); i++) {
        y = (AxisY) (yAxes.elementAt(i));
        // adjust the axis Limits
        y.setAxisMinLimit();
        y.setAxisMaxLimit();
        // set the range of the axis
        y.setAxisRange();
      }

      // call zoomUpdate to cause a redraw
      this.zoomUpdate();
      repaint();
      // repaint the graph Key also ... so the new scale will show (for XYtoZ)
      // graph.graphKey.draw ();
      // graph.graphKey.repaint ();

    }

    /** Process component events to cause a redraw when the component size changes */
    public void processMouseEvent(MouseEvent e) {
      switch (e.getID()) {
        case MouseEvent.MOUSE_CLICKED:
          // set axis range if a double click
          if (e.getClickCount() < 2) break;

          int x = e.getX();
          int y = e.getY();

          AxisY yAxis;
          AxisX xAxis;

          if (xAxisRect.contains(x, y)) {
            for (int i = 0; i < xAxes.size(); i++) {
              xAxis = (AxisX) xAxes.elementAt(i);
              if (xAxis.getBounds().contains(x, y)) {
                Axis.editAxis.show(xAxis);
                break;
              }
            }
          } else if (yAxisRect.contains(x, y)) {
            for (int i = 0; i < yAxes.size(); i++) {
              yAxis = (AxisY) yAxes.elementAt(i);
              if (yAxis.getBounds().contains(x, y)) {
                Axis.editAxis.show(yAxis);
                break;
              }
            }
          } else {
            break;
          }

          // this redraws the GraphAxisData for XtoY or gets the appropriate subImage for XYtoZ
          // this is better than calling draw which redraws for both of them
          this.zoomUpdate();
          repaint();
          break;
        default:
          super.processMouseEvent(e);
          break;
      }
    }
  }

  /** Inner static class to handle XY to Z plotting */
  public static class XYtoZ extends GraphAxisData {

    protected AxisX xAxis;
    protected AxisY yAxis;

    public XYtoZ() {
      super();
      graphDataset = new GraphDataset.XYtoZ(this);
      graphDataset.addPropertyChangeListener(this);
      this.add(graphDataset);
    }

    /**
     * This constructor should be used with caution The only reason this constructor exists is to
     * allow the graphDataset to be subclassed by ... other classes to include more functionality
     * Make it PRIVATE since both objects graphDataset & GraphAxisData, must be created together...
     * this is because the graphDataset must have a reference to this graphAxisData for this to work
     * THE PROGRAMMER SHOULD MAKE SURE OF THIS
     */
    public XYtoZ(GraphDataset.XYtoZ graphDataset) {
      super(graphDataset);
    }

    public XYtoZ(GraphDataset.XYtoZ graphDataset, Graph.XYtoZ graph) {
      super(graphDataset, graph);
    }

    /** preferred way to attach a data set to graphDataset */
    protected void attachDataset(DatasetXYtoZ d) {
      if (!((GraphDataset.XYtoZ) graphDataset).isAttached(d)) {
        ((GraphDataset.XYtoZ) graphDataset).attachDataset(d);
        // attachAxisX (d.xAxis);
        xAxis = d.xAxis;
        // attachAxisY (d.yAxis);
        yAxis = d.yAxis;

        d.addPropertyChangeListener(this);
      }
    }

    /** preferred way to detach a data set to graphDataset */
    protected void detachDataset(DatasetXYtoZ d) {
      if (((GraphDataset.XYtoZ) graphDataset).isAttached(d)) {
        // detach everything
        d.removePropertyChangeListener(this);
        // this sets the dataset to null
        graphDataset.detachAllDatasets();
        // set the axes to null also
        xAxis = null;
        yAxis = null;
      }
    }

    /** detaches all the axes and datasets on this graphAxisData */
    protected void detachAllDatasets() {
      DatasetXYtoZ d = ((GraphDataset.XYtoZ) graphDataset).dataset;
      d.removePropertyChangeListener(this);
      // this sets the dataset to null
      graphDataset.detachAllDatasets();
      // set the axes to null also
      xAxis = null;
      yAxis = null;
    }

    // get numYAxes ...
    public int getNumYAxes() {
      return ((yAxis == null) ? 0 : 1);
    }

    // get numXAxes ...
    public int getNumXAxes() {
      return ((xAxis == null) ? 0 : 1);
    }

    protected void drawAxisY() {
      yAxis.draw(yAxisRect);
    }

    protected void drawAxisX() {
      xAxis.draw(xAxisRect);
    }

    // make it private since it needs some information from other methods here to work
    protected void paintAxes(Graphics2D g) {
      yAxis.paint(g);
      xAxis.paint(g);
    }

    /** performs zooming operations by calling setting the axis range and calling draw */
    protected void zoom(Rectangle zoomRect) {
      try {
        // reset the axis ranges based on the zoom rectangle
        double max = 0.0;
        double min = 0.0;

        max = yAxis.getDouble(zoomRect.y);
        min = yAxis.getDouble(zoomRect.y + zoomRect.height);
        yAxis.setAxisMax(max);
        yAxis.setAxisMin(min);

        max = xAxis.getDouble(zoomRect.x + zoomRect.width);
        min = xAxis.getDouble(zoomRect.x);
        xAxis.setAxisMax(max);
        xAxis.setAxisMin(min);

      } catch (Exception exc) {
      } finally {
        // draw everything again
        this.zoomUpdate();
        // schedule component for a repainting
        repaint();
      }
    }

    /**
     * given the x and y c0-ordinate points, it calculates the actual x, y, and value and calls
     * setPositionValue on Graph with these values
     *
     * <p>if these values are -1, then we know that the mouse exited and we report no values in the
     * position component
     *
     * <p>if we catch an error, just cause a zoomUpdate and repaint, this redraws the axes, gets the
     * graphImage and repaints
     */
    protected void setPositionValue(int x, int y) {
      try {
        double[] oldPosition = new double[] {mousePosition[0], mousePosition[1], mousePosition[2]};

        if (x == -1 || y == -1) {
          mousePosition[0] = mousePosition[1] = mousePosition[2] = -1;
        } else {
          mousePosition[0] = Math.floor(xAxis.getDouble(x));
          mousePosition[1] = Math.floor(yAxis.getDouble(y));

          // have to figure out how to get this value
          mousePosition[2] =
              ((GraphDataset.XYtoZ) graphDataset).getValue(mousePosition[0], mousePosition[1]);
        }

        firePropertyChange("position", oldPosition, mousePosition);

        // call setPositionValue on Graph with these values
        // graph.setPositionValue (xValue, yValue, value);
      } catch (Exception exc) {
        // draw everything again
        this.zoomUpdate();
        // schedule component for a repainting
        repaint();
      }
    }

    /** causes the axes to reset their axes ranges */
    public void resetAxesRange() {
      // adjust the axis Limits
      xAxis.setAxisMinLimit();
      xAxis.setAxisMaxLimit();
      // set the range of the axis
      xAxis.setAxisRange();

      // adjust the axis Limits
      yAxis.setAxisMinLimit();
      yAxis.setAxisMaxLimit();
      // set the range of the axis
      yAxis.setAxisRange();

      // call zoomUpdate to get the appropriate graphImage first
      this.zoomUpdate();
      repaint();
      // repaint the graph Key also ... so the new scale will show (for XYtoZ)
      // graph.graphKey.draw ();
      // graph.graphKey.repaint ();

    }

    /** Process component events to cause a redraw when the component size changes */
    public void processMouseEvent(MouseEvent e) {
      switch (e.getID()) {
        case MouseEvent.MOUSE_CLICKED:
          // set axis range if a double click
          if (e.getClickCount() < 2) break;
          if (xAxis.getBounds().contains(e.getX(), e.getY())) {
            Axis.editAxis.show(xAxis);
          } else if (yAxis.getBounds().contains(e.getX(), e.getY())) {
            Axis.editAxis.show(yAxis);
          } else break;

          // this redraws the GraphAxisData for XtoY or gets the appropriate subImage for XYtoZ
          // this is better than calling draw which redraws for both of them
          this.zoomUpdate();
          repaint();
          break;
        default:
          super.processMouseEvent(e);
          break;
      }
    }
  }

  // implements the layout for this GraphAxisData
  public class LayoutMgr implements LayoutManager2, Serializable {

    /** The method that actually performs the layout */
    public void layoutContainer(Container target) {
      if (graphDataset == null) return;

      setComponentBounds(target);

      graphDataset.setBounds(graphDatasetRect);
    }

    protected void setComponentBounds(Container target) {
      int numXAxes = getNumXAxes();
      int numYAxes = getNumYAxes();

      Insets in = target.getInsets();
      int w = target.getWidth();
      int h = target.getHeight();

      // get the heights of each X axis that will be drawn
      // if less than minDim, do not show the axes
      if (h >= preferredDim.height) {
        if ((maxSingleAxisWidth * numXAxes) > maxAxisWidth) {
          singleAxisXWidth = maxAxisWidth / numXAxes;
        } else {
          singleAxisXWidth = maxSingleAxisWidth;
        }
      } else if (h >= minDim.height) {
        int axisWidth = (int) ((double) maxAxisWidth / (double) preferredDim.height * (double) h);
        if ((maxSingleAxisWidth * numXAxes) > axisWidth) {
          singleAxisXWidth = axisWidth / numXAxes;
        } else {
          singleAxisXWidth = maxSingleAxisWidth;
        }
      } else {
        singleAxisXWidth = 0;
      }

      // get the widths of each Y axis that will be drawn
      if (w >= preferredDim.width) {
        if ((maxSingleAxisWidth * numYAxes) > maxAxisWidth) {
          singleAxisYWidth = maxAxisWidth / numYAxes;
        } else {
          singleAxisYWidth = maxSingleAxisWidth;
        }
      } else if (w >= minDim.width) {
        int axisWidth = (int) ((double) maxAxisWidth / (double) preferredDim.width * (double) w);
        if ((maxSingleAxisWidth * numYAxes) > axisWidth) {
          singleAxisYWidth = axisWidth / numYAxes;
        } else {
          singleAxisYWidth = maxSingleAxisWidth;
        }
      } else {
        singleAxisYWidth = 0;
      }

      // set the bounds of the graphDataset and the Rectangles where axes will be drawn
      int xHeight = singleAxisXWidth * numXAxes;
      int yWidth = singleAxisYWidth * numYAxes;

      yAxisRect.setBounds(0 + in.left, 0 + in.top, yWidth - in.left, h - xHeight - in.top);
      xAxisRect.setBounds(yWidth, h - xHeight, w - yWidth - in.right, xHeight - in.bottom);
      graphDatasetRect.setBounds(yWidth, 0 + in.top, w - yWidth - in.right, h - xHeight - in.top);
    }

    public Dimension preferredLayoutSize(Container parent) {
      return GraphAxisData.preferredDim;
    }

    public Dimension maximumLayoutSize(Container target) {
      return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public Dimension minimumLayoutSize(Container target) {
      Insets in = target.getInsets();
      Dimension dim = target.getParent().getSize();
      dim.width = dim.width - in.left - in.right;
      dim.height = dim.height - in.top - in.bottom;
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
