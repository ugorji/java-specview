package spec.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import spec.lib.Misc;
import spec.lib.io.IOUtilities;
import spec.lib.io.TextAreaOutputStream;

/**
 * This class is an interface for typing commands directly to the Java virtual machine ... (most
 * likely using Tcl/TK ... or other verbs Note: Only one SpectrumTranscript can be open at any time
 * ... ... since we need to redirect System.out and System.err so their messages can appear in out
 * textarea
 *
 * <p>A StringBufferOutputStream is used as the standard error ... When information is written to
 * the std err, we just append it to the string buffer ... and inform the user that errors were
 * written to the string buffer The user can choose to view it, in which we just append the string
 * to a textarea ... which he can clear when he is done viewing ... or log to a file
 *
 * <p>A Timer is used to monitor error information ... it basically checks every ERROR_TIMER_DELAY
 * seconds ... if the error buffer has changed in size and notifies us.
 */
public class SpectrumTranscript extends JDialog implements ActionListener {

  /** where information is dumped */
  protected JTextArea dumpArea = new JTextArea(20, 0);
  /** where typed commands are stored */
  protected JTextArea controlArea = new JTextArea(4, 0);

  /** where standard error is displayed */
  protected JTextArea errorArea = null;
  /** length of the document of the errorArea */
  private int recordedErrorContentLength = 0;

  /** where commands are typed */
  protected JTextField controlField = new JTextField();

  /** button to allow error information be logged */
  protected JButton errorLogButton = new JButton("log error");
  /** button to clear error information */
  protected JButton errorClearButton = new JButton("Clear error");
  /** button to close error information */
  protected JButton errorCloseButton = new JButton("Close");

  /**
   * Timer that periodically checks to see if the document of the errorArea has increased in size
   */
  private Timer errorTimer;

  public JMenuBar menuBar = new JMenuBar();
  /** the associated specUI */
  protected SpecUI specUI;

  /** boolean to indicate that the solo instance of SpectrumTranscript has been created or not */
  private static boolean transcriptCreated = false;
  /** single instance of SpectrumTranscript */
  private static SpectrumTranscript soloInstance;

  private JSplitPane splitPane;

  public static final String SAVE_DUMP_COMMAND = "save dump";
  public static final String SAVE_CONTROL_COMMAND = "save control";
  public static final String HIDE_COMMAND = "hide transcript";
  public static final String CLEAR_DUMP_COMMAND = "clear dump";
  public static final String CLEAR_CONTROL_COMMAND = "clear control";
  public static final String SHOW_ERROR_COMMAND = "Show Error";
  public static final String CLEAR_ERROR_COMMAND = "Clear error";
  public static final String SAVE_ERROR_COMMAND = "save error";

  /** time in milliseconds between times when we check for errors */
  public static final int ERROR_TIMER_DELAY = 6000;

  public static String specViewInfo;
  public static String initInfo;

  // Streams ... so that our System.out and System.err will be put into the text area
  protected PrintStream outStream;
  protected PrintStream errStream;

  /** Dialog for displaying errors ... make it modal */
  protected final JDialog errorDialog;
  /** component for the error information */
  private JPanel errorInfoComp = new JPanel(new GridLayout(0, 1));

  /** calendar used for formatting the time */
  private Calendar cal = Calendar.getInstance();

  static {
    StringBuffer strBuf = new StringBuffer();

    strBuf.append("\tSpecView for Spectral Analysis\n");
    strBuf.append("\tDeveloped in NSCL-MSU\n");
    strBuf.append("\t******************************************\n");

    specViewInfo = strBuf.toString();

    strBuf.delete(0, strBuf.length());

    strBuf.append("\n\tThis frame is used to interact with the JVM\n");

    initInfo = strBuf.toString();
  }

  /**
   * Way to get a SpectrumTranscript ... We can only have one SpectrumTranscript object at any time
   * ... since it extends the System.out to print stuff on its textarea
   */
  public static SpectrumTranscript createSpectrumTranscript(SpecUI specUI) {
    if (transcriptCreated) return soloInstance;

    soloInstance = new SpectrumTranscript(specUI);
    transcriptCreated = true;
    return soloInstance;
  }

