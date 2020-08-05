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
  private boolean contraEntrega;
  private TipoBulto[] tiposDeBulto;
  private BigDecimal[] cantidadDeBultos;
}
