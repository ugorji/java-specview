package spec.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import spec.lib.Misc;

/**
 * inner static class for labels for infoPanel ... should only be used by SpecView ... just allows
 * us instantiate a label with a color and font specified
 */
public final class AboutSpecViewWindow extends JWindow implements ActionListener {
  /** Default status text */
  public static final String DEFAULT_STATUS_TEXT;

  public static final String VERSION = "SpecView Version 1.0 Alpha";

  private JPanel infoPanel;
  // the label that will be updated as the init status is changed
  private InfoLabel statusLabel;

  private JButton okButton = new JButton("OK");

  static {
    StringBuffer buf = new StringBuffer();
    buf.append("Running on JAVA ");
    buf.append(System.getProperty("java.specification.version"));
    buf.append(", ");
    buf.append(System.getProperty("java.compiler"));
    buf.append("; ");
    buf.append(System.getProperty("os.name"));
    buf.append(" ");
    buf.append(System.getProperty("os.version"));
    buf.append(", ");
    buf.append(System.getProperty("os.arch"));

    DEFAULT_STATUS_TEXT = buf.toString();
  }

  public AboutSpecViewWindow() {
    super();

    Color fg = Color.white;
    Color fg2 = new Color(0.88f, 0.88f, 0.88f);
    Font font2 = new Font("Helvetica", Font.PLAIN, 12);

    okButton.addActionListener(this);

    infoPanel = new JPanel();
    // infoPanel.setSize (350, 300);
    JPanel centerPanel = new JPanel();
    JPanel southPanel = new JPanel();
    JPanel northPanel = new JPanel();
    JPanel okPanel = new JPanel();

    Color bgColor = new Color(0.2f, 0.2f, 0.7f);
    infoPanel.setBackground(bgColor);
    northPanel.setBackground(bgColor);
    centerPanel.setBackground(bgColor);
    southPanel.setBackground(bgColor);
    okPanel.setBackground(bgColor);

    // build the  screen
    InfoLabel version =
        new InfoLabel(VERSION, new Font("Helvetica", Font.BOLD + Font.ITALIC, 20), fg);
    InfoLabel description =
        new InfoLabel(
            "[for analysis of spectral data]",
            new Font("Helvetica", Font.BOLD + Font.ITALIC, 12),
            fg);

    northPanel.setLayout(new BorderLayout());
    northPanel.add(version, BorderLayout.CENTER);
    northPanel.add(description, BorderLayout.SOUTH);

    northPanel.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "NSCL-MSU",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            font2,
            fg2));

    centerPanel.setLayout(new GridLayout(5, 1));
    centerPanel.add(new InfoLabel("Copyright 1998"));
    centerPanel.add(new InfoLabel("National SuperConducting Cyclotron Laboratory"));
    centerPanel.add(new InfoLabel("Michigan State University"));
    centerPanel.add(new InfoLabel("Author: Ugorji Nwoke"));
    centerPanel.add(new InfoLabel("Supervisor: Ron Fox"));

    centerPanel.setBorder(BorderFactory.createEtchedBorder());

    statusLabel = new InfoLabel(DEFAULT_STATUS_TEXT, font2, fg2);
    okPanel.add(okButton);
    southPanel.setLayout(new BorderLayout());
    southPanel.add(statusLabel, BorderLayout.NORTH);
    southPanel.add(okPanel, BorderLayout.CENTER);

    infoPanel.setLayout(new BorderLayout());

    infoPanel.add(northPanel, BorderLayout.NORTH);
    infoPanel.add(centerPanel, BorderLayout.CENTER);
    infoPanel.add(southPanel, BorderLayout.SOUTH);

    Border outerBorder =
        BorderFactory.createCompoundBorder(
            BorderFactory.createBevelBorder(
                BevelBorder.RAISED, Color.red.brighter(), Color.red.darker()),
            BorderFactory.createEtchedBorder());

    infoPanel.setBorder(
        BorderFactory.createCompoundBorder(
            outerBorder, BorderFactory.createLineBorder(Color.cyan, 5)));

    infoPanel.repaint();

    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    contentPane.add(infoPanel, BorderLayout.CENTER);

    pack();
    repaint();

    Misc.centerOnScreen(this);
  }

  /** sets the text that should be the status of the initialization */
  public void setStatusText(String str) {
    statusLabel.setText(str.trim());
  }

  /** sets the default text for the status label */
  public void setDefaultStatusText() {
    statusLabel.setText(DEFAULT_STATUS_TEXT);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okButton) {
      this.setVisible(false);
    }
  }

  /**
   * inner static class for labels for infoPanel ... should only be used by SpecView ... just allows
   * us instantiate a label with a color and font specified
   */
  protected static final class InfoLabel extends JLabel {

    protected InfoLabel(String label, Font font, Color fg) {
      super(label, JLabel.CENTER);
      setFont(font);
      setForeground(fg);
    }

    protected InfoLabel(String label) {
      this(label, new Font("Helvetica", Font.BOLD, 14), Color.white);
    }
  }
}
