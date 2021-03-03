package spec.lib.graph;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import spec.lib.ui.FontChooser;
import spec.lib.ui.ViewerDialog;

/** CLASS TO ENCAPSULATE THE RENDERING OF THE TITLE */
public final class GraphTitle extends JComponent {
  public String title;
  public Font font = new Font("Helvetica", Font.BOLD + Font.ITALIC, 16);
  public Color color = Color.black;
  /** flag to indicate if printing is being done or just on-screen rendering, when painting */
  protected boolean printFlag = false;

  private AttributedString styledTitle;
  // determines if this is underlined or not
  private boolean underline = true;

  protected static EditGraphTitle graphTitleModifier = new EditGraphTitle();

  public GraphTitle() {
    this("Graph");
  }

  public GraphTitle(String str) {
    super();
    this.setVisible(true);
    title = str;
    // allow mouse events to be trapped ...
    // so we can double-click on it to change the title string
    this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
  }

  public void draw() {
    styledTitle = new AttributedString(title);
    styledTitle.addAttribute(TextAttribute.FONT, this.font);
    styledTitle.addAttribute(TextAttribute.FOREGROUND, this.color);
    if (underline) styledTitle.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
  }

  /** sets the graph title and redraws */
  public void setGraphTitle(String str) {
    this.title = str.trim();
    draw();
    repaint();
  }

  /** gets the graph title */
  public String getGraphTitle() {
    return title;
  }

  /** sets whether this should be underlined or not */
  public void setUnderlined(boolean b) {
    if (underline == b) return;
    underline = b;
    draw();
    repaint();
  }

  public void updateUI() {
    super.updateUI();
    SwingUtilities.updateComponentTreeUI(graphTitleModifier.contentPane);
  }

  protected void paintComponent(Graphics g_orig) {
    super.paintComponent(g_orig);

    if (styledTitle == null) return;

    Graphics2D g = (Graphics2D) g_orig;
    Dimension dim = this.getSize();
    int titleLength = title.length();

    Point2D.Float pen = new Point2D.Float();
    pen.y += 3.0f;

    TextLayout layout;
    AttributedCharacterIterator styledTitleIterator = styledTitle.getIterator();

    LineBreakMeasurer measurer =
        new LineBreakMeasurer(styledTitleIterator, g.getFontRenderContext());
    float wrappingWidth = dim.width - 15;

    while (measurer.getPosition() < styledTitleIterator.getEndIndex()) {
      layout = measurer.nextLayout(wrappingWidth);
      pen.y += (layout.getAscent());
      // set pen.x so this text layout will be centered
      pen.x = (float) ((this.getWidth() - layout.getAdvance()) / 2.0);
      // float dx = layout.isLeftToRight() ? 0 : (wrappingWidth - layout.getAdvance());
      // g.drawString (layout, pen.x, pen.y);
      layout.draw(g, pen.x, pen.y);
      pen.y += layout.getDescent() + layout.getLeading();
    }
  }

  /**
   * set the print flag to true or false, so components will know to paint in the preferred way for
   * printing or normal on-screen rendering
   */
  protected void setPrintFlag(boolean b) {
    printFlag = b;
  }

  /** pop the window for editing the attributes of the graph title */
  public void processMouseEvent(MouseEvent e) {
    switch (e.getID()) {
      case MouseEvent.MOUSE_CLICKED:
        // return if user did not double-click
        if (e.getClickCount() >= 2) graphTitleModifier.show(this);
        break;
      default:
        super.processMouseEvent(e);
        break;
    }
  }

  /** Protected class to handle showing the dialog to edit the GraphTitle */
  protected static class EditGraphTitle implements ActionListener {
    // represents the contentPane for ...
    protected JPanel contentPane = new JPanel();

    protected JTextField titleField = new JTextField();
    protected JTextField fontField = new JTextField();
    protected JCheckBox underlineChkBox = new JCheckBox("Underline");

    protected JButton colorButton = new JButton("Choose color and font");
    protected ImageIcon colorImageIcon = new ImageIcon();

