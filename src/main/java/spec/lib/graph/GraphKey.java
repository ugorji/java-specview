package spec.lib.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import spec.lib.MathPlus;

public abstract class GraphKey extends JComponent implements PropertyChangeListener {
  /** flag to indicate if printing is being done or just on-screen rendering, when painting */
  protected boolean printFlag = false;

  private GraphKeyHeader header;
  protected Font keyFont = new Font("Helvetica", Font.PLAIN, 12);
  protected Font keyTitleFont = new Font("Helvetica", Font.BOLD, 20);
  // protected JPanel componentPanel = new JPanel ();
  protected GraphKeyComponentPanel componentPanel = new GraphKeyComponentPanel();
  protected GraphAxisData graphAxisData;
  protected GraphDataset graphDataset;

  // this is the primary dataset, which cannot be detached except no others
  // are attached, and which is used to determine the grid and stuff
  protected Dataset primaryDataset;

  private GraphKey() {}

  public GraphKey(GraphAxisData graphAxisData) {
    super();
    this.setVisible(true);
    this.graphAxisData = graphAxisData;
    this.graphDataset = this.graphAxisData.getGraphDataset();
    this.graphDataset.addPropertyChangeListener(this);

    header = new GraphKeyHeader(this);
    this.setLayout(new BorderLayout());

    this.add(header, BorderLayout.NORTH);
    this.add(componentPanel, BorderLayout.CENTER);
  }

  /** GraphDataset.SCALE_PROPERTY listened for */
  public void propertyChange(PropertyChangeEvent e) {
    Object source = e.getSource();

    if (e.getPropertyName().equals(GraphDataset.SCALE_PROPERTY)) {
      this.repaint();
    } else if (e.getPropertyName().equals(GraphDataset.PRIMARY_DATASET_PROPERTY)) {
      primaryDataset = (Dataset) e.getNewValue();
    }
  }

  /** detach all datasets here */
  protected abstract void detachAllDatasets();

  /** default implementation of draw ... does nothing for now */
  public void draw() {}

  /**
   * set the print flag to true or false, so components will know to paint in the preferred way for
   * printing or normal on-screen rendering
   */
  protected void setPrintFlag(boolean b) {
    printFlag = b;
  }

  public Font getKeyFont() {
    return keyFont;
  }

  /** Inner static class to handle X to Y datasets */
  public static final class XtoY extends GraphKey {
    /** Vector containing a list of attached Datasets */
    protected java.util.List dataset = new ArrayList();

    // Map holds datasets to DatasetXtoYGraphKeyComponent mappings
    protected Map datasetToComponentMap = new HashMap();

    public XtoY(GraphAxisData.XtoY graphAxisData) {
      super(graphAxisData);
      // componentPanel.setLayout (new GridLayout (0, 1, 1, 1) );
      componentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
    }

    protected void attachDataset(DatasetXtoY d) {
      if (d == null) return;
      if (!(dataset.contains(d))) {
        d.addPropertyChangeListener(this);
        dataset.add(d);
        DatasetXtoYGraphKeyComponent comp = new DatasetXtoYGraphKeyComponent(this, d);
        componentPanel.add(comp);
        datasetToComponentMap.put(d, comp);
        componentPanel.validate();
        // if (primaryDataset == null)
        //   primaryDataset = (DatasetXtoY) dataset.get (0);

      }
    }

    protected void detachDataset(DatasetXtoY d) {
      if (d == null) return;
      if (dataset.contains(d)) {
        d.removePropertyChangeListener(this);
        dataset.remove(d);
        componentPanel.remove((JComponent) datasetToComponentMap.get(d));
        datasetToComponentMap.remove(d);
        componentPanel.validate();

        // if (primaryDataset == d) {
        //   if (dataset.isEmpty () )
        //     primaryDataset = null;
        //   else
        //     primaryDataset = (DatasetXtoY) dataset.get (0);
        // }
      }
    }

    protected void detachAllDatasets() {
      if (dataset.isEmpty()) return;
      DatasetXtoY d;
      for (int i = 0; i < dataset.size(); i++) {
        detachDataset((DatasetXtoY) dataset.get(i));
      }
    }

