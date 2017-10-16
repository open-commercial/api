package sic.builder;

import sic.modelo.Empresa;
import sic.modelo.FormaDePago;

public class FormaDePagoBuilder {

    private long id_FormaDePago = 0L;
    private String nombre = "Efectivo";
    private boolean afectaCaja = true;
    private boolean predeterminado = true;
    private Empresa empresa = new EmpresaBuilder().build();
    private boolean eliminada = false;
    
    public FormaDePago build() {
        return new FormaDePago(id_FormaDePago, nombre, afectaCaja, predeterminado, empresa, eliminada);
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
    
    public FormaDePagoBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }
    
    public FormaDePagoBuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }
}
