package sic.builder;

import sic.modelo.Empresa;
import sic.modelo.Localidad;
import sic.modelo.Transportista;
import sic.modelo.Ubicacion;

public class TransportistaBuilder {

  private long id_Transportista = 0L;
  private String nombre = "Correo OCA";
  private Ubicacion ubicacion = new UbicacionBuilder().build();
  private String web = "pedidos@oca.com.ar";
  private String telefono = "379 5402356";
  private Empresa empresa = new EmpresaBuilder().build();
  private boolean eliminado = false;

  public Transportista build() {
    return new Transportista(
        id_Transportista, nombre, ubicacion, web, telefono, empresa, eliminado);
  }

  public TransportistaBuilder withId_Transportista(long idTransportista) {
    this.id_Transportista = idTransportista;
    return this;
  }

  public TransportistaBuilder withNombre(String nombre) {
    this.nombre = nombre;
    return this;
  }

  public TransportistaBuilder withUbicacion(Ubicacion ubicacion) {
    this.ubicacion = ubicacion;
    return this;
  }

  public TransportistaBuilder withWeb(String web) {
    this.web = web;
    return this;
  }

  public TransportistaBuilder withTelefono(String telefono) {
    this.telefono = telefono;
    return this;
  }

  public TransportistaBuilder withEmpresa(Empresa empresa) {
    this.empresa = empresa;
    return this;
  }

  public TransportistaBuilder withEliminado(boolean eliminado) {
    this.eliminado = eliminado;
    return this;
  }
}