    /** Dataset.SCALE_PROPERTY, DatasetXtoY.LINE_ATTRIBUTE_PROPERTY listened for */
    public void propertyChange(PropertyChangeEvent e) {
      Object source = e.getSource();

      // redraw if the scale of the dataset is changed, so we can reflect the new scale
      if (e.getPropertyName().equals(Dataset.SCALE_PROPERTY)) {
        componentPanel.validate();
        DatasetXtoY d = (DatasetXtoY) source;
        ((JComponent) datasetToComponentMap.get(d)).repaint();
      } else if (e.getPropertyName().equals(DatasetXtoY.LINE_ATTRIBUTE_PROPERTY)) {
        componentPanel.validate();
        DatasetXtoY d = (DatasetXtoY) source;
        ((JComponent) datasetToComponentMap.get(d)).repaint();
      } else super.propertyChange(e);
    }
  }

  /** Inner static class to handle XY to Z datasets */
  public static final class XYtoZ extends GraphKey {
    /** Data Set which is being drawn on this graph */
    protected DatasetXYtoZ dataset;

    DatasetXYtoZGraphKeyComponent datasetComponent;

    public XYtoZ(GraphAxisData.XYtoZ graphAxisData) {
      super(graphAxisData);
      componentPanel.setLayout(new BorderLayout());
    }

    /**
     * Attach a dataset which will be the only dataset in here let this GraphKey listen to the
     * dataset for changes in its color map
     */
    protected void attachDataset(DatasetXYtoZ d) {
      if (dataset != null) {
        System.out.println("a dataset is already attached");
        return;
      }

      dataset = d;
      dataset.addPropertyChangeListener(this);
      datasetComponent = new DatasetXYtoZGraphKeyComponent(this, d);
      componentPanel.add(datasetComponent, BorderLayout.CENTER);
      // componentPanel.validate ();
    }

    protected void detachDataset(DatasetXYtoZ d) {
      if (dataset == d) detachDataset();
    }

    protected void detachDataset() {
      componentPanel.remove(datasetComponent);
      componentPanel.validate();

      dataset.removePropertyChangeListener(this);
      dataset = null;
      datasetComponent = null;
    }

    protected void detachAllDatasets() {
      detachDataset();
    }

    /** Dataset.SCALE_PROPERTY, DatasetXYtoZ.COLOR_MAP_PROPERTY listened for */
    public void propertyChange(PropertyChangeEvent e) {
      Object source = e.getSource();

      // redraw if the scale of the dataset is changed, so we can reflect the new scale
      if (e.getPropertyName().equals(Dataset.SCALE_PROPERTY)) {
        componentPanel.validate();
        if (source == dataset) datasetComponent.repaint();
      } else if (e.getPropertyName().equals(DatasetXYtoZ.COLOR_MAP_PROPERTY)) {
        if (source == dataset) datasetComponent.repaint();
      } else super.propertyChange(e);
    }
  }

  /** contains the components that represent each attached dataset ... It is not opaque */
  protected static final class GraphKeyComponentPanel extends JComponent {
    protected GraphKeyComponentPanel() {
      super();
      if (isOpaque()) setOpaque(false);
    }
  }

  /** Component representing the header for the graph */
  protected static final class GraphKeyHeader extends JComponent {
    private final GraphKey graphKey;
    private Dimension preferredDim = new Dimension(150, 150);
    private Font keyTitleFont;
    private Font keyFont;
    private String headerString = "KEY";
    // a helper point object
    private Point2D.Float pt = new Point2D.Float(5.0f, 5.0f);

    protected GraphKeyHeader(GraphKey graphKey) {
      super();
      this.setVisible(true);

      this.graphKey = graphKey;
      keyFont = graphKey.keyFont;
      keyTitleFont = graphKey.keyTitleFont;
      measure();
      repaint();
    }

    public Dimension getPreferredSize() {
      return preferredDim;
    }

    /** calculates the preferred size */
    private void measure() {
      FontMetrics fm = this.getToolkit().getFontMetrics(keyTitleFont);
      int h = fm.getHeight();
      int w = fm.stringWidth(headerString);

      preferredDim.width = w + 25;
      preferredDim.height = h + 12;
    }

