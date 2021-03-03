package spec.spectrum;

public class SpectrumException extends Exception {
  public SpectrumException(String msg) {
    super(msg);
  }

  public SpectrumException(String msg, Throwable thr) {
    super(msg, thr);
  }
}
