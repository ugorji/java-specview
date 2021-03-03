package spec.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JViewport;
import spec.lib.graph.Graph;
import spec.main.SpecConstants;
import spec.spectrum.DrawableSpectrum;
import spec.spectrum.Spectrum;
import spec.spectrum.SpectrumException;

/**
 * An internal frame specifically designed for SpecView
 *
 * <p>It has a scroll pane for scrolling the displayed graph It also has actions to allow the graph
 * to be expanded to fill up the whole frame
 */
public class SpectrumInternalFrame extends JInternalFrame implements ActionListener, SpecConstants {
  /** property that the spectrum frame has been fully closed and removed from containers */
  public static final String SPECFRAME_CLOSED_FULLY_PROPERTY = "Spectrum Frame fully Closed";

  private Graph graph;
  private DrawableSpectrum drawSpectrum;
  // private JMenuBar menuBar = new JMenuBar ();
  private SpectrumManipulator spectrumManipulator;
  private PlotPanel plotPanel;
  private SpecUI.MDI specUI;
  public int specFrameRefNum = 0;
  protected static int numSpecFrames = 0;

  protected static final String[] specFrameCommands =
      new String[] {
        "display",
        "print",
        "capture",
        "overlay",
        "refresh",
        "redraw",
        "resizeGraph",
        "clear",
        "close"
      };

  /** dialog for window captures */
  protected static final WindowCaptureDialog windowCaptureDialog = new WindowCaptureDialog();

  /** scroll pane for the spectrum frames */
  protected JScrollPane scrollPane = new JScrollPane();

  /** minimum size of the spectrum frame */
  public static final Dimension minimumSize = new Dimension(150, 150);

  public static String helpMsg;

  static {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("The graph frame allows you\n");
    strBuf.append("analyze and manipulate spectra\n");
    strBuf.append("visually\n");

    helpMsg = strBuf.toString();
  }

  /**
   * Make a new internal frame for the spectrum display ... Do not initialize the spectrum from here
   * since everything has to ... be visible (hierachy of containers must be visible to avoid a
   * NullPointerEx ... when creating graphics for the various sub-components *** DO NOT ALLOW ANYONE
   * INSTANTIATE THIS *
   */
  private SpectrumInternalFrame() {}

  /**
   * allows you add the frame to a PlotPanel and display a spectrum in it and get the reference to
   * the spectrumManipulator managing you
   */
  public SpectrumInternalFrame(
      SpecUI.MDI specUI,
      PlotPanel plotPanel,
      DrawableSpectrum drawSpectrum,
      SpectrumManipulator spectrumManipulator)
      throws PlotPanel.PlotPanelException {
    super("No spectrum displayed", true, true, true, true);

    // SPECUI DOES ALL THE WORK ASSOCIATED WITH CLOSING
    setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

    // only one listener can (and should) be registered for internal frame events
    // let that be the SpecUI (since it has to remove the menuItem assos. and any other things )

    // window cannot be smaller that 150x150 pixels (the defined minimum size)
    setMinimumSize(minimumSize);

    this.specUI = specUI;
    this.addInternalFrameListener(this.specUI);
    this.addPropertyChangeListener(this.specUI);
    this.spectrumManipulator = spectrumManipulator;
    this.plotPanel = plotPanel;

    // set the Layout for the contentPane to border layout
    Container cont = getContentPane();
    LayoutManager layout = cont.getLayout();
    if (!(layout instanceof BorderLayout)) cont.setLayout(new BorderLayout());
    // add the scroll pane to the frame
    cont.add(scrollPane, BorderLayout.CENTER);

    // setVisible (true);
    // set the bounds to this before you maximize it ... so ...
    setBounds(10, 10, 300, 300);

    // add the frame to the desktop
    this.addToDesktop();

    numSpecFrames++;
    this.specFrameRefNum = numSpecFrames;

    this.setTitle("No spectrum displayed");

    // add the spectrum ... since everything is visible now
    try {
      this.addDrawableSpectrum(drawSpectrum);
    } catch (SpectrumException e) {
      JOptionPane.showMessageDialog(
          this, e.getMessage(), "Error displaying spectrum", JOptionPane.ERROR_MESSAGE);
    }
  }

