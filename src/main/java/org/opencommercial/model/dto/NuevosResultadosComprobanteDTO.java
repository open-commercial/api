package org.opencommercial.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.TipoDeComprobante;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevosResultadosComprobanteDTO {

  private BigDecimal[] importe;
  private BigDecimal[] ivaPorcentajes;
  private BigDecimal[] ivaNetos;
  private BigDecimal[] cantidades;
  private TipoDeComprobante tipoDeComprobante;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal recargoPorcentaje;
}
