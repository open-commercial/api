package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.dto.FacturaVentaDTO;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaFacturaVentaDTO {

  private FacturaVentaDTO facturaVenta;
  private Long[] idsFormaDePago;
  private BigDecimal[] montos;
  private int[] indices;
  private Long idPedido;
}
