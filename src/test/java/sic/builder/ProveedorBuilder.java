package sic.builder;

import sic.modelo.*;

public class ProveedorBuilder {

  private long idProveedor = 0L;
  private String codigo = "ABC123";
  private String razonSocial = "Chamaco S.R.L.";
  private CategoriaIVA categoriaIVA = CategoriaIVA.RESPONSABLE_INSCRIPTO;
  private Long idFiscal = 23127895679L;
  private String telPrimario = "379 4356778";
  private String telSecundario = "379 4894514";
  private String contacto = "Raul Gamez";
  private String email = "chamacosrl@gmail.com";
  private String web = "www.chamacosrl.com.ar";
  private Ubicacion ubicacion = new UbicacionBuilder().build();
  private Empresa empresa = new EmpresaBuilder().build();
  private boolean eliminado = false;

  public Proveedor build() {
    return new Proveedor(
        idProveedor,
        codigo,
        razonSocial,
        categoriaIVA,
        idFiscal,
        telPrimario,
        telSecundario,
        contacto,
        email,
        web,
        ubicacion,
        empresa,
        eliminado);
  }

  public ProveedorBuilder withId_Proveedor(long id_Proveedor) {
    this.idProveedor = id_Proveedor;
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

  public ProveedorBuilder withCategoriaIVA(CategoriaIVA categoriaIVA) {
    this.categoriaIVA = categoriaIVA;
    return this;
  }

  public ProveedorBuilder withIdFiscal(Long idFiscal) {
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

  public ProveedorBuilder withUbicacion(Ubicacion ubicacion) {
    this.ubicacion = ubicacion;
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
