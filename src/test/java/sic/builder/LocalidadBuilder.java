package sic.builder;

import sic.modelo.Localidad;
import sic.modelo.Provincia;

import java.math.BigDecimal;

public class LocalidadBuilder {

  private long id_Localidad = 0L;
  private String nombre = "Goya";
  private String codigoPostal = "3450";
  private Provincia provincia = new ProvinciaBuilder().build();
  private boolean envioGratuito = false;
  private BigDecimal costoEnvio = BigDecimal.ZERO;
  private boolean eliminada = false;

  public Localidad build() {
    return new Localidad(id_Localidad, nombre, codigoPostal, provincia, envioGratuito, costoEnvio, eliminada);
  }

  public LocalidadBuilder withId_Localidad(long id_Localidad) {
    this.id_Localidad = id_Localidad;
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

  public LocalidadBuilder withEliminada(boolean eliminada) {
    this.eliminada = eliminada;
    return this;
  }
}
