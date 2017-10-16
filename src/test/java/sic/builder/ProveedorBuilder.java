package sic.builder;

import sic.modelo.CondicionIVA;
import sic.modelo.Empresa;
import sic.modelo.Localidad;
import sic.modelo.Proveedor;

public class ProveedorBuilder {

    private long id_Proveedor = 0L;
    private String codigo = "ABC123";
    private String razonSocial = "Chamaco S.R.L.";
    private String direccion = "La Rioja 2047";
    private CondicionIVA condicionIVA = new CondicionIVABuilder().build();
    private String idFiscal = "23127895679";
    private String telPrimario = "379 4356778";
    private String telSecundario = "379 4894514";
    private String contacto = "Raul Gamez";
    private String email = "chamacosrl@gmail.com";
    private String web = "www.chamacosrl.com.ar";
    private Localidad localidad = new LocalidadBuilder().build();
    private Empresa empresa = new EmpresaBuilder().build();
    private boolean eliminado = false;
    
    public Proveedor build() {
        return new Proveedor(id_Proveedor, codigo, razonSocial, direccion, condicionIVA, idFiscal,
                telPrimario, telSecundario, contacto, email, web, localidad, empresa, eliminado);
    }
    
    public ProveedorBuilder withId_Proveedor(long id_Proveedor) {
        this.id_Proveedor = id_Proveedor;
        return this;
    }
    
    public ProveedorBuilder withCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }
    
    public ProveedorBuilder withRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
        return this;
    }
    
    public ProveedorBuilder withDireccion(String direccion) {
        this.direccion = direccion;
        return this;
    }
    
    public ProveedorBuilder withCondicionIVA(CondicionIVA condicionIVA) {
        this.condicionIVA = condicionIVA;
        return this;
    }
    
    public ProveedorBuilder withIdFiscal(String idFiscal) {
        this.idFiscal = idFiscal;
        return this;
    }
    
    public ProveedorBuilder withTelPrimario(String telPrimario) {
        this.telPrimario = telPrimario;
        return this;
    }
    
    public ProveedorBuilder withTelSecundario(String telSecundario) {
        this.telSecundario = telSecundario;
        return this;
    }
    
    public ProveedorBuilder withContacto(String contacto) {
        this.contacto = contacto;
        return this;
    }
    
    public ProveedorBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public ProveedorBuilder withWeb(String web) {
        this.web = web;
        return this;
    }
    
    public ProveedorBuilder withLocalidad(Localidad localidad) {
        this.localidad = localidad;
        return this;
    }
    
    public ProveedorBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }
    
    public ProveedorBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
}
