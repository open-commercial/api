package sic.builder;

import sic.modelo.Rubro;

public class RubroBuilder {
    
    private long idRubro = 0L;
    private String nombre = "Ferreteria";
    private boolean eliminado = false;
    
    public Rubro build() {
        return new Rubro(idRubro, nombre, eliminado);
    }
    
    public RubroBuilder withIdRubro(long idRubro) {
        this.idRubro = idRubro;
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
