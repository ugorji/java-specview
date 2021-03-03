package spec.spectrum;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.SwingPropertyChangeSupport;
import spec.io.SpecFormat;
import spec.io.SpectrumReader;
import spec.lib.ui.ViewerDialog;

/**
 * let class implement Cloneable (so we can make a clone of it) and Serializable (so we can save its
 * state)
 */
public abstract class Spectrum implements SpectrumInterface, Cloneable, Serializable {
  /** property for the color map */
  public static final String SPECTRUM_NAME_PROPERTY = "SPECTRUM NAME PROPERTY";

  public String specName;
  protected int specDimension;
  protected Date specDate;
  public int specRefNum = 0;
  protected static int numSpectra = 0;

  /** Handles the interactive setting of the spectrum attributes */
  public static EditSpectrum specModifier = new EditSpectrum();

  /** multiply the count for each channel by a double value */
  public abstract void multiply(double value);

  /** add an int value to the count for each channel */
  public abstract void add(int value);

  /** handles the property changes */
  private SwingPropertyChangeSupport changeSupport;

  /** Constructor with arguments */
  public Spectrum(String aspecName, int aspecDimension, Date aspecDate) {
    super();
    this.specName = aspecName.trim();
    this.specDimension = aspecDimension;
    this.specDate = aspecDate;
    numSpectra++;
    this.specRefNum = numSpectra;
  }

  /** Default Constructor */
  public Spectrum() {
    this("", 1, new Date());
  }

  /** Copy Constructor */
  public Spectrum(Spectrum aSpectrum) {
    this(aSpectrum.specName, aSpectrum.specDimension, aSpectrum.specDate);
  }

  /** read the file and call the constructor below to assign instance variables */
  public Spectrum(File aspecFile) throws SpecFormat.IOException {
    this(SpectrumReader.read(aspecFile));
  }

  public String toString() {
    return (oneLineSummary());
  }

  /**
   * returns the spectrum as a one-line short string ... this can be used in a List Box to represent
   * the spectrum
   */
  public String oneLineSummary() {
    StringBuffer sb = new StringBuffer();
    sb.append("(");
    sb.append(specRefNum);
    sb.append(")");
    sb.append(specName);
    sb.append(" : ");
    sb.append(specDimension);
    sb.append(" : ");
    sb.append((new SimpleDateFormat()).format(specDate));

    return sb.toString();
  }

  /** returns the spectrum as a long string (with all the details) ... */
  public String details() {
    StringBuffer sb = new StringBuffer();
    sb.append("Spectrum Name : ");
    sb.append(specName);
    sb.append("\nSpectrum Dimension : ");
    sb.append(specDimension);
    sb.append("\nListed modification date : ");
    sb.append((new SimpleDateFormat()).format(specDate));

    return sb.toString();
  }

  public boolean equals(Object o) {
    Spectrum aSpectrum = (Spectrum) o;
    if (aSpectrum == null) return false;
    if (!(specName.equals(aSpectrum.specName))) return false;
    else if (specDimension != aSpectrum.specDimension) return false;
    else if (!(specDate.equals(aSpectrum.specDate))) return false;
    else return true;
  }

  public int hashCode() {
    int i = 17;
    i = (specName != null) ? (i ^ specName.hashCode()) : i;
    i = (i ^ specDimension);
    i = (specDate != null) ? (i ^ specDate.hashCode()) : i;
    return i;
  }

  public int getSpecDimension() {
    return specDimension;
  }

  public String getSpecName() {
    return specName;
  }

  /** set the spec name ... if the argument is more than 1 character long */
  public void setSpecName(String aSpecName) {
    String oldValue = specName;

    String str = aSpecName.trim();
    if (str.length() >= 1) specName = str;

    // fire the property Change
    this.firePropertyChange(Spectrum.SPECTRUM_NAME_PROPERTY, oldValue, specName);
  }

  public Date getSpecDate() {
    return specDate;
  }

  public void setSpecDate(Date aspecDate) {
    this.specDate = aspecDate;
  }

  /** return the maximum spectrum channel count */
  public abstract int getMaxCount();

  /** get the modifier to edit the spectrum attributes */
  public void editSpectrum() {
    specModifier.show(this);
  }

  /** get the Container that has all the fields for spectrum information */
  public static EditSpectrum getSpecModifier() {
    return specModifier;
  }

