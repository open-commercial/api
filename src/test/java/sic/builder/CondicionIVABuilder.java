package sic.builder;

import sic.modelo.CondicionIVA;

public class CondicionIVABuilder {

    private long id_CondicionIVA = 0L;
    private String nombre = "Responsable Inscripto";
    private boolean discriminaIVA = true;
    private boolean eliminada = false;
    
    public CondicionIVA build() {
        return new CondicionIVA(id_CondicionIVA, nombre, discriminaIVA, eliminada);
    }
    
    public CondicionIVABuilder withId_CondicionIVA(long id_CondicionIVA) {
        this.id_CondicionIVA = id_CondicionIVA;
        return this;
    }
    
    public CondicionIVABuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public CondicionIVABuilder withDiscriminaIVA(boolean discriminaIVA) {
        this.discriminaIVA = discriminaIVA;
        return this;
    }
    
    public CondicionIVABuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }
}
