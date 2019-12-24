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
public class ProductosParaActualizarDTO {

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
  private BigDecimal porcentajeBonificacionPrecio;
  private Boolean publico;
}
