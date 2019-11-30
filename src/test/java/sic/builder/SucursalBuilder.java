package sic.builder;


import sic.modelo.CategoriaIVA;
import sic.modelo.Sucursal;
import sic.modelo.Ubicacion;

import java.time.LocalDateTime;
import java.time.Month;

public class SucursalBuilder {

  private long idSucursal = 0L;
  private String nombre = "Globo Corporation";
  private String lema = "Enjoy the life";
  private CategoriaIVA categoriaIVA = CategoriaIVA.RESPONSABLE_INSCRIPTO;
  private Long idFiscal = 23154587589L;
  private Long ingresosBrutos = 123456789L;
  private LocalDateTime fechaInicioActividad = LocalDateTime.of(1987, Month.FEBRUARY, 10, 00, 00);
  private String email = "support@globocorporation.com";
  private String telefono = "379 4895549";
  private Ubicacion ubicacion = new UbicacionBuilder().build();
  private String logo = "";
  private boolean eliminada = false;

  public Sucursal build() {
    return new Sucursal(
        idSucursal,
        nombre,
        lema,
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

  public SucursalBuilder withIdSucursal(long idSucursal) {
    this.idSucursal = idSucursal;
    return this;
  }

  public SucursalBuilder withNombre(String nombre) {
    this.nombre = nombre;
    return this;
  }

  public SucursalBuilder withLema(String lema) {
    this.lema = lema;
    return this;
  }

  public SucursalBuilder withCondicionIVA(CategoriaIVA categoriaIVA) {
    this.categoriaIVA = categoriaIVA;
    return this;
  }

  public SucursalBuilder withIdFiscal(Long idFiscal) {
    this.idFiscal = idFiscal;
    return this;
  }

  public SucursalBuilder withIngresosBrutos(Long ingresosBrutos) {
    this.ingresosBrutos = ingresosBrutos;
    return this;
  }

  public SucursalBuilder withFechaInicioActividad(LocalDateTime fechaInicioActividad) {
    this.fechaInicioActividad = fechaInicioActividad;
    return this;
  }

  public SucursalBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public SucursalBuilder withTelefono(String telefono) {
    this.telefono = telefono;
    return this;
  }

  public SucursalBuilder withUbicacion(Ubicacion ubicacion) {
    this.ubicacion = ubicacion;
    return this;
  }

  public SucursalBuilder withLogo(String logo) {
    this.logo = logo;
    return this;
  }

  public SucursalBuilder withEliminada(boolean eliminada) {
    this.eliminada = eliminada;
    return this;
  }
}
