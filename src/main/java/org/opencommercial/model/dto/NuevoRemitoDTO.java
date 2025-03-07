package org.opencommercial.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.TipoBulto;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoRemitoDTO {

  private long[] idFacturaVenta;
  private long idTransportista;
  private TipoBulto[] tiposDeBulto;
  private BigDecimal[] cantidadPorBulto;
  private BigDecimal costoDeEnvio;
  private BigDecimal pesoTotalEnKg;
  private BigDecimal volumenTotalEnM3;
  private String observaciones;
}
