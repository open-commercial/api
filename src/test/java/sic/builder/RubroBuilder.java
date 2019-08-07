package sic.builder;

import sic.modelo.Rubro;

public class RubroBuilder {
    
    private long id_Rubro = 0L;
    private String nombre = "Ferreteria";
    private boolean eliminado = false;
    
    public Rubro build() {
        return new Rubro(id_Rubro, nombre, eliminado);
    }
    
    public RubroBuilder withId_Rubro(long id_Rubro) {
        this.id_Rubro = id_Rubro;
        return this;
    }
    
    public RubroBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public RubroBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
}
