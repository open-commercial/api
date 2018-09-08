package sic.builder;

import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import java.util.Date;

public class ConfiguracionDelSistemaBuilder {

  private long id_ConfiguracionDelSistema = 0L;
  private boolean usarFacturaVentaPreImpresa = true;
  private int cantidadMaximaDeRenglonesEnFactura = 28;
  private boolean facturaElectronicaHabilitada = false;
  private byte[] certificadoAfip = null;
  private String firmanteCertificadoAfip = "test";
  private String passwordCertificadoAfip = "test123";
  private int nroPuntoDeVentaAfip = 1;
  private String tokenWSAA = null;
  private String signTokenWSAA = null;
  private boolean emailSenderHabilitado = true;
  private String emailUserName = "test@mail.com";
  private String emailPassword = "password";
  private Date fechaGeneracionTokenWSAA = null;
  private Date fechaVencimientoTokenWSAA = null;
  private Empresa empresa = new EmpresaBuilder().build();

  public ConfiguracionDelSistema build() {
    return new ConfiguracionDelSistema(
        id_ConfiguracionDelSistema,
        usarFacturaVentaPreImpresa,
        cantidadMaximaDeRenglonesEnFactura,
        facturaElectronicaHabilitada,
        certificadoAfip,
        firmanteCertificadoAfip,
        passwordCertificadoAfip,
        nroPuntoDeVentaAfip,
        tokenWSAA,
        signTokenWSAA,
        emailSenderHabilitado,
        emailUserName,
        emailPassword,
        fechaGeneracionTokenWSAA,
        fechaVencimientoTokenWSAA,
        empresa);
  }

  public ConfiguracionDelSistemaBuilder withIdConfiguracionDelSistema(long idCds) {
    this.id_ConfiguracionDelSistema = idCds;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withUsarFacturaVentaPreImpresa(
      boolean usarFacturaPreImpresa) {
    this.usarFacturaVentaPreImpresa = usarFacturaPreImpresa;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withCantidadMaximaDeRenglonesEnFactura(
      int cantidadMaximaDeRenglonesEnFactura) {
    this.cantidadMaximaDeRenglonesEnFactura = cantidadMaximaDeRenglonesEnFactura;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withFacturaElectronicaHabilitada(
      boolean facturaElectronicaHabilitada) {
    this.facturaElectronicaHabilitada = facturaElectronicaHabilitada;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withCertificadoAfip(byte[] certificadoAfip) {
    this.certificadoAfip = certificadoAfip;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withFirmanteCertificadoAfip(
      String firmanteCertificadoAfip) {
    this.firmanteCertificadoAfip = firmanteCertificadoAfip;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withPasswordCertificadoAfip(
      String passwordCertificadoAfip) {
    this.passwordCertificadoAfip = passwordCertificadoAfip;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withNroPuntoDeVentaAfip(int nroPuntoDeVentaAfip) {
    this.nroPuntoDeVentaAfip = nroPuntoDeVentaAfip;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withTokenWSAA(String tokenWSAA) {
    this.tokenWSAA = tokenWSAA;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withSignTokenWSAA(String signTokenWSAA) {
    this.signTokenWSAA = signTokenWSAA;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withEmailSenderHabilitado(boolean emailSenderHabilitado) {
    this.emailSenderHabilitado = emailSenderHabilitado;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withEmailUserName(String emailUserName) {
    this.emailUserName = emailUserName;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withEmailPassword(String emailPassword) {
    this.emailPassword = emailPassword;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withFechaGeneracionTokenWSAA(
      Date fechaGeneracionTokenWSAA) {
    this.fechaGeneracionTokenWSAA = fechaGeneracionTokenWSAA;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withFechaVencimientoTokenWSAA(
      Date fechaVencimientoTokenWSAA) {
    this.fechaVencimientoTokenWSAA = fechaVencimientoTokenWSAA;
    return this;
  }

  public ConfiguracionDelSistemaBuilder withEmpresa(Empresa empresa) {
    this.empresa = empresa;
    return this;
  }
}
