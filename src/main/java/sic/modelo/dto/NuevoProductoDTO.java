package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoProductoDTO {

  private String codigo;
  @NotNull(message = "{mensaje_producto_vacio_descripcion}")
  @NotEmpty(message = "{mensaje_producto_vacio_descripcion}")
  private String descripcion;
  @NotEmpty(message = "{mensaje_producto_cantidad_en_sucursales_vacia}")
  private Map<Long,BigDecimal> cantidadEnSucursal;
  private boolean hayStock;
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidadMinima_negativa}")
  private BigDecimal cantMinima;
  @DecimalMin(value = "1", message = "{mensaje_producto_cantidad_venta_minima_invalida}")
  @NotNull(message = "{mensaje_producto_cantidad_venta_minima_invalida}")
  private BigDecimal bulto;
  @DecimalMin(value = "0", message = "{mensaje_producto_precioCosto_negativo}")
  private BigDecimal precioCosto;
  @DecimalMin(value = "0", message = "{mensaje_producto_gananciaPorcentaje_negativo}")
  private BigDecimal gananciaPorcentaje;
  @DecimalMin(value = "0", message = "{mensaje_producto_gananciaNeto_negativo}")
  private BigDecimal gananciaNeto;
  @DecimalMin(value = "0", message = "{mensaje_producto_venta_publico_negativo}")
  private BigDecimal precioVentaPublico;
  @DecimalMin(value = "0", message = "{mensaje_producto_IVAPorcentaje_negativo}")
  private BigDecimal ivaPorcentaje;
  @DecimalMin(value = "0", message = "{mensaje_producto_IVANeto_negativo}")
  private BigDecimal ivaNeto;
  private boolean oferta;
  private byte[] imagen;
  @DecimalMax(value = "100", inclusive = false, message = "{mensaje_producto_oferta_superior_100}")
  private BigDecimal porcentajeBonificacionOferta;
  @DecimalMax(value = "100", inclusive = false, message = "{mensaje_producto_bonificacion_superior_100}")
  private BigDecimal porcentajeBonificacionPrecio;
  @DecimalMin(value = "0", message = "{mensaje_producto_precioLista_negativo}")
  private BigDecimal precioLista;
  private boolean ilimitado;
  private boolean publico;
  private LocalDateTime fechaUltimaModificacion;
  private String estanteria;
  private String estante;
  private String nota;
  private LocalDate fechaVencimiento;
}
