package sic.builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import sic.modelo.*;

public class ClienteBuilder {

  private long idCliente = 0L;
  private BigDecimal bonificacion = BigDecimal.TEN;
  private BigDecimal saldo = BigDecimal.ZERO;
  private String nroCliente = "00001";
  private String nombreFiscal = "Construcciones S.A.";
  private String nombreFantasia = "Servimetal";
  private CategoriaIVA categoriaIVA = CategoriaIVA.RESPONSABLE_INSCRIPTO;
  private Long idFiscal = 23248527419L;
  private String email = "servimetal@hotmail.com";
  private String telefono = "3794587114";
  private Ubicacion ubicacionFacturacion = new UbicacionBuilder().build();
  private Ubicacion ubicacionEnvio = new UbicacionBuilder().build();
  private String contacto = "Facundo Pastore";
  private LocalDateTime fechaAlta = LocalDateTime.now();
  private Usuario viajante = new UsuarioBuilder().build();
  private Usuario credencial = new UsuarioBuilder().build();
  private boolean eliminado = false;
  private boolean predeterminado = false;

  public Cliente build() {
    return new Cliente(
      idCliente,
        bonificacion,
        saldo,
        nroCliente,
        nombreFiscal,
        nombreFantasia,
        categoriaIVA,
        idFiscal,
        email,
        telefono,
        ubicacionFacturacion,
        ubicacionEnvio,
        contacto,
        fechaAlta,
        viajante,
        credencial,
        eliminado,
        predeterminado);
  }

  public ClienteBuilder withIdCliente(long idCliente) {
    this.idCliente = idCliente;
    return this;
  }

  public ClienteBuilder withBonificacion(BigDecimal bonificacion) {
    this.bonificacion = bonificacion;
    return this;
  }

  public ClienteBuilder withSaldo(BigDecimal saldo) {
    this.saldo = saldo;
    return this;
  }

  public ClienteBuilder withNroCliente(String nroCliente) {
    this.nroCliente = nroCliente;
    return this;
  }

  public ClienteBuilder withNombreFiscal(String nombreFiscal) {
    this.nombreFiscal = nombreFiscal;
    return this;
  }

  public ClienteBuilder withNombreFantasia(String nombreFantasia) {
    this.nombreFantasia = nombreFantasia;
    return this;
  }

  public ClienteBuilder withCategoriaIVA(CategoriaIVA categoriaIVA) {
    this.categoriaIVA = categoriaIVA;
    return this;
  }

  public ClienteBuilder withIdFiscal(Long idFiscal) {
    this.idFiscal = idFiscal;
    return this;
  }

  public ClienteBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public ClienteBuilder withTelefono(String telefono) {
    this.telefono = telefono;
    return this;
  }

  public ClienteBuilder withUbicacionFacturacion(Ubicacion ubicacionFacturacion) {
    this.ubicacionFacturacion = ubicacionFacturacion;
    return this;
  }

  public ClienteBuilder withUbicacionEnvio(Ubicacion ubicacionEnvio) {
    this.ubicacionEnvio = ubicacionEnvio;
    return this;
  }

  public ClienteBuilder withContacto(String contacto) {
    this.contacto = contacto;
    return this;
  }

  public ClienteBuilder withFechaAlta(LocalDateTime fechaAlta) {
    this.fechaAlta = fechaAlta;
    return this;
  }

  public ClienteBuilder withViajante(Usuario viajante) {
    this.viajante = viajante;
    return this;
  }

  public ClienteBuilder withCredencial(Usuario credencial) {
    this.credencial = credencial;
    return this;
  }

  public ClienteBuilder withEliminado(boolean eliminado) {
    this.eliminado = eliminado;
    return this;
  }

  public ClienteBuilder withPredeterminado(boolean predeterminado) {
    this.predeterminado = predeterminado;
    return this;
  }
}
