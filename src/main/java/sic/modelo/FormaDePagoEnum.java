package sic.modelo;

public enum FormaDePagoEnum {
  CONTRAREEMBOLSO("Contrareembolso"),
  TRANSFERENCIA_BANCARIA("Transferencia Bancaria"),
  EFECTIVO("Efectivo"),
  DEPOSITO_BANCARIO("Deposito Bancario"),
  CHEQUES_DE_3ROS("Cheques de 3ros"),
  TARJETA_NARANJA("Tarjeta Naranja"),
  TARJETA_VISA("Tarjeta Visa"),
  TARJETA_MASTERCARD("Tarjeta Mastercard"),
  TARJETA_MAESTRO("Tarjeta Maestro"),
  TARJETA_CABAL("Tarjeta Cabal"),
  TARJETA_NEVADA("Tarjeta Nevada"),
  TARJETA_VISADEBITO("Tarjeta Visa Debito"),
  CREDICOMPRAS("Credicompras"),
  NATIVA("Nativa"),
  MASTER_DEBITO("Master Debito"),
  MERCADO_PAGO("Mercado Pago"),
  CHEQUES_PROPIOS("Cheques Propios");

  private final String text;

  FormaDePagoEnum(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
