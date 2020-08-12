package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.TipoBulto;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoRemitoDTO {

  private long idFacturaVenta;
  private boolean dividir;
  private long idTransportista;
  private TipoBulto[] tiposDeBulto;
  private BigDecimal[] cantidadPorBulto;
  private BigDecimal costoDeEnvio;
  private BigDecimal pesoTotalKg;
  private BigDecimal volumenM3;
  private String observaciones;
}
