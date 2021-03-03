package spec.io;

import java.io.Serializable;

public abstract class SpecViewStateFormat implements Cloneable, Serializable {
  /*
  maybe make this produce a SpecApplication Object. This object encompasses the states of all open Spectra, state of the SpecGraph, etc.
  */
  public abstract boolean read();

  public abstract boolean write();
}
