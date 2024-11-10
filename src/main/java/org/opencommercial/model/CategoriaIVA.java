package org.opencommercial.model;

public enum CategoriaIVA {
  RESPONSABLE_INSCRIPTO,
  EXENTO,
  CONSUMIDOR_FINAL,
  MONOTRIBUTO;

  public static boolean discriminaIVA(CategoriaIVA categoriaIVA) {
    switch (categoriaIVA) {
      case RESPONSABLE_INSCRIPTO:
        return true;
      case EXENTO:
        return false;
      case CONSUMIDOR_FINAL:
        return false;
      case MONOTRIBUTO:
        return false;
      default:
        return false;
    }
  }
}
