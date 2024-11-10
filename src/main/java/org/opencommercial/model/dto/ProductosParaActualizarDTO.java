package org.opencommercial.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductosParaActualizarDTO {

  private Set<Long> idProducto;
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
