package spec.lib.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * This manager manages changing the status messages for a label When the mouse enters a component
 * that registers it as a listener, it checks if that component has a client property for the status
 * message ... "StatusMessage" if it does, it sets the status labels text to that message It however
 * has a private String variable that it sets back to when the mouse leaves that component This
 * private String can be set by calling a method ... and setting it overrides the mouse listeneing
 */
public class StatusBarManager implements MouseListener, MouseMotionListener {

  private String statusMsg = "";
  private JLabel statusLabel;
  private boolean statusToBeChanged = false;

  /** create a Status Manager, giving it a JLabel whose test we manage */
  public StatusBarManager(JLabel statusLabel) {
    this.statusLabel = statusLabel;
    this.statusMsg = statusLabel.getText();
    if (statusMsg == null) statusMsg = "";
  }

  /**
   * When the mouse enters a JComponent, we get the client property for the "StatusMessage" and set
   * the text of the status label to that
   */
  public void mouseEntered(MouseEvent e) {
    Object o = e.getSource();
    if (o instanceof JComponent) {
      JComponent c = (JComponent) o;
      String str = (String) c.getClientProperty("StatusMessage");
      if (str != null) {
        statusLabel.setText(str);
        statusToBeChanged = true;
      }
    }
  }

  /** When the mouse leaves a JComponent, we set the statusLabel to the previous status message */
  public void mouseExited(MouseEvent e) {
    if (statusToBeChanged) {
      statusLabel.setText(statusMsg);
      statusToBeChanged = false;
    }
  }

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {}

  public void mouseDragged(MouseEvent e) {}

  public void mouseMoved(MouseEvent e) {}

  public void setStatusMsg(String statusText) {
    this.statusMsg = statusText.trim();
    statusLabel.setText(statusMsg);
  }
}
