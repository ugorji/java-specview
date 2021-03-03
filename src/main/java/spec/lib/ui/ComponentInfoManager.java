package spec.lib.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * This manager manages changing the status messages for a label When the mouse enters a component
 * that registers it as a listener, it checks if that component has a client property for the status
 * message ... "component.info" if it does, it sets the status labels text to that message It
 * however has a private String variable that it sets back to when the mouse leaves that component
 * This private String can be set by calling a method ... and setting it overrides the mouse
 * listeneing
 */
public class ComponentInfoManager implements MouseListener, MouseMotionListener {

  private String infoMsg = "";
  private JLabel infoLabel;
  private boolean infoToBeChanged = false;
  private boolean componentToBeSwitched = false;

  private Component switchComponent = null;
  private Container switchContainer = null;

  /**
   * create a ComponentInfoManager, giving it a JLabel whose text we manage, and a component we
   * switch with, and a Container that contains one or the other component
   */
  public ComponentInfoManager(
      JLabel infoLabel, Component switchComponent, Container switchContainer) {
    this.infoLabel = infoLabel;
    this.infoMsg = infoLabel.getText();
    if (infoMsg == null) infoMsg = "";

    this.switchComponent = switchComponent;
    this.switchContainer = switchContainer;
  }

  /**
   * When the mouse enters a JComponent, we get the client property for the "component.info" and set
   * the text of the info label to that
   */
  public void mouseEntered(MouseEvent e) {
    Object o = e.getSource();
    if (o instanceof JComponent) {
      JComponent c = (JComponent) o;
      String str = (String) c.getClientProperty("component.info");
      if (str != null) {
        infoLabel.setText(str);
        infoToBeChanged = true;
        componentToBeSwitched = true;

        switchContainer.remove(switchComponent);
        switchContainer.add(infoLabel);
        switchContainer.repaint();
      }
    }
  }

  /** When the mouse leaves a JComponent, we set the infoLabel to the previous info message */
  public void mouseExited(MouseEvent e) {
    if (infoToBeChanged) {
      infoLabel.setText(infoMsg);
      infoToBeChanged = false;
    }
    if (componentToBeSwitched) {
      switchContainer.remove(infoLabel);
      switchContainer.add(switchComponent);
      switchContainer.repaint();
      componentToBeSwitched = false;
    }
  }

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {}

  public void mouseDragged(MouseEvent e) {}

  public void mouseMoved(MouseEvent e) {}

  public void setInfoMsg(String infoText) {
    this.infoMsg = infoText.trim();
    infoLabel.setText(infoMsg);
  }
}
