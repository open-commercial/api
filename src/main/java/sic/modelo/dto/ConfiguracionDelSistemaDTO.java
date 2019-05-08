package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfiguracionDelSistemaDTO {

  private long id_ConfiguracionDelSistema;

  private boolean usarFacturaVentaPreImpresa;

  private int cantidadMaximaDeRenglonesEnFactura;

  private boolean facturaElectronicaHabilitada;

  private byte[] certificadoAfip;

  private boolean existeCertificado;

  private String firmanteCertificadoAfip;

  private String passwordCertificadoAfip;

  private int nroPuntoDeVentaAfip;

  private boolean emailSenderHabilitado;

  private String emailUsername;

  private String emailPassword;

  private long idEmpresa;

  private String nombreEmpresa;

}
