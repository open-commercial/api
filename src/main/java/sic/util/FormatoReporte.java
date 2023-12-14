package sic.util;

public enum FormatoReporte {
  PDF,
  XLSX;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
