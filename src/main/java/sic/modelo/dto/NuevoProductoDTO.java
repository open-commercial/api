package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private String descripcion;
  private Map<Long,BigDecimal> cantidadEnSucursal;
  private boolean hayStock;
  private BigDecimal cantMinima;
  private BigDecimal bulto;
  private BigDecimal precioCosto;
  private BigDecimal gananciaPorcentaje;
  private BigDecimal gananciaNeto;
  private BigDecimal precioVentaPublico;
  private BigDecimal ivaPorcentaje;
  private BigDecimal ivaNeto;
  private BigDecimal precioBonificado;
  private BigDecimal porcentajeBonificacionPrecio;
  private BigDecimal precioLista;
  private boolean ilimitado;
  private boolean publico;
  private boolean oferta;
  private LocalDateTime fechaUltimaModificacion;
  private String estanteria;
  private String estante;
  private String nota;
  private LocalDate fechaVencimiento;
  private boolean eliminado;
}
