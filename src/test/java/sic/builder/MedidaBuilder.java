package sic.builder;

import sic.modelo.Medida;

public class MedidaBuilder {

    private long idMedida = 0L;
    private String nombre = "Metro";
    private boolean eliminada = false;
    
    public Medida build() {
        return new Medida(idMedida, nombre, eliminada);
    }
    
    public MedidaBuilder withIdMedida(long idMedida) {
        this.idMedida = idMedida;
        return this;
    }
    
    public MedidaBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public MedidaBuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }
}