  /** show help information */
  public static void aboutSpecGraph() {
    JOptionPane.showMessageDialog(
        null, helpMsg, "About Spec graph", JOptionPane.INFORMATION_MESSAGE);
  }

  /** returns a string representing the spec internal frame */
  public String toString() {
    return (this.getTitle());
  }

  /** override the setTitle to include the ref number */
  public void setTitle(String str) {
    super.setTitle("[" + this.specFrameRefNum + "] " + str.trim());
  }

  /** gets the drawable Spectrum */
  public DrawableSpectrum getDrawableSpectrum() {
    return drawSpectrum;
  }

  /** adds the specframe to the PlotPanel */
  private void addToDesktop() {
    try {
      // only one internal frame can be maximum at any given time ...
      // so restore all others to non-maximized size
      plotPanel.unMaximizeAllFrames();

      // add this to the plot Panel
      plotPanel.add(this);
      // selects, shows and brings this to the front ... U need to add it to a container b4 U call
      // show
      this.show();
      // set the bounds to this before you maximize it ... so restoring brings this bounds back
      this.setBounds(10, 10, 300, 300);
      // try and select this frame ...
      // plotPanel.getDesktopManager().maximizeFrame (this);
      this.setMaximum(true);
    } catch (java.beans.PropertyVetoException e) {
    }
  }

  /** add a spectrum to this and throw an exception if the spectrum member exists (non-null) */
  public void addSpectrum(Spectrum aSpectrum) throws SpectrumException {
    if (aSpectrum == null) return;

    addDrawableSpectrum(spectrumManipulator.getDrawableSpectrum(aSpectrum));
  }

  /** add a drawable spectrum */
  public void addDrawableSpectrum(DrawableSpectrum drawSpectrum) throws SpectrumException {
    if (drawSpectrum == null) return;

    if (this.drawSpectrum != null)
      throw new SpectrumException("A spectrum is already contained here ... clear first");

    try {
      this.drawSpectrum = drawSpectrum;
      this.graph = this.drawSpectrum.getGraph();
      this.setTitle(this.drawSpectrum.getSpecName());

      // add the spectrum to this spectrumManipulator
      spectrumManipulator.addDrawableSpectrum(this.drawSpectrum);
      updateGraph();
      // add the mapping of this drawSpectrum to this frame
      spectrumManipulator.mapSpectrumToFrame(this.drawSpectrum, this);
    } finally {
      specUI.enableSpecFrameActions();
    }
  }

  /**
   * updates the graph bounds to fill the whole frame ... and stretches the graph to fill the frame
   */
  protected void updateGraph() {
    updateGraph(true);
  }

  /**
   * updates the graph
   *
   * @param resizeGraph if true, stretch the graph to fill the frame
   */
  protected void updateGraph(boolean resizeGraph) {
    if (graph == null) return;

    if (!(graph.isVisible())) graph.setVisible(true);

    JViewport viewport = scrollPane.getViewport();

    if (viewport != null) {
      if (viewport.getView() != graph) {
        viewport.setView(graph);
      }
    } else {
      scrollPane.setViewportView(graph);
      viewport = scrollPane.getViewport();
    }

    if (!(viewport.isBackingStoreEnabled())) viewport.setBackingStoreEnabled(true);

    if (!(viewport.isVisible())) viewport.setVisible(true);

    if (resizeGraph) {
      scrollPane.getVerticalScrollBar().setVisible(false);
      scrollPane.getHorizontalScrollBar().setVisible(false);

      Rectangle rect = scrollPane.getViewportBorderBounds();
      graph.setSize((int) rect.getWidth() - 1, (int) rect.getHeight() - 1);
    }

    // scrollPane.validate ();
    getContentPane().repaint();
  }

