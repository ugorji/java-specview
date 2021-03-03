package spec.lib.graph;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import spec.lib.MathPlus;

/** component that shows the position of the mouse on the graphDataset */
public class GraphPosition extends JComponent implements PropertyChangeListener {

  public Font positionFont = new Font("Helvetica", Font.PLAIN, 10);
  // potential use: to tell exactly how much space it drew into
  public Dimension usedDimension = new Dimension();
  /** flag to indicate if printing is being done or just on-screen rendering, when painting */
  protected boolean printFlag = false;

  private GraphAxisData graphAxisData;

  /**
   * -1 is the default value. When mouse leaves GrahDataset, values are set to -1 again. This causes
   * an empty string to be drawn for the values
   */
  private double[] position = new double[] {-1, -1, -1};

  /**
   * The strings that are actually painted ... initialize them to non-white space values so
   * drawString can draw them when they are unknown
   */
  private String[] positionString = new String[] {"-", "-", "-"};

  public GraphPosition() {
    this(null);
  }

  public GraphPosition(GraphAxisData graphAxisData) {
    super();
    this.setVisible(true);
    setGraphAxisData(graphAxisData);
  }

  public void setGraphAxisData(GraphAxisData graphAxisData) {
    this.graphAxisData = graphAxisData;
    graphAxisData.addPropertyChangeListener(this);
  }

  public GraphAxisData getGraphAxisData() {
    return graphAxisData;
  }

  public void propertyChange(PropertyChangeEvent e) {
    double[] newPosition;

    if (e.getPropertyName().equals(GraphAxisData.POSITION_PROPERTY)) {
      newPosition = (double[]) e.getNewValue();
      position[0] = newPosition[0];
      position[1] = newPosition[1];
      position[2] = newPosition[2];

      updateStrings();
      repaint();
    }
  }

  private void updateStrings() {
    // since -1 are the default values, if all values are -1,
    // set strings to empty strings
    if (position[0] == -1 && position[1] == -1 && position[2] == -1) {
      positionString[0] = positionString[1] = positionString[2] = "-";
      return;
    }

    positionString[0] = getString(position[0]);
    positionString[1] = getString(position[1]);

    if (position[2] == GraphAxisData.POSITION_VALUE_UNKNOWN) positionString[2] = "UNKNOWN";
    else positionString[2] = getString(position[2]);
  }

  /** given a value, returns a easily comprehensible String */
  private String getString(double value) {
    String valueString;
    int exponent;

    // make valueString easily comprehensible
    if (value <= 0.0) exponent = 0;
    else exponent = (int) (Math.floor(MathPlus.log10(value)));

    if ((exponent >= -2) && (exponent <= 3)) exponent = 0;

    value = value * Math.pow(10, -1.0 * exponent);
    valueString = String.valueOf(value);

    if (valueString.length() > 5) valueString = valueString.substring(0, 5);

    if (exponent != 0) valueString = valueString + "E" + exponent;

    return valueString;
  }

  /**
   * should do some drawing operation ... does nothing now Leave though ... it is called by others
   * and might do something in the future
   */
  public void draw() {}

  /** Paints the position component */
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Font origFont = g.getFont();
    g.setFont(positionFont);

    Point pt = new Point(10, 10);
    g.drawString("MOUSE POSITION", pt.x, pt.y);
    pt.y += 4;
    g.drawLine(pt.x, pt.y, pt.x + this.getWidth() - 15, pt.y);
    pt.y += 12;
    g.drawString("X", pt.x, pt.y);
    pt.y += 12;
    g.drawString(positionString[0], pt.x, pt.y);
    pt.y += 12;
    g.drawString("Y", pt.x, pt.y);
    pt.y += 12;
    g.drawString(positionString[1], pt.x, pt.y);
    pt.y += 12;
    g.drawString("Value", pt.x, pt.y);
    pt.y += 12;
    g.drawString(positionString[2], pt.x, pt.y);

    g.setFont(origFont);
  }

  /**
   * set the print flag to true or false, so components will know to paint in the preferred way for
   * printing or normal on-screen rendering
   */
  protected void setPrintFlag(boolean b) {
    printFlag = b;
  }
}
