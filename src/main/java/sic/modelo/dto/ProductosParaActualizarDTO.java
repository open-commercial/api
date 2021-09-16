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
  private BigDecimal cantidadVentaMinima;
  private Long idMedida;
  private Long idRubro;
  private Long idProveedor;
  private BigDecimal gananciaPorcentaje;
  private BigDecimal ivaPorcentaje;
  private BigDecimal precioCosto;
  private BigDecimal porcentajeBonificacionPrecio;
  private BigDecimal porcentajeBonificacionOferta;
  private Boolean publico;
  private Boolean paraCatalogo;
}
