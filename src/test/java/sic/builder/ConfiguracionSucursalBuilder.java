package sic.builder;

import sic.modelo.ConfiguracionSucursal;
import sic.modelo.Sucursal;
import java.time.LocalDateTime;

public class ConfiguracionSucursalBuilder {

  private long idConfiguracionSucursal = 0L;
  private boolean usarFacturaVentaPreImpresa = true;
  private int cantidadMaximaDeRenglonesEnFactura = 28;
  private boolean facturaElectronicaHabilitada = false;
  private byte[] certificadoAfip = null;
  private String firmanteCertificadoAfip = "test";
  private String passwordCertificadoAfip = "test123";
  private int nroPuntoDeVentaAfip = 1;
  private String tokenWSAA = null;
  private String signTokenWSAA = null;
  private boolean puntoDeRetiro = true;
  private LocalDateTime fechaGeneracionTokenWSAA = null;
  private LocalDateTime fechaVencimientoTokenWSAA = null;
  private Sucursal sucursal = new SucursalBuilder().build();

  public ConfiguracionSucursal build() {
    return new ConfiguracionSucursal(
        idConfiguracionSucursal,
        usarFacturaVentaPreImpresa,
        cantidadMaximaDeRenglonesEnFactura,
        facturaElectronicaHabilitada,
        certificadoAfip,
        firmanteCertificadoAfip,
        passwordCertificadoAfip,
        nroPuntoDeVentaAfip,
        tokenWSAA,
        signTokenWSAA,
        puntoDeRetiro,
        fechaGeneracionTokenWSAA,
        fechaVencimientoTokenWSAA,
        sucursal);
  }

  public ConfiguracionSucursalBuilder withIdConfiguracionSucursal(long idConfiguracionSucursal) {
    this.idConfiguracionSucursal = idConfiguracionSucursal;
    return this;
  }

  public ConfiguracionSucursalBuilder withUsarFacturaVentaPreImpresa(
      boolean usarFacturaPreImpresa) {
    this.usarFacturaVentaPreImpresa = usarFacturaPreImpresa;
    return this;
  }

  public ConfiguracionSucursalBuilder withCantidadMaximaDeRenglonesEnFactura(
      int cantidadMaximaDeRenglonesEnFactura) {
    this.cantidadMaximaDeRenglonesEnFactura = cantidadMaximaDeRenglonesEnFactura;
    return this;
  }

  public ConfiguracionSucursalBuilder withFacturaElectronicaHabilitada(
      boolean facturaElectronicaHabilitada) {
    this.facturaElectronicaHabilitada = facturaElectronicaHabilitada;
    return this;
  }

  public ConfiguracionSucursalBuilder withCertificadoAfip(byte[] certificadoAfip) {
    this.certificadoAfip = certificadoAfip;
    return this;
  }

  public ConfiguracionSucursalBuilder withFirmanteCertificadoAfip(
      String firmanteCertificadoAfip) {
    this.firmanteCertificadoAfip = firmanteCertificadoAfip;
    return this;
  }

  public ConfiguracionSucursalBuilder withPasswordCertificadoAfip(
      String passwordCertificadoAfip) {
    this.passwordCertificadoAfip = passwordCertificadoAfip;
    return this;
  }

  public ConfiguracionSucursalBuilder withNroPuntoDeVentaAfip(int nroPuntoDeVentaAfip) {
    this.nroPuntoDeVentaAfip = nroPuntoDeVentaAfip;
    return this;
  }

  public ConfiguracionSucursalBuilder withTokenWSAA(String tokenWSAA) {
    this.tokenWSAA = tokenWSAA;
    return this;
  }

  public ConfiguracionSucursalBuilder withSignTokenWSAA(String signTokenWSAA) {
    this.signTokenWSAA = signTokenWSAA;
    return this;
  }

  public ConfiguracionSucursalBuilder withPuntoDeRetiro(boolean puntoDeRetiro) {
    this.puntoDeRetiro = puntoDeRetiro;
    return this;
  }

  public ConfiguracionSucursalBuilder withFechaGeneracionTokenWSAA(
    LocalDateTime fechaGeneracionTokenWSAA) {
    this.fechaGeneracionTokenWSAA = fechaGeneracionTokenWSAA;
    return this;
  }

  public ConfiguracionSucursalBuilder withFechaVencimientoTokenWSAA(
    LocalDateTime fechaVencimientoTokenWSAA) {
    this.fechaVencimientoTokenWSAA = fechaVencimientoTokenWSAA;
    return this;
  }

  public ConfiguracionSucursalBuilder withSucursal(Sucursal sucursal) {
    this.sucursal = sucursal;
    return this;
  }
}
