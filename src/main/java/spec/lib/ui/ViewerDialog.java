package spec.lib.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import spec.lib.Misc;

/**
 * Defines methods to show a component in an Ok-Cancel type dialog ... or CLOSE type dialog. and
 * return a number to indicate the button that was clicked
 *
 * <p>Note that the constructor is private ... Thus, only this class can instantiate it ...
 */
public class ViewerDialog extends JDialog implements ActionListener {
  private final JButton okButton = new JButton("OK");
  private final JButton cancelButton = new JButton("CANCEL");
  private final JButton closeButton = new JButton("CLOSE");

  /** These define the type of dialog */
  public static final int CLOSE_DIALOG_OPTION = 123;

  public static final int OK_CANCEL_DIALOG_OPTION = 124;

  /** These define return values when the dialog is hidden */
  public static final int OK_OPTION = 126;

  public static final int CANCEL_OPTION = 127;
  public static final int CLOSE_OPTION = 128;

  private final JPanel controlPanel = new JPanel();

  /** This is changed everytime this ViewerDialog is hidden */
  private int viewerValue = -1;

  /** recycled dialogs that can show a component */
  private static ViewerDialog reusedDialog = new ViewerDialog();

  private static ViewerDialog backupReusedDialog = new ViewerDialog();

  /**
   * create the backup dialogs passing a frame as parent ... This is necessary to stop[ dialogs from
   * going to the back of the frame
   */
  public static final void recreateBackUpDialogs(Frame f) {
    reusedDialog.dispose();
    backupReusedDialog.dispose();
    reusedDialog = new ViewerDialog(f);
    backupReusedDialog = new ViewerDialog(f);
  }

  // instance initializer
  {
    // it is not resizable ... resizing it caused bad effects
    // setResizable (false);
    setModal(true);
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    okButton.addActionListener(this);
    cancelButton.addActionListener(this);
    closeButton.addActionListener(this);

    okButton.setToolTipText("Save these changes");
    cancelButton.setToolTipText("Cancel changes");
    closeButton.setToolTipText("Close dialog");

    Container c = getContentPane();

    if (c instanceof JComponent)
      ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel southPanel = new JPanel();
    southPanel.setLayout(new BorderLayout());
    southPanel.add(new JSeparator(), BorderLayout.NORTH);
    southPanel.add(controlPanel, BorderLayout.CENTER);

    c.add(southPanel, BorderLayout.SOUTH);
  }

  /** extend a modal JDialog ... */
  private ViewerDialog() {
    super();
  }

  /** extend a modal JDialog ... giving it a frame */
  private ViewerDialog(Frame f) {
    super(f);
  }

  /** make sure that closing the window simulates clicking the cancel button */
  public void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      if (cancelButton.isVisible()) cancelButton.doClick();
      else if (closeButton.isVisible()) closeButton.doClick();
      else {
        viewerValue = CLOSE_OPTION;
        setVisible(false);
      }
    }
  }

  /** updates the UI's of the buttons */
  public void updateDialogUI() {
    SwingUtilities.updateComponentTreeUI(this);

    okButton.updateUI();
    cancelButton.updateUI();
    closeButton.updateUI();
  }

  /** Update the UI's of the recycled dialogs */
  public static void updateReusableDialogUIs() {
    reusedDialog.updateDialogUI();
    backupReusedDialog.updateDialogUI();
  }

  /** hide dialog when a button is pressed, and set the value of viewerValue */
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();

    if (source == okButton) {
      viewerValue = OK_OPTION;
      setVisible(false);
    } else if (source == cancelButton) {
      viewerValue = CANCEL_OPTION;
      setVisible(false);
    } else if (source == closeButton) {
      viewerValue = CLOSE_OPTION;
      setVisible(false);
    } else {
      viewerValue = CANCEL_OPTION;
      setVisible(false);
    }
  }

  /** get the value set when a button is clicked */
  public int getValue() {
    return viewerValue;
  }

  /** configures the dialog to fit this option type defaults to an ok-cancel dialog type */
  private void setOption(int dialogOption) {
    switch (dialogOption) {
      case CLOSE_DIALOG_OPTION:
        if (controlPanel.isAncestorOf(okButton)) controlPanel.remove(okButton);
        if (controlPanel.isAncestorOf(cancelButton)) controlPanel.remove(cancelButton);

        if (!(controlPanel.isAncestorOf(closeButton))) controlPanel.add(closeButton);
        break;
      case OK_CANCEL_DIALOG_OPTION:
      default:
        if (controlPanel.isAncestorOf(closeButton)) controlPanel.remove(closeButton);

        if (!(controlPanel.isAncestorOf(okButton))) controlPanel.add(okButton);
        if (!(controlPanel.isAncestorOf(cancelButton))) controlPanel.add(cancelButton);
        break;
    }
  }

  /**
   * Given a component and a title, show an ok-cancel dialog
   *
   * @return OK_OPTION (if ok button was clicked)
   * @return CANCEL_OPTION (if cancel button was clicked)
   */
  public static final int showOkCancelDialog(Component comp, String title) {
    return (showDialog(comp, title, OK_CANCEL_DIALOG_OPTION));
  }

  /** Given a component and a title, show a dialog (with a CLOSE button) */
  public static final void showCloseDialog(Component comp, String title) {
    showDialog(comp, title, CLOSE_DIALOG_OPTION);
  }

  /**
   * Given a component and a title, set the title of the dialog to this ... then add the component
   * to the dialog Take into consideration that the recycled dialog might be in use ... ... if so,
   * use the backup dialog ... If that is in use also, create another dialog and use that
   *
   * @param comp: The component ... if null, CANCEL_OPTION is returned
   * @param title: can be null
   * @param dialogOption: CLOSE_DIALOG_OPTION or OK_CANCEL_DIALOG_OPTION
   * @return OK_OPTION if the okbutton was clicked
   * @return CANCEL_OPTION (any other time)
   */
  private static final int showDialog(Component comp, String title, int dialogOption) {
    if (comp == null) return CANCEL_OPTION;

    ViewerDialog dialog = reusedDialog;

    // if already in use, set to the backup dialog
    if (dialog.isVisible()) dialog = backupReusedDialog;

    // if already in use, instantiate  to a new ViewerDialog
    if (dialog.isVisible()) dialog = new ViewerDialog();

    if (title != null) dialog.setTitle(title);

    dialog.setOption(dialogOption);

    Container c = dialog.getContentPane();
    c.add(comp, BorderLayout.CENTER);
    dialog.pack();
    Misc.centerOnScreen(dialog);
    dialog.show();
    c.remove(comp);
    dialog.setTitle("");

    return dialog.getValue();
  }
}
