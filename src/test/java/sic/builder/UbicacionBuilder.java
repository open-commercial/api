package sic.builder;

import sic.modelo.Localidad;
import sic.modelo.Ubicacion;

public class UbicacionBuilder {

  private long idUbicacion = 0L;
  private Localidad localidad = new LocalidadBuilder().build();
  private String descripcion = "Rejas Verdes, el perro ladra mucho";
  private Double latitud = -27.468805;
  private Double longitud = -58.834360;
  private String calle = "Pedernera";
  private Integer numero = 4585;
  private String piso = "local 4";
  private String departamento = "no posee";

  public Ubicacion build() {
    return new Ubicacion(
        idUbicacion,
        localidad,
        descripcion,
        latitud,
        longitud,
        calle,
        numero,
        piso,
        departamento);
  }

  public UbicacionBuilder withIdUbicacion(long idUbicacion) {
    this.idUbicacion = idUbicacion;
    return this;
  }

  public UbicacionBuilder withLocalidad(Localidad localidad) {
    this.localidad = localidad;
    return this;
  }

  public UbicacionBuilder withDescripcion(String descripcion) {
    this.descripcion = descripcion;
    return this;
  }

  public UbicacionBuilder withLatitud(Double latitud) {
    this.latitud = latitud;
    return this;
  }

  public UbicacionBuilder withLongitud(Double longitud) {
    this.longitud = longitud;
    return this;
  }

  public UbicacionBuilder withCalle(String calle) {
    this.calle = calle;
    return this;
  }

  public UbicacionBuilder withNumero(Integer numero) {
    this.numero = numero;
    return this;
  }

  public UbicacionBuilder withPiso(String piso) {
    this.piso = piso;
    return this;
  }

  public UbicacionBuilder withDepartamento(String codigoPostal) {
    this.piso = piso;
    return this;
  }
}
