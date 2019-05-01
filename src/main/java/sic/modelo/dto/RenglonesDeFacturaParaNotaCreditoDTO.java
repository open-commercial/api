package sic.modelo.dto;


import lombok.*;
import sic.modelo.TipoDeComprobante;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RenglonesDeFacturaParaNotaCreditoDTO {

  private Long[] idsRenglonesFactura;
  private BigDecimal[] cantidades;
  TipoDeComprobante tipoDeComprobante;
}
