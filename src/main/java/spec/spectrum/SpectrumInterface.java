package spec.spectrum;

import java.util.Date;

/**
 * "Spectrum" Interface ... implemented by spectrum ... Put here so that classes that act as
 * superclasses for others that subclass the Spectrum.OneDim and so on can still implement this
 * interface and be treated as spectra e.g DrawableSpectrum
 */
public interface SpectrumInterface {
  /** Methods from the Spectrum Class */
  public int getSpecDimension();

  public String getSpecName();

  public void setSpecName(String aSpecName);

  public Date getSpecDate();

  public void setSpecDate(Date aspecDate);

  public int getMaxCount();

  public String toString();

  public String oneLineSummary();

  public String details();

  public void editSpectrum();
}
