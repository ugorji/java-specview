package spec.lib.ui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterJob;
import javax.swing.JButton;
import javax.swing.JDialog;

/** extends Thread to cancel a printing job */
public class CancelPrint extends Thread implements ActionListener {
  private final JButton cancelButton = new JButton("Cancel Printing");
  private final PrinterJob printerJob;
  private final Frame frame;
  private final JDialog cancelDialog;
  private boolean printingDone = false;

  /** frame can be null */
  public CancelPrint(PrinterJob printerJob, Frame frame) {
    super();
    // this.setPriority (Thread.MIN_PRIORITY);

    this.printerJob = printerJob;
    this.frame = frame;

    if (frame != null) {
      cancelDialog = new JDialog(frame);
      cancelDialog.setLocationRelativeTo(frame);
    } else cancelDialog = new JDialog();

    cancelDialog.setModal(false);
    cancelDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    cancelDialog.getContentPane().add(cancelButton);

    cancelButton.addActionListener(this);
  }

  public CancelPrint(PrinterJob printerJob) {
    this(printerJob, null);
  }

  public void start() {
    cancelDialog.pack();
    cancelDialog.show();
    cancelDialog.repaint();

    super.start();
  }

  /** what the ... are you doing here */
  public void run() {
    while (!(printingDone)) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }

      // yield ();
    }
    cancelDialog.dispose();
  }

  public void setPrintingDone(boolean b) {
    printingDone = b;
  }

  public JDialog getDialog() {
    return cancelDialog;
  }

  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == cancelButton) {
      printerJob.cancel();
      setPrintingDone(true);
      cancelDialog.dispose();
    }
  }
}
