package sic.builder;

import java.util.Date;

import sic.modelo.CategoriaIVA;
import sic.modelo.Empresa;
import sic.modelo.Localidad;
import sic.modelo.Ubicacion;

public class EmpresaBuilder {

  private long id_Empresa = 0L;
  private String nombre = "Globo Corporation";
  private String lema = "Enjoy the life";
  private String direccion = "Viamonte 542";
  private CategoriaIVA categoriaIVA = CategoriaIVA.RESPONSABLE_INSCRIPTO;
  private Long idFiscal = 23154587589L;
  private Long ingresosBrutos = 123456789L;
  private Date fechaInicioActividad = new Date(539924400000L); // 10-02-1987
  private String email = "support@globocorporation.com";
  private String telefono = "379 4895549";
  private Ubicacion ubicacion = new UbicacionBuilder().build();
  private String logo = "";
  private boolean eliminada = false;

  public Empresa build() {
    return new Empresa(
        id_Empresa,
        nombre,
        lema,
        direccion,
        categoriaIVA,
        idFiscal,
        ingresosBrutos,
        fechaInicioActividad,
        email,
        telefono,
        ubicacion,
        logo,
        eliminada);
  }

  public EmpresaBuilder withId_Empresa(long id_Empresa) {
    this.id_Empresa = id_Empresa;
    return this;
  }

  public EmpresaBuilder withNombre(String nombre) {
    this.nombre = nombre;
    return this;
  }

  public EmpresaBuilder withLema(String lema) {
    this.lema = lema;
    return this;
  }

  public EmpresaBuilder withDireccion(String direccion) {
    this.direccion = direccion;
    return this;
  }

  public EmpresaBuilder withCondicionIVA(CategoriaIVA categoriaIVA) {
    this.categoriaIVA = categoriaIVA;
    return this;
  }

  public EmpresaBuilder withIdFiscal(Long idFiscal) {
    this.idFiscal = idFiscal;
    return this;
  }

  public EmpresaBuilder withIngresosBrutos(Long ingresosBrutos) {
    this.ingresosBrutos = ingresosBrutos;
    return this;
  }

  public EmpresaBuilder withFechaInicioActividad(Date fechaInicioActividad) {
    this.fechaInicioActividad = fechaInicioActividad;
    return this;
  }

  public EmpresaBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public EmpresaBuilder withTelefono(String telefono) {
    this.telefono = telefono;
    return this;
  }

  public EmpresaBuilder withUbicacion(Ubicacion ubicacion) {
    this.ubicacion = ubicacion;
    return this;
  }

  public EmpresaBuilder withLogo(String logo) {
    this.logo = logo;
    return this;
  }

  public EmpresaBuilder withEliminada(boolean eliminada) {
    this.eliminada = eliminada;
    return this;
  }
}
