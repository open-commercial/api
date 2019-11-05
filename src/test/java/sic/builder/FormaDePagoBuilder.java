package sic.builder;

import sic.modelo.FormaDePago;

public class FormaDePagoBuilder {

    private long idFormaDePago = 0L;
    private String nombre = "Efectivo";
    private boolean afectaCaja = true;
    private boolean predeterminado = true;
    private boolean eliminada = false;
    
    public FormaDePago build() {
        return new FormaDePago(idFormaDePago, nombre, afectaCaja, predeterminado, eliminada);
    }
    
    public FormaDePagoBuilder withIdFormaDePago(long idFormaDePago) {
        this.idFormaDePago = idFormaDePago;
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
}
