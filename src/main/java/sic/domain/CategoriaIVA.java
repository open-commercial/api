package sic.domain;

public enum CategoriaIVA {
  RESPONSABLE_INSCRIPTO,
  EXENTO,
  CONSUMIDOR_FINAL,
  MONOTRIBUTO;

  public static boolean discriminaIVA(CategoriaIVA categoriaIVA) {
    return switch (categoriaIVA) {
      case RESPONSABLE_INSCRIPTO -> true;
      case EXENTO -> false;
      case CONSUMIDOR_FINAL -> false;
      case MONOTRIBUTO -> false;
      default -> false;
    };
  }
}