  /**
   * make it protected only so that sub classes can extend it ... should really be private since we
   * want to maintain that only ... one transcript can be around at any one time
   */
  protected SpectrumTranscript(SpecUI specUI) {
    super(specUI.getMainWindow(), "Spectrum Transcript", false);
    // super ("Spectrum Transcript");
    this.specUI = specUI;

    dumpArea.setText(specViewInfo);
    dumpArea.append(initInfo);

    dumpArea.setEditable(false);
    controlArea.setEditable(false);
    controlField.setEditable(true);
    controlField.addActionListener(this);

    errorLogButton.setActionCommand(SAVE_ERROR_COMMAND);
    errorLogButton.addActionListener(this);
    errorClearButton.setActionCommand(CLEAR_ERROR_COMMAND);
    errorClearButton.addActionListener(this);
    errorCloseButton.addActionListener(this);

    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    createMenuBar();
    setJMenuBar(menuBar);

    errorDialog = new JDialog(this, "Standard Error", false);
    errorDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    errorArea = new JTextArea(20, 0);

    updateLayout();

    controlField.setText(">> ");

    setStreams();

    JLabel ll1 = new JLabel("Error information generated ... ");
    JLabel ll2 = new JLabel("        View error???");

    errorInfoComp.setBorder(BorderFactory.createLineBorder(Color.red, 5));

    errorInfoComp.add(ll1);
    errorInfoComp.add(ll2);

    errorTimer = new Timer(ERROR_TIMER_DELAY, this);
    errorTimer.addActionListener(this);
    errorTimer.setRepeats(true);
    errorTimer.start();
  }

  /** redirect the output stream to the text area ... */
  private void setStreams() {
    TextAreaOutputStream out = new TextAreaOutputStream(dumpArea);
    outStream = new PrintStream(out, true); // autoflush
    // DocumentOutputStream err = new DocumentOutputStream (errorContent);
    TextAreaOutputStream err = new TextAreaOutputStream(errorArea);
    errStream = new PrintStream(err, true); // autoflush

    System.setOut(outStream);
    System.setErr(errStream);
  }

