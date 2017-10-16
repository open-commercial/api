package sic.builder;

import sic.modelo.Pais;

public class PaisBuilder {

    private long id_Pais = 0L;
    private String nombre = "Argentina";
    private boolean eliminado = false;

    public Pais build() {
        return new Pais(id_Pais, nombre, eliminado);
    }
    
    public PaisBuilder withId_Pais(long id_Pais) {
        this.id_Pais = id_Pais;
        return this;
    }
    
    public PaisBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public PaisBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
}
