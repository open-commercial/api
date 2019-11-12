package sic.builder;

import sic.modelo.Empresa;
import sic.modelo.Rubro;

public class RubroBuilder {
    
    private long idRubro = 0L;
    private String nombre = "Ferreteria";
    private Empresa empresa = new EmpresaBuilder().build();
    private boolean eliminado = false;
    
    public Rubro build() {
        return new Rubro(idRubro, nombre, empresa, eliminado);
    }
    
    public RubroBuilder withIdRubro(long idRubro) {
        this.idRubro = idRubro;
        return this;
    }
    
    public RubroBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public RubroBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }
    
    public RubroBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
}
