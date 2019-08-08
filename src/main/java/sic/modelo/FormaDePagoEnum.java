package sic.modelo;

public enum FormaDePagoEnum {
  CONTRAREEMBOLSO("Contrareembolso"),
  TRANSFERENCIABANCARIA("Transferencia Bancaria"),
  EFECTIVO("Efectivo"),
  DEPOSITOBANCARIO("Deposito Bancario"),
  CHEQUESDE3ROS("Cheques de 3ros"),
  TARJETANARANJA("Tarjeta Naranja"),
  TARJETAVISA("Tarjeta Visa"),
  TARJETAMASTERCARD("Tarjeta Mastercard"),
  TARJETAMAESTRO("Tarjeta Maestro"),
  TARJETACABAL("Tarjeta Cabal"),
  TARJETANEVADA("Tarjeta Nevada"),
  TARJETAVISADEBITO("Tarjeta Visa Debito"),
  CREDICOMPRAS("Credicompras"),
  NATIVA("Nativa"),
  MASTERDEBITO("Master Debito"),
  MERCADOPAGO("Mercado Pago"),
  CHEQUESPROPIOS("Cheques Propios");

  private final String text;

  FormaDePagoEnum(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
