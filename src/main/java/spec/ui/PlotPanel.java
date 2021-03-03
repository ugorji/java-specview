package spec.ui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import spec.lib.ui.DesktopMgr;
import spec.spectrum.DrawableSpectrum;

/**
 * convenience class that extends JDesktopPane and re-implements the painting so a texture can be
 * painted also ... this prevents the background from matching the desktop
 */
public class PlotPanel extends JDesktopPane {
  /** the maximum number of internal frames that can be contained in a PlotPanel */
  public static final int MAX_NUM_FRAMES = 24;

  protected static TexturePaint contentPaneTexture;
  protected Icon backgroundIcon;
  protected JLabel backgroundLabel;

  private SpecUI.MDI specUI;

  /** static initializer to initialize the texture */
  static {
    // get the buffered Image for this texture
    BufferedImage bi = new BufferedImage(110, 70, BufferedImage.TYPE_INT_RGB);
    Graphics2D big = bi.createGraphics();
    // Render into the BufferedImage graphics to create the texture
    big.setColor(Color.lightGray);
    big.fillRect(0, 0, 110, 70);
    big.setColor(Color.gray);
    big.setFont(new Font("Helvetica", Font.BOLD + Font.ITALIC, 18));

    big.drawString("Spectral", 10, 20);
    big.drawString("Analysis", 20, 40);
    big.drawString("Program", 30, 60);
    // big.fillOval(0, 0, 110, 70);

    // Create a texture paint from the buffered image
    Rectangle rect = new Rectangle(0, 0, 110, 70);
    contentPaneTexture = new TexturePaint(bi, rect);
  }

  private PlotPanel() {}; // no-one can instantiate this

  /**
   * Preferred way to create a new spectrum internal frame ... this ensures that no more than the
   * number of windows allowed are created
   *
   * <p>allows you add the frame to a PlotPanel and display a spectrum in it and get the reference
   * to the spectrumManipulator managing you throw Exception if up to PlotPanel.MAX_NUM_FRAMES are
   * already displayed
   */
  public static final SpectrumInternalFrame newSpecFrame(
      SpecUI.MDI specUI,
      PlotPanel plotPanel,
      DrawableSpectrum drawSpectrum,
      SpectrumManipulator spectrumManipulator)
      throws PlotPanel.PlotPanelException {
    if (plotPanel.getAllFrames().length >= PlotPanel.MAX_NUM_FRAMES) {
      throw new PlotPanel.PlotPanelException(
          "A maximum of "
              + PlotPanel.MAX_NUM_FRAMES
              + " Spectrum Windows can be open at any time. "
              + "Please close some");
    } else {
      return new SpectrumInternalFrame(specUI, plotPanel, drawSpectrum, spectrumManipulator);
    }
  }

  /** use a desktop manager that prevents frames from being resized outside the desktop */
  public PlotPanel(SpecUI.MDI specUI) {
    super();
    this.specUI = specUI;

    // allow outline dragging mode ... SHORT-TERM soln from JAVASOFT
    this.putClientProperty("JDesktopPane.dragMode", "outline");

    this.setDesktopManager(new DesktopMgr());

    // ... cause resizing the desktop to eg. center the background label
    this.enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
    loadBackgroundImage();
  }

  /** loads the background image that will be used in this plotPanel */
  private void loadBackgroundImage() {

    Properties properties = specUI.getProperties();

    String backgroundFile = properties.getProperty("images." + "background");

    if (backgroundFile != null) {
      backgroundFile = backgroundFile.trim();

      backgroundIcon =
          new ImageIcon(
              getClass().getClassLoader().getResource("spec/resources/images/" + backgroundFile));

      backgroundLabel = new JLabel(backgroundIcon);
      backgroundLabel.setBounds(
          0, 0, backgroundIcon.getIconWidth(), backgroundIcon.getIconHeight());

      add(backgroundLabel, new Integer(Integer.MIN_VALUE));

      int aW = (this.getWidth() - backgroundLabel.getWidth()) / 2;
      int aH = (this.getHeight() - backgroundLabel.getHeight()) / 2;

      backgroundLabel.setLocation(aW, aH);
    } else {
      System.out.println("Problems getting the background image");
    }
  }

  /** sets all the internal frames in this PlotPanel to non-maximized states */
  public void unMaximizeAllFrames() {
    JInternalFrame[] allFrames = this.getAllFrames();

    for (int i = 0; i < allFrames.length; i++) {
      try {
        if (!(allFrames[i].isVisible())) allFrames[i].setVisible(true);
        if (allFrames[i].isMaximum()) allFrames[i].setMaximum(false);
      } catch (java.beans.PropertyVetoException e) {
        System.out.println("Problems unMaximizing frame" + allFrames[i].getTitle());
      }
    }
  }

  /** returns the selected frame in this component */
  public JInternalFrame getSelectedFrame() {
    JInternalFrame[] allFrames = getAllFrames();

    for (int i = 0; i < allFrames.length; i++) {
      if (allFrames[i].isSelected()) return (allFrames[i]);
    }

    return null;
  }

  /** over-ride to paint the component */
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;
    // Add the texture paint to the graphics context.
    g2.setPaint(contentPaneTexture);
    // Create and render a rectangle filled with the texture.
    g2.fillRect(0, 0, this.getWidth(), this.getHeight());
  }

  /** Process component events to ensure that the background label is always centered */
  public void processComponentEvent(ComponentEvent e) {
    switch (e.getID()) {
      case ComponentEvent.COMPONENT_RESIZED:
        // if the component size is changed, no frame should be maximized
        unMaximizeAllFrames();
        // case ComponentEvent.COMPONENT_MOVED:
        int width = this.getWidth();
        int height = this.getHeight();

        // reset the location of the background Label
        if (backgroundLabel != null) {
          int aW = (width - backgroundLabel.getWidth()) / 2;
          int aH = (height - backgroundLabel.getHeight()) / 2;

          backgroundLabel.setLocation(aW, aH);
        }
        break;
      default:
        break;
    }
    super.processComponentEvent(e);
  }

  /** A DesktopManager that keeps its frames inside the desktop */

  /**
   * Exception is thrown when the maximum number of frames are already contained in this PlotPanel
   */
  public static class PlotPanelException extends Exception {
    public PlotPanelException() {
      this("The maximum number of frames is already displayed in this component");
    }

    public PlotPanelException(String msg) {
      super(msg);
    }
  }
}
