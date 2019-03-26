package sic.modelo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ActualizarProductosDTO {

  private long[] idProducto;
  private BigDecimal descuentoRecargoPorcentaje;
  private Long idMedida;
  private Long idRubro;
  private Long idProveedor;
  private BigDecimal gananciaNeto;
  private BigDecimal gananciaPorcentaje;
  private BigDecimal ivaNeto;
  private BigDecimal ivaPorcentaje;
  private BigDecimal precioCosto;
  private BigDecimal precioLista;
  private BigDecimal precioVentaPublico;
  private Boolean publico;
}
