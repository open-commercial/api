package sic.builder;

import sic.modelo.Provincia;

public class ProvinciaBuilder {
    
    private long id_Provincia = 0L;
    private String nombre = "Corrientes";
    private boolean eliminada = false;
    
    public Provincia build() {
        return new Provincia(id_Provincia, nombre, eliminada);
    }
    
    public ProvinciaBuilder withId_Provincia(long id_Provincia) {
        this.id_Provincia = id_Provincia;
        return this;
    }
    
    public ProvinciaBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public ProvinciaBuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }
}
