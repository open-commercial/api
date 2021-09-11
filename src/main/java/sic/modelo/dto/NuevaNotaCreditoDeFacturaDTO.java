package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaNotaCreditoDeFacturaDTO {

  private Long idFactura;
  private BigDecimal[] cantidades;
  private Long[] idsRenglonesFactura;
  private boolean modificaStock;
  private String motivo;
  private DetalleCompraDTO detalleCompra;
}
