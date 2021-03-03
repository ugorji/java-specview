package spec.lib.ui;

import java.awt.Frame;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

public class BackgroundPrint extends Thread {
  private Printable printable;
  private Frame frame;
  private String jobName;

  public BackgroundPrint(Printable printable, Frame frame, String jobName) {
    super();
    this.setPriority(Thread.MIN_PRIORITY);

    this.printable = printable;
    this.frame = frame;
    this.jobName = jobName;
  }

  public BackgroundPrint(Printable printable, Frame frame) {
    this(printable, frame, null);
  }

  public void run() {
    if (printable == null || frame == null) return;

    PrinterJob printJob = PrinterJob.getPrinterJob();

    CancelPrint cancelPrint = new CancelPrint(printJob, frame);
    cancelPrint.start();

    // Ask user for page format (e.g., portrait/landscape)
    PageFormat pf = printJob.pageDialog(printJob.defaultPage());

    if (jobName != null) printJob.setJobName(jobName);

    printJob.setPrintable(printable, pf);

    if (printJob.printDialog()) {
      try {
        cancelPrint.getDialog().toFront();
        printJob.print();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        cancelPrint.setPrintingDone(true);
      }
    }
  }
}
