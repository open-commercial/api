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
public class ProductosParaVerificarStockDTO {

  private Long idSucursal;
  private Long idPedido;
  private long[] idProducto;
  private BigDecimal[] cantidad;
}
