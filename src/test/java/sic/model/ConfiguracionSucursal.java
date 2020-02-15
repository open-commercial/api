package sic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfiguracionSucursal {

  private long idConfiguracionSucursal;

  private boolean usarFacturaVentaPreImpresa;

  private int cantidadMaximaDeRenglonesEnFactura;

  private boolean facturaElectronicaHabilitada;

  private byte[] certificadoAfip;

  private boolean existeCertificado;

  private String firmanteCertificadoAfip;

  private String passwordCertificadoAfip;

  private int nroPuntoDeVentaAfip;

  private boolean puntoDeRetiro;

  private long idSucursal;

  private String nombreSucursal;
}
