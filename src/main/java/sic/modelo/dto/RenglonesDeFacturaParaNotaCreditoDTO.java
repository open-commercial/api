package sic.modelo.dto;


import lombok.*;
import sic.modelo.TipoDeComprobante;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RenglonesDeFacturaParaNotaCreditoDTO {

  Map<Long, BigDecimal> idsYCantidades;
  TipoDeComprobante tipoDeComprobante;
}
