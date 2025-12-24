package org.opencommercial.integration.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(
        exclude = {
                "certificadoAfip",
                "existeCertificado",
                "passwordCertificadoAfip"
        })
public class ConfiguracionSucursalTest {

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
  private boolean predeterminada;
  private boolean comparteStock;
}