  /** update the layout */
  private void updateLayout() {
    JScrollPane scrollPane;

    splitPane.setOneTouchExpandable(true);

    Container c = getContentPane();
    c.add(splitPane, BorderLayout.CENTER);

    scrollPane = new JScrollPane(dumpArea);
    scrollPane.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Information dump",
            TitledBorder.LEFT,
            TitledBorder.TOP));

    splitPane.setTopComponent(scrollPane);

    scrollPane = new JScrollPane(controlArea);

    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(new BorderLayout());
    lowerPanel.add(scrollPane, BorderLayout.CENTER);
    lowerPanel.add(controlField, BorderLayout.SOUTH);

    lowerPanel.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Control area",
            TitledBorder.LEFT,
            TitledBorder.TOP));

    splitPane.setBottomComponent(lowerPanel);

    Misc.proportionOnScreen(this, 0.50, 0.50);

    pack();

    JPanel p = new JPanel();
    p.add(errorLogButton);
    p.add(errorClearButton);
    p.add(errorCloseButton);

    JPanel pp = new JPanel(new BorderLayout());
    pp.add(p, BorderLayout.CENTER);
    pp.add(new JSeparator(), BorderLayout.NORTH);

    Container cc = errorDialog.getContentPane();
    cc.add(new JScrollPane(errorArea), BorderLayout.CENTER);
    cc.add(pp, BorderLayout.SOUTH);

    errorDialog.pack();
    Misc.centerOnScreen(errorDialog);
  }

  /** Take the text typed in the control string append it to the controlArea ... and act on it */
  public void control() {
    String str = controlField.getText();

    if (str == null || str.length() <= 0) return;

    while (str.startsWith(">")) {
      str = (str.substring(1)).trim();
    }
    if (str.length() > 0) {
      controlArea.append(str + '\n');
      act(str);
    }

    controlField.setText(">> ");
  }

  /** Does nothing for now */
  public void act(String str) {}

  /** Handle actions */
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    String command = e.getActionCommand();

    if (source == controlField) {
      control();
    } else if (source == errorCloseButton) {
      errorDialog.setVisible(false);
    } else if (source == errorTimer) {
      Document errorContent = errorArea.getDocument();
      int ebLength = errorContent.getLength();
      if (ebLength > recordedErrorContentLength) {
        // System.out.println ("Error information generated ...");
        // JOptionPane.showMessageDialog
        // (this, "Error information generated ...", "Error information generated ...",
        // JOptionPane.ERROR_MESSAGE);
        // int i = ViewerDialog.showOkCancelDialog (errorInfoComp, "Error information generated
        // ...");
        // if (i == ViewerDialog.OK_OPTION) {
        // errorDialog.show ();
        // }

        String currentTime = Misc.getCurrentTime();

        String errorInfo = "Error information generated at " + currentTime + " ... view error\n";
        specUI.setStatusText(errorInfo);
        dumpArea.append(errorInfo);
      }

      recordedErrorContentLength = ebLength;
    }
    // Transcript menu commands
    else if (HIDE_COMMAND.equals(command)) {
      setVisible(false);
    } else if (SHOW_ERROR_COMMAND.equals(command)) {
      errorDialog.show();
    }
    // Clear menu commands
    else if (CLEAR_DUMP_COMMAND.equals(command)) {
      dumpArea.setText(specViewInfo);
    } else if (CLEAR_CONTROL_COMMAND.equals(command)) {
      controlArea.setText("");
    } else if (CLEAR_ERROR_COMMAND.equals(command)) {
      Document errorContent = errorArea.getDocument();
      int length = errorContent.getLength();
      try {
        errorContent.remove(0, length);
      } catch (BadLocationException ble) {
      }
    }
    // save the info in the dump area
    else if (SAVE_DUMP_COMMAND.equals(command)) {
      save(dumpArea.getText());
    }
    // Save the control commands
    else if (SAVE_CONTROL_COMMAND.equals(command)) {
      save(controlArea.getText());
    } else if (SAVE_ERROR_COMMAND.equals(command)) {
      save(errorArea.getText());
    }
    // TestMenu commands
    else if ("testStdOut".equals(command)) {
      System.out.println("Test: This was printed out to std output");
    } else if ("testStdErr".equals(command)) {
      System.err.println("Test: This was printed out to std error");
    }
  }

  /** creates the menu bar */
  private void createMenuBar() {
    JMenuItem menuItem;
    JMenu transcriptMenu = (JMenu) menuBar.add(new JMenu("Transcript"));
    transcriptMenu.setMnemonic('T');

    String[] transcriptStrings =
        new String[] {"Hide Transcript", "Show Error", "Save Dump", "Save Control", "Save Error"};
    String[] transcriptCommands =
        new String[] {
          HIDE_COMMAND,
          SHOW_ERROR_COMMAND,
          SAVE_DUMP_COMMAND,
          SAVE_CONTROL_COMMAND,
          SAVE_ERROR_COMMAND
        };

    for (int i = 0; i < transcriptStrings.length; i++) {
      menuItem = (JMenuItem) transcriptMenu.add(transcriptStrings[i]);
      menuItem.setMargin(new Insets(1, 1, 1, 1));
      menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
      menuItem.setActionCommand(transcriptCommands[i]);
      menuItem.addActionListener(this);
    }

    JMenu clearMenu = (JMenu) menuBar.add(new JMenu("Clear"));
    clearMenu.setMnemonic('C');

    String[] clearStrings = new String[] {"Clear Dump", "Clear control", "Clear error"};
    String[] clearCommands =
        new String[] {CLEAR_DUMP_COMMAND, CLEAR_CONTROL_COMMAND, CLEAR_ERROR_COMMAND};

    for (int i = 0; i < clearStrings.length; i++) {
      menuItem = (JMenuItem) clearMenu.add(clearStrings[i]);
      menuItem.setMargin(new Insets(1, 1, 1, 1));
      menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
      menuItem.setActionCommand(clearCommands[i]);
      menuItem.addActionListener(this);
    }

    JMenu testMenu = (JMenu) menuBar.add(new JMenu("Test"));
    testMenu.setMnemonic('S');

    String[] testStrings = new String[] {"Test Std Output", "Test Std Error"};
    String[] testCommands = new String[] {"testStdOut", "testStdErr"};

    for (int i = 0; i < testStrings.length; i++) {
      menuItem = (JMenuItem) testMenu.add(testStrings[i]);
      menuItem.setMargin(new Insets(1, 1, 1, 1));
      menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
      menuItem.setActionCommand(testCommands[i]);
      menuItem.addActionListener(this);
    }
  }

  /** save some string to a file */
  private final boolean save(String contents) {
    boolean isSaved = IOUtilities.save(contents, specUI.getSpectrumFileChooser());
    return isSaved;
  }
}
