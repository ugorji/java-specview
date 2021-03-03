package spec.lib.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * FontChooser.java A font chooser that allows users to pick a font by name, size, style, and color.
 * The color selection will be provided by a JColorChooser pane. This builds an AttributeSet
 * suitable for use with JTextPane.
 */
public class FontChooser extends JPanel implements ActionListener {

  private final JColorChooser colorChooser;
  private final JComboBox fontName;
  private final JCheckBox fontBold, fontItalic;
  private final JTextField fontSize;
  private final JLabel previewLabel;
  private final SimpleAttributeSet attributes;
  private final JPanel previewPanel = new JPanel();
  private Font newFont;
  private Color newColor;

  // instance initializer
  {
    attributes = new SimpleAttributeSet();

    JPanel fontPanel = new JPanel();
    fontName = new JComboBox(new String[] {"TimesRoman", "Helvetica", "Courier"});
    fontName.setSelectedIndex(1);
    fontName.addActionListener(this);
    fontSize = new JTextField("12", 4);
    fontSize.setHorizontalAlignment(SwingConstants.RIGHT);
    fontSize.addActionListener(this);
    fontBold = new JCheckBox("Bold");
    fontBold.setSelected(true);
    fontBold.addActionListener(this);
    fontItalic = new JCheckBox("Italic");
    fontItalic.addActionListener(this);

    fontPanel.add(fontName);
    fontPanel.add(new JLabel(" Size: "));
    fontPanel.add(fontSize);
    fontPanel.add(fontBold);
    fontPanel.add(fontItalic);

    this.add(fontPanel, BorderLayout.NORTH);

    // Set up the color chooser panel and attach a change listener so that color
    // updates get reflected in our preview label.
    colorChooser = new JColorChooser(Color.black);
    colorChooser
        .getSelectionModel()
        .addChangeListener(
            new ChangeListener() {
              public void stateChanged(ChangeEvent e) {
                updatePreviewColor();
              }
            });
    this.add(colorChooser, BorderLayout.CENTER);

    previewPanel.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Font and Color preview",
            TitledBorder.LEFT,
            TitledBorder.TOP));

    previewLabel = new JLabel("Sample Font / Color");
    previewLabel.setForeground(colorChooser.getColor());
    previewPanel.add(previewLabel);

    // Give the preview label room to grow.
    previewPanel.setMinimumSize(new Dimension(300, 75));
    previewPanel.setPreferredSize(new Dimension(300, 75));

    this.add(previewPanel, BorderLayout.SOUTH);
  }

  public FontChooser() {
    super(new BorderLayout());
    // this.setLayout (new BorderLayout () );
  }

  /**
   * 1) Handles changes in the font ... figure that out and make a new font for the preview label
   */
  public void actionPerformed(ActionEvent ae) {
    Object source = ae.getSource();

    // Check the name of the font
    if (!StyleConstants.getFontFamily(attributes).equals(fontName.getSelectedItem())) {
      StyleConstants.setFontFamily(attributes, (String) fontName.getSelectedItem());
    }
    // Check the font size (no error checking yet)
    if (StyleConstants.getFontSize(attributes) != Integer.parseInt(fontSize.getText())) {
      StyleConstants.setFontSize(attributes, Integer.parseInt(fontSize.getText()));
    }
    // Check to see if the font should be bold
    if (StyleConstants.isBold(attributes) != fontBold.isSelected()) {
      StyleConstants.setBold(attributes, fontBold.isSelected());
    }
    // Check to see if the font should be italic
    if (StyleConstants.isItalic(attributes) != fontItalic.isSelected()) {
      StyleConstants.setItalic(attributes, fontItalic.isSelected());
    }
    // and update our preview label
    updatePreviewFont();
  }

  /** Get the appropriate font from our attributes object and update the preview label */
  protected void updatePreviewFont() {
    String name = StyleConstants.getFontFamily(attributes);
    boolean bold = StyleConstants.isBold(attributes);
    boolean ital = StyleConstants.isItalic(attributes);
    int size = StyleConstants.getFontSize(attributes);

    // Bold and italic don't work properly in beta 4.
    Font f = new Font(name, (bold ? Font.BOLD : 0) + (ital ? Font.ITALIC : 0), size);
    previewLabel.setFont(f);
    // manually force the label to repaint
    previewPanel.validate();
    previewPanel.repaint();
  }

  /** Get the appropriate color from our chooser and update previewLabel */
  protected void updatePreviewColor() {
    previewLabel.setForeground(colorChooser.getColor());
    // manually force the label to repaint
    previewPanel.validate();
    previewPanel.repaint();
  }

  /** set the newFont and newColor to null */
  public void setToNull() {
    newFont = null;
    newColor = null;
  }

  public Font getNewFont() {
    return newFont;
  }

  public Color getNewColor() {
    return newColor;
  }

  public AttributeSet getAttributes() {
    return attributes;
  }

  public void save() {
    // save font & color information
    newFont = previewLabel.getFont();
    newColor = previewLabel.getForeground();
  }

  public void cancel() {
    // erase any font information and then close the window
    setToNull();
  }

  public void showDialog() {
    int value = ViewerDialog.showOkCancelDialog(this, "Font Chooser");

    if (value == ViewerDialog.OK_OPTION) save();
    else cancel();
  }
}
