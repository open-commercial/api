package sic.builder;

import sic.modelo.FormaDePago;

public class FormaDePagoBuilder {

    private long id_FormaDePago = 0L;
    private String nombre = "Efectivo";
    private boolean afectaCaja = true;
    private boolean predeterminado = true;
    private boolean eliminada = false;
    private String paymentMethodId = "";
    
    public FormaDePago build() {
        return new FormaDePago(id_FormaDePago, nombre, afectaCaja, predeterminado, eliminada, paymentMethodId);
    }
    
    public FormaDePagoBuilder withId_FormaDePago(long idFormaDePago) {
        this.id_FormaDePago = idFormaDePago;
        return this;
    }
    
    public FormaDePagoBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public FormaDePagoBuilder withAfectaCaja(boolean afecta) {
        this.afectaCaja = afecta;
        return this;
    }
    public FormaDePagoBuilder withPredeterminado(boolean predeterminado) {
        this.predeterminado = predeterminado;
        return this;
    }
    
    public FormaDePagoBuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }

    public FormaDePagoBuilder withPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
        return this;
    }
}
