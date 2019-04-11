package sic.builder;

import sic.modelo.Localidad;
import sic.modelo.Provincia;

import java.math.BigDecimal;

public class LocalidadBuilder {

  private long idLocalidad = 0L;
  private String nombre = "Goya";
  private String codigoPostal = "3450";
  private Provincia provincia = new ProvinciaBuilder().build();
  private boolean envioGratuito = false;
  private BigDecimal costoEnvio = BigDecimal.ZERO;

  public Localidad build() {
    return new Localidad(idLocalidad, nombre, codigoPostal, provincia, envioGratuito, costoEnvio);
  }

  public LocalidadBuilder withIdLocalidad(long idLocalidad) {
    this.idLocalidad = idLocalidad;
    return this;
  }

  public LocalidadBuilder withNombre(String nombre) {
    this.nombre = nombre;
    return this;
  }

  public LocalidadBuilder withCodigoPostal(String codigoPostal) {
    this.codigoPostal = codigoPostal;
    return this;
  }

  public LocalidadBuilder withProvincia(Provincia provincia) {
    this.provincia = provincia;
    return this;
  }

  public LocalidadBuilder withenvioGratuito(boolean envioGratuito) {
    this.envioGratuito = envioGratuito;
    return this;
  }

  public LocalidadBuilder withCostoEnvio(BigDecimal costoEnvio) {
    this.costoEnvio = costoEnvio;
    return this;
  }
}