    protected void paintComponent(Graphics gr) {
      Graphics2D g = (Graphics2D) gr;

      pt.x = pt.y = 5.0f;

      // Write headerString at the top of the Key Component and underline it
      AttributedString title = new AttributedString(headerString);

      title.addAttribute(TextAttribute.FONT, keyTitleFont);
      title.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);

      TextLayout layout1 = new TextLayout(title.getIterator(), g.getFontRenderContext());
      pt.y += layout1.getAscent();
      layout1.draw(g, pt.x, pt.y);
      pt.y += layout1.getDescent() + layout1.getLeading();
    }
  }

  protected static final class DatasetXtoYGraphKeyComponent extends JComponent {
    private final GraphKey graphKey;
    private Dimension preferredDim = new Dimension(100, 75);
    private final DatasetXtoY dataset;
    private Font keyFont;
    // a helper point object
    private Point2D.Float pt = new Point2D.Float(5.0f, 5.0f);

    protected DatasetXtoYGraphKeyComponent(GraphKey graphKey, DatasetXtoY dataset) {
      super();
      this.setVisible(true);
      // this.setBorder (BorderFactory.createEtchedBorder () );

      this.graphKey = graphKey;
      this.dataset = dataset;
      keyFont = graphKey.keyFont;
      measure();
      repaint();
    }

    public Dimension getPreferredSize() {
      measure();
      return preferredDim;
    }

    /** calculates the preferred size */
    private void measure() {
      Container parent = getParent();

      FontMetrics fm = this.getToolkit().getFontMetrics(keyFont);
      int h = fm.getHeight();
      int w = fm.stringWidth(dataset.getScaleAsString());

      if (parent != null) {
        Insets in = parent.getInsets();
        preferredDim.width = parent.getWidth() - in.left - in.right;
        if (preferredDim.width < (w + 25)) preferredDim.width = w + 25;
        preferredDim.height = h * 2 + 12;
      } else {
        preferredDim.width = w + 25;
        preferredDim.height = h * 2 + 12;
      }
    }

    protected void paintComponent(Graphics gr) {
      Graphics2D g = (Graphics2D) gr;

      TextLayout layout1;
      DatasetXtoY d;

      pt.x = pt.y = 5.0f;

      // AttributedString to hold strings to be drawn
      AttributedString title;

      d = dataset;

      Stroke oldStroke = g.getStroke();
      Color oldColor = g.getColor();

      // render a color/line style representation of the data set
      g.setStroke(d.getLineAttributes().getLineStyle());
      g.setColor(d.getLineAttributes().getLineColor());
      Line2D.Float line = new Line2D.Float(pt.x, pt.y, this.getWidth() - pt.x, pt.y);
      g.draw(line);

      g.setStroke(oldStroke);
      g.setColor(oldColor);

      // render the scale of the data set
      title = new AttributedString("(" + d.getScaleAsString() + ")");
      title.addAttribute(TextAttribute.FONT, keyFont);
      layout1 = new TextLayout(title.getIterator(), g.getFontRenderContext());
      pt.y += layout1.getAscent();
      layout1.draw(g, pt.x, pt.y);
      pt.y += layout1.getDescent() + layout1.getLeading();

      // render the name of the data set
      title = new AttributedString(d.name);
      title.addAttribute(TextAttribute.FONT, keyFont);
      title.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
      layout1 = new TextLayout(title.getIterator(), g.getFontRenderContext());
      pt.y += layout1.getAscent();
      layout1.draw(g, pt.x, pt.y);
      pt.y += layout1.getDescent() + layout1.getLeading();
    }
  }

  protected static final class DatasetXYtoZGraphKeyComponent extends JComponent {
    private final GraphKey graphKey;
    private Dimension preferredDim = new Dimension(150, 500);
    private final DatasetXYtoZ dataset;
    private Font keyFont;
    // a helper point object
    private Point2D.Float pt = new Point2D.Float(5.0f, 5.0f);

    protected DatasetXYtoZGraphKeyComponent(GraphKey graphKey, DatasetXYtoZ dataset) {
      super();
      this.setVisible(true);
      // this.setBorder (BorderFactory.createEtchedBorder () );

      this.graphKey = graphKey;
      this.dataset = dataset;
      keyFont = graphKey.keyFont;
      measure();
      repaint();
    }

    public Dimension getPreferredSize() {
      return preferredDim;
    }

    /**
     * calculates the preferred size ... not fully implemented not yet necessary since the graphkey
     * here uses a border layout
     */
    private void measure() {}

    protected void paintComponent(Graphics gr) {
      Graphics2D g = (Graphics2D) gr;

      TextLayout layout1;

      pt.x = pt.y = 5.0f;

      // AttributedString to hold strings to be drawn
      AttributedString title;

      // render the scale of the data set
      title = new AttributedString("(" + dataset.getScaleAsString() + ")");
      title.addAttribute(TextAttribute.FONT, keyFont);
      layout1 = new TextLayout(title.getIterator(), g.getFontRenderContext());
      pt.y += layout1.getAscent();
      layout1.draw(g, pt.x, pt.y);
      pt.y += layout1.getDescent() + layout1.getLeading();

      // render the name of the data set
      title = new AttributedString(dataset.name);
      title.addAttribute(TextAttribute.FONT, keyFont);
      title.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
      layout1 = new TextLayout(title.getIterator(), g.getFontRenderContext());
      pt.y += layout1.getAscent();
      layout1.draw(g, pt.x, pt.y);
      pt.y += layout1.getDescent() + layout1.getLeading();

      // based on the renderMethod of the data set, draw a representation
      switch (dataset.renderMethod) {
        case DatasetXYtoZ.COLORPLOT:
          paintColorPlot(g, (int) pt.y);
          break;
        default:
          paintColorPlot(g, (int) pt.y);
          break;
      }
    }

    /** Handles painting the color plot on the Key */
    private void paintColorPlot(Graphics2D g, int hOffset) {

      hOffset += 10;

      int ht = this.getHeight() - hOffset - 10;
      double w = (double) this.getWidth() / 4.0;

      int suggestedHt = 300;
      int numTics = 10;

      Point2D.Double pt = new Point2D.Double();

      // set the ht and number of tics
      if (ht > suggestedHt) {
        ht = suggestedHt;
        numTics = 11;
      } else {
        // ht = ht;
        numTics = (int) (ht / 25) + 1;
        if (numTics > 11) numTics = 11;
        else if (numTics > 6) numTics = 6;
        else if (numTics == 6 || numTics == 5) ;
        else numTics = 3;
      }

      double increment = ht / 101.0;

      Rectangle2D.Double aRect = new Rectangle2D.Double();

      pt.x = 5;
      pt.y = hOffset;

      int startHt = (int) pt.y;

      for (byte b = 100; b >= 0; b--) {
        aRect.setRect(pt.x, pt.y, w, increment);
        g.setColor((Color) dataset.colorTable.get(new Byte(b)));
        g.fill(aRect);

        pt.y += increment;
      }

      int endHt = (int) (pt.y - increment);

      pt.x = pt.x + w + 2;

      Color oldColor = g.getColor();

      g.setColor(Color.black);
      g.drawLine((int) pt.x, startHt, (int) pt.x, endHt);

      pt.y = (double) startHt;

      double ticIncr = (double) (endHt - startHt) / (numTics - 1);

      double datasetRange = dataset.upperLimit - dataset.lowerLimit;
      double rangeIncr = datasetRange / (numTics - 1);
      double value;
      String valueString;
      int exponent;

      for (int i = 0; i < numTics; i++) {
        g.drawLine((int) pt.x, (int) pt.y, (int) pt.x + 3, (int) pt.y);
        value = dataset.upperLimit - (i * rangeIncr);

        // make valueString easily comprehensible
        if (value <= 0.0) exponent = 0;
        else exponent = (int) (Math.floor(MathPlus.log10(value)));

        if ((exponent >= -2) && (exponent <= 3)) exponent = 0;

        value = value * Math.pow(10, -1.0 * exponent);
        valueString = String.valueOf(value);

        if (valueString.length() > 5) valueString = valueString.substring(0, 5);

        if (exponent != 0) valueString = valueString + "E" + exponent;

        /* ******************************************
        if (valueString.length() > 7)
        valueString = valueString.substring (0, 7);
        ****************************************** */

        g.drawString(valueString, (int) pt.x + 6, (int) pt.y + 6);

        pt.y += ticIncr;
      }

      g.setColor(oldColor);
    }
  }
}
