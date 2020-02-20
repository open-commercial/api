package sic.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"certificadoAfip", "existeCertificado", "passwordCertificadoAfip"})
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
