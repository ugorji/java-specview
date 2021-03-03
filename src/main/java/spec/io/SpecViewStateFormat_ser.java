package spec.io;

import java.io.Serializable;

public class SpecViewStateFormat_ser extends SpecViewStateFormat implements Cloneable, Serializable
/*
cannot be subclassed from SpecFormat ... because it does IO operations
on the Application, not directly on Spectra
*/
{
  public SpecViewStateFormat_ser() {
    super();
  }

  public boolean read() {
    return false;
  }

  public boolean write() {
    return false;
  }
}