  /**
   * Support for reporting bound property changes. replica of what is in JComponent ... overloaded
   * also
   */
  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (changeSupport != null) {
      changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  /*
   * PENDING(hmuller) in JDK1.2 the following firePropertyChange overloads
   * should additional check for a non-empty listener list with
   * changeSupport.hasListeners(propertyName) before calling firePropertyChange.
   */

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Byte(oldValue), new Byte(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, char oldValue, char newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(
          propertyName, new Character(oldValue), new Character(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, short oldValue, short newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Short(oldValue), new Short(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(
          propertyName, new java.lang.Integer(oldValue), new java.lang.Integer(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, long oldValue, long newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Long(oldValue), new Long(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, float oldValue, float newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Float(oldValue), new Float(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, double oldValue, double newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(
          propertyName, new java.lang.Double(oldValue), new java.lang.Double(newValue));
    }
  }

  /**
   * Reports a bound property change.
   *
   * @see #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    if ((changeSupport != null) && (oldValue != newValue)) {
      changeSupport.firePropertyChange(propertyName, new Boolean(oldValue), new Boolean(newValue));
    }
  }

  /**
   * Add a PropertyChangeListener to the listener list. The listener is registered for all
   * properties.
   *
   * <p>A PropertyChangeEvent will get fired in response to setting a bound property, e.g. setFont,
   * setBackground, or setForeground. Note that if the current component is inheriting its
   * foreground, background, or font from its container, then no event will be fired in response to
   * a change in the inherited property.
   *
   * <p>This method will migrate to java.awt.Component in the next major JDK release
   *
   * @param listener The PropertyChangeListener to be added
   */
  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport == null) {
      changeSupport = new SwingPropertyChangeSupport(this);
    }
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Remove a PropertyChangeListener from the listener list. This removes a PropertyChangeListener
   * that was registered for all properties.
   *
   * <p>This method will migrate to java.awt.Component in the next major JDK release
   *
   * @param listener The PropertyChangeListener to be removed
   */
  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport != null) {
      changeSupport.removePropertyChangeListener(listener);
    }
  }

  /** Protected class to handle showing the dialog to edit the spectrum name and date */
  public static class EditSpectrum implements ActionListener {
    protected JPanel contentPane = new JPanel();
    // protected JPanel bottomPane = new JPanel ();

    protected JTextField nameField = new JTextField();
    protected JTextField dateField = new JTextField();

    protected JRadioButton origDateButton = new JRadioButton("Use Original Date");
    protected JRadioButton currentDateButton = new JRadioButton("Use Current Date");

    protected JTextArea infoArea = new JTextArea(7, 50);
    // protected JScrollPane infoScroller = new JScrollPane();

    protected Date chosenDate = new Date();
    protected Date origDate = null;

    private String origDateCommandString = "useOrigDate";
    private String currrentDateCommandString = "useCurrentDate";

    // date formatter object to format our dates as needed
    private SimpleDateFormat dateFormatter = new SimpleDateFormat();

    public EditSpectrum() {
      super();
      dateField.setEditable(false);

      origDateButton.setMnemonic('O');
      origDateButton.setActionCommand(origDateCommandString);
      origDateButton.addActionListener(this);
      origDateButton.setSelected(true);

      currentDateButton.setMnemonic('C');
      currentDateButton.setActionCommand(currrentDateCommandString);
      currentDateButton.addActionListener(this);

      // Group the radio buttons.
      ButtonGroup group = new ButtonGroup();
      group.add(origDateButton);
      group.add(currentDateButton);

      infoArea.setEditable(false);
      // infoScroller.getViewport().setView (infoArea);

      // set the layout of the contentPane container
      updateLayout();
    }

    protected void show(Spectrum spectrum) {
      if (spectrum == null) return;
      origDate = null;

      // get the values for this axis
      getValues(spectrum);
      int selection = ViewerDialog.showOkCancelDialog(contentPane, "Edit the spectrum attributes");
      origDate = null;

      switch (selection) {
        case ViewerDialog.OK_OPTION:
          // set the values for this axis
          setValues(spectrum);
          break;
        case ViewerDialog.CANCEL_OPTION:
        default:
          break;
      }
    }

    private void getValues(Spectrum spectrum) {
      nameField.setText(spectrum.getSpecName());
      dateField.setText(dateFormatter.format(spectrum.getSpecDate()));
      origDate = spectrum.getSpecDate();
      chosenDate = origDate;
      infoArea.setText(spectrum.details());
    }

    /** should not be called by other classes ... they should call show */
    private void setValues(Spectrum spectrum) {
      String str;
      str = nameField.getText().trim();
      if (str.length() > 1) spectrum.setSpecName(str);

      if (chosenDate != null) spectrum.setSpecDate(chosenDate);
    }

    public JPanel getContentPanel() {
      return contentPane;
    }

    /** handles updating the layout for the contentPane */
    private void updateLayout() {
      contentPane.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(5, 5, 5, 5);

      // add the components
      c.gridx = c.gridy = 0;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(new JLabel("Edit Spectrum Attrributes"), c);

      c.gridwidth = 1;

      c.gridy = 1;
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      contentPane.add(new JLabel("Spectrum Name"), c);
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(nameField, c);

      c.gridy = 2;
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      contentPane.add(new JLabel("Spectrum Date"), c);
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(dateField, c);

      c.gridwidth = 1;
      c.gridx = 0;
      c.gridy = 3;
      c.fill = GridBagConstraints.CENTER;
      contentPane.add(origDateButton, c);

      c.gridx = 1;
      c.gridy = 3;
      c.fill = GridBagConstraints.CENTER;
      contentPane.add(currentDateButton, c);

      c.gridwidth = 2;
      c.gridx = 0;
      c.gridy = 4;
      c.fill = GridBagConstraints.HORIZONTAL;
      contentPane.add(new JLabel("Spectrum Information"), c);

      c.gridx = 0;
      c.gridy = 5;
      c.fill = GridBagConstraints.CENTER;
      contentPane.add(new JScrollPane(infoArea), c);
    }

    public void actionPerformed(ActionEvent e) {
      String command = e.getActionCommand();

      if (command.equals(origDateCommandString)) {
        if (origDate != null) {
          chosenDate = origDate;
          dateField.setText(dateFormatter.format(chosenDate));
        } else {
          chosenDate = null;
          dateField.setText("Unknown original date");
        }
      } else if (command.equals(currrentDateCommandString)) {
        chosenDate = new Date();
        dateField.setText(dateFormatter.format(chosenDate));
      }
    }
  }

  /** For one dimensional spectra */
  public static class OneDim extends Spectrum {
    private SpecChannel.OneDim specChannel;

    // Default Constructor
    public OneDim() {
      super("", 1, new Date());
      specChannel = new SpecChannel.OneDim();
    }

    // Constructor with arguments - Update the argument list to initialize superclass data members
    public OneDim(String aspecName, Date aspecDate, SpecChannel.OneDim aSpecChannel) {
      super(aspecName, 1, aspecDate);
      specChannel = aSpecChannel;
    }

    // read the file and call the constructor below to assign instance variables
    public OneDim(File aspecFile) throws SpecFormat.IOException {
      super(aspecFile);
    }

    // Copy Constructor
    public OneDim(OneDim aOneDim) {
      this(aOneDim.specName, aOneDim.specDate, aOneDim.specChannel);
    }

    public String oneLineSummary() {
      StringBuffer sb = new StringBuffer();
      sb.append(super.oneLineSummary());
      // sb.append (" : ");
      // sb.append ( specChannel.toString() );

      return sb.toString();
    }

    public String details() {
      StringBuffer sb = new StringBuffer();
      sb.append(super.details());
      sb.append("\nInformation on Spectrum Channels:\n");
      sb.append(specChannel.toString());

      return sb.toString();
    }

    public boolean equals(Object o) {
      OneDim aOneDim = (OneDim) o;
      return super.equals(o);
    }

    public SpecChannel.OneDim getSpecChannel() {
      return (specChannel);
    }

    protected void setSpecChannel(SpecChannel.OneDim aSpecChannel) {
      this.specChannel = aSpecChannel;
    }

    /** return the maximum spectrum channel count */
    public int getMaxCount() {
      return (specChannel.getMaxCount());
    }

    /*
     *************************************************************************
     * INSTANCE METHODS FOR MANIPULATIONS BETWEEN TWO ONE-DIMENSIONAL SPECTRA
     *************************************************************************
     */

    /** add this spectrum to the parsed spectrum channel by channel */
    public void add(Spectrum.OneDim spectrum2) throws SpecChannel.ShapeException {
      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      int specShape = specChannel.getSpecShape();

      // throw exception if not the same shape
      if (specShape != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = specChannel.getSpecCount();
      double[] specUncertainty = specChannel.getSpecUncertainty();

      for (int i = 0; i < specShape; i++) {
        specUncertainty[i] =
            Math.sqrt(
                (specUncertainty[i] * specUncertainty[i])
                    + (specUncertainty2[i] * specUncertainty2[i]));
        specCount[i] += specCount2[i];
      }
    }

    /** subtract this spectrum to the parsed spectrum channel by channel */
    public void subtract(Spectrum.OneDim spectrum2) throws SpecChannel.ShapeException {
      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      int specShape = specChannel.getSpecShape();

      // throw exception if not the same shape
      if (specShape != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = specChannel.getSpecCount();
      double[] specUncertainty = specChannel.getSpecUncertainty();

      for (int i = 0; i < specShape; i++) {
        specUncertainty[i] =
            Math.sqrt(
                (specUncertainty[i] * specUncertainty[i])
                    + (specUncertainty2[i] * specUncertainty2[i]));
        specCount[i] -= specCount2[i];
      }
    }

    /** multiply this spectrum by the parsed spectrum channel by channel */
    public void multiply(Spectrum.OneDim spectrum2) throws SpecChannel.ShapeException {
      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      int specShape = specChannel.getSpecShape();

      // throw exception if not the same shape
      if (specShape != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = specChannel.getSpecCount();
      double[] specUncertainty = specChannel.getSpecUncertainty();

      for (int i = 0; i < specShape; i++) {
        specUncertainty[i] =
            Math.sqrt(
                Math.pow(specCount2[i] * specUncertainty[i], 2)
                    + Math.pow(specCount[i] * specUncertainty2[i], 2));
        specCount[i] *= specCount2[i];
      }
    }

    /** divide this spectrum by the parsed spectrum channel by channel */
    public void divide(Spectrum.OneDim spectrum2) throws SpecChannel.ShapeException {
      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      int specShape = specChannel.getSpecShape();

      // throw exception if not the same shape
      if (specShape != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = specChannel.getSpecCount();
      double[] specUncertainty = specChannel.getSpecUncertainty();

      for (int i = 0; i < specShape; i++) {
        specUncertainty[i] =
            (specCount[i] / specCount2[i])
                * Math.sqrt(
                    (specUncertainty[i] * specUncertainty[i]) / (specCount[i] * specCount[i])
                        + (specUncertainty2[i] * specUncertainty2[i])
                            / (specCount2[i] * specCount2[i]));
        specCount[i] /= specCount2[i];
      }
    }

    /**
     * compress a spectrum by a power of two ... e.g compressing a 256 channel by exponent of 3 ...
     * means making it to 256/(2*2*2) = 32 channels
     */
    public void compress(int exponent) throws Exception {
      int specShape = specChannel.getSpecShape();

      // maximum allowed compression exponent
      int maxExponent = (int) Math.round(Math.log((double) specShape) / Math.log(2.0));

      if (exponent >= maxExponent)
        throw new Exception(
            "Invalid exponent ... this reduces the number" + "of channels to a negative number");

      int[] specCount = specChannel.getSpecCount();
      double[] specUncertainty = specChannel.getSpecUncertainty();

      // get the new specShapes
      int numInGroup = (int) Math.pow(2.0, (double) exponent);
      int newSpecShape = specShape / numInGroup;
      // calculate the new spec counts and uncertainties
      int[] newSpecCount = new int[newSpecShape];
      double[] newSpecUncertainty = new double[newSpecShape];
      // holds the count and uncertainty sum before the cound is set
      int countSum = 0;
      double uncertaintySum = 0.0;

      for (int i = 0; i < newSpecShape; i++) {
        countSum = 0;
        uncertaintySum = 0.0;
        for (int j = numInGroup * i + 1; j <= numInGroup * i + numInGroup; j++) {
          countSum = countSum + specCount[i];
          uncertaintySum = uncertaintySum + (specUncertainty[i] * specUncertainty[i]);
        }
        // newSpecCount is the total area (sum of counts) over that region
        newSpecCount[i] = countSum;
        newSpecUncertainty[i] = Math.sqrt(uncertaintySum);
      }

      try {
        specChannel.setSpecChannelAttributes(newSpecShape, newSpecCount, newSpecUncertainty);
      } catch (Exception e) {
        System.out.println("Error in compressing the spectrum\n" + e.getMessage());
      }
    }

    /** multiply the count for each channel by a double value */
    public void multiply(double value) {
      int specShape = specChannel.getSpecShape();
      int[] specCount = specChannel.getSpecCount();
      double[] specUncertainty = specChannel.getSpecUncertainty();

      for (int i = 0; i < specShape; i++) {
        specCount[i] = (int) (specCount[i] * value);
        specUncertainty[i] = specUncertainty[i] * value;
      }
    }

    /** add an int value to the count for each channel */
    public void add(int value) {
      int specShape = specChannel.getSpecShape();
      int[] specCount = specChannel.getSpecCount();
      double[] specUncertainty = specChannel.getSpecUncertainty();

      for (int i = 0; i < specShape; i++) {
        specCount[i] += value;
        // uncertainty does not change
      }
    }

    /**
     * find the area between an interval of channels (inclusive) ... basically add the channel
     * counts in the range Convention is that channels are labelled from 1 to specShape
     */
    public int area(int lowChannel, int highChannel) throws Exception {
      int specShape = specChannel.getSpecShape();
      int[] specCount = specChannel.getSpecCount();

      if ((lowChannel >= highChannel) || (highChannel > specShape))
        throw new Exception("Invalid range of values");

      int area = 0;
      for (int i = lowChannel - 1; i < highChannel; i++) {
        area += specCount[i];
      }

      return area;
    }

    /**
     * find the average channel count between an interval of channels ... basically divide the area
     * by the range
     */
    public int average(int lowChannel, int highChannel) throws Exception {
      int area = area(lowChannel, highChannel);
      int average = area / (highChannel - lowChannel + 1);

      return average;
    }

    /*
     *************************************************************************
     * STATIC/CLASS METHODS FOR MANIPULATIONS BETWEEN TWO ONE DIMENSIONAL SPECTRA
     *************************************************************************
     */

    /** add the first spectrum to the second spectrum channel by channel */
    public static Spectrum.OneDim add(Spectrum.OneDim spectrum1, Spectrum.OneDim spectrum2)
        throws SpecChannel.ShapeException, SpectrumException {
      SpecChannel.OneDim specChannel;

      SpecChannel.OneDim specChannel1 = spectrum1.getSpecChannel();
      int specShape1 = specChannel1.getSpecShape();

      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      // throw exception if not the same shape
      if (specShape1 != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount1 = specChannel1.getSpecCount();
      double[] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = new int[specShape1];
      double[] specUncertainty = new double[specShape1];

      for (int i = 0; i < specShape1; i++) {
        specUncertainty[i] =
            Math.sqrt(
                (specUncertainty1[i] * specUncertainty1[i])
                    + (specUncertainty2[i] * specUncertainty2[i]));
        specCount[i] = specCount1[i] + specCount2[i];
      }

      String specName = spectrum1.getSpecName() + "_+_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.OneDim(specShape1, specCount, specUncertainty);

      return new Spectrum.OneDim(specName, new Date(), specChannel);
    }

    /** subtract the first spectrum from the second spectrum channel by channel */
    public static Spectrum.OneDim subtract(Spectrum.OneDim spectrum1, Spectrum.OneDim spectrum2)
        throws SpecChannel.ShapeException, SpectrumException {
      SpecChannel.OneDim specChannel;

      SpecChannel.OneDim specChannel1 = spectrum1.getSpecChannel();
      int specShape1 = specChannel1.getSpecShape();

      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      // throw exception if not the same shape
      if (specShape1 != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount1 = specChannel1.getSpecCount();
      double[] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = new int[specShape1];
      double[] specUncertainty = new double[specShape1];

      for (int i = 0; i < specShape1; i++) {
        specUncertainty[i] =
            Math.sqrt(
                (specUncertainty1[i] * specUncertainty1[i])
                    + (specUncertainty2[i] * specUncertainty2[i]));
        specCount[i] = specCount1[i] - specCount2[i];
      }

      String specName = spectrum1.getSpecName() + "_-_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.OneDim(specShape1, specCount, specUncertainty);

      return new Spectrum.OneDim(specName, new Date(), specChannel);
    }

    /** multiply the first spectrum by the second spectrum channel by channel */
    public static Spectrum.OneDim multiply(Spectrum.OneDim spectrum1, Spectrum.OneDim spectrum2)
        throws SpecChannel.ShapeException, SpectrumException {
      SpecChannel.OneDim specChannel;

      SpecChannel.OneDim specChannel1 = spectrum1.getSpecChannel();
      int specShape1 = specChannel1.getSpecShape();

      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      // throw exception if not the same shape
      if (specShape1 != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount1 = specChannel1.getSpecCount();
      double[] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = new int[specShape1];
      double[] specUncertainty = new double[specShape1];

      for (int i = 0; i < specShape1; i++) {
        specUncertainty[i] =
            Math.sqrt(
                Math.pow(specCount2[i] * specUncertainty1[i], 2)
                    + Math.pow(specCount1[i] * specUncertainty2[i], 2));
        specCount[i] = specCount1[i] * specCount2[i];
      }

      String specName = spectrum1.getSpecName() + "_*_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.OneDim(specShape1, specCount, specUncertainty);

      return new Spectrum.OneDim(specName, new Date(), specChannel);
    }

    /** divide the first spectrum by the second spectrum channel by channel */
    public static Spectrum.OneDim divide(Spectrum.OneDim spectrum1, Spectrum.OneDim spectrum2)
        throws SpecChannel.ShapeException, SpectrumException {
      SpecChannel.OneDim specChannel;

      SpecChannel.OneDim specChannel1 = spectrum1.getSpecChannel();
      int specShape1 = specChannel1.getSpecShape();

      SpecChannel.OneDim specChannel2 = spectrum2.getSpecChannel();
      int specShape2 = specChannel2.getSpecShape();

      // throw exception if not the same shape
      if (specShape1 != specShape2)
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[] specCount1 = specChannel1.getSpecCount();
      double[] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[] specCount2 = specChannel2.getSpecCount();
      double[] specUncertainty2 = specChannel2.getSpecUncertainty();

      int[] specCount = new int[specShape1];
      double[] specUncertainty = new double[specShape1];

      for (int i = 0; i < specShape1; i++) {
        specUncertainty[i] =
            (specCount1[i] / specCount2[i])
                * Math.sqrt(
                    (specUncertainty1[i] * specUncertainty1[i]) / (specCount1[i] * specCount1[i])
                        + (specUncertainty2[i] * specUncertainty2[i])
                            / (specCount2[i] * specCount2[i]));
        specCount[i] = specCount[i] / specCount2[i];
      }

      String specName = spectrum1.getSpecName() + "_/_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.OneDim(specShape1, specCount, specUncertainty);

      return new Spectrum.OneDim(specName, new Date(), specChannel);
    }
  }

  /* Arrays are rectangular of y indices with x sub-indices {y,x}
  * Each element of the array is an array of X values for a particular Y value
  This is due to the way the initial program was written
  */
  public static class TwoDim extends Spectrum {

    private SpecChannel.TwoDim specChannel;

    // Default Constructor
    public TwoDim() {
      super("", 2, new Date());
      specChannel = new SpecChannel.TwoDim();
    }

    // Constructor with arguments
    public TwoDim(String aspecName, Date aspecDate, SpecChannel.TwoDim aSpecChannel) {
      super(aspecName, 2, aspecDate);

      specChannel = aSpecChannel;
    }

    // read the file and call the constructor below to assign instance variables
    public TwoDim(File aspecFile) throws SpecFormat.IOException {
      super(aspecFile);
    }

    // Copy Constructor
    public TwoDim(TwoDim aTwoDim) {
      this(aTwoDim.specName, aTwoDim.specDate, aTwoDim.specChannel);
    }

    public String oneLineSummary() {
      StringBuffer sb = new StringBuffer();
      sb.append(super.oneLineSummary());
      // sb.append (" : ");
      // sb.append ( specChannel.toString() );
      return sb.toString();
    }

    public String details() {
      StringBuffer sb = new StringBuffer();
      sb.append(super.details());
      sb.append("\nInformation on Spectrum Channels:\n");
      sb.append(specChannel.toString());
      return sb.toString();
    }

    public boolean equals(TwoDim aTwoDim) {
      if (aTwoDim == null) return false;
      else if (!(specName.equals(aTwoDim.specName))) return false;
      else if (specDimension != aTwoDim.specDimension) return false;
      else if (!(specDate.equals(aTwoDim.specDate))) return false;

      return true;
    }

    public SpecChannel.TwoDim getSpecChannel() {
      return (specChannel);
    }

    protected void setSpecChannel(SpecChannel.TwoDim aSpecChannel) {
      specChannel = aSpecChannel;
    }

    /** return the maximum spectrum channel count */
    public int getMaxCount() {
      return (specChannel.getMaxCount());
    }

    /*
     *************************************************************************
     * INSTANCE METHODS FOR MANIPULATIONS BETWEEN TWO TWO-DIMENSIONAL SPECTRA
     *************************************************************************
     */

    /** add this spectrum to the parsed spectrum channel by channel */
    public void add(Spectrum.TwoDim spectrum2) throws SpecChannel.ShapeException {
      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      int specShape0 = specChannel.getSpecShape0();
      int specShape1 = specChannel.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape0 == specShape02) && (specShape1 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount = specChannel.getSpecCount();
      double[][] specUncertainty = specChannel.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      for (int i = 0; i < specShape02; i++) {
        for (int j = 0; j < specShape12; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  (specUncertainty[i][j] * specUncertainty[i][j])
                      + (specUncertainty2[i][j] * specUncertainty2[i][j]));
          specCount[i][j] += specCount2[i][j];
        }
      }
    }

    /** subtract this spectrum to the parsed spectrum channel by channel */
    public void subtract(Spectrum.TwoDim spectrum2) throws SpecChannel.ShapeException {
      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      int specShape0 = specChannel.getSpecShape0();
      int specShape1 = specChannel.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape0 == specShape02) && (specShape1 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount = specChannel.getSpecCount();
      double[][] specUncertainty = specChannel.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      for (int i = 0; i < specShape02; i++) {
        for (int j = 0; j < specShape12; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  (specUncertainty[i][j] * specUncertainty[i][j])
                      + (specUncertainty2[i][j] * specUncertainty2[i][j]));
          specCount[i][j] -= specCount2[i][j];
        }
      }
    }

    /** multiply this spectrum by the parsed spectrum channel by channel */
    public void multiply(Spectrum.TwoDim spectrum2) throws SpecChannel.ShapeException {
      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      int specShape0 = specChannel.getSpecShape0();
      int specShape1 = specChannel.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape0 == specShape02) && (specShape1 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount = specChannel.getSpecCount();
      double[][] specUncertainty = specChannel.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      for (int i = 0; i < specShape02; i++) {
        for (int j = 0; j < specShape12; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  Math.pow(specCount2[i][j] * specUncertainty[i][j], 2)
                      + Math.pow(specCount[i][j] * specUncertainty2[i][j], 2));
          specCount[i][j] *= specCount2[i][j];
        }
      }
    }

    /** divide this spectrum by the parsed spectrum channel by channel */
    public void divide(Spectrum.TwoDim spectrum2) throws SpecChannel.ShapeException {
      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      int specShape0 = specChannel.getSpecShape0();
      int specShape1 = specChannel.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape0 == specShape02) && (specShape1 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount = specChannel.getSpecCount();
      double[][] specUncertainty = specChannel.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      for (int i = 0; i < specShape02; i++) {
        for (int j = 0; j < specShape12; j++) {
          specUncertainty[i][j] =
              (specCount[i][j] / specCount2[i][j])
                  * Math.sqrt(
                      (specUncertainty[i][j] * specUncertainty[i][j])
                              / (specCount[i][j] * specCount[i][j])
                          + (specUncertainty2[i][j] * specUncertainty2[i][j])
                              / (specCount2[i][j] * specCount2[i][j]));
          specCount[i][j] /= specCount2[i][j];
        }
      }
    }

    /**
     * compress a spectrum by a power of two on the y and x scale ... e.g compressing a 256 channel
     * by exponent of 3 ... means making it to 256/(2*2*2) = 32 channels
     */
    public void compress(int yExponent, int xExponent) throws Exception {
      int specShape0 = specChannel.getSpecShape0();
      int specShape1 = specChannel.getSpecShape1();

      // maximum allowed compression exponent
      int maxYExponent = (int) Math.round(Math.log((double) specShape0) / Math.log(2.0));
      int maxXExponent = (int) Math.round(Math.log((double) specShape1) / Math.log(2.0));

      if ((yExponent >= maxYExponent) || (xExponent >= maxXExponent))
        throw new Exception(
            "Invalid exponent ... this reduces the number" + "of channels to a negative number");

      int[][] specCount = specChannel.getSpecCount();
      double[][] specUncertainty = specChannel.getSpecUncertainty();

      // get the number in Y & X direction that will be grouped together
      int numYInGroup = (int) Math.pow(2.0, (double) yExponent);
      int numXInGroup = (int) Math.pow(2.0, (double) xExponent);

      // get the new specShapes
      int newSpecShape0 = specShape0 / numYInGroup;
      int newSpecShape1 = specShape1 / numXInGroup;
      // calculate the new spec counts and uncertainties
      int[][] newSpecCount = new int[newSpecShape0][newSpecShape1];
      double[][] newSpecUncertainty = new double[newSpecShape0][newSpecShape1];
      // holds the count and uncertainty sum before the cound is set
      int countSum = 0;
      double uncertaintySum = 0.0;

      // loop through to set the newSpecCount for each channel in the new SpecChannel
      for (int i = 0; i < newSpecShape0; i++) {
        for (int j = 0; j < newSpecShape1; j++) {
          // get the average over the rectangular region that is made into one channel
          countSum = 0;
          for (int a = i * numYInGroup; a < (i + 1) * numYInGroup; a++) {
            for (int b = j * numXInGroup; b < (j + 1) * numXInGroup; b++) {
              countSum = countSum + specCount[a][b];
              uncertaintySum = uncertaintySum + (specUncertainty[a][b] * specUncertainty[a][b]);
            }
          }
          // newSpecCount is the total area (sum of counts) over that rectangular region
          newSpecCount[i][j] = countSum;
          newSpecUncertainty[i][j] = Math.sqrt(uncertaintySum);
        }
      }

      try {
        specChannel.setSpecChannelAttributes(
            newSpecShape0, newSpecShape1, newSpecCount, newSpecUncertainty);
      } catch (Exception e) {
        System.out.println("Error in compressing the spectrum\n" + e.getMessage());
      }
    }

    /** multiply the count for each channel by a double value */
    public void multiply(double value) {
      int specShape0 = specChannel.getSpecShape0();
      int specShape1 = specChannel.getSpecShape1();

      int[][] specCount = specChannel.getSpecCount();
      double[][] specUncertainty = specChannel.getSpecUncertainty();

      for (int i = 0; i < specShape0; i++) {
        for (int j = 0; j < specShape1; j++) {
          specCount[i][j] = (int) (specCount[i][j] * value);
          specUncertainty[i][j] = specUncertainty[i][j] * value;
        }
      }
    }

    /** add an int value to the count for each channel */
    public void add(int value) {
      int specShape0 = specChannel.getSpecShape0();
      int specShape1 = specChannel.getSpecShape1();

      int[][] specCount = specChannel.getSpecCount();

      for (int i = 0; i < specShape0; i++) {
        for (int j = 0; j < specShape1; j++) {
          specCount[i][j] += value;
        }
      }
    }

    /*
     *************************************************************************
     * STATIC CLASS METHODS FOR MANIPULATIONS BETWEEN TWO TWO-DIMENSIONAL SPECTRA
     *************************************************************************
     */

    /** add the first spectrum to the second spectrum channel by channel */
    public static Spectrum.TwoDim add(Spectrum.TwoDim spectrum1, Spectrum.TwoDim spectrum2)
        throws SpecChannel.ShapeException, SpectrumException {
      SpecChannel.TwoDim specChannel1 = spectrum1.getSpecChannel();
      int specShape01 = specChannel1.getSpecShape0();
      int specShape11 = specChannel1.getSpecShape1();

      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape01 == specShape02) && (specShape11 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount1 = specChannel1.getSpecCount();
      double[][] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      SpecChannel.TwoDim specChannel;
      int[][] specCount = new int[specShape01][specShape11];
      double[][] specUncertainty = new double[specShape01][specShape11];

      for (int i = 0; i < specShape01; i++) {
        for (int j = 0; j < specShape11; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  (specUncertainty1[i][j] * specUncertainty1[i][j])
                      + (specUncertainty2[i][j] * specUncertainty2[i][j]));
          specCount[i][j] = specCount1[i][j] + specCount2[i][j];
        }
      }

      String specName = spectrum1.getSpecName() + "_+_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.TwoDim(specShape01, specShape11, specCount, specUncertainty);

      return new Spectrum.TwoDim(specName, new Date(), specChannel);
    }

    /** subtract the first spectrum from the second spectrum channel by channel */
    public static Spectrum.TwoDim subtract(Spectrum.TwoDim spectrum1, Spectrum.TwoDim spectrum2)
        throws SpecChannel.ShapeException, SpectrumException {
      SpecChannel.TwoDim specChannel1 = spectrum1.getSpecChannel();
      int specShape01 = specChannel1.getSpecShape0();
      int specShape11 = specChannel1.getSpecShape1();

      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape01 == specShape02) && (specShape11 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount1 = specChannel1.getSpecCount();
      double[][] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      SpecChannel.TwoDim specChannel;
      int[][] specCount = new int[specShape01][specShape11];
      double[][] specUncertainty = new double[specShape01][specShape11];

      for (int i = 0; i < specShape01; i++) {
        for (int j = 0; j < specShape11; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  (specUncertainty1[i][j] * specUncertainty1[i][j])
                      + (specUncertainty2[i][j] * specUncertainty2[i][j]));
          specCount[i][j] = specCount1[i][j] - specCount2[i][j];
        }
      }

      String specName = spectrum1.getSpecName() + "_-_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.TwoDim(specShape01, specShape11, specCount, specUncertainty);

      return new Spectrum.TwoDim(specName, new Date(), specChannel);
    }

    /** multiply the first spectrum by the second spectrum channel by channel */
    public static Spectrum.TwoDim multiply(Spectrum.TwoDim spectrum1, Spectrum.TwoDim spectrum2)
        throws SpecChannel.ShapeException, SpectrumException {
      SpecChannel.TwoDim specChannel1 = spectrum1.getSpecChannel();
      int specShape01 = specChannel1.getSpecShape0();
      int specShape11 = specChannel1.getSpecShape1();

      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape01 == specShape02) && (specShape11 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount1 = specChannel1.getSpecCount();
      double[][] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      SpecChannel.TwoDim specChannel;
      int[][] specCount = new int[specShape01][specShape11];
      double[][] specUncertainty = new double[specShape01][specShape11];

      for (int i = 0; i < specShape01; i++) {
        for (int j = 0; j < specShape11; j++) {
          specUncertainty[i][j] =
              Math.sqrt(
                  Math.pow(specCount2[i][j] * specUncertainty1[i][j], 2)
                      + Math.pow(specCount1[i][j] * specUncertainty2[i][j], 2));
          specCount[i][j] = specCount1[i][j] * specCount2[i][j];
        }
      }

      String specName = spectrum1.getSpecName() + "_*_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.TwoDim(specShape01, specShape11, specCount, specUncertainty);

      return new Spectrum.TwoDim(specName, new Date(), specChannel);
    }

    /** divide the first spectrum by the second spectrum channel by channel */
    public static Spectrum.TwoDim divide(Spectrum.TwoDim spectrum1, Spectrum.TwoDim spectrum2)
        throws SpecChannel.ShapeException, SpectrumException {
      SpecChannel.TwoDim specChannel1 = spectrum1.getSpecChannel();
      int specShape01 = specChannel1.getSpecShape0();
      int specShape11 = specChannel1.getSpecShape1();

      SpecChannel.TwoDim specChannel2 = spectrum2.getSpecChannel();
      int specShape02 = specChannel2.getSpecShape0();
      int specShape12 = specChannel2.getSpecShape1();

      // throw exception if not the same shape
      if (!((specShape01 == specShape02) && (specShape11 == specShape12)))
        throw new SpecChannel.ShapeException("Spectrum must have the same shape ... ");

      int[][] specCount1 = specChannel1.getSpecCount();
      double[][] specUncertainty1 = specChannel1.getSpecUncertainty();

      int[][] specCount2 = specChannel2.getSpecCount();
      double[][] specUncertainty2 = specChannel2.getSpecUncertainty();

      SpecChannel.TwoDim specChannel;
      int[][] specCount = new int[specShape01][specShape11];
      double[][] specUncertainty = new double[specShape01][specShape11];

      for (int i = 0; i < specShape01; i++) {
        for (int j = 0; j < specShape11; j++) {
          specUncertainty[i][j] =
              (specCount1[i][j] / specCount2[i][j])
                  * Math.sqrt(
                      (specUncertainty1[i][j] * specUncertainty1[i][j])
                              / (specCount1[i][j] * specCount1[i][j])
                          + (specUncertainty2[i][j] * specUncertainty2[i][j])
                              / (specCount2[i][j] * specCount2[i][j]));
          specCount[i][j] = specCount1[i][j] + specCount2[i][j];
        }
      }

      String specName = spectrum1.getSpecName() + "_/_" + spectrum2.getSpecName();
      specChannel = new SpecChannel.TwoDim(specShape01, specShape11, specCount, specUncertainty);

      return new Spectrum.TwoDim(specName, new Date(), specChannel);
    }
  }
}
