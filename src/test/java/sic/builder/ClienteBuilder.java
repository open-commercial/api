package sic.builder;

import java.math.BigDecimal;
import java.util.Date;
import sic.modelo.*;

public class ClienteBuilder {

  private long id_Cliente = 0L;
  private BigDecimal bonificacion = BigDecimal.TEN;
  private String nroCliente = "00001";
  private String nombreFiscal = "Construcciones S.A.";
  private String nombreFantasia = "Servimetal";
  private String direccion = "Perugorria 2421";
  private CategoriaIVA categoriaIVA = CategoriaIVA.RESPONSABLE_INSCRIPTO;
  private Long idFiscal = 23248527419L;
  private String email = "servimetal@hotmail.com";
  private String telefono = "3794587114";
  private Ubicacion ubicacion = new UbicacionBuilder().build();
  private String contacto = "Facundo Pastore";
  private Date fechaAlta = new Date(1458010800000L); // 15-03-2016;
  private Empresa empresa = new EmpresaBuilder().build();
  private Usuario viajante = new UsuarioBuilder().build();
  private Usuario credencial = new UsuarioBuilder().build();
  private boolean eliminado = false;
  private boolean predeterminado = false;

  public Cliente build() {
    return new Cliente(
        id_Cliente,
        bonificacion,
        nroCliente,
        nombreFiscal,
        nombreFantasia,
        direccion,
        categoriaIVA,
        idFiscal,
        email,
        telefono,
        ubicacion,
        contacto,
        fechaAlta,
        empresa,
        viajante,
        credencial,
        eliminado,
        predeterminado);
  }

  public ClienteBuilder withId_Cliente(long id_Cliente) {
    this.id_Cliente = id_Cliente;
    return this;
  }

  public ClienteBuilder withBonificacion(BigDecimal bonificacion) {
    this.bonificacion = bonificacion;
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

  public ClienteBuilder withDireccion(String direccion) {
    this.direccion = direccion;
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

  public ClienteBuilder withUbicacion(Ubicacion ubicacion) {
    this.ubicacion = ubicacion;
    return this;
  }

  public ClienteBuilder withContacto(String contacto) {
    this.contacto = contacto;
    return this;
  }

  public ClienteBuilder withFechaAlta(Date fechaAlta) {
    this.fechaAlta = fechaAlta;
    return this;
  }

  public ClienteBuilder withEmpresa(Empresa empresa) {
    this.empresa = empresa;
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