  /**
   * clears the contents of the specframe argument to determine whether to prompt for a confirmation
   * first or not returns true or false if graph was cleared or not
   */
  public boolean clear(boolean confirm) {
    if (this.drawSpectrum == null) return false;

    if (confirm) {
      int reply =
          JOptionPane.showConfirmDialog(
              this,
              "Do you want to clear this graph ... \nThe spectrum displayed will still be open",
              "Clear graph?",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);

      if (reply != JOptionPane.YES_OPTION) return false;
    }

    // ensure that the frame is not iconified
    try {
      if (this.isIcon()) this.setIcon(false);
    } catch (java.beans.PropertyVetoException e) {
    }

    try {
      // Container graphContainer = this.getContentPane();
      // if (graphContainer.isAncestorOf (graph) )
      //  graphContainer.remove (graph);

      // use setView instead of setViewportView, since painting with a null view
      // ... and backingStoreEnabled causes a null pointer exception
      scrollPane.getViewport().remove(graph);
      scrollPane.setViewportView(null);
      scrollPane.setViewport(null);

      // remove the mapping of this drawSpectrum to this frame ... b4 setting graph to null
      spectrumManipulator.unMapSpectrumToFrame(this.drawSpectrum);

      this.graph = null;
      this.drawSpectrum = null;
      this.setTitle("No spectrum displayed");
      this.repaint();

      return true;
    } finally {
      specUI.enableSpecFrameActions();
    }
  }

  /** clears the contents of the specframe (defaults to prompting for confirmation first) */
  public boolean clear() {
    return (clear(true));
  }

  /** resizeGraphs the graph to fill the spec frame, thus removing the scroll bars */
  public void resizeGraph() {
    try {
      specUI.setWaitCursor();
      updateGraph();
    } finally {
      specUI.setDefaultCursor();
    }
  }

