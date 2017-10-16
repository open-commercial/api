package sic.builder;

import sic.modelo.Empresa;
import sic.modelo.Medida;

public class MedidaBuilder {

    private long id_Medida = 0L;
    private String nombre = "Metro";
    private Empresa empresa = new EmpresaBuilder().build();
    private boolean eliminada = false;
    
    public Medida build() {
        return new Medida(id_Medida, nombre, empresa, eliminada);
    }
    
    public MedidaBuilder withId_Medida(long id_Medida) {
        this.id_Medida = id_Medida;
        return this;
    }
    
    public MedidaBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public MedidaBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }
    
    public MedidaBuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }
}