    protected BufferedImage colorImage;
    protected Color selectedColor = null;
    protected Font selectedFont = null;

    protected Graphics colorGraphics;

    protected FontChooser fontChooser = Graph.fontChooser;

    private GraphTitle currentGraphTitle = null;

    public EditGraphTitle() {
      super();
      fontField.setEnabled(false);

      colorImage = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
      colorImageIcon.setImage(colorImage);

      colorButton.setIcon(colorImageIcon);

      colorButton.addActionListener(this);

      // set the layout of the contentPane container
      updateLayout();
    }

    public void show(GraphTitle graphTitle) {
      if (graphTitle == null) return;

      currentGraphTitle = graphTitle;

      // get the values for this graph title
      getValues();

      int selection =
          ViewerDialog.showOkCancelDialog(contentPane, "Edit the GraphTitle attributes");

      switch (selection) {
        case ViewerDialog.OK_OPTION:
          // set the values for this graph title
          setValues();
          currentGraphTitle.draw();
          currentGraphTitle.repaint();
          break;
        case ViewerDialog.CANCEL_OPTION:
        default:
          break;
      }

      currentGraphTitle = null;
    }

    /** gets the initial values for the fields */
    private void getValues() {
      if (currentGraphTitle == null) return;

      selectedColor = currentGraphTitle.color;
      selectedFont = currentGraphTitle.font;

      titleField.setText(currentGraphTitle.title);
      underlineChkBox.setSelected(currentGraphTitle.underline);

      // update the image that identifies the color
      colorGraphics = colorImage.createGraphics();
      colorGraphics.setColor(currentGraphTitle.color);
      colorGraphics.fillRect(0, 0, colorImage.getWidth(), colorImage.getHeight());

      fontChooser.setToNull();

      String fontAsString = "Not set";

      // fontAsString  = selectedFont.getName() + " (" + selectedFont.getFontName() +
      // ") " + selectedFont.getSize ();
      fontField.setText(fontAsString);
    }

    /** should not be called by other classes ... they should call show */
    private void setValues() {
      if (currentGraphTitle == null) return;

      String str = titleField.getText().trim();
      if (str.length() > 1) currentGraphTitle.title = str;

      currentGraphTitle.underline = underlineChkBox.isSelected();

      if (selectedColor != null) currentGraphTitle.color = selectedColor;
      if (selectedFont != null) currentGraphTitle.font = selectedFont;
    }

    /** handles updating the layout for the contentPane */
    private void updateLayout() {
      contentPane.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(5, 5, 5, 5);

      // add the components
      c.gridx = c.gridy = 0;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(new JLabel("Edit Graph Title Attrributes"), c);

      c.gridwidth = 1;

      c.gridy = 1;
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      contentPane.add(new JLabel("Graph Title"), c);
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(titleField, c);

      c.gridy = 2;
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      contentPane.add(new JLabel("Selected Font"), c);
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(fontField, c);

      c.gridwidth = 1;
      c.gridy = 3;
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      contentPane.add(underlineChkBox, c);

      c.gridwidth = 1;
      c.gridy = 3;
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(colorButton, c);
    }

    public void actionPerformed(ActionEvent e) {
      if (currentGraphTitle == null) return;

      if (e.getSource() == colorButton) {
        fontChooser.showDialog();
        selectedFont = fontChooser.getNewFont();
        selectedColor = fontChooser.getNewColor();

        if (selectedColor == null) selectedColor = currentGraphTitle.color;
        if (selectedFont == null) selectedFont = currentGraphTitle.font;

        colorGraphics = colorImage.createGraphics();
        colorGraphics.setColor(selectedColor);
        colorGraphics.fillRect(0, 0, colorImage.getWidth(), colorImage.getHeight());

        String fontAsString;
        fontAsString =
            selectedFont.getName()
                + " ("
                + selectedFont.getFontName()
                + ") "
                + selectedFont.getSize();
        fontField.setText(fontAsString);
      }
    }
  }
}
