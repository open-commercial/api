package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.TipoDeComprobante;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaNotaDebitoDeReciboDTO {

  private long idRecibo;
  private BigDecimal gastoAdministrativo;
  private String motivo;
  private Long serie;
  private Long nroNota;
  private Long CAE;
  private TipoDeComprobante tipoDeComprobante;

}