  /** shows the spectrumManipulator dialog ... so we select a spectrum */
  public void display() {
    try {
      specUI.setWaitCursor();
      // ensure that the frame is not iconified
      try {
        if (this.isIcon()) this.setIcon(false);
      } catch (java.beans.PropertyVetoException e) {
      }

      // return if a spectrum is already being displayed or spectrumManipulator is null
      if (spectrumManipulator == null) return;

      // if a drawable spectrum is contained, check if user wants to clear it
      if (this.graph != null) {
        boolean confirmedClear = this.clear();
        if (!(confirmedClear)) return;
      }

      DrawableSpectrum aDrawSpectrum = spectrumManipulator.chooseSpectrum();
      if (aDrawSpectrum == null) return;

      // if drawable spectrum is already displayed in another frame, return
      SpectrumInternalFrame aFrame = spectrumManipulator.getAssocFrame(aDrawSpectrum);
      if (aFrame != null) {
        JOptionPane.showMessageDialog(
            this,
            "The spectrum has already been displayed in the frame titled\n" + aFrame.getTitle(),
            "Spectrum already displayed",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      // add the spectrum ... since everything is visible now
      try {
        this.addDrawableSpectrum(aDrawSpectrum);
      } catch (SpectrumException e) {
        JOptionPane.showMessageDialog(
            this, e.getMessage(), "Error displaying spectrum", JOptionPane.ERROR_MESSAGE);
      }
    } finally {
      specUI.setDefaultCursor();
    }
  }

  /**
   * method to print the graph ... defaults to foreground printing ... since background printing has
   * some problems for now
   */
  public void print() {
    print(false);
  }

  /**
   * method to print the graph ... argument says whether in the background (true) or foreground
   * (false)
   */
  public void print(boolean background) {
    // ensure that the frame is not iconified
    try {
      specUI.setWaitCursor();
      if (this.isIcon()) this.setIcon(false);

      if (graph != null) {
        if (background) graph.printGraphBg();
        else graph.printGraph();
      }
    } catch (java.beans.PropertyVetoException e) {
    } finally {
      specUI.setDefaultCursor();
    }
  }

  /** method to place the graph in a dialog so it can be printed */
  public void capture() {
    if (graph == null) return;

    try {
      specUI.setWaitCursor();
      windowCaptureDialog.capture(this);
    } finally {
      specUI.setDefaultCursor();
    }
  }

  /** method to redraw the graph */
  public void redraw() {
    // ensure that the frame is not iconified
    try {
      specUI.setWaitCursor();
      if (this.isIcon()) this.setIcon(false);

      if (graph != null) {
        if (!(graph.isVisible())) graph.setVisible(true);

        graph.updateAll();
        graph.repaint();
      }
    } catch (java.beans.PropertyVetoException e) {
    } finally {
      specUI.setDefaultCursor();
    }
  }

  /** method to overlay a spectrum on another spectrum ... */
  public void overlay() {
    try {
      specUI.setWaitCursor();
      if (drawSpectrum instanceof DrawableSpectrum.OneDim)
        ((DrawableSpectrum.OneDim) drawSpectrum).overlay();
    } finally {
      specUI.setDefaultCursor();
    }
  }

  /** called to close the internal frame ... calls setClosed () */
  public void close() {
    try {
      setClosed(true);
    } catch (java.beans.PropertyVetoException e) {
      System.out.println("Problems closing the frame. ");
    }
  }

  /**
   * overrides setClosed (boolean) to obtain confirmation first ... attempts to clear graph if a
   * graph is displayed first, before prompting to close window it also selects the frame first ...
   * *** It then enables or disables the relevant actions of the SpecUI (so the specUI actions will
   * be enable or disabled correctly)
   */
  public void setClosed(boolean b) throws java.beans.PropertyVetoException {
    try {
      if (!(isSelected())) setSelected(true);

      if (b) {
        if (graph != null) {
          String msg =
              "Close this window? (after clearing any graphs on it ... "
                  + "\nAny spectrum contained will still remain open!!!)";
          int value =
              JOptionPane.showConfirmDialog(this, msg, "Close window?", JOptionPane.YES_NO_OPTION);

          if (value != JOptionPane.YES_OPTION) return;

          // clear the graph first ... without prompting for a confirmation
          clear(false);
        }
        this.removePropertyChangeListener(this.specUI);
      }
      super.setClosed(b);
    } finally {
      // enable the actions of the specUI
      if (specUI != null) specUI.enableActions();
    }
  }

  /** Handles the menu ... from ActionListener Interface */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();

    if (command.equals("display")) {
      this.display();
    } else if (command.equals("print")) {
      this.print();
    } else if (command.equals("capture")) {
      this.capture();
    } else if (command.equals("overlay")) {
      this.overlay();
    } else if (command.equals("refresh")) {
      this.repaint();
    } else if (command.equals("redraw")) {
      this.redraw();
    } else if (command.equals("clear")) {
      this.clear();
    } else if (command.equals("close")) {
      this.close();
    } else if (command.equals("about")) {
      // System.out.println ("display information about the application called");
      String msg = "Allows the display and visual manipulation of spectra";
      JOptionPane.showMessageDialog(this, msg, "About Spec graph", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  /** A JDialog customized for window captures */
  protected static class WindowCaptureDialog extends JDialog
      implements ActionListener, KeyListener {

    private SpectrumInternalFrame frame;
    private Graph graph;
    private int preferredHt = 400;
    private int preferredW = 500;

    private String closeButtonText = "RETURN GRAPH TO GRAPH WINDOW";
    private JButton closeButton = new JButton(closeButtonText);

    // private JDialog closeButtonDialog;

    /** WindowCaptureDialog is modal, resizable, with a BorderLayout */
    protected WindowCaptureDialog() {
      super();

      this.setModal(true);

      // this.enableEvent (AWTEvent.KEY_EVENT_MASK);

      if (!(isResizable())) setResizable(true);
      if (isVisible()) setVisible(false);

      // default size
      this.setSize(preferredW, preferredHt);

      this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

      closeButton.setToolTipText(
          "Returns the graph to the graph window \nwhich previously contained it");
      closeButton.setActionCommand(closeButtonText);
      closeButton.addActionListener(this);

      this.addKeyListener(this);

      // closeButtonDialog = new JDialog (this, closeButtonText, true);
      // closeButtonDialog.getContentPane ().add (closeButton);

      JPanel southPanel = new JPanel();
      JPanel closePanel = new JPanel();
      closePanel.add(closeButton);
      southPanel.setLayout(new BorderLayout());
      southPanel.add(new JSeparator(), BorderLayout.NORTH);
      southPanel.add(closePanel, BorderLayout.CENTER);

      Container theContentPane = getContentPane();
      LayoutManager layout = theContentPane.getLayout();
      if (layout == null || !(layout instanceof BorderLayout))
        theContentPane.setLayout(new BorderLayout());
      // add the southPanel ... remove now ... let us use Ctrl-Z to return
      // theContentPane.add (southPanel, BorderLayout.SOUTH);

    }

    /** make sure that closing the window returns the graph */
    public void processWindowEvent(WindowEvent e) {
      super.processWindowEvent(e);
      if (e.getID() == WindowEvent.WINDOW_CLOSING) {
        returnGraph();
      }
    }

    /**
     * ... given a frame, the associated graph is removed from the frame and added to this dialog
     * ...
     */
    protected void capture(SpectrumInternalFrame frame) {
      if (frame == null) return;

      StringBuffer msgB = new StringBuffer();
      msgB.append("This will put the graph in a window so it can be captured and printed\n");
      msgB.append("        ********* NOTE (below) ***************\n");
      msgB.append("        To return the graph to the graph window:\n");
      msgB.append("        1) Hit the SPACE BAR <OR>\n");
      msgB.append("        2) Hit ENTER <OR>\n");
      msgB.append("        3) close the window\n");
      msgB.append("        **************************************\n");
      msgB.append("        --->\n        --->        PROCEED???");
      String msg = msgB.toString();
      msgB = null;

      int reply =
          JOptionPane.showConfirmDialog(
              frame,
              msg,
              "Screen capture printing",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);

      if (reply != JOptionPane.YES_OPTION) return;

      DrawableSpectrum drawSpec = frame.getDrawableSpectrum();

      if (drawSpec == null) return;

      int w = getWidth();
      int h = getHeight();
      if (w < preferredW) w = preferredW;
      if (h < preferredHt) h = preferredHt;
      setSize(w, h);

      this.frame = frame;
      this.graph = drawSpec.getGraph();

      this.frame.scrollPane.getViewport().remove(graph);
      this.frame.scrollPane.setViewportView(null);
      this.frame.scrollPane.setViewport(null);
      this.frame.scrollPane.repaint();

      this.setLocationRelativeTo(frame);

      msgB = new StringBuffer();
      msgB.append("Screen capture for ");
      // msgB.append (frame.getTitle ().trim () );
      msgB.append(drawSpec.getSpectrum().getSpecName().trim());
      msgB.append(" *** Hit SPACE BAR or ENTER or close window ... to return ***");
      msg = msgB.toString();
      msgB = null;
      this.setTitle(msg);

      Container graphContainer = this.getContentPane();
      // Insets insets = graphContainer.getInsets ();
      // graphContainer.setSize
      // (graph.getWidth()+insets.left+insets.right, graph.getHeight()+insets.top+insets.bottom);

      graphContainer.add(graph, BorderLayout.CENTER);

      if (!(graph.isVisible())) graph.setVisible(true);

      this.requestFocus();

      this.pack();
      this.show();
    }

    /** return the graph to the spectrum internal frame */
    private void returnGraph() {
      this.getContentPane().remove(graph);
      // update the graph without resizing ...
      frame.updateGraph(false);
      frame.scrollPane.validate();
      frame = null;
      graph = null;
      this.setVisible(false);
    }

    /**
     * performs action when the closeButton is clicked ... ... or if that action is sent (e.g.
     * through closing the dialog window to hide the dialog and return the graph to the window it
     * came from
     */
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == closeButton || closeButtonText.equals(e.getActionCommand())) {
        returnGraph();
      }
    }

    public void keyPressed(KeyEvent e) {}

    /** if the space bar of enter key is released (after being pressed), return the graph */
    public void keyReleased(KeyEvent e) {
      int i = e.getKeyCode();

      switch (i) {
        case KeyEvent.VK_SPACE:
        case KeyEvent.VK_ENTER:
          returnGraph();
          break;
      }
    }

    public void keyTyped(KeyEvent e) {}

    /** return the minimum size of this dialog */
    public Dimension getMinimumSize() {
      return SpectrumInternalFrame.minimumSize;
    }
  }
}
